package com.github.adrian83.robome.web.table;

import static com.github.adrian83.robome.util.http.HttpMethod.DELETE;
import static com.github.adrian83.robome.util.http.HttpMethod.GET;
import static com.github.adrian83.robome.util.http.HttpMethod.POST;
import static com.github.adrian83.robome.util.http.HttpMethod.PUT;

import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.auth.Authorization;
import com.github.adrian83.robome.domain.common.UserAndForm;
import com.github.adrian83.robome.domain.table.TableService;
import com.github.adrian83.robome.domain.table.model.GetTableRequest;
import com.github.adrian83.robome.domain.table.model.ListTablesRequest;
import com.github.adrian83.robome.domain.table.model.NewTable;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.table.model.UpdatedTable;
import com.github.adrian83.robome.domain.user.model.User;
import com.github.adrian83.robome.web.common.ExceptionHandler;
import com.github.adrian83.robome.web.common.Response;
import com.github.adrian83.robome.web.common.Routes;
import com.github.adrian83.robome.web.common.Security;
import com.github.adrian83.robome.web.table.validation.NewTableValidator;
import com.github.adrian83.robome.web.table.validation.UpdatedTableValidator;
import com.google.inject.Inject;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class TableController extends AllDirectives {

  private static final Logger LOGGER = LoggerFactory.getLogger(TableController.class);

  public static final String TABLES = "tables";

  private static final UpdatedTableValidator UPDATE_VALIDATOR = new UpdatedTableValidator();
  private static final NewTableValidator CREATE_VALIDATOR = new NewTableValidator();

  private TableService tableService;
  private ExceptionHandler exceptionHandler;
  private Response response;
  private Security security;
  private Routes routes;

  @Inject
  public TableController(
      TableService tableService,
      ExceptionHandler exceptionHandler,
      Response response,
      Routes routes,
      Security security) {
    this.tableService = tableService;
    this.exceptionHandler = exceptionHandler;
    this.response = response;
    this.routes = routes;
    this.security = security;
  }

  public Route createRoute() {
    return route(
        get(routes.prefixVarSlash(TABLES, this::getTableAction)),
        get(routes.prefixSlash(TABLES, security.jwtSecured(this::getTables))),
        post(routes.prefixFormSlash(TABLES, NewTable.class, this::createTableAction)),
        put(routes.prefixVarFormSlash(TABLES, UpdatedTable.class, this::updateTableAction)),
        delete(routes.prefixVarSlash(TABLES, this::deleteTableAction)),
        options(routes.prefixSlash(TABLES, handleOptionsRequest())),
        options(routes.prefixVarSlash(TABLES, tableId -> handleOptionsRequestWithId())));
  }



  private Route getTables(CompletionStage<User> userF) {
    LOGGER.info("New list table request");

    CompletionStage<HttpResponse> responseF =
        userF
            .thenApply(Authorization::canReadTables)
            .thenApply(this::toListTablesRequest)
            .thenCompose(tableService::getTables)
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route getTableById(CompletionStage<User> userF, String tableIdStr) {
    LOGGER.info("New find table request, tableId: {}", tableIdStr);

    CompletionStage<HttpResponse> responseF =
        userF
            .thenApply(Authorization::canReadTables)
            .thenApply(user -> toGetTableRequest(user, tableIdStr))
            .thenCompose(tableService::getTable)
            .thenApply(response::jsonFromOptional)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route deleteTable(CompletionStage<User> userF, String tableIdStr) {
    LOGGER.info("New delete table request, tableId: {}", tableIdStr);

    CompletionStage<HttpResponse> responseF =
        userF
            .thenApply(Authorization::canWriteTables)
            .thenCompose(user -> tableService.deleteTable(user, TableKey.parse(tableIdStr)))
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route updateTable(
      CompletionStage<User> userF, String tableIdStr, UpdatedTable updatedTable) {
    LOGGER.info("New update table request, tableId: {}, update: {}", tableIdStr, updatedTable);

    CompletionStage<HttpResponse> responseF =
        userF
            .thenApply(Authorization::canWriteTables)
            .thenApply(user -> new UserAndForm<UpdatedTable>(user, updatedTable, UPDATE_VALIDATOR))
            .thenApply(UserAndForm::validate)
            .thenCompose(
                uaf ->
                    tableService.updateTable(
                        uaf.getUser(), TableKey.parse(tableIdStr), uaf.getForm()))
            .thenApply(table -> response.jsonFromObject(table))
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route persistTable(CompletionStage<User> userF, NewTable newTable) {
    LOGGER.info("New persist table request, table: {}", newTable);

    CompletionStage<HttpResponse> responseF =
        userF
            .thenApply(Authorization::canWriteTables)
            .thenApply(user -> new UserAndForm<NewTable>(user, newTable, CREATE_VALIDATOR))
            .thenApply(UserAndForm::validate)
            .thenCompose(uaf -> tableService.saveTable(uaf.getUser(), uaf.getForm()))
            .thenApply(table -> response.jsonFromObject(table))
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route handleOptionsRequestWithId() {
    return complete(response.response200(GET, POST, PUT));
  }

  private Route handleOptionsRequest() {
    return complete(response.response200(GET, DELETE));
  }
  
  private Route getTableAction(String tableId) {
	    return security.jwtSecured(tableId, this::getTableById);
	  }

	  private Route deleteTableAction(String tableId) {
	    return security.jwtSecured(tableId, this::deleteTable);
	  }

	  private Route updateTableAction(String tableId, Class<UpdatedTable> clazz) {
	    return security.jwtSecured(tableId, clazz, this::updateTable);
	  }

	  private Route createTableAction(Class<NewTable> clazz) {
	    return security.secured(clazz, this::persistTable);
	  }
	  
	  private ListTablesRequest toListTablesRequest(User user) {
		  return ListTablesRequest.builder().userId(user.getId()).build();
	  }
	  
	  private GetTableRequest toGetTableRequest(User user, String tableIdStr) {
		  return GetTableRequest.builder().userId(user.getId()).tableKey(TableKey.parse(tableIdStr)).build();
	  }
  
}
