package com.github.adrian83.robome.auth;

import java.util.Arrays;

import com.github.adrian83.robome.domain.user.User;

public class PermissionChecker {
	
	private User user;
	private boolean disable;

	private PermissionChecker(User user) {
		this.user = user;
	}

	public static PermissionChecker check(User user) {
		return new PermissionChecker(user);
	}
	
	public PermissionChecker canListTables() {
		disable = disable && hasAnyRole(Role.READ_TABLES, Role.ADMIN);
		return this;
	}
	
	public boolean permitted() {
		return !disable;
	}
	
	private boolean hasAnyRole(Role ...roles) {
		return Arrays.stream(roles).anyMatch((role) -> user.getRoles().contains(role));
	}
	
	
}
