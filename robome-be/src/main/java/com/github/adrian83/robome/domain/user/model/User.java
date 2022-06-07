package com.github.adrian83.robome.domain.user.model;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.github.adrian83.robome.common.Time;


public record User(
   UUID id,
   String email,
   String passwordHash,
   LocalDateTime createdAt,
   LocalDateTime modifiedAt,
   Set<Role> roles
   ) {
	
	public User(String email, String passwordHash, Set<Role> roles) {
		this(UUID.randomUUID(), email, passwordHash, Time.utcNow(), Time.utcNow(), roles);
	}
	
}
