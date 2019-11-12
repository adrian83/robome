package com.github.adrian83.robome.web.stage.validation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.adrian83.robome.common.web.Validation;
import com.github.adrian83.robome.common.web.ValidationError;
import com.github.adrian83.robome.domain.common.Validator;
import com.github.adrian83.robome.domain.stage.model.UpdatedStage;
import com.google.common.base.Strings;

public class UpdatedStageValidator implements Validator<UpdatedStage> {

	  private static final String TITLE_LABEL = "title";
	  private static final String EMPTY_TITLE_KEY = "stage.update.title.empty";
	  private static final String EMPTY_TITLE_MSG = "Stage title cannot be empty";

	  private static final ValidationError EMPTY_NAME =
	      new ValidationError(TITLE_LABEL, EMPTY_TITLE_KEY, EMPTY_TITLE_MSG);

	  @Override
	  public List<ValidationError> validate(UpdatedStage form) {
	    return Stream.of(Validation.check(form.getTitle(), EMPTY_NAME, Strings::isNullOrEmpty))
	        .filter(Optional::isPresent)
	        .map(Optional::get)
	        .collect(Collectors.toList());
	  }
	
}
