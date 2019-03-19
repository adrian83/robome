package com.github.adrian83.robome.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.mindrot.jbcrypt.BCrypt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.adrian83.robome.common.time.TimeUtils;
import com.github.adrian83.robome.common.web.AbstractController;
import com.github.adrian83.robome.common.web.ValidationError;
import com.github.adrian83.robome.domain.user.User;
import com.github.adrian83.robome.domain.user.UserService;
import com.github.adrian83.robome.util.http.Cors;
import com.github.adrian83.robome.util.http.HttpHeader;
import com.github.adrian83.robome.util.http.HttpMethod;
import com.github.adrian83.robome.util.http.Options;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.Done;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;

public class AuthController extends AbstractController {

	public static final String AUTH = "auth";
	public static final String LOGIN = "login";
	public static final String REGISTER = "register";

	private UserService userService;

	@Inject
	public AuthController(UserService userService, JwtAuthorizer jwtAuthorizer, Config config,
			ObjectMapper objectMapper) {
		super(jwtAuthorizer, objectMapper, config);
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
										() -> entity(Jackson.unmarshaller(RegisterForm.class), this::registerUser))))),
				post(() -> pathPrefix(AUTH, () -> pathPrefix(LOGIN, () -> pathEndOrSingleSlash(
						() -> entity(Jackson.unmarshaller(LoginForm.class), this::loginUser))))));
	}

	private Route loginUser(LoginForm login) {

		List<ValidationError> validationErrors = login.validate(config);
		if (!validationErrors.isEmpty()) {
			return complete(response400(validationErrors));
		}

		CompletionStage<Optional<User>> futureUser = userService.findUserByEmail(login.getEmail());

		CompletionStage<HttpResponse> futureResponse = futureUser.thenApply(maybeUser -> maybeUser.map(user -> {
			if (BCrypt.checkpw(login.getPassword(), user.getPasswordHash())) {

				return HttpResponse.create().withStatus(StatusCodes.OK)
						.addHeaders(headers(jwt(jwtAuthorizer.createAuthorizationToken(user)),
								Cors.origin(corsOrigin()), Cors.exposeHeaders(HttpHeader.AUTHORIZATION.getText())));

			} else {
				return response404();
			}
		}).orElse(response404()));

		return completeWithFuture(futureResponse);

	}

	private Route registerUser(RegisterForm register) {

		List<ValidationError> validationErrors = register.validate(config);
		if (!validationErrors.isEmpty()) {
			return complete(response400(validationErrors));
		}

		LocalDateTime utcNow = TimeUtils.utcNow();
		String hashedPassword = BCrypt.hashpw(register.getPassword(), BCrypt.gensalt());

		User user = new User(UUID.randomUUID(), register.getEmail(), hashedPassword, utcNow, utcNow);

		HttpResponse response = HttpResponse.create().withStatus(StatusCodes.CREATED)
				.addHeader(Cors.origin(corsOrigin()));

		CompletionStage<Done> futureSaved = userService.saveUser(user);
		return onSuccess(() -> futureSaved, done -> complete(response));

	}

	private Route handleLoginOptions() {
		HttpResponse response = new Options()
				.withHeaders(HttpHeader.AUTHORIZATION.getText(), HttpHeader.CONTENT_TYPE.getText())
				.withMethods(HttpMethod.POST.name()).withOrigin(corsOrigin()).response();

		return complete(response);
	}

	private Route handleRegisterOptions() {
		HttpResponse response = new Options()
				.withHeaders(HttpHeader.AUTHORIZATION.getText(), HttpHeader.CONTENT_TYPE.getText())
				.withMethods(HttpMethod.POST.name()).withOrigin(corsOrigin()).response();

		return complete(response);
	}

}
