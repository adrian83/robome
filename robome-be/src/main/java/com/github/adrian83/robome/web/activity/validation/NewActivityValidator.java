package com.github.adrian83.robome.web.activity.validation;

import static com.github.adrian83.robome.web.common.Validation.check;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.adrian83.robome.domain.common.Validator;
import com.github.adrian83.robome.web.activity.model.NewActivity;
import com.github.adrian83.robome.web.common.ValidationError;
import com.google.common.base.Strings;

public class NewActivityValidator { // implements Validator<NewActivity> {

  private static final String NAME_LABEL = "name";
  private static final String EMPTY_NAME_KEY = "activity.create.name.empty";
  private static final String EMPTY_NAME_MSG = "Activity name cannot be empty";

  private static final ValidationError EMPTY_NAME =
      new ValidationError(NAME_LABEL, EMPTY_NAME_KEY, EMPTY_NAME_MSG);

  //@Override
  public List<ValidationError> validate(NewActivity form) {
    return Stream.of(check(form.getName(), EMPTY_NAME, Strings::isNullOrEmpty))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }
}
