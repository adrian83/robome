package com.github.adrian83.robome.common.web;

import java.util.List;

import com.typesafe.config.Config;

public final class Validation {


	private Validation() {}
	
	public static <T extends Validable> T validate(T form, Config config) {
		
		List<ValidationError> errors = form.validate(config);
		if(!errors.isEmpty()) {
			throw new ValidationException(errors);
		}
		
		return form;
	}
	
}
