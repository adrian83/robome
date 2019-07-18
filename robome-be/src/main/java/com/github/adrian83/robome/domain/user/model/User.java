package com.github.adrian83.robome.domain.user.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.github.adrian83.robome.auth.Role;
import com.github.adrian83.robome.common.time.TimeUtils;

public class User {

  private UUID id;
  private String email;
  private String passwordHash;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;
  private List<Role> roles;

  public User(String email, String passwordHash, List<Role> roles) {
    this(UUID.randomUUID(), email, passwordHash, roles, TimeUtils.utcNow(), TimeUtils.utcNow());
  }

  public User(
      UUID id,
      String email,
      String passwordHash,
      List<Role> roles,
      LocalDateTime createdAt,
      LocalDateTime modifiedAt) {
    super();
    this.id = id;
    this.email = email;
    this.passwordHash = passwordHash;
    this.roles = roles;
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

  public List<Role> getRoles() {
    return roles;
  }
}
