package com.github.adrian83.robome.common.validation;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class Validation {

    private Validation() {
    }

    public static Validator validate(Validator validator) {
	List<ValidationError> errors = validator.validate();
	if (!errors.isEmpty()) {
	    throw new ValidationException(errors);
	}
	return validator;
    }

    public static <T> Optional<ValidationError> check(T arg, ValidationError error, Function<T, Boolean> isInvalid) {
	return isInvalid.apply(arg) ? Optional.ofNullable(error) : Optional.empty();
    }
}
