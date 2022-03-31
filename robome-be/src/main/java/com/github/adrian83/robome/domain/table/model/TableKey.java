package com.github.adrian83.robome.domain.table.model;

import static java.util.UUID.randomUUID;

import java.util.UUID;

public record TableKey(UUID userId, UUID tableId) {

  public static TableKey random(UUID userId) {
    return new TableKey(userId, randomUUID());
  }

  public static TableKey parse(UUID userId, String uuidStr) {
    return new TableKey(userId, UUID.fromString(uuidStr));
  }
}
