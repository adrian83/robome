package com.github.adrian83.robome.domain.common;

import java.util.List;

import com.github.adrian83.robome.web.common.ValidationError;

public interface Validator<T> {

  List<ValidationError> validate(T form);
}
