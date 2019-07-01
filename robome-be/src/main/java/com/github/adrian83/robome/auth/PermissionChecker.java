package com.github.adrian83.robome.auth;

import java.util.Arrays;

import com.github.adrian83.robome.domain.user.User;

public final class PermissionChecker {

	private PermissionChecker() {
	}

	public static boolean canReadTables(User user) {
		return hasAnyRole(user, Role.READ_TABLES, Role.ADMIN);
	}

	public static boolean canWriteTables(User user) {
		return hasAnyRole(user, Role.WRITE_TABLES, Role.ADMIN);
	}
	
	public static boolean canReadStages(User user) {
		return hasAnyRole(user, Role.READ_STAGES, Role.ADMIN);
	}

	public static boolean canWriteStages(User user) {
		return hasAnyRole(user, Role.WRITE_STAGES, Role.ADMIN);
	}
	
	private static boolean hasAnyRole(User user, Role... roles) {
		return Arrays.stream(roles).anyMatch((role) -> user.getRoles().contains(role));
	}

}
