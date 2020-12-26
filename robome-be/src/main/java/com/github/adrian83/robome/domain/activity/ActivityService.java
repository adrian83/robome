package com.github.adrian83.robome.domain.activity;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.domain.activity.model.Activity;
import com.github.adrian83.robome.domain.activity.model.ActivityEntity;
import com.github.adrian83.robome.domain.activity.model.ActivityKey;
import com.github.adrian83.robome.domain.activity.model.request.NewActivityRequest;
import com.github.adrian83.robome.domain.activity.model.request.UpdatedActivityRequest;
import com.github.adrian83.robome.domain.activity.model.request.DeleteActivityRequest;
import com.github.adrian83.robome.domain.activity.model.request.GetActivityRequest;
import com.github.adrian83.robome.domain.activity.model.request.ListStageActivitiesRequest;
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

  public CompletionStage<Optional<Activity>> getActivity(GetActivityRequest req) {
    return activityRepository
        .getById(req.getActivityKey(), req.getUserId())
        .map((maybeActivity) -> maybeActivity.map(this::toActivity))
        .runWith(Sink.head(), actorSystem);
  }

  public CompletionStage<List<Activity>> getStageActivities(ListStageActivitiesRequest req) {
    return activityRepository
        .getStageActivities(req.getUserId(), req.getStageKey())
        .map(this::toActivity)
        .runWith(Sink.seq(), actorSystem);
  }

  public CompletionStage<ActivityKey> deleteActivity(DeleteActivityRequest req) {
    Sink<ActivityKey, CompletionStage<ActivityKey>> sink =
        activityRepository
            .deleteActivity(req.getUserId())
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> req.getActivityKey()));

    return Source.single(req.getActivityKey()).runWith(sink, actorSystem);
  }

  public CompletionStage<Activity> updateActivity(UpdatedActivityRequest req) {
    var entity = ActivityEntity.newActivity(req.getActivityKey(), req.getUserId(), req.getName());

    Sink<ActivityEntity, CompletionStage<Activity>> sink =
        activityRepository
            .updateActivity()
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> toActivity(entity)));

    return Source.single(entity).runWith(sink, actorSystem);
  }

  public CompletionStage<Activity> saveActivity(NewActivityRequest req) {
    var entity = new ActivityEntity(req.getStageKey(), req.getUserId(), req.getName());

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
