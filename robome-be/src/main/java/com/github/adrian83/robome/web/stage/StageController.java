package com.github.adrian83.robome.web.stage;

import static com.github.adrian83.robome.util.http.HttpMethod.*;
import static com.github.adrian83.robome.web.table.TableController.TABLES;

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import com.github.adrian83.robome.auth.Authorization;
import com.github.adrian83.robome.domain.common.UserAndForm;
import com.github.adrian83.robome.domain.stage.StageService;
import com.github.adrian83.robome.domain.stage.model.ListTableStagesRequest;
import com.github.adrian83.robome.domain.stage.model.NewStage;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.domain.stage.model.UpdatedStage;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.user.model.User;
import com.github.adrian83.robome.util.tuple.Tuple2;
import com.github.adrian83.robome.web.common.ExceptionHandler;
import com.github.adrian83.robome.web.common.Response;
import com.github.adrian83.robome.web.common.Routes;
import com.github.adrian83.robome.web.common.Security;
import com.github.adrian83.robome.web.stage.validation.NewStageValidator;
import com.github.adrian83.robome.web.stage.validation.UpdatedStageValidator;
import com.google.inject.Inject;
import java.util.concurrent.CompletionStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StageController extends AllDirectives {

  private static final Logger LOGGER = LoggerFactory.getLogger(StageController.class);

  public static final String STAGES = "stages";

  public static final Tuple2<String, String> PATH_ELEMENTS = new Tuple2<>(TABLES, STAGES);

  private static final NewStageValidator CREATE_VALIDATOR = new NewStageValidator();
  private static final UpdatedStageValidator UPDATE_VALIDATOR = new UpdatedStageValidator();

  private StageService stageService;
  private ExceptionHandler exceptionHandler;
  private Response response;
  private Security security;
  private Routes routes;

  @Inject
  public StageController(
      StageService stageService,
      Response response,
      ExceptionHandler exceptionHandler,
      Routes routes,
      Security security) {
    this.stageService = stageService;
    this.exceptionHandler = exceptionHandler;
    this.response = response;
    this.routes = routes;
    this.security = security;
  }

  public Route createRoute() {
    return route(
        get(routes.prefixVarPrefixSlash(PATH_ELEMENTS, this::getTableStagesAction)),
        get(routes.prefixVarPrefixVarSlash(PATH_ELEMENTS, this::getStageByIdAction)),
        options(routes.prefixVarPrefixSlash(PATH_ELEMENTS, this::handleOptionsRequest)),
        delete(routes.prefixVarPrefixVarSlash(PATH_ELEMENTS, this::deleteStageAction)),
        options(routes.prefixVarPrefixVarSlash(PATH_ELEMENTS, this::handleOptionsRequestWithId)),
        post(
            routes.prefixVarPrefixFormSlash(
                PATH_ELEMENTS, NewStage.class, this::persistStageAction)),
        put(
            routes.prefixVarPrefixVarFormSlash(
                PATH_ELEMENTS, UpdatedStage.class, this::updateTableStageAction)));
  }

  private Route getTableStagesAction(String tableId) {
    return security.jwtSecured(tableId, this::getTableStages);
  }

  private Route getStageByIdAction(String tableId, String stageId) {
    return security.secured(tableId, stageId, this::getStageById);
  }

  private Route persistStageAction(String tableId, Class<NewStage> clazz) {
    return security.jwtSecured(tableId, clazz, this::persistStage);
  }

  private Route updateTableStageAction(String tableId, String stageId, Class<UpdatedStage> clazz) {
    return security.secured(tableId, stageId, clazz, this::updateStage);
  }

  private Route deleteStageAction(String tableId, String stageId) {
    return security.secured(tableId, stageId, this::deleteStage);
  }

  private Route handleOptionsRequestWithId(String tableId, String stageId) {
    return complete(response.response200(GET, DELETE, PUT));
  }

  private Route handleOptionsRequest(String tableId) {
    return complete(response.response200(GET, POST));
  }

  private ListTableStagesRequest toListTableStagesRequest(User user, String tableIdStr) {
    return ListTableStagesRequest.builder()
        .userId(user.getId())
        .tableKey(TableKey.parse(tableIdStr))
        .build();
  }

  private Route getTableStages(CompletionStage<User> userF, String tableIdStr) {
    var tableKey = TableKey.parse(tableIdStr);

    LOGGER.info("New list stages request, tableKey: {}", tableKey);

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

    LOGGER.info("New get stage by id request, stageKey: {}", stageKey);

    var responseF =
        userF
            .thenApply(Authorization::canReadStages)
            .thenCompose(user -> stageService.getStage(user, stageKey))
            .thenApply(response::jsonFromOptional)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route persistStage(CompletionStage<User> userF, String tableIdStr, NewStage newStage) {
    var tableKey = TableKey.parse(tableIdStr);

    LOGGER.info("New persist stage request, tableKey: {}, newStage: {}", tableKey, newStage);

    var responseF =
        userF
            .thenApply(Authorization::canWriteStages)
            .thenApply(user -> new UserAndForm<NewStage>(user, newStage, CREATE_VALIDATOR))
            .thenApply(UserAndForm::validate)
            .thenCompose(uaf -> stageService.saveStage(uaf.getUser(), tableKey, uaf.getForm()))
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route updateStage(
      CompletionStage<User> userF,
      String tableIdStr,
      String stageIdStr,
      UpdatedStage updatedStage) {
    var stageKey = StageKey.parse(tableIdStr, stageIdStr);

    LOGGER.info("New update stage request, stageKey: {}, newStage: {}", stageKey, updatedStage);

    var responseF =
        userF
            .thenApply(Authorization::canWriteStages)
            .thenApply(user -> new UserAndForm<UpdatedStage>(user, updatedStage, UPDATE_VALIDATOR))
            .thenApply(UserAndForm::validate)
            .thenCompose(uaf -> stageService.updateStage(uaf.getUser(), stageKey, uaf.getForm()))
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route deleteStage(CompletionStage<User> userF, String tableIdStr, String stageIdStr) {
    var stageKey = StageKey.parse(tableIdStr, stageIdStr);

    LOGGER.info("New delete stage request, stageKey: {}", stageKey);

    var responseF =
        userF
            .thenApply(Authorization::canWriteStages)
            .thenCompose(user -> stageService.deleteStage(user, stageKey))
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }
}
