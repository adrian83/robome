package ab.java.robome.table;

import com.google.inject.Inject;

public class TableService {
	
	private TableRepository tableRepository;
	
	@Inject
	public TableService(TableRepository repository) {
		this.tableRepository = repository;
	}

}
