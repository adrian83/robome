package ab.java.robome.web.auth;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;

import ab.java.robome.web.auth.model.Register;
import ab.java.robome.web.common.validation.ImmutableValidationError;
import ab.java.robome.web.common.validation.ValidationError;

public class RegisterValidator {

	public List<ValidationError> validate(Register register) {
		List<ValidationError> errors = new ArrayList<>();
		
		if (Strings.isNullOrEmpty(register.email())) {
			ValidationError error = ImmutableValidationError.builder()
					.field("email")
					.messageCode("register.email.empty")
					.message("Email cannot be empty")
					.build();
			
			errors.add(error);
		}
		
		if (Strings.isNullOrEmpty(register.password())) {
			
			ValidationError error = ImmutableValidationError.builder()
					.field("password")
					.messageCode("register.password.empty")
					.message("Password cannot be empty")
					.build();
			
			errors.add(error);
			
		} else if(!register.password().equals(register.repeatedPassword())) {
			
			ValidationError error = ImmutableValidationError.builder()
					.field("repeatedPassword")
					.messageCode("register.repeatedPassword.notEquals")
					.message("Repeated password cannot be different than password")
					.build();
			errors.add(error);
		}
		
		return errors;
	}
	
}
