package com.github.adrian83.robome.domain.table;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.adrian83.robome.auth.AuthContext;
import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.common.time.TimeUtils;
import com.github.adrian83.robome.common.web.AbstractController;
import com.github.adrian83.robome.common.web.ValidationError;
import com.github.adrian83.robome.domain.table.Table;
import com.github.adrian83.robome.domain.user.User;
import com.github.adrian83.robome.domain.user.UserService;
import com.github.adrian83.robome.util.http.Cors;
import com.github.adrian83.robome.util.http.HttpHeader;
import com.github.adrian83.robome.util.http.HttpMethod;
import com.github.adrian83.robome.util.http.Options;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.Done;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.Route;

public class TableController extends AbstractController {

	public static final String TABLES = "tables";

	private TableService tableService;
	private UserService userService;

	@Inject
	public TableController(UserService userService, TableService tableService, JwtAuthorizer jwtAuthorizer, Config config,
			ObjectMapper objectMapper) {
		super(jwtAuthorizer, objectMapper, config);
		this.tableService = tableService;
		this.userService = userService;
	}

	public Route createRoute() {
		return route(createTableOptionsRoute, updateTableOptionsRoute, getTablesRoute, getTableRoute, deleteTableRoute, 
				updateTableRoute, createTableRoute);
	}

	
	private Route createTableOptionsRoute = options(() -> pathPrefix(TABLES, () -> pathEndOrSingleSlash(this::handleCreateTableOptions)));
	
	private Route updateTableOptionsRoute = options(() -> pathPrefix(TABLES, () -> pathPrefix(segment(), tableId -> pathEndOrSingleSlash(this::handleUpdateTableOptions))));
	
	private Route getTablesRoute = get(() -> pathPrefix(TABLES, () -> pathEndOrSingleSlash(() -> jwtSecured(this::getTables))));
	
	private Route getTableRoute = get(() -> pathPrefix(TABLES, () -> pathPrefix(segment(), tableId -> pathEndOrSingleSlash(() -> jwtSecured(tableId, this::getTableById)))));
	
	private Route deleteTableRoute = delete(() -> pathPrefix(TABLES, () -> pathPrefix(segment(),tableId -> pathEndOrSingleSlash(() -> jwtSecured(tableId, this::deleteTable)))));
	
	private Route updateTableRoute = put(() -> pathPrefix(TABLES, () -> pathPrefix(segment(), tableId -> pathEndOrSingleSlash(() -> jwtSecured(tableId, NewTable.class, this::updateTable)))));
	
	private Route createTableRoute = post(() -> path(TABLES, () -> pathEndOrSingleSlash(() -> jwtSecured(NewTable.class, this::persistTable))));

	
	private Route getTables(AuthContext authContext) {
		
		final CompletionStage<Optional<User>> user = userService.findUserByEmail(authContext.getUserEmail());

		final CompletionStage<List<Table>> futureTables = tableService.getTables(user);
 
		return onSuccess(() -> futureTables, tables -> {
			HttpResponse response = HttpResponse.create().withStatus(StatusCodes.OK)
					.withEntity(ContentTypes.APPLICATION_JSON, toBytes(tables))
					.addHeaders(headers(Cors.origin(corsOrigin())));

			return complete(response);
		});
	}

	private Route getTableById(AuthContext authContext, String tableId) {
		UUID tableUuid = UUID.fromString(tableId);

		

		final CompletionStage<Optional<Table>> futureMaybeTable = tableService.getTable(authContext.getUserId(), tableUuid);

		return onSuccess(() -> futureMaybeTable, maybeTable -> maybeTable.map(table -> {
			HttpResponse response = HttpResponse.create().withStatus(StatusCodes.OK)
					.withEntity(ContentTypes.APPLICATION_JSON, toBytes(table))
					.addHeaders(headers(Cors.origin(corsOrigin())));

			return complete(response);
		}).orElseGet(() -> complete(response404())));
	}

	private Route deleteTable(AuthContext authContext, String tableId) {
		UUID tableUuid = UUID.fromString(tableId);

		final CompletionStage<Optional<Table>> futureMaybeTable = tableService.getTable(authContext.getUserId(), tableUuid);

		return onSuccess(() -> futureMaybeTable, maybeTable -> maybeTable.map(table -> {
			HttpResponse response = HttpResponse.create().withStatus(StatusCodes.OK)
					.withEntity(ContentTypes.APPLICATION_JSON, toBytes(table))
					.addHeaders(headers(Cors.origin(corsOrigin())));

			return complete(response);
		}).orElseGet(() -> complete(response404())));
	}

	private Route updateTable(AuthContext authContext, String tableIdStr, NewTable newTable) {

		List<ValidationError> validationErrors = newTable.validate(config);
		if (!validationErrors.isEmpty()) {
			return complete(response400(validationErrors));
		}

		LocalDateTime utcNow = TimeUtils.utcNow();
		UUID id = UUID.fromString(tableIdStr);

		Location locationHeader = this.locationFor(TableController.TABLES, id.toString());

		TableId tableId = new TableId(id);

		CompletionStage<Optional<Done>> futureMaybeTable = tableService.getTable(authContext.getUserId(), id)
				.thenApply(maybeTable -> maybeTable.map(table -> {
					Table updatedTable = new Table(tableId, authContext.getUserId(), newTable.getTitle(),
							newTable.getDescription(), TableState.ACTIVE, utcNow, table.getCreatedAt());
					return updatedTable;
				}))
				.thenCompose(maybeTable -> maybeTable
						.map(table -> tableService.saveTable(table).thenApply(done -> Optional.ofNullable(done)))
						.orElseGet(() -> CompletableFuture.completedFuture(Optional.empty())));

		HttpResponse response = HttpResponse.create().withStatus(StatusCodes.CREATED)
				.addHeaders(headers(locationHeader, Cors.origin(corsOrigin())));

		return onSuccess(() -> futureMaybeTable,
				maybeDone -> complete(maybeDone.map(done -> response).orElse(response404())));
	}

	private Route persistTable(AuthContext authContext, NewTable newTable) {

		List<ValidationError> validationErrors = newTable.validate(config);
		if (!validationErrors.isEmpty()) {
			return complete(response400(validationErrors));
		}

		UUID id = UUID.randomUUID();

		Location locationHeader = this.locationFor(TableController.TABLES, id.toString());

		TableId tableId = new TableId(id);

		Table table = Table.newTable(tableId, authContext.getUserId(), newTable.getTitle(), newTable.getDescription());

		HttpResponse response = HttpResponse.create().withStatus(StatusCodes.CREATED)
				.addHeaders(headers(locationHeader, Cors.origin(corsOrigin())));

		CompletionStage<Done> futureSaved = tableService.saveTable(table);
		return onSuccess(() -> futureSaved, done -> complete(response));
	}

	private Route handleCreateTableOptions() {
		HttpResponse response = new Options()
				.withHeaders(HttpHeader.AUTHORIZATION.getText(), HttpHeader.CONTENT_TYPE.getText())
				.withMethods(HttpMethod.POST.name(), HttpMethod.GET.name()).withOrigin(corsOrigin()).response();

		return complete(response);
	}

	private Route handleUpdateTableOptions() {
		HttpResponse response = new Options()
				.withHeaders(HttpHeader.AUTHORIZATION.getText(), HttpHeader.CONTENT_TYPE.getText())
				.withMethods(HttpMethod.PUT.name(), HttpMethod.GET.name(), HttpMethod.DELETE.name())
				.withOrigin(corsOrigin()).response();

		return complete(response);
	}

}
