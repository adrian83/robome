package com.github.adrian83.robome.domain.table.model;

import static java.util.UUID.randomUUID;

import java.util.UUID;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@ToString
public class TableKey {

  private UUID tableId;
  
  public static TableKey random() {
	  return TableKey.builder().tableId(randomUUID()).build();
  }

  public static TableKey parse(String uuidStr) {
    return TableKey.builder().tableId(UUID.fromString(uuidStr)).build();
  }
}
