package com.github.adrian83.robome.domain.common;

import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.common.validation.Validation;
import com.github.adrian83.robome.common.validation.Validator;

public record UserAndForm<T extends Validator>(UserData userData, T form) {

    public UserAndForm<T> validate() {
        Validation.validate(form);
        return new UserAndForm<T>(userData, form);
    }
}
