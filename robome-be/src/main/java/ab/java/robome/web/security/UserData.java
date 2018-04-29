package ab.java.robome.web.security;

import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class UserData {

	private String email;
	
	private UUID id;
	
	private String token;
	
}
