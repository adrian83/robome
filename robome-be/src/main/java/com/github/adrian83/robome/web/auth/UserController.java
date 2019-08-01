package com.github.adrian83.robome.web.auth;

import static com.github.adrian83.robome.util.http.HttpMethod.POST;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.common.web.ExceptionHandler;
import com.github.adrian83.robome.common.web.Response;
import com.github.adrian83.robome.common.web.Validation;
import com.github.adrian83.robome.domain.user.UserService;
import com.github.adrian83.robome.domain.user.model.Role;
import com.github.adrian83.robome.domain.user.model.User;
import com.github.adrian83.robome.web.auth.model.Login;
import com.github.adrian83.robome.web.auth.model.Register;
import com.github.adrian83.robome.web.auth.validation.LoginValidator;
import com.github.adrian83.robome.web.auth.validation.RegisterValidator;
import com.github.adrian83.robome.web.common.AbstractController;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.Route;

public class UserController extends AbstractController {

  public static final String AUTH = "auth";
  public static final String LOGIN = "login";
  public static final String REGISTER = "register";

  private static final LoginValidator LOGIN_VALIDATOR = new LoginValidator();
  private static final RegisterValidator REGISTER_VALIDATOR = new RegisterValidator();

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

  public Route createRoute() {
    return route(
        options(prefixPrefix(AUTH, REGISTER, handleRegisterOptionsRequest())),
        post(prefixPrefixForm(AUTH, REGISTER, Register.class, registerAction)),
        options(prefixPrefix(AUTH, LOGIN, handleLoginOptionsRequest())),
        post(prefixPrefixForm(AUTH, LOGIN, Login.class, loginAction)));
  }

  Function<Class<Register>, Route> registerAction =
      (Class<Register> clazz) -> unsecured(clazz, this::registerUser);

  Function<Class<Login>, Route> loginAction =
      (Class<Login> clazz) -> unsecured(clazz, this::loginUser);

  private Route loginUser(Login login) {

    CompletableFuture<HttpResponse> responseF =
        CompletableFuture.completedFuture(login)
            .thenApply(form -> Validation.validate(form, LOGIN_VALIDATOR))
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
            .thenApply(form -> Validation.validate(form, REGISTER_VALIDATOR))
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

  private Route handleRegisterOptionsRequest() {
    return complete(responseProducer.response200(POST));
  }

  private Route handleLoginOptionsRequest() {
    return complete(responseProducer.response200(POST));
  }
}
