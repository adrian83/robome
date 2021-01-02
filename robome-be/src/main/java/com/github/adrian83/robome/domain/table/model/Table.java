package com.github.adrian83.robome.domain.table.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.github.adrian83.robome.domain.stage.model.Stage;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString
@EqualsAndHashCode
public class Table {
  private TableKey key;
  private UUID userId;
  private String title;
  private String description;
  private TableState state;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;
  private List<Stage> stages;

  public Table copyWithStages(List<Stage> stages) {
    return Table.builder()
        .key(key)
        .userId(userId)
        .title(title)
        .description(description)
        .state(state)
        .modifiedAt(modifiedAt)
        .createdAt(createdAt)
        .stages(stages)
        .build();
  }
}
