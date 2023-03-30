package com.github.adrian83.robome.web.table.model;

import static com.github.adrian83.robome.common.validation.Validation.check;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.adrian83.robome.common.validation.ValidationError;
import com.github.adrian83.robome.common.validation.Validator;
import com.google.common.base.Strings;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonSerialize
public record NewTable(@JsonProperty(NewTable.TITLE_LABEL) String title,
		@JsonProperty(NewTable.DESC_LABEL) String description) implements Validator {

	private static final String TITLE_LABEL = "title";
	private static final String EMPTY_TITLE_KEY = "table.create.title.empty";
	private static final String EMPTY_TITLE_MSG = "Table title cannot be empty";

	private static final String DESC_LABEL = "description";
	private static final String EMPTY_DESC_KEY = "table.create.description.empty";
	private static final String EMPTY_DESC_MSG = "Table description cannot be empty";

	private static final ValidationError EMPTY_TITLE = new ValidationError(TITLE_LABEL, EMPTY_TITLE_KEY,
			EMPTY_TITLE_MSG);

	private static final ValidationError EMPTY_DESCRIPTION = new ValidationError(DESC_LABEL, EMPTY_DESC_KEY,
			EMPTY_DESC_MSG);

	@Override
	public List<ValidationError> validate() {
		return Stream.of(
						check(title, EMPTY_TITLE, Strings::isNullOrEmpty),
						check(description, EMPTY_DESCRIPTION, Strings::isNullOrEmpty))
				.flatMap(Optional::stream)
				.collect(Collectors.toList());
	}
}
