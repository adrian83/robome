package ab.java.robome.domain.stage.model;

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
