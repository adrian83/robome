package com.github.adrian83.robome.auth;

import com.github.adrian83.robome.domain.common.Ownerable;
import com.github.adrian83.robome.auth.exception.UserNotAuthorizedException;
import com.github.adrian83.robome.domain.common.Idable;
import com.github.adrian83.robome.domain.user.User;

public final class Authorization {

  private Authorization() {}

  public static User canReadTables(User user) {
    if (!PermissionChecker.canReadTables(user)) {
      throw new UserNotAuthorizedException("user cannot read tables");
    }
    return user;
  }

  public static User canWriteTables(User user) {
    if (!PermissionChecker.canWriteTables(user)) {
      throw new UserNotAuthorizedException("user cannot write tables");
    }
    return user;
  }

  public static <T extends Ownerable & Idable> T canUse(User user, T thing) {
    if (thing.getOwner().equals(user.getId())) {
      throw new UserNotAuthorizedException(
          String.format(
              "user: {0} cannot modify table: {1}", user.getId(), thing.getIdRepresentation()));
    }
    return thing;
  }

  public static User canReadStages(User user) {
    if (!PermissionChecker.canReadStages(user)) {
      throw new UserNotAuthorizedException("user cannot read stages");
    }
    return user;
  }

  public static User canWriteStages(User user) {
    if (!PermissionChecker.canWriteStages(user)) {
      throw new UserNotAuthorizedException("user cannot write stages");
    }
    return user;
  }
}
