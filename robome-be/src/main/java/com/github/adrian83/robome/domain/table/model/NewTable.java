package com.github.adrian83.robome.domain.table.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.adrian83.robome.common.web.Validable;
import com.github.adrian83.robome.common.web.ValidationError;
import com.google.common.base.Strings;
import com.typesafe.config.Config;

public class NewTable implements Validable {

  final String title;
  final String description;

  @JsonCreator
  public NewTable(
      @JsonProperty("title") String title, @JsonProperty("description") String description) {
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
  public List<ValidationError> validate(Config config) {
    List<ValidationError> errors = new ArrayList<>();

    if (Strings.isNullOrEmpty(getTitle())) {
      ValidationError error =
          new ValidationError("title", "table.create.title.empty", "Table title cannot be empty");
      errors.add(error);
    }

    if (Strings.isNullOrEmpty(getDescription())) {
      ValidationError error =
          new ValidationError(
              "description", "table.create.description.empty", "Table description cannot be empty");
      errors.add(error);
    }

    return errors;
  }
}
