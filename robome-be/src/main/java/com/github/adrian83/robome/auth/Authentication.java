package com.github.adrian83.robome.auth;

import static com.github.adrian83.robome.domain.user.model.Role.DEFAULT_USER_ROLES;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.mindrot.jbcrypt.BCrypt;

import com.github.adrian83.robome.auth.exception.InvalidSignInDataException;
import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.auth.model.command.LoginRequest;
import com.github.adrian83.robome.auth.model.command.RegisterRequest;
import com.github.adrian83.robome.common.Time;
import com.github.adrian83.robome.domain.user.UserService;
import com.github.adrian83.robome.domain.user.model.User;
import com.google.inject.Inject;

public class Authentication {

  private UserService userService;
  private JwtAuthorizer jwtAuthorizer;

  @Inject
  public Authentication(UserService userService, JwtAuthorizer jwtAuthorizer) {
    this.userService = userService;
    this.jwtAuthorizer = jwtAuthorizer;
  }

  public CompletionStage<UserData> findUserWithPassword(LoginRequest req) {
    return userService
        .findUserByEmail(req.email())
        .thenApply(
            maybeUser ->
                maybeUser.orElseThrow(
                    () -> new InvalidSignInDataException("invalid password or email")))
        .thenApply(
            user -> {
              var valid = validPassword(req.password(), user.passwordHash());
              if (!valid) throw new InvalidSignInDataException("invalid password");
              return new UserData(user.id(), user.email(), user.roles());
            });
  }

  public CompletionStage<UserData> registerUser(RegisterRequest req) {
    var user =
        new User(
            UUID.randomUUID(),
            req.email(),
            hashPassword(req.password()),
            Time.utcNow(),
            Time.utcNow(),
            DEFAULT_USER_ROLES);

    return userService
        .saveUser(user)
        .thenApply(savedUser -> new UserData(savedUser.id(), savedUser.email(), savedUser.roles()));
  }

  public CompletionStage<UserData> findUserByToken(String token) {
    return CompletableFuture.completedFuture(token).thenApply(jwtAuthorizer::userFromToken);
    // throw exception
  }

  public String createAuthToken(UserData user) {
    return jwtAuthorizer.createToken(user);
  }

  private String hashPassword(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt());
  }

  private boolean validPassword(String password, String passwordHash) {
    return BCrypt.checkpw(password, passwordHash);
  }
}
