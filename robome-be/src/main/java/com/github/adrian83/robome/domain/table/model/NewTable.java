package com.github.adrian83.robome.domain.table.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.adrian83.robome.common.web.Validable;
import com.github.adrian83.robome.common.web.ValidationError;
import com.google.common.base.Strings;
import com.typesafe.config.Config;

public class NewTable implements Validable {

	private static final String TITLE_LABEL = "title";
	private static final String EMPTY_TITLE_KEY = "table.create.title.empty";
	private static final String EMPTY_TITLE_MSG = "Table title cannot be empty";

	private static final String DESC_LABEL = "description";
	private static final String EMPTY_DESC_KEY = "table.create.description.empty";
	private static final String EMPTY_DESC_MSG = "Table description cannot be empty";

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

	public String getDescription() {
		return description;
	}

	@Override
	public List<ValidationError> validate(Config config) {
		List<ValidationError> errors = new ArrayList<>();

		if (Strings.isNullOrEmpty(getTitle())) {
			errors.add(new ValidationError(TITLE_LABEL, EMPTY_TITLE_KEY, EMPTY_TITLE_MSG));
		}

		if (Strings.isNullOrEmpty(getDescription())) {
			errors.add(new ValidationError(DESC_LABEL, EMPTY_DESC_KEY, EMPTY_DESC_MSG));
		}

		return errors;
	}
}
