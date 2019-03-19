package com.github.adrian83.robome.auth;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.adrian83.robome.common.web.Validable;
import com.github.adrian83.robome.common.web.ValidationError;
import com.google.common.base.Strings;
import com.typesafe.config.Config;

public class LoginForm implements Validable {

	private String email;
	private String password;

	@JsonCreator
	public LoginForm(@JsonProperty("email") String email, @JsonProperty("password") String password) {
		super();
		this.email = email;
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public List<ValidationError> validate(Config config) {
		List<ValidationError> errors = new ArrayList<>();

		if (Strings.isNullOrEmpty(email)) {
			ValidationError error = new ValidationError("email", "loginForm.email.empty", "Email cannot be empty");
			errors.add(error);
		}

		if (Strings.isNullOrEmpty(password)) {
			ValidationError error = new ValidationError("password", "loginForm.password.empty",
					"Password cannot be empty");
			errors.add(error);
		}

		return errors;
	}
}
