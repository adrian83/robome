package com.github.adrian83.robome.domain.stage.model;

import static java.util.UUID.randomUUID;

import java.util.UUID;

import com.github.adrian83.robome.domain.table.model.TableKey;

public class StageKey extends TableKey {

  private UUID stageId;

  public static StageKey fromStrings(String tableIdStr, String stageIdStr) {
    return new StageKey(UUID.fromString(tableIdStr), UUID.fromString(stageIdStr));
  }

  public StageKey(TableKey tableKey) {
    this(tableKey.getTableId(), randomUUID());
  }

  public StageKey(UUID tableId, UUID stageId) {
    super(tableId);
    this.stageId = stageId;
  }

  public UUID getStageId() {
    return stageId;
  }

  @Override
  public String toString() {
    return "StageKey [tableId=" + getTableId() + ", stageId=" + stageId + "]";
  }
}
