package com.github.adrian83.robome.domain.stage.model;

import java.util.UUID;

public class StageId {
  private UUID tableId;
  private UUID stageId;

  public static StageId fromStrings(String tableIdStr, String stageIdStr) {
    return new StageId(UUID.fromString(tableIdStr), UUID.fromString(stageIdStr));
  }

  public StageId() {
    this(UUID.randomUUID(), UUID.randomUUID());
  }

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
