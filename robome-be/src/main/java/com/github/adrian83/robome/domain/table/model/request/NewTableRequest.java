package com.github.adrian83.robome.domain.table.model.request;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString
@EqualsAndHashCode
public class NewTableRequest {
  private String title;
  private String description;
  private UUID userId;
}
