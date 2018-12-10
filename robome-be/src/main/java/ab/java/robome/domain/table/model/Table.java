package ab.java.robome.domain.table.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Table {

	private TableId id;
	private UUID userId;
	private String title;
	private String description;
	private TableState state;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;

	public Table(TableId id, UUID userId, String title, String description, TableState state, LocalDateTime createdAt,
			LocalDateTime modifiedAt) {
		super();
		this.id = id;
		this.userId = userId;
		this.title = title;
		this.description = description;
		this.state = state;
		this.createdAt = createdAt;
		this.modifiedAt = modifiedAt;
	}

	public TableId getId() {
		return id;
	}

	public UUID getUserId() {
		return userId;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
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
