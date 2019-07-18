package com.github.adrian83.robome.auth;

import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;

import com.github.adrian83.robome.auth.exception.InvalidSignInDataException;
import com.github.adrian83.robome.auth.exception.UserNotFoundException;
import com.github.adrian83.robome.common.NotFoundException;
import com.github.adrian83.robome.domain.table.model.Table;
import com.github.adrian83.robome.domain.user.model.User;

public final class Authentication {

  private Authentication() {}

  public static User userWithPasswordExists(Optional<User> maybeUser, String password) {
    return maybeUser
        .filter(user -> BCrypt.checkpw(password, user.getPasswordHash()))
        .orElseThrow(() -> new InvalidSignInDataException());
  }

  public static String hashPassword(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt());
  }

  public static User userExists(Optional<User> maybeUser) {
    return maybeUser.orElseThrow(() -> new UserNotFoundException("user cannot be found"));
  }

  public static Table tableExists(Optional<Table> maybeTable) {
    return maybeTable.orElseThrow(() -> new NotFoundException("table cannot be found"));
  }
}
