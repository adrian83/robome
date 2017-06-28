package ab.java.robome.web.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
import ab.java.robome.web.common.validation.ValidationError;
import akka.Done;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.Route;

public class AuthController extends AbstractController {

	public static final String AUTH = "auth";
	public static final String LOGIN = "login";
		
	private UserService userService;
	
	@Inject
	public AuthController(UserService userService, Config config, ObjectMapper objectMapper) {
		super(objectMapper, config);
		this.userService = userService;
	}
	

	public Route createRoute() { 
		return route(
				post(() -> pathPrefix(AUTH, 
						() -> pathPrefix("register", 
								() -> pathEndOrSingleSlash(
										() ->  entity(Jackson.unmarshaller(RegisterForm.class), this::registerUser))))),
				post(() -> pathPrefix(AUTH, 
						() -> pathPrefix(LOGIN, 
								() -> pathEndOrSingleSlash(
										() ->  entity(Jackson.unmarshaller(LoginForm.class), this::loginUser)))))
				);
	}

	private Route loginUser(LoginForm login) {
		
		List<ValidationError> validationErrors = login.validate(config);
		if (!validationErrors.isEmpty()) {
			return onValidationErrors(validationErrors);
		}
		
		Location locationHeader = Location.create("/" + AUTH + "/" );
		
		System.out.println("LOGIN USER: " + login);
		
		HttpResponse response = HttpResponse.create()
				.withStatus(StatusCodes.CREATED)
				.addHeader(locationHeader);

		CompletionStage<Done> futureSaved = CompletableFuture.completedFuture(Done.getInstance());
		return onSuccess(() -> futureSaved, done -> complete(response));
		
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
