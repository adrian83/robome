package com.github.adrian83.robome.domain.user;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {

	private UUID id;
	private String email;
	private String passwordHash;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;

	public User(UUID id, String email, String passwordHash, LocalDateTime createdAt, LocalDateTime modifiedAt) {
		super();
		this.id = id;
		this.email = email;
		this.passwordHash = passwordHash;
		this.createdAt = createdAt;
		this.modifiedAt = modifiedAt;
	}

	public UUID getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getModifiedAt() {
		return modifiedAt;
	}

}
