package ab.java.robome.server;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import ab.java.robome.RobomeModule;
import ab.java.robome.table.model.NewTable;
import ab.java.robome.table.model.Table;
import ab.java.robome.web.table.TableController;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;

import static akka.http.javadsl.server.PathMatchers.longSegment;

public class Server extends AllDirectives {
	
	 

  public static void main(String[] args) throws Exception {
	  
	 Injector injector = Guice.createInjector(new RobomeModule());
	 TableController tableController = injector.getInstance(TableController.class);
	 Config config = injector.getInstance(Config.class);
	  

    ActorSystem system = ActorSystem.create("routes");

    final Http http = Http.get(system);
    final ActorMaterializer materializer = ActorMaterializer.create(system);

    Server app = new Server();

    final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materializer);
    final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
      ConnectHttp.toHost(config.getString("server.host"), config.getInt("server.port")), materializer);


    
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        binding
        .thenCompose(ServerBinding::unbind) 
        .thenAccept(unbound -> system.terminate()); 
    }));


  }


  private CompletionStage<Optional<Table>> fetchTable(long itemId) {
    return CompletableFuture.completedFuture(Optional.of(new Table("foo", itemId)));
  }


  private CompletionStage<Done> saveNewTable(final NewTable order) {
    return CompletableFuture.completedFuture(Done.getInstance());
  }

  private Route createRoute() {

    return route(
      get(() ->
        pathPrefix("tables", () ->
          path(longSegment(), (Long id) -> {
            final CompletionStage<Optional<Table>> futureMaybeTable = fetchTable(id);
            return onSuccess(() -> futureMaybeTable, maybeItem ->
              maybeItem.map(item -> completeOK(item, Jackson.marshaller()))
                .orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found"))
            );
          }))),
      post(() ->
        path("tables", () ->
          entity(Jackson.unmarshaller(NewTable.class), table -> {
            CompletionStage<Done> futureSaved = saveNewTable(table);
            return onSuccess(() -> futureSaved, done ->
              complete("new table created")
            );
          })))
    );
  }

}