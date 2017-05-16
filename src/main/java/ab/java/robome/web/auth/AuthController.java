package ab.java.robome.web.auth;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import ab.java.robome.web.auth.model.Login;
import ab.java.robome.web.auth.model.Register;
import ab.java.robome.web.common.AbstractController;
import akka.Done;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.Route;

public class AuthController extends AbstractController {

	public static final String PATH = "auth";
	

	
	@Inject
	public AuthController(ObjectMapper objectMapper) {
		super(objectMapper);
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
		
		Location locationHeader = Location.create("/" + PATH + "/" );
		
		System.out.println("REGISTER USER: " + register);
		
		HttpResponse response = HttpResponse.create()
				.withStatus(StatusCodes.CREATED)
				.addHeader(locationHeader);

		CompletionStage<Done> futureSaved = CompletableFuture.completedFuture(Done.getInstance());
		return onSuccess(() -> futureSaved, done -> complete(response));
		
	}
}
