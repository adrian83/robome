package ab.java.robome.web.domain.stage;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import ab.java.robome.common.time.TimeUtils;
import ab.java.robome.domain.stage.StageService;
import ab.java.robome.domain.stage.model.NewStage;
import ab.java.robome.domain.stage.model.Stage;
import ab.java.robome.domain.stage.model.StageId;
import ab.java.robome.domain.table.model.TableState;
import ab.java.robome.web.common.AbstractController;
import ab.java.robome.web.domain.table.TableController;
import ab.java.robome.web.security.SecurityUtils;
import akka.Done;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.Route;

public class StageController extends AbstractController {

	public static final String PATH = "stages";

	private StageService stageService;

	@Inject
	public StageController(StageService stageService, SecurityUtils securityUtils, Config config,
			ObjectMapper objectMapper) {

		super(securityUtils, objectMapper, config);
		this.stageService = stageService;
	}

	public Route createRoute() {

		return route(
				get(() -> pathPrefix(TableController.TABLES,
						() -> pathPrefix(segment(),
								tableId -> pathPrefix(PATH, () -> pathPrefix(segment(),
										stageId -> pathEndOrSingleSlash(() -> getStageById(tableId, stageId))))))),
				post(() -> pathPrefix(TableController.TABLES, () -> pathPrefix(segment(),
						tableId -> pathPrefix(PATH, () -> pathEndOrSingleSlash(
								() -> entity(Jackson.unmarshaller(NewStage.class), e -> persistStage(tableId, e))))))),
				get(() -> pathPrefix(TableController.TABLES, () -> pathPrefix(segment(),
						tableId -> pathPrefix(PATH, () -> pathEndOrSingleSlash(() -> getTableStages(tableId)))))));
	}

	private Route getTableStages(String tableId) {
		UUID tableUuid = UUID.fromString(tableId);

		final CompletionStage<List<Stage>> futureStages = stageService.getTableStages(tableUuid);
		return onSuccess(() -> futureStages, stages -> completeOK(stages, Jackson.marshaller(objectMapper)));
	}

	private Route getStageById(String tableId, String stageId) {

		StageId id = new StageId(UUID.fromString(tableId), UUID.fromString(stageId));

		final CompletionStage<Optional<Stage>> futureMaybeTable = stageService.getStage(id);

		return onSuccess(() -> futureMaybeTable,
				maybeItem -> maybeItem.map(item -> completeOK(item, Jackson.marshaller(objectMapper)))
						.orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found")));
	}

	private Route persistStage(String tableId, NewStage newStage) {

		LocalDateTime utcNow = TimeUtils.utcNow();
		UUID id = UUID.randomUUID();

		Location locationHeader = locationFor(TableController.TABLES, tableId, PATH, id.toString());

		StageId stageId = new StageId(UUID.fromString(tableId), id);

		Stage stage = new Stage(stageId, newStage.getName(), TableState.ACTIVE, utcNow, utcNow);

		HttpResponse redirectResponse = HttpResponse.create().withStatus(StatusCodes.CREATED).addHeader(locationHeader);

		CompletionStage<Done> futureSaved = stageService.saveStage(stage);
		return onSuccess(() -> futureSaved, done -> complete(redirectResponse));

	}

}
