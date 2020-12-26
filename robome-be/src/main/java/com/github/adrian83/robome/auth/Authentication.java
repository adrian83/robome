package com.github.adrian83.robome.auth;

import static com.github.adrian83.robome.domain.user.model.Role.DEFAULT_USER_ROLES;

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

  public CompletionStage<Done> registerUser(RegisterRequest req) {
    User user = new User(req.getEmail(), hashPassword(req.getPassword()), DEFAULT_USER_ROLES);
    return userService.saveUser(user);
  }

  public CompletionStage<User> findUserByToken(String token) {
    return CompletableFuture.completedFuture(token)
        .thenApply(this::getEmailFromToken)
        .thenCompose(userService::findUserByEmail)
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
}
