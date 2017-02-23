package ab.java.robome.web.table;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.google.inject.Inject;

import ab.java.robome.common.time.UTCUtils;
import ab.java.robome.table.TableService;
import ab.java.robome.table.model.NewTable;
import ab.java.robome.table.model.Table;
import ab.java.robome.table.model.TableState;
import akka.Done;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class TableController extends AllDirectives {
	
	protected static final String PATH = "tables";

	private TableService tableService;

	@Inject
	public TableController(TableService tableService) {
		this.tableService = tableService;
	}

	public Route createRoute() {

		return route(
				get(() -> pathPrefix(PATH, () -> path(segment(), this::getTableById))), 
				post(() -> path(PATH, () -> entity(Jackson.unmarshaller(NewTable.class), this::persistTable)))
				);
	}

	private Route getTableById(String tableId) {
		final CompletionStage<Optional<Table>> futureMaybeTable = tableService.getTable(tableId);
		
		return onSuccess(() -> futureMaybeTable, maybeItem -> maybeItem.map(item -> completeOK(item, Jackson.marshaller()))
				.orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found")));
	}
	
	private Route persistTable(NewTable newTable) {
		

		LocalDateTime utcNow = UTCUtils.utcNow();
		
		Table table = new Table(UUID.randomUUID(), newTable.getName(), TableState.ACTIVE, utcNow, utcNow);
		
		CompletionStage<Done> futureSaved = tableService.saveTable(table);
		return onSuccess(() -> futureSaved, done -> complete("new table created"));
	}
	
}
