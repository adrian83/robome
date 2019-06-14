package com.github.adrian83.robome.auth;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

public enum Role {

	ADMIN,
	READ_TABLES,
	WRITE_TABLES;
	
	
	public static final List<Role> DEFAULT_USER_ROLES = Collections.unmodifiableList(Lists.newArrayList(READ_TABLES, WRITE_TABLES)); //Lists<Role>.asList(READ_TABLES, WRITE_TABLES));
	
	public static List<String> toStringList(List<Role> roles) {
		return roles.stream().map(Role::name).collect(Collectors.toList());
	}
	
	public static List<Role> fromStringList(List<String> strings) {
		return strings.stream().map(Role::valueOf).collect(Collectors.toList());
	}
	
}
