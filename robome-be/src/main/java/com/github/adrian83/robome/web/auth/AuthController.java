package com.github.adrian83.robome.web.auth;

import static com.github.adrian83.robome.common.function.Functions.use;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.model.command.LoginCommand;
import com.github.adrian83.robome.auth.model.command.RegisterCommand;
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

public class AuthController extends AllDirectives {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

	private static final String LOGIN_PATH = "/auth/login/";
	private static final String REGISTER_PATH = "/auth/register/";

	private static final String LOG_LOGIN = "Logging in: {}";
	private static final String LOG_REGISTER = "Registering: {}";

	private Response response;
	private Security security;
	private Authentication authentication;

	private final FormRoute<Login> loggingRoute = new FormRoute<Login>(LOGIN_PATH, Login.class,
			(clz) -> security.unsecured(clz, this::loginUser));

	private final FormRoute<Register> registeringRoute = new FormRoute<Register>(REGISTER_PATH, Register.class,
			(clz) -> security.unsecured(clz, this::registerUser));

	@Inject
	public AuthController(Authentication authentication, Response response, Security security) {
		this.authentication = authentication;
		this.security = security;
		this.response = response;
	}

	public Route createRoute() {
		return route(post(loggingRoute),
				options(new PrefixRoute(LOGIN_PATH, complete(response.response200(HttpMethod.POST)))),
				post(registeringRoute),
				options(new PrefixRoute(REGISTER_PATH, complete(response.response200(HttpMethod.POST)))));
	}

	private CompletionStage<HttpResponse> loginUser(Login login) {
		var cLog = use((Login form) -> LOGGER.info(LOG_LOGIN, form));
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
		var cLog = use((Register form) -> LOGGER.info(LOG_REGISTER, form));
		return CompletableFuture.completedFuture(register)
				.thenApply(cLog::apply)
				.thenApply(Validation::validate)
				.thenApply(v -> toRegisterCommand(register))
				.thenCompose(authentication::registerUser)
				.thenApply(done -> response.response201());
	}

	private LoginCommand toLoginRequest(Login form) {
		return new LoginCommand(form.email(), form.password());
	}

	private RegisterCommand toRegisterCommand(Register form) {
		return new RegisterCommand(form.email(), form.password());
	}
}
