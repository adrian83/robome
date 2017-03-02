package ab.java.robome.stage;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

import ab.java.robome.stage.model.Stage;
import akka.Done;

public class StageService {

	
	public CompletionStage<Optional<Stage>> getStage(String tableId, String stageId) {
		return null;
	}
	
	public CompletionStage<Done> saveStage(Stage newStage) {
		return null;
	}
	
}
