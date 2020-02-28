package com.github.adrian83.robome.common.web;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.github.adrian83.robome.domain.common.Validator;

public final class Validation {

  private Validation() {}

  public static <T> T validate(T form, Validator<T> validator) {
    List<ValidationError> errors = validator.validate(form);
    if (!errors.isEmpty()) {
      throw new ValidationException(errors);
    }
    return form;
  }

  public static <T> Optional<ValidationError> check(
      T arg, ValidationError error, Function<T, Boolean> isInvalid) {
    return isInvalid.apply(arg) ? Optional.ofNullable(error) : Optional.empty();
  }
}
