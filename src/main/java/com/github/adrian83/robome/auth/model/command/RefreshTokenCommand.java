package com.github.adrian83.robome.auth.model.command;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import static com.github.adrian83.robome.common.validation.Validation.check;
import com.github.adrian83.robome.common.validation.ValidationError;
import com.github.adrian83.robome.common.validation.Validator;
import com.google.common.base.Strings;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonSerialize
public record RefreshTokenCommand(
    @JsonProperty("refreshToken") String refreshToken) implements Validator {

    private static final String REFRESH_TOKEN_LABEL = "refreshToken";
    private static final String EMPTY_REFRESH_TOKEN_KEY = "auth.refresh.token.empty";
    private static final String EMPTY_REFRESH_TOKEN_MSG = "Refresh token cannot be empty";

    private static final ValidationError EMPTY_REFRESH_TOKEN = new ValidationError(
        REFRESH_TOKEN_LABEL, EMPTY_REFRESH_TOKEN_KEY, EMPTY_REFRESH_TOKEN_MSG);

    public static RefreshTokenCommand of(String refreshToken) {
        return new RefreshTokenCommand(refreshToken);
    }

    @Override
    public List<ValidationError> validate() {
        return Stream.of(check(refreshToken, EMPTY_REFRESH_TOKEN, Strings::isNullOrEmpty))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }
}
