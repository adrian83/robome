package ab.java.robome.table.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Table {

	final Long id;
	final String name;

	@JsonCreator
	public Table(@JsonProperty("name") String name, @JsonProperty("id") Long id) {
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public Long getId() {
		return id;
	}

}
