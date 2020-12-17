package com.github.adrian83.robome.auth;

import static com.github.adrian83.robome.common.Strings.fromBegining;
import static com.github.adrian83.robome.common.Strings.fromEnd;
import static com.github.adrian83.robome.domain.user.model.Role.DEFAULT_USER_ROLES;
import static java.util.Optional.ofNullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.mindrot.jbcrypt.BCrypt;

import com.github.adrian83.robome.auth.exception.InvalidSignInDataException;
import com.github.adrian83.robome.auth.exception.UserNotFoundException;
import com.github.adrian83.robome.auth.model.LoginRequest;
import com.github.adrian83.robome.auth.model.RegisterRequest;
import com.github.adrian83.robome.domain.user.UserService;
import com.github.adrian83.robome.domain.user.model.User;
import com.google.inject.Inject;

import akka.Done;

public class Authentication {

  private static final int MIN_PASSWORD_LENGTH = 3;

  private UserService userService;
  private JwtAuthorizer jwtAuthorizer;

  @Inject
  public Authentication(UserService userService, JwtAuthorizer jwtAuthorizer) {
    this.userService = userService;
    this.jwtAuthorizer = jwtAuthorizer;
  }

  public CompletionStage<User> findUserWithPassword(LoginRequest req) {
    return userService
        .findUserByEmail(req.getEmail())
        .thenApply(
            mUser -> mUser.filter(u -> validPassword(req.getPassword(), u.getPasswordHash())))
        .thenApply(mUser -> mUser.orElseThrow(() -> new InvalidSignInDataException()));
  }
  
  public CompletionStage<Done> registerUser(RegisterRequest req){
	  User user = new User(req.getEmail(), hashPassword(req.getPassword()), DEFAULT_USER_ROLES);
	  return userService.saveUser(user);
  }

  public CompletionStage<User> findUserByToken(String token) {
    return CompletableFuture.completedFuture(token)
        .thenApply(this::getEmailFromToken)
        .thenCompose(email -> userService.findUserByEmail(email))
        .thenApply(this::userExists);
  }

  public String createAuthToken(User user) {
	    return jwtAuthorizer.createToken(user);
	  }
  
  private String hashPassword(String password) {
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
