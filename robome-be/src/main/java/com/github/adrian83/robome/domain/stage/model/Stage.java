package com.github.adrian83.robome.domain.stage.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.github.adrian83.robome.common.time.TimeUtils;

public class Stage {

	private StageId stageId;
	private UUID userId;
	private String title;
	private StageState state;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;

	public Stage(StageId stageId, UUID userId, String title, StageState state, LocalDateTime createdAt, LocalDateTime modifiedAt) {
		super();
		this.stageId = stageId;
		this.userId = userId;
		this.title = title;
		this.state = state;
		this.createdAt = createdAt;
		this.modifiedAt = modifiedAt;
	}
	
	public Stage(UUID userId, String title) {
		this(new StageId(),  userId,  title, StageState.ACTIVE, TimeUtils.utcNow(), TimeUtils.utcNow());
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

	public StageState getState() {
		return state;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getModifiedAt() {
		return modifiedAt;
	}

}
