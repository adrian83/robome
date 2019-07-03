package com.github.adrian83.robome.domain.activity.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.adrian83.robome.common.web.Validable;
import com.github.adrian83.robome.common.web.Validation;
import com.github.adrian83.robome.common.web.ValidationError;
import com.google.common.base.Strings;

public class NewActivity implements Validable {

  private static final String NAME_LABEL = "name";
  private static final String EMPTY_NAME_KEY = "activity.create.name.empty";
  private static final String EMPTY_NAME_MSG = "Activity name cannot be empty";

  private static final ValidationError EMPTY_NAME =
      new ValidationError(NAME_LABEL, EMPTY_NAME_KEY, EMPTY_NAME_MSG);

  private String name;

  @JsonCreator
  public NewActivity(@JsonProperty(NAME_LABEL) String name) {
    super();
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public List<ValidationError> validate() {
    return Stream.of(Validation.check(getName(), EMPTY_NAME, Strings::isNullOrEmpty))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }
}
