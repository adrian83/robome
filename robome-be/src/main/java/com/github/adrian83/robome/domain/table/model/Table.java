package com.github.adrian83.robome.domain.table.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.github.adrian83.robome.domain.stage.model.Stage;

public class Table {

  private TableKey key;
  private UUID userId;
  private String title;
  private String description;
  private TableState state;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;
  private List<Stage> stages;

  public Table(
      TableKey key,
      UUID userId,
      String title,
      String description,
      TableState state,
      LocalDateTime createdAt,
      LocalDateTime modifiedAt) {
    super();
    this.key = key;
    this.userId = userId;
    this.title = title;
    this.description = description;
    this.state = state;
    this.createdAt = createdAt;
    this.modifiedAt = modifiedAt;
    this.stages = new ArrayList<Stage>();
  }

  public Table withStages(List<Stage> stages) {
    Table table =
        new Table(
            this.key,
            this.userId,
            this.title,
            this.description,
            this.state,
            this.createdAt,
            this.modifiedAt);
    table.stages = stages;
    return table;
  }

  public TableKey getKey() {
    return key;
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

  public List<Stage> getStages() {
    return stages;
  }
}
