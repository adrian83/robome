package com.github.adrian83.robome.domain.activity;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.domain.activity.model.Activity;
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

  public CompletionStage<Optional<Activity>> getActivity(User user, ActivityKey activityId) {
    return activityRepository.getById(activityId).runWith(Sink.head(), actorMaterializer);
  }

  public CompletionStage<List<Activity>> getStageActivities(User user, StageKey stageId) {
    return activityRepository
        .getStageActivities(user.getId(), stageId)
        .runWith(Sink.seq(), actorMaterializer);
  }

  public CompletionStage<Activity> saveActivity(User user, StageKey stageKey, NewActivity newActivity) {

    Activity activity = new Activity(stageKey, user.getId(), newActivity.getName());

    Sink<Activity, CompletionStage<Activity>> sink =
        activityRepository
            .saveActivity()
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> activity));

    return Source.lazily(() -> Source.single(activity)).runWith(sink, actorMaterializer);
  }
}
