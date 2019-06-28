package com.github.adrian83.robome.domain.stage;

import java.time.LocalDateTime;
import java.util.UUID;

import com.github.adrian83.robome.domain.table.model.TableState;

public class Stage {

	private StageId stageId;
	private UUID userId;
	private String title;
	private TableState state;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;

	public Stage(StageId stageId, UUID userId, String title, TableState state, LocalDateTime createdAt, LocalDateTime modifiedAt) {
		super();
		this.stageId = stageId;
		this.userId = userId;
		this.title = title;
		this.state = state;
		this.createdAt = createdAt;
		this.modifiedAt = modifiedAt;
	}

	public StageId getStageId() {
		return stageId;
	}

	public UUID getUserId() {
		return userId;
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
