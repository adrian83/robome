package ab.java.robome.web.auth;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import ab.java.robome.web.common.response.Cors;
import ab.java.robome.web.common.response.Options;
import ab.java.robome.web.common.validation.ValidationError;
import ab.java.robome.web.security.SecurityUtils;
import akka.Done;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.Route;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;


public class AuthController extends AbstractController {

	public static final String AUTH = "auth";
	public static final String LOGIN = "login";
	public static final String REGISTER = "register";
		
	private UserService userService;
	private SecurityUtils securityUtils;
	
	@Inject
	public AuthController(UserService userService, SecurityUtils securityUtils, Config config, ObjectMapper objectMapper) {
		super(objectMapper, config);
		this.userService = userService;
		this.securityUtils = securityUtils;
	}
	

	public Route createRoute() { 
		return route(
				options(() -> pathPrefix(AUTH, () -> pathPrefix(REGISTER, () -> pathEndOrSingleSlash(this::handleRegisterOptions)))),
				options(() -> pathPrefix(AUTH, () -> pathPrefix(LOGIN, () -> pathEndOrSingleSlash(this::handleLoginOptions)))),

				post(() -> pathPrefix(AUTH, 
						() -> pathPrefix(REGISTER, 
								() -> pathEndOrSingleSlash(
										() -> entity(Jackson.unmarshaller(RegisterForm.class), this::registerUser))))),
				post(() -> pathPrefix(AUTH, 
						() -> pathPrefix(LOGIN, 
								() -> pathEndOrSingleSlash(
										() ->  entity(Jackson.unmarshaller(LoginForm.class), this::loginUser)))))
				);
	}
	
	private Route handleLoginOptions() {
		return complete(new Options().withHeaders(JWT_TOKEN , "Content-Type").withMethods("POST").response());
	}
	
	private Route handleRegisterOptions() {
		return complete(new Options().withHeaders(JWT_TOKEN , "Content-Type").withMethods("POST").response());
	}

	private Route loginUser(LoginForm login) {
		
		List<ValidationError> validationErrors = login.validate(config);
		if (!validationErrors.isEmpty()) {
			return onValidationErrors(validationErrors);
		}
		
		Location locationHeader = Location.create("/" + AUTH + "/" );
		
		System.out.println("LOGIN USER: " + login);
		
		Key key = securityUtils.getSecurityKey();
		

		 CompletionStage<Optional<User>> futureUser = userService.findUserByEmail(login.email());
		 
		 CompletionStage<HttpResponse> futureResponse = futureUser.thenApply(maybeUser -> maybeUser.map(user -> {
			 if(BCrypt.checkpw(login.password(), user.passwordHash())) {
				 
				 Map<String, Object> claims = new HashMap<>();
				 claims.put("user_email", user.email());
				 
					String compactJws = Jwts.builder()
							  .setSubject("Joe")
							  .setClaims(claims)
							  .signWith(SignatureAlgorithm.HS512, key)
							  .compact();
					
					System.out.println("JWS: " + compactJws);
					
					return HttpResponse.create()
							.withStatus(StatusCodes.OK)
							.addHeaders(headers(
									locationHeader, 
									jwt(compactJws), 
									Cors.origin("*"), 
									Cors.methods("POST"), 
									Cors.headers(JWT_TOKEN , "Content-Type"), 
									json()));
			 } else {
				 return HttpResponse.create()
						 .withStatus(StatusCodes.NOT_FOUND)
						 .addHeaders(headers(
									Cors.origin("*"), 
									Cors.methods("POST"), 
									Cors.headers(JWT_TOKEN , "Content-Type"), 
								 json()));
			 }
		 }).orElse(HttpResponse.create().withStatus(StatusCodes.NOT_FOUND).addHeaders(headers(
					Cors.origin("*"), 
					Cors.methods("POST"), 
					Cors.headers(JWT_TOKEN , "Content-Type"), 
					json()))));
		 
		return completeWithFuture(futureResponse);
		
	}
	
	private Route registerUser(RegisterForm register) {
		
		List<ValidationError> validationErrors = register.validate(config);
		if (!validationErrors.isEmpty()) {
			return onValidationErrors(validationErrors);
		}
		
		Location locationHeader = locationFor(AUTH, LOGIN);
		
		System.out.println("REGISTER USER: " + register);
		LocalDateTime utcNow = TimeUtils.utcNow();
		String hashedPassword = BCrypt.hashpw(register.password(), BCrypt.gensalt());

		
		HttpResponse response = HttpResponse.create()
				.withStatus(StatusCodes.CREATED)
				.addHeader(locationHeader);
		
		User user = ImmutableUser.builder()
				.email(register.email())
				.passwordHash(hashedPassword)
				.createdAt(utcNow)
				.modifiedAt(utcNow)
				.build();

		CompletionStage<Done> futureSaved = userService.saveUser(user);
		return onSuccess(() -> futureSaved, done -> complete(response));
		
	}
}
