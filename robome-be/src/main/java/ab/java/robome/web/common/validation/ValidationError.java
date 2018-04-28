package ab.java.robome.web.common.validation;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


@Value.Immutable
@JsonSerialize(as = ImmutableValidationError.class)
@JsonDeserialize(as = ImmutableValidationError.class)
public interface ValidationError {

	String field();
	
	String messageCode();
	
	String message();
	
}
