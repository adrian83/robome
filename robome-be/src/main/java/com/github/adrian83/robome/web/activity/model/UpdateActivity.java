package com.github.adrian83.robome.web.activity.model;

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
public class UpdateActivity implements Validator {

  private static final String NAME_LABEL = "name";
  private static final String EMPTY_NAME_KEY = "activity.update.name.empty";
  private static final String EMPTY_NAME_MSG = "Activity name cannot be empty";

  private static final ValidationError EMPTY_NAME =
      ValidationError.builder()
          .field(NAME_LABEL)
          .messageCode(EMPTY_NAME_KEY)
          .message(EMPTY_NAME_MSG)
          .build();

  private String name;

  @JsonCreator
  public UpdateActivity(@JsonProperty(NAME_LABEL) String name) {
    super();
    this.name = name;
  }

  @Override
  public List<ValidationError> validate() {
    return Stream.of(check(name, EMPTY_NAME, Strings::isNullOrEmpty))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }
}
