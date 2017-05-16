package ab.java.robome.web.auth.model;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableLogin.class)
@JsonDeserialize(as = ImmutableLogin.class)
public interface Login {

	String email();
	
	String password();
}
