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

	final String title;
	final String description;

	@JsonCreator
	public NewTable(@JsonProperty("title") String title, @JsonProperty("description") String description) {
		this.title = title;
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	@Override
	public List<ValidationError> validate(Config config) {
		List<ValidationError> errors = new ArrayList<>();
		
		if (Strings.isNullOrEmpty(getTitle())) {
			ValidationError error = ImmutableValidationError.builder()
					.field("title")
					.messageCode("table.create.title.empty")
					.message("Table title cannot be empty")
					.build();
			
			errors.add(error);
		}
		
		return errors;
	}

	
}
