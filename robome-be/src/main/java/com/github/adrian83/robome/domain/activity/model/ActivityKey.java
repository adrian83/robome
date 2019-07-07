package com.github.adrian83.robome.domain.activity.model;

import java.util.UUID;

import com.github.adrian83.robome.domain.stage.model.StageKey;

public class ActivityKey extends StageKey {

  private UUID activityId;

  public static ActivityKey fromStrings(
      String tableIdStr, String stageIdStr, String activityIdStr) {
    return new ActivityKey(
        UUID.fromString(tableIdStr), UUID.fromString(stageIdStr), UUID.fromString(activityIdStr));
  }

  public ActivityKey(UUID tableId, UUID stageId, UUID activityId) {
    super(tableId, stageId);
    this.activityId = activityId;
  }

  public ActivityKey() {
    this(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
  }

  public UUID getActivityId() {
    return activityId;
  }
}
