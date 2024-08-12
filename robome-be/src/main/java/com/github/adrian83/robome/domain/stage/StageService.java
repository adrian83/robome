package com.github.adrian83.robome.domain.stage;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.common.Time;
import com.github.adrian83.robome.domain.activity.ActivityService;
import com.github.adrian83.robome.domain.activity.model.request.ListStageActivitiesQuery;
import com.github.adrian83.robome.domain.stage.model.Stage;
import com.github.adrian83.robome.domain.stage.model.StageEntity;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.domain.stage.model.StageState;
import com.github.adrian83.robome.domain.stage.model.request.DeleteStageCommand;
import com.github.adrian83.robome.domain.stage.model.request.GetStageQuery;
import com.github.adrian83.robome.domain.stage.model.request.ListTableStagesQuery;
import com.github.adrian83.robome.domain.stage.model.request.NewStageCommand;
import com.github.adrian83.robome.domain.stage.model.request.UpdateStageCommand;
import com.google.inject.Inject;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class StageService {

    private static final int DEFAULT_PARALLERISM = 1;

    private final StageRepository stageRepository;
    private final ActivityService activityService;
    private final ActorSystem actorSystem;

    @Inject
    public StageService(StageRepository stageRepository, ActivityService activityService, ActorSystem actorSystem) {
        this.stageRepository = stageRepository;
        this.actorSystem = actorSystem;
        this.activityService = activityService;
    }

    public CompletionStage<Stage> saveStage(NewStageCommand req) {
        var entity = new StageEntity(StageKey.basedOnTableKey(req.tableKey()), req.title(), StageState.ACTIVE, Time.utcNow(), Time.utcNow());
        return Source.single(entity).via(stageRepository.saveStage())
                .map(this::toStage)
                .runWith(Sink.head(), actorSystem);
    }

    public CompletionStage<Stage> updateStage(UpdateStageCommand req) {
        var entity = new StageEntity(req.stageKey(), req.title(), StageState.ACTIVE, Time.utcNow(), Time.utcNow());
        return Source.single(entity).
                via(stageRepository.updateStage())
                .map(this::toStage)
                .runWith(Sink.head(), actorSystem);
    }

    public CompletionStage<StageKey> deleteStage(DeleteStageCommand req) {
        return Source.single(req.stageKey())
                .via(stageRepository.deleteStage())
                .runWith(Sink.head(), actorSystem);
    }

    public CompletionStage<Optional<Stage>> getStage(GetStageQuery req) {
        return stageRepository.getById(req.stageKey())
                .map(this::toStage)
                .mapAsync(DEFAULT_PARALLERISM, this::getStageWithActivities)
                .runWith(Sink.headOption(), actorSystem);
    }

    public CompletionStage<List<Stage>> getTableStages(ListTableStagesQuery req) {
        return stageRepository.getTableStages(req.tableKey())
                .map(this::toStage)
                .mapAsync(DEFAULT_PARALLERISM, this::getStageWithActivities)
                .runWith(Sink.seq(), actorSystem);
    }

    private CompletionStage<Stage> getStageWithActivities(Stage stage) {
        var listReq = new ListStageActivitiesQuery(stage.key());
        return activityService.getStageActivities(listReq)
                .thenApply(stage::withActivities);
    }

    private Stage toStage(StageEntity entity) {
        return new Stage(entity.key(), entity.title(), entity.state(), entity.createdAt(), entity.modifiedAt(), emptyList());
    }
}
