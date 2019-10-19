package com.github.adrian83.robome.web.stage.validation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.adrian83.robome.common.web.Validation;
import com.github.adrian83.robome.common.web.ValidationError;
import com.github.adrian83.robome.domain.common.Validator;
import com.github.adrian83.robome.domain.stage.model.NewStage;
import com.google.common.base.Strings;

public class NewStageValidator implements Validator<NewStage> {

  private static final String NAME_LABEL = "name";
  private static final String EMPTY_NAME_KEY = "stage.create.name.empty";
  private static final String EMPTY_NAME_MSG = "Stage name cannot be empty";

  private static final ValidationError EMPTY_NAME =
      new ValidationError(NAME_LABEL, EMPTY_NAME_KEY, EMPTY_NAME_MSG);

  @Override
  public List<ValidationError> validate(NewStage form) {
    return Stream.of(Validation.check(form.getName(), EMPTY_NAME, Strings::isNullOrEmpty))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }
}
