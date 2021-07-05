package com.github.adrian83.robome.domain.table;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.common.Time;
import com.github.adrian83.robome.domain.stage.StageService;
import com.github.adrian83.robome.domain.stage.model.request.ListTableStagesRequest;
import com.github.adrian83.robome.domain.table.model.Table;
import com.github.adrian83.robome.domain.table.model.TableEntity;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.table.model.TableState;
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

  private static final int DEFAULT_PARALLERISM = 1;

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

  public CompletionStage<Table> saveTable(NewTableRequest req) {
    var entity =
        TableEntity.builder()
            .key(TableKey.random())
            .userId(req.getUserId())
            .title(req.getTitle())
            .description(req.getDescription())
            .state(TableState.ACTIVE)
            .modifiedAt(Time.utcNow())
            .createdAt(Time.utcNow())
            .build();

    return Source.single(entity)
        .via(tableRepository.saveTable())
        .map(this::toTable)
        .runWith(Sink.head(), actorSystem);
  }

  public CompletionStage<Table> updateTable(UpdateTableRequest req) {
    var entity =
        TableEntity.builder()
            .key(req.getTableKey())
            .userId(req.getUserId())
            .title(req.getTitle())
            .description(req.getDescription())
            .state(TableState.ACTIVE)
            .modifiedAt(Time.utcNow())
            .createdAt(Time.utcNow())
            .build();

    return Source.single(entity)
        .via(tableRepository.updateTable())
        .map(this::toTable)
        .runWith(Sink.head(), actorSystem);
  }

  public CompletionStage<TableKey> deleteTable(DeleteTableRequest req) {
    return Source.single(req.getTableKey())
        .via(tableRepository.deleteTable(req.getUserId()))
        .runWith(Sink.head(), actorSystem);
  }

  public CompletionStage<Optional<Table>> getTable(GetTableRequest req) {
    return tableRepository
        .getById(req.getUserId(), req.getTableKey().getTableId())
        .map(this::toTable)
        .mapAsync(DEFAULT_PARALLERISM, this::getTableWithStages)
        .runWith(Sink.headOption(), actorSystem);
  }

  public CompletionStage<List<Table>> getTables(ListTablesRequest req) {
    return tableRepository
        .getUserTables(req.getUserId())
        .map(this::toTable)
        .runWith(Sink.seq(), actorSystem);
  }

  private CompletionStage<Table> getTableWithStages(Table table) {
    var listReq =
        ListTableStagesRequest.builder().userId(table.getUserId()).tableKey(table.getKey()).build();

    return stageService.getTableStages(listReq).thenApply(table::copyWithStages);
  }

  private Table toTable(TableEntity entity) {
    return Table.builder()
        .key(entity.getKey())
        .userId(entity.getUserId())
        .title(entity.getTitle())
        .description(entity.getDescription())
        .state(entity.getState())
        .modifiedAt(entity.getModifiedAt())
        .createdAt(entity.getCreatedAt())
        .stages(Collections.emptyList())
        .build();
  }
}
