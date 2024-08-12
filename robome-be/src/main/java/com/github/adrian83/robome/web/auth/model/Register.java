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
public record Register(@JsonProperty(EMAIL_LABEL)
        String email, @JsonProperty(PASSWORD_1_LABEL)
        String password,
        @JsonProperty(PASSWORD_2_LABEL)
        String repeatedPassword) implements Validator {

    private static final String EMAIL_LABEL = "email";
    private static final String EMPTY_EMAIL_KEY = "user.register.email.empty";
    private static final String EMPTY_EMAIL_MSG = "Email cannot be empty";

    private static final String PASSWORD_1_LABEL = "password";
    private static final String EMPTY_PASSWORD_1_KEY = "user.register.password.empty";
    private static final String EMPTY_PASSWORD_1_MSG = "Password cannot be empty";

    private static final String PASSWORD_2_LABEL = "repeatedPassword";
    private static final String DIFFERENT_PASSWORDS_KEY = "user.register.passwords.different";
    private static final String DIFFERENT_PASSWORDS_MSG = "Repeated password cannot be different";

    private static final ValidationError EMPTY_EMAIL = new ValidationError(EMAIL_LABEL, EMPTY_EMAIL_KEY, EMPTY_EMAIL_MSG);
    private static final ValidationError EMPTY_PASSWORD_1 = new ValidationError(PASSWORD_1_LABEL, EMPTY_PASSWORD_1_KEY, EMPTY_PASSWORD_1_MSG);
    private static final ValidationError DIFFERENT_PASSWORDS = new ValidationError(PASSWORD_2_LABEL, DIFFERENT_PASSWORDS_KEY, DIFFERENT_PASSWORDS_MSG);

    @Override
    public List<ValidationError> validate() {
        return Stream
                .of(
                        check(email, EMPTY_EMAIL, Strings::isNullOrEmpty),
                        check(password, EMPTY_PASSWORD_1, Strings::isNullOrEmpty),
                        check(repeatedPassword, DIFFERENT_PASSWORDS, (String pass2) -> !pass2.equals(password))
                )
                .flatMap(Optional::stream)
                .toList();
    }
}
