package com.github.adrian83.robome.web.activity;

import static com.github.adrian83.robome.common.function.Functions.use;
import static com.github.adrian83.robome.web.common.http.HttpMethod.*;

import java.util.concurrent.CompletionStage;

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
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.web.activity.model.NewActivity;
import com.github.adrian83.robome.web.activity.model.UpdateActivity;
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

  private static final String LOG_LIST_ACTS =
      "User: {} issued list stage's activities request, tableId: {}, stageId: {}";
  private static final String LOG_CREATE_ACT =
      "User: {} issued persist activity request, tableId: {}, stageId: {}, data: {}";
  private static final String LOG_GET_ACT_BY_ID =
      "User: {} issued get activity by id request, tableId: {}, stageId: {}, activityId: {}";
  private static final String LOG_DEL_ACT_BY_ID =
      "User: {} issued delete activity by id request, tableId: {}, stageId: {}, activityId: {}";
  private static final String LOG_UPDATE_ACT =
      "User: {} issued update activity request, tableId: {}, stageId: {}, activityId: {}, data: {}";

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

  private CompletionStage<HttpResponse> persistActivity(
      CompletionStage<UserData> userF, String tableIdStr, String stageIdStr, NewActivity form) {

    var cLog =
        use((UserData user) -> log.info(LOG_CREATE_ACT, user.getEmail(), tableIdStr, stageIdStr, form));

    return userF
        .thenApply(cLog::apply)
        .thenApply(Authorization::canWriteAcivities)
        .thenApply(user -> new UserAndForm<NewActivity>(user, form))
        .thenApply(UserAndForm::validate)
        .thenApply(
            uaf ->
                toNewActivityRequest(
                    uaf.getUser(), tableIdStr, stageIdStr, uaf.getForm().getName()))
        .thenCompose(activityService::saveActivity)
        .thenApply(response::jsonFromObject);
  }

  private CompletionStage<HttpResponse> updateActivity(
      CompletionStage<UserData> userF,
      String tableIdStr,
      String stageIdStr,
      String activityIdStr,
      UpdateActivity form) {

    var cLog =
        use(
            (UserData user) ->
                log.info(
                    LOG_UPDATE_ACT, user.getEmail(), tableIdStr, stageIdStr, activityIdStr, form));

    return userF
        .thenApply(cLog::apply)
        .thenApply(Authorization::canWriteAcivities)
        .thenApply(user -> new UserAndForm<UpdateActivity>(user, form))
        .thenApply(UserAndForm::validate)
        .thenApply(
            uaf ->
                toUpdateActivityRequest(
                    uaf.getUser(), tableIdStr, stageIdStr, activityIdStr, uaf.getForm()))
        .thenCompose(activityService::updateActivity)
        .thenApply(response::jsonFromObject);
  }

  private CompletionStage<HttpResponse> deleteActivity(
      CompletionStage<UserData> userF, String tableIdStr, String stageIdStr, String activityIdStr) {

    var cLog =
        use(
            (UserData user) ->
                log.info(
                    LOG_DEL_ACT_BY_ID, user.getEmail(), tableIdStr, stageIdStr, activityIdStr));

    return userF
        .thenApply(cLog::apply)
        .thenApply(Authorization::canWriteAcivities)
        .thenApply(u -> toDeleteActivityRequest(u, tableIdStr, stageIdStr, activityIdStr))
        .thenCompose(activityService::deleteActivity)
        .thenApply(response::jsonFromObject);
  }

  private CompletionStage<HttpResponse> getActivityById(
      CompletionStage<UserData> userF, String tableIdStr, String stageIdStr, String activityIdStr) {

    var cLog =
        use(
            (UserData user) ->
                log.info(
                    LOG_GET_ACT_BY_ID, user.getEmail(), tableIdStr, stageIdStr, activityIdStr));

    return userF
        .thenApply(cLog::apply)
        .thenApply(Authorization::canReadAcivities)
        .thenApply(u -> toGetActivityRequest(u, tableIdStr, stageIdStr, activityIdStr))
        .thenCompose(activityService::getActivity)
        .thenApply(response::jsonFromObject);
  }

  private CompletionStage<HttpResponse> getStageActivities(
      CompletionStage<UserData> userF, String tableIdStr, String stageIdStr) {

    var cLog = use((UserData user) -> log.info(LOG_LIST_ACTS, user.getEmail(), tableIdStr, stageIdStr));

    return userF
        .thenApply(cLog::apply)
        .thenApply(Authorization::canReadAcivities)
        .thenApply(user -> toListStageActivitiesRequest(user, tableIdStr, stageIdStr))
        .thenCompose(activityService::getStageActivities)
        .thenApply(response::jsonFromObject);
  }

  private NewActivityRequest toNewActivityRequest(
		  UserData user, String tableIdStr, String stageIdStr, String activityName) {

    return NewActivityRequest.builder()
        .name(activityName)
        .stageKey(StageKey.parse(tableIdStr, stageIdStr))
        .userId(user.getId())
        .build();
  }

  private UpdateActivityRequest toUpdateActivityRequest(
		  UserData user,
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
		  UserData user, String tableIdStr, String stageIdStr, String activityIdStr) {

    return GetActivityRequest.builder()
        .activityKey(ActivityKey.parse(tableIdStr, stageIdStr, activityIdStr))
        .userId(user.getId())
        .build();
  }

  private DeleteActivityRequest toDeleteActivityRequest(
		  UserData user, String tableIdStr, String stageIdStr, String activityIdStr) {

    return DeleteActivityRequest.builder()
        .activityKey(ActivityKey.parse(tableIdStr, stageIdStr, activityIdStr))
        .userId(user.getId())
        .build();
  }

  private ListStageActivitiesRequest toListStageActivitiesRequest(
		  UserData user, String tableIdStr, String stageIdStr) {

    return ListStageActivitiesRequest.builder()
        .userId(user.getId())
        .stageKey(StageKey.parse(tableIdStr, stageIdStr))
        .build();
  }
}
