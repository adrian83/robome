package ab.java.robome.web.domain.table;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import ab.java.robome.common.time.TimeUtils;
import ab.java.robome.domain.table.TableService;
import ab.java.robome.domain.table.model.Table;
import ab.java.robome.domain.table.model.TableId;
import ab.java.robome.domain.table.model.TableState;
import ab.java.robome.web.common.AbstractController;
import ab.java.robome.web.common.HttpHeader;
import ab.java.robome.web.common.HttpMethod;
import ab.java.robome.web.common.response.Cors;
import ab.java.robome.web.common.response.Options;
import ab.java.robome.web.common.validation.ValidationError;
import ab.java.robome.web.domain.table.model.NewTable;
import ab.java.robome.web.security.SecurityUtils;
import ab.java.robome.web.security.UserData;
import akka.Done;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.Route;

public class TableController extends AbstractController {
	
	public static final String TABLES = "tables";

	private TableService tableService;	

	@Inject
	public TableController(TableService tableService, SecurityUtils securityUtils, 
			Config config, ObjectMapper objectMapper) {
		super(securityUtils, objectMapper, config);
		this.tableService = tableService;
	}

	public Route createRoute() { 
		return route(
				options(() -> pathPrefix(TABLES, () -> pathEndOrSingleSlash(this::handleCreateTableOptions))),
				options(() -> pathPrefix(TABLES, () -> pathPrefix(segment(), tableId -> pathEndOrSingleSlash(this::handleUpdateTableOptions)))),

				get(() -> pathPrefix(
						TABLES, 
						() -> pathEndOrSingleSlash(
								() -> optionalHeaderValueByName(
										HttpHeader.AUTHORIZATION.getText(), 
										jwtToken -> securityUtils.authorized(
												jwtToken, 
												userData -> getTables(userData)))))),
				
				get(() -> pathPrefix(TABLES, () -> pathPrefix(segment(), tableId -> pathEndOrSingleSlash(
						() -> optionalHeaderValueByName(
								HttpHeader.AUTHORIZATION.getText(), 
								jwtToken -> securityUtils.authorized(
										jwtToken, 
										userData -> getTableById(tableId, userData))))))), 
				
				delete(() -> pathPrefix(TABLES, () -> pathPrefix(segment(), tableId -> pathEndOrSingleSlash(
						() -> optionalHeaderValueByName(
								HttpHeader.AUTHORIZATION.getText(), 
								jwtToken -> securityUtils.authorized(
										jwtToken, 
										userData -> deleteTable(tableId, userData))))))), 
				
				put(() -> pathPrefix(TABLES, () -> pathPrefix(segment(), tableId -> pathEndOrSingleSlash(
						() -> optionalHeaderValueByName(
								HttpHeader.AUTHORIZATION.getText(), 
								jwtToken -> securityUtils.authorized(
										jwtToken, 
										userData -> entity(Jackson.unmarshaller(NewTable.class), updatedTable -> this.updateTable(tableId, updatedTable, userData)))))))), 
				
				post(() -> path(TABLES, () -> pathEndOrSingleSlash(
						() -> optionalHeaderValueByName(
								HttpHeader.AUTHORIZATION.getText(), 
								jwtToken -> securityUtils.authorized(
										jwtToken, 
										userData -> entity(Jackson.unmarshaller(NewTable.class), newTable -> this.persistTable(newTable, userData)))))))
				);
	}


	
	private Route getTables(UserData userData){
		
		final CompletionStage<List<Table>> futureTables = tableService.getTables(userData.getId());
		
		return onSuccess(() -> futureTables, tables -> {
			HttpResponse response = HttpResponse.create()
					 .withStatus(StatusCodes.OK)
					 .withEntity(ContentTypes.APPLICATION_JSON, toBytes(tables))
					 .addHeaders(headers(
							 Cors.origin(corsOrigin())));
			
			return complete(response);
		});
	}
	
	private Route getTableById(String tableId, UserData userData) {
		UUID tableUuid = UUID.fromString(tableId);
		
		final CompletionStage<Optional<Table>> futureMaybeTable = tableService.getTable(userData.getId(), tableUuid);
		
		return onSuccess(
				() -> futureMaybeTable, 
				maybeTable-> maybeTable.map(table -> { 
					HttpResponse response = HttpResponse.create()
							 .withStatus(StatusCodes.OK)
							 .withEntity(ContentTypes.APPLICATION_JSON, toBytes(table))
							 .addHeaders(headers(
									 Cors.origin(corsOrigin())));
					
					return complete(response);
					})
				.orElseGet(() -> complete(response404())));
	}
	
