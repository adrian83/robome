package com.github.adrian83.robome.web.stage.validation;

import com.github.adrian83.robome.domain.common.Validator;
import com.github.adrian83.robome.domain.stage.model.NewStage;
import com.github.adrian83.robome.web.common.ValidationError;
import com.google.common.base.Strings;

import static com.github.adrian83.robome.web.common.Validation.check;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NewStageValidator implements Validator<NewStage> {

  private static final String TITLE_LABEL = "title";
  private static final String EMPTY_TITLE_KEY = "stage.create.title.empty";
  private static final String EMPTY_TITLE_MSG = "Stage title cannot be empty";

  private static final ValidationError EMPTY_TITLE =
      new ValidationError(TITLE_LABEL, EMPTY_TITLE_KEY, EMPTY_TITLE_MSG);

  @Override
  public List<ValidationError> validate(NewStage form) {
    return Stream.of(check(form.getTitle(), EMPTY_TITLE, Strings::isNullOrEmpty))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }
}
