package com.github.adrian83.robome.web.stage.validation;

import static com.github.adrian83.robome.web.common.Validation.check;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.adrian83.robome.domain.common.Validator;
import com.github.adrian83.robome.web.common.ValidationError;
import com.github.adrian83.robome.web.stage.model.UpdateStage;
import com.google.common.base.Strings;

public class UpdateStageValidator { // implements Validator<UpdateStage> {

  private static final String TITLE_LABEL = "title";
  private static final String EMPTY_TITLE_KEY = "stage.update.title.empty";
  private static final String EMPTY_TITLE_MSG = "Stage title cannot be empty";

  private static final ValidationError EMPTY_NAME =
      new ValidationError(TITLE_LABEL, EMPTY_TITLE_KEY, EMPTY_TITLE_MSG);

  // @Override
  public List<ValidationError> validate(UpdateStage form) {
    return Stream.of(check(form.getTitle(), EMPTY_NAME, Strings::isNullOrEmpty))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }
}
