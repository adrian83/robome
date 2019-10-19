package com.github.adrian83.robome.web.auth.validation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.adrian83.robome.common.web.Validation;
import com.github.adrian83.robome.common.web.ValidationError;
import com.github.adrian83.robome.domain.common.Validator;
import com.github.adrian83.robome.web.auth.model.Login;
import com.google.common.base.Strings;

public class LoginValidator implements Validator<Login> {

  private static final String EMAIL_LABEL = "email";
  private static final String EMPTY_EMAIL_KEY = "user.login.email.empty";
  private static final String EMPTY_EMAIL_MSG = "Email cannot be empty";

  private static final String PASSWORD_LABEL = "password";
  private static final String EMPTY_PASSWORD_KEY = "user.login.password.empty";
  private static final String EMPTY_PASSWORD_MSG = "Password cannot be empty";

  private static final ValidationError EMPTY_EMAIL =
      new ValidationError(EMAIL_LABEL, EMPTY_EMAIL_KEY, EMPTY_EMAIL_MSG);
  private static final ValidationError EMPTY_PASSWORD =
      new ValidationError(PASSWORD_LABEL, EMPTY_PASSWORD_KEY, EMPTY_PASSWORD_MSG);

  @Override
  public List<ValidationError> validate(Login form) {
    return Stream.of(
            Validation.check(form.getEmail(), EMPTY_EMAIL, Strings::isNullOrEmpty),
            Validation.check(form.getPassword(), EMPTY_PASSWORD, Strings::isNullOrEmpty))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }
}
