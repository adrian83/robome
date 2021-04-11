package com.github.adrian83.robome.auth;

import java.util.Optional;

import com.github.adrian83.robome.auth.exception.UserNotAuthorizedException;
import com.github.adrian83.robome.auth.model.UserData;

public final class Authorization {

  private Authorization() {}

  public static UserData canReadTables(UserData user) {
    return Optional.ofNullable(user)
        .filter(PermissionChecker::canReadTables)
        .orElseThrow(() -> new UserNotAuthorizedException("user cannot read tables"));
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

  public static UserData canWriteAcivities(UserData user) {
    return Optional.ofNullable(user)
        .filter(PermissionChecker::canWriteAcivities)
        .orElseThrow(() -> new UserNotAuthorizedException("user cannot write activities"));
  }
}
