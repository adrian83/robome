package com.github.adrian83.robome.domain.table;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.domain.stage.StageService;
import com.github.adrian83.robome.domain.stage.model.ListTableStagesRequest;
import com.github.adrian83.robome.domain.table.model.Table;
import com.github.adrian83.robome.domain.table.model.TableEntity;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.table.model.request.DeleteTableRequest;
import com.github.adrian83.robome.domain.table.model.request.GetTableRequest;
import com.github.adrian83.robome.domain.table.model.request.ListTablesRequest;
import com.github.adrian83.robome.domain.table.model.request.NewTableRequest;
import com.github.adrian83.robome.domain.table.model.request.UpdateTableRequest;
import com.google.inject.Inject;

import akka.actor.ActorSystem;
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

  public CompletionStage<Optional<Table>> getTable(GetTableRequest req) {
    return tableRepository
        .getById(req.getUserId(), req.getTableKey().getTableId())
        .map((maybeEntity) -> maybeEntity.map(this::toTable))
        .mapAsync(1, (table) -> fetchStages(req.getUserId(), req.getTableKey(), table))
        .runWith(Sink.head(), actorSystem);
  }

  public CompletionStage<Table> saveTable(NewTableRequest req) {
    var entity = new TableEntity(req.getUserId(), req.getTitle(), req.getDescription());

    Sink<TableEntity, CompletionStage<Table>> sink =
        tableRepository
            .saveTable()
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> toTable(entity)));

    return Source.single(entity).runWith(sink, actorSystem);
  }

  public CompletionStage<Table> updateTable(UpdateTableRequest req) {

    TableEntity entity =
        TableEntity.newTable(
            req.getTableKey(), req.getUserId(), req.getTitle(), req.getDescription());

    Sink<TableEntity, CompletionStage<Table>> sink =
        tableRepository
            .updateTable()
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> toTable(entity)));

    return Source.single(entity).runWith(sink, actorSystem);
  }

  public CompletionStage<List<Table>> getTables(ListTablesRequest req) {
    return tableRepository
        .getUserTables(req.getUserId())
        .map(this::toTable)
        .runWith(Sink.seq(), actorSystem);
  }

  public CompletionStage<TableKey> deleteTable(DeleteTableRequest req) {
    Sink<TableKey, CompletionStage<TableKey>> sink =
        tableRepository
            .deleteTable(req.getUserId())
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> req.getTableKey()));

    return Source.single(req.getTableKey()).runWith(sink, actorSystem);
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

  private CompletionStage<Optional<Table>> fetchStages(UUID userId, Table table) {
    var listReq = ListTableStagesRequest.builder().userId(userId).tableKey(table.getKey()).build();
    return stageService
        .getTableStages(listReq)
        .thenApply((stages) -> table.withStages(stages))
        .thenApply(Optional::ofNullable);
  }

  private CompletionStage<Optional<Table>> fetchStages(
      UUID userId, TableKey tableKey, Optional<Table> maybeTable) {
    return maybeTable
        .map((table) -> fetchStages(userId, table))
        .orElse(CompletableFuture.<Optional<Table>>completedFuture(Optional.<Table>empty()));
  }
}
