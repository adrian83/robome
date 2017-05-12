package ab.java.robome.web.auth;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import ab.java.robome.auth.model.NewUser;
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
						() -> pathEndOrSingleSlash(
								() ->  entity(Jackson.unmarshaller(NewUser.class), this::registerUser))))
				);
	}

	private Route registerUser(NewUser newUser) {
		
		Location locationHeader = Location.create("/" + PATH + "/" );
		
		System.out.println("USER: " + newUser);
		
		HttpResponse response = HttpResponse.create()
				.withStatus(StatusCodes.CREATED)
				.addHeader(locationHeader);

		CompletionStage<Done> futureSaved = CompletableFuture.completedFuture(Done.getInstance());
		return onSuccess(() -> futureSaved, done -> complete(response));
		
	}
}
