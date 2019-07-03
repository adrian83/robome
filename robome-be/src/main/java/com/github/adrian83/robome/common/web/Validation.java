package com.github.adrian83.robome.common.web;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class Validation {

  private Validation() {}

  public static <T extends Validable> T validate(T form) {
    List<ValidationError> errors = form.validate();
    if (!errors.isEmpty()) {
      throw new ValidationException(errors);
    }
    return form;
  }

  public static <T> Optional<ValidationError> check(
      T arg, ValidationError error, Function<T, Boolean> isValid) {
    return isValid.apply(arg) ? Optional.empty() : Optional.ofNullable(error);
  }
}
