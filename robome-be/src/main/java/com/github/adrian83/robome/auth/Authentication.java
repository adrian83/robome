package com.github.adrian83.robome.auth;

import static com.github.adrian83.robome.common.Strings.fromBegining;
import static com.github.adrian83.robome.common.Strings.fromEnd;
import static java.util.Optional.ofNullable;

import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;

import com.github.adrian83.robome.auth.exception.InvalidSignInDataException;
import com.github.adrian83.robome.auth.exception.UserNotFoundException;
import com.github.adrian83.robome.domain.user.model.User;

public final class Authentication {

  private static final int MIN_PASSWORD_LENGTH = 3;

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

    return ofNullable(
            password != null && password.length() > MIN_PASSWORD_LENGTH ? password.length() : null)
        .map(
            (len) -> {
              var stars = new String(new char[len - 2]).replace("\0", "*");
              return fromBegining(password, 1) + stars + fromEnd(password, 1);
            })
        .orElse("**");
  }
}
