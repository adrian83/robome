package com.github.adrian83.robome.domain.user.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

public enum Role {
	ADMIN, READ_TABLES, WRITE_TABLES, READ_STAGES, WRITE_STAGES, READ_ACTIVITIES, WRITE_ACTIVITIES;

	private static final Set<Role> DEF_USER_ROLES = Sets.newHashSet(READ_TABLES, WRITE_TABLES, READ_STAGES,
			WRITE_STAGES, READ_ACTIVITIES, WRITE_ACTIVITIES);

	public static final Set<Role> DEFAULT_USER_ROLES = Collections.unmodifiableSet(Role.DEF_USER_ROLES);

	public static List<String> toStringList(List<Role> roles) {
		return roles.stream().map(Role::name).collect(Collectors.toList());
	}

	public static String toString(Set<Role> roles) {
		return roles.stream().map(Role::name).collect(Collectors.joining(","));
	}

	public static Set<Role> fromStringList(List<String> strings) {
		return strings.stream().map(Role::valueOf).collect(Collectors.toSet());
	}

	public static Set<Role> fromString(String rolesStr) {
		return fromStringList(Arrays.asList(rolesStr.split(",")));
	}
}
