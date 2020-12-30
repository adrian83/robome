package com.github.adrian83.robome.web.table.model;

import static com.github.adrian83.robome.web.common.Validation.check;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.adrian83.robome.domain.common.Validator;
import com.github.adrian83.robome.web.common.ValidationError;
import com.google.common.base.Strings;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString
@EqualsAndHashCode
public class UpdateTable implements Validator {

  private static final String TITLE_LABEL = "title";
  private static final String EMPTY_TITLE_KEY = "table.update.title.empty";
  private static final String EMPTY_TITLE_MSG = "New table title cannot be empty";

  private static final String DESC_LABEL = "description";
  private static final String EMPTY_DESC_KEY = "table.update.description.empty";
  private static final String EMPTY_DESC_MSG = "New table description cannot be empty";

  private static final ValidationError EMPTY_TITLE =
      new ValidationError(TITLE_LABEL, EMPTY_TITLE_KEY, EMPTY_TITLE_MSG);
  private static final ValidationError EMPTY_DESCRIPTION =
      new ValidationError(DESC_LABEL, EMPTY_DESC_KEY, EMPTY_DESC_MSG);

  final String title;
  final String description;

  @JsonCreator
  public UpdateTable(
      @JsonProperty(TITLE_LABEL) String title, @JsonProperty(DESC_LABEL) String description) {
    this.title = title;
    this.description = description;
  }

  @Override
  public List<ValidationError> validate() {
    return Stream.of(
            check(title, EMPTY_TITLE, Strings::isNullOrEmpty),
            check(description, EMPTY_DESCRIPTION, Strings::isNullOrEmpty))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }
}
