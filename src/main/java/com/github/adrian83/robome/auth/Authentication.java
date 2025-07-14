package com.github.adrian83.robome.auth;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.mindrot.jbcrypt.BCrypt;

import com.github.adrian83.robome.auth.exception.InvalidSignInDataException;
import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.auth.model.command.LoginCommand;
import com.github.adrian83.robome.auth.model.command.RegisterCommand;
import com.github.adrian83.robome.domain.user.UserService;
import static com.github.adrian83.robome.domain.user.model.Role.DEFAULT_USER_ROLES;
import com.github.adrian83.robome.domain.user.model.User;
import com.google.inject.Inject;

import akka.actor.ActorSystem;

public class Authentication {

    private static final RuntimeException INVALID_PASS_OR_EMAIL_EXCEPTION = new InvalidSignInDataException(
            "invalid password or email");

    private final UserService userService;
    private final ActorSystem actorSystem;
    private final AuthTokenService authTokenService;

    @Inject
    public Authentication(UserService userService, AuthTokenService authTokenService, ActorSystem actorSystem) {
        this.userService = userService;
        this.authTokenService = authTokenService;
        this.actorSystem = actorSystem;
    }

    public CompletionStage<UserData> findUserById(UUID userId) {
        return userService.findUserById(userId)
                .thenApply(maybeUser -> maybeUser
                        .map(user -> new UserData(user.id(), user.email(), user.roles()))
                        .orElseThrow(() -> new InvalidSignInDataException("user not found")));
    }

    public ActorSystem getMaterializer() {
        return actorSystem;
    }

    public CompletionStage<UserData> loginUser(LoginCommand cmd) {
        return userService.findUserByEmail(cmd.email())
                .thenApply(this::userExists)
                .thenApply(user -> isPasswordCorrect(user, cmd.password()))
                .thenApply(user -> new UserData(user.id(), user.email(), user.roles()));
    }

    public CompletionStage<UserData> registerUser(RegisterCommand req) {
        var passwordHash = hashPassword(req.password());
        var user = new User(req.email(), passwordHash, DEFAULT_USER_ROLES);
        return userService.saveUser(user)
                .thenApply(savedUser -> new UserData(savedUser.id(), savedUser.email(), savedUser.roles()));
    }

    public UserData findUserByToken(String token) {
        return authTokenService.extractUserDataFromToken(token);
    }

    public String createAuthToken(UserData user) {
        return authTokenService.createAuthToken(user);
    }

    private User userExists(Optional<User> maybeUser) {
        return maybeUser.orElseThrow(() -> INVALID_PASS_OR_EMAIL_EXCEPTION);
    }

    private User isPasswordCorrect(User user, String password) {
        var valid = isPasswordValid(password, user.passwordHash());
        if (!valid) {
            throw INVALID_PASS_OR_EMAIL_EXCEPTION;
        }
        return user;
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private boolean isPasswordValid(String password, String passwordHash) {
        return BCrypt.checkpw(password, passwordHash);
    }
}
