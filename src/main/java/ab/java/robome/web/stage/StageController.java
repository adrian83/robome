package ab.java.robome.web.stage;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import ab.java.robome.common.time.TimeUtils;
import ab.java.robome.stage.StageService;
import ab.java.robome.stage.model.ImmutableStage;
import ab.java.robome.stage.model.NewStage;
import ab.java.robome.stage.model.Stage;
import ab.java.robome.table.model.TableState;
import ab.java.robome.web.table.TableController;
import akka.Done;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class StageController extends AllDirectives {
	
	public static final String PATH = "stages";

	private StageService stageService;
	private ObjectMapper objectMapper;

	@Inject
	public StageController(StageService stageService, ObjectMapper objectMapper) {
		this.stageService = stageService;
		this.objectMapper = objectMapper;
	}

	public Route createRoute() {

		return route(
				get(() -> pathPrefix(TableController.PATH, () -> path(segment(), tableId -> pathPrefix(PATH, () -> path(segment(), stageId -> getStageById(tableId, stageId)))))),
				post(() ->  pathPrefix(TableController.PATH, () -> path(segment(), tableId -> pathPrefix(PATH, () -> entity(Jackson.unmarshaller(NewStage.class), this::persistStage)))))
				);
	}

	private Route getStageById(String tableId, String stageId) {
		final CompletionStage<Optional<Stage>> futureMaybeTable = stageService.getStage(tableId, stageId);
		
		return onSuccess(() -> futureMaybeTable, maybeItem -> maybeItem.map(item -> completeOK(item, Jackson.marshaller(objectMapper)))
				.orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found")));
	}
	
	private Route persistStage(NewStage newTable) {
		
		LocalDateTime utcNow = TimeUtils.utcNow();
		
		Stage stage = ImmutableStage.builder()
				.id(UUID.randomUUID())
				.tableId(newTable.tableId())
				.name(newTable.name())
				.state(TableState.ACTIVE)
				.createdAt(utcNow)
				.modifiedAt(utcNow)
				.build();

		CompletionStage<Done> futureSaved = stageService.saveStage(stage);
		return onSuccess(() -> futureSaved, done -> complete("new stage created"));
	}
	
}
