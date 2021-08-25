package com.github.adrian83.robome.domain.stage.model;

import static java.util.UUID.randomUUID;
import static java.util.UUID.fromString;

import java.util.UUID;

import com.github.adrian83.robome.domain.table.model.TableKey;

public record StageKey(UUID tableId, UUID stageId) {

  public static StageKey parse(String tableIdStr, String stageIdStr) {
    return new StageKey(fromString(tableIdStr), fromString(stageIdStr));
  }

  public static StageKey randomWithTableKey(TableKey tableKey) {
    return new StageKey(tableKey.tableId(), randomUUID());
  }
}
