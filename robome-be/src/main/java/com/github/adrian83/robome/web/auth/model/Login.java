package com.github.adrian83.robome.web.auth.model;

import static com.github.adrian83.robome.common.validation.Validation.check;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.adrian83.robome.common.validation.ValidationError;
import com.github.adrian83.robome.common.validation.Validator;
import com.google.common.base.Strings;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString(exclude = {"password"})
@EqualsAndHashCode
public class Login implements Validator {

  private static final String EMAIL_LABEL = "email";
  private static final String EMPTY_EMAIL_KEY = "user.login.email.empty";
  private static final String EMPTY_EMAIL_MSG = "Email cannot be empty";

  private static final String PASSWORD_LABEL = "password";
  private static final String EMPTY_PASSWORD_KEY = "user.login.password.empty";
  private static final String EMPTY_PASSWORD_MSG = "Password cannot be empty";

  private static final ValidationError EMPTY_EMAIL =
      ValidationError.builder()
          .field(EMAIL_LABEL)
          .messageCode(EMPTY_EMAIL_KEY)
          .message(EMPTY_EMAIL_MSG)
          .build();

  private static final ValidationError EMPTY_PASSWORD =
      ValidationError.builder()
          .field(PASSWORD_LABEL)
          .messageCode(EMPTY_PASSWORD_KEY)
          .message(EMPTY_PASSWORD_MSG)
          .build();

  private String email;
  private String password;

  @JsonCreator
  public Login(
      @JsonProperty(EMAIL_LABEL) String email, @JsonProperty(PASSWORD_LABEL) String password) {
    super();
    this.email = email;
    this.password = password;
  }

  @Override
  public List<ValidationError> validate() {
    return Stream.of(
            check(email, EMPTY_EMAIL, Strings::isNullOrEmpty),
            check(password, EMPTY_PASSWORD, Strings::isNullOrEmpty))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }
}
