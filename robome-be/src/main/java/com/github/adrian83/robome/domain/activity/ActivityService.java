package com.github.adrian83.robome.domain.activity;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.domain.activity.model.Activity;
import com.github.adrian83.robome.domain.activity.model.ActivityEntity;
import com.github.adrian83.robome.domain.activity.model.ActivityKey;
import com.github.adrian83.robome.domain.activity.model.ListStageActivitiesRequest;
import com.github.adrian83.robome.domain.activity.model.NewActivity;
import com.github.adrian83.robome.domain.activity.model.UpdatedActivity;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.domain.user.model.User;
import com.google.inject.Inject;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class ActivityService {

  private ActivityRepository activityRepository;
  private ActorSystem actorSystem;

  @Inject
  public ActivityService(ActivityRepository activityRepository, ActorSystem actorSystem) {
    this.activityRepository = activityRepository;
    this.actorSystem = actorSystem;
  }

  public CompletionStage<Optional<Activity>> getActivity(User user, ActivityKey activityKey) {
    return activityRepository
        .getById(activityKey, user)
        .map((maybeActivity) -> maybeActivity.map(this::toActivity))
        .runWith(Sink.head(), actorSystem);
  }

  public CompletionStage<List<Activity>> getStageActivities(ListStageActivitiesRequest req) {
    return activityRepository
        .getStageActivities(req.getUserId(), req.getStageKey())
        .map(this::toActivity)
        .runWith(Sink.seq(), actorSystem);
  }

  public CompletionStage<ActivityKey> deleteActivity(User user, ActivityKey activityKey) {
    Sink<ActivityKey, CompletionStage<ActivityKey>> sink =
        activityRepository
            .deleteActivity(user.getId())
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> activityKey));

    return Source.single(activityKey).runWith(sink, actorSystem);
  }

  public CompletionStage<Activity> updateActivity(
      User user, ActivityKey key, UpdatedActivity updatedActivity) {
    var entity = ActivityEntity.newActivity(key, user.getId(), updatedActivity.getName());

    Sink<ActivityEntity, CompletionStage<Activity>> sink =
        activityRepository
            .updateActivity()
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> toActivity(entity)));

    return Source.single(entity).runWith(sink, actorSystem);
  }

  public CompletionStage<Activity> saveActivity(
      User user, StageKey stageKey, NewActivity newActivity) {
    var entity = new ActivityEntity(stageKey, user.getId(), newActivity.getName());

    Sink<ActivityEntity, CompletionStage<Activity>> sink =
        activityRepository
            .saveActivity()
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> toActivity(entity)));

    return Source.single(entity).runWith(sink, actorSystem);
  }

  private Activity toActivity(ActivityEntity entity) {
    return Activity.builder()
        .key(entity.getKey())
        .userId(entity.getUserId())
        .name(entity.getName())
        .state(entity.getState())
        .createdAt(entity.getCreatedAt())
        .modifiedAt(entity.getModifiedAt())
        .build();
  }
}
