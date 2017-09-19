package ab.java.robome.web.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;


import org.mindrot.jbcrypt.BCrypt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import ab.java.robome.common.time.TimeUtils;
import ab.java.robome.domain.user.UserService;
import ab.java.robome.domain.user.model.ImmutableUser;
import ab.java.robome.domain.user.model.User;
import ab.java.robome.web.auth.model.LoginForm;
import ab.java.robome.web.auth.model.RegisterForm;
import ab.java.robome.web.common.AbstractController;
import ab.java.robome.web.common.HttpHeader;
import ab.java.robome.web.common.HttpMethod;
import ab.java.robome.web.common.response.Cors;
import ab.java.robome.web.common.response.Options;
import ab.java.robome.web.common.validation.ValidationError;
import ab.java.robome.web.security.SecurityUtils;
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
	public AuthController(UserService userService, SecurityUtils securityUtils, 
			Config config, ObjectMapper objectMapper) {
		super(securityUtils, objectMapper, config);
		this.userService = userService;
	}
	

	public Route createRoute() { 
		return route(
				options(() -> pathPrefix(AUTH, () -> pathPrefix(REGISTER, () -> pathEndOrSingleSlash(this::handleRegisterOptions)))),
				options(() -> pathPrefix(AUTH, () -> pathPrefix(LOGIN, () -> pathEndOrSingleSlash(this::handleLoginOptions)))),

				post(() -> pathPrefix(
						AUTH, 
						() -> pathPrefix(
								REGISTER, 
								() -> pathEndOrSingleSlash(
										() -> entity(Jackson.unmarshaller(RegisterForm.class), this::registerUser))))),
				post(() -> pathPrefix(
						AUTH, 
						() -> pathPrefix(
								LOGIN, 
								() -> pathEndOrSingleSlash(
										() ->  entity(Jackson.unmarshaller(LoginForm.class), this::loginUser)))))
				);
	}
	


	private Route loginUser(LoginForm login) {
		
		List<ValidationError> validationErrors = login.validate(config);
		if (!validationErrors.isEmpty()) {
			return complete(response400(validationErrors));
		}
		

		CompletionStage<Optional<User>> futureUser = userService.findUserByEmail(login.email());
		 
		CompletionStage<HttpResponse> futureResponse = futureUser.thenApply(maybeUser -> maybeUser.map(user -> {
			if(BCrypt.checkpw(login.password(), user.passwordHash())) {
			
				return HttpResponse.create()
						.withStatus(StatusCodes.OK)
						.addHeaders(headers(
								jwt(securityUtils.createAuthorizationToken(user)), 
								Cors.origin(corsOrigin())));
				
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
		String hashedPassword = BCrypt.hashpw(register.password(), BCrypt.gensalt());

		User user = ImmutableUser.builder()
				.id(UUID.randomUUID())
				.email(register.email())
				.passwordHash(hashedPassword)
				.createdAt(utcNow)
				.modifiedAt(utcNow)
				.build();

		
		HttpResponse response = HttpResponse.create()
				.withStatus(StatusCodes.CREATED)
				 .addHeaders(
						 headers(
								 Cors.origin(corsOrigin()), 
								 Cors.methods(HttpMethod.POST.name()), 
								 Cors.allowHeaders(
										 HttpHeader.AUTHORIZATION.getText(), 
										 HttpHeader.CONTENT_TYPE.getText())));

		CompletionStage<Done> futureSaved = userService.saveUser(user);
		return onSuccess(() -> futureSaved, done -> complete(response));
		
	}
	
	private Route handleLoginOptions() {
		HttpResponse response = new Options()
				.withHeaders(
						HttpHeader.AUTHORIZATION.getText(), 
						HttpHeader.CONTENT_TYPE.getText())
				.withMethods(HttpMethod.POST.name())
				.withOrigin(corsOrigin())
				.response();
		
		return complete(response);
	}
	
	private Route handleRegisterOptions() {
		HttpResponse response = new Options()
				.withHeaders(
						HttpHeader.AUTHORIZATION.getText(), 
						HttpHeader.CONTENT_TYPE.getText())
				.withMethods(HttpMethod.POST.name())
				.withOrigin(corsOrigin())
				.response();
		
		return complete(response);
	}
	
}
