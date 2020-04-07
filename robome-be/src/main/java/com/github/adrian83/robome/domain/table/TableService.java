package com.github.adrian83.robome.domain.table;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import akka.actor.ActorSystem;
import akka.japi.function.Function;

import com.github.adrian83.robome.domain.stage.StageService;
import com.github.adrian83.robome.domain.table.model.NewTable;
import com.github.adrian83.robome.domain.table.model.Table;
import com.github.adrian83.robome.domain.table.model.TableEntity;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.table.model.UpdatedTable;
import com.github.adrian83.robome.domain.user.model.User;
import com.google.inject.Inject;

import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class TableService {

  private TableRepository tableRepository;
  private StageService stageService;
  private ActorSystem actorSystem;

  @Inject
  public TableService(
      TableRepository tableRepository, StageService stageService, ActorSystem actorSystem) {
    this.tableRepository = tableRepository;
    this.stageService = stageService;
    this.actorSystem = actorSystem;
  }

  public CompletionStage<Optional<Table>> getTable(User user, TableKey tableId) {
    return tableRepository
        .getById(user.getId(), tableId.getTableId())
        .map((maybeEntity) -> maybeEntity.map(this::toTable))
        .mapAsync(1, fetchStages(user, tableId))
        .runWith(Sink.head(), actorSystem);
  }

  public CompletionStage<Table> saveTable(User user, NewTable newTable) {
    TableEntity entity =
        new TableEntity(user.getId(), newTable.getTitle(), newTable.getDescription());
    Sink<TableEntity, CompletionStage<Table>> sink =
        tableRepository
            .saveTable()
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> toTable(entity)));

    return Source.single(entity).runWith(sink, actorSystem);
  }

  public CompletionStage<Table> updateTable(
      User user, TableKey tableID, UpdatedTable updatedTable) {

    TableEntity entity =
        TableEntity.newTable(
            tableID, user.getId(), updatedTable.getTitle(), updatedTable.getDescription());

    Sink<TableEntity, CompletionStage<Table>> sink =
        tableRepository
            .updateTable(entity)
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> toTable(entity)));

    return Source.single(entity).runWith(sink, actorSystem);
  }

  public CompletionStage<List<Table>> getTables(User user) {
    return tableRepository
        .getUserTables(user.getId())
        .map(this::toTable)
        .runWith(Sink.seq(), actorSystem);
  }

  public CompletionStage<TableKey> deleteTable(User user, TableKey tableId) {

    Sink<TableKey, CompletionStage<TableKey>> sink =
        tableRepository
            .deleteTable(tableId, user.getId())
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> tableId));

    return Source.single(tableId).runWith(sink, actorSystem);
  }

  private Table toTable(TableEntity entity) {
    return new Table(
        entity.getKey(),
        entity.getUserId(),
        entity.getTitle(),
        entity.getDescription(),
        entity.getState(),
        entity.getCreatedAt(),
        entity.getModifiedAt());
  }

  protected Function<Optional<Table>, CompletionStage<Optional<Table>>> fetchStages(
      User user, TableKey tableId) {

    return (Optional<Table> maybeTable) -> {
      return maybeTable
          .map(
              (table) ->
                  stageService
                      .getTableStages(user, tableId)
                      .thenApply((stages) -> Optional.of(table.withStages(stages))))
          .orElse(CompletableFuture.<Optional<Table>>completedFuture(Optional.<Table>empty()));
    };
  }
}
