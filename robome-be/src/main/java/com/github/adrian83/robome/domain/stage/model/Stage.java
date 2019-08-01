package com.github.adrian83.robome.domain.stage.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.github.adrian83.robome.common.time.TimeUtils;
import com.github.adrian83.robome.domain.table.model.TableKey;

public class Stage {

  private StageKey key;
  private UUID userId;
  private String title;
  private StageState state;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;

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
  }

  public Stage(TableKey tableKey, UUID userId, String title) {
    this(new StageKey(tableKey), userId, title, StageState.ACTIVE, TimeUtils.utcNow(), TimeUtils.utcNow());
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

}
