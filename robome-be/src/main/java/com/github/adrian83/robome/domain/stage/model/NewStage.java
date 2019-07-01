package com.github.adrian83.robome.domain.stage.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.adrian83.robome.common.web.Validable;
import com.github.adrian83.robome.common.web.ValidationError;
import com.google.common.base.Strings;
import com.typesafe.config.Config;

public class NewStage implements Validable {

  private String name;

  @JsonCreator
  public NewStage(@JsonProperty("name") String name) {
    super();
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public List<ValidationError> validate(Config config) {
    List<ValidationError> errors = new ArrayList<>();

    if (Strings.isNullOrEmpty(getName())) {
      ValidationError error =
          new ValidationError("name", "stage.create.name.empty", "Stage name cannot be empty");
      errors.add(error);
    }

    return errors;
  }
}
