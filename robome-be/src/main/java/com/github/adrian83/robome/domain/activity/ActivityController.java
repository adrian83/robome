package com.github.adrian83.robome.domain.activity;

import static com.github.adrian83.robome.domain.stage.StageController.STAGES;
import static com.github.adrian83.robome.domain.table.TableController.TABLES;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.Authorization;
import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.common.web.AbstractController;
import com.github.adrian83.robome.common.web.ExceptionHandler;
import com.github.adrian83.robome.common.web.Response;
import com.github.adrian83.robome.domain.activity.model.ActivityKey;
import com.github.adrian83.robome.domain.activity.model.NewActivity;
import com.github.adrian83.robome.domain.common.UserAndForm;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.domain.user.User;
import com.github.adrian83.robome.util.function.TriFunction;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.http.javadsl.model.HttpResponse;
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

  private TriFunction<String, String, String, Route> getActivityByIdAction =
      (String tableId, String stageId, String activityId) ->
          jwtSecured(tableId, stageId, activityId, this::getActivityById);

  private TriFunction<String, String, Class<NewActivity>, Route> persistActivityAction =
      (String tableId, String stageId, Class<NewActivity> clazz) ->
          jwtSecured(tableId, stageId, clazz, this::persistActivity);

  private BiFunction<String, String, Route> getStageActivitiesAction =
      (String tableId, String stageId) -> jwtSecured(tableId, stageId, this::getStageActivities);



  public Route createRoute() {
    return route(
        get(prefixVarPrefixVarPrefixVar(TABLES, STAGES, ACTIVITIES, getActivityByIdAction)),
        post(prefixVarPrefixVarPrefixForm(TABLES, STAGES, ACTIVITIES, NewActivity.class, persistActivityAction)),
        get(prefixVarPrefixVarPrefix(TABLES, STAGES, ACTIVITIES, getStageActivitiesAction)));
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
                        user, StageKey.fromStrings(tableIdStr, stageIdStr)))
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
                        user, ActivityKey.fromStrings(tableIdStr, stageIdStr, activityIdStr)))
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
            .thenApply(user -> new UserAndForm<NewActivity>(user, newActivity))
            .thenApply(UserAndForm::validate)
            .thenCompose(uaf -> activityService.saveActivity(uaf.getUser(), uaf.getForm()))
            .thenApply(activity -> responseProducer.response201(location(activity.getKey())))
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Location location(ActivityKey activityId) {
    return locationFor(
        TABLES,
        activityId.getTableId().toString(),
        STAGES,
        activityId.getStageId().toString(),
        ActivityController.ACTIVITIES,
        activityId.getActivityId().toString());
  }
}
