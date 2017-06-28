package ab.java.robome.web.common.validation;

import java.util.List;

import com.typesafe.config.Config;

public interface Validable {

	List<ValidationError> validate(Config config);
}
