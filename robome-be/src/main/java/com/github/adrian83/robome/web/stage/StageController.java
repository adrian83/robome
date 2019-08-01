package com.github.adrian83.robome.web.stage;

import static com.github.adrian83.robome.domain.stage.model.StageKey.fromStrings;
import static com.github.adrian83.robome.domain.table.model.TableKey.fromString;
import static com.github.adrian83.robome.web.table.TableController.TABLES;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.Authorization;
import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.common.web.ExceptionHandler;
import com.github.adrian83.robome.common.web.Response;
import com.github.adrian83.robome.domain.common.UserAndForm;
import com.github.adrian83.robome.domain.stage.StageService;
import com.github.adrian83.robome.domain.stage.model.NewStage;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.domain.user.model.User;
import com.github.adrian83.robome.web.common.AbstractController;
import com.github.adrian83.robome.web.stage.validation.NewStageValidator;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.Route;

public class StageController extends AbstractController {

  public static final String STAGES = "stages";

  private static final NewStageValidator CREATE_VALIDATOR = new NewStageValidator();

  private StageService stageService;

  @Inject
  public StageController(
      StageService stageService,
      JwtAuthorizer jwtAuthorizer,
      Config config,
      Response responseProducer,
      ExceptionHandler exceptionHandler) {
    super(jwtAuthorizer, exceptionHandler, config, responseProducer);
    this.stageService = stageService;
  }

  public Route createRoute() {
    return route(
        get(prefixVarPrefix(TABLES, STAGES, getTableStagesAction)),
        get(prefixVarPrefixVar(TABLES, STAGES, getStageByIdAction)),
        post(prefixVarPrefixForm(TABLES, STAGES, NewStage.class, persistStageAction)));
  }

  private Function<String, Route> getTableStagesAction =
      (var tableId) -> jwtSecured(tableId, this::getTableStages);

  private BiFunction<String, String, Route> getStageByIdAction =
      (var tableId, var stageId) -> jwtSecured(tableId, stageId, this::getStageById);

  private BiFunction<String, Class<NewStage>, Route> persistStageAction =
      (var tableId, var clazz) -> jwtSecured(tableId, clazz, this::persistStage);

  private Route getTableStages(CompletionStage<Optional<User>> maybeUserF, String tableIdStr) {

    var responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canReadStages)
            .thenCompose(user -> stageService.getTableStages(user, fromString(tableIdStr)))
            .thenApply(responseProducer::jsonFromObject)
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route getStageById(
      CompletionStage<Optional<User>> maybeUserF, String tableIdStr, String stageIdStr) {

    var responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canReadStages)
            .thenCompose(user -> stageService.getStage(user, fromStrings(tableIdStr, stageIdStr)))
            .thenApply(responseProducer::jsonFromObject)
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route persistStage(
      CompletionStage<Optional<User>> maybeUserF, String tableId, NewStage newStage) {

    var responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canWriteStages)
            .thenApply(user -> new UserAndForm<NewStage>(user, newStage, CREATE_VALIDATOR))
            .thenApply(UserAndForm::validate)
            .thenCompose(uaf -> stageService.saveStage(uaf.getUser(), fromString(tableId), uaf.getForm()))
            .thenApply(stage -> responseProducer.response201(location(stage.getKey())))
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Location location(StageKey stageId) {
    return locationFor(
        TABLES, stageId.getTableId().toString(), STAGES, stageId.getStageId().toString());
  }
}
