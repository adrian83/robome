package com.github.adrian83.robome.web.stage;

import static com.github.adrian83.robome.util.http.HttpMethod.DELETE;
import static com.github.adrian83.robome.util.http.HttpMethod.GET;
import static com.github.adrian83.robome.util.http.HttpMethod.POST;
import static com.github.adrian83.robome.util.http.HttpMethod.PUT;

import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.auth.Authorization;
import com.github.adrian83.robome.domain.common.UserAndForm;
import com.github.adrian83.robome.domain.stage.StageService;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.domain.stage.model.request.DeleteStageRequest;
import com.github.adrian83.robome.domain.stage.model.request.GetStageRequest;
import com.github.adrian83.robome.domain.stage.model.request.ListTableStagesRequest;
import com.github.adrian83.robome.domain.stage.model.request.NewStageRequest;
import com.github.adrian83.robome.domain.stage.model.request.UpdateStageRequest;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.user.model.User;
import com.github.adrian83.robome.web.common.ExceptionHandler;
import com.github.adrian83.robome.web.common.Response;
import com.github.adrian83.robome.web.common.Security;
import com.github.adrian83.robome.web.common.routes.OneParamAndFormRoute;
import com.github.adrian83.robome.web.common.routes.OneParamRoute;
import com.github.adrian83.robome.web.common.routes.TwoParamsAndFormRoute;
import com.github.adrian83.robome.web.common.routes.TwoParamsRoute;
import com.github.adrian83.robome.web.stage.model.NewStage;
import com.github.adrian83.robome.web.stage.model.UpdateStage;
import com.google.inject.Inject;

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StageController extends AllDirectives {

  private static final String STAGES_PATH = "/tables/{tableId}/stages/";
  private static final String STAGE_PATH = "/tables/{tableId}/stages/{stageId}/";

  private StageService stageService;
  private ExceptionHandler exceptionHandler;
  private Response response;
  private Security security;

  @Inject
  public StageController(
      StageService stageService,
      Response response,
      ExceptionHandler exceptionHandler,
      Security security) {
    this.stageService = stageService;
    this.exceptionHandler = exceptionHandler;
    this.response = response;
    this.security = security;
  }

  public Route createRoute() {
    return route(
        get(
            new OneParamRoute(
                STAGES_PATH, (tabId) -> security.jwtSecured(tabId, this::getTableStages))),
        get(
            new TwoParamsRoute(
                STAGE_PATH, (tabId, stgId) -> security.secured(tabId, stgId, this::getStageById))),
        delete(
            new TwoParamsRoute(
                STAGE_PATH, (tabId, stgId) -> security.secured(tabId, stgId, this::deleteStage))),
        post(
            new OneParamAndFormRoute<NewStage>(
                STAGES_PATH,
                NewStage.class,
                (tabId, clz) -> security.jwtSecured(tabId, clz, this::persistStage))),
        put(
            new TwoParamsAndFormRoute<UpdateStage>(
                STAGE_PATH,
                UpdateStage.class,
                (tabId, stgId, clz) -> security.secured(tabId, stgId, clz, this::updateStage))),
        options(
            new OneParamRoute(STAGES_PATH, (tabId) -> complete(response.response200(GET, POST)))),
        options(
            new TwoParamsRoute(
                STAGE_PATH, (tabId, stgId) -> complete(response.response200(GET, DELETE, PUT)))));
  }

  private Route getTableStages(CompletionStage<User> userF, String tableIdStr) {
    var tableKey = TableKey.parse(tableIdStr);

    log.info("New list stages request, tableKey: {}", tableKey);

    var responseF =
        userF
            .thenApply(Authorization::canReadStages)
            .thenApply(user -> toListTableStagesRequest(user, tableIdStr))
            .thenCompose(stageService::getTableStages)
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route getStageById(CompletionStage<User> userF, String tableIdStr, String stageIdStr) {
    var stageKey = StageKey.parse(tableIdStr, stageIdStr);

    log.info("New get stage by id request, stageKey: {}", stageKey);

    var responseF =
        userF
            .thenApply(Authorization::canReadStages)
            .thenApply(u -> toGetStageRequest(u, tableIdStr, stageIdStr))
            .thenCompose(stageService::getStage)
            .thenApply(response::jsonFromOptional)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route persistStage(CompletionStage<User> userF, String tableIdStr, NewStage newStage) {
    var tableKey = TableKey.parse(tableIdStr);

    log.info("New persist stage request, tableKey: {}, newStage: {}", tableKey, newStage);

    var responseF =
        userF
            .thenApply(Authorization::canWriteStages)
            .thenApply(user -> new UserAndForm<NewStage>(user, newStage))
            .thenApply(UserAndForm::validate)
            .thenApply(uaf -> toNewStageRequest(uaf.getUser(), tableIdStr, uaf.getForm()))
            .thenCompose(stageService::saveStage)
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route updateStage(
      CompletionStage<User> userF, String tableIdStr, String stageIdStr, UpdateStage updatedStage) {
    var stageKey = StageKey.parse(tableIdStr, stageIdStr);

    log.info("New update stage request, stageKey: {}, newStage: {}", stageKey, updatedStage);

    var responseF =
        userF
            .thenApply(Authorization::canWriteStages)
            .thenApply(user -> new UserAndForm<UpdateStage>(user, updatedStage))
            .thenApply(UserAndForm::validate)
            .thenApply(
                uaf -> toUpdateStageRequest(uaf.getUser(), tableIdStr, stageIdStr, uaf.getForm()))
            .thenCompose(stageService::updateStage)
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route deleteStage(CompletionStage<User> userF, String tableIdStr, String stageIdStr) {
    var stageKey = StageKey.parse(tableIdStr, stageIdStr);

    log.info("New delete stage request, stageKey: {}", stageKey);

    var responseF =
        userF
            .thenApply(Authorization::canWriteStages)
            .thenApply(u -> toDeleteStageRequest(u, tableIdStr, stageIdStr))
            .thenCompose(stageService::deleteStage)
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private ListTableStagesRequest toListTableStagesRequest(User user, String tableIdStr) {
    return ListTableStagesRequest.builder()
        .userId(user.getId())
        .tableKey(TableKey.parse(tableIdStr))
        .build();
  }

  private DeleteStageRequest toDeleteStageRequest(User user, String tableIdStr, String stageIdStr) {
    return DeleteStageRequest.builder()
        .stageKey(StageKey.parse(tableIdStr, stageIdStr))
        .userId(user.getId())
        .build();
  }

  private UpdateStageRequest toUpdateStageRequest(
      User user, String tableIdStr, String stageIdStr, UpdateStage form) {
    return UpdateStageRequest.builder()
        .stageKey(StageKey.parse(tableIdStr, stageIdStr))
        .userId(user.getId())
        .title(form.getTitle())
        .build();
  }

  private NewStageRequest toNewStageRequest(User user, String tableIdStr, NewStage form) {
    return NewStageRequest.builder()
        .tableKey(TableKey.parse(tableIdStr))
        .userId(user.getId())
        .title(form.getTitle())
        .build();
  }

  private GetStageRequest toGetStageRequest(User user, String tableIdStr, String stageIdStr) {
    return GetStageRequest.builder()
        .stageKey(StageKey.parse(tableIdStr, stageIdStr))
        .userId(user.getId())
        .build();
  }
}
