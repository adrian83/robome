package com.github.adrian83.robome.domain.activity.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Builder
@Data
@ToString
public class Activity {

  private ActivityKey key;
  private UUID userId;
  private String name;
  private ActivityState state;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;
}
