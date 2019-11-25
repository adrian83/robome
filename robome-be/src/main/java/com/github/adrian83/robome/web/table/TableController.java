package com.github.adrian83.robome.web.table;

import static com.github.adrian83.robome.util.http.HttpMethod.DELETE;
import static com.github.adrian83.robome.util.http.HttpMethod.GET;
import static com.github.adrian83.robome.util.http.HttpMethod.POST;
import static com.github.adrian83.robome.util.http.HttpMethod.PUT;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.Authorization;
import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.common.web.ExceptionHandler;
import com.github.adrian83.robome.common.web.Response;
import com.github.adrian83.robome.domain.common.UserAndForm;
import com.github.adrian83.robome.domain.table.TableService;
import com.github.adrian83.robome.domain.table.model.NewTable;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.table.model.UpdatedTable;
import com.github.adrian83.robome.domain.user.model.User;
import com.github.adrian83.robome.web.common.AbstractController;
import com.github.adrian83.robome.web.common.Routes;
import com.github.adrian83.robome.web.table.validation.NewTableValidator;
import com.github.adrian83.robome.web.table.validation.UpdatedTableValidator;
import com.google.inject.Inject;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.Route;

public class TableController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(TableController.class);
	
  public static final String TABLES = "tables";

  private static final UpdatedTableValidator UPDATE_VALIDATOR = new UpdatedTableValidator();
  private static final NewTableValidator CREATE_VALIDATOR = new NewTableValidator();

  private TableService tableService;
  private Routes routes;

  @Inject
  public TableController(
      TableService tableService,
      JwtAuthorizer jwtAuthorizer,
      ExceptionHandler exceptionHandler,
      Response response,
       Routes routes) {
    super(jwtAuthorizer, exceptionHandler, response);
    this.tableService = tableService;
    this.routes = routes;
  }

  public Route createRoute() {
    return route(
    	put(routes.prefixVarFormSlash(TABLES, UpdatedTable.class, updateTableAction)),
        options(routes.prefixSlash(TABLES, handleOptionsRequest())),
        post(routes.prefixFormSlash(TABLES, NewTable.class, createTableAction)),
        get(routes.prefixSlash(TABLES, jwtSecured(this::getTables))),
        options(routes.prefixVarSlash(TABLES, tableId -> handleOptionsRequestWithId())),
        get(routes.prefixVarSlash(TABLES, getTableAction)),
        delete(routes.prefixVarSlash(TABLES, deleteTableAction)));
  }

  Function<String, Route> getTableAction = (var tableId) -> jwtSecured(tableId, this::getTableById);

  Function<String, Route> deleteTableAction =
      (var tableId) -> jwtSecured(tableId, this::deleteTable);

  BiFunction<String, Class<UpdatedTable>, Route> updateTableAction =
      (var tableId, var clazz) -> jwtSecured(tableId, clazz, this::updateTable);

  Function<Class<NewTable>, Route> createTableAction =
      (var clazz) -> jwtSecured(clazz, this::persistTable);

  private Route getTables(CompletionStage<Optional<User>> maybeUserF) {
	  
	  LOGGER.info("New list table request");

    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canReadTables)
            .thenCompose(tableService::getTables)
            .thenApply(responseProducer::jsonFromObject)
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route getTableById(CompletionStage<Optional<User>> maybeUserF, String tableIdStr) {
	  
	  LOGGER.info("New find table request, tableId: {}", tableIdStr);

    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canReadTables)
            .thenCompose(user -> tableService.getTable(user, TableKey.fromString(tableIdStr)))
            .thenApply(responseProducer::jsonFromOptional)
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route deleteTable(CompletionStage<Optional<User>> maybeUserF, String tableIdStr) {
	  
	  LOGGER.info("New delete table request, tableId: {}", tableIdStr);

    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canWriteTables)
            .thenCompose(user -> tableService.deleteTable(user, TableKey.fromString(tableIdStr)))
            .thenApply(responseProducer::jsonFromObject)
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route updateTable(
      CompletionStage<Optional<User>> maybeUserF, String tableIdStr, UpdatedTable updatedTable) {

	  LOGGER.info("New update table request, tableId: {}, update: {}", tableIdStr, updatedTable);
	  
    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canWriteTables)
            .thenApply(user -> new UserAndForm<UpdatedTable>(user, updatedTable, UPDATE_VALIDATOR))
            .thenApply(UserAndForm::validate)
            .thenCompose(
                uaf ->
                    tableService.updateTable(
                        uaf.getUser(), TableKey.fromString(tableIdStr), uaf.getForm()))
            .thenApply(table -> responseProducer.jsonFromObject(table))
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route persistTable(CompletionStage<Optional<User>> maybeUserF, NewTable newTable) {

	  LOGGER.info("New persist table request, table: {}", newTable);
	  
    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canWriteTables)
            .thenApply(user -> new UserAndForm<NewTable>(user, newTable, CREATE_VALIDATOR))
            .thenApply(UserAndForm::validate)
            .thenCompose(uaf -> tableService.saveTable(uaf.getUser(), uaf.getForm()))
            .thenApply(table -> responseProducer.jsonFromObject(table))
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route handleOptionsRequestWithId() {
    return complete(responseProducer.response200(GET, POST, PUT));
  }

  private Route handleOptionsRequest() {
    return complete(responseProducer.response200(GET, DELETE));
  }
}
