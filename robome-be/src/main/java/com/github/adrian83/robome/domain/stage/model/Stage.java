package com.github.adrian83.robome.domain.stage.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.github.adrian83.robome.domain.activity.model.Activity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString
@EqualsAndHashCode
public class Stage {
  private StageKey key;
  private UUID userId;
  private String title;
  private StageState state;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;
  private List<Activity> activities;

  public Stage copyWithActivities(List<Activity> activities) {
    return Stage.builder()
        .key(key)
        .userId(userId)
        .title(title)
        .state(state)
        .createdAt(createdAt)
        .modifiedAt(modifiedAt)
        .activities(activities)
        .build();
  }
}
