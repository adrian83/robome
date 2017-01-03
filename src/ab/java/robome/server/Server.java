package ab.java.robome.server;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpEntity.Chunked;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.ContentType;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;
import akka.util.ByteString;

import java.io.IOException;
import java.util.concurrent.CompletionStage;


public class Server extends AllDirectives  {

	public static void main(String[] args) throws IOException {


	    final ActorSystem system = ActorSystem.create();
	    final ActorMaterializer materializer = ActorMaterializer.create(system);

	    // HttpApp.bindRoute expects a route being provided by HttpApp.createRoute
	    final Server app = new Server();
	    final Route route = app.createRoute();

	    final Flow<HttpRequest, HttpResponse, NotUsed> handler = route.flow(system, materializer);
	    final CompletionStage<ServerBinding> binding = Http.get(system).bindAndHandle(handler, ConnectHttp.toHost("127.0.0.1", 8080), materializer);

	    binding.exceptionally(failure -> {
	      System.err.println("Something very bad happened! " + failure.getMessage());
	      system.terminate();
	      return null;
	    });

	    system.terminate();
		
		

	}

	public Route createRoute() {
		return route(
				get( () -> pathEndOrSingleSlash(() -> complete(index()))),
				get( () -> path("ping", () -> complete(index())))
			
				
			);
	}

	private HttpResponse index() {

		final Source<ByteString, Object> data = Source.single("<html><body><h1>Hello Akka HTTP</h1></body></html>")
				.map(ByteString::fromString).mapMaterializedValue(s -> null);

		final ContentType ct = ContentTypes.TEXT_HTML_UTF8;
		final Chunked chunked = HttpEntities.create(ct, data);
		return HttpResponse.create().withEntity(chunked);
	}

}