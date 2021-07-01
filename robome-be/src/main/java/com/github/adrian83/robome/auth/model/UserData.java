package com.github.adrian83.robome.auth.model;

import java.util.Set;
import java.util.UUID;

import com.github.adrian83.robome.domain.user.model.Role;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserData {
  private UUID id;
  private String email;
  private Set<Role> roles;

  public String[] roleName() {
    return roles.stream().map(Role::name).toArray(String[]::new);
  }
}
