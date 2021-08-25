package com.github.adrian83.robome.web.activity.model;

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
public record NewActivity(@JsonProperty(NewActivity.NAME_LABEL) String name) implements Validator {

  private static final String NAME_LABEL = "name";
  private static final String EMPTY_NAME_KEY = "activity.create.name.empty";
  private static final String EMPTY_NAME_MSG = "Activity name cannot be empty";

  private static final ValidationError EMPTY_NAME =
      new ValidationError(NAME_LABEL, EMPTY_NAME_KEY, EMPTY_NAME_MSG);

  @Override
  public List<ValidationError> validate() {
    return Stream.of(check(name, EMPTY_NAME, Strings::isNullOrEmpty))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }
}
