package com.github.adrian83.robome.domain.activity;

import java.util.List;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.common.Time;
import com.github.adrian83.robome.domain.activity.model.Activity;
import com.github.adrian83.robome.domain.activity.model.ActivityEntity;
import com.github.adrian83.robome.domain.activity.model.ActivityKey;
import com.github.adrian83.robome.domain.activity.model.ActivityState;
import com.github.adrian83.robome.domain.activity.model.request.DeleteActivityRequest;
import com.github.adrian83.robome.domain.activity.model.request.GetActivityRequest;
import com.github.adrian83.robome.domain.activity.model.request.ListStageActivitiesRequest;
import com.github.adrian83.robome.domain.activity.model.request.NewActivityRequest;
import com.github.adrian83.robome.domain.activity.model.request.UpdateActivityRequest;
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

  public CompletionStage<Activity> saveActivity(NewActivityRequest req) {
    var entity =
        ActivityEntity.builder()
            .key(ActivityKey.randomWithStageKey(req.getStageKey()))
            .userId(req.getUserId())
            .name(req.getName())
            .state(ActivityState.ACTIVE)
            .modifiedAt(Time.utcNow())
            .createdAt(Time.utcNow())
            .build();

    return Source.single(entity)
        .via(activityRepository.saveActivity())
        .map(this::toActivity)
        .runWith(Sink.head(), actorSystem);
  }

  public CompletionStage<Activity> updateActivity(UpdateActivityRequest req) {
    var entity =
        ActivityEntity.builder()
            .key(req.getActivityKey())
            .userId(req.getUserId())
            .name(req.getName())
            .state(ActivityState.ACTIVE)
            .modifiedAt(Time.utcNow())
            .createdAt(Time.utcNow())
            .build();

    return Source.single(entity)
        .via(activityRepository.updateActivity())
        .map(this::toActivity)
        .runWith(Sink.head(), actorSystem);
  }

  public CompletionStage<ActivityKey> deleteActivity(DeleteActivityRequest req) {
    return Source.single(req.getActivityKey())
        .via(activityRepository.deleteActivity(req.getUserId()))
        .runWith(Sink.head(), actorSystem);
  }

  public CompletionStage<Activity> getActivity(GetActivityRequest req) {
    return activityRepository
        .getById(req.getActivityKey(), req.getUserId())
        .map(this::toActivity)
        .runWith(Sink.head(), actorSystem);
  }

  public CompletionStage<List<Activity>> getStageActivities(ListStageActivitiesRequest req) {
    return activityRepository
        .getStageActivities(req.getStageKey(), req.getUserId())
        .map(this::toActivity)
        .runWith(Sink.seq(), actorSystem);
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
