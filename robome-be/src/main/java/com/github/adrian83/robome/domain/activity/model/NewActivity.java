package com.github.adrian83.robome.domain.activity.model;

import java.util.ArrayList;
import java.util.List;

import com.github.adrian83.robome.common.web.Validable;
import com.github.adrian83.robome.common.web.ValidationError;
import com.google.common.base.Strings;
import com.typesafe.config.Config;

public class NewActivity implements Validable {

  private static final String NAME_LABEL = "name";
  private static final String EMPTY_NAME_KEY = "activity.create.name.empty";
  private static final String EMPTY_NAME_MSG = "Activity name cannot be empty";

  private String name;

  public NewActivity(String name) {
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
      errors.add(new ValidationError(NAME_LABEL, EMPTY_NAME_KEY, EMPTY_NAME_MSG));
    }

    return errors;
  }
}
