package ab.java.robome.web.auth.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;
import com.typesafe.config.Config;

import ab.java.robome.web.common.validation.Validable;
import ab.java.robome.web.common.validation.ValidationError;

public class RegisterForm implements Validable {

	private String email;
	private String password;
	private String repeatedPassword;

	public RegisterForm(String email, String password, String repeatedPassword) {
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

	public List<ValidationError> validate(Config config) {
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
