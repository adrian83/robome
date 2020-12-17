package com.github.adrian83.robome.domain.stage.model;

import static java.util.UUID.randomUUID;
import static java.util.UUID.fromString;

import java.util.UUID;

import com.github.adrian83.robome.domain.table.model.TableKey;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class StageKey extends TableKey {

  private UUID stageId;

  public static StageKey parse(String tableIdStr, String stageIdStr) {
    return StageKey.builder()
        .tableId(fromString(tableIdStr))
        .stageId(fromString(stageIdStr))
        .build();
  }

  public static StageKey randomWithTableKey(TableKey tableKey) {
    return StageKey.builder().tableId(tableKey.getTableId()).stageId(randomUUID()).build();
  }
}
