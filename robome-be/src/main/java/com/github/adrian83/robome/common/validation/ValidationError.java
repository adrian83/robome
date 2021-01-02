package com.github.adrian83.robome.common.validation;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString
@EqualsAndHashCode
public class ValidationError {
  private String field;
  private String messageCode;
  private String message;
}
