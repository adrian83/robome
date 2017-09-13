package ab.java.robome.web.security;

import org.immutables.value.Value;

@Value.Immutable
public interface UserData {

	String email();
	
	String token();
	
}
