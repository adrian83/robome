package com.github.adrian83.robome.web.stage;

import static com.github.adrian83.robome.common.function.Functions.use;
import static com.github.adrian83.robome.web.common.http.HttpMethod.*;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.auth.Authorization;
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
import com.github.adrian83.robome.web.common.Response;
import com.github.adrian83.robome.web.common.Security;
import com.github.adrian83.robome.web.common.routes.OneParamRoute;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StageController extends AllDirectives {

  private static final String STAGES_PATH = "/users/{userId}/tables/{tableId}/stages/";
  private static final String STAGE_PATH = "/users/{userId}/tables/{tableId}/stages/{stageId}/";

  private static final String LOG_LIST_STGS =
      "User: {} issued list stages by table request, tableId: {}";
  private static final String LOG_CREATE_STG =
      "User: {} issued persist stage request, tableId: {}, data: {}";
  private static final String LOG_GET_STG_BY_ID =
      "User: {} issued get stage by id request, tableId: {}, stageId: {}";
  private static final String LOG_DEL_STG_BY_ID =
      "User: {} issued delete stage by id request, tableId: {}, stageId: {}";
  private static final String LOG_UPDATE_STG =
      "User: {} issued update stage request, tableId: {}, stageId: {}, data: {}";

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
                (resourceOwnerIdStr, tabId) ->
                    security.secured(resourceOwnerIdStr, tabId, this::getTableStages))),
        get(
            new ThreeParamsRoute(
                STAGE_PATH,
                (resourceOwnerIdStr, tabId, stgId) ->
                    security.secured(resourceOwnerIdStr, tabId, stgId, this::getStageById))),
        delete(
            new ThreeParamsRoute(
                STAGE_PATH,
                (resourceOwnerIdStr, tabId, stgId) ->
                    security.secured(resourceOwnerIdStr, tabId, stgId, this::deleteStage))),
        post(
            new TwoParamsAndFormRoute<NewStage>(
                STAGES_PATH,
                NewStage.class,
                (resourceOwnerIdStr, tabId, clz) ->
                    security.secured(resourceOwnerIdStr, tabId, clz, this::persistStage))),
        put(
            new ThreeParamsAndFormRoute<UpdateStage>(
                STAGE_PATH,
                UpdateStage.class,
                (resourceOwnerIdStr, tabId, stgId, clz) ->
                    security.secured(resourceOwnerIdStr, tabId, stgId, clz, this::updateStage))),
        options(
            new OneParamRoute(STAGES_PATH, (tabId) -> complete(response.response200(GET, POST)))),
        options(
            new TwoParamsRoute(
                STAGE_PATH, (tabId, stgId) -> complete(response.response200(GET, DELETE, PUT)))));
  }

  private CompletionStage<HttpResponse> persistStage(
      CompletionStage<UserData> userF,
      String resourceOwnerIdStr,
      String tableIdStr,
      NewStage form) {

    var cLog = use((UserData user) -> log.info(LOG_CREATE_STG, user.getEmail(), tableIdStr, form));

    return userF
        .thenApply(cLog::apply)
        .thenApply(
            user ->
                UserContext.builder()
                    .loggedInUser(user)
                    .resourceOwner(Optional.of(UUID.fromString(resourceOwnerIdStr)))
                    .build())
        .thenApply(Authorization::canWriteStages)
        .thenApply(
            userCtx -> UserAndForm.<NewStage>builder().userContext(userCtx).form(form).build())
        .thenApply(UserAndForm::validate)
        .thenApply(uaf -> toNewStageRequest(uaf.getUserContext(), tableIdStr, uaf.getForm()))
        .thenCompose(stageService::saveStage)
        .thenApply(response::jsonFromObject);
  }

  private CompletionStage<HttpResponse> updateStage(
      CompletionStage<UserData> userF,
      String resourceOwnerIdStr,
      String tableIdStr,
      String stageIdStr,
      UpdateStage form) {

    var cLog =
        use(
            (UserData user) ->
                log.info(LOG_UPDATE_STG, user.getEmail(), tableIdStr, stageIdStr, form));

    return userF
        .thenApply(cLog::apply)
        .thenApply(
            user ->
                UserContext.builder()
                    .loggedInUser(user)
                    .resourceOwner(Optional.of(UUID.fromString(resourceOwnerIdStr)))
                    .build())
        .thenApply(Authorization::canWriteStages)
        .thenApply(
            userCtx -> UserAndForm.<UpdateStage>builder().userContext(userCtx).form(form).build())
        .thenApply(UserAndForm::validate)
        .thenApply(
            uaf ->
                toUpdateStageRequest(uaf.getUserContext(), tableIdStr, stageIdStr, uaf.getForm()))
        .thenCompose(stageService::updateStage)
        .thenApply(response::jsonFromObject);
  }

  private CompletionStage<HttpResponse> deleteStage(
      CompletionStage<UserData> userF,
      String resourceOwnerIdStr,
      String tableIdStr,
      String stageIdStr) {

    var cLog =
        use(
            (UserData user) ->
                log.info(LOG_DEL_STG_BY_ID, user.getEmail(), tableIdStr, stageIdStr));

    return userF
        .thenApply(cLog::apply)
        .thenApply(
            user ->
                UserContext.builder()
                    .loggedInUser(user)
                    .resourceOwner(Optional.of(UUID.fromString(resourceOwnerIdStr)))
                    .build())
        .thenApply(Authorization::canWriteStages)
        .thenApply(userCtx -> toDeleteStageRequest(userCtx, tableIdStr, stageIdStr))
        .thenCompose(stageService::deleteStage)
        .thenApply(response::jsonFromObject);
  }

  private CompletionStage<HttpResponse> getStageById(
      CompletionStage<UserData> userF,
      String resourceOwnerIdStr,
      String tableIdStr,
      String stageIdStr) {

    var cLog =
        use(
            (UserData user) ->
                log.info(LOG_GET_STG_BY_ID, user.getEmail(), tableIdStr, stageIdStr));

    return userF
        .thenApply(cLog::apply)
        .thenApply(
            user ->
                UserContext.builder()
                    .loggedInUser(user)
                    .resourceOwner(Optional.of(UUID.fromString(resourceOwnerIdStr)))
                    .build())
        .thenApply(Authorization::canReadStages)
        .thenApply(userCtx -> toGetStageRequest(userCtx, tableIdStr, stageIdStr))
        .thenCompose(stageService::getStage)
        .thenApply(response::jsonFromObject);
  }

  private CompletionStage<HttpResponse> getTableStages(
      CompletionStage<UserData> userF, String resourceOwnerIdStr, String tableIdStr) {

    var cLog = use((UserData user) -> log.info(LOG_LIST_STGS, user.getEmail(), tableIdStr));

    return userF
        .thenApply(cLog::apply)
        .thenApply(
            userData ->
                UserContext.builder()
                    .loggedInUser(userData)
                    .resourceOwner(Optional.of(UUID.fromString(resourceOwnerIdStr)))
                    .build())
        .thenApply(Authorization::canReadStages)
        .thenApply(userCtx -> toListTableStagesRequest(userCtx, tableIdStr))
        .thenCompose(stageService::getTableStages)
        .thenApply(response::jsonFromObject);
  }

  private ListTableStagesRequest toListTableStagesRequest(UserContext userCtx, String tableIdStr) {

    return ListTableStagesRequest.builder()
        .userId(userCtx.resourceOwnerIdOrError())
        .tableKey(TableKey.parse(tableIdStr))
        .build();
  }

  private DeleteStageRequest toDeleteStageRequest(
      UserContext userCtx, String tableIdStr, String stageIdStr) {

    return DeleteStageRequest.builder()
        .stageKey(StageKey.parse(tableIdStr, stageIdStr))
        .userId(userCtx.resourceOwnerIdOrError())
        .build();
  }

  private UpdateStageRequest toUpdateStageRequest(
      UserContext userCtx, String tableIdStr, String stageIdStr, UpdateStage form) {

    return UpdateStageRequest.builder()
        .stageKey(StageKey.parse(tableIdStr, stageIdStr))
        .userId(userCtx.resourceOwnerIdOrError())
        .title(form.getTitle())
        .build();
  }

  private NewStageRequest toNewStageRequest(UserContext userCtx, String tableIdStr, NewStage form) {

    return NewStageRequest.builder()
        .tableKey(TableKey.parse(tableIdStr))
        .userId(userCtx.resourceOwnerIdOrError())
        .title(form.getTitle())
        .build();
  }

  private GetStageRequest toGetStageRequest(
      UserContext userCtx, String tableIdStr, String stageIdStr) {

    return GetStageRequest.builder()
        .stageKey(StageKey.parse(tableIdStr, stageIdStr))
        .userId(userCtx.resourceOwnerIdOrError())
        .build();
  }
}
