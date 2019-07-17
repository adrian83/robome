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

public class Register implements Validable {

  private static final String EMAIL_LABEL = "email";
  private static final String EMPTY_EMAIL_KEY = "user.register.email.empty";
  private static final String EMPTY_EMAIL_MSG = "Email cannot be empty";

  private static final String PASSWORD_1_LABEL = "password";
  private static final String EMPTY_PASSWORD_1_KEY = "user.register.password.empty";
  private static final String EMPTY_PASSWORD_1_MSG = "Password cannot be empty";

  private static final String PASSWORD_2_LABEL = "repeatedPassword";
  private static final String DIFFERENT_PASSWORDS_KEY = "user.register.passwords.different";
  private static final String DIFFERENT_PASSWORDS_MSG = "Repeated password cannot be different";

  private static final ValidationError EMPTY_EMAIL =
      new ValidationError(EMAIL_LABEL, EMPTY_EMAIL_KEY, EMPTY_EMAIL_MSG);
  private static final ValidationError EMPTY_PASSWORD_1 =
      new ValidationError(PASSWORD_1_LABEL, EMPTY_PASSWORD_1_KEY, EMPTY_PASSWORD_1_MSG);
  private static final ValidationError DIFFERENT_PASSWORDS =
      new ValidationError(PASSWORD_2_LABEL, DIFFERENT_PASSWORDS_KEY, DIFFERENT_PASSWORDS_MSG);

  private String email;
  private String password;
  private String repeatedPassword;

  @JsonCreator
  public Register(
      @JsonProperty(EMAIL_LABEL) String email,
      @JsonProperty(PASSWORD_1_LABEL) String password,
      @JsonProperty(PASSWORD_2_LABEL) String repeatedPassword) {
    super();
    this.email = email;
    this.password = password;
    this.repeatedPassword = repeatedPassword;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public String getRepeatedPassword() {
    return repeatedPassword;
  }

  @Override
  public List<ValidationError> validate() {
    return Stream.of(
            Validation.check(getEmail(), EMPTY_EMAIL, Strings::isNullOrEmpty),
            Validation.check(getPassword(), EMPTY_PASSWORD_1, Strings::isNullOrEmpty),
            Validation.check(
                getRepeatedPassword(),
                DIFFERENT_PASSWORDS,
                (String pass2) -> !pass2.equals(password)))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }
}
