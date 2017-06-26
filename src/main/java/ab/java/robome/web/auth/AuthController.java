package ab.java.robome.web.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.mindrot.jbcrypt.BCrypt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import ab.java.robome.common.time.TimeUtils;
import ab.java.robome.domain.user.UserService;
import ab.java.robome.domain.user.model.ImmutableUser;
import ab.java.robome.domain.user.model.User;
import ab.java.robome.web.auth.model.Login;
import ab.java.robome.web.auth.model.Register;
import ab.java.robome.web.common.AbstractController;
import ab.java.robome.web.common.validation.ValidationError;
import akka.Done;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.Route;

public class AuthController extends AbstractController {

	public static final String PATH = "auth";
	
	private UserService userService;
	
	@Inject
	public AuthController(UserService userService, ObjectMapper objectMapper) {
		super(objectMapper);
		this.userService = userService;
	}
	

	public Route createRoute() { 
		return route(
				post(() -> pathPrefix(PATH, 
						() -> pathPrefix("register", 
								() -> pathEndOrSingleSlash(
										() ->  entity(Jackson.unmarshaller(Register.class), this::registerUser))))),
				post(() -> pathPrefix(PATH, 
						() -> pathPrefix("login", 
								() -> pathEndOrSingleSlash(
										() ->  entity(Jackson.unmarshaller(Login.class), this::loginUser)))))
				);
	}

	private Route loginUser(Login login) {
		
		Location locationHeader = Location.create("/" + PATH + "/" );
		
		System.out.println("LOGIN USER: " + login);
		
		HttpResponse response = HttpResponse.create()
				.withStatus(StatusCodes.CREATED)
				.addHeader(locationHeader);

		CompletionStage<Done> futureSaved = CompletableFuture.completedFuture(Done.getInstance());
		return onSuccess(() -> futureSaved, done -> complete(response));
		
	}
	
	private Route registerUser(Register register) {
		
		List<ValidationError> validationErrors = new RegisterValidator().validate(register);
		if (!validationErrors.isEmpty()) {
			 HttpResponse response = HttpResponse.create()
					 .withStatus(StatusCodes.BAD_REQUEST)
					 .withEntity(ContentTypes.APPLICATION_JSON, toBytes(validationErrors));
			 return complete(response);
		}
		
		Location locationHeader = Location.create("/" + PATH + "/" + "login" );
		
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
