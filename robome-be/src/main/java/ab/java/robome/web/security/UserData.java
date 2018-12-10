package ab.java.robome.web.security;

import java.util.UUID;

public class UserData {

	private UUID id;
	private String email;
	private String token;

	public UserData(UUID id, String email, String token) {
		super();
		this.id = id;
		this.email = email;
		this.token = token;
	}

	public UUID getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getToken() {
		return token;
	}

}
