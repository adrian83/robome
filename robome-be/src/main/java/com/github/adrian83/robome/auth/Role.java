package com.github.adrian83.robome.auth;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

public enum Role {

	ADMIN,
	READ_TABLES,
	WRITE_TABLES, 
	READ_STAGES, 
	WRITE_STAGES, 
	READ_ACTIVITIES, 
	WRITE_ACTIVITIES;
	
	private static final List<Role> DEF_USER_ROLES = Lists.newArrayList(READ_TABLES, WRITE_TABLES, READ_STAGES, 
			WRITE_STAGES, READ_ACTIVITIES, WRITE_ACTIVITIES);
	
	public static final List<Role> DEFAULT_USER_ROLES = Collections.unmodifiableList(Role.DEF_USER_ROLES); 
	
	public static List<String> toStringList(List<Role> roles) {
		return roles.stream().map(Role::name).collect(Collectors.toList());
	}
	
	public static List<Role> fromStringList(List<String> strings) {
		return strings.stream().map(Role::valueOf).collect(Collectors.toList());
	}
	
}
