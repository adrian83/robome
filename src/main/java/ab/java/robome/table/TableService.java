package ab.java.robome.table;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.google.inject.Inject;

import ab.java.robome.table.model.Table;
import akka.NotUsed;
import akka.stream.javadsl.Source;

public class TableService {
	
	private TableRepository tableRepository;
	
	@Inject
	public TableService(TableRepository repository) {
		this.tableRepository = repository;
	}
	
	public Source<Optional<Table>,CompletionStage<NotUsed>> getTable(String tableId) {
		return Source.lazily(() -> Source.single(tableId))
				.map(UUID::fromString)
				.map(tableRepository::getById);
	}
	

}
