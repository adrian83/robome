package com.github.adrian83.robome.domain.user;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.auth.Role;
import com.github.adrian83.robome.common.web.AbstractController;
import com.github.adrian83.robome.common.web.ExceptionHandler;
import com.github.adrian83.robome.common.web.Response;
import com.github.adrian83.robome.common.web.Validation;
import com.github.adrian83.robome.domain.user.model.Login;
import com.github.adrian83.robome.domain.user.model.Register;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.Route;

public class UserController extends AbstractController {

  public static final String AUTH = "auth";
  public static final String LOGIN = "login";
  public static final String REGISTER = "register";

  private UserService userService;

  @Inject
  public UserController(
      UserService userService,
      JwtAuthorizer jwtAuthorizer,
      Config config,
      ExceptionHandler exceptionHandler,
      Response responseProducer) {
    super(jwtAuthorizer, exceptionHandler, config, responseProducer);
    this.userService = userService;
  }

  Function<Class<Register>, Route> registerAction =
      (Class<Register> clazz) -> unsecured(clazz, this::registerUser);

  Function<Class<Login>, Route> loginAction =
      (Class<Login> clazz) -> unsecured(clazz, this::loginUser);

  public Route createRoute() {
    return route(
        post(prefixPrefixForm(AUTH, REGISTER, Register.class, registerAction)),
        post(prefixPrefixForm(AUTH, LOGIN, Login.class, loginAction)),
        options(prefixPrefix(AUTH, REGISTER, handleRegisterOptions())),
        options(prefixPrefix(AUTH, LOGIN, handleLoginOptions())));
  }

  private Route loginUser(Login login) {

    CompletableFuture<HttpResponse> responseF =
        CompletableFuture.completedFuture(login)
            .thenApply(form -> Validation.validate(form))
            .thenCompose(form -> userService.findUserByEmail(form.getEmail()))
            .thenApply(
                maybeUser -> Authentication.userWithPasswordExists(maybeUser, login.getPassword()))
            .thenApply(
                user ->
                    responseProducer.response200(jwt(jwtAuthorizer.createAuthorizationToken(user))))
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route registerUser(Register register) {

    CompletableFuture<HttpResponse> responseF =
        CompletableFuture.completedFuture(register)
            .thenApply(form -> Validation.validate(form))
            .thenApply(
                form ->
                    new User(
                        form.getEmail(),
                        Authentication.hashPassword(form.getPassword()),
                        Role.DEFAULT_USER_ROLES))
            .thenCompose(userService::saveUser)
            .thenApply(done -> responseProducer.response201())
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route handleRegisterOptions() {
    return complete(responseProducer.response200());
  }

  private Route handleLoginOptions() {
    return complete(responseProducer.response200());
  }
}
