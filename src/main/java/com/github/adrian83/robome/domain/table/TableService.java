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
    public TableService(TableRepository tableRepository, StageService stageService, ActorSystem actorSystem) {
	this.tableRepository = tableRepository;
	this.stageService = stageService;
	this.actorSystem = actorSystem;
    }

    public CompletionStage<Table> saveTable(NewTableRequest req) {
	var entity = new TableEntity(TableKey.create(req.userId()), req.title(), req.description(), TableState.ACTIVE,
		Time.utcNow(), Time.utcNow());

	return Source.single(entity).via(tableRepository.saveTable()).map(this::toTable).runWith(Sink.head(),
		actorSystem);
    }

    public CompletionStage<Table> updateTable(UpdateTableRequest req) {
	var entity = new TableEntity(req.tableKey(), req.title(), req.description(), TableState.ACTIVE, Time.utcNow(),
		Time.utcNow());

	return Source.single(entity).via(tableRepository.updateTable()).map(this::toTable).runWith(Sink.head(),
		actorSystem);
    }

    public CompletionStage<TableKey> deleteTable(DeleteTableRequest req) {
	return Source.single(req.tableKey()).via(tableRepository.deleteTable(req.userId())).runWith(Sink.head(),
		actorSystem);
    }

    public CompletionStage<Optional<Table>> getTable(GetTableRequest req) {
	return tableRepository.getById(req.userId(), req.tableKey().tableId()).map(this::toTable)
		.mapAsync(DEFAULT_PARALLERISM, this::getTableWithStages).runWith(Sink.headOption(), actorSystem);
    }

    public CompletionStage<List<Table>> getTables(ListTablesRequest req) {
	return tableRepository.getUserTables(req.userId()).map(this::toTable).runWith(Sink.seq(), actorSystem);
    }

    private CompletionStage<Table> getTableWithStages(Table table) {
	var listReq = new ListTableStagesRequest(table.key());
	return stageService.getTableStages(listReq).thenApply(table::withStages);
    }

    private Table toTable(TableEntity entity) {
	return new Table(entity.key(), entity.title(), entity.description(), entity.state(), entity.createdAt(),
		entity.modifiedAt(), Collections.emptyList());
    }
}
