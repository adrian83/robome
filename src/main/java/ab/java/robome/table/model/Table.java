package ab.java.robome.table.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Table {

	final UUID id;
	final String name;
	final TableState state;
	final LocalDateTime createdAt;
	final LocalDateTime modifiedAt;
	
	@JsonCreator
	public Table(UUID id, String name, TableState state, LocalDateTime createdAt, LocalDateTime modifiedAt) {
		super();
		this.id = id;
		this.name = name;
		this.state = state;
		this.createdAt = createdAt;
		this.modifiedAt = modifiedAt;
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public TableState getState() {
		return state;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getModifiedAt() {
		return modifiedAt;
	}

	
	

}
