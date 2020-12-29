package com.github.adrian83.robome.domain.common;

import java.util.List;

import com.github.adrian83.robome.web.common.ValidationError;

@FunctionalInterface
public interface Validator {
  List<ValidationError> validate();
}
