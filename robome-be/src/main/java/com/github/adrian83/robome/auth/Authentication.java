package com.github.adrian83.robome.auth;

import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;

import com.github.adrian83.robome.common.NotFoundException;
import com.github.adrian83.robome.domain.table.model.Table;
import com.github.adrian83.robome.domain.user.User;

public final class Authentication {

	private Authentication() {}
	
	public static boolean passwordEqual(String password, String passwordHash) {
		return BCrypt.checkpw(password, passwordHash);
	}
	
	public static User userExists(Optional<User> maybeUser) {
		return maybeUser.orElseThrow(() -> new UserNotFoundException("user cannot be found"));
	}

	public static Table tableExists(Optional<Table> maybeTable) {
		return maybeTable.orElseThrow(() -> new NotFoundException("table cannot be found"));
	}
	
}
