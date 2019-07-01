package com.github.adrian83.robome.domain.stage;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.domain.table.model.TableId;
import com.github.adrian83.robome.domain.user.User;
import com.google.inject.Inject;

import akka.Done;
import akka.NotUsed;
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
		return stageRepository.getById(user.getId(), stageId)
				.runWith(Sink.head(), actorMaterializer);
	}
	
	public CompletionStage<List<Stage>> getTableStages(User user, TableId tableId) {
		return stageRepository.getTableStages(user.getId(), tableId.getTableId())
				.runWith(Sink.seq(), actorMaterializer);
	}
	
	public CompletionStage<Done> saveStage(Stage newStage) {
		Source<Stage, CompletionStage<NotUsed>> source = Source.lazily(() -> Source.single(newStage));
		Sink<Stage, CompletionStage<Done>> sink = stageRepository.saveStage();
		return source.runWith(sink, actorMaterializer);
	}
	
}
