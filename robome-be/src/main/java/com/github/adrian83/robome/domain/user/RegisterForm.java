package com.github.adrian83.robome.domain.user;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.adrian83.robome.common.web.Validable;
import com.github.adrian83.robome.common.web.ValidationError;
import com.google.common.base.Strings;

public class RegisterForm implements Validable {

	private String email;
	private String password;
	private String repeatedPassword;

	@JsonCreator
	public RegisterForm(
			@JsonProperty("email") String email, 
			@JsonProperty("password") String password, 
			@JsonProperty("repeatedPassword") String repeatedPassword) {
		super();
		this.email = email;
		this.password = password;
		this.repeatedPassword = repeatedPassword;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public String getRepeatedPassword() {
		return repeatedPassword;
	}

	public List<ValidationError> validate() {
		List<ValidationError> errors = new ArrayList<>();

		if (Strings.isNullOrEmpty(email)) {
			ValidationError error = new ValidationError("email", "register.email.empty", "Email cannot be empty");

			errors.add(error);
		}

		if (Strings.isNullOrEmpty(password)) {

			ValidationError error = new ValidationError("password", "register.password.empty",
					"Password cannot be empty");

			errors.add(error);

		} else if (!password.equals(repeatedPassword)) {

			ValidationError error = new ValidationError("repeatedPassword", "register.repeatedPassword.notEquals",
					"Repeated password cannot be different than password");
			errors.add(error);
		}

		return errors;
	}
}
