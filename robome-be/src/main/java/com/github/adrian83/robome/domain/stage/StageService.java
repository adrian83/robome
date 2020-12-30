package com.github.adrian83.robome.domain.stage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.domain.activity.ActivityService;
import com.github.adrian83.robome.domain.activity.model.request.ListStageActivitiesRequest;
import com.github.adrian83.robome.domain.stage.model.Stage;
import com.github.adrian83.robome.domain.stage.model.StageEntity;
import com.github.adrian83.robome.domain.stage.model.StageKey;
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

  private StageRepository stageRepository;
  private ActivityService activityService;
  private ActorSystem actorSystem;

  @Inject
  public StageService(
      StageRepository stageRepository, ActivityService activityService, ActorSystem actorSystem) {
    this.stageRepository = stageRepository;
    this.actorSystem = actorSystem;
    this.activityService = activityService;
  }

  public CompletionStage<Optional<Stage>> getStage(GetStageRequest req) {
    return stageRepository
        .getById(req.getUserId(), req.getStageKey())
        .map((maybeStage) -> maybeStage.map(this::toStage))
        .mapAsync(1, (maybeStage) -> fetchStageActivities(req.getUserId(), maybeStage))
        .runWith(Sink.head(), actorSystem);
  }

  public CompletionStage<List<Stage>> getTableStages(ListTableStagesRequest req) {
    return stageRepository
        .getTableStages(req.getUserId(), req.getTableKey().getTableId())
        .map(this::toStage)
        .mapAsync(1, (stage) -> fetchActivities(req.getUserId(), stage))
        .runWith(Sink.seq(), actorSystem);
  }

  public CompletionStage<Stage> saveStage(NewStageRequest req) {
    var entity = new StageEntity(req.getTableKey(), req.getUserId(), req.getTitle());
    var sink =
        stageRepository
            .saveStage()
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> toStage(entity)));
    return Source.single(entity).runWith(sink, actorSystem);
  }

  public CompletionStage<Stage> updateStage(UpdateStageRequest req) {
    StageEntity entity = StageEntity.newStage(req.getStageKey(), req.getUserId(), req.getTitle());
    Sink<StageEntity, CompletionStage<Stage>> sink =
        stageRepository
            .updateStage()
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> toStage(entity)));

    return Source.single(entity).runWith(sink, actorSystem);
  }

  public CompletionStage<StageKey> deleteStage(DeleteStageRequest req) {
    Sink<StageKey, CompletionStage<StageKey>> sink =
        stageRepository
            .deleteStage(req.getUserId())
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> req.getStageKey()));

    return Source.single(req.getStageKey()).runWith(sink, actorSystem);
  }

  private Stage toStage(StageEntity entity) {
    return new Stage(
        entity.getKey(),
        entity.getUserId(),
        entity.getTitle(),
        entity.getState(),
        entity.getCreatedAt(),
        entity.getModifiedAt());
  }

  private CompletionStage<Stage> fetchActivities(UUID userId, Stage stage) {
    var listReq =
        ListStageActivitiesRequest.builder().userId(userId).stageKey(stage.getKey()).build();
    return activityService
        .getStageActivities(listReq)
        .thenApply((activities) -> stage.withActivities(activities));
  }

  private CompletionStage<Optional<Stage>> getStageWithActivities(UUID userId, Stage stage) {
    var listReq =
        ListStageActivitiesRequest.builder().userId(userId).stageKey(stage.getKey()).build();
    return activityService
        .getStageActivities(listReq)
        .thenApply((activities) -> stage.withActivities(activities))
        .thenApply((resultStage) -> Optional.of(resultStage));
  }

  private CompletionStage<Optional<Stage>> fetchStageActivities(
      UUID userId, Optional<Stage> maybeStage) {
    return maybeStage
        .map((stage) -> getStageWithActivities(userId, stage))
        .orElse(CompletableFuture.completedFuture(Optional.empty()));
  }
}
