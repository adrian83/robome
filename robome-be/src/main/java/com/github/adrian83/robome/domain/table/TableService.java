package com.github.adrian83.robome.domain.table;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.domain.table.model.NewTable;
import com.github.adrian83.robome.domain.table.model.Table;
import com.github.adrian83.robome.domain.table.model.TableId;
import com.github.adrian83.robome.domain.table.model.UpdatedTable;
import com.github.adrian83.robome.domain.user.User;
import com.google.inject.Inject;

import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class TableService {

  private TableRepository tableRepository;
  private ActorMaterializer actorMaterializer;

  @Inject
  public TableService(TableRepository repository, ActorMaterializer actorMaterializer) {
    this.tableRepository = repository;
    this.actorMaterializer = actorMaterializer;
  }

  public CompletionStage<Optional<Table>> getTable(User user, TableId tableId) {

    return tableRepository
        .getById(user.getId(), tableId.getTableId())
        .runWith(Sink.head(), actorMaterializer);
  }

  public CompletionStage<Table> saveTable(User user, NewTable newTable) {

    Table table = new Table(user.getId(), newTable.getTitle(), newTable.getDescription());

    Sink<Table, CompletionStage<Table>> sink =
        tableRepository.saveTable().mapMaterializedValue(doneF -> doneF.thenApply(done -> table));

    return Source.lazily(() -> Source.single(table)).runWith(sink, actorMaterializer);
  }

  public CompletionStage<Table> updateTable(User user, TableId tableID, UpdatedTable updatedTable) {

    Table table =
        Table.newTable(
            tableID, user.getId(), updatedTable.getTitle(), updatedTable.getDescription());

    Sink<Table, CompletionStage<Table>> sink =
        tableRepository.saveTable().mapMaterializedValue(doneF -> doneF.thenApply(done -> table));

    return Source.lazily(() -> Source.single(table)).runWith(sink, actorMaterializer);
  }

  public CompletionStage<List<Table>> getTables(User user) {

    return tableRepository.getUserTables(user.getId()).runWith(Sink.seq(), actorMaterializer);
  }

  public CompletionStage<TableId> deleteTable(User user, TableId tableId) {

    Sink<TableId, CompletionStage<TableId>> sink =
        tableRepository
            .deleteTable(tableId, user.getId())
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> tableId));

    return Source.lazily(() -> Source.single(tableId)).runWith(sink, actorMaterializer);
  }
}
