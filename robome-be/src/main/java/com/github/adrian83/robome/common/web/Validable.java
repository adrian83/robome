package com.github.adrian83.robome.common.web;

import java.util.List;

public interface Validable {

	List<ValidationError> validate();
}
