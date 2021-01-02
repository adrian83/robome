package com.github.adrian83.robome.web.stage.model;

import static com.github.adrian83.robome.common.validation.Validation.check;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.adrian83.robome.common.validation.ValidationError;
import com.github.adrian83.robome.common.validation.Validator;
import com.google.common.base.Strings;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString
@EqualsAndHashCode
public class UpdateStage implements Validator {
  private static final String TITLE_LABEL = "title";
  private static final String EMPTY_TITLE_KEY = "stage.update.title.empty";
  private static final String EMPTY_TITLE_MSG = "Stage title cannot be empty";

  private static final ValidationError EMPTY_NAME =
      ValidationError.builder()
          .field(TITLE_LABEL)
          .messageCode(EMPTY_TITLE_KEY)
          .message(EMPTY_TITLE_MSG)
          .build();

  private String title;

  @JsonCreator
  public UpdateStage(@JsonProperty(TITLE_LABEL) String title) {
    super();
    this.title = title;
  }

  @Override
  public List<ValidationError> validate() {
    return Stream.of(check(title, EMPTY_NAME, Strings::isNullOrEmpty))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }
}
