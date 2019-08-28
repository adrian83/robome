package com.github.adrian83.robome.web.activity;

import static com.github.adrian83.robome.util.http.HttpMethod.DELETE;
import static com.github.adrian83.robome.util.http.HttpMethod.GET;
import static com.github.adrian83.robome.util.http.HttpMethod.POST;
import static com.github.adrian83.robome.util.http.HttpMethod.PUT;
import static com.github.adrian83.robome.web.stage.StageController.STAGES;
import static com.github.adrian83.robome.web.table.TableController.TABLES;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.Authorization;
import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.common.web.ExceptionHandler;
import com.github.adrian83.robome.common.web.Response;
import com.github.adrian83.robome.domain.activity.ActivityService;
import com.github.adrian83.robome.domain.activity.model.ActivityKey;
import com.github.adrian83.robome.domain.activity.model.NewActivity;
import com.github.adrian83.robome.domain.common.UserAndForm;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.domain.user.model.User;
import com.github.adrian83.robome.util.function.TriFunction;
import com.github.adrian83.robome.web.activity.validation.NewActivityValidator;
import com.github.adrian83.robome.web.common.AbstractController;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.Route;

public class ActivityController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ActivityController.class);
	
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
        options(prefixVarPrefixVarPrefix(TABLES, STAGES, ACTIVITIES, (tableId, stageId) -> handleOptionsRequest())),
        options(prefixVarPrefixVarPrefixVar(TABLES, STAGES, ACTIVITIES, (tableId, stageId, activityId) -> handleOptionsRequestWithId())),
        get(prefixVarPrefixVarPrefix(TABLES, STAGES, ACTIVITIES, getStageActivitiesAction)));
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
            .thenApply(user -> new UserAndForm<NewActivity>(user, newActivity, new NewActivityValidator()))
            .thenApply(UserAndForm::validate)
            .thenCompose(uaf -> activityService.saveActivity(uaf.getUser(), StageKey.fromStrings(tableId, stageId), uaf.getForm()))
            .thenApply(responseProducer::jsonFromObject)
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  
  private Route handleOptionsRequestWithId() {
	    return complete(responseProducer.response200(GET, DELETE, PUT));
	  }

	  private Route handleOptionsRequest() {
	    return complete(responseProducer.response200(GET, POST));
	  }
}
