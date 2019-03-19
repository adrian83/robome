package com.github.adrian83.robome.domain.table;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.domain.table.Table;
import com.google.inject.Inject;

import akka.Done;
import akka.NotUsed;
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
	
	public CompletionStage<Optional<Table>> getTable(UUID userId, UUID tableUuid) {
		return tableRepository.getById(userId, tableUuid)
				.runWith(Sink.head(), actorMaterializer);
	}
	
	public CompletionStage<Done> saveTable(Table newTable) {
		Source<Table, NotUsed> source = Source.single(newTable);
		Sink<Table, CompletionStage<Done>> sink = tableRepository.saveTable();
		return source.runWith(sink, actorMaterializer);
	}

	public CompletionStage<List<Table>> getTables(UUID userId) {
		return tableRepository.getUserTables(userId)
				.runWith(Sink.seq(), actorMaterializer);
	}

}
