package com.github.adrian83.robome.domain.common;

import java.util.Optional;
import java.util.UUID;

import com.github.adrian83.robome.auth.model.UserData;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserContext {
  private UserData loggedInUser;
  private Optional<UUID> resourceOwner;

  public static UserContext withUser(UserData loggedInUser) {
    return new UserContext(loggedInUser, Optional.empty());
  }

  public static UserContext withUserAndResourceOwnerId(UserData loggedInUser, UUID resourceOwner) {
    return new UserContext(loggedInUser, Optional.ofNullable(resourceOwner));
  }

  public boolean userOwnsResource() {
    return resourceOwner.map(userId -> loggedInUser.getId().equals(userId)).orElse(true);
  }

  public UUID resourceOwnerIdOrError() {
    return resourceOwner.orElseThrow(() -> new IllegalArgumentException("userId is missing"));
  }
}
