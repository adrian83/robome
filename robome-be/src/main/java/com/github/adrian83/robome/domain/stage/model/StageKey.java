package com.github.adrian83.robome.domain.stage.model;

import java.util.UUID;

import com.github.adrian83.robome.domain.table.model.TableKey;

public class StageKey extends TableKey {
	
  private UUID stageId;

  public static StageKey fromStrings(String tableIdStr, String stageIdStr) {
    return new StageKey(UUID.fromString(tableIdStr), UUID.fromString(stageIdStr));
  }

  public StageKey() {
    this(UUID.randomUUID(), UUID.randomUUID());
  }

  public StageKey(UUID tableId, UUID stageId) {
    super(tableId);
    this.stageId = stageId;
  }

  public UUID getStageId() {
    return stageId;
  }
}
