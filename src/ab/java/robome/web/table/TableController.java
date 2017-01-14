package ab.java.robome.web.table;

import com.google.inject.Inject;

import ab.java.robome.table.TableService;



public class TableController {
	
	private TableService tableService;
	
	@Inject
	public TableController(TableService tableService) {
		this.tableService = tableService;
	}

}
