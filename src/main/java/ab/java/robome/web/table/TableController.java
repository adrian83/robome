package ab.java.robome.web.table;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import ab.java.robome.common.time.TimeUtils;
import ab.java.robome.table.TableService;
import ab.java.robome.table.model.ImmutableTable;
import ab.java.robome.table.model.NewTable;
import ab.java.robome.table.model.Table;
import ab.java.robome.table.model.TableState;
import ab.java.robome.web.common.AbstractController;
import akka.Done;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.Route;

public class TableController extends AbstractController {
	
	public static final String PATH = "tables";

	private TableService tableService;
	private ObjectMapper objectMapper;

	@Inject
	public TableController(TableService tableService, ObjectMapper objectMapper) {
		this.tableService = tableService;
		this.objectMapper = objectMapper;
	}

	public Route createRoute() {

		return route(
				get(() -> pathPrefix(PATH, () -> path(segment(), this::getTableById))), 
				post(() -> path(PATH, () -> entity(Jackson.unmarshaller(NewTable.class), this::persistTable)))
				);
	}

	private Route getTableById(String tableId) {
		final CompletionStage<Optional<Table>> futureMaybeTable = tableService.getTable(tableId);
		
		return onSuccess(() -> futureMaybeTable, maybeItem -> maybeItem.map(item -> completeOK(item, Jackson.marshaller(objectMapper)))
				.orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found")));
	}
	
	private Route persistTable(NewTable newTable) {
		

		LocalDateTime utcNow = TimeUtils.utcNow();
		UUID id = UUID.randomUUID();
		
		Location locationHeader = Location.create("/" + TableController.PATH + "/" + id.toString());

		
		Table table = ImmutableTable.builder()
				.id(id)
				.name(newTable.getName())
				.state(TableState.ACTIVE)
				.createdAt(utcNow)
				.modifiedAt(utcNow)
				.build();
		
		HttpResponse response = HttpResponse.create().withStatus(StatusCodes.CREATED).addHeader(locationHeader);

		CompletionStage<Done> futureSaved = tableService.saveTable(table);
		return onSuccess(() -> futureSaved, done -> complete(response));
		
	}
	
}
