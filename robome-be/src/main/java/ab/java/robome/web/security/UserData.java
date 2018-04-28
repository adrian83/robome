package ab.java.robome.web.security;

import java.util.UUID;

import org.immutables.value.Value;

@Value.Immutable
public interface UserData {

	String email();
	
	UUID id();
	
	String token();
	
}
