package com.github.adrian83.robome.domain.stage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.domain.activity.ActivityService;
import com.github.adrian83.robome.domain.activity.model.ListStageActivitiesRequest;
import com.github.adrian83.robome.domain.stage.model.ListTableStagesRequest;
import com.github.adrian83.robome.domain.stage.model.NewStage;
import com.github.adrian83.robome.domain.stage.model.Stage;
import com.github.adrian83.robome.domain.stage.model.StageEntity;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.domain.stage.model.UpdatedStage;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.user.model.User;
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

  public CompletionStage<Optional<Stage>> getStage(User user, StageKey stageKey) {
    return stageRepository
        .getById(user.getId(), stageKey)
        .map((maybeStage) -> maybeStage.map(this::toStage))
        .mapAsync(1, (maybeStage) -> fetchStageActivities(user, maybeStage))
        .runWith(Sink.head(), actorSystem);
  }

  public CompletionStage<List<Stage>> getTableStages(ListTableStagesRequest req) {
    return stageRepository
        .getTableStages(req.getUserId(), req.getTableKey().getTableId())
        .map(this::toStage)
        .mapAsync(1, (stage) -> fetchActivities(req.getUserId(), stage))
        .runWith(Sink.seq(), actorSystem);
  }

  public CompletionStage<Stage> saveStage(User user, TableKey tableKey, NewStage newStage) {
    var entity = new StageEntity(tableKey, user.getId(), newStage.getTitle());
    var sink =
        stageRepository
            .saveStage()
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> toStage(entity)));
    return Source.single(entity).runWith(sink, actorSystem);
  }

  public CompletionStage<Stage> updateStage(User user, StageKey key, UpdatedStage updatedStage) {
    StageEntity entity = StageEntity.newStage(key, user.getId(), updatedStage.getTitle());
    Sink<StageEntity, CompletionStage<Stage>> sink =
        stageRepository
            .updateStage()
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> toStage(entity)));

    return Source.single(entity).runWith(sink, actorSystem);
  }

  public CompletionStage<StageKey> deleteStage(User user, StageKey stageKey) {
    Sink<StageKey, CompletionStage<StageKey>> sink =
        stageRepository
            .deleteStage(user.getId())
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> stageKey));

    return Source.single(stageKey).runWith(sink, actorSystem);
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
	  var listReq = ListStageActivitiesRequest.builder().userId(userId).stageKey(stage.getKey()).build();
    return activityService
        .getStageActivities(listReq)
        .thenApply((activities) -> stage.withActivities(activities));
  }

  private CompletionStage<Optional<Stage>> getStageWithActivities(UUID userId, Stage stage) {
	  var listReq = ListStageActivitiesRequest.builder().userId(userId).stageKey(stage.getKey()).build();
    return activityService
        .getStageActivities(listReq)
        .thenApply((activities) -> stage.withActivities(activities))
        .thenApply((resultStage) -> Optional.of(resultStage));
  }

  private CompletionStage<Optional<Stage>> fetchStageActivities(
      User user, Optional<Stage> maybeStage) {
    return maybeStage
        .map((stage) -> getStageWithActivities(user.getId(), stage))
        .orElse(CompletableFuture.completedFuture(Optional.empty()));
  }
}
