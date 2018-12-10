package ab.java.robome.web.domain.activity;

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
import ab.java.robome.domain.activity.ActivityService;
import ab.java.robome.domain.activity.model.Activity;
import ab.java.robome.domain.activity.model.ActivityId;
import ab.java.robome.domain.activity.model.NewActivity;
import ab.java.robome.domain.stage.model.StageId;
import ab.java.robome.domain.table.model.TableState;
import ab.java.robome.web.common.AbstractController;
import ab.java.robome.web.domain.table.TableController;
import ab.java.robome.web.security.SecurityUtils;
import ab.java.robome.web.domain.stage.StageController;

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
	public ActivityController(ObjectMapper objectMapper, SecurityUtils securityUtils, Config config,
			ActivityService activityService) {

		super(securityUtils, objectMapper, config);
		this.activityService = activityService;
	}

	public Route createRoute() {

		return route(
				get(() -> pathPrefix(TableController.TABLES,
						() -> pathPrefix(segment(),
								tableId -> pathPrefix(StageController.PATH,
										() -> pathPrefix(segment(), stageId -> pathPrefix(PATH,
												() -> pathPrefix(segment(), activityId -> pathEndOrSingleSlash(
														() -> getActivityById(tableId, stageId, activityId))))))))),
				post(() -> pathPrefix(TableController.TABLES,
						() -> pathPrefix(segment(),
								tableId -> pathPrefix(StageController.PATH,
										() -> pathPrefix(segment(),
												stageId -> pathPrefix(PATH,
														() -> entity(Jackson.unmarshaller(NewActivity.class),
																e -> persistActivity(tableId, stageId, e)))))))),
				get(() -> pathPrefix(TableController.TABLES,
						() -> pathPrefix(segment(), tableId -> pathPrefix(StageController.PATH,
								() -> pathPrefix(segment(), stageId -> pathPrefix(PATH,
										() -> pathEndOrSingleSlash(() -> getStageActivities(tableId, stageId)))))))));
	}

	private Route getStageActivities(String tableIdStr, String stageIdStr) {
		UUID tableUuid = UUID.fromString(tableIdStr);
		UUID stageUuid = UUID.fromString(stageIdStr);

		StageId stageId = new StageId(tableUuid, stageUuid);

		final CompletionStage<List<Activity>> futureStages = activityService.getStageActivities(stageId);
		return onSuccess(() -> futureStages, stages -> completeOK(stages, Jackson.marshaller(objectMapper)));
	}

	private Route getActivityById(String tableId, String stageId, String activityId) {

		ActivityId id = new ActivityId(UUID.fromString(tableId), UUID.fromString(stageId), UUID.fromString(activityId));

		final CompletionStage<Optional<Activity>> futureMaybeTable = activityService.getActivity(id);

		return onSuccess(() -> futureMaybeTable,
				maybeItem -> maybeItem.map(item -> completeOK(item, Jackson.marshaller(objectMapper)))
						.orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found")));
	}

	private Route persistActivity(String tableId, String stageId, NewActivity newActivity) {

		LocalDateTime utcNow = TimeUtils.utcNow();
		UUID id = UUID.randomUUID();

		Location locationHeader = locationFor(TableController.TABLES, tableId, StageController.PATH, stageId, PATH,
				id.toString());

		ActivityId activityId = new ActivityId(UUID.fromString(tableId), UUID.fromString(stageId), id);

		Activity activity = new Activity(activityId, newActivity.getName(), TableState.ACTIVE, utcNow, utcNow);

		HttpResponse redirectResponse = HttpResponse.create().withStatus(StatusCodes.CREATED).addHeader(locationHeader);

		CompletionStage<Done> futureSaved = activityService.saveActivity(activity);
		return onSuccess(() -> futureSaved, done -> complete(redirectResponse));

	}

}
