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
        .findUserByEmail(req.getEmail())
        .thenApply(
            user -> {
              var valid = validPassword(req.getPassword(), user.getPasswordHash());
              if (!valid) throw new InvalidSignInDataException();
              return UserData.builder()
                  .id(user.getId())
                  .email(user.getEmail())
                  .roles(user.getRoles())
                  .build();
            });
  }

  public CompletionStage<UserData> registerUser(RegisterRequest req) {
    var user =
        User.builder()
            .id(UUID.randomUUID())
            .email(req.getEmail())
            .passwordHash(hashPassword(req.getPassword()))
            .roles(DEFAULT_USER_ROLES)
            .modifiedAt(Time.utcNow())
            .createdAt(Time.utcNow())
            .build();

    return userService
        .saveUser(user)
        .thenApply(
            savedUser ->
                UserData.builder()
                    .id(savedUser.getId())
                    .email(savedUser.getEmail())
                    .roles(savedUser.getRoles())
                    .build());
  }

  public CompletionStage<UserData> findUserByToken(String token) {
    return CompletableFuture.completedFuture(token).thenApply(jwtAuthorizer::emailFromToken);
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
