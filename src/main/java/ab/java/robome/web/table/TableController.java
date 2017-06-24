package ab.java.robome.web.table;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import ab.java.robome.common.time.TimeUtils;
import ab.java.robome.domain.table.TableService;
import ab.java.robome.domain.table.model.ImmutableTable;
import ab.java.robome.domain.table.model.NewTable;
import ab.java.robome.domain.table.model.Table;
import ab.java.robome.domain.table.model.TableState;
import ab.java.robome.web.common.AbstractController;
import ab.java.robome.web.common.validation.ValidationError;
import akka.Done;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.Route;

public class TableController extends AbstractController {
	
	public static final String PATH = "tables";

	private TableService tableService;

	@Inject
	public TableController(TableService tableService, ObjectMapper objectMapper) {
		super(objectMapper);
		this.tableService = tableService;
	}

	public Route createRoute() { 
		return route(
				get(() -> pathPrefix(PATH, () -> pathEndOrSingleSlash(() -> getTables()))),
				get(() -> pathPrefix(PATH, () -> pathPrefix(segment(), tableId -> pathEndOrSingleSlash(() -> getTableById(tableId))))), 
				post(() -> path(PATH, () -> pathEndOrSingleSlash(() -> entity(Jackson.unmarshaller(NewTable.class), this::persistTable))))
				);
	}

	private Route getTables(){
		final CompletionStage<List<Table>> futureTables = tableService.getTables();
		
		return onSuccess(() -> futureTables, tables -> completeOK(tables, Jackson.marshaller(objectMapper)));
	}
	
	private Route getTableById(String tableId) {
		UUID tableUuid = UUID.fromString(tableId);
		
		final CompletionStage<Optional<Table>> futureMaybeTable = tableService.getTable(tableUuid);
		
		return onSuccess(() -> futureMaybeTable, maybeItem -> maybeItem.map(item -> completeOK(item, Jackson.marshaller(objectMapper)))
				.orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found")));
	}
	
	private Route persistTable(NewTable newTable) {
		
		List<ValidationError> validationErrors = new NewTableValidator().validate(newTable);
		if (!validationErrors.isEmpty()) {
			 HttpResponse response = HttpResponse.create()
					 .withStatus(StatusCodes.BAD_REQUEST)
					 .withEntity(ContentTypes.APPLICATION_JSON, toBytes(validationErrors));
			 return complete(response);
		}
		

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
		
		HttpResponse response = HttpResponse.create()
				.withStatus(StatusCodes.CREATED)
				.addHeader(locationHeader);

		CompletionStage<Done> futureSaved = tableService.saveTable(table);
		return onSuccess(() -> futureSaved, done -> complete(response));
		
	}

	private String toBytes(List<ValidationError> validationErrors) {
		try {
			return objectMapper.writeValueAsString(validationErrors);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
}
