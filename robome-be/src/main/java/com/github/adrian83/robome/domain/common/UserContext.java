package com.github.adrian83.robome.domain.common;

import java.util.Optional;
import java.util.UUID;

import com.github.adrian83.robome.auth.model.UserData;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserContext {
  private UserData loggedInUser;
  private Optional<UUID> resourceOwner;

  public boolean userOwnsResource() {
    return resourceOwner.map(userId -> loggedInUser.getId().equals(userId)).orElse(true);
  }

  public UUID resourceOwnerIdOrError() {
    return resourceOwner.orElseThrow(() -> new IllegalArgumentException("userId is missing"));
  }
}
