package com.github.adrian83.robome.domain.table;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.auth.PermissionChecker;
import com.github.adrian83.robome.auth.UserNotFoundException;
import com.github.adrian83.robome.domain.table.Table;
import com.github.adrian83.robome.domain.user.User;
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
		return tableRepository.getById(userId, tableUuid).runWith(Sink.head(), actorMaterializer);
	}

	public CompletionStage<Done> saveTable(Table newTable) {
		Source<Table, NotUsed> source = Source.single(newTable);
		Sink<Table, CompletionStage<Done>> sink = tableRepository.saveTable();
		return source.runWith(sink, actorMaterializer);
	}

	public CompletionStage<List<Table>> getTables(CompletionStage<Optional<User>> maybeUserF) {
		return maybeUserF.thenApply(this::getUser).thenApply(this::canReadTables).thenCompose(
				(user) -> tableRepository.getUserTables(user.getId()).runWith(Sink.seq(), actorMaterializer));
	}

	private User canReadTables(User user) {
		return Optional.ofNullable(PermissionChecker.check(user).canListTables().permitted())
				.map((d) -> user)
				.orElseThrow(() -> new RuntimeException("cannot read tables"));
	}
	
	private User getUser(Optional<User> maybeUser) {
		return maybeUser.orElseThrow(() -> new UserNotFoundException("user cannotbe found"));
	}

}
