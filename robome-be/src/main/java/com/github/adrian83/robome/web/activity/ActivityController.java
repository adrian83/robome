package com.github.adrian83.robome.web.activity;

import static com.github.adrian83.robome.util.http.HttpMethod.DELETE;
import static com.github.adrian83.robome.util.http.HttpMethod.GET;
import static com.github.adrian83.robome.util.http.HttpMethod.POST;
import static com.github.adrian83.robome.util.http.HttpMethod.PUT;

import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.auth.Authorization;
import com.github.adrian83.robome.domain.activity.ActivityService;
import com.github.adrian83.robome.domain.activity.model.ActivityKey;
import com.github.adrian83.robome.domain.activity.model.request.DeleteActivityRequest;
import com.github.adrian83.robome.domain.activity.model.request.GetActivityRequest;
import com.github.adrian83.robome.domain.activity.model.request.ListStageActivitiesRequest;
import com.github.adrian83.robome.domain.activity.model.request.NewActivityRequest;
import com.github.adrian83.robome.domain.activity.model.request.UpdateActivityRequest;
import com.github.adrian83.robome.domain.common.UserAndForm;
import com.github.adrian83.robome.domain.common.Validator;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.domain.user.model.User;
import com.github.adrian83.robome.web.activity.model.NewActivity;
import com.github.adrian83.robome.web.activity.model.UpdateActivity;
import com.github.adrian83.robome.web.activity.validation.NewActivityValidator;
import com.github.adrian83.robome.web.activity.validation.UpdatedActivityValidator;
import com.github.adrian83.robome.web.common.ExceptionHandler;
import com.github.adrian83.robome.web.common.Response;
import com.github.adrian83.robome.web.common.Security;
import com.github.adrian83.robome.web.common.routes.ThreeParamsAndFormRoute;
import com.github.adrian83.robome.web.common.routes.ThreeParamsRoute;
import com.github.adrian83.robome.web.common.routes.TwoParamsAndFormRoute;
import com.github.adrian83.robome.web.common.routes.TwoParamsRoute;
import com.google.inject.Inject;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ActivityController extends AllDirectives {

  private static final String ACTIVITIES_PATH = "/tables/{tableId}/stages/{stageId}/activities/";
  private static final String ACTIVITY_PATH =
      "/tables/{tableId}/stages/{stageId}/activities/{activityId}/";

  //private static final Validator<NewActivity> CREATE_VALIDATOR = new NewActivityValidator();
  //private static final Validator<UpdateActivity> UPDATE_VALIDATOR = new UpdatedActivityValidator();

  private ActivityService activityService;
  private ExceptionHandler exceptionHandler;
  private Security security;
  private Response response;

  @Inject
  public ActivityController(
      ActivityService activityService,
      ExceptionHandler exceptionHandler,
      Response response,
      Security security) {
    this.activityService = activityService;
    this.exceptionHandler = exceptionHandler;
    this.security = security;
    this.response = response;
  }

  public Route createRoute() {
    return route(
        get(
            new TwoParamsRoute(
                ACTIVITIES_PATH,
                (tabId, stgId) -> security.secured(tabId, stgId, this::getStageActivities))),
        get(
            new ThreeParamsRoute(
                ACTIVITY_PATH,
                (tabId, stgId, actId) ->
                    security.secured(tabId, stgId, actId, this::getActivityById))),
        delete(
            new ThreeParamsRoute(
                ACTIVITY_PATH,
                (tabId, stgId, actId) ->
                    security.secured(tabId, stgId, actId, this::deleteActivity))),
        put(
            new ThreeParamsAndFormRoute<UpdateActivity>(
                ACTIVITY_PATH,
                UpdateActivity.class,
                (tabId, stgId, actId, clz) ->
                    security.secured(tabId, stgId, actId, clz, this::updateActivity))),
        post(
            new TwoParamsAndFormRoute<NewActivity>(
                ACTIVITIES_PATH,
                NewActivity.class,
                (tabId, stgId, clz) -> security.secured(tabId, stgId, clz, this::persistActivity))),
        options(
            new TwoParamsRoute(
                ACTIVITIES_PATH, (tabId, stgId) -> complete(response.response200(GET, POST)))),
        options(
            new ThreeParamsRoute(
                ACTIVITY_PATH,
                (tabId, stgId, actId) -> complete(response.response200(GET, DELETE, PUT)))));
  }

  private Route getStageActivities(
      CompletionStage<User> userF, String tableIdStr, String stageIdStr) {

    log.info("New list stage activities, tableId: {}, stageId: {}", tableIdStr, stageIdStr);

    CompletionStage<HttpResponse> responseF =
        userF
            .thenApply(Authorization::canReadAcivities)
            .thenApply(user -> toListStageActivitiesRequest(user, tableIdStr, stageIdStr))
            .thenCompose(activityService::getStageActivities)
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route deleteActivity(
      CompletionStage<User> userF, String tableIdStr, String stageIdStr, String activityIdStr) {

    log.info(
        "New delete activity request, tableId: {}, stageId: {} activityId: {}",
        tableIdStr,
        stageIdStr,
        activityIdStr);

    var responseF =
        userF
            .thenApply(Authorization::canWriteAcivities)
            .thenApply(u -> toDeleteActivityRequest(u, tableIdStr, stageIdStr, activityIdStr))
            .thenCompose(activityService::deleteActivity)
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route getActivityById(
      CompletionStage<User> userF, String tableIdStr, String stageIdStr, String activityIdStr) {

    log.info(
        "New find activity request, tableId: {}, stageId: {} activityId: {}",
        tableIdStr,
        stageIdStr,
        activityIdStr);

    CompletionStage<HttpResponse> responseF =
        userF
            .thenApply(Authorization::canReadAcivities)
            .thenApply(u -> toGetActivityRequest(u, tableIdStr, stageIdStr, activityIdStr))
            .thenCompose(activityService::getActivity)
            .thenApply(response::jsonFromOptional)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route persistActivity(
      CompletionStage<User> userF, String tableIdStr, String stageIdStr, NewActivity newActivity) {

    log.info(
        "New persist activity request, tableId: {}, stageId: {}, activity: {}",
        tableIdStr,
        stageIdStr,
        newActivity);

    var responseF =
        userF
            .thenApply(Authorization::canWriteAcivities)
            .thenApply(user -> new UserAndForm<NewActivity>(user, newActivity))
            .thenApply(UserAndForm::validate)
            .thenApply(
                uaf ->
                    toNewActivityRequest(
                        uaf.getUser(), tableIdStr, stageIdStr, uaf.getForm().getName()))
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

    log.info(
        "New update activity request, tableId: {}, stageId: {}, activityId: {}, form: {}",
        tableIdStr,
        stageIdStr,
        activityIdStr,
        updateActivity);

    CompletionStage<HttpResponse> responseF =
        userF
            .thenApply(Authorization::canWriteAcivities)
            .thenApply(
                user -> new UserAndForm<UpdateActivity>(user, updateActivity))
            .thenApply(UserAndForm::validate)
            .thenApply(
                uaf ->
                    toUpdateActivityRequest(
                        uaf.getUser(), tableIdStr, stageIdStr, activityIdStr, uaf.getForm()))
            .thenCompose(activityService::updateActivity)
            .thenApply(response::jsonFromObject)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private NewActivityRequest toNewActivityRequest(
      User user, String tableIdStr, String stageIdStr, String activityName) {

    return NewActivityRequest.builder()
        .name(activityName)
        .stageKey(StageKey.parse(tableIdStr, stageIdStr))
        .userId(user.getId())
        .build();
  }

  private UpdateActivityRequest toUpdateActivityRequest(
      User user,
      String tableIdStr,
      String stageIdStr,
      String activityIdStr,
      UpdateActivity updateActivity) {

    return UpdateActivityRequest.builder()
        .name(updateActivity.getName())
        .activityKey(ActivityKey.parse(tableIdStr, stageIdStr, activityIdStr))
        .userId(user.getId())
        .build();
  }

  private GetActivityRequest toGetActivityRequest(
      User user, String tableIdStr, String stageIdStr, String activityIdStr) {

    return GetActivityRequest.builder()
        .activityKey(ActivityKey.parse(tableIdStr, stageIdStr, activityIdStr))
        .userId(user.getId())
        .build();
  }

  private DeleteActivityRequest toDeleteActivityRequest(
      User user, String tableIdStr, String stageIdStr, String activityIdStr) {

    return DeleteActivityRequest.builder()
        .activityKey(ActivityKey.parse(tableIdStr, stageIdStr, activityIdStr))
        .userId(user.getId())
        .build();
  }

  private ListStageActivitiesRequest toListStageActivitiesRequest(
      User user, String tableIdStr, String stageIdStr) {

    return ListStageActivitiesRequest.builder()
        .userId(user.getId())
        .stageKey(StageKey.parse(tableIdStr, stageIdStr))
        .build();
  }
}
