package com.github.adrian83.robome.common.web;

import java.util.List;

import com.typesafe.config.Config;

public interface Validable {

	List<ValidationError> validate(Config config);
}
