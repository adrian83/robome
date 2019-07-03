package com.github.adrian83.robome.domain.activity;

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
import com.github.adrian83.robome.domain.activity.model.ActivityId;
import com.github.adrian83.robome.domain.activity.model.NewActivity;
import com.github.adrian83.robome.domain.stage.StageController;
import com.github.adrian83.robome.domain.stage.model.StageId;
import com.github.adrian83.robome.domain.table.TableController;
import com.github.adrian83.robome.domain.user.User;
import com.github.adrian83.robome.util.http.Cors;
import com.github.adrian83.robome.util.tuple.Tuple2;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.Route;

public class ActivityController extends AbstractController {

  public static final String ACTIVITIES = "activities";

  private ActivityService activityService;

  @Inject
  public ActivityController(
      JwtAuthorizer jwtAuthorizer,
      Config config,
      ActivityService activityService,
      ExceptionHandler exceptionHandler,
      Response responseProducer) {
    super(jwtAuthorizer, exceptionHandler, config, responseProducer);
    this.activityService = activityService;
  }

  public Route createRoute() {
    return route(
        get(
            () ->
                pathPrefix(
                    TableController.TABLES,
                    () ->
                        pathPrefix(
                            segment(),
                            tableId ->
                                pathPrefix(
                                    StageController.STAGES,
                                    () ->
                                        pathPrefix(
                                            segment(),
                                            stageId ->
                                                pathPrefix(
                                                    ACTIVITIES,
                                                    () ->
                                                        pathPrefix(
                                                            segment(),
                                                            activityId ->
                                                                jwtSecured(
                                                                    tableId,
                                                                    stageId,
                                                                    activityId,
                                                                    this::getActivityById)))))))),
        post(
            () ->
                pathPrefix(
                    TableController.TABLES,
                    () ->
                        pathPrefix(
                            segment(),
                            tableId ->
                                pathPrefix(
                                    StageController.STAGES,
                                    () ->
                                        pathPrefix(
                                            segment(),
                                            stageId ->
                                                pathPrefix(
                                                    ACTIVITIES,
                                                    () ->
                                                        jwtSecured(
                                                            tableId,
                                                            stageId,
                                                            NewActivity.class,
                                                            this::persistActivity))))))),
        get(
            () ->
                pathPrefix(
                    TableController.TABLES,
                    () ->
                        pathPrefix(
                            segment(),
                            tableId ->
                                pathPrefix(
                                    StageController.STAGES,
                                    () ->
                                        pathPrefix(
                                            segment(),
                                            stageId ->
                                                pathPrefix(
                                                    ACTIVITIES,
                                                    () ->
                                                        jwtSecured(
                                                            tableId,
                                                            stageId,
                                                            this::getStageActivities))))))));
  }

  private Route getStageActivities(
      CompletionStage<Optional<User>> maybeUserF, String tableIdStr, String stageIdStr) {

    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canReadStages)
            .thenCompose(
                user ->
                    activityService.getStageActivities(
                        user, StageId.fromStrings(tableIdStr, stageIdStr)))
            .thenApply(responseProducer::jsonFromObject)
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route getActivityById(
      CompletionStage<Optional<User>> maybeUserF,
      String tableIdStr,
      String stageIdStr,
      String activityIdStr) {

    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canReadStages)
            .thenCompose(
                user ->
                    activityService.getActivity(
                        user, ActivityId.fromStrings(tableIdStr, stageIdStr, activityIdStr)))
            .thenApply(responseProducer::jsonFromOptional)
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route persistActivity(
      CompletionStage<Optional<User>> maybeUserF,
      String tableId,
      String stageId,
      NewActivity newActivity) {

    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canWriteStages)
            .thenApply(user -> new Tuple2<User, NewActivity>(user, newActivity))
            .thenApply(
                tuple2 ->
                    new Tuple2<User, NewActivity>(
                        tuple2.getObj1(), Validation.validate(tuple2.getObj2())))
            .thenCompose(tuple2 -> activityService.saveActivity(tuple2.getObj1(), tuple2.getObj2()))
            .thenApply(
                activity ->
                    HttpResponse.create()
                        .withStatus(StatusCodes.CREATED)
                        .addHeaders(headers(location(activity.getId()), Cors.origin(corsOrigin()))))
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Location location(ActivityId activityId) {
    return locationFor(
        TableController.TABLES,
        activityId.getTableId().toString(),
        StageController.STAGES,
        activityId.getStageId().toString(),
        ActivityController.ACTIVITIES,
        activityId.getActivityId().toString());
  }
}
