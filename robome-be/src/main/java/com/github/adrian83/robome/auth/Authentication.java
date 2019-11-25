package com.github.adrian83.robome.auth;

import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;

import com.github.adrian83.robome.auth.exception.InvalidSignInDataException;
import com.github.adrian83.robome.auth.exception.UserNotFoundException;
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

  public static String hidePassword(String password) {
    if (password == null) {
      return "";
    }

    var len = password.length();

    if (len < 3) {
      return "**";
    } else {
      var begining = password.substring(0, 1);
      var end = password.substring(len - 2, len - 1);
      var stars = new String(new char[len - 2]).replace("\0", "*");
      return begining + stars + end;
    }
  }
}
