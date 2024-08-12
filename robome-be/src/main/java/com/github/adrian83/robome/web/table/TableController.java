package com.github.adrian83.robome.web.table;

import static java.util.UUID.fromString;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.auth.model.UserData;
import static com.github.adrian83.robome.common.Logging.logAction;
import com.github.adrian83.robome.domain.common.UserAndForm;
import com.github.adrian83.robome.domain.common.UserContext;
import static com.github.adrian83.robome.domain.common.UserContext.withUserAndResourceOwnerId;
import com.github.adrian83.robome.domain.table.TableService;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.table.model.request.DeleteTableCommand;
import com.github.adrian83.robome.domain.table.model.request.GetTableQuery;
import com.github.adrian83.robome.domain.table.model.request.ListTablesQuery;
import com.github.adrian83.robome.domain.table.model.request.NewTableCommand;
import com.github.adrian83.robome.domain.table.model.request.UpdateTableCommand;
import com.github.adrian83.robome.web.auth.Authorization;
import com.github.adrian83.robome.web.common.Response;
import com.github.adrian83.robome.web.common.Security;
import static com.github.adrian83.robome.web.common.http.HttpMethod.DELETE;
import static com.github.adrian83.robome.web.common.http.HttpMethod.GET;
import static com.github.adrian83.robome.web.common.http.HttpMethod.POST;
import static com.github.adrian83.robome.web.common.http.HttpMethod.PUT;
import com.github.adrian83.robome.web.common.routes.OneParamAndFormRoute;
import com.github.adrian83.robome.web.common.routes.OneParamRoute;
import com.github.adrian83.robome.web.common.routes.TwoParamsAndFormRoute;
import com.github.adrian83.robome.web.common.routes.TwoParamsRoute;
import com.github.adrian83.robome.web.table.model.NewTable;
import com.github.adrian83.robome.web.table.model.UpdateTable;
import com.google.inject.Inject;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class TableController extends AllDirectives {

    private static final Logger LOGGER = LoggerFactory.getLogger(TableController.class);

    private static final String TABLES_PATH = "/users/{userId}/tables/";
    private static final String TABLE_PATH = "/users/{userId}/tables/{tableId}/";

    private static final String LOG_LIST_TABS = "list tables request";
    private static final String LOG_CREATE_TAB = "persist table request, data: {}";
    private static final String LOG_GET_TAB_BY_ID = "get table by id request, tableId: {}";
    private static final String LOG_DEL_TAB_BY_ID = "delete table by id request, tableId: {}";
    private static final String LOG_UPDATE_TAB = "update table request, tableId: {}, data: {}";

    private final TableService tableService;
    private final Response response;
    private final Security security;

    @Inject
    public TableController(TableService tableService, Response response, Security security) {
        this.tableService = tableService;
        this.response = response;
        this.security = security;
    }

    public Route createRoute() {
        return route(
                get(new OneParamRoute(TABLES_PATH, (resourceOwnerId) -> security.secured(resourceOwnerId, this::getTables))),
                get(new TwoParamsRoute(TABLE_PATH, (resourceOwnerId, tabId) -> security.secured(resourceOwnerId, tabId, this::getTableById))),
                post(new OneParamAndFormRoute<>(TABLES_PATH, NewTable.class, (resourceOwnerId, clz) -> security.secured(resourceOwnerId, clz, this::persistTable))),
                put(new TwoParamsAndFormRoute<>(TABLE_PATH, UpdateTable.class, (resourceOwnerId, tabId, clz) -> security.secured(resourceOwnerId, tabId, clz, this::updateTable))),
                delete(new TwoParamsRoute(TABLE_PATH, (resourceOwnerId, tabId) -> security.secured(resourceOwnerId, tabId, this::deleteTable))),
                options(new TwoParamsRoute(TABLE_PATH, (resourceOwnerId, tabId) -> complete(response.response200(GET, PUT, DELETE)))),
                options(new OneParamRoute(TABLES_PATH, (resourceOwnerId) -> complete(response.response200(GET, POST))))
        );
    }

    private CompletionStage<HttpResponse> persistTable(UserData user, String resourceOwnerIdStr, NewTable form) {
        
        return logAction(LOGGER, user, LOG_CREATE_TAB, form)
                .thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerIdStr)))
                .thenApply(Authorization::canWriteTables)
                .thenApply(userCtx -> new UserAndForm<NewTable>(userCtx, form))
                .thenApply(UserAndForm::validate)
                .thenApply(uaf -> toNewTableRequest(uaf.userContext(), uaf.form()))
                .thenCompose(tableService::saveTable)
                .thenApply(table -> response.jsonFromObject(table));
    }

    private CompletionStage<HttpResponse> updateTable(UserData user, String resourceOwnerIdStr, String tableIdStr,
            UpdateTable form) {

        return logAction(LOGGER, user, LOG_UPDATE_TAB, tableIdStr, form)
                .thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerIdStr)))
                .thenApply(Authorization::canWriteTables)
                .thenApply(userCtx -> new UserAndForm<UpdateTable>(userCtx, form)).thenApply(UserAndForm::validate)
                .thenApply(uaf -> toUpdateTableRequest(uaf.userContext(), tableIdStr, uaf.form()))
                .thenCompose(tableService::updateTable).thenApply(table -> response.jsonFromObject(table));
    }

    private CompletionStage<HttpResponse> deleteTable(UserData user, String resourceOwnerIdStr, String tableIdStr) {

        return logAction(LOGGER, user, LOG_DEL_TAB_BY_ID, tableIdStr)
                .thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerIdStr)))
                .thenApply(Authorization::canWriteTables).thenApply(u -> toDeleteTableRequest(u, tableIdStr))
                .thenCompose(tableService::deleteTable).thenApply(response::jsonFromObject);
    }

    private CompletionStage<HttpResponse> getTableById(UserData user, String resourceOwnerIdStr, String tableIdStr) {

        return logAction(LOGGER, user, LOG_GET_TAB_BY_ID, tableIdStr)
                .thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerIdStr)))
                .thenApply(Authorization::canReadTables).thenApply(userCtx -> toGetTableRequest(userCtx, tableIdStr))
                .thenCompose(tableService::getTable).thenApply(response::jsonFromOptional);
    }

    private CompletionStage<HttpResponse> getTables(UserData user, String resourceOwnerIdStr) {

        return logAction(LOGGER, user, LOG_LIST_TABS)
                .thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerIdStr)))
                .thenApply(Authorization::canReadTables).thenApply(this::toListTablesRequest)
                .thenCompose(tableService::getTables).thenApply(response::jsonFromObject);
    }

    private ListTablesQuery toListTablesRequest(UserContext userCtx) {
        return new ListTablesQuery(userCtx.resourceOwnerIdOrError());
    }

    private GetTableQuery toGetTableRequest(UserContext userCtx, String tableIdStr) {
        return new GetTableQuery(userCtx.resourceOwnerIdOrError(),
                TableKey.create(userCtx.resourceOwnerIdOrError(), tableIdStr));
    }

    private DeleteTableCommand toDeleteTableRequest(UserContext userCtx, String tableIdStr) {
        return new DeleteTableCommand(userCtx.resourceOwnerIdOrError(),
                TableKey.create(userCtx.resourceOwnerIdOrError(), tableIdStr));
    }

    private UpdateTableCommand toUpdateTableRequest(UserContext userCtx, String tableIdStr, UpdateTable form) {
        return new UpdateTableCommand(form.title(), form.description(), userCtx.resourceOwnerIdOrError(),
                TableKey.create(userCtx.resourceOwnerIdOrError(), tableIdStr));
    }

    private NewTableCommand toNewTableRequest(UserContext userCtx, NewTable form) {
        return new NewTableCommand(form.title(), form.description(), userCtx.resourceOwnerIdOrError());
    }
}
