package com.github.adrian83.robome.web.auth.validation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.adrian83.robome.common.web.Validation;
import com.github.adrian83.robome.common.web.ValidationError;
import com.github.adrian83.robome.domain.common.Validator;
import com.github.adrian83.robome.web.auth.model.Register;
import com.google.common.base.Strings;

public class RegisterValidator implements Validator<Register> {

	  private static final String EMAIL_LABEL = "email";
	  private static final String EMPTY_EMAIL_KEY = "user.register.email.empty";
	  private static final String EMPTY_EMAIL_MSG = "Email cannot be empty";

	  private static final String PASSWORD_1_LABEL = "password";
	  private static final String EMPTY_PASSWORD_1_KEY = "user.register.password.empty";
	  private static final String EMPTY_PASSWORD_1_MSG = "Password cannot be empty";

	  private static final String PASSWORD_2_LABEL = "repeatedPassword";
	  private static final String DIFFERENT_PASSWORDS_KEY = "user.register.passwords.different";
	  private static final String DIFFERENT_PASSWORDS_MSG = "Repeated password cannot be different";

	  private static final ValidationError EMPTY_EMAIL =
	      new ValidationError(EMAIL_LABEL, EMPTY_EMAIL_KEY, EMPTY_EMAIL_MSG);
	  private static final ValidationError EMPTY_PASSWORD_1 =
	      new ValidationError(PASSWORD_1_LABEL, EMPTY_PASSWORD_1_KEY, EMPTY_PASSWORD_1_MSG);
	  private static final ValidationError DIFFERENT_PASSWORDS =
	      new ValidationError(PASSWORD_2_LABEL, DIFFERENT_PASSWORDS_KEY, DIFFERENT_PASSWORDS_MSG);
	
	@Override
	public List<ValidationError> validate(Register form) {
	    return Stream.of(
	            Validation.check(form.getEmail(), EMPTY_EMAIL, Strings::isNullOrEmpty),
	            Validation.check(form.getPassword(), EMPTY_PASSWORD_1, Strings::isNullOrEmpty),
	            Validation.check(
	                form.getRepeatedPassword(),
	                DIFFERENT_PASSWORDS,
	                (String pass2) -> !pass2.equals(form.getPassword())))
	        .filter(Optional::isPresent)
	        .map(Optional::get)
	        .collect(Collectors.toList());
	}

}
