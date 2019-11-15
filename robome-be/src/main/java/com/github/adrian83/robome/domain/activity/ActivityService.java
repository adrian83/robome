package com.github.adrian83.robome.domain.activity;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.domain.activity.model.Activity;
import com.github.adrian83.robome.domain.activity.model.ActivityEntity;
import com.github.adrian83.robome.domain.activity.model.ActivityKey;
import com.github.adrian83.robome.domain.activity.model.NewActivity;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.domain.user.model.User;
import com.google.inject.Inject;

import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class ActivityService {

  private ActivityRepository activityRepository;
  private ActorMaterializer actorMaterializer;

  @Inject
  public ActivityService(
      ActivityRepository activityRepository, ActorMaterializer actorMaterializer) {
    this.activityRepository = activityRepository;
    this.actorMaterializer = actorMaterializer;
  }

  public CompletionStage<Optional<Activity>> getActivity(User user, ActivityKey activityKey) {
    return activityRepository.getById(activityKey, user)
    		.map((maybeActivity) -> maybeActivity.map(this::toActivity))
    		.runWith(Sink.head(), actorMaterializer);
  }

  public CompletionStage<List<Activity>> getStageActivities(User user, StageKey stageKey) {
    return activityRepository
        .getStageActivities(user.getId(), stageKey)
        .map(this::toActivity)
        .runWith(Sink.seq(), actorMaterializer);
  }

  public CompletionStage<ActivityKey> deleteActivity(User user, ActivityKey activityKey) {

	    Sink<ActivityKey, CompletionStage<ActivityKey>> sink =
	    		activityRepository
	            .deleteActivity(activityKey, user.getId())
	            .mapMaterializedValue(doneF -> doneF.thenApply(done -> activityKey));

	    return Source.lazily(() -> Source.single(activityKey)).runWith(sink, actorMaterializer);
	  }
  
  public CompletionStage<Activity> updateActivity(User user, ActivityKey key, NewActivity updatedActivity) {

	    ActivityEntity entity = ActivityEntity.newActivity(key, user.getId(), updatedActivity.getName());

	    Sink<ActivityEntity, CompletionStage<Activity>> sink =
	    		activityRepository.updateActivity(entity).mapMaterializedValue(doneF -> doneF.thenApply(done -> toActivity(entity)));

	    return Source.lazily(() -> Source.single(entity)).runWith(sink, actorMaterializer);
	  }
  
  public CompletionStage<Activity> saveActivity(User user, StageKey stageKey, NewActivity newActivity) {

    ActivityEntity entity = new ActivityEntity(stageKey, user.getId(), newActivity.getName());

    Sink<ActivityEntity, CompletionStage<Activity>> sink =
        activityRepository
            .saveActivity()
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> toActivity(entity)));

    return Source.lazily(() -> Source.single(entity)).runWith(sink, actorMaterializer);
  }
  
  private Activity toActivity(ActivityEntity entity) {
	  return new Activity(
			  entity.getKey(),
			  entity.getUserId(),
			  entity.getName(),
			  entity.getState(),
			  entity.getCreatedAt(),
			  entity.getModifiedAt());
  }
  
}
