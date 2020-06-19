package com.github.adrian83.robome.domain.table.model;

import static java.util.UUID.randomUUID;

import java.util.UUID;

public class TableKey {

  private UUID tableId;

  public TableKey(UUID tableId) {
    super();
    this.tableId = tableId;
  }

  public TableKey() {
    this(randomUUID());
  }

  public static TableKey fromString(String uuidStr) {
    return new TableKey(UUID.fromString(uuidStr));
  }

  public UUID getTableId() {
    return tableId;
  }

  @Override
  public String toString() {
    return "TableKey [tableId=" + tableId + "]";
  }
}
