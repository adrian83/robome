package com.github.adrian83.robome.auth;

import java.util.Arrays;

import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.domain.user.model.Role;

public final class PermissionChecker {

  private PermissionChecker() {}

  public static boolean canReadTables(UserData user) {
    return hasAnyRole(user, Role.READ_TABLES, Role.ADMIN);
  }

  public static boolean canWriteTables(UserData user) {
    return hasAnyRole(user, Role.WRITE_TABLES, Role.ADMIN);
  }

  public static boolean canReadStages(UserData user) {
    return hasAnyRole(user, Role.READ_STAGES, Role.ADMIN);
  }

  public static boolean canWriteStages(UserData user) {
    return hasAnyRole(user, Role.WRITE_STAGES, Role.ADMIN);
  }

  public static boolean canReadAcivities(UserData user) {
    return hasAnyRole(user, Role.READ_ACTIVITIES, Role.ADMIN);
  }

  public static boolean canWriteAcivities(UserData user) {
    return hasAnyRole(user, Role.WRITE_ACTIVITIES, Role.ADMIN);
  }

  private static boolean hasAnyRole(UserData user, Role... roles) {
    return Arrays.stream(roles).anyMatch((role) -> user.getRoles().contains(role));
  }
}
