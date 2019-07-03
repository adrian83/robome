package com.github.adrian83.robome.domain.stage;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.Authorization;
import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.common.web.AbstractController;
import com.github.adrian83.robome.common.web.ExceptionHandler;
import com.github.adrian83.robome.common.web.Response;
import com.github.adrian83.robome.common.web.Validation;
import com.github.adrian83.robome.domain.stage.model.NewStage;
import com.github.adrian83.robome.domain.stage.model.StageId;
import com.github.adrian83.robome.domain.table.TableController;
import com.github.adrian83.robome.domain.table.model.TableId;
import com.github.adrian83.robome.domain.user.User;
import com.github.adrian83.robome.util.http.Cors;
import com.github.adrian83.robome.util.tuple.Tuple2;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
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
    return route(getTableStagesRoute, getStageRoute, createStageRoute);
  }

  private Route getTableStagesRoute =
      get(
          () ->
              pathPrefix(
                  TableController.TABLES,
                  () ->
                      pathPrefix(
                          segment(),
                          tableId ->
                              pathPrefix(
                                  STAGES, () -> jwtSecured(tableId, this::getTableStages)))));

  private Route getStageRoute =
      get(
          () ->
              pathPrefix(
                  TableController.TABLES,
                  () ->
                      pathPrefix(
                          segment(),
                          tableId ->
                              pathPrefix(
                                  STAGES,
                                  () ->
                                      pathPrefix(
                                          segment(),
                                          stageId ->
                                              pathEndOrSingleSlash(
                                                  () ->
                                                      jwtSecured(
                                                          tableId,
                                                          stageId,
                                                          this::getStageById)))))));

  private Route createStageRoute =
      post(
          () ->
              pathPrefix(
                  TableController.TABLES,
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
            .thenCompose(user -> stageService.getTableStages(user, TableId.fromString(tableIdStr)))
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
                user -> stageService.getStage(user, StageId.fromStrings(tableIdStr, stageIdStr)))
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
            .thenApply(user -> new Tuple2<User, NewStage>(user, newStage))
            .thenApply(
                tuple2 ->
                    new Tuple2<User, NewStage>(
                        tuple2.getObj1(), Validation.validate(tuple2.getObj2())))
            .thenCompose(tuple2 -> stageService.saveStage(tuple2.getObj1(), tuple2.getObj2()))
            .thenApply(
                stage ->
                    HttpResponse.create()
                        .withStatus(StatusCodes.CREATED)
                        .addHeaders(
                            headers(location(stage.getStageId()), Cors.origin(corsOrigin()))))
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Location location(StageId stageId) {
    return locationFor(
        TableController.TABLES,
        stageId.getTableId().toString(),
        StageController.STAGES,
        stageId.getStageId().toString());
  }
}
