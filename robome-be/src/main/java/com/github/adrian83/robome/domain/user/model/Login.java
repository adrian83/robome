package com.github.adrian83.robome.domain.user.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.adrian83.robome.common.web.Validable;
import com.github.adrian83.robome.common.web.Validation;
import com.github.adrian83.robome.common.web.ValidationError;
import com.google.common.base.Strings;

public class Login implements Validable {

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

  private String email;
  private String password;

  @JsonCreator
  public Login(
      @JsonProperty(EMAIL_LABEL) String email, @JsonProperty(PASSWORD_LABEL) String password) {
    super();
    this.email = email;
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  @Override
  public List<ValidationError> validate() {
    return Stream.of(
            Validation.check(getEmail(), EMPTY_EMAIL, Strings::isNullOrEmpty),
            Validation.check(getPassword(), EMPTY_PASSWORD, Strings::isNullOrEmpty))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }
}
