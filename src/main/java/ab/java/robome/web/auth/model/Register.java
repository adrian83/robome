package ab.java.robome.web.auth.model;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableRegister.class)
@JsonDeserialize(as = ImmutableRegister.class)
public interface Register {

	String email();
	
	String password();
	
	String repeatedPassword();
	
}
