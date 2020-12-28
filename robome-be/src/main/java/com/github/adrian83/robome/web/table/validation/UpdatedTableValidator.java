package com.github.adrian83.robome.web.table.validation;

import static com.github.adrian83.robome.web.common.Validation.check;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.adrian83.robome.domain.common.Validator;
import com.github.adrian83.robome.web.common.ValidationError;
import com.github.adrian83.robome.web.table.model.UpdatedTable;
import com.google.common.base.Strings;

public class UpdatedTableValidator implements Validator<UpdatedTable> {

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

  @Override
  public List<ValidationError> validate(UpdatedTable form) {
    return Stream.of(
            check(form.getTitle(), EMPTY_TITLE, Strings::isNullOrEmpty),
            check(form.getDescription(), EMPTY_DESCRIPTION, Strings::isNullOrEmpty))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }
}
