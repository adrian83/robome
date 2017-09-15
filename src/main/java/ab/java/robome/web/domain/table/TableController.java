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
	private SecurityUtils securityUtils;

	@Inject
	public TableController(TableService tableService, SecurityUtils securityUtils, 
			Config config, ObjectMapper objectMapper) {
		super(objectMapper, config);
		this.tableService = tableService;
		this.securityUtils = securityUtils;
	}

	public Route createRoute() { 
		return route(
				options(() -> pathPrefix(TABLES, () -> pathEndOrSingleSlash(this::handleCreateTableOptions))),
				options(() -> pathPrefix(TABLES, () -> pathPrefix(segment(), tableId -> pathEndOrSingleSlash(this::handleCreateTableOptions)))),

				get(() -> pathPrefix(
						TABLES, 
						() -> pathEndOrSingleSlash(
								() -> optionalHeaderValueByName(
										HttpHeader.JWT_TOKEN.getText(), 
										jwtToken -> securityUtils.authorized(
												jwtToken, 
												userData -> getTables(userData)))))),
				
				get(() -> pathPrefix(TABLES, () -> pathPrefix(segment(), tableId -> pathEndOrSingleSlash(
						() -> optionalHeaderValueByName(
								HttpHeader.JWT_TOKEN.getText(), 
								jwtToken -> securityUtils.authorized(
										jwtToken, 
										userData -> getTableById(tableId, userData))))))), 
				
				post(() -> path(TABLES, () -> pathEndOrSingleSlash(
						() -> optionalHeaderValueByName(
								HttpHeader.JWT_TOKEN.getText(), 
								jwtToken -> securityUtils.authorized(
										jwtToken, 
										userData -> entity(Jackson.unmarshaller(NewTable.class), newTable -> this.persistTable(newTable, userData)))))))
				);
	}


	
	private Route getTables(UserData userData){
		
		final CompletionStage<List<Table>> futureTables = tableService.getTables(userData.id());
		
		return onSuccess(() -> futureTables, tables -> {
			HttpResponse response = HttpResponse.create()
					 .withStatus(StatusCodes.OK)
					 .withEntity(ContentTypes.APPLICATION_JSON, toBytes(tables))
					 .addHeaders(headers(
							 Cors.allowHeaders(
									 HttpHeader.JWT_TOKEN.getText(), 
									 HttpHeader.CONTENT_TYPE.getText()), 
							 Cors.methods(
									 HttpMethod.POST.name(), 
									 HttpMethod.GET.name()),
							 Cors.origin(corsOrigin()), 
							 jwt(userData.token())));
			
			return complete(response);
		});
	}
	
	private Route getTableById(String tableId, UserData userData) {
		UUID tableUuid = UUID.fromString(tableId);
		
		final CompletionStage<Optional<Table>> futureMaybeTable = tableService.getTable(tableUuid);
		
		return onSuccess(() -> futureMaybeTable, maybeItem -> maybeItem.map(item -> completeOK(item, Jackson.marshaller(objectMapper)))
				.orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found")));
	}
	
	private Route persistTable(NewTable newTable, UserData userData) {
		
		List<ValidationError> validationErrors = newTable.validate(config);
		if (!validationErrors.isEmpty()) {
			 return onValidationErrors(validationErrors);
		}
		

		LocalDateTime utcNow = TimeUtils.utcNow();
		UUID id = UUID.randomUUID();
		
		Location locationHeader = Location.create("/" + TableController.TABLES + "/" + id.toString());
		
		TableId tableId = ImmutableTableId.builder()
				.tableId(id)
				.build();

		Table table = ImmutableTable.builder()
				.id(tableId)
				.title(newTable.getTitle())
				.userId(userData.id())
				.state(TableState.ACTIVE)
				.createdAt(utcNow)
				.modifiedAt(utcNow)
				.build();
		
		HttpResponse response = HttpResponse.create()
				.withStatus(StatusCodes.CREATED)
				.addHeaders(
						headers(
								locationHeader, 
								Cors.origin("*"), 
								Cors.methods("POST"), 
								Cors.allowHeaders(HttpHeader.JWT_TOKEN.getText() , "Content-Type")));

		CompletionStage<Done> futureSaved = tableService.saveTable(table);
		return onSuccess(() -> futureSaved, done -> complete(response));
	}

	private Route handleCreateTableOptions() {
		HttpResponse response = new Options()
				.withHeaders(
						HttpHeader.JWT_TOKEN.getText(), 
						HttpHeader.CONTENT_TYPE.getText())
				.withMethods(HttpMethod.POST.name(), HttpMethod.GET.name())
				.withOrigin(corsOrigin())
				.response();
		
		return complete(response);
	}
	
}
