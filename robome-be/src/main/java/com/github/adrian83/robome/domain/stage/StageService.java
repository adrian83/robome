package com.github.adrian83.robome.domain.stage;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.domain.stage.model.NewStage;
import com.github.adrian83.robome.domain.stage.model.Stage;
import com.github.adrian83.robome.domain.stage.model.StageId;
import com.github.adrian83.robome.domain.table.model.TableId;
import com.github.adrian83.robome.domain.user.User;
import com.google.inject.Inject;

import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class StageService {

  private StageRepository stageRepository;
  private ActorMaterializer actorMaterializer;

  @Inject
  public StageService(StageRepository stageRepository, ActorMaterializer actorMaterializer) {
    this.stageRepository = stageRepository;
    this.actorMaterializer = actorMaterializer;
  }

  public CompletionStage<Optional<Stage>> getStage(User user, StageId stageId) {
    return stageRepository.getById(user.getId(), stageId).runWith(Sink.head(), actorMaterializer);
  }

  public CompletionStage<List<Stage>> getTableStages(User user, TableId tableId) {
    return stageRepository
        .getTableStages(user.getId(), tableId.getTableId())
        .runWith(Sink.seq(), actorMaterializer);
  }

  public CompletionStage<Stage> saveStage(User user, NewStage newStage) {

    Stage stage = new Stage(user.getId(), newStage.getName());

    Sink<Stage, CompletionStage<Stage>> sink =
        stageRepository.saveStage().mapMaterializedValue(doneF -> doneF.thenApply(done -> stage));

    return Source.lazily(() -> Source.single(stage)).runWith(sink, actorMaterializer);
  }
}
