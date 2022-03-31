package com.github.adrian83.robome.domain.stage.model;

import java.time.LocalDateTime;
import java.util.List;

import com.github.adrian83.robome.domain.activity.model.Activity;

public record Stage(
    StageKey key,
    String title,
    StageState state,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt,
    List<Activity> activities) {

  public Stage copyWithActivities(List<Activity> activities) {
    return new Stage(key, title, state, createdAt, modifiedAt, activities);
  }
}
