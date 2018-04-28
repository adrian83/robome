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
@JsonSerialize(as = ImmutableLoginForm.class)
@JsonDeserialize(as = ImmutableLoginForm.class)
public interface LoginForm extends Validable {

	String email();
	
	String password();
	
	default List<ValidationError> validate(Config config) {
		List<ValidationError> errors = new ArrayList<>();
		
		if (Strings.isNullOrEmpty(email())) {
			ValidationError error = ImmutableValidationError.builder()
					.field("email")
					.messageCode("loginForm.email.empty")
					.message("Email cannot be empty")
					.build();
			
			errors.add(error);
		}
		
		if (Strings.isNullOrEmpty(password())) {
			
			ValidationError error = ImmutableValidationError.builder()
					.field("password")
					.messageCode("loginForm.password.empty")
					.message("Password cannot be empty")
					.build();
			
			errors.add(error);
		}
		
		return errors;
	}
}
