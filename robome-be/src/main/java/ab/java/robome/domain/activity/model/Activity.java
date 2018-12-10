package ab.java.robome.domain.activity.model;

import java.time.LocalDateTime;

import ab.java.robome.domain.table.model.TableState;

public class Activity {

	private ActivityId id;
	private String name;
	private TableState state;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;

	public Activity(ActivityId id, String name, TableState state, LocalDateTime createdAt, LocalDateTime modifiedAt) {
		super();
		this.id = id;
		this.name = name;
		this.state = state;
		this.createdAt = createdAt;
		this.modifiedAt = modifiedAt;
	}

	public ActivityId getId() {
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
