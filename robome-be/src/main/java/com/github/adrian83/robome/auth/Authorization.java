package com.github.adrian83.robome.auth;

import com.github.adrian83.robome.domain.user.model.User;

import java.util.Optional;

import com.github.adrian83.robome.auth.exception.UserNotAuthorizedException;

public final class Authorization {

  private Authorization() {}

  public static User canReadTables(User user) {
    return Optional.ofNullable(user)
        .filter(PermissionChecker::canReadTables)
        .orElseThrow(() -> new UserNotAuthorizedException("user cannot read tables"));
  }

  public static User canWriteTables(User user) {
    return Optional.ofNullable(user)
        .filter(PermissionChecker::canWriteTables)
        .orElseThrow(() -> new UserNotAuthorizedException("user cannot write tables"));
  }

  public static User canReadStages(User user) {
    return Optional.ofNullable(user)
        .filter(PermissionChecker::canReadStages)
        .orElseThrow(() -> new UserNotAuthorizedException("user cannot read stages"));
  }

  public static User canWriteStages(User user) {
    return Optional.ofNullable(user)
        .filter(PermissionChecker::canWriteStages)
        .orElseThrow(() -> new UserNotAuthorizedException("user cannot write stages"));
  }

  public static User canReadAcivities(User user) {
    return Optional.ofNullable(user)
        .filter(PermissionChecker::canReadAcivities)
        .orElseThrow(() -> new UserNotAuthorizedException("user cannot read activities"));
  }

  public static User canWriteAcivities(User user) {
    return Optional.ofNullable(user)
        .filter(PermissionChecker::canWriteAcivities)
        .orElseThrow(() -> new UserNotAuthorizedException("user cannot write activities"));
  }
}
