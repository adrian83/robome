package com.github.adrian83.robome.domain.stage;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.domain.activity.ActivityService;
import com.github.adrian83.robome.domain.stage.model.NewStage;
import com.github.adrian83.robome.domain.stage.model.Stage;
import com.github.adrian83.robome.domain.stage.model.StageEntity;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.domain.stage.model.UpdatedStage;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.user.model.User;
import com.google.inject.Inject;

import akka.actor.ActorSystem;
import akka.japi.function.Function;
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
        .mapAsync(1, fetchStageActivities(user))
        .runWith(Sink.head(), actorSystem);
  }

  public CompletionStage<List<Stage>> getTableStages(User user, TableKey id) {
    return stageRepository
        .getTableStages(user.getId(), id.getTableId())
        .map(this::toStage)
        .mapAsync(1, fetchActivities(user))
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
            .updateStage(entity)
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> toStage(entity)));

    return Source.single(entity).runWith(sink, actorSystem);
  }

  public CompletionStage<StageKey> deleteStage(User user, StageKey stageKey) {
    Sink<StageKey, CompletionStage<StageKey>> sink =
        stageRepository
            .deleteStage(stageKey, user.getId())
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

  protected Function<Stage, CompletionStage<Stage>> fetchActivities(User user) {
    return (Stage stage) ->
        activityService
            .getStageActivities(user, stage.getKey())
            .thenApply((activities) -> stage.withActivities(activities));
  }

  protected Function<Optional<Stage>, CompletionStage<Optional<Stage>>> fetchStageActivities(
      User user) {
    return (Optional<Stage> maybeStage) ->
        maybeStage
            .map(
                (stage) ->
                    activityService
                        .getStageActivities(user, stage.getKey())
                        .thenApply((activities) -> stage.withActivities(activities))
                        .thenApply((resultStage) -> Optional.of(resultStage)))
            .orElse(CompletableFuture.completedFuture(Optional.empty()));
  }
}
