package com.github.adrian83.robome.common.validation;

import java.util.List;

@FunctionalInterface
public interface Validator {
    List<ValidationError> validate();
}
