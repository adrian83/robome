package com.github.adrian83.robome.domain.stage.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString
@EqualsAndHashCode
public class StageEntity {
  private StageKey key;
  private UUID userId;
  private String title;
  private StageState state;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;
}
