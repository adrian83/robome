package com.github.adrian83.robome.web.stage;

import static com.github.adrian83.robome.common.Logging.logAction;
import static com.github.adrian83.robome.domain.common.UserContext.withUserAndResourceOwnerId;
import static com.github.adrian83.robome.web.common.http.HttpMethod.*;
import static java.util.UUID.fromString;

import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.domain.common.UserAndForm;
import com.github.adrian83.robome.domain.common.UserContext;
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
import com.github.adrian83.robome.web.common.routes.ThreeParamsAndFormRoute;
import com.github.adrian83.robome.web.common.routes.ThreeParamsRoute;
import com.github.adrian83.robome.web.common.routes.TwoParamsAndFormRoute;
import com.github.adrian83.robome.web.common.routes.TwoParamsRoute;
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
  private static final String LOG_GET_STG_BY_ID =
      "get stage by id request, tableId: {}, stageId: {}";
  private static final String LOG_DEL_STG_BY_ID =
      "delete stage by id request, tableId: {}, stageId: {}";
  private static final String LOG_UPDATE_STG =
      "update stage request, tableId: {}, stageId: {}, data: {}";

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
        get(
            new TwoParamsRoute(
                STAGES_PATH,
                (resourceOwnerId, tabId) ->
                    security.secured(resourceOwnerId, tabId, this::getTableStages))),
        get(
            new ThreeParamsRoute(
                STAGE_PATH,
                (resourceOwnerId, tabId, stgId) ->
                    security.secured(resourceOwnerId, tabId, stgId, this::getStageById))),
        delete(
            new ThreeParamsRoute(
                STAGE_PATH,
                (resourceOwnerId, tabId, stgId) ->
                    security.secured(resourceOwnerId, tabId, stgId, this::deleteStage))),
        post(
            new TwoParamsAndFormRoute<NewStage>(
                STAGES_PATH,
                NewStage.class,
                (resourceOwnerId, tabId, clz) ->
                    security.secured(resourceOwnerId, tabId, clz, this::persistStage))),
        put(
            new ThreeParamsAndFormRoute<UpdateStage>(
                STAGE_PATH,
                UpdateStage.class,
                (resourceOwnerId, tabId, stgId, clz) ->
                    security.secured(resourceOwnerId, tabId, stgId, clz, this::updateStage))),
        options(
            new TwoParamsRoute(
                STAGES_PATH,
                (resourceOwnerId, tabId) -> complete(response.response200(GET, POST)))),
        options(
            new ThreeParamsRoute(
                STAGE_PATH,
                (resourceOwnerId, tabId, stgId) ->
                    complete(response.response200(GET, DELETE, PUT)))));
  }

  private CompletionStage<HttpResponse> persistStage(
      UserData user,
      String resourceOwnerIdStr,
      String tableIdStr,
      NewStage form) {

    return logAction(LOGGER, user, LOG_CREATE_STG, tableIdStr, form)
        .thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerIdStr)))
        .thenApply(Authorization::canWriteStages)
        .thenApply(userCtx -> new UserAndForm<NewStage>(userCtx, form))
        .thenApply(UserAndForm::validate)
        .thenApply(uaf -> toNewStageRequest(uaf.userContext(), tableIdStr, uaf.form()))
        .thenCompose(stageService::saveStage)
        .thenApply(response::jsonFromObject);
  }

  private CompletionStage<HttpResponse> updateStage(
      UserData user,
      String resourceOwnerIdStr,
      String tableIdStr,
      String stageIdStr,
      UpdateStage form) {

    return logAction(LOGGER, user, LOG_UPDATE_STG, tableIdStr, stageIdStr, form)
        .thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerIdStr)))
        .thenApply(Authorization::canWriteStages)
        .thenApply(userCtx -> new UserAndForm<UpdateStage>(userCtx, form))
        .thenApply(UserAndForm::validate)
        .thenApply(
            uaf -> toUpdateStageRequest(uaf.userContext(), tableIdStr, stageIdStr, uaf.form()))
        .thenCompose(stageService::updateStage)
        .thenApply(response::jsonFromObject);
  }

  private CompletionStage<HttpResponse> deleteStage(
      UserData user,
      String resourceOwnerIdStr,
      String tableIdStr,
      String stageIdStr) {

    return logAction(LOGGER, user, LOG_DEL_STG_BY_ID, tableIdStr, stageIdStr)
        .thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerIdStr)))
        .thenApply(Authorization::canWriteStages)
        .thenApply(userCtx -> toDeleteStageRequest(userCtx, tableIdStr, stageIdStr))
        .thenCompose(stageService::deleteStage)
        .thenApply(response::jsonFromObject);
  }

  private CompletionStage<HttpResponse> getStageById(
      UserData user,
      String resourceOwnerIdStr,
      String tableIdStr,
      String stageIdStr) {

    return logAction(LOGGER, user, LOG_GET_STG_BY_ID, tableIdStr, stageIdStr)
        .thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerIdStr)))
        .thenApply(Authorization::canReadStages)
        .thenApply(userCtx -> toGetStageRequest(userCtx, tableIdStr, stageIdStr))
        .thenCompose(stageService::getStage)
        .thenApply(response::jsonFromOptional);
  }

  private CompletionStage<HttpResponse> getTableStages(
      UserData user, 
      String resourceOwnerIdStr, 
      String tableIdStr) {

    return logAction(LOGGER, user, LOG_LIST_STGS, tableIdStr)
        .thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerIdStr)))
        .thenApply(Authorization::canReadStages)
        .thenApply(userCtx -> toListTableStagesRequest(userCtx, tableIdStr))
        .thenCompose(stageService::getTableStages)
        .thenApply(response::jsonFromObject);
  }

  private ListTableStagesRequest toListTableStagesRequest(UserContext userCtx, String tableIdStr) {
    return new ListTableStagesRequest(TableKey.parse(userCtx.resourceOwnerIdOrError(), tableIdStr));
  }

  private DeleteStageRequest toDeleteStageRequest(
      UserContext userCtx, String tableIdStr, String stageIdStr) {
    return new DeleteStageRequest(StageKey.parse(userCtx.resourceOwnerIdOrError(), tableIdStr, stageIdStr));
  }

  private UpdateStageRequest toUpdateStageRequest(
      UserContext userCtx, String tableIdStr, String stageIdStr, UpdateStage form) {

    return new UpdateStageRequest(
        form.title(), StageKey.parse(userCtx.resourceOwnerIdOrError(), tableIdStr, stageIdStr));
  }

  private NewStageRequest toNewStageRequest(UserContext userCtx, String tableIdStr, NewStage form) {
    return new NewStageRequest(
        form.title(), userCtx.resourceOwnerIdOrError(), TableKey.parse(userCtx.resourceOwnerIdOrError(), tableIdStr));
  }

  private GetStageRequest toGetStageRequest(
      UserContext userCtx, String tableIdStr, String stageIdStr) {

    return new GetStageRequest(StageKey.parse(userCtx.resourceOwnerIdOrError(), tableIdStr, stageIdStr));
  }
}
