package ab.java.robome.web.activity;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import ab.java.robome.activity.ActivityService;
import ab.java.robome.activity.model.Activity;
import ab.java.robome.activity.model.ActivityId;
import ab.java.robome.activity.model.ImmutableActivity;
import ab.java.robome.activity.model.ImmutableActivityId;
import ab.java.robome.activity.model.NewActivity;
import ab.java.robome.common.time.TimeUtils;
import ab.java.robome.stage.model.ImmutableStageId;
import ab.java.robome.stage.model.StageId;
import ab.java.robome.table.model.TableState;
import ab.java.robome.web.common.AbstractController;
import ab.java.robome.web.table.TableController;
import ab.java.robome.web.stage.StageController;

import akka.Done;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.Route;

public class ActivityController extends AbstractController {

	public static final String PATH = "activities";
	
	private ActivityService activityService;
	
	@Inject
	public ActivityController(ObjectMapper objectMapper, ActivityService activityService) {
		super(objectMapper);
		this.activityService = activityService;
	}
	

	public Route createRoute() {

		return route(
				get(() -> pathPrefix(TableController.PATH, () -> pathPrefix(segment(), tableId -> pathPrefix(StageController.PATH, () -> pathPrefix(segment(), stageId -> pathPrefix(PATH, () -> pathPrefix(segment(), activityId -> pathEndOrSingleSlash(() -> getActivityById(tableId, stageId, activityId))))))))),
				post(() ->  pathPrefix(TableController.PATH, () -> pathPrefix(segment(), tableId -> pathPrefix(StageController.PATH, () -> pathPrefix(segment(), stageId -> pathPrefix(PATH, () -> entity(Jackson.unmarshaller(NewActivity.class), e -> persistActivity(tableId, stageId, e)))))))),
				get(() -> pathPrefix(TableController.PATH, () -> pathPrefix(segment(), tableId -> pathPrefix(StageController.PATH, () -> pathPrefix(segment(), stageId -> pathPrefix(PATH, () -> pathEndOrSingleSlash(() -> getStageActivities(tableId, stageId))))))))
				);
	}

	private Route getStageActivities(String tableIdStr, String stageIdStr) {
		UUID tableUuid = UUID.fromString(tableIdStr);
		UUID stageUuid = UUID.fromString(stageIdStr);
		
		StageId stageId = ImmutableStageId.builder()
				.id(stageUuid)
				.tableId(tableUuid)
				.build();
		
		final CompletionStage<List<Activity>> futureStages = activityService.getStageActivities(stageId);
		return onSuccess(() -> futureStages, stages -> completeOK(stages, Jackson.marshaller(objectMapper)));
	}
	
	private Route getActivityById(String tableId, String stageId, String activityId) {
		
		ActivityId id = ImmutableActivityId.builder()
				.id(UUID.fromString(activityId))
				.stageId(UUID.fromString(stageId))
				.tableId(UUID.fromString(tableId))
				.build();
		
		final CompletionStage<Optional<Activity>> futureMaybeTable = activityService.getActivity(id);
		
		return onSuccess(() -> futureMaybeTable, maybeItem -> maybeItem
				.map(item -> completeOK(item, Jackson.marshaller(objectMapper)))
				.orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found")));
	}
	
	private Route persistActivity(String tableId, String stageId, NewActivity newActivity) {
		
		LocalDateTime utcNow = TimeUtils.utcNow();
		UUID id = UUID.randomUUID();
		
		Location locationHeader = locationFor(TableController.PATH, tableId, StageController.PATH, stageId, PATH, id.toString());
		
		ActivityId activityId = ImmutableActivityId.builder()
				.id(id)
				.stageId(UUID.fromString(stageId))
				.tableId(UUID.fromString(tableId))
				.build();
		
		Activity activity = ImmutableActivity.builder()
				.id(activityId)
				.name(newActivity.name())
				.state(TableState.ACTIVE)
				.createdAt(utcNow)
				.modifiedAt(utcNow)
				.build();

		HttpResponse redirectResponse = HttpResponse.create().withStatus(StatusCodes.CREATED).addHeader(locationHeader);
		
		CompletionStage<Done> futureSaved = activityService.saveActivity(activity);
		return onSuccess(() -> futureSaved, done -> complete(redirectResponse));
	
	}
	
	
}
