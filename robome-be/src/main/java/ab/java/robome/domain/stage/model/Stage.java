package ab.java.robome.domain.stage.model;

import java.time.LocalDateTime;

import ab.java.robome.domain.table.model.TableState;

public class Stage {

	private StageId stageId;
	private String title;
	private TableState state;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;

	public Stage(StageId stageId, String title, TableState state, LocalDateTime createdAt, LocalDateTime modifiedAt) {
		super();
		this.stageId = stageId;
		this.title = title;
		this.state = state;
		this.createdAt = createdAt;
		this.modifiedAt = modifiedAt;
	}

	public StageId getStageId() {
		return stageId;
	}

	public String getTitle() {
		return title;
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
