package com.github.adrian83.robome.web.auth;

import static com.github.adrian83.robome.common.function.Functions.use;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.model.command.LoginRequest;
import com.github.adrian83.robome.auth.model.command.RegisterRequest;
import com.github.adrian83.robome.common.validation.Validation;
import com.github.adrian83.robome.web.auth.model.Login;
import com.github.adrian83.robome.web.auth.model.Register;
import com.github.adrian83.robome.web.common.Response;
import com.github.adrian83.robome.web.common.Security;
import com.github.adrian83.robome.web.common.http.HttpMethod;
import com.github.adrian83.robome.web.common.routes.FormRoute;
import com.github.adrian83.robome.web.common.routes.PrefixRoute;
import com.google.inject.Inject;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthController extends AllDirectives {

  private static final String LOGIN_PATH = "/auth/login/";
  private static final String REGISTER_PATH = "/auth/register/";

  private static final String LOG_LOGIN = "Loging in: {}";
  private static final String LOG_REGISTER = "Registering: {}";

  private Response response;
  private Security security;
  private Authentication authentication;

  @Inject
  public AuthController(Authentication authentication, Response response, Security security) {
    this.authentication = authentication;
    this.security = security;
    this.response = response;
  }

  public Route createRoute() {
    return route(
        post(
            new FormRoute<Login>(
                LOGIN_PATH, Login.class, (clz) -> security.unsecured(clz, this::loginUser))),
        options(new PrefixRoute(LOGIN_PATH, complete(response.response200(HttpMethod.POST)))),
        post(
            new FormRoute<Register>(
                REGISTER_PATH,
                Register.class,
                (clz) -> security.unsecured(clz, this::registerUser))),
        options(new PrefixRoute(REGISTER_PATH, complete(response.response200(HttpMethod.POST)))));
  }

  private CompletionStage<HttpResponse> loginUser(Login login) {

    var cLog = use((Login form) -> log.info(LOG_LOGIN, form));

    return CompletableFuture.completedFuture(login)
        .thenApply(cLog::apply)
        .thenApply(Validation::validate)
        .thenApply(v -> toLoginRequest(login))
        .thenCompose(authentication::findUserWithPassword)
        .thenApply(authentication::createAuthToken)
        .thenApply(security::createAuthHeader)
        .thenApply(response::response200);
  }

  private CompletionStage<HttpResponse> registerUser(Register register) {

    var cLog = use((Register form) -> log.info(LOG_REGISTER, form));

    return CompletableFuture.completedFuture(register)
        .thenApply(cLog::apply)
        .thenApply(Validation::validate)
        .thenApply(v -> toRegisterRequest(register))
        .thenCompose(authentication::registerUser)
        .thenApply(done -> response.response201());
  }

  private LoginRequest toLoginRequest(Login form) {
    return LoginRequest.builder().email(form.getEmail()).password(form.getPassword()).build();
  }

  private RegisterRequest toRegisterRequest(Register form) {
    return RegisterRequest.builder().email(form.getEmail()).password(form.getPassword()).build();
  }
}
