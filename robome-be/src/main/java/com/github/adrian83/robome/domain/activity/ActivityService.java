package com.github.adrian83.robome.domain.activity;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.common.Time;
import com.github.adrian83.robome.domain.activity.model.Activity;
import com.github.adrian83.robome.domain.activity.model.ActivityEntity;
import com.github.adrian83.robome.domain.activity.model.ActivityKey;
import com.github.adrian83.robome.domain.activity.model.ActivityState;
import com.github.adrian83.robome.domain.activity.model.request.DeleteActivityCommand;
import com.github.adrian83.robome.domain.activity.model.request.GetActivityQuery;
import com.github.adrian83.robome.domain.activity.model.request.ListStageActivitiesQuery;
import com.github.adrian83.robome.domain.activity.model.request.NewActivityCommand;
import com.github.adrian83.robome.domain.activity.model.request.UpdateActivityCommand;
import com.google.inject.Inject;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActorSystem actorSystem;

    @Inject
    public ActivityService(ActivityRepository activityRepository, ActorSystem actorSystem) {
        this.activityRepository = activityRepository;
        this.actorSystem = actorSystem;
    }

    public CompletionStage<Activity> saveActivity(NewActivityCommand req) {
        var entity = new ActivityEntity(ActivityKey.basedOnStageKey(req.stageKey()), req.name(), ActivityState.ACTIVE, Time.utcNow(), Time.utcNow());
        return Source.single(entity)
                .via(activityRepository.saveActivity())
                .map(this::toActivity)
                .runWith(Sink.head(), actorSystem);
    }

    public CompletionStage<Activity> updateActivity(UpdateActivityCommand req) {
        var entity = new ActivityEntity(req.activityKey(), req.name(), ActivityState.ACTIVE, Time.utcNow(), Time.utcNow());
        return Source.single(entity)
                .via(activityRepository.updateActivity())
                .map(this::toActivity)
                .runWith(Sink.head(), actorSystem);
    }

    public CompletionStage<ActivityKey> deleteActivity(DeleteActivityCommand req) {
        return Source.single(req.activityKey())
                .via(activityRepository.deleteActivity(req.activityKey().userId()))
                .runWith(Sink.head(), actorSystem);
    }

    public CompletionStage<Optional<Activity>> getActivity(GetActivityQuery req) {
        return activityRepository.getById(req.activityKey())
                .map(this::toActivity)
                .runWith(Sink.headOption(), actorSystem);
    }

    public CompletionStage<List<Activity>> getStageActivities(ListStageActivitiesQuery req) {
        return activityRepository.getStageActivities(req.stageKey())
                .map(this::toActivity)
                .runWith(Sink.seq(), actorSystem);
    }

    private Activity toActivity(ActivityEntity entity) {
        return new Activity(entity.key(), entity.name(), entity.state(), entity.createdAt(), entity.modifiedAt());
    }
}
