package com.github.adrian83.robome.auth;

import java.util.Optional;
import java.util.function.Function;

import com.github.adrian83.robome.auth.exception.UserNotAuthorizedException;
import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.domain.common.UserContext;

public final class Authorization {

  private Authorization() {}

  public static UserContext canReadTables(UserContext userCtx) {
	    boolean hasPermision = can(userCtx, PermissionChecker::canReadTables);
	    if (hasPermision) {
	      return userCtx;
	    }
	    throw new UserNotAuthorizedException("user cannot read tables");
	  }
  
  public static UserData canWriteTables(UserData user) {
    return Optional.ofNullable(user)
        .filter(PermissionChecker::canWriteTables)
        .orElseThrow(() -> new UserNotAuthorizedException("user cannot write tables"));
  }

  public static UserData canReadStages(UserData user) {
    return Optional.ofNullable(user)
        .filter(PermissionChecker::canReadStages)
        .orElseThrow(() -> new UserNotAuthorizedException("user cannot read stages"));
  }

  public static UserData canWriteStages(UserData user) {
    return Optional.ofNullable(user)
        .filter(PermissionChecker::canWriteStages)
        .orElseThrow(() -> new UserNotAuthorizedException("user cannot write stages"));
  }

  public static UserData canReadAcivities(UserData user) {
    return Optional.ofNullable(user)
        .filter(PermissionChecker::canReadAcivities)
        .orElseThrow(() -> new UserNotAuthorizedException("user cannot read activities"));
  }

  public static UserContext canWriteAcivities(UserContext userCtx) {
	    boolean hasPermision = can(userCtx, PermissionChecker::canWriteAcivities);
	    if (hasPermision) {
	      return userCtx;
	    }
	    throw new UserNotAuthorizedException("user cannot write activities");
  }

  public static UserContext canWriteTables(UserContext userCtx) {
    boolean hasPermision = can(userCtx, PermissionChecker::canWriteTables);
    if (hasPermision) {
      return userCtx;
    }
    throw new UserNotAuthorizedException("user cannot write tables");
  }

  private static boolean can(UserContext userCtx, Function<UserData, Boolean> hasPermision) {
    return PermissionChecker.isAdmin(userCtx.getLoggedInUser())
        || (hasPermision.apply(userCtx.getLoggedInUser()) && userCtx.userOwnsResource());
  }
}
