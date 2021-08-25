package com.github.adrian83.robome.domain.table.model;

import static java.util.UUID.randomUUID;

import java.util.UUID;

public record TableKey(UUID tableId) {

  public static TableKey random() {
    return new TableKey(randomUUID());
  }

  public static TableKey parse(String uuidStr) {
    return new TableKey(UUID.fromString(uuidStr));
  }
}
