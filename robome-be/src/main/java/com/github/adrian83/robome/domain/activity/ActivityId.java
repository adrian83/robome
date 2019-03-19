package com.github.adrian83.robome.domain.activity;

import java.util.UUID;

public class ActivityId {
	private UUID tableId;
	private UUID stageId;
	private UUID activityId;

	public ActivityId(UUID tableId, UUID stageId, UUID activityId) {
		super();
		this.stageId = stageId;
		this.tableId = tableId;
		this.activityId = activityId;
	}

	public UUID getStageId() {
		return stageId;
	}

	public UUID getTableId() {
		return tableId;
	}

	public UUID getActivityId() {
		return activityId;
	}

}
