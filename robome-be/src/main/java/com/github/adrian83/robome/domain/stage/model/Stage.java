package com.github.adrian83.robome.domain.stage.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.github.adrian83.robome.common.Time;
import com.github.adrian83.robome.domain.activity.model.Activity;
import com.github.adrian83.robome.domain.table.model.TableKey;

public class Stage {

  private StageKey key;
  private UUID userId;
  private String title;
  private StageState state;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;
  private List<Activity> activities;

  public Stage(
      StageKey key,
      UUID userId,
      String title,
      StageState state,
      LocalDateTime createdAt,
      LocalDateTime modifiedAt) {
    super();
    this.key = key;
    this.userId = userId;
    this.title = title;
    this.state = state;
    this.createdAt = createdAt;
    this.modifiedAt = modifiedAt;
    this.activities = new ArrayList<Activity>();
  }

  public Stage(TableKey tableKey, UUID userId, String title) {
    this(new StageKey(tableKey), userId, title, StageState.ACTIVE, Time.utcNow(), Time.utcNow());
  }

  public Stage withActivities(List<Activity> activities) {
    Stage stage =
        new Stage(this.key, this.userId, this.title, this.state, this.createdAt, this.modifiedAt);
    stage.activities = activities;
    return stage;
  }

  public StageKey getKey() {
    return key;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getTitle() {
    return title;
  }

  public StageState getState() {
    return state;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getModifiedAt() {
    return modifiedAt;
  }

  public List<Activity> getActivities() {
    return activities;
  }
}
