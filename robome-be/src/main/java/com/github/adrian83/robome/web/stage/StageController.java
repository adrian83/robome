package com.github.adrian83.robome.web.stage;

import java.util.Map;
import static java.util.UUID.fromString;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.auth.model.UserData;
import static com.github.adrian83.robome.common.Logging.logAction;
import com.github.adrian83.robome.domain.common.UserAndForm;
import com.github.adrian83.robome.domain.common.UserContext;
import static com.github.adrian83.robome.domain.common.UserContext.withUserAndResourceOwnerId;
import com.github.adrian83.robome.domain.stage.StageService;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.domain.stage.model.request.DeleteStageRequest;
import com.github.adrian83.robome.domain.stage.model.request.GetStageRequest;
import com.github.adrian83.robome.domain.stage.model.request.ListTableStagesRequest;
import com.github.adrian83.robome.domain.stage.model.request.NewStageRequest;
import com.github.adrian83.robome.domain.stage.model.request.UpdateStageRequest;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.web.auth.Authorization;
import com.github.adrian83.robome.web.common.Response;
import com.github.adrian83.robome.web.common.Security;
import static com.github.adrian83.robome.web.common.http.HttpMethod.DELETE;
import static com.github.adrian83.robome.web.common.http.HttpMethod.GET;
import static com.github.adrian83.robome.web.common.http.HttpMethod.POST;
import static com.github.adrian83.robome.web.common.http.HttpMethod.PUT;
import com.github.adrian83.robome.web.common.routes.RouteSupplier;
import com.github.adrian83.robome.web.stage.model.NewStage;
import com.github.adrian83.robome.web.stage.model.UpdateStage;
import com.google.inject.Inject;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class StageController extends AllDirectives {

    private static final Logger LOGGER = LoggerFactory.getLogger(StageController.class);

    private static final String STAGES_PATH = "/users/{userId}/tables/{tableId}/stages/";
    private static final String STAGE_PATH = "/users/{userId}/tables/{tableId}/stages/{stageId}/";

    private static final String LOG_LIST_STGS = "list stages by table request, tableId: {}";
    private static final String LOG_CREATE_STG = "persist stage request, tableId: {}, data: {}";
    private static final String LOG_GET_STG_BY_ID = "get stage by id request, tableId: {}, stageId: {}";
    private static final String LOG_DEL_STG_BY_ID = "delete stage by id request, tableId: {}, stageId: {}";
    private static final String LOG_UPDATE_STG = "update stage request, tableId: {}, stageId: {}, data: {}";

    private StageService stageService;
    private Response response;
    private Security security;

    @Inject
    public StageController(StageService stageService, Response response, Security security) {
        this.stageService = stageService;
        this.response = response;
        this.security = security;
    }

    public Route createRoute() {
        return route(
                get(new RouteSupplier(STAGE_PATH, (pathParams) -> security.secured2(pathParams, this::getStageById))),
                get(new RouteSupplier(STAGES_PATH, (pathParams) -> security.secured2(pathParams, this::getTableStages))),
                delete(new RouteSupplier(STAGE_PATH, (pathParams) -> security.secured2(pathParams, this::deleteStage))),
                post(new RouteSupplier(STAGES_PATH, (pathParams) -> security.secured2(pathParams, NewStage.class, this::persistStage))),
                put(new RouteSupplier(STAGE_PATH, (pathParams) -> security.secured2(pathParams, UpdateStage.class, this::updateStage))),
                options(new RouteSupplier(STAGE_PATH, (pathParams) -> complete(response.response200(GET, DELETE, PUT)))),
                options(new RouteSupplier(STAGES_PATH, (pathParams) -> complete(response.response200(GET, POST))))
        );
    }

    private CompletionStage<HttpResponse> persistStage(UserData user, Map<String, String> pathParams, NewStage form) {
        final String resourceOwnerIdStr = pathParams.get("userId");
        final String tableIdStr = pathParams.get("tableId");

        return logAction(LOGGER, user, LOG_CREATE_STG, tableIdStr, form)
                .thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerIdStr)))
                .thenApply(Authorization::canWriteStages).thenApply(userCtx -> new UserAndForm<NewStage>(userCtx, form))
                .thenApply(UserAndForm::validate)
                .thenApply(uaf -> toNewStageRequest(uaf.userContext(), tableIdStr, uaf.form()))
                .thenCompose(stageService::saveStage).thenApply(response::jsonFromObject);
    }

    private CompletionStage<HttpResponse> updateStage(UserData user, Map<String, String> pathParams, UpdateStage form) {
        final String resourceOwnerIdStr = pathParams.get("userId");
        final String tableIdStr = pathParams.get("tableId");
        final String stageIdStr = pathParams.get("stageId");

        return logAction(LOGGER, user, LOG_UPDATE_STG, tableIdStr, stageIdStr, form)
                .thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerIdStr)))
                .thenApply(Authorization::canWriteStages)
                .thenApply(userCtx -> new UserAndForm<UpdateStage>(userCtx, form)).thenApply(UserAndForm::validate)
                .thenApply(uaf -> toUpdateStageRequest(uaf.userContext(), tableIdStr, stageIdStr, uaf.form()))
                .thenCompose(stageService::updateStage).thenApply(response::jsonFromObject);
    }

    private CompletionStage<HttpResponse> deleteStage(UserData user, Map<String, String> pathParams) {
        final String resourceOwnerIdStr = pathParams.get("userId");
        final String tableIdStr = pathParams.get("tableId");
        final String stageIdStr = pathParams.get("stageId");

        return logAction(LOGGER, user, LOG_DEL_STG_BY_ID, tableIdStr, stageIdStr)
                .thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerIdStr)))
                .thenApply(Authorization::canWriteStages)
                .thenApply(userCtx -> toDeleteStageRequest(userCtx, tableIdStr, stageIdStr))
                .thenCompose(stageService::deleteStage).thenApply(response::jsonFromObject);
    }

    private CompletionStage<HttpResponse> getStageById(UserData user, Map<String, String> pathParams) {
        final String resourceOwnerIdStr = pathParams.get("userId");
        final String tableIdStr = pathParams.get("tableId");
        final String stageIdStr = pathParams.get("stageId");

        return logAction(LOGGER, user, LOG_GET_STG_BY_ID, tableIdStr, stageIdStr)
                .thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerIdStr)))
                .thenApply(Authorization::canReadStages)
                .thenApply(userCtx -> toGetStageRequest(userCtx, tableIdStr, stageIdStr))
                .thenCompose(stageService::getStage).thenApply(response::jsonFromOptional);
    }

    private CompletionStage<HttpResponse> getTableStages(UserData user, Map<String, String> pathParams) {
        final String resourceOwnerIdStr = pathParams.get("userId");
        final String tableIdStr = pathParams.get("tableId");

        return logAction(LOGGER, user, LOG_LIST_STGS, tableIdStr)
                .thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerIdStr)))
                .thenApply(Authorization::canReadStages)
                .thenApply(userCtx -> toListTableStagesRequest(userCtx, tableIdStr))
                .thenCompose(stageService::getTableStages).thenApply(response::jsonFromObject);
    }

    private ListTableStagesRequest toListTableStagesRequest(UserContext userCtx, String tableIdStr) {
        return new ListTableStagesRequest(TableKey.create(userCtx.resourceOwnerIdOrError(), tableIdStr));
    }

    private DeleteStageRequest toDeleteStageRequest(UserContext userCtx, String tableIdStr, String stageIdStr) {
        return new DeleteStageRequest(StageKey.create(userCtx.resourceOwnerIdOrError(), tableIdStr, stageIdStr));
    }

    private UpdateStageRequest toUpdateStageRequest(UserContext userCtx, String tableIdStr, String stageIdStr, UpdateStage form) {
        return new UpdateStageRequest(form.title(), StageKey.create(userCtx.resourceOwnerIdOrError(), tableIdStr, stageIdStr));
    }

    private NewStageRequest toNewStageRequest(UserContext userCtx, String tableIdStr, NewStage form) {
        return new NewStageRequest(form.title(), userCtx.resourceOwnerIdOrError(),
                TableKey.create(userCtx.resourceOwnerIdOrError(), tableIdStr));
    }

    private GetStageRequest toGetStageRequest(UserContext userCtx, String tableIdStr, String stageIdStr) {

        return new GetStageRequest(StageKey.create(userCtx.resourceOwnerIdOrError(), tableIdStr, stageIdStr));
    }
}
