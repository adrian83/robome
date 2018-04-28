package ab.java.robome.web.auth.model;

import java.util.ArrayList;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Strings;
import com.typesafe.config.Config;

import ab.java.robome.web.common.validation.ImmutableValidationError;
import ab.java.robome.web.common.validation.Validable;
import ab.java.robome.web.common.validation.ValidationError;

@Value.Immutable
@JsonSerialize(as = ImmutableRegisterForm.class)
@JsonDeserialize(as = ImmutableRegisterForm.class)
public interface RegisterForm extends Validable {

	String email();
	
	String password();
	
	String repeatedPassword();
	

	default List<ValidationError> validate(Config config) {
		List<ValidationError> errors = new ArrayList<>();
		
		if (Strings.isNullOrEmpty(email())) {
			ValidationError error = ImmutableValidationError.builder()
					.field("email")
					.messageCode("register.email.empty")
					.message("Email cannot be empty")
					.build();
			
			errors.add(error);
		}
		
		if (Strings.isNullOrEmpty(password())) {
			
			ValidationError error = ImmutableValidationError.builder()
					.field("password")
					.messageCode("register.password.empty")
					.message("Password cannot be empty")
					.build();
			
			errors.add(error);
			
		} else if(!password().equals(repeatedPassword())) {
			
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
