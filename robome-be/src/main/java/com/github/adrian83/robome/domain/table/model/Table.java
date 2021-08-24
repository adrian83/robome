package com.github.adrian83.robome.domain.table.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.github.adrian83.robome.domain.stage.model.Stage;

public record Table(
    TableKey key,
    UUID userId,
    String title,
    String description,
    TableState state,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt,
    List<Stage> stages) {

  public Table copyWithStages(List<Stage> stages) {
    return new Table(key, userId, title, description, state, createdAt, modifiedAt, stages);
  }
}
