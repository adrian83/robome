package com.github.adrian83.robome.domain.stage;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.Authorization;
import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.common.time.TimeUtils;
import com.github.adrian83.robome.common.web.AbstractController;
import com.github.adrian83.robome.common.web.ExceptionHandler;
import com.github.adrian83.robome.common.web.Response;
import com.github.adrian83.robome.domain.table.TableController;
import com.github.adrian83.robome.domain.table.model.TableId;
import com.github.adrian83.robome.domain.table.model.TableState;
import com.github.adrian83.robome.domain.user.User;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.Done;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.Route;

public class StageController extends AbstractController {

	public static final String PATH = "stages";

	private StageService stageService;

	@Inject
	public StageController(StageService stageService, JwtAuthorizer jwtAuthorizer, Config config, Response responseProducer, ExceptionHandler exceptionHandler) {
		super(jwtAuthorizer, exceptionHandler, config, responseProducer);
		this.stageService = stageService;
		
	}

	public Route createRoute() {
		return route(getTableStagesRoute, getStageRoute, createStageRoute);
	}

	private Route getTableStagesRoute = get(() -> pathPrefix(TableController.TABLES, () -> pathPrefix(segment(), tableId -> pathPrefix(PATH, () -> jwtSecured(tableId, this::getTableStages)))));
	
	private Route getStageRoute = get(() -> pathPrefix(TableController.TABLES, () -> pathPrefix(segment(), tableId -> pathPrefix(PATH, () -> pathPrefix(segment(), stageId -> pathEndOrSingleSlash( () -> jwtSecured(tableId, stageId, this::getStageById)))))));
	
	private Route createStageRoute = post(() -> pathPrefix(TableController.TABLES, () -> pathPrefix(segment(), tableId -> pathPrefix(PATH, () -> pathEndOrSingleSlash(() -> jwtSecured(tableId, NewStage.class, this::persistStage))))));
	
	private Route getTableStages(CompletionStage<Optional<User>> maybeUserF, String tableIdStr) {
		
		CompletionStage<HttpResponse> responseF = maybeUserF.thenApply(Authentication::userExists)
				.thenApply(Authorization::canReadStages)
				.thenCompose(user -> stageService.getTableStages(user, TableId.fromString(tableIdStr)))
				.thenApply(responseProducer::jsonFromObject)
				.exceptionally(exceptionHandler::handleException);

		return completeWithFuture(responseF);
	}

	private Route getStageById(CompletionStage<Optional<User>> maybeUserF, String tableIdStr, String stageIdStr) {
		
		CompletionStage<HttpResponse> responseF = maybeUserF.thenApply(Authentication::userExists)
				.thenApply(Authorization::canReadStages)
				.thenCompose(user -> stageService.getStage(user, StageId.fromStrings(tableIdStr, stageIdStr)))
				.thenApply(responseProducer::jsonFromObject)
				.exceptionally(exceptionHandler::handleException);

		return completeWithFuture(responseF);
	}

	private Route persistStage(CompletionStage<Optional<User>> maybeUserF, String tableId, NewStage newStage) {
/*
		CompletionStage<HttpResponse> responseF = maybeUserF.thenApply(Authentication::userExists)
				.thenApply(Authorization::canWriteStages)
				.thenCompose(user -> stageService.getStage(user, StageId.fromStrings(tableIdStr, stageIdStr)))
				.thenApply(responseProducer::jsonFromObject)
				.exceptionally(exceptionHandler::handleException);

		return completeWithFuture(responseF);
*/		
		LocalDateTime utcNow = TimeUtils.utcNow();
		UUID id = UUID.randomUUID();

		Location locationHeader = locationFor(TableController.TABLES, tableId, PATH, id.toString());

		StageId stageId = new StageId(UUID.fromString(tableId), id);

		// TODO fix it
		Stage stage = new Stage(stageId, null, newStage.getName(), TableState.ACTIVE, utcNow, utcNow);

		HttpResponse redirectResponse = HttpResponse.create().withStatus(StatusCodes.CREATED).addHeader(locationHeader);

		CompletionStage<Done> futureSaved = stageService.saveStage(stage);
		return onSuccess(() -> futureSaved, done -> complete(redirectResponse));

	}

}
