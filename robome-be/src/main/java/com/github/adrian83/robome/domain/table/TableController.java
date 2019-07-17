package com.github.adrian83.robome.domain.table;


import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.Authorization;
import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.common.web.AbstractController;
import com.github.adrian83.robome.common.web.ExceptionHandler;
import com.github.adrian83.robome.common.web.Response;
import com.github.adrian83.robome.domain.common.UserAndForm;
import com.github.adrian83.robome.domain.table.model.NewTable;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.table.model.UpdatedTable;
import com.github.adrian83.robome.domain.user.User;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.Route;

public class TableController extends AbstractController {

	public static final String TABLES = "tables";

	private TableService tableService;

	@Inject
	public TableController(TableService tableService, JwtAuthorizer jwtAuthorizer, Config config,
			ExceptionHandler exceptionHandler, Response response) {
		super(jwtAuthorizer, exceptionHandler, config, response);
		this.tableService = tableService;
	}

	public Route createRoute() {
		return route(options(prefix(TABLES, handleCreateTableOptions())),
				options(prefixVar(TABLES, this::handleUpdateTableOptions)),
				get(prefix(TABLES, jwtSecured(this::getTables))), get(prefixVar(TABLES, getTableAction)),
				delete(prefixVar(TABLES, deleteTableAction)),
				put(prefixVarForm(TABLES, UpdatedTable.class, updateTableAction)),
				post(prefixForm(TABLES, NewTable.class, createTableAction)));
	}

	Function<String, Route> getTableAction = (String tableId) -> jwtSecured(tableId, this::getTableById);

	Function<String, Route> deleteTableAction = (String tableId) -> jwtSecured(tableId, this::deleteTable);

	BiFunction<String, Class<UpdatedTable>, Route> updateTableAction = (String tableId,
			Class<UpdatedTable> clazz) -> jwtSecured(tableId, clazz, this::updateTable);

	Function<Class<NewTable>, Route> createTableAction = (Class<NewTable> clazz) -> jwtSecured(clazz,
			this::persistTable);

	private Route getTables(CompletionStage<Optional<User>> maybeUserF) {

		CompletionStage<HttpResponse> responseF = maybeUserF.thenApply(Authentication::userExists)
				.thenApply(Authorization::canReadTables).thenCompose(tableService::getTables)
				.thenApply(responseProducer::jsonFromObject).exceptionally(exceptionHandler::handleException);

		return completeWithFuture(responseF);
	}

	private Route getTableById(CompletionStage<Optional<User>> maybeUserF, String tableIdStr) {

		CompletionStage<HttpResponse> responseF = maybeUserF.thenApply(Authentication::userExists)
				.thenApply(Authorization::canReadTables)
				.thenCompose(user -> tableService.getTable(user, TableKey.fromString(tableIdStr)))
				.thenApply(responseProducer::jsonFromOptional).exceptionally(exceptionHandler::handleException);

		return completeWithFuture(responseF);
	}

	private Route deleteTable(CompletionStage<Optional<User>> maybeUserF, String tableIdStr) {

		CompletionStage<HttpResponse> responseF = maybeUserF.thenApply(Authentication::userExists)
				.thenApply(Authorization::canWriteTables)
				.thenCompose(user -> tableService.deleteTable(user, TableKey.fromString(tableIdStr)))
				.thenApply(responseProducer::jsonFromObject).exceptionally(exceptionHandler::handleException);

		return completeWithFuture(responseF);
	}

	private Route updateTable(CompletionStage<Optional<User>> maybeUserF, String tableIdStr,
			UpdatedTable updatedTable) {

		CompletionStage<HttpResponse> responseF = maybeUserF.thenApply(Authentication::userExists)
				.thenApply(Authorization::canWriteTables)
				.thenApply(user -> new UserAndForm<UpdatedTable>(user, updatedTable)).thenApply(UserAndForm::validate)
				.thenCompose(
						uaf -> tableService.updateTable(uaf.getUser(), TableKey.fromString(tableIdStr), uaf.getForm()))
				.thenApply(table -> responseProducer.response200(location(table.getKey())))
				.exceptionally(exceptionHandler::handleException);

		return completeWithFuture(responseF);
	}

	private Route persistTable(CompletionStage<Optional<User>> maybeUserF, NewTable newTable) {

		CompletionStage<HttpResponse> responseF = maybeUserF.thenApply(Authentication::userExists)
				.thenApply(Authorization::canWriteTables).thenApply(user -> new UserAndForm<NewTable>(user, newTable))
				.thenApply(UserAndForm::validate)
				.thenCompose(uaf -> tableService.saveTable(uaf.getUser(), uaf.getForm()))
				.thenApply(table -> responseProducer.response201(location(table.getKey())))
				.exceptionally(exceptionHandler::handleException);

		return completeWithFuture(responseF);
	}

	private Route handleCreateTableOptions() {
		return complete(responseProducer.response200());
	}

	private Route handleUpdateTableOptions(String tableId) {
		return complete(responseProducer.response200());
	}

	private Location location(TableKey tableKey) {
		return locationFor(TableController.TABLES, tableKey.getTableId().toString());
	}
}
