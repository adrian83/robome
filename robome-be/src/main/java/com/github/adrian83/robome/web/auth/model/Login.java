package com.github.adrian83.robome.web.auth.model;

import static com.github.adrian83.robome.auth.Authentication.hidePassword;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Login {

	private static final String EMAIL_LABEL = "email";
	private static final String PASSWORD_LABEL = "password";

	private String email;
	private String password;

	@JsonCreator
	public Login(@JsonProperty(EMAIL_LABEL) String email, @JsonProperty(PASSWORD_LABEL) String password) {
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

	@Override
	public String toString() {
		return "Login [email=" + email + ", password=" + hidePassword(password) + "]";
	}

}
