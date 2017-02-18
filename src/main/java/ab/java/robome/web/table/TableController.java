package ab.java.robome.web.table;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.google.inject.Inject;

import ab.java.robome.table.TableService;
import ab.java.robome.table.model.NewTable;
import ab.java.robome.table.model.Table;
import akka.Done;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;

public class TableController extends AllDirectives {

	private TableService tableService;
	private ActorMaterializer actorMaterializer;

	@Inject
	public TableController(TableService tableService, ActorMaterializer actorMaterializer) {
		this.tableService = tableService;
		this.actorMaterializer = actorMaterializer;
	}

	public Route createRoute() {

		return route(get(() -> pathPrefix("tables", () -> path(segment(), (String id) -> {


			final CompletionStage<Optional<Table>> futureMaybeTable = tableService
					.getTable(id)
					.runWith(Sink.head(), actorMaterializer);
			
			return onSuccess(() -> futureMaybeTable,
					
					maybeItem -> maybeItem
					.map(item -> completeOK(item, Jackson.marshaller()))
					.orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found")));
		}))), 
				post(() -> path("tables", () -> entity(Jackson.unmarshaller(NewTable.class), table -> {
			CompletionStage<Done> futureSaved = saveNewTable(table);
			return onSuccess(() -> futureSaved, done -> complete("new table created"));
		}))));
	}


	private CompletionStage<Done> saveNewTable(final NewTable order) {
		return CompletableFuture.completedFuture(Done.getInstance());
	}

}
