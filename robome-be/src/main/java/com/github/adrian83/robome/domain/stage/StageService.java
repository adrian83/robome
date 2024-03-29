package com.github.adrian83.robome.domain.stage;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.common.Time;
import com.github.adrian83.robome.domain.activity.ActivityService;
import com.github.adrian83.robome.domain.activity.model.request.ListStageActivitiesRequest;
import com.github.adrian83.robome.domain.stage.model.Stage;
import com.github.adrian83.robome.domain.stage.model.StageEntity;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.domain.stage.model.StageState;
import com.github.adrian83.robome.domain.stage.model.request.DeleteStageRequest;
import com.github.adrian83.robome.domain.stage.model.request.GetStageRequest;
import com.github.adrian83.robome.domain.stage.model.request.ListTableStagesRequest;
import com.github.adrian83.robome.domain.stage.model.request.NewStageRequest;
import com.github.adrian83.robome.domain.stage.model.request.UpdateStageRequest;
import com.google.inject.Inject;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class StageService {

    private static final int DEFAULT_PARALLERISM = 1;

    private StageRepository stageRepository;
    private ActivityService activityService;
    private ActorSystem actorSystem;

    @Inject
    public StageService(StageRepository stageRepository, ActivityService activityService, ActorSystem actorSystem) {
	this.stageRepository = stageRepository;
	this.actorSystem = actorSystem;
	this.activityService = activityService;
    }

    public CompletionStage<Stage> saveStage(NewStageRequest req) {
	var entity = new StageEntity(StageKey.basedOnTableKey(req.tableKey()), req.title(), StageState.ACTIVE,
		Time.utcNow(), Time.utcNow());

	return Source.single(entity).via(stageRepository.saveStage()).map(this::toStage).runWith(Sink.head(),
		actorSystem);
    }

    public CompletionStage<Stage> updateStage(UpdateStageRequest req) {
	var entity = new StageEntity(req.stageKey(), req.title(), StageState.ACTIVE, Time.utcNow(), Time.utcNow());

	return Source.single(entity).via(stageRepository.updateStage()).map(this::toStage).runWith(Sink.head(),
		actorSystem);
    }

    public CompletionStage<StageKey> deleteStage(DeleteStageRequest req) {
	return Source.single(req.stageKey()).via(stageRepository.deleteStage()).runWith(Sink.head(), actorSystem);
    }

    public CompletionStage<Optional<Stage>> getStage(GetStageRequest req) {
	return stageRepository.getById(req.stageKey()).map(this::toStage)
		.mapAsync(DEFAULT_PARALLERISM, this::getStageWithActivities).runWith(Sink.headOption(), actorSystem);
    }

    public CompletionStage<List<Stage>> getTableStages(ListTableStagesRequest req) {
	return stageRepository.getTableStages(req.tableKey()).map(this::toStage)
		.mapAsync(DEFAULT_PARALLERISM, this::getStageWithActivities).runWith(Sink.seq(), actorSystem);
    }

    private CompletionStage<Stage> getStageWithActivities(Stage stage) {
	var listReq = new ListStageActivitiesRequest(stage.key());
	return activityService.getStageActivities(listReq).thenApply(stage::withActivities);
    }

    private Stage toStage(StageEntity entity) {
	return new Stage(entity.key(), entity.title(), entity.state(), entity.createdAt(), entity.modifiedAt(),
		emptyList());
    }
}
