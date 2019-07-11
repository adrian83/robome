package com.github.adrian83.robome.domain.user;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.mindrot.jbcrypt.BCrypt;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.auth.Role;
import com.github.adrian83.robome.common.time.TimeUtils;
import com.github.adrian83.robome.common.web.AbstractController;
import com.github.adrian83.robome.common.web.ExceptionHandler;
import com.github.adrian83.robome.common.web.Response;
import com.github.adrian83.robome.common.web.Validation;
import com.github.adrian83.robome.domain.user.model.Login;
import com.github.adrian83.robome.domain.user.model.Register;
import com.github.adrian83.robome.util.http.Cors;
import com.github.adrian83.robome.util.http.Header;
import com.github.adrian83.robome.util.http.HttpMethod;
import com.github.adrian83.robome.util.http.Options;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;

public class UserController extends AbstractController {

	public static final String AUTH = "auth";
	public static final String LOGIN = "login";
	public static final String REGISTER = "register";

	private UserService userService;

	@Inject
	public UserController(UserService userService, JwtAuthorizer jwtAuthorizer, Config config,
			ExceptionHandler exceptionHandler, Response responseProducer) {
		super(jwtAuthorizer, exceptionHandler, config, responseProducer);
		this.userService = userService;
	}

	public Route createRoute() {
		return route(
				options(() -> pathPrefix(AUTH,
						() -> pathPrefix(REGISTER, () -> pathEndOrSingleSlash(this::handleRegisterOptions)))),

				options(() -> pathPrefix(AUTH,
						() -> pathPrefix(LOGIN, () -> pathEndOrSingleSlash(this::handleLoginOptions)))),

				post(() -> pathPrefix(AUTH,
						() -> pathPrefix(REGISTER,
								() -> pathEndOrSingleSlash(
										() -> entity(Jackson.unmarshaller(Register.class), this::registerUser))))),

				post(() -> pathPrefix(AUTH, () -> pathPrefix(LOGIN, () -> pathEndOrSingleSlash(
						() -> entity(Jackson.unmarshaller(Login.class), this::loginUser))))));
	}

	private Route loginUser(Login login) {

		CompletableFuture<HttpResponse> futureResponse = CompletableFuture.completedFuture(login)
				.thenApply(form -> Validation.validate(form))
				.thenCompose(form -> userService.findUserByEmail(form.getEmail()))
				.thenApply(maybeUser -> maybeUser.map(user -> {
					if (Authentication.passwordEqual(login.getPassword(), user.getPasswordHash())) {

						return HttpResponse.create().withStatus(StatusCodes.OK)
								.addHeaders(headers(jwt(jwtAuthorizer.createAuthorizationToken(user)),
										Cors.origin(corsOrigin()),
										Cors.exposeHeaders(Header.AUTHORIZATION.getText())));

					} else {
						return responseProducer.response404();
					}
				}).orElse(responseProducer.response404()));

		return completeWithFuture(futureResponse);

	}

	private Route registerUser(Register register) {

		CompletableFuture<HttpResponse> result = CompletableFuture.completedFuture(register)
				.thenApply(form -> Validation.validate(form)).thenCompose(form -> {
					LocalDateTime utcNow = TimeUtils.utcNow();
					String hashedPassword = BCrypt.hashpw(register.getPassword(), BCrypt.gensalt());

					User user = new User(UUID.randomUUID(), register.getEmail(), hashedPassword,
							Role.DEFAULT_USER_ROLES, utcNow, utcNow);

					return userService.saveUser(user);
				}).thenApply(done -> HttpResponse.create().withStatus(StatusCodes.CREATED)
						.addHeader(Cors.origin(corsOrigin())));

		return onSuccess(() -> result, response -> complete(response));

	}

	private Route handleLoginOptions() {
		HttpResponse response = new Options()
				.withHeaders(Header.AUTHORIZATION.getText(), Header.CONTENT_TYPE.getText())
				.withMethods(HttpMethod.POST.name()).withOrigin(corsOrigin()).response();

		return complete(response);
	}

	private Route handleRegisterOptions() {
		HttpResponse response = new Options()
				.withHeaders(Header.AUTHORIZATION.getText(), Header.CONTENT_TYPE.getText())
				.withMethods(HttpMethod.POST.name()).withOrigin(corsOrigin()).response();

		return complete(response);
	}

}
