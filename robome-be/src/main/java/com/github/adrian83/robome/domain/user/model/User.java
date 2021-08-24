package com.github.adrian83.robome.domain.user.model;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;


public record User(
   UUID id,
   String email,
   String passwordHash,
   LocalDateTime createdAt,
   LocalDateTime modifiedAt,
   Set<Role> roles
   ) {}
