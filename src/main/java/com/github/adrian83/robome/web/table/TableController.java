package com.github.adrian83.robome.web.table;

import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.auth.model.UserData;
import static com.github.adrian83.robome.common.Logging.logAction;
import com.github.adrian83.robome.domain.common.UserAndForm;
import com.github.adrian83.robome.domain.table.TableService;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.table.model.request.DeleteTableRequest;
import com.github.adrian83.robome.domain.table.model.request.GetTableRequest;
import com.github.adrian83.robome.domain.table.model.request.ListTablesRequest;
import com.github.adrian83.robome.domain.table.model.request.NewTableRequest;
import com.github.adrian83.robome.domain.table.model.request.UpdateTableRequest;
import com.github.adrian83.robome.web.auth.Authorization;
import com.github.adrian83.robome.web.common.PathParams;
import com.github.adrian83.robome.web.common.Response;
import com.github.adrian83.robome.web.common.Security;
import static com.github.adrian83.robome.web.common.http.HttpMethod.DELETE;
import static com.github.adrian83.robome.web.common.http.HttpMethod.GET;
import static com.github.adrian83.robome.web.common.http.HttpMethod.POST;
import static com.github.adrian83.robome.web.common.http.HttpMethod.PUT;
import com.github.adrian83.robome.web.common.routes.RouteSupplier;
import com.github.adrian83.robome.web.table.model.NewTable;
import com.github.adrian83.robome.web.table.model.UpdateTable;
import com.google.inject.Inject;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class TableController extends AllDirectives implements PathParams {

    private static final Logger LOGGER = LoggerFactory.getLogger(TableController.class);

    private static final String TABLES_PATH = "/api/v1/tables/";
    private static final String TABLE_PATH = "/api/v1/tables/{" + PATH_PARAM_TABLE_ID + "}/";

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
                get(new RouteSupplier(TABLE_PATH, (pathParams) -> security.secured(pathParams, this::getTableById))),
                get(new RouteSupplier(TABLES_PATH, (pathParams) -> security.secured(pathParams, this::getTables))),
                post(new RouteSupplier(TABLES_PATH, (pathParams) -> security.secured(pathParams, NewTable.class, this::persistTable))),
                put(new RouteSupplier(TABLE_PATH, (pathParams) -> security.secured(pathParams, UpdateTable.class, this::updateTable))),
                delete(new RouteSupplier(TABLE_PATH, (pathParams) -> security.secured(pathParams, this::deleteTable))),
                options(new RouteSupplier(TABLE_PATH, (pathParams) -> complete(response.response200(GET, PUT, DELETE)))),
                options(new RouteSupplier(TABLES_PATH, (pathParams) -> complete(response.response200(GET, POST))))
        );
    }

    private CompletionStage<HttpResponse> persistTable(UserData userData, Map<String, String> pathParams, NewTable form) {
        return logAction(LOGGER, userData, LOG_CREATE_TAB, form)
                .thenApply(Authorization::canWriteTables)
                .thenApply(ud -> new UserAndForm<NewTable>(userData, form))
                .thenApply(UserAndForm::validate)
                .thenApply(uaf -> toNewTableRequest(uaf.userData(), uaf.form()))
                .thenCompose(tableService::saveTable)
                .thenApply(table -> response.jsonFromObject(table));
    }

    private CompletionStage<HttpResponse> updateTable(UserData userData, Map<String, String> pathParams, UpdateTable form) {
        String tableIdStr = pathParams.get(PATH_PARAM_TABLE_ID);

        return logAction(LOGGER, userData, LOG_UPDATE_TAB, tableIdStr, form)
                .thenApply(Authorization::canWriteTables)
                .thenApply(ud -> new UserAndForm<UpdateTable>(userData, form))
                .thenApply(UserAndForm::validate)
                .thenApply(uaf -> toUpdateTableRequest(uaf.userData(), tableIdStr, uaf.form()))
                .thenCompose(tableService::updateTable)
                .thenApply(table -> response.jsonFromObject(table));
    }

    private CompletionStage<HttpResponse> deleteTable(UserData user, Map<String, String> pathParams) {
        String tableIdStr = pathParams.get(PATH_PARAM_TABLE_ID);

        return logAction(LOGGER, user, LOG_DEL_TAB_BY_ID, tableIdStr)
                .thenApply(Authorization::canWriteTables)
                .thenApply(u -> toDeleteTableRequest(u, tableIdStr))
                .thenCompose(tableService::deleteTable)
                .thenApply(response::jsonFromObject);
    }

    private CompletionStage<HttpResponse> getTableById(UserData user, Map<String, String> pathParams) {
        String tableIdStr = pathParams.get(PATH_PARAM_TABLE_ID);

        return logAction(LOGGER, user, LOG_GET_TAB_BY_ID, tableIdStr)
                .thenApply(Authorization::canReadTables)
                .thenApply(userCtx -> toGetTableRequest(userCtx, tableIdStr))
                .thenCompose(tableService::getTable)
                .thenApply(response::jsonFromOptional);
    }

    private CompletionStage<HttpResponse> getTables(UserData user, Map<String, String> pathParams) {
        return logAction(LOGGER, user, LOG_LIST_TABS)
                .thenApply(Authorization::canReadTables)
                .thenApply(this::toListTablesRequest)
                .thenCompose(tableService::getTables)
                .thenApply(response::jsonFromObject);
    }

    private ListTablesRequest toListTablesRequest(UserData userData) {
        return new ListTablesRequest(userData.id());
    }

    private GetTableRequest toGetTableRequest(UserData userData, String tableIdStr) {
        return new GetTableRequest(
                userData.id(),
                TableKey.create(userData.id(), tableIdStr)
        );
    }

    private DeleteTableRequest toDeleteTableRequest(UserData userData, String tableIdStr) {
        return new DeleteTableRequest(
                userData.id(),
                TableKey.create(userData.id(), tableIdStr)
        );
    }

    private UpdateTableRequest toUpdateTableRequest(UserData userData, String tableIdStr, UpdateTable form) {
        return new UpdateTableRequest(
                form.title(),
                form.description(),
                userData.id(),
                TableKey.create(userData.id(), tableIdStr)
        );
    }

    private NewTableRequest toNewTableRequest(UserData userData, NewTable form) {
        return new NewTableRequest(
                form.title(),
                form.description(),
                userData.id()
        );
    }
}
