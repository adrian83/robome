package ab.java.robome.web.table;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;

import ab.java.robome.table.model.NewTable;
import ab.java.robome.web.common.validation.ImmutableValidationError;
import ab.java.robome.web.common.validation.ValidationError;

public class NewTableValidator {

	public List<ValidationError> validate(NewTable newTable) {
		List<ValidationError> errors = new ArrayList<>();
		
		if (Strings.isNullOrEmpty(newTable.getName())) {
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
