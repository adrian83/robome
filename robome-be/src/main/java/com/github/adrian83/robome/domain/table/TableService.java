package com.github.adrian83.robome.domain.table;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import akka.japi.function.Function;

import com.github.adrian83.robome.domain.stage.StageService;
import com.github.adrian83.robome.domain.stage.model.Stage;
import com.github.adrian83.robome.domain.table.model.NewTable;
import com.github.adrian83.robome.domain.table.model.Table;
import com.github.adrian83.robome.domain.table.model.TableEntity;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.table.model.UpdatedTable;
import com.github.adrian83.robome.domain.user.model.User;
import com.google.inject.Inject;

import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class TableService {

  private TableRepository tableRepository;
  private StageService stageService;
  private ActorMaterializer actorMaterializer;

  @Inject
  public TableService(TableRepository tableRepository, StageService stageService, ActorMaterializer actorMaterializer) {
    this.tableRepository = tableRepository;
    this.stageService = stageService;
    this.actorMaterializer = actorMaterializer;
  }


  
  public CompletionStage<Optional<Table>> getTable(User user, TableKey tableId) {
	 return tableRepository
        .getById(user.getId(), tableId.getTableId())
        .map((maybeEntity) -> maybeEntity.map(this::toTable))
        .mapAsync(1, fetchStages(user,  tableId))
        .runWith(Sink.head(), actorMaterializer);
  }
  
  public CompletionStage<Table> saveTable(User user, NewTable newTable) {

    TableEntity entity = new TableEntity(user.getId(), newTable.getTitle(), newTable.getDescription());

    Sink<TableEntity, CompletionStage<Table>> sink =
        tableRepository.saveTable()
        .mapMaterializedValue(doneF -> doneF.thenApply(done -> toTable(entity)));

    return Source.lazily(() -> Source.single(entity)).runWith(sink, actorMaterializer);
  }

  public CompletionStage<Table> updateTable(User user, TableKey tableID, UpdatedTable updatedTable) {

    TableEntity entity =
        TableEntity.newTable(
            tableID, user.getId(), updatedTable.getTitle(), updatedTable.getDescription());

    Sink<TableEntity, CompletionStage<Table>> sink =
        tableRepository.updateTable(entity).mapMaterializedValue(doneF -> doneF.thenApply(done -> toTable(entity)));

    return Source.lazily(() -> Source.single(entity)).runWith(sink, actorMaterializer);
  }

  public CompletionStage<List<Table>> getTables(User user) {

    return tableRepository.getUserTables(user.getId()).map(this::toTable).runWith(Sink.seq(), actorMaterializer);
  }

  public CompletionStage<TableKey> deleteTable(User user, TableKey tableId) {

    Sink<TableKey, CompletionStage<TableKey>> sink =
        tableRepository
            .deleteTable(tableId, user.getId())
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> tableId));

    return Source.lazily(() -> Source.single(tableId)).runWith(sink, actorMaterializer);
  }
  
  private Table toTable(TableEntity entity) {
	  return new Table(entity.getKey(), entity.getUserId(), entity.getTitle(), 
			  entity.getDescription(), entity.getState(), entity.getCreatedAt(), entity.getModifiedAt());
  }
  
  protected Function<Optional<Table>, CompletionStage<Optional<Table>>> fetchStages(User user, TableKey tableId){
	  CompletionStage<List<Stage>> stagesF = stageService.getTableStages(user, tableId);
	  return (Optional<Table> maybeTable) -> stagesF.thenApply((stages) -> maybeTable.map((table) -> table.withStages(stages)));
  }
}
