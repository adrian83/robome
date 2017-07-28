package ab.java.robome.web.domain.table;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import ab.java.robome.common.time.TimeUtils;
import ab.java.robome.domain.table.TableService;
import ab.java.robome.domain.table.model.ImmutableTable;
import ab.java.robome.domain.table.model.ImmutableTableId;
import ab.java.robome.domain.table.model.Table;
import ab.java.robome.domain.table.model.TableId;
import ab.java.robome.domain.table.model.TableState;
import ab.java.robome.web.common.AbstractController;
import ab.java.robome.web.common.validation.ValidationError;
import ab.java.robome.web.domain.table.model.NewTable;
import ab.java.robome.web.security.SecurityUtils;
import akka.Done;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.Route;

public class TableController extends AbstractController {
	
	public static final String TABLES = "tables";

	private TableService tableService;	
	private SecurityUtils securityUtils;

	@Inject
	public TableController(TableService tableService, SecurityUtils securityUtils, Config config, ObjectMapper objectMapper) {
		super(objectMapper, config);
		this.tableService = tableService;
		this.securityUtils = securityUtils;
	}

	public Route createRoute() { 
		return route(
				get(() -> pathPrefix(TABLES, () -> pathEndOrSingleSlash(() -> optionalHeaderValueByName("jwt", jwtToken -> securityUtils.can(jwtToken, () -> getTables()))))),
				get(() -> pathPrefix(TABLES, () -> pathPrefix(segment(), tableId -> pathEndOrSingleSlash(() -> getTableById(tableId))))), 
				post(() -> path(TABLES, () -> pathEndOrSingleSlash(() -> entity(Jackson.unmarshaller(NewTable.class), this::persistTable))))
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
		
		List<ValidationError> validationErrors = newTable.validate(config);
		if (!validationErrors.isEmpty()) {
			 return onValidationErrors(validationErrors);
		}
		

		LocalDateTime utcNow = TimeUtils.utcNow();
		UUID id = UUID.randomUUID();
		
		Location locationHeader = Location.create("/" + TableController.TABLES + "/" + id.toString());
		
		TableId tableId = ImmutableTableId.builder().tableId(id).build();

		Table table = ImmutableTable.builder()
				.id(tableId)
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

}
