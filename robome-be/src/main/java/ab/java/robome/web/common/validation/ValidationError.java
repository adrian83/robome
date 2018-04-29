package ab.java.robome.web.common.validation;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ValidationError {

	private String field;
	private String messageCode;
	private String message;
	
}
