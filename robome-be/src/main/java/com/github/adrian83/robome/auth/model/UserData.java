package com.github.adrian83.robome.auth.model;

import java.util.Set;
import java.util.UUID;

import com.github.adrian83.robome.domain.user.model.Role;

public record UserData(UUID id, String email, Set<Role> roles) {
  public String[] roleNames() {
    return roles.stream().map(Role::name).toArray(String[]::new);
  }
}
