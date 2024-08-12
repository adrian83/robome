package com.github.adrian83.robome.domain.activity.model;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import java.util.UUID;

import com.github.adrian83.robome.domain.stage.model.StageKey;

public record ActivityKey(UUID userId, UUID tableId, UUID stageId, UUID activityId) {

    public static ActivityKey create(UUID userId, String tableIdStr, String stageIdStr, String activityIdStr) {
        return new ActivityKey(userId, fromString(tableIdStr), fromString(stageIdStr), fromString(activityIdStr));
    }

    public static ActivityKey basedOnStageKey(StageKey stageKey) {
        return new ActivityKey(stageKey.userId(), stageKey.tableId(), stageKey.stageId(), randomUUID());
    }
}
