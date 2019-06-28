package com.github.adrian83.robome.domain.table;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.auth.AuthContext;
import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.Authorization;
import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.common.web.AbstractController;
import com.github.adrian83.robome.common.web.ExceptionHandler;
import com.github.adrian83.robome.common.web.Response;
import com.github.adrian83.robome.common.web.Validation;
import com.github.adrian83.robome.domain.table.model.NewTable;
import com.github.adrian83.robome.domain.table.model.TableId;
import com.github.adrian83.robome.domain.table.model.UpdatedTable;
import com.github.adrian83.robome.domain.user.User;
import com.github.adrian83.robome.domain.user.UserService;
import com.github.adrian83.robome.util.http.Cors;
import com.github.adrian83.robome.util.http.HttpHeader;
import com.github.adrian83.robome.util.http.HttpMethod;
import com.github.adrian83.robome.util.http.Options;
import com.github.adrian83.robome.util.tuple.Tuple2;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;

public class TableController extends AbstractController {

	public static final String TABLES = "tables";

	private TableService tableService;
	private UserService userService;

	@Inject
	public TableController(UserService userService, TableService tableService, JwtAuthorizer jwtAuthorizer,
			Config config, ExceptionHandler exceptionHandler, Response response) {
		super(jwtAuthorizer, exceptionHandler, config, response);
		this.tableService = tableService;
		this.userService = userService;
	}

	public Route createRoute() {
		return route(createTableOptionsRoute, updateTableOptionsRoute, getTablesRoute, getTableRoute, deleteTableRoute,
				updateTableRoute, createTableRoute);
	}

	private Route createTableOptionsRoute = options(
			() -> pathPrefix(TABLES, () -> pathEndOrSingleSlash(this::handleCreateTableOptions)));

	private Route updateTableOptionsRoute = options(() -> pathPrefix(TABLES,
			() -> pathPrefix(segment(), tableId -> pathEndOrSingleSlash(this::handleUpdateTableOptions))));

	private Route getTablesRoute = get(
			() -> pathPrefix(TABLES, () -> pathEndOrSingleSlash(() -> jwtSecured(this::getTables))));

	private Route getTableRoute = get(() -> pathPrefix(TABLES, () -> pathPrefix(segment(),
			tableId -> pathEndOrSingleSlash(() -> jwtSecured(tableId, this::getTableById)))));

	private Route deleteTableRoute = delete(() -> pathPrefix(TABLES, () -> pathPrefix(segment(),
			tableId -> pathEndOrSingleSlash(() -> jwtSecured(tableId, this::deleteTable)))));

	private Route updateTableRoute = put(() -> pathPrefix(TABLES, () -> pathPrefix(segment(),
			tableId -> pathEndOrSingleSlash(() -> jwtSecured(tableId, UpdatedTable.class, this::updateTable)))));

	private Route createTableRoute = post(
			() -> path(TABLES, () -> pathEndOrSingleSlash(() -> jwtSecured(NewTable.class, this::persistTable))));

	
	private Route getTables(AuthContext authContext) {

		CompletionStage<HttpResponse> responseF = userService.findUserByEmail(authContext.getUserEmail())
				.thenApply(Authentication::userExists)
				.thenApply(Authorization::canReadTables)
				.thenCompose(tableService::getTables)
				.thenApply(responseProducer::jsonFromObject)
				.exceptionally(exceptionHandler::handleException);

		return completeWithFuture(responseF);
	}

	
	private Route getTableById(AuthContext authContext, String tableIdStr) {

		CompletionStage<HttpResponse> responseF = userService.findUserByEmail(authContext.getUserEmail())
				.thenApply(Authentication::userExists)
				.thenApply(Authorization::canReadTables)
				.thenCompose(user -> tableService.getTable(user, TableId.fromString(tableIdStr)))
				.thenApply(responseProducer::jsonFromOptional)
				.exceptionally(exceptionHandler::handleException);

		return completeWithFuture(responseF);
	}


	// TODO fix - delete instead of get
	private Route deleteTable(AuthContext authContext, String tableIdStr) {

		CompletionStage<HttpResponse> responseF = userService.findUserByEmail(authContext.getUserEmail())
				.thenApply(Authentication::userExists)
				.thenApply(Authorization::canWriteTables)
				.thenCompose(user -> tableService.getTable(user, TableId.fromString(tableIdStr)))
				.thenApply(responseProducer::jsonFromOptional)
				.exceptionally(exceptionHandler::handleException);

		return completeWithFuture(responseF);
	}
	
	
	private Route updateTable(AuthContext authContext, String tableIdStr, UpdatedTable updatedTable) {
				
		CompletionStage<HttpResponse> responseF = userService.findUserByEmail(authContext.getUserEmail())
				.thenApply(Authentication::userExists)
				.thenApply(Authorization::canWriteTables)
				.thenApply(user -> new Tuple2<User, UpdatedTable>(user, updatedTable))
				.thenApply(tuple -> new Tuple2<User, UpdatedTable>(tuple.getObj1(), Validation.validate(tuple.getObj2(), config)))
				.thenCompose(tuple -> tableService.updateTable(tuple.getObj1(), TableId.fromString(tableIdStr), tuple.getObj2()))
				.thenApply(table -> HttpResponse.create().withStatus(StatusCodes.OK).addHeaders(headers(locationFor(TableController.TABLES, table.getId().getTableId().toString()), Cors.origin(corsOrigin()))))
				.exceptionally(exceptionHandler::handleException);
		
		return completeWithFuture(responseF);
	}

	
	private Route persistTable(AuthContext authContext, NewTable newTable) {

		CompletionStage<HttpResponse> responseF = userService.findUserByEmail(authContext.getUserEmail())
				.thenApply(Authentication::userExists)
				.thenApply(Authorization::canWriteTables)
				.thenApply(user -> new Tuple2<User, NewTable>(user, newTable))
				.thenApply(tuple2 -> new Tuple2<User, NewTable>(tuple2.getObj1(), Validation.validate(tuple2.getObj2(), config)))
				.thenCompose(tuple -> tableService.saveTable(tuple.getObj1(), tuple.getObj2()))
				.thenApply(table -> HttpResponse.create().withStatus(StatusCodes.CREATED).addHeaders(headers(locationFor(TableController.TABLES, table.getId().getTableId().toString()), Cors.origin(corsOrigin()))))
				.exceptionally(exceptionHandler::handleException);
		
		return completeWithFuture(responseF);
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
