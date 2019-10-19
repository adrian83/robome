package com.github.adrian83.robome.web.auth;

import static com.github.adrian83.robome.auth.Authentication.hashPassword;
import static com.github.adrian83.robome.auth.Authentication.userWithPasswordExists;
import static com.github.adrian83.robome.domain.user.model.Role.DEFAULT_USER_ROLES;
import static com.github.adrian83.robome.util.http.HttpMethod.POST;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.common.web.ExceptionHandler;
import com.github.adrian83.robome.common.web.Response;
import com.github.adrian83.robome.common.web.Validation;
import com.github.adrian83.robome.domain.user.UserService;
import com.github.adrian83.robome.domain.user.model.User;
import com.github.adrian83.robome.web.auth.model.Login;
import com.github.adrian83.robome.web.auth.model.Register;
import com.github.adrian83.robome.web.auth.validation.LoginValidator;
import com.github.adrian83.robome.web.auth.validation.RegisterValidator;
import com.github.adrian83.robome.web.common.AbstractController;
import com.github.adrian83.robome.web.common.Routes;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.Route;

public class AuthController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

  public static final String AUTH = "auth";
  public static final String LOGIN = "login";
  public static final String REGISTER = "register";

  private static final LoginValidator LOGIN_VALIDATOR = new LoginValidator();
  private static final RegisterValidator REGISTER_VALIDATOR = new RegisterValidator();

  private UserService userService;
  private Routes routes;

  @Inject
  public AuthController(
      UserService userService,
      JwtAuthorizer jwtAuthorizer,
      Config config,
      ExceptionHandler exceptionHandler,
      Response responseProducer,
      Routes routes) {
    super(jwtAuthorizer, exceptionHandler, config, responseProducer);
    this.userService = userService;
    this.routes = routes;
  }

  public Route createRoute() {
    return route(
        options(routes.prefixPrefixSlash(AUTH, LOGIN, handleLoginOptionsRequest())),
        post(routes.prefixPrefixFormSlash(AUTH, LOGIN, Login.class, loginAction)),
        options(routes.prefixPrefixSlash(AUTH, REGISTER, handleRegisterOptionsRequest())),
        post(routes.prefixPrefixFormSlash(AUTH, REGISTER, Register.class, registerAction)));
  }

  Function<Class<Register>, Route> registerAction =
      (Class<Register> clazz) -> unsecured(clazz, this::registerUser);

  Function<Class<Login>, Route> loginAction =
      (Class<Login> clazz) -> unsecured(clazz, this::loginUser);

  private Route loginUser(Login login) {

    LOGGER.info("Signing in user: {}", login);

    CompletableFuture<HttpResponse> responseF =
        CompletableFuture.completedFuture(login)
            .thenApply(form -> Validation.validate(form, LOGIN_VALIDATOR))
            .thenCompose(form -> userService.findUserByEmail(form.getEmail()))
            .thenApply(maybeUser -> userWithPasswordExists(maybeUser, login.getPassword()))
            .thenApply(jwtAuthorizer::createAuthorizationToken)
            .thenApply(token -> responseProducer.response200(jwt(token)))
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route registerUser(Register register) {

    LOGGER.info("Registering new user: {}", register);

    CompletableFuture<HttpResponse> responseF =
        CompletableFuture.completedFuture(register)
            .thenApply(form -> Validation.validate(form, REGISTER_VALIDATOR))
            .thenApply(this::toUser)
            .thenCompose(userService::saveUser)
            .thenApply(done -> responseProducer.response201())
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private User toUser(Register form) {
    return new User(form.getEmail(), hashPassword(form.getPassword()), DEFAULT_USER_ROLES);
  }

  private Route handleRegisterOptionsRequest() {
    return complete(responseProducer.response200(POST));
  }

  private Route handleLoginOptionsRequest() {
    return complete(responseProducer.response200(POST));
  }
}
