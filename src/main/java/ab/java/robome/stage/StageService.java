package ab.java.robome.stage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.google.inject.Inject;

import ab.java.robome.stage.model.Stage;
import ab.java.robome.stage.model.StageId;
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

	public CompletionStage<Optional<Stage>> getStage(StageId stageId) {
		return stageRepository.getById(stageId)
				.runWith(Sink.head(), actorMaterializer);
	}
	
	public CompletionStage<List<Stage>> getTableStages(UUID tableUuid) {
		return stageRepository.getTableStages(tableUuid)
				.runWith(Sink.seq(), actorMaterializer);
	}
	
	public CompletionStage<Done> saveStage(Stage newStage) {
		Source<Stage, CompletionStage<NotUsed>> source = Source.lazily(() -> Source.single(newStage));
		Sink<Stage, CompletionStage<Done>> sink = stageRepository.saveStage();
		return source.runWith(sink, actorMaterializer);
	}
	
}
