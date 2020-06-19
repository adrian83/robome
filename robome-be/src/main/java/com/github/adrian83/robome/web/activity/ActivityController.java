package com.github.adrian83.robome.web.activity;

import static com.github.adrian83.robome.util.http.HttpMethod.*;
import static com.github.adrian83.robome.web.stage.StageController.STAGES;
import static com.github.adrian83.robome.web.table.TableController.TABLES;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

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
import com.github.adrian83.robome.util.tuple.Tuple3;
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

  public static final Tuple3<String, String, String> PATH_ELEMENTS =
      new Tuple3<>(TABLES, STAGES, ACTIVITIES);

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

  public Route createRoute() {
    return route(
        get(routes.prefixVarPrefixVarPrefixVarSlash(PATH_ELEMENTS, this::getActivityByIdAction)),
        get(routes.prefixVarPrefixVarPrefixSlash(PATH_ELEMENTS, this::getStageActivitiesAction)),
        options(routes.prefixVarPrefixVarPrefixSlash(PATH_ELEMENTS, this::handleOptionsRequest)),
        delete(routes.prefixVarPrefixVarPrefixVarSlash(PATH_ELEMENTS, this::deleteActivityAction)),
        put(
            routes.prefixVarPrefixVarPrefixVarFormSlash(
                PATH_ELEMENTS, UpdatedActivity.class, this::updateActivityAction)),
        post(
            routes.prefixVarPrefixVarPrefixFormSlash(
                PATH_ELEMENTS, NewActivity.class, this::persistActivityAction)),
        options(
            routes.prefixVarPrefixVarPrefixVarSlash(
                PATH_ELEMENTS, this::handleOptionsRequestWithId)));
  }

  private Route getActivityByIdAction(String tableId, String stageId, String activityId) {
    return security.jwtSecured(tableId, stageId, activityId, this::getActivityById);
  }

  private Route persistActivityAction(String tableId, String stageId, Class<NewActivity> clazz) {
    return security.jwtSecured(tableId, stageId, clazz, this::persistActivity);
  }

  private Route deleteActivityAction(String tableId, String stageId, String activityId) {
    return security.jwtSecured(tableId, stageId, activityId, this::deleteActivity);
  }

  private Route updateActivityAction(
      String tableId, String stageId, String activityId, Class<UpdatedActivity> clazz) {
    return security.jwtSecured(tableId, stageId, activityId, clazz, this::updateActivity);
  }

  private Route getStageActivitiesAction(String tableId, String stageId) {
    return security.jwtSecured(tableId, stageId, this::getStageActivities);
  }

  private Route handleOptionsRequestWithId(String tableId, String stageId, String activityId) {
    return complete(response.response200(GET, DELETE, PUT));
  }

  private Route handleOptionsRequest(String tableId, String stageId) {
    return complete(response.response200(GET, POST));
  }

  private Route getStageActivities(
      CompletionStage<Optional<User>> maybeUserF, String tableIdStr, String stageIdStr) {

    var stageKey = newStageKey(tableIdStr, stageIdStr);

    LOGGER.info("New list stage activities, stageKey: {}", stageKey);

    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canReadAcivities)
            .thenCompose(user -> activityService.getStageActivities(user, stageKey))
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route deleteActivity(
      CompletionStage<Optional<User>> maybeUserF,
      String tableIdStr,
      String stageIdStr,
      String activityIdStr) {

    var activityKey = newActivityKey(tableIdStr, stageIdStr, activityIdStr);

    LOGGER.info("New delete activity request, activityKey: {}", activityKey);

    var responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canWriteAcivities)
            .thenCompose(user -> activityService.deleteActivity(user, activityKey))
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route getActivityById(
      CompletionStage<Optional<User>> maybeUserF,
      String tableIdStr,
      String stageIdStr,
      String activityIdStr) {

    var activityKey = newActivityKey(tableIdStr, stageIdStr, activityIdStr);

    LOGGER.info("New find activity request, activityKey: {}", activityKey);

    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canReadAcivities)
            .thenCompose(user -> activityService.getActivity(user, activityKey))
            .thenApply(response::jsonFromOptional)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route persistActivity(
      CompletionStage<Optional<User>> maybeUserF,
      String tableIdStr,
      String stageIdStr,
      NewActivity newActivity) {

    var stageKey = newStageKey(tableIdStr, stageIdStr);

    LOGGER.info("New persist table request, stageKey: {}, activity: {}", stageKey, newActivity);

    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canWriteAcivities)
            .thenApply(user -> new UserAndForm<NewActivity>(user, newActivity, CREATE_VALIDATOR))
            .thenApply(UserAndForm::validate)
            .thenCompose(
                uaf -> activityService.saveActivity(uaf.getUser(), stageKey, uaf.getForm()))
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route updateActivity(
      CompletionStage<Optional<User>> maybeUserF,
      String tableIdStr,
      String stageIdStr,
      String activityIdStr,
      UpdatedActivity updatedActivity) {

    var activityKey = newActivityKey(tableIdStr, stageIdStr, activityIdStr);

    LOGGER.info(
        "New update activity request, activityKey: {}, form: {}", activityKey, updatedActivity);

    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canWriteAcivities)
            .thenApply(
                user -> new UserAndForm<UpdatedActivity>(user, updatedActivity, UPDATE_VALIDATOR))
            .thenApply(UserAndForm::validate)
            .thenCompose(
                uaf -> activityService.updateActivity(uaf.getUser(), activityKey, uaf.getForm()))
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private StageKey newStageKey(String tableIdStr, String stageIdStr) {
    return StageKey.fromStrings(tableIdStr, stageIdStr);
  }

  private ActivityKey newActivityKey(String tableIdStr, String stageIdStr, String activityIdStr) {
    return ActivityKey.fromStrings(tableIdStr, stageIdStr, activityIdStr);
  }
}
