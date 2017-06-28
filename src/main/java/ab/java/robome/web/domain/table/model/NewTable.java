package ab.java.robome.web.domain.table.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.typesafe.config.Config;

import ab.java.robome.web.common.validation.ImmutableValidationError;
import ab.java.robome.web.common.validation.Validable;
import ab.java.robome.web.common.validation.ValidationError;

public class NewTable implements Validable {

	final String name;

	@JsonCreator
	public NewTable(@JsonProperty("name") String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public List<ValidationError> validate(Config config) {
		List<ValidationError> errors = new ArrayList<>();
		
		if (Strings.isNullOrEmpty(getName())) {
			ValidationError error = ImmutableValidationError.builder()
					.field("name")
					.messageCode("table.create.name.empty")
					.message("Table name cannot be empty")
					.build();
			
			errors.add(error);
		}
		
		return errors;
	}

	
}