	private Route deleteTable(String tableId, UserData userData) {
		UUID tableUuid = UUID.fromString(tableId);
		
		final CompletionStage<Optional<Table>> futureMaybeTable = tableService.getTable(userData.getId(), tableUuid);
		
		return onSuccess(
				() -> futureMaybeTable, 
				maybeTable-> maybeTable.map(table -> { 
					HttpResponse response = HttpResponse.create()
							 .withStatus(StatusCodes.OK)
							 .withEntity(ContentTypes.APPLICATION_JSON, toBytes(table))
							 .addHeaders(headers(
									 Cors.origin(corsOrigin())));
					
					return complete(response);
					})
				.orElseGet(() -> complete(response404())));
	}
	
	private Route updateTable(String tableIdStr, NewTable newTable, UserData userData) {
		
		List<ValidationError> validationErrors = newTable.validate(config);
		if (!validationErrors.isEmpty()) {
			 return complete(response400(validationErrors));
		}
		

		LocalDateTime utcNow = TimeUtils.utcNow();
		UUID id = UUID.fromString(tableIdStr);
		
		Location locationHeader = this.locationFor(TableController.TABLES, id.toString());
		
		TableId tableId = TableId.builder()
				.tableId(id)
				.build();

		CompletionStage<Optional<Done>> futureMaybeTable = tableService.getTable(userData.getId(), id)
				.thenApply(maybeTable -> maybeTable.map(table -> {
					Table updatedTable = Table.builder()
							.id(tableId)
							.title(newTable.getTitle())
							.userId(userData.getId())
							.description(newTable.getDescription())
							.state(TableState.ACTIVE)
							.createdAt(table.getCreatedAt())
							.modifiedAt(utcNow)
							.build();
					return updatedTable; 
				}))
				.thenCompose(maybeTable ->  maybeTable
						.map(table -> tableService.saveTable(table)
								.thenApply(done -> Optional.ofNullable(done)))
						.orElseGet(() -> CompletableFuture.completedFuture(Optional.empty())));

		
		HttpResponse response = HttpResponse.create()
				.withStatus(StatusCodes.CREATED)
				.addHeaders(
						headers(
								locationHeader, 
								Cors.origin(corsOrigin())));

		return onSuccess(() -> futureMaybeTable, maybeDone -> complete(maybeDone.map(done -> response).orElse(response404())));
	}
	
	private Route persistTable(NewTable newTable, UserData userData) {
		
		List<ValidationError> validationErrors = newTable.validate(config);
		if (!validationErrors.isEmpty()) {
			 return complete(response400(validationErrors));
		}
		

		LocalDateTime utcNow = TimeUtils.utcNow();
		UUID id = UUID.randomUUID();
		
		Location locationHeader = this.locationFor(TableController.TABLES, id.toString());
		
		TableId tableId = TableId.builder()
				.tableId(id)
				.build();

		Table table = Table.builder()
				.id(tableId)
				.title(newTable.getTitle())
				.userId(userData.getId())
				.description(newTable.getDescription())
				.state(TableState.ACTIVE)
				.createdAt(utcNow)
				.modifiedAt(utcNow)
				.build();
		
		HttpResponse response = HttpResponse.create()
				.withStatus(StatusCodes.CREATED)
				.addHeaders(
						headers(
								locationHeader, 
								Cors.origin(corsOrigin())));

		CompletionStage<Done> futureSaved = tableService.saveTable(table);
		return onSuccess(() -> futureSaved, done -> complete(response));
	}

	private Route handleCreateTableOptions() {
		HttpResponse response = new Options()
				.withHeaders(
						HttpHeader.AUTHORIZATION.getText(), 
						HttpHeader.CONTENT_TYPE.getText())
				.withMethods(HttpMethod.POST.name(), HttpMethod.GET.name())
				.withOrigin(corsOrigin())
				.response();
		
		return complete(response);
	}
	
	private Route handleUpdateTableOptions() {
		HttpResponse response = new Options()
				.withHeaders(
						HttpHeader.AUTHORIZATION.getText(), 
						HttpHeader.CONTENT_TYPE.getText())
				.withMethods(HttpMethod.PUT.name(), HttpMethod.GET.name(), HttpMethod.DELETE.name())
				.withOrigin(corsOrigin())
				.response();
		
		return complete(response);
	}
	
}
