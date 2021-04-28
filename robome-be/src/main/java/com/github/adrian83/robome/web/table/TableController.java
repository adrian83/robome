package com.github.adrian83.robome.web.table;

import static com.github.adrian83.robome.common.function.Functions.use;
import static com.github.adrian83.robome.web.common.http.HttpMethod.*;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.auth.Authorization;
import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.domain.common.UserAndForm;
import com.github.adrian83.robome.domain.common.UserContext;
import com.github.adrian83.robome.domain.table.TableService;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.table.model.request.DeleteTableRequest;
import com.github.adrian83.robome.domain.table.model.request.GetTableRequest;
import com.github.adrian83.robome.domain.table.model.request.ListTablesRequest;
import com.github.adrian83.robome.domain.table.model.request.NewTableRequest;
import com.github.adrian83.robome.domain.table.model.request.UpdateTableRequest;
import com.github.adrian83.robome.web.common.Response;
import com.github.adrian83.robome.web.common.Security;
import com.github.adrian83.robome.web.common.routes.OneParamAndFormRoute;
import com.github.adrian83.robome.web.common.routes.OneParamRoute;
import com.github.adrian83.robome.web.common.routes.PrefixRoute;
import com.github.adrian83.robome.web.common.routes.TwoParamsAndFormRoute;
import com.github.adrian83.robome.web.common.routes.TwoParamsRoute;
import com.github.adrian83.robome.web.table.model.NewTable;
import com.github.adrian83.robome.web.table.model.UpdateTable;
import com.google.inject.Inject;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TableController extends AllDirectives {

  private static final String TABLES_PATH = "/users/{userId}/tables/";
  private static final String TABLE_PATH = "/users/{userId}/tables/{tableId}/";

  private static final String LOG_LIST_TABS = "User: {} issued list tables request";
  private static final String LOG_CREATE_TAB = "User: {} issued persist table request, data: {}";
  private static final String LOG_GET_TAB_BY_ID =
      "User: {} issued get table by id request, tableId: {}";
  private static final String LOG_DEL_TAB_BY_ID =
      "User: {} issued delete table by id request, tableId: {}";
  private static final String LOG_UPDATE_TAB =
      "User: {} issued update table request, tableId: {}, data: {}";

  private TableService tableService;
  private Response response;
  private Security security;

  @Inject
  public TableController(TableService tableService, Response response, Security security) {
    this.tableService = tableService;
    this.response = response;
    this.security = security;
  }

  public Route createRoute() {
    return route(
        get(
            new OneParamRoute(
                TABLES_PATH,
                (resourceOwnerId) -> security.secured(resourceOwnerId, this::getTables))),
        get(
            new TwoParamsRoute(
                TABLE_PATH,
                (resourceOwnerId, tabId) ->
                    security.secured(resourceOwnerId, tabId, this::getTableById))),
        post(
            new OneParamAndFormRoute<NewTable>(
                TABLES_PATH,
                NewTable.class,
                (resourceOwnerId, clz) ->
                    security.secured(resourceOwnerId, clz, this::persistTable))),
        put(
            new TwoParamsAndFormRoute<UpdateTable>(
                TABLE_PATH,
                UpdateTable.class,
                (resourceOwnerId, tabId, clz) ->
                    security.secured(resourceOwnerId, tabId, clz, this::updateTable))),
        delete(
            new TwoParamsRoute(
                TABLE_PATH,
                (resourceOwnerId, tabId) ->
                    security.secured(resourceOwnerId, tabId, this::deleteTable))),
        options(new PrefixRoute(TABLES_PATH, complete(response.response200(GET, POST)))),
        options(
            new OneParamRoute(
                TABLE_PATH, (tabId) -> complete(response.response200(GET, PUT, DELETE)))));
  }

  private CompletionStage<HttpResponse> persistTable(
      CompletionStage<UserData> userF, String resourceOwnerIdStr, NewTable form) {

    var cLog = use((UserData user) -> log.info(LOG_CREATE_TAB, user.getEmail(), form));

    return userF
        .thenApply(cLog::apply)
        .thenApply(
            user ->
                UserContext.builder()
                    .loggedInUser(user)
                    .resourceOwner(Optional.of(UUID.fromString(resourceOwnerIdStr)))
                    .build())
        .thenApply(Authorization::canWriteTables)
        .thenApply(
            userCtx -> UserAndForm.<NewTable>builder().userContext(userCtx).form(form).build())
        .thenApply(UserAndForm::validate)
        .thenApply(uaf -> toNewTableRequest(uaf.getUserContext(), uaf.getForm()))
        .thenCompose(tableService::saveTable)
        .thenApply(table -> response.jsonFromObject(table));
  }

  private CompletionStage<HttpResponse> updateTable(
      CompletionStage<UserData> userF,
      String resourceOwnerIdStr,
      String tableIdStr,
      UpdateTable form) {

    var cLog = use((UserData user) -> log.info(LOG_UPDATE_TAB, user.getEmail(), tableIdStr, form));

    return userF
        .thenApply(cLog::apply)
        .thenApply(
            user ->
                UserContext.builder()
                    .loggedInUser(user)
                    .resourceOwner(Optional.of(UUID.fromString(resourceOwnerIdStr)))
                    .build())
        .thenApply(Authorization::canWriteTables)
        .thenApply(
            userCtx -> UserAndForm.<UpdateTable>builder().userContext(userCtx).form(form).build())
        .thenApply(UserAndForm::validate)
        .thenApply(uaf -> toUpdateTableRequest(uaf.getUserContext(), tableIdStr, uaf.getForm()))
        .thenCompose(tableService::updateTable)
        .thenApply(table -> response.jsonFromObject(table));
  }

  private CompletionStage<HttpResponse> deleteTable(
      CompletionStage<UserData> userF, String resourceOwnerIdStr, String tableIdStr) {

    var cLog = use((UserData user) -> log.info(LOG_DEL_TAB_BY_ID, user.getEmail(), tableIdStr));

    return userF
        .thenApply(cLog::apply)
        .thenApply(
            user ->
                UserContext.builder()
                    .loggedInUser(user)
                    .resourceOwner(Optional.of(UUID.fromString(resourceOwnerIdStr)))
                    .build())
        .thenApply(Authorization::canWriteTables)
        .thenApply(u -> toDeleteTableRequest(u, tableIdStr))
        .thenCompose(tableService::deleteTable)
        .thenApply(response::jsonFromObject);
  }

  private CompletionStage<HttpResponse> getTableById(
      CompletionStage<UserData> userF, String resourceOwnerIdStr, String tableIdStr) {

    var cLog = use((UserData user) -> log.info(LOG_GET_TAB_BY_ID, user.getEmail(), tableIdStr));

    return userF
        .thenApply(cLog::apply)
        .thenApply(
            user ->
                UserContext.builder()
                    .loggedInUser(user)
                    .resourceOwner(Optional.of(UUID.fromString(resourceOwnerIdStr)))
                    .build())
        .thenApply(Authorization::canReadTables)
        .thenApply(userCtx -> toGetTableRequest(userCtx, tableIdStr))
        .thenCompose(tableService::getTable)
        .thenApply(response::jsonFromObject);
  }

  private CompletionStage<HttpResponse> getTables(
      CompletionStage<UserData> userF, String resourceOwnerIdStr) {

    var cLog = use((UserData user) -> log.info(LOG_LIST_TABS, user.getEmail()));

    return userF
        .thenApply(cLog::apply)
        .thenApply(
            user ->
                UserContext.builder()
                    .loggedInUser(user)
                    .resourceOwner(Optional.of(UUID.fromString(resourceOwnerIdStr)))
                    .build())
        .thenApply(Authorization::canReadTables)
        .thenApply(this::toListTablesRequest)
        .thenCompose(tableService::getTables)
        .thenApply(response::jsonFromObject);
  }

  private ListTablesRequest toListTablesRequest(UserContext userCtx) {
    return ListTablesRequest.builder().userId(userCtx.resourceOwnerIdOrError()).build();
  }

  private GetTableRequest toGetTableRequest(UserContext userCtx, String tableIdStr) {

    return GetTableRequest.builder()
        .userId(userCtx.resourceOwnerIdOrError())
        .tableKey(TableKey.parse(tableIdStr))
        .build();
  }

  private DeleteTableRequest toDeleteTableRequest(UserContext userCtx, String tableIdStr) {
    return DeleteTableRequest.builder()
        .userId(userCtx.resourceOwnerIdOrError())
        .tableKey(TableKey.parse(tableIdStr))
        .build();
  }

  private UpdateTableRequest toUpdateTableRequest(
      UserContext userCtx, String tableIdStr, UpdateTable form) {
    return UpdateTableRequest.builder()
        .tableKey(TableKey.parse(tableIdStr))
        .userId(userCtx.resourceOwnerIdOrError())
        .title(form.getTitle())
        .description(form.getDescription())
        .build();
  }

  private NewTableRequest toNewTableRequest(UserContext userCtx, NewTable form) {
    return NewTableRequest.builder()
        .userId(userCtx.resourceOwnerIdOrError())
        .title(form.getTitle())
        .description(form.getDescription())
        .build();
  }
}
