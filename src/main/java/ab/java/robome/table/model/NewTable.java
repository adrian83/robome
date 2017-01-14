package ab.java.robome.table.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NewTable {

	final String name;

	@JsonCreator
	public NewTable(@JsonProperty("name") String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	
}
