package com.github.adrian83.robome.web.activity;

import static com.github.adrian83.robome.util.http.HttpMethod.*;
import static com.github.adrian83.robome.web.stage.StageController.STAGES;
import static com.github.adrian83.robome.web.table.TableController.TABLES;

import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.auth.Authorization;
import com.github.adrian83.robome.domain.activity.ActivityService;
import com.github.adrian83.robome.domain.activity.model.ActivityKey;
import com.github.adrian83.robome.domain.activity.model.request.NewActivityRequest;
import com.github.adrian83.robome.domain.activity.model.request.UpdatedActivityRequest;
import com.github.adrian83.robome.domain.activity.model.request.DeleteActivityRequest;
import com.github.adrian83.robome.domain.activity.model.request.GetActivityRequest;
import com.github.adrian83.robome.domain.activity.model.request.ListStageActivitiesRequest;
import com.github.adrian83.robome.domain.common.UserAndForm;
import com.github.adrian83.robome.domain.common.Validator;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.domain.user.model.User;
import com.github.adrian83.robome.util.tuple.Tuple3;
import com.github.adrian83.robome.web.activity.model.NewActivity;
import com.github.adrian83.robome.web.activity.model.UpdateActivity;
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
  private static final Validator<UpdateActivity> UPDATE_VALIDATOR = new UpdatedActivityValidator();

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
                PATH_ELEMENTS, UpdateActivity.class, this::updateActivityAction)),
        post(
            routes.prefixVarPrefixVarPrefixFormSlash(
                PATH_ELEMENTS, NewActivity.class, this::persistActivityAction)),
        options(
            routes.prefixVarPrefixVarPrefixVarSlash(
                PATH_ELEMENTS, this::handleOptionsRequestWithId)));
  }

  private Route getStageActivities(
      CompletionStage<User> userF, String tableIdStr, String stageIdStr) {

    var stageKey = newStageKey(tableIdStr, stageIdStr);

    LOGGER.info("New list stage activities, stageKey: {}", stageKey);

    CompletionStage<HttpResponse> responseF =
        userF
            .thenApply(Authorization::canReadAcivities)
            .thenApply(user -> toListStageActivitiesRequest(user, stageKey))
            .thenCompose(activityService::getStageActivities)
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route deleteActivity(
      CompletionStage<User> userF, String tableIdStr, String stageIdStr, String activityIdStr) {

    var activityKey = newActivityKey(tableIdStr, stageIdStr, activityIdStr);

    LOGGER.info("New delete activity request, activityKey: {}", activityKey);

    var responseF =
        userF
            .thenApply(Authorization::canWriteAcivities)
            .thenApply(u -> toDeleteActivityRequest(u, activityKey))
            .thenCompose(activityService::deleteActivity)
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route getActivityById(
      CompletionStage<User> userF, String tableIdStr, String stageIdStr, String activityIdStr) {

    var activityKey = newActivityKey(tableIdStr, stageIdStr, activityIdStr);

    LOGGER.info("New find activity request, activityKey: {}", activityKey);

    CompletionStage<HttpResponse> responseF =
        userF
            .thenApply(Authorization::canReadAcivities)
            .thenApply(u -> toGetActivityRequest(u, activityKey))
            .thenCompose(activityService::getActivity)
            .thenApply(response::jsonFromOptional)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route persistActivity(
      CompletionStage<User> userF, String tableIdStr, String stageIdStr, NewActivity newActivity) {

    var stageKey = newStageKey(tableIdStr, stageIdStr);

    LOGGER.info("New persist activity request, stageKey: {}, activity: {}", stageKey, newActivity);

    var responseF =
        userF
            .thenApply(Authorization::canWriteAcivities)
            .thenApply(user -> new UserAndForm<NewActivity>(user, newActivity, CREATE_VALIDATOR))
            .thenApply(UserAndForm::validate)
            .thenApply(
                uaf -> toNewActivityRequest(uaf.getUser(), stageKey, uaf.getForm().getName()))
            .thenCompose(activityService::saveActivity)
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route updateActivity(
      CompletionStage<User> userF,
      String tableIdStr,
      String stageIdStr,
      String activityIdStr,
      UpdateActivity updateActivity) {

    var activityKey = newActivityKey(tableIdStr, stageIdStr, activityIdStr);

    LOGGER.info(
        "New update activity request, activityKey: {}, form: {}", activityKey, updateActivity);

    CompletionStage<HttpResponse> responseF =
        userF
            .thenApply(Authorization::canWriteAcivities)
            .thenApply(
                user -> new UserAndForm<UpdateActivity>(user, updateActivity, UPDATE_VALIDATOR))
            .thenApply(UserAndForm::validate)
            .thenApply(
                uaf ->
                    toUpdatedActivityRequest(uaf.getUser(), activityKey, uaf.getForm().getName()))
            .thenCompose(activityService::updateActivity)
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private StageKey newStageKey(String tableIdStr, String stageIdStr) {
    return StageKey.parse(tableIdStr, stageIdStr);
  }

  private ActivityKey newActivityKey(String tableIdStr, String stageIdStr, String activityIdStr) {
    return ActivityKey.parse(tableIdStr, stageIdStr, activityIdStr);
  }

  private Route getActivityByIdAction(String tableId, String stageId, String activityId) {
    return security.secured(tableId, stageId, activityId, this::getActivityById);
  }

  private Route persistActivityAction(String tableId, String stageId, Class<NewActivity> clazz) {
    return security.secured(tableId, stageId, clazz, this::persistActivity);
  }

  private Route deleteActivityAction(String tableId, String stageId, String activityId) {
    return security.secured(tableId, stageId, activityId, this::deleteActivity);
  }

  private Route updateActivityAction(
      String tableId, String stageId, String activityId, Class<UpdateActivity> clazz) {
    return security.secured(tableId, stageId, activityId, clazz, this::updateActivity);
  }

  private Route getStageActivitiesAction(String tableId, String stageId) {
    return security.secured(tableId, stageId, this::getStageActivities);
  }

  private Route handleOptionsRequestWithId(String tableId, String stageId, String activityId) {
    return complete(response.response200(GET, DELETE, PUT));
  }

  private Route handleOptionsRequest(String tableId, String stageId) {
    return complete(response.response200(GET, POST));
  }

  private NewActivityRequest toNewActivityRequest(
      User user, StageKey stageKey, String activityName) {
    return NewActivityRequest.builder()
        .name(activityName)
        .stageKey(stageKey)
        .userId(user.getId())
        .build();
  }

  private UpdatedActivityRequest toUpdatedActivityRequest(
      User user, ActivityKey activityKey, String activityName) {
    return UpdatedActivityRequest.builder()
        .name(activityName)
        .activityKey(activityKey)
        .userId(user.getId())
        .build();
  }
  
  private GetActivityRequest toGetActivityRequest(
	      User user, ActivityKey activityKey) {
	    return GetActivityRequest.builder()
	        .activityKey(activityKey)
	        .userId(user.getId())
	        .build();
	  }

  private DeleteActivityRequest toDeleteActivityRequest(
	      User user, ActivityKey activityKey) {
	    return DeleteActivityRequest.builder()
	        .activityKey(activityKey)
	        .userId(user.getId())
	        .build();
	  }
  
  private ListStageActivitiesRequest toListStageActivitiesRequest(User user, StageKey stageKey) {
    return ListStageActivitiesRequest.builder().userId(user.getId()).stageKey(stageKey).build();
  }
  
}
