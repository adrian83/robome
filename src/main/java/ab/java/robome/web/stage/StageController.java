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
import ab.java.robome.stage.model.ImmutableStageId;
import ab.java.robome.stage.model.NewStage;
import ab.java.robome.stage.model.Stage;
import ab.java.robome.stage.model.StageId;
import ab.java.robome.table.model.TableState;
import ab.java.robome.web.table.TableController;
import akka.Done;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.Location;
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
				//post(() ->  path(TableController.PATH, () -> path(segment(), tableId -> path(PATH, () -> entity(Jackson.unmarshaller(NewStage.class), e -> persistStage(tableId, e) )))))
				post(() ->  pathPrefix(TableController.PATH, () -> pathPrefix(segment(), tableId -> pathPrefix(PATH, () -> entity(Jackson.unmarshaller(NewStage.class), e -> persistStage(tableId, e) )))))
				//post(() ->  path(PATH, () -> entity(Jackson.unmarshaller(NewStage.class), e -> persistStage("57a20049-a59f-422f-bc37-8e9a4961ffc7", e) )))
				
	
				);
	}

	private Route getStageById(String tableId, String stageId) {
		
		StageId id = ImmutableStageId.builder()
				.id(UUID.fromString(stageId))
				.tableId(UUID.fromString(tableId))
				.build();
		
		final CompletionStage<Optional<Stage>> futureMaybeTable = stageService.getStage(id);
		
		return onSuccess(() -> futureMaybeTable, maybeItem -> maybeItem
				.map(item -> completeOK(item, Jackson.marshaller(objectMapper)))
				.orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found")));
	}
	
	private Route persistStage(String tableId, NewStage newStage) {
		
		LocalDateTime utcNow = TimeUtils.utcNow();
		UUID id = UUID.randomUUID();
		
		Location locationHeader = Location.create("/" + TableController.PATH + "/" + tableId
				+ "/" + PATH + "/" + id.toString());
		
		StageId stageId = ImmutableStageId.builder()
				.id(id)
				.tableId(UUID.fromString(tableId))
				.build();
		
		Stage stage = ImmutableStage.builder()
				.stageId(stageId)
				.name(newStage.name())
				.state(TableState.ACTIVE)
				.createdAt(utcNow)
				.modifiedAt(utcNow)
				.build();

		HttpResponse redirectResponse = HttpResponse.create().withStatus(StatusCodes.CREATED).addHeader(locationHeader);
		
		CompletionStage<Done> futureSaved = stageService.saveStage(stage);
		return onSuccess(() -> futureSaved, done -> complete(redirectResponse));
	
	}
	
}
