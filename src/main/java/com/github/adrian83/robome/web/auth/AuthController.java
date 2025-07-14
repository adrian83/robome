package com.github.adrian83.robome.web.auth;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.auth.AuthTokenService;
import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.auth.model.command.LoginCommand;
import com.github.adrian83.robome.auth.model.command.RefreshTokenCommand;
import com.github.adrian83.robome.auth.model.command.RegisterCommand;
import static com.github.adrian83.robome.common.function.Functions.use;
import com.github.adrian83.robome.common.validation.Validation;
import com.github.adrian83.robome.web.auth.model.Login;
import com.github.adrian83.robome.web.auth.model.Register;
import com.github.adrian83.robome.web.common.PathParams;
import com.github.adrian83.robome.web.common.Response;
import com.github.adrian83.robome.web.common.Security;
import com.github.adrian83.robome.web.common.http.HttpMethod;
import com.github.adrian83.robome.web.common.routes.RouteSupplier;
import com.google.inject.Inject;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class AuthController extends AllDirectives implements PathParams {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private static final String LOGIN_PATH = "/api/v1/auth/login/";
    private static final String REGISTER_PATH = "/api/v1/auth/register/";
    private static final String REFRESH_PATH = "/api/v1/auth/refresh/";
    private static final String LOGOUT_PATH = "/api/v1/auth/logout/";

    private static final String LOG_LOGIN = "Logging in: {}";
    private static final String LOG_REGISTER = "Registering: {}";
    private static final String LOG_REFRESH = "Refreshing token: {}";
    private static final String LOG_LOGOUT = "Logging out: {}";

    private final Response response;
    private final Security security;
    private final Authentication authentication;
    private final AuthTokenService refreshTokenService;

    @Inject
    public AuthController(Authentication authentication, Response response, Security security, AuthTokenService refreshTokenService) {
        this.authentication = authentication;
        this.security = security;
        this.response = response;
        this.refreshTokenService = refreshTokenService;
    }

    public Route createRoute() {
        return route(
                post(new RouteSupplier(LOGIN_PATH, (pathParams) -> security.unsecured(Login.class, this::loginUser))),
                post(new RouteSupplier(REGISTER_PATH, (pathParams) -> security.unsecured(Register.class, this::registerUser))),
                post(new RouteSupplier(REFRESH_PATH, (pathParams) -> security.unsecured(RefreshTokenCommand.class, this::refreshTokens))),
                post(new RouteSupplier(LOGOUT_PATH, (pathParams) -> security.secured(this::logoutUser))),
                options(new RouteSupplier(LOGIN_PATH, (pathParams) -> complete(response.response200(HttpMethod.POST)))),
                options(new RouteSupplier(REGISTER_PATH, (pathParams) -> complete(response.response200(HttpMethod.POST)))),
                options(new RouteSupplier(REFRESH_PATH, (pathParams) -> complete(response.response200(HttpMethod.POST)))),
                options(new RouteSupplier(LOGOUT_PATH, (pathParams) -> complete(response.response200(HttpMethod.POST))))
        );
    }

    private CompletionStage<HttpResponse> loginUser(Login login) {
        var cLog = use((Login form) -> LOGGER.info(LOG_LOGIN, form));
        return CompletableFuture.completedFuture(login)
                .thenApply(cLog::apply)
                .thenApply(Validation::validate)
                .thenApply(v -> toLoginRequest(login))
                .thenCompose(authentication::loginUser)
                .thenCompose(userData -> {
                    String accessToken = authentication.createAuthToken(userData);
                    return refreshTokenService.createTokens(userData)
                            .thenApply(tokens -> security.createAuthHeader(accessToken));
                })
                .thenApply(response::response200);
    }

    private CompletionStage<HttpResponse> registerUser(Register register) {
        var cLog = use((Register form) -> LOGGER.info(LOG_REGISTER, form));
        return CompletableFuture.completedFuture(register)
                .thenApply(cLog::apply)
                .thenApply(Validation::validate)
                .thenApply(v -> toRegisterCommand(register))
                .thenCompose(authentication::registerUser)
                .thenApply(done -> response.response201());
    }

    private CompletionStage<HttpResponse> refreshTokens(RefreshTokenCommand cmd) {
        var cLog = use((RefreshTokenCommand r) -> LOGGER.info(LOG_REFRESH, r));
        return CompletableFuture.completedFuture(cmd)
                .thenApply(cLog::apply)
                .thenApply(Validation::validate)
                .thenCompose(v -> refreshTokenService.refreshTokens(cmd.refreshToken()))
                .thenApply(tokens -> security.createAuthHeader(tokens.accessToken()))
                .thenApply(response::response200);
    }

    private CompletionStage<HttpResponse> logoutUser(UserData user) {
        var cLog = use((UserData u) -> LOGGER.info(LOG_LOGOUT, u));
        return CompletableFuture.completedFuture(user)
                .thenApply(cLog::apply)
                .thenCompose(u -> refreshTokenService.revokeUserTokens(u.id()))
                .thenApply(v -> response.response204());
    }

    private LoginCommand toLoginRequest(Login form) {
        return new LoginCommand(
                form.email(),
                form.password()
        );
    }

    private RegisterCommand toRegisterCommand(Register form) {
        return new RegisterCommand(
                form.email(),
                form.password()
        );
    }
}
