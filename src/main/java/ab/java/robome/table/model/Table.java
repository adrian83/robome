package ab.java.robome.table.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Table {

	final UUID id;
	final String name;

	@JsonCreator
	public Table(@JsonProperty("name") String name, @JsonProperty("id") UUID id) {
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public UUID getId() {
		return id;
	}

}
