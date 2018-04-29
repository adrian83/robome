package ab.java.robome.web.auth.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;
import com.typesafe.config.Config;

import ab.java.robome.web.common.validation.Validable;
import ab.java.robome.web.common.validation.ValidationError;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class LoginForm implements Validable {

	private String email;
	private String password;
	
	public List<ValidationError> validate(Config config) {
		List<ValidationError> errors = new ArrayList<>();
		
		if (Strings.isNullOrEmpty(email)) {
			ValidationError error = ValidationError.builder()
					.field("email")
					.messageCode("loginForm.email.empty")
					.message("Email cannot be empty")
					.build();
			
			errors.add(error);
		}
		
		if (Strings.isNullOrEmpty(password)) {
			
			ValidationError error = ValidationError.builder()
					.field("password")
					.messageCode("loginForm.password.empty")
					.message("Password cannot be empty")
					.build();
			
			errors.add(error);
		}
		
		return errors;
	}
}
