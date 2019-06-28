package com.github.adrian83.robome.domain.stage;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.auth.AuthContext;
import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.common.time.TimeUtils;
import com.github.adrian83.robome.common.web.AbstractController;
import com.github.adrian83.robome.common.web.ExceptionHandler;
import com.github.adrian83.robome.common.web.Response;
import com.github.adrian83.robome.domain.table.TableController;
import com.github.adrian83.robome.domain.table.model.TableState;
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
	
	private Route getTableStages(AuthContext authContext, String tableId) {
		UUID tableUuid = UUID.fromString(tableId);

		final CompletionStage<List<Stage>> futureStages = stageService.getTableStages(tableUuid);
		return onSuccess(() -> futureStages, stages -> complete(responseProducer.jsonFromObject(stages)));
	}

	private Route getStageById(AuthContext authContext, String tableId, String stageId) {

		StageId id = new StageId(UUID.fromString(tableId), UUID.fromString(stageId));

		final CompletionStage<Optional<Stage>> futureMaybeTable = stageService.getStage(id);

		return onSuccess(() -> futureMaybeTable,
				maybeItem -> complete(responseProducer.jsonFromOptional(maybeItem)));
	}

	private Route persistStage(AuthContext authContext, String tableId, NewStage newStage) {

		LocalDateTime utcNow = TimeUtils.utcNow();
		UUID id = UUID.randomUUID();

		Location locationHeader = locationFor(TableController.TABLES, tableId, PATH, id.toString());

		StageId stageId = new StageId(UUID.fromString(tableId), id);

		Stage stage = new Stage(stageId, authContext.getUserId(), newStage.getName(), TableState.ACTIVE, utcNow, utcNow);

		HttpResponse redirectResponse = HttpResponse.create().withStatus(StatusCodes.CREATED).addHeader(locationHeader);

		CompletionStage<Done> futureSaved = stageService.saveStage(stage);
		return onSuccess(() -> futureSaved, done -> complete(redirectResponse));

	}

}
