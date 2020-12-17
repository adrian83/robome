package com.github.adrian83.robome.auth;

import static com.github.adrian83.robome.common.Strings.fromBegining;
import static com.github.adrian83.robome.common.Strings.fromEnd;
import static java.util.Optional.ofNullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.mindrot.jbcrypt.BCrypt;

import com.github.adrian83.robome.auth.exception.InvalidSignInDataException;
import com.github.adrian83.robome.auth.exception.UserNotFoundException;
import com.github.adrian83.robome.domain.user.UserService;
import com.github.adrian83.robome.domain.user.model.User;
import com.google.inject.Inject;

public class Authentication {

  private static final int MIN_PASSWORD_LENGTH = 3;

  private UserService userService;
  private JwtAuthorizer jwtAuthorizer;

  @Inject
  public Authentication(UserService userService, JwtAuthorizer jwtAuthorizer) {
    this.userService = userService;
    this.jwtAuthorizer = jwtAuthorizer;
  }

  public CompletionStage<User> findUserWithPassword(String email, String password) {
    return userService
        .findUserByEmail(email)
        .thenApply(mUser -> mUser.filter(u -> validPassword(password, u.getPasswordHash())))
        .thenApply(mUser -> mUser.orElseThrow(() -> new InvalidSignInDataException()));
  }

  public CompletionStage<User> findUserByToken(String token) {
    return CompletableFuture.completedFuture(token)
        .thenApply(this::getEmailFromToken)
        .thenCompose(email -> userService.findUserByEmail(email))
        .thenApply(this::userExists);
  }

  public static String hashPassword(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt());
  }

  private String getEmailFromToken(String token) {
    return jwtAuthorizer
        .emailFromToken(token)
        .orElseThrow(() -> new UserNotFoundException("user cannot be found"));
  }

  private boolean validPassword(String password, String passwordHash) {
    return BCrypt.checkpw(password, passwordHash);
  }

  private User userExists(Optional<User> maybeUser) {
    return maybeUser.orElseThrow(() -> new UserNotFoundException("user cannot be found"));
  }

  public static String hidePassword(String password) {
    return ofNullable(password)
        .filter(p -> p.length() > MIN_PASSWORD_LENGTH)
        .map(p -> new String(new char[p.length() - 2]).replace("\0", "*"))
        .map(s -> fromBegining(password, 1) + s + fromEnd(password, 1))
        .orElse("**");
  }
}
