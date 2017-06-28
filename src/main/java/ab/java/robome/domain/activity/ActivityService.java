package ab.java.robome.domain.activity;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.google.inject.Inject;

import ab.java.robome.domain.activity.model.Activity;
import ab.java.robome.domain.activity.model.ActivityId;
import ab.java.robome.domain.stage.model.StageId;
import akka.Done;
import akka.NotUsed;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class ActivityService {

	private ActivityRepository activityRepository;
	private ActorMaterializer actorMaterializer;

	@Inject
	public ActivityService(ActivityRepository activityRepository, ActorMaterializer actorMaterializer) {
		this.activityRepository = activityRepository;
		this.actorMaterializer = actorMaterializer;
	}

	public CompletionStage<Optional<Activity>> getActivity(ActivityId activityId) {
		return activityRepository.getById(activityId)
				.runWith(Sink.head(), actorMaterializer);
	}
	
	public CompletionStage<List<Activity>> getStageActivities(StageId stageId) {
		return activityRepository.getStageActivities(stageId)
				.runWith(Sink.seq(), actorMaterializer);
	}
	
	public CompletionStage<Done> saveActivity(Activity newActivity) {
		Source<Activity, CompletionStage<NotUsed>> source = Source.lazily(() -> Source.single(newActivity));
		Sink<Activity, CompletionStage<Done>> sink = activityRepository.saveActivity();
		return source.runWith(sink, actorMaterializer);
	}
	
}