package com.github.adrian83.robome.web.stage;

import static com.github.adrian83.robome.domain.stage.model.StageKey.fromStrings;
import static com.github.adrian83.robome.domain.table.model.TableKey.fromString;
import static com.github.adrian83.robome.util.http.HttpMethod.*;
import static com.github.adrian83.robome.web.table.TableController.TABLES;

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.Authorization;
import com.github.adrian83.robome.common.web.ExceptionHandler;
import com.github.adrian83.robome.common.web.Response;
import com.github.adrian83.robome.domain.common.UserAndForm;
import com.github.adrian83.robome.domain.stage.StageService;
import com.github.adrian83.robome.domain.stage.model.NewStage;
import com.github.adrian83.robome.domain.stage.model.UpdatedStage;
import com.github.adrian83.robome.domain.user.model.User;
import com.github.adrian83.robome.util.function.TriFunction;
import com.github.adrian83.robome.web.common.Routes;
import com.github.adrian83.robome.web.common.Security;
import com.github.adrian83.robome.web.stage.validation.NewStageValidator;
import com.github.adrian83.robome.web.stage.validation.UpdatedStageValidator;
import com.google.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StageController extends AllDirectives {

  private static final Logger LOGGER = LoggerFactory.getLogger(StageController.class);

  public static final String STAGES = "stages";

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
        get(routes.prefixVarPrefixSlash(TABLES, STAGES, getTableStagesAction)),
        get(routes.prefixVarPrefixVarSlash(TABLES, STAGES, getStageByIdAction)),
        options(routes.prefixVarPrefixSlash(TABLES, STAGES, (tableId) -> handleOptionsRequest())),
        options(
            routes.prefixVarPrefixVarSlash(
                TABLES, STAGES, (tableId, stageId) -> handleOptionsRequestWithId())),
        post(routes.prefixVarPrefixFormSlash(TABLES, STAGES, NewStage.class, persistStageAction)),
        delete(routes.prefixVarPrefixVarSlash(TABLES, STAGES, deleteStageAction)),
        put(
            routes.prefixVarPrefixVarFormSlash(
                TABLES, STAGES, UpdatedStage.class, updateTableStageAction)));
  }

  private Function<String, Route> getTableStagesAction =
      (var tableId) -> security.jwtSecured(tableId, this::getTableStages);

  private BiFunction<String, String, Route> getStageByIdAction =
      (var tableId, var stageId) -> security.jwtSecured(tableId, stageId, this::getStageById);

  private BiFunction<String, Class<NewStage>, Route> persistStageAction =
      (var tableId, var clazz) -> security.jwtSecured(tableId, clazz, this::persistStage);

  private TriFunction<String, String, Class<UpdatedStage>, Route> updateTableStageAction =
      (var tableId, var stageId, var clazz) ->
          security.jwtSecured(tableId, stageId, clazz, this::updateStage);

  private BiFunction<String, String, Route> deleteStageAction =
      (var tableId, var stageId) -> security.jwtSecured(tableId, stageId, this::deleteStage);

  private Route getTableStages(CompletionStage<Optional<User>> maybeUserF, String tableIdStr) {

    LOGGER.info("New list stages request, tableId: {}", tableIdStr);

    var responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canReadStages)
            .thenCompose(user -> stageService.getTableStages(user, fromString(tableIdStr)))
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route getStageById(
      CompletionStage<Optional<User>> maybeUserF, String tableIdStr, String stageIdStr) {

    LOGGER.info("New get stage by id request, tableId: {}, stageId: {}", tableIdStr, stageIdStr);

    var responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canReadStages)
            .thenCompose(user -> stageService.getStage(user, fromStrings(tableIdStr, stageIdStr)))
            .thenApply(response::jsonFromOptional)
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route persistStage(
      CompletionStage<Optional<User>> maybeUserF, String tableId, NewStage newStage) {

    LOGGER.info("New persist stage request, tableId: {}, newStage: {}", tableId, newStage);

    var responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canWriteStages)
            .thenApply(user -> new UserAndForm<NewStage>(user, newStage, CREATE_VALIDATOR))
            .thenApply(UserAndForm::validate)
            .thenCompose(
                uaf -> stageService.saveStage(uaf.getUser(), fromString(tableId), uaf.getForm()))
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route updateStage(
      CompletionStage<Optional<User>> maybeUserF,
      String tableId,
      String stageId,
      UpdatedStage updatedStage) {

    LOGGER.info(
        "New update stage request, tableId: {}, stageId: {}, newStage: {}",
        tableId,
        stageId,
        updatedStage);

    var responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canWriteStages)
            .thenApply(user -> new UserAndForm<UpdatedStage>(user, updatedStage, UPDATE_VALIDATOR))
            .thenApply(UserAndForm::validate)
            .thenCompose(
                uaf ->
                    stageService.updateStage(
                        uaf.getUser(), fromStrings(tableId, stageId), uaf.getForm()))
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route deleteStage(
      CompletionStage<Optional<User>> maybeUserF, String tableId, String stageId) {

    LOGGER.info("New delete stage request, tableId: {}, stageId: {}", tableId, stageId);

    var responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canWriteStages)
            .thenCompose(user -> stageService.deleteStage(user, fromStrings(tableId, stageId)))
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route handleOptionsRequestWithId() {
    return complete(response.response200(GET, DELETE, PUT));
  }

  private Route handleOptionsRequest() {
    return complete(response.response200(GET, POST));
  }
}
