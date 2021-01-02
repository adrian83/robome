package com.github.adrian83.robome.domain.table.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString
@EqualsAndHashCode
public class TableEntity {
  private TableKey key;
  private UUID userId;
  private String title;
  private String description;
  private TableState state;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;
}
