package com.github.adrian83.robome.domain.table.model;

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

public class NewTable implements Validable {

  private static final String TITLE_LABEL = "title";
  private static final String EMPTY_TITLE_KEY = "table.create.title.empty";
  private static final String EMPTY_TITLE_MSG = "Table title cannot be empty";

  private static final String DESC_LABEL = "description";
  private static final String EMPTY_DESC_KEY = "table.create.description.empty";
  private static final String EMPTY_DESC_MSG = "Table description cannot be empty";

  private static final ValidationError EMPTY_TITLE =
      new ValidationError(TITLE_LABEL, EMPTY_TITLE_KEY, EMPTY_TITLE_MSG);
  private static final ValidationError EMPTY_DESCRIPTION =
      new ValidationError(DESC_LABEL, EMPTY_DESC_KEY, EMPTY_DESC_MSG);

  final String title;
  final String description;

  @JsonCreator
  public NewTable(
      @JsonProperty(TITLE_LABEL) String title, @JsonProperty(DESC_LABEL) String description) {
    this.title = title;
    this.description = description;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public List<ValidationError> validate() {
    return Stream.of(
            Validation.check(getTitle(), EMPTY_TITLE, Strings::isNullOrEmpty),
            Validation.check(getDescription(), EMPTY_DESCRIPTION, Strings::isNullOrEmpty))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return "NewTable [title=" + title + ", description=" + description + "]";
  }
}
