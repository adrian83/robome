package com.github.adrian83.robome.domain.activity.model;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;

import java.util.UUID;

import com.github.adrian83.robome.domain.stage.model.StageKey;

public record ActivityKey(UUID tableId, UUID stageId, UUID activityId) {

  public static ActivityKey parse(String tableIdStr, String stageIdStr, String activityIdStr) {
    return new ActivityKey(
        fromString(tableIdStr), fromString(stageIdStr), fromString(activityIdStr));
  }

  public static ActivityKey randomWithStageKey(StageKey stageKey) {
    return new ActivityKey(stageKey.tableId(), stageKey.stageId(), randomUUID());
  }
}
