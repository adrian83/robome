package com.github.adrian83.robome.domain.activity.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.github.adrian83.robome.common.Time;
import com.github.adrian83.robome.domain.stage.model.StageKey;

public class ActivityEntity {

  private ActivityKey key;
  private UUID userId;
  private String name;
  private ActivityState state;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;

  public ActivityEntity(
      ActivityKey key,
      UUID userId,
      String name,
      ActivityState state,
      LocalDateTime createdAt,
      LocalDateTime modifiedAt) {
    super();
    this.key = key;
    this.userId = userId;
    this.name = name;
    this.state = state;
    this.createdAt = createdAt;
    this.modifiedAt = modifiedAt;
  }

  public ActivityEntity(StageKey stageKey, UUID userId, String name) {
    this(
        new ActivityKey(stageKey),
        userId,
        name,
        ActivityState.ACTIVE,
        Time.utcNow(),
        Time.utcNow());
  }

  public static ActivityEntity newActivity(ActivityKey activityKey, UUID userId, String name) {
    return new ActivityEntity(
        activityKey, userId, name, ActivityState.ACTIVE, Time.utcNow(), Time.utcNow());
  }

  public ActivityKey getKey() {
    return key;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getName() {
    return name;
  }

  public ActivityState getState() {
    return state;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getModifiedAt() {
    return modifiedAt;
  }
}
