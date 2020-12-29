package com.github.adrian83.robome.domain.stage.model.request;

import java.util.UUID;

import com.github.adrian83.robome.domain.table.model.TableKey;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString
@EqualsAndHashCode
public class NewStageRequest {
  private String title;
  private UUID userId;
  private TableKey tableKey;
}
