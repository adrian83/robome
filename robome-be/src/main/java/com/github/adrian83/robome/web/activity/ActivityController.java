package com.github.adrian83.robome.web.activity;

import java.util.Map;
import static java.util.UUID.fromString;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.auth.model.UserData;
import static com.github.adrian83.robome.common.Logging.logAction;
import com.github.adrian83.robome.domain.activity.ActivityService;
import com.github.adrian83.robome.domain.activity.model.ActivityKey;
import com.github.adrian83.robome.domain.activity.model.request.DeleteActivityRequest;
import com.github.adrian83.robome.domain.activity.model.request.GetActivityRequest;
import com.github.adrian83.robome.domain.activity.model.request.ListStageActivitiesRequest;
import com.github.adrian83.robome.domain.activity.model.request.NewActivityRequest;
import com.github.adrian83.robome.domain.activity.model.request.UpdateActivityRequest;
import com.github.adrian83.robome.domain.common.UserAndForm;
import com.github.adrian83.robome.domain.common.UserContext;
import static com.github.adrian83.robome.domain.common.UserContext.withUserAndResourceOwnerId;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.web.activity.model.NewActivity;
import com.github.adrian83.robome.web.activity.model.UpdateActivity;
import com.github.adrian83.robome.web.auth.Authorization;
import com.github.adrian83.robome.web.common.Response;
import com.github.adrian83.robome.web.common.Security;
import static com.github.adrian83.robome.web.common.http.HttpMethod.DELETE;
import static com.github.adrian83.robome.web.common.http.HttpMethod.GET;
import static com.github.adrian83.robome.web.common.http.HttpMethod.POST;
import static com.github.adrian83.robome.web.common.http.HttpMethod.PUT;
import com.github.adrian83.robome.web.common.routes.RouteSupplier;
import com.google.inject.Inject;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class ActivityController extends AllDirectives {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityController.class);

    private static final String ACTIVITIES_PATH = "/users/{userId}/tables/{tableId}/stages/{stageId}/activities/";
    private static final String ACTIVITY_PATH = "/users/{userId}/tables/{tableId}/stages/{stageId}/activities/{activityId}/";

    private static final String LOG_LIST_ACTS = "list stage's activities request, tableId: {}, stageId: {}";
    private static final String LOG_CREATE_ACT = "persist activity request, tableId: {}, stageId: {}, data: {}";
    private static final String LOG_GET_ACT_BY_ID = "get activity by id request, tableId: {}, stageId: {}, activityId: {}";
    private static final String LOG_DEL_ACT_BY_ID = "delete activity by id request, tableId: {}, stageId: {}, activityId: {}";
    private static final String LOG_UPDATE_ACT = "update activity request, tableId: {}, stageId: {}, activityId: {}, data: {}";

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
            get(new RouteSupplier(ACTIVITY_PATH, (pathParams) -> security.secured2(pathParams, this::getActivityById))),
            get(new RouteSupplier(ACTIVITIES_PATH, (pathParams) -> security.secured2(pathParams, this::getStageActivities))),
            delete(new RouteSupplier(ACTIVITY_PATH, (pathParams) -> security.secured2(pathParams, this::deleteActivity))),
            put(new RouteSupplier(ACTIVITY_PATH, (pathParams) -> security.secured2(pathParams, UpdateActivity.class, this::updateActivity))),
            post(new RouteSupplier(ACTIVITIES_PATH, (pathParams) -> security.secured2(pathParams, NewActivity.class, this::persistActivity))),
            options(new RouteSupplier(ACTIVITY_PATH, (pathParams) -> complete(response.response200(GET, DELETE, PUT)))),
            options(new RouteSupplier(ACTIVITIES_PATH, (pathParams) -> complete(response.response200(GET, POST)))));
    }

    private CompletionStage<HttpResponse> persistActivity(final UserData user, Map<String, String> pathParams, final NewActivity form) {
        final String resourceOwnerId = pathParams.get("userId");
        final String tableId = pathParams.get("tableId");
        final String stageId = pathParams.get("stageId");

	return logAction(LOGGER, user, LOG_CREATE_ACT, tableId, stageId, form)
		.thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerId)))
		.thenApply(Authorization::canWriteStages)
		.thenApply(userCtx -> new UserAndForm<NewActivity>(userCtx, form)).thenApply(UserAndForm::validate)
		.thenApply(uaf -> toNewActivityRequest(uaf, tableId, stageId))
		.thenCompose(activityService::saveActivity).thenApply(response::jsonFromObject);
    }

    private CompletionStage<HttpResponse> updateActivity(final UserData user, Map<String, String> pathParams, final UpdateActivity form) {
        final String resourceOwnerId = pathParams.get("userId");
        final String tableId = pathParams.get("tableId");
        final String stageId = pathParams.get("stageId");
        final String activityId = pathParams.get("activityId");

	return logAction(LOGGER, user, LOG_UPDATE_ACT, tableId, stageId, activityId, form)
		.thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerId)))
		.thenApply(Authorization::canWriteAcivities)
		.thenApply(userCtx -> new UserAndForm<UpdateActivity>(userCtx, form)).thenApply(UserAndForm::validate)
		.thenApply(uaf -> toUpdateActivityRequest(uaf, tableId, stageId, activityId))
		.thenCompose(activityService::updateActivity).thenApply(response::jsonFromObject);
    }

    private CompletionStage<HttpResponse> deleteActivity(final UserData user, Map<String, String> pathParams) {
        final String resourceOwnerId = pathParams.get("userId");
        final String tableId = pathParams.get("tableId");
        final String stageId = pathParams.get("stageId");
        final String activityId = pathParams.get("activityId");

	return logAction(LOGGER, user, LOG_DEL_ACT_BY_ID, tableId, stageId, activityId)
		.thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerId)))
		.thenApply(Authorization::canWriteAcivities)
		.thenApply(userCtx -> toDeleteActivityRequest(userCtx, tableId, stageId, activityId))
		.thenCompose(activityService::deleteActivity).thenApply(response::jsonFromObject);
    }

    private CompletionStage<HttpResponse> getActivityById(final UserData user, Map<String, String> pathParams) {
        final String resourceOwnerId = pathParams.get("userId");
        final String tableId = pathParams.get("tableId");
        final String stageId = pathParams.get("stageId");
        final String activityId = pathParams.get("activityId");

	return logAction(LOGGER, user, LOG_GET_ACT_BY_ID, tableId, stageId, activityId)
		.thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerId)))
		.thenApply(Authorization::canReadAcivities)
		.thenApply(userCtx -> toGetActivityRequest(userCtx, tableId, stageId, activityId))
		.thenCompose(activityService::getActivity).thenApply(response::jsonFromOptional);
    }

    private CompletionStage<HttpResponse> getStageActivities(final UserData user, Map<String, String> pathParams) {
        final String resourceOwnerId = pathParams.get("userId");
        final String tableId = pathParams.get("tableId");
        final String stageId = pathParams.get("stageId");

	return logAction(LOGGER, user, LOG_LIST_ACTS, tableId, stageId)
		.thenApply(userData -> withUserAndResourceOwnerId(userData, fromString(resourceOwnerId)))
		.thenApply(Authorization::canReadAcivities)
		.thenApply(userCtx -> toListStageActivitiesRequest(userCtx, tableId, stageId))
		.thenCompose(activityService::getStageActivities).thenApply(response::jsonFromObject);
    }

    private NewActivityRequest toNewActivityRequest(final UserAndForm<NewActivity> userAndForm, final String tableIdStr,
	    final String stageIdStr) {

	return new NewActivityRequest(userAndForm.form().name(),
		StageKey.create(userAndForm.userContext().resourceOwnerIdOrError(), tableIdStr, stageIdStr));
    }

    private UpdateActivityRequest toUpdateActivityRequest(final UserAndForm<UpdateActivity> userAndForm,
	    final String tableIdStr, final String stageIdStr, final String activityIdStr) {

	return new UpdateActivityRequest(userAndForm.form().name(), ActivityKey
		.create(userAndForm.userContext().resourceOwnerIdOrError(), tableIdStr, stageIdStr, activityIdStr));
    }

    private GetActivityRequest toGetActivityRequest(final UserContext userCtx, final String tableIdStr,
	    final String stageIdStr, final String activityIdStr) {

	return new GetActivityRequest(
		ActivityKey.create(userCtx.resourceOwnerIdOrError(), tableIdStr, stageIdStr, activityIdStr));
    }

    private DeleteActivityRequest toDeleteActivityRequest(final UserContext userCtx, final String tableIdStr,
	    final String stageIdStr, final String activityIdStr) {

	return new DeleteActivityRequest(
		ActivityKey.create(userCtx.resourceOwnerIdOrError(), tableIdStr, stageIdStr, activityIdStr));
    }

    private ListStageActivitiesRequest toListStageActivitiesRequest(final UserContext userCtx, final String tableIdStr,
	    final String stageIdStr) {
	return new ListStageActivitiesRequest(
		StageKey.create(userCtx.resourceOwnerIdOrError(), tableIdStr, stageIdStr));
    }
}
