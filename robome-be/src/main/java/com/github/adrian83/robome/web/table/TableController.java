package com.github.adrian83.robome.web.table;

import static com.github.adrian83.robome.util.http.HttpMethod.DELETE;
import static com.github.adrian83.robome.util.http.HttpMethod.GET;
import static com.github.adrian83.robome.util.http.HttpMethod.POST;
import static com.github.adrian83.robome.util.http.HttpMethod.PUT;

import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

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
import com.github.adrian83.robome.web.table.model.UpdateTable;
import com.google.inject.Inject;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TableController extends AllDirectives {

  private static final String TABLES_PATH = "/tables/";
  private static final String TABLE_PATH = "/tables/{tableId}/";

  private static final String LOG_LIST_TABS = "User: {} issued list tables request";
  private static final String LOG_CREATE_TAB = "User: {} issued persis table request, data: {}";
  private static final String LOG_GET_TAB_BY_ID =
      "User: {} issued get table by id request, tableId: {}";
  private static final String LOG_DEL_TAB_BY_ID =
      "User: {} issued delete table by id request, tableId: {}";
  private static final String LOG_UPDATE_TAB =
      "User: {} issued update table request, tableId: {}, data: {}";

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
        get(new PrefixRoute(TABLES_PATH, security.secured(this::getTables))),
        get(new OneParamRoute(TABLE_PATH, (tabId) -> security.secured(tabId, this::getTableById))),
        post(
            new FormRoute<NewTable>(
                TABLES_PATH, NewTable.class, (clz) -> security.secured(clz, this::persistTable))),
        put(
            new OneParamAndFormRoute<UpdateTable>(
                TABLE_PATH,
                UpdateTable.class,
                (tabId, clz) -> security.secured(tabId, clz, this::updateTable))),
        delete(
            new OneParamRoute(TABLE_PATH, (tabId) -> security.secured(tabId, this::deleteTable))),
        options(new PrefixRoute(TABLES_PATH, complete(response.response200(GET, POST)))),
        options(
            new OneParamRoute(
                TABLE_PATH, (tabId) -> complete(response.response200(GET, PUT, DELETE)))));
  }

  private CompletionStage<HttpResponse> getTables(CompletionStage<User> userF) {

    var cLog = genLog((User user) -> log.info(LOG_LIST_TABS, user.getEmail()));

    return userF
        .thenApply(cLog::apply)
        .thenApply(Authorization::canReadTables)
        .thenApply(this::toListTablesRequest)
        .thenCompose(tableService::getTables)
        .thenApply(response::jsonFromObject)
        .exceptionally(exceptionHandler::handle);
  }

  private CompletionStage<HttpResponse> getTableById(
      CompletionStage<User> userF, String tableIdStr) {

    var cLog = genLog((User user) -> log.info(LOG_GET_TAB_BY_ID, user.getEmail(), tableIdStr));

    return userF
        .thenApply(cLog::apply)
        .thenApply(Authorization::canReadTables)
        .thenApply(user -> toGetTableRequest(user, tableIdStr))
        .thenCompose(tableService::getTable)
        .thenApply(response::jsonFromOptional)
        .exceptionally(exceptionHandler::handle);
  }

  private CompletionStage<HttpResponse> deleteTable(
      CompletionStage<User> userF, String tableIdStr) {

    var cLog = genLog((User user) -> log.info(LOG_DEL_TAB_BY_ID, user.getEmail(), tableIdStr));

    return userF
        .thenApply(cLog::apply)
        .thenApply(Authorization::canWriteTables)
        .thenApply(u -> toDeleteTableRequest(u, tableIdStr))
        .thenCompose(tableService::deleteTable)
        .thenApply(response::jsonFromObject)
        .exceptionally(exceptionHandler::handle);
  }

  private CompletionStage<HttpResponse> updateTable(
      CompletionStage<User> userF, String tableIdStr, UpdateTable form) {

    var cLog = genLog((User user) -> log.info(LOG_UPDATE_TAB, user.getEmail(), tableIdStr, form));

    return userF
        .thenApply(cLog::apply)
        .thenApply(Authorization::canWriteTables)
        .thenApply(user -> new UserAndForm<UpdateTable>(user, form))
        .thenApply(UserAndForm::validate)
        .thenApply(uaf -> toUpdateTableRequest(uaf.getUser(), tableIdStr, uaf.getForm()))
        .thenCompose(tableService::updateTable)
        .thenApply(table -> response.jsonFromObject(table))
        .exceptionally(exceptionHandler::handle);
  }

  private CompletionStage<HttpResponse> persistTable(CompletionStage<User> userF, NewTable form) {

    var cLog = genLog((User user) -> log.info(LOG_CREATE_TAB, user.getEmail(), form));

    return userF
        .thenApply(cLog::apply)
        .thenApply(Authorization::canWriteTables)
        .thenApply(user -> new UserAndForm<NewTable>(user, form))
        .thenApply(UserAndForm::validate)
        .thenApply(uaf -> toNewTableRequest(uaf.getUser(), uaf.getForm()))
        .thenCompose(tableService::saveTable)
        .thenApply(table -> response.jsonFromObject(table))
        .exceptionally(exceptionHandler::handle);
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

  private UpdateTableRequest toUpdateTableRequest(User user, String tableIdStr, UpdateTable form) {
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

  private Function<User, User> genLog(Consumer<User> c) {
    return (user) -> {
      c.accept(user);
      return user;
    };
  }
}
