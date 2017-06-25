package ab.java.robome.domain.table;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.google.inject.Inject;

import ab.java.robome.domain.table.model.Table;
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
	
	public CompletionStage<Optional<Table>>  getTable(UUID tableUuid) {
		return tableRepository.getById(tableUuid)
				.runWith(Sink.head(), actorMaterializer);
	}
	
	public CompletionStage<Done> saveTable(Table newTable) {
		Source<Table, NotUsed> source = Source.single(newTable);
		Sink<Table, CompletionStage<Done>> sink = tableRepository.saveTable();
		return source.runWith(sink, actorMaterializer);
	}

	public CompletionStage<List<Table>> getTables() {
		return tableRepository.getAllTables()
				.runWith(Sink.seq(), actorMaterializer);
	}

}
