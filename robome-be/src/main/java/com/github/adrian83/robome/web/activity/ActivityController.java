package com.github.adrian83.robome.web.activity;

import static com.github.adrian83.robome.util.http.HttpMethod.*;
import static com.github.adrian83.robome.web.stage.StageController.STAGES;
import static com.github.adrian83.robome.web.table.TableController.TABLES;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.Authorization;
import com.github.adrian83.robome.domain.activity.ActivityService;
import com.github.adrian83.robome.domain.activity.model.ActivityKey;
import com.github.adrian83.robome.domain.activity.model.NewActivity;
import com.github.adrian83.robome.domain.activity.model.UpdatedActivity;
import com.github.adrian83.robome.domain.common.UserAndForm;
import com.github.adrian83.robome.domain.common.Validator;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.domain.user.model.User;
import com.github.adrian83.robome.util.function.TetraFunction;
import com.github.adrian83.robome.util.function.TriFunction;
import com.github.adrian83.robome.web.activity.validation.NewActivityValidator;
import com.github.adrian83.robome.web.activity.validation.UpdatedActivityValidator;
import com.github.adrian83.robome.web.common.ExceptionHandler;
import com.github.adrian83.robome.web.common.Response;
import com.github.adrian83.robome.web.common.Routes;
import com.github.adrian83.robome.web.common.Security;
import com.google.inject.Inject;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class ActivityController extends AllDirectives {

  private static final Logger LOGGER = LoggerFactory.getLogger(ActivityController.class);

  private static final Validator<NewActivity> CREATE_VALIDATOR = new NewActivityValidator();
  private static final Validator<UpdatedActivity> UPDATE_VALIDATOR = new UpdatedActivityValidator();

  public static final String ACTIVITIES = "activities";

  private ActivityService activityService;
  private ExceptionHandler exceptionHandler;
  private Security security;
  private Routes routes;
  private Response response;

  @Inject
  public ActivityController(
      ActivityService activityService,
      ExceptionHandler exceptionHandler,
      Response response,
      Routes routes,
      Security security) {
    this.activityService = activityService;
    this.exceptionHandler = exceptionHandler;
    this.security = security;
    this.routes = routes;
    this.response = response;
  }

  private TriFunction<String, String, String, Route> getActivityByIdAction =
      (String tableId, String stageId, String activityId) ->
          security.jwtSecured(tableId, stageId, activityId, this::getActivityById);

  private TriFunction<String, String, Class<NewActivity>, Route> persistActivityAction =
      (String tableId, String stageId, Class<NewActivity> clazz) ->
          security.jwtSecured(tableId, stageId, clazz, this::persistActivity);

  private TriFunction<String, String, String, Route> deleteActivityAction =
      (String tableId, String stageId, String activityId) ->
          security.jwtSecured(tableId, stageId, activityId, this::deleteActivity);

  private TetraFunction<String, String, String, Class<UpdatedActivity>, Route>
      updateActivityAction =
          (String tableId, String stageId, String activityId, Class<UpdatedActivity> clazz) ->
              security.jwtSecured(tableId, stageId, activityId, clazz, this::updateActivity);

  private BiFunction<String, String, Route> getStageActivitiesAction =
      (String tableId, String stageId) ->
          security.jwtSecured(tableId, stageId, this::getStageActivities);

  public Route createRoute() {
    return route(
        get(
            routes.prefixVarPrefixVarPrefixVarSlash(
                TABLES, STAGES, ACTIVITIES, getActivityByIdAction)),
        put(
            routes.prefixVarPrefixVarPrefixVarFormSlash(
                TABLES, STAGES, ACTIVITIES, UpdatedActivity.class, updateActivityAction)),
        post(
            routes.prefixVarPrefixVarPrefixFormSlash(
                TABLES, STAGES, ACTIVITIES, NewActivity.class, persistActivityAction)),
        options(
            routes.prefixVarPrefixVarPrefixSlash(
                TABLES, STAGES, ACTIVITIES, (tableId, stageId) -> handleOptionsRequest())),
        options(
            routes.prefixVarPrefixVarPrefixVarSlash(
                TABLES,
                STAGES,
                ACTIVITIES,
                (tableId, stageId, activityId) -> handleOptionsRequestWithId())),
        delete(
            routes.prefixVarPrefixVarPrefixVarSlash(
                TABLES, STAGES, ACTIVITIES, deleteActivityAction)),
        get(
            routes.prefixVarPrefixVarPrefixSlash(
                TABLES, STAGES, ACTIVITIES, getStageActivitiesAction)));
  }

  private Route getStageActivities(
      CompletionStage<Optional<User>> maybeUserF, String tableIdStr, String stageIdStr) {
    LOGGER.info("New list stage activities, tableId: {}, stageId: {}", tableIdStr, stageIdStr);

    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canReadStages)
            .thenCompose(
                user ->
                    activityService.getStageActivities(
                        user, StageKey.fromStrings(tableIdStr, stageIdStr)))
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route deleteActivity(
      CompletionStage<Optional<User>> maybeUserF,
      String tableId,
      String stageId,
      String activityId) {
    LOGGER.info(
        "New delete activity request, tableId: {}, stageId: {}, activityId: {}",
        tableId,
        stageId,
        activityId);

    var responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canWriteStages)
            .thenCompose(
                user ->
                    activityService.deleteActivity(
                        user, ActivityKey.fromStrings(tableId, stageId, activityId)))
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

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
            .thenApply(response::jsonFromOptional)
            .exceptionally(exceptionHandler::handle);

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
            .thenApply(user -> new UserAndForm<NewActivity>(user, newActivity, CREATE_VALIDATOR))
            .thenApply(UserAndForm::validate)
            .thenCompose(
                uaf ->
                    activityService.saveActivity(
                        uaf.getUser(), StageKey.fromStrings(tableId, stageId), uaf.getForm()))
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route updateActivity(
      CompletionStage<Optional<User>> maybeUserF,
      String tableId,
      String stageId,
      String activityId,
      UpdatedActivity updatedActivity) {
	  
    LOGGER.info(
        "New update activity request, tableId: {}, stageId: {}, activityId: {}, form: {}",
        tableId,
        stageId,
        activityId,
        updatedActivity);

    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canWriteStages)
            .thenApply(
                user -> new UserAndForm<UpdatedActivity>(user, updatedActivity, UPDATE_VALIDATOR))
            .thenApply(UserAndForm::validate)
            .thenCompose(
                uaf ->
                    activityService.updateActivity(
                        uaf.getUser(),
                        ActivityKey.fromStrings(tableId, stageId, activityId),
                        uaf.getForm()))
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route handleOptionsRequestWithId() {
    return complete(response.response200(GET, DELETE, PUT));
  }

  private Route handleOptionsRequest() {
    return complete(response.response200(GET, POST));
  }
}
