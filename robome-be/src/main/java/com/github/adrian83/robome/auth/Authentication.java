package com.github.adrian83.robome.auth;

import static com.github.adrian83.robome.domain.user.model.Role.DEFAULT_USER_ROLES;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.mindrot.jbcrypt.BCrypt;

import com.github.adrian83.robome.auth.exception.InvalidSignInDataException;
import com.github.adrian83.robome.auth.exception.UserNotFoundException;
import com.github.adrian83.robome.auth.model.LoginRequest;
import com.github.adrian83.robome.auth.model.RegisterRequest;
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

  public CompletionStage<User> findUserWithPassword(LoginRequest req) {
    return userService
        .findUserByEmail(req.getEmail())
        .thenApply(
            user -> {
              var valid = validPassword(req.getPassword(), user.getPasswordHash());
              if (!valid) throw new InvalidSignInDataException();
              return user;
            });
  }

  public CompletionStage<User> registerUser(RegisterRequest req) {
    var user =
        User.builder()
            .id(UUID.randomUUID())
            .email(req.getEmail())
            .passwordHash(hashPassword(req.getPassword()))
            .roles(DEFAULT_USER_ROLES)
            .modifiedAt(Time.utcNow())
            .createdAt(Time.utcNow())
            .build();

    return userService.saveUser(user);
  }

  public CompletionStage<User> findUserByToken(String token) {
    return CompletableFuture.completedFuture(token)
        .thenApply(this::getEmailFromToken)
        .thenCompose(userService::findUserByEmail);
    // throw exception
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
}
