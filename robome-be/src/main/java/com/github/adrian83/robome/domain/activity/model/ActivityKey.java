package com.github.adrian83.robome.domain.activity.model;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;

import java.util.UUID;

import com.github.adrian83.robome.domain.stage.model.StageKey;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@ToString
@EqualsAndHashCode(callSuper = false)
public class ActivityKey extends StageKey {

  private UUID activityId;

  public static ActivityKey parse(String tableIdStr, String stageIdStr, String activityIdStr) {
    return ActivityKey.builder()
        .tableId(fromString(tableIdStr))
        .stageId(fromString(stageIdStr))
        .activityId(fromString(activityIdStr))
        .build();
  }

  public static ActivityKey randomWithStageKey(StageKey stageKey) {
    return ActivityKey.builder()
        .tableId(stageKey.getTableId())
        .stageId(stageKey.getStageId())
        .activityId(randomUUID())
        .build();
  }
}
