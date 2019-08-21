package com.github.adrian83.robome.web.stage;

import static com.github.adrian83.robome.domain.stage.model.StageKey.fromStrings;
import static com.github.adrian83.robome.domain.table.model.TableKey.fromString;
import static com.github.adrian83.robome.util.http.HttpMethod.DELETE;
import static com.github.adrian83.robome.util.http.HttpMethod.GET;
import static com.github.adrian83.robome.util.http.HttpMethod.POST;
import static com.github.adrian83.robome.util.http.HttpMethod.PUT;
import static com.github.adrian83.robome.web.table.TableController.TABLES;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.Authorization;
import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.common.web.ExceptionHandler;
import com.github.adrian83.robome.common.web.Response;
import com.github.adrian83.robome.domain.common.UserAndForm;
import com.github.adrian83.robome.domain.stage.StageService;
import com.github.adrian83.robome.domain.stage.model.NewStage;
import com.github.adrian83.robome.domain.user.model.User;
import com.github.adrian83.robome.web.common.AbstractController;
import com.github.adrian83.robome.web.stage.validation.NewStageValidator;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.http.javadsl.server.Route;

public class StageController extends AbstractController {
	
  private static final Logger LOGGER = LoggerFactory.getLogger(StageController.class);

  public static final String STAGES = "stages";

  private static final NewStageValidator CREATE_VALIDATOR = new NewStageValidator();

  private StageService stageService;

  @Inject
  public StageController(
      StageService stageService,
      JwtAuthorizer jwtAuthorizer,
      Config config,
      Response responseProducer,
      ExceptionHandler exceptionHandler) {
    super(jwtAuthorizer, exceptionHandler, config, responseProducer);
    this.stageService = stageService;
  }

  public Route createRoute() {
    return route(
        get(prefixVarPrefix(TABLES, STAGES, getTableStagesAction)),
        get(prefixVarPrefixVar(TABLES, STAGES, getStageByIdAction)),
        options(prefixVarPrefix(TABLES, STAGES, (tableId) -> handleOptionsRequest())),
        options(prefixVarPrefixVar(TABLES, STAGES, (tableId, stageId) -> handleOptionsRequestWithId())),
        post(prefixVarPrefixForm(TABLES, STAGES, NewStage.class, persistStageAction)));
    
  }

  private Function<String, Route> getTableStagesAction =
      (var tableId) -> jwtSecured(tableId, this::getTableStages);

  private BiFunction<String, String, Route> getStageByIdAction =
      (var tableId, var stageId) -> jwtSecured(tableId, stageId, this::getStageById);

  private BiFunction<String, Class<NewStage>, Route> persistStageAction =
      (var tableId, var clazz) -> jwtSecured(tableId, clazz, this::persistStage);

  private Route getTableStages(CompletionStage<Optional<User>> maybeUserF, String tableIdStr) {

	LOGGER.info("New list stages request, tableId: {}", tableIdStr);
	  
    var responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canReadStages)
            .thenCompose(user -> stageService.getTableStages(user, fromString(tableIdStr)))
            .thenApply(responseProducer::jsonFromObject)
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route getStageById(
      CompletionStage<Optional<User>> maybeUserF, String tableIdStr, String stageIdStr) {

	LOGGER.info("New get stage by id request, tableId: {}, stageId: {}", tableIdStr, stageIdStr);
	  
    var responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canReadStages)
            .thenCompose(user -> stageService.getStage(user, fromStrings(tableIdStr, stageIdStr)))
            .thenApply(responseProducer::jsonFromOptional)
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route persistStage(
      CompletionStage<Optional<User>> maybeUserF, String tableId, NewStage newStage) {

	LOGGER.info("New persist stage request, tableId: {}, newStage: {}", tableId, newStage);
    
	var responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canWriteStages)
            .thenApply(user -> new UserAndForm<NewStage>(user, newStage, CREATE_VALIDATOR))
            .thenApply(UserAndForm::validate)
            .thenCompose(uaf -> stageService.saveStage(uaf.getUser(), fromString(tableId), uaf.getForm()))
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
