package ab.java.robome.table;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.google.inject.Inject;

import ab.java.robome.table.model.Table;
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
	
	public CompletionStage<Optional<Table>>  getTable(String tableId) {
		return Source.lazily(() -> Source.single(tableId))
				.map(UUID::fromString)
				.map(tableRepository::getById)
				.runWith(Sink.head(), actorMaterializer);
	}
	
	public CompletionStage<Done> saveTable(Table newTable) {
		Source<Table, CompletionStage<NotUsed>> source = Source.lazily(() -> Source.single(newTable));
		Sink<Table, CompletionStage<Done>> sink = tableRepository.saveTable();
		return source.runWith(sink, actorMaterializer);
		
	}

}
