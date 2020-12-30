package com.github.adrian83.robome.domain.activity.model.request;

import java.util.UUID;

import com.github.adrian83.robome.domain.activity.model.ActivityKey;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString
@EqualsAndHashCode
public class UpdateActivityRequest {
  private String name;
  private UUID userId;
  private ActivityKey activityKey;
}
