package com.github.adrian83.robome.web.activity;

import static com.github.adrian83.robome.common.Logging.logAction;
import static com.github.adrian83.robome.domain.common.UserContext.withUserAndResourceOwnerId;
import static com.github.adrian83.robome.web.common.http.HttpMethod.*;
import static java.util.UUID.fromString;

import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.auth.Authorization;
import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.domain.activity.ActivityService;
import com.github.adrian83.robome.domain.activity.model.ActivityKey;
import com.github.adrian83.robome.domain.activity.model.request.DeleteActivityRequest;
import com.github.adrian83.robome.domain.activity.model.request.GetActivityRequest;
import com.github.adrian83.robome.domain.activity.model.request.ListStageActivitiesRequest;
import com.github.adrian83.robome.domain.activity.model.request.NewActivityRequest;
import com.github.adrian83.robome.domain.activity.model.request.UpdateActivityRequest;
import com.github.adrian83.robome.domain.common.UserAndForm;
import com.github.adrian83.robome.domain.common.UserContext;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.web.activity.model.NewActivity;
import com.github.adrian83.robome.web.activity.model.UpdateActivity;
import com.github.adrian83.robome.web.common.Response;
import com.github.adrian83.robome.web.common.Security;
import com.github.adrian83.robome.web.common.routes.FourParamAndFormRoute;
import com.github.adrian83.robome.web.common.routes.FourParamRoute;
import com.github.adrian83.robome.web.common.routes.ThreeParamsAndFormRoute;
import com.github.adrian83.robome.web.common.routes.ThreeParamsRoute;
import com.google.inject.Inject;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class ActivityController extends AllDirectives {

  private static final Logger LOGGER = LoggerFactory.getLogger(ActivityController.class);

  private static final String ACTIVITIES_PATH =
      "/users/{userId}/tables/{tableId}/stages/{stageId}/activities/";
  private static final String ACTIVITY_PATH =
      "/users/{userId}/tables/{tableId}/stages/{stageId}/activities/{activityId}/";

  private static final String LOG_LIST_ACTS =
      "list stage's activities request, tableId: {}, stageId: {}";
  private static final String LOG_CREATE_ACT =
      "persist activity request, tableId: {}, stageId: {}, data: {}";
  private static final String LOG_GET_ACT_BY_ID =
      "get activity by id request, tableId: {}, stageId: {}, activityId: {}";
  private static final String LOG_DEL_ACT_BY_ID =
      "delete activity by id request, tableId: {}, stageId: {}, activityId: {}";
  private static final String LOG_UPDATE_ACT =
      "update activity request, tableId: {}, stageId: {}, activityId: {}, data: {}";

  private ActivityService activityService;
  private Security security;
  private Response response;

  @Inject
  public ActivityController(ActivityService activityService, Response response, Security security) {
    this.activityService = activityService;
    this.security = security;
    this.response = response;
  }

  public Route createRoute() {
    return route(
        get(
            new ThreeParamsRoute(
                ACTIVITIES_PATH,
                (resourceOwnerId, tabId, stgId) ->
                    security.secured(resourceOwnerId, tabId, stgId, this::getStageActivities))),
        get(
            new FourParamRoute(
                ACTIVITY_PATH,
                (resourceOwnerId, tabId, stgId, actId) ->
                    security.secured(resourceOwnerId, tabId, stgId, actId, this::getActivityById))),
        delete(
            new FourParamRoute(
                ACTIVITY_PATH,
                (resourceOwnerId, tabId, stgId, actId) ->
                    security.secured(resourceOwnerId, tabId, stgId, actId, this::deleteActivity))),
        put(
            new FourParamAndFormRoute<UpdateActivity>(
                ACTIVITY_PATH,
                UpdateActivity.class,
                (resourceOwnerId, tabId, stgId, actId, clz) ->
                    security.secured(
                        resourceOwnerId, tabId, stgId, actId, clz, this::updateActivity))),
        post(
            new ThreeParamsAndFormRoute<NewActivity>(
                ACTIVITIES_PATH,
                NewActivity.class,
                (resourceOwnerId, tabId, stgId, clz) ->
                    security.secured(resourceOwnerId, tabId, stgId, clz, this::persistActivity))),
        options(
            new ThreeParamsRoute(
                ACTIVITIES_PATH,
                (resourceOwnerId, tabId, stgId) -> complete(response.response200(GET, POST)))),
        options(
            new FourParamRoute(
                ACTIVITY_PATH,
                (resourceOwnerId, tabId, stgId, actId) ->
                    complete(response.response200(GET, DELETE, PUT)))));
  }

  private CompletionStage<HttpResponse> persistActivity(
      final CompletionStage<UserData> userF,
      final String resourceOwnerId,
      final String tableId,
      final String stageId,
      final NewActivity form) {

    return logAction(LOGGER, userF, LOG_CREATE_ACT, tableId, stageId, form)
        .thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerId)))
        .thenApply(Authorization::canWriteStages)
        .thenApply(userCtx -> new UserAndForm<NewActivity>(userCtx, form))
        .thenApply(UserAndForm::validate)
        .thenApply(uaf -> toNewActivityRequest(uaf, tableId, stageId))
        .thenCompose(activityService::saveActivity)
        .thenApply(response::jsonFromObject);
  }

  private CompletionStage<HttpResponse> updateActivity(
      final CompletionStage<UserData> userF,
      final String resourceOwnerId,
      final String tableId,
      final String stageId,
      final String activityId,
      final UpdateActivity form) {

    return logAction(LOGGER, userF, LOG_UPDATE_ACT, tableId, stageId, activityId, form)
        .thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerId)))
        .thenApply(Authorization::canWriteAcivities)
        .thenApply(userCtx -> new UserAndForm<UpdateActivity>(userCtx, form))
        .thenApply(UserAndForm::validate)
        .thenApply(uaf -> toUpdateActivityRequest(uaf, tableId, stageId, activityId))
        .thenCompose(activityService::updateActivity)
        .thenApply(response::jsonFromObject);
  }

  private CompletionStage<HttpResponse> deleteActivity(
      final CompletionStage<UserData> userF,
      final String resourceOwnerId,
      final String tableId,
      final String stageId,
      final String activityId) {

    return logAction(LOGGER, userF, LOG_DEL_ACT_BY_ID, tableId, stageId, activityId)
        .thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerId)))
        .thenApply(Authorization::canWriteAcivities)
        .thenApply(userCtx -> toDeleteActivityRequest(userCtx, tableId, stageId, activityId))
        .thenCompose(activityService::deleteActivity)
        .thenApply(response::jsonFromObject);
  }

  private CompletionStage<HttpResponse> getActivityById(
      final CompletionStage<UserData> userF,
      final String resourceOwnerId,
      final String tableId,
      final String stageId,
      final String activityId) {

    return logAction(LOGGER, userF, LOG_GET_ACT_BY_ID, tableId, stageId, activityId)
        .thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerId)))
        .thenApply(Authorization::canReadAcivities)
        .thenApply(userCtx -> toGetActivityRequest(userCtx, tableId, stageId, activityId))
        .thenCompose(activityService::getActivity)
        .thenApply(response::jsonFromOptional);
  }

  private CompletionStage<HttpResponse> getStageActivities(
      final CompletionStage<UserData> userF,
      final String resourceOwnerId,
      final String tableId,
      final String stageId) {

    return logAction(LOGGER, userF, LOG_LIST_ACTS, tableId, stageId)
        .thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerId)))
        .thenApply(Authorization::canReadAcivities)
        .thenApply(userCtx -> toListStageActivitiesRequest(userCtx, tableId, stageId))
        .thenCompose(activityService::getStageActivities)
        .thenApply(response::jsonFromObject);
  }

  private NewActivityRequest toNewActivityRequest(
      final UserAndForm<NewActivity> userAndForm,
      final String tableIdStr,
      final String stageIdStr) {

    return new NewActivityRequest(
        userAndForm.form().name(),
        userAndForm.userContext().resourceOwnerIdOrError(),
        StageKey.parse(tableIdStr, stageIdStr));
  }

  private UpdateActivityRequest toUpdateActivityRequest(
      final UserAndForm<UpdateActivity> userAndForm,
      final String tableIdStr,
      final String stageIdStr,
      final String activityIdStr) {

    return new UpdateActivityRequest(
        userAndForm.form().name(),
        userAndForm.userContext().resourceOwnerIdOrError(),
        ActivityKey.parse(tableIdStr, stageIdStr, activityIdStr));
  }

  private GetActivityRequest toGetActivityRequest(
      final UserContext userCtx,
      final String tableIdStr,
      final String stageIdStr,
      final String activityIdStr) {

    return new GetActivityRequest(
        userCtx.resourceOwnerIdOrError(), ActivityKey.parse(tableIdStr, stageIdStr, activityIdStr));
  }

  private DeleteActivityRequest toDeleteActivityRequest(
      final UserContext userCtx,
      final String tableIdStr,
      final String stageIdStr,
      final String activityIdStr) {

    return new DeleteActivityRequest(
        userCtx.resourceOwnerIdOrError(), ActivityKey.parse(tableIdStr, stageIdStr, activityIdStr));
  }

  private ListStageActivitiesRequest toListStageActivitiesRequest(
      final UserContext userCtx, final String tableIdStr, final String stageIdStr) {

    return new ListStageActivitiesRequest(
        userCtx.resourceOwnerIdOrError(), StageKey.parse(tableIdStr, stageIdStr));
  }
}
