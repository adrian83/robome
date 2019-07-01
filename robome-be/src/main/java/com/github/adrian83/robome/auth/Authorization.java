package com.github.adrian83.robome.auth;

import com.github.adrian83.robome.domain.table.model.Table;
import com.github.adrian83.robome.domain.user.User;

public final class Authorization {

	private Authorization() {}
	
	public static User canReadTables(User user) {
		if(!PermissionChecker.canReadTables(user)) {
			throw new UserNotAuthorizedException("user cannot read tables");
		}
		return user;
	}
	
	public static User canWriteTables(User user) {
		if(!PermissionChecker.canWriteTables(user)) {
			throw new UserNotAuthorizedException("user cannot write tables");
		}
		return user;
	}
	
	public static Table canWriteTable(User user, Table table) {
		if(table.getUserId().equals(user.getId())) {
			throw new UserNotAuthorizedException(String.format("user: {0} cannot modify table: {1}", user.getId(), table.getId()));
		}
		return table;
	}
	
	public static User canReadStages(User user) {
		if(!PermissionChecker.canReadStages(user)) {
			throw new UserNotAuthorizedException("user cannot read stages");
		}
		return user;
	}
	
	public static User canWriteStages(User user) {
		if(!PermissionChecker.canWriteStages(user)) {
			throw new UserNotAuthorizedException("user cannot write stages");
		}
		return user;
	}
}
