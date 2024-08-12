package com.github.adrian83.robome.domain.stage.model;

import static java.util.UUID.randomUUID;
import static java.util.UUID.fromString;

import java.util.UUID;

import com.github.adrian83.robome.domain.table.model.TableKey;

public record StageKey(UUID userId, UUID tableId, UUID stageId) {

    public static StageKey create(UUID userId, String tableIdStr, String stageIdStr) {
        return new StageKey(userId, fromString(tableIdStr), fromString(stageIdStr));
    }

    public static StageKey basedOnTableKey(TableKey tableKey) {
        return new StageKey(tableKey.userId(), tableKey.tableId(), randomUUID());
    }
}
