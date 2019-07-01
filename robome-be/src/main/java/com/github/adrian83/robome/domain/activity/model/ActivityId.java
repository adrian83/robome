package com.github.adrian83.robome.domain.activity.model;

import java.util.UUID;

public class ActivityId {
  private UUID tableId;
  private UUID stageId;
  private UUID activityId;

  public static ActivityId fromStrings(String tableIdStr, String stageIdStr, String activityIdStr) {
    return new ActivityId(
        UUID.fromString(tableIdStr), UUID.fromString(stageIdStr), UUID.fromString(activityIdStr));
  }

  public ActivityId(UUID tableId, UUID stageId, UUID activityId) {
    super();
    this.stageId = stageId;
    this.tableId = tableId;
    this.activityId = activityId;
  }

  public ActivityId() {
    this(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
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
