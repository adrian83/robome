package ab.java.robome.auth.model;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableNewUser.class)
@JsonDeserialize(as = ImmutableNewUser.class)
public interface NewUser {
	
	String email();
	
}
