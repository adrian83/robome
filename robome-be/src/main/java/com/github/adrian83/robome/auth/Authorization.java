package com.github.adrian83.robome.auth;

import com.github.adrian83.robome.domain.user.model.User;
import com.github.adrian83.robome.auth.exception.UserNotAuthorizedException;

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
