package com.github.adrian83.robome.web.auth.model;

import static com.github.adrian83.robome.common.validation.Validation.check;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.adrian83.robome.common.validation.ValidationError;
import com.github.adrian83.robome.common.validation.Validator;
import com.google.common.base.Strings;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonSerialize
public record Login(
    @JsonProperty(EMAIL_LABEL) String email, @JsonProperty(PASSWORD_LABEL) String password)
    implements Validator {

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
  public List<ValidationError> validate() {
    return Stream.of(
            check(email, EMPTY_EMAIL, Strings::isNullOrEmpty),
            check(password, EMPTY_PASSWORD, Strings::isNullOrEmpty))
    	.flatMap(Optional::stream)
        .collect(Collectors.toList());
  }
}
