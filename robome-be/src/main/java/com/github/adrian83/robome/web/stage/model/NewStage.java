package com.github.adrian83.robome.web.stage.model;

import static com.github.adrian83.robome.common.validation.Validation.check;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.adrian83.robome.common.validation.ValidationError;
import com.github.adrian83.robome.common.validation.Validator;
import com.google.common.base.Strings;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonSerialize
public record NewStage(@JsonProperty(TITLE_LABEL)
        String title) implements Validator {

    private static final String TITLE_LABEL = "title";
    private static final String EMPTY_TITLE_KEY = "stage.create.title.empty";
    private static final String EMPTY_TITLE_MSG = "Stage title cannot be empty";

    private static final ValidationError EMPTY_TITLE = new ValidationError(TITLE_LABEL, EMPTY_TITLE_KEY, EMPTY_TITLE_MSG);

    @Override
    public List<ValidationError> validate() {
        return Stream.of(check(title, EMPTY_TITLE, Strings::isNullOrEmpty))
                .flatMap(Optional::stream)
                .toList();
    }
}
