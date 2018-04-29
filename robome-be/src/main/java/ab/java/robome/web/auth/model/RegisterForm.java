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
public class RegisterForm implements Validable {

	private String email;
	
	private String password;
	
	private String repeatedPassword;
	

	public List<ValidationError> validate(Config config) {
		List<ValidationError> errors = new ArrayList<>();
		
		if (Strings.isNullOrEmpty(email)) {
			ValidationError error = ValidationError.builder()
					.field("email")
					.messageCode("register.email.empty")
					.message("Email cannot be empty")
					.build();
			
			errors.add(error);
		}
		
		if (Strings.isNullOrEmpty(password)) {
			
			ValidationError error = ValidationError.builder()
					.field("password")
					.messageCode("register.password.empty")
					.message("Password cannot be empty")
					.build();
			
			errors.add(error);
			
		} else if(!password.equals(repeatedPassword)) {
			
			ValidationError error = ValidationError.builder()
					.field("repeatedPassword")
					.messageCode("register.repeatedPassword.notEquals")
					.message("Repeated password cannot be different than password")
					.build();
			errors.add(error);
		}
		
		return errors;
	}
}
