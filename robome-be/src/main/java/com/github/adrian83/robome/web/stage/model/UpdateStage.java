package com.github.adrian83.robome.web.stage.model;

import static com.github.adrian83.robome.common.validation.Validation.check;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.adrian83.robome.common.validation.ValidationError;
import com.github.adrian83.robome.common.validation.Validator;
import com.google.common.base.Strings;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonSerialize
public record UpdateStage(@JsonProperty(TITLE_LABEL) String title) implements Validator {
  private static final String TITLE_LABEL = "title";
  private static final String EMPTY_TITLE_KEY = "stage.update.title.empty";
  private static final String EMPTY_TITLE_MSG = "Stage title cannot be empty";

  private static final ValidationError EMPTY_NAME =
      new ValidationError(TITLE_LABEL, EMPTY_TITLE_KEY, EMPTY_TITLE_MSG);

  @Override
  public List<ValidationError> validate() {
    return Stream.of(check(title, EMPTY_NAME, Strings::isNullOrEmpty))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }
}
