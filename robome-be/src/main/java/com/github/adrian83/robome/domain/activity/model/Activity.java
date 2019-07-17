package com.github.adrian83.robome.domain.activity.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.github.adrian83.robome.common.time.TimeUtils;
import com.github.adrian83.robome.domain.common.Ownerable;

public class Activity implements Ownerable {

  private ActivityKey key;
  private UUID userId;
  private String name;
  private ActivityState state;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;

  public Activity(
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

  public Activity(UUID userId, String name) {
    this(
        new ActivityKey(),
        userId,
        name,
        ActivityState.ACTIVE,
        TimeUtils.utcNow(),
        TimeUtils.utcNow());
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

  @Override
  public UUID getOwner() {
    return getUserId();
  }
}