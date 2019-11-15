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

import akka.japi.function.Function;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class StageService {

  private StageRepository stageRepository;
  private ActorMaterializer actorMaterializer;
  private ActivityService activityService;

  @Inject
  public StageService(StageRepository stageRepository, ActivityService activityService, ActorMaterializer actorMaterializer) {
    this.stageRepository = stageRepository;
    this.actorMaterializer = actorMaterializer;
    this.activityService = activityService;
  }

  public CompletionStage<Optional<Stage>> getStage(User user, StageKey stageKey) {
    return stageRepository.getById(user.getId(), stageKey)
    		.map((maybeStage) -> maybeStage.map(this::toStage))
    		.mapAsync(1, fetchActivities2(user))
    		.runWith(Sink.head(), actorMaterializer);
  }

  public CompletionStage<List<Stage>> getTableStages(User user, TableKey id) {
    return stageRepository
        .getTableStages(user.getId(), id.getTableId())
        .map(this::toStage)
        .mapAsync(1, fetchActivities(user))
        .runWith(Sink.seq(), actorMaterializer);
  }

  public CompletionStage<Stage> saveStage(User user, TableKey tableKey, NewStage newStage) {
    var entity = new StageEntity(tableKey, user.getId(), newStage.getName());
    var sink = stageRepository.saveStage().mapMaterializedValue(doneF -> doneF.thenApply(done -> toStage(entity)));
    return Source.lazily(() -> Source.single(entity)).runWith(sink, actorMaterializer);
  }
  
  public CompletionStage<Stage> updateStage(User user, StageKey key, UpdatedStage updatedStage) {

	    StageEntity entity = StageEntity.newStage(key, user.getId(), updatedStage.getTitle());

	    Sink<StageEntity, CompletionStage<Stage>> sink =
	        stageRepository.updateStage(entity).mapMaterializedValue(doneF -> doneF.thenApply(done -> toStage(entity)));

	    return Source.lazily(() -> Source.single(entity)).runWith(sink, actorMaterializer);
	  }
  
  public CompletionStage<StageKey> deleteStage(User user, StageKey stageKey) {

	    Sink<StageKey, CompletionStage<StageKey>> sink =
	    		stageRepository
	            .deleteStage(stageKey, user.getId())
	            .mapMaterializedValue(doneF -> doneF.thenApply(done -> stageKey));

	    return Source.lazily(() -> Source.single(stageKey)).runWith(sink, actorMaterializer);
	  }
  
  private Stage toStage(StageEntity entity) {
	  return new Stage(entity.getKey(),
			  entity.getUserId(),
			  entity.getTitle(),
			  entity.getState(),
			  entity.getCreatedAt(),
			  entity.getModifiedAt());
  }
  
  protected Function<Stage, CompletionStage<Stage>> fetchActivities(User user){
	  return (Stage stage) -> activityService.getStageActivities(user, stage.getKey())
			  .thenApply((activities) -> stage.withActivities(activities));
  }
  
  
  
  protected Function<Optional<Stage>, CompletionStage<Optional<Stage>>> fetchActivities2(User user){
	  return (Optional<Stage> maybeStage) -> maybeStage
			  	.map((stage) -> activityService.getStageActivities(user, stage.getKey())
						  .thenApply((activities) -> stage.withActivities(activities)).thenApply((resultStage) -> Optional.of(resultStage)))
			  	.orElse(CompletableFuture.completedFuture(Optional.empty()));
			  
			  
  }
}
