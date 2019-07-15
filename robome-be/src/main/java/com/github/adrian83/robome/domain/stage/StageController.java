package com.github.adrian83.robome.domain.stage;

import static akka.http.javadsl.server.PathMatchers.segment;
import static com.github.adrian83.robome.domain.table.TableController.TABLES;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.Authorization;
import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.common.web.AbstractController;
import com.github.adrian83.robome.common.web.ExceptionHandler;
import com.github.adrian83.robome.common.web.Response;
import com.github.adrian83.robome.domain.common.UserAndForm;
import com.github.adrian83.robome.domain.stage.model.NewStage;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.user.User;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.Route;

public class StageController extends AbstractController {

  public static final String STAGES = "stages";

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
        createStageRoute);
  }

  private Function<String, Route> getTableStagesAction =
      (String tableId) -> jwtSecured(tableId, this::getTableStages);

  private BiFunction<String, String, Route> getStageByIdAction =
      (String tableId, String stageId) -> jwtSecured(tableId, stageId, this::getStageById);

  private Route createStageRoute =
      post(
          () ->
              pathPrefix(
                  TABLES,
                  () ->
                      pathPrefix(
                          segment(),
                          tableId ->
                              pathPrefix(
                                  STAGES,
                                  () ->
                                      pathEndOrSingleSlash(
                                          () ->
                                              jwtSecured(
                                                  tableId, NewStage.class, this::persistStage))))));

  private Route getTableStages(CompletionStage<Optional<User>> maybeUserF, String tableIdStr) {

    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canReadStages)
            .thenCompose(user -> stageService.getTableStages(user, TableKey.fromString(tableIdStr)))
            .thenApply(responseProducer::jsonFromObject)
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route getStageById(
      CompletionStage<Optional<User>> maybeUserF, String tableIdStr, String stageIdStr) {

    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canReadStages)
            .thenCompose(
                user -> stageService.getStage(user, StageKey.fromStrings(tableIdStr, stageIdStr)))
            .thenApply(responseProducer::jsonFromObject)
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route persistStage(
      CompletionStage<Optional<User>> maybeUserF, String tableId, NewStage newStage) {

    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canWriteStages)
            .thenApply(user -> new UserAndForm<NewStage>(user, newStage))
            .thenApply(UserAndForm::validate)
            .thenCompose(uaf -> stageService.saveStage(uaf.getUser(), uaf.getForm()))
            .thenApply(stage -> responseProducer.response201(location(stage.getKey())))
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Location location(StageKey stageId) {
    return locationFor(
        TABLES,
        stageId.getTableId().toString(),
        StageController.STAGES,
        stageId.getStageId().toString());
  }
}
