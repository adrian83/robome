package com.github.adrian83.robome.domain.stage;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.domain.stage.model.NewStage;
import com.github.adrian83.robome.domain.stage.model.Stage;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.user.model.User;
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

  public CompletionStage<Optional<Stage>> getStage(User user, StageKey stageKey) {
    return stageRepository.getById(user.getId(), stageKey).runWith(Sink.head(), actorMaterializer);
  }

  public CompletionStage<List<Stage>> getTableStages(User user, TableKey id) {
    return stageRepository
        .getTableStages(user.getId(), id.getTableId())
        .runWith(Sink.seq(), actorMaterializer);
  }

  public CompletionStage<Stage> saveStage(User user, TableKey tableKey, NewStage newStage) {
    var stage = new Stage(tableKey, user.getId(), newStage.getName());
    var sink =
        stageRepository.saveStage().mapMaterializedValue(doneF -> doneF.thenApply(done -> stage));
    return Source.lazily(() -> Source.single(stage)).runWith(sink, actorMaterializer);
  }
}
