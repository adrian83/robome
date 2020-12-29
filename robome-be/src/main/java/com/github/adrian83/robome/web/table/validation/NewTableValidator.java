package com.github.adrian83.robome.web.table.validation;

import static com.github.adrian83.robome.web.common.Validation.check;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.adrian83.robome.domain.common.Validator;
import com.github.adrian83.robome.web.common.ValidationError;
import com.github.adrian83.robome.web.table.model.NewTable;
import com.google.common.base.Strings;

public class NewTableValidator { // implements Validator<NewTable> {

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

  // @Override
  public List<ValidationError> validate(NewTable form) {
    return Stream.of(
            check(form.getTitle(), EMPTY_TITLE, Strings::isNullOrEmpty),
            check(form.getDescription(), EMPTY_DESCRIPTION, Strings::isNullOrEmpty))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }
}
