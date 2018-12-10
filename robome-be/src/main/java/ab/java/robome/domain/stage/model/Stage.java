package ab.java.robome.domain.stage.model;

import java.time.LocalDateTime;

import ab.java.robome.domain.table.model.TableState;

public class Stage {

	private StageId stageId;
	private String name;
	private TableState state;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;

	public Stage(StageId stageId, String name, TableState state, LocalDateTime createdAt, LocalDateTime modifiedAt) {
		super();
		this.stageId = stageId;
		this.name = name;
		this.state = state;
		this.createdAt = createdAt;
		this.modifiedAt = modifiedAt;
	}

	public StageId getStageId() {
		return stageId;
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
