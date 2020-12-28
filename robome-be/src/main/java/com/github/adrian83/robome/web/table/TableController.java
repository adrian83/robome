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
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.table.model.request.DeleteTableRequest;
import com.github.adrian83.robome.domain.table.model.request.GetTableRequest;
import com.github.adrian83.robome.domain.table.model.request.ListTablesRequest;
import com.github.adrian83.robome.domain.table.model.request.NewTableRequest;
import com.github.adrian83.robome.domain.table.model.request.UpdateTableRequest;
import com.github.adrian83.robome.domain.user.model.User;
import com.github.adrian83.robome.web.common.ExceptionHandler;
import com.github.adrian83.robome.web.common.Response;
import com.github.adrian83.robome.web.common.Security;
import com.github.adrian83.robome.web.common.routes.FormRoute;
import com.github.adrian83.robome.web.common.routes.OneParamAndFormRoute;
import com.github.adrian83.robome.web.common.routes.OneParamRoute;
import com.github.adrian83.robome.web.common.routes.PrefixRoute;
import com.github.adrian83.robome.web.table.model.NewTable;
import com.github.adrian83.robome.web.table.model.UpdatedTable;
import com.github.adrian83.robome.web.table.validation.NewTableValidator;
import com.github.adrian83.robome.web.table.validation.UpdatedTableValidator;
import com.google.inject.Inject;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class TableController extends AllDirectives {

  private static final Logger LOGGER = LoggerFactory.getLogger(TableController.class);

  private static final String TABLES_PATH = "/tables/";
  private static final String TABLE_PATH = "/tables/{tableId}/";

  private static final UpdatedTableValidator UPDATE_VALIDATOR = new UpdatedTableValidator();
  private static final NewTableValidator CREATE_VALIDATOR = new NewTableValidator();

  private TableService tableService;
  private ExceptionHandler exceptionHandler;
  private Response response;
  private Security security;

  @Inject
  public TableController(
      TableService tableService,
      ExceptionHandler exceptionHandler,
      Response response,
      Security security) {
    this.tableService = tableService;
    this.exceptionHandler = exceptionHandler;
    this.response = response;
    this.security = security;
  }

  public Route createRoute() {
    return route(
        get(new PrefixRoute(TABLES_PATH, security.jwtSecured(this::getTables))),
        get(
            new OneParamRoute(
                TABLE_PATH, (tabId) -> security.jwtSecured(tabId, this::getTableById))),
        post(
            new FormRoute<NewTable>(
                TABLES_PATH, NewTable.class, (clz) -> security.secured(clz, this::persistTable))),
        put(
            new OneParamAndFormRoute<UpdatedTable>(
                TABLE_PATH,
                UpdatedTable.class,
                (tabId, clz) -> security.jwtSecured(tabId, clz, this::updateTable))),
        delete(
            new OneParamRoute(
                TABLE_PATH, (tabId) -> security.jwtSecured(tabId, this::deleteTable))),
        options(new PrefixRoute(TABLES_PATH, complete(response.response200(GET, DELETE)))),
        options(
            new OneParamRoute(
                TABLES_PATH, (tabId) -> complete(response.response200(GET, POST, PUT)))));
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
            .thenApply(u -> toDeleteTableRequest(u, tableIdStr))
            .thenCompose(tableService::deleteTable)
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
            .thenApply(uaf -> toUpdateTableRequest(uaf.getUser(), tableIdStr, uaf.getForm()))
            .thenCompose(tableService::updateTable)
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
            .thenApply(uaf -> toNewTableRequest(uaf.getUser(), uaf.getForm()))
            .thenCompose(tableService::saveTable)
            .thenApply(table -> response.jsonFromObject(table))
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private ListTablesRequest toListTablesRequest(User user) {
    return ListTablesRequest.builder().userId(user.getId()).build();
  }

  private GetTableRequest toGetTableRequest(User user, String tableIdStr) {
    return GetTableRequest.builder()
        .userId(user.getId())
        .tableKey(TableKey.parse(tableIdStr))
        .build();
  }

  private DeleteTableRequest toDeleteTableRequest(User user, String tableIdStr) {
    return DeleteTableRequest.builder()
        .userId(user.getId())
        .tableKey(TableKey.parse(tableIdStr))
        .build();
  }

  private UpdateTableRequest toUpdateTableRequest(User user, String tableIdStr, UpdatedTable form) {
    return UpdateTableRequest.builder()
        .tableKey(TableKey.parse(tableIdStr))
        .userId(user.getId())
        .title(form.getTitle())
        .description(form.getDescription())
        .build();
  }

  private NewTableRequest toNewTableRequest(User user, NewTable form) {
    return NewTableRequest.builder()
        .userId(user.getId())
        .title(form.getTitle())
        .description(form.getDescription())
        .build();
  }
}
