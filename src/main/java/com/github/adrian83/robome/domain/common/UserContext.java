package com.github.adrian83.robome.domain.common;

import java.util.Optional;
import java.util.UUID;

import com.github.adrian83.robome.auth.model.UserData;

public record UserContext(UserData loggedInUser, Optional<UUID> resourceOwner) {

    public static UserContext withUser(UserData loggedInUser) {
	return new UserContext(loggedInUser, Optional.empty());
    }

    public static UserContext withUserAndResourceOwnerId(UserData loggedInUser, UUID resourceOwner) {
	return new UserContext(loggedInUser, Optional.ofNullable(resourceOwner));
    }

    public boolean userOwnsResource() {
	return resourceOwner.map(userId -> loggedInUser.id().equals(userId)).orElse(false);
    }

    public UUID resourceOwnerIdOrError() {
	return resourceOwner.orElseThrow(() -> new IllegalArgumentException("userId is missing"));
    }
}
