package com.github.adrian83.robome.domain.table.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.github.adrian83.robome.common.time.TimeUtils;

public class Table {

  private TableId id;
  private UUID userId;
  private String title;
  private String description;
  private TableState state;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;

  public Table(UUID userId, String title, String description) {
    this(
        new TableId(),
        userId,
        title,
        description,
        TableState.ACTIVE,
        TimeUtils.utcNow(),
        TimeUtils.utcNow());
  }

  public Table(
      TableId id,
      UUID userId,
      String title,
      String description,
      TableState state,
      LocalDateTime createdAt,
      LocalDateTime modifiedAt) {
    super();
    this.id = id;
    this.userId = userId;
    this.title = title;
    this.description = description;
    this.state = state;
    this.createdAt = createdAt;
    this.modifiedAt = modifiedAt;
  }

  public static Table newTable(TableId id, UUID userId, String title, String description) {
    return new Table(
        id, userId, title, description, TableState.ACTIVE, TimeUtils.utcNow(), TimeUtils.utcNow());
  }

  public static Table updatedTable(
      TableId id,
      UUID userId,
      String title,
      String description,
      TableState state,
      LocalDateTime createdAt) {
    return new Table(id, userId, title, description, state, createdAt, TimeUtils.utcNow());
  }

  public TableId getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public TableState getState() {
    return state;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getModifiedAt() {
    return modifiedAt;
  }
}
