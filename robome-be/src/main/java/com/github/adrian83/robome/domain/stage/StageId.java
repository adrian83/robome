package com.github.adrian83.robome.domain.stage;

import java.util.UUID;

public class StageId {
	private UUID tableId;
	private UUID stageId;

	public StageId(UUID tableId, UUID stageId) {
		super();
		this.tableId = tableId;
		this.stageId = stageId;
	}

	public UUID getTableId() {
		return tableId;
	}

	public UUID getStageId() {
		return stageId;
	}

}
