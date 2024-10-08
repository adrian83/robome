package com.github.adrian83.robome.common.validation;

import java.util.List;

public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 423423411L;

    private final List<ValidationError> errors;

    public ValidationException(List<ValidationError> errors) {
        super();
        this.errors = errors;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }
}
