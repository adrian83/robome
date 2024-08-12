package com.github.adrian83.robome.domain.common;

import com.github.adrian83.robome.common.validation.Validation;
import com.github.adrian83.robome.common.validation.Validator;

public record UserAndForm<T extends Validator>(UserContext userContext, T form) {

    public UserAndForm<T> validate() {
        Validation.validate(form);
        return new UserAndForm(userContext, form);
    }
}
