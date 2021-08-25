package com.github.adrian83.robome.domain.stage.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.github.adrian83.robome.domain.activity.model.Activity;

public record Stage(
    StageKey key,
    UUID userId,
    String title,
    StageState state,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt,
    List<Activity> activities) {

  public Stage copyWithActivities(List<Activity> activities) {
    return new Stage(key, userId, title, state, createdAt, modifiedAt, activities);
  }
}
