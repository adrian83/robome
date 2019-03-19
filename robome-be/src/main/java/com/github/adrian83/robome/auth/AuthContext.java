package com.github.adrian83.robome.auth;

import java.util.UUID;

public class AuthContext {

	private UUID userId;
	private String userEmail;
	private String securityToken;

	public AuthContext(UUID userId, String userEmail, String securityToken) {
		super();
		this.userId = userId;
		this.userEmail = userEmail;
		this.securityToken = securityToken;
	}

	public UUID getUserId() {
		return userId;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public String getSecurityToken() {
		return securityToken;
	}

}
