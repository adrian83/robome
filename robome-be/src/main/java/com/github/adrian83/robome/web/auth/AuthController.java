package com.github.adrian83.robome.web.auth;

import static com.github.adrian83.robome.auth.Authentication.hashPassword;
import static com.github.adrian83.robome.auth.Authentication.userWithPasswordExists;
import static com.github.adrian83.robome.domain.user.model.Role.DEFAULT_USER_ROLES;
import static com.github.adrian83.robome.util.http.HttpMethod.POST;
import static com.github.adrian83.robome.util.http.HttpMethod.GET;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.domain.user.UserService;
import com.github.adrian83.robome.domain.user.model.User;
import com.github.adrian83.robome.web.auth.model.Login;
import com.github.adrian83.robome.web.auth.model.Register;
import com.github.adrian83.robome.web.auth.validation.LoginValidator;
import com.github.adrian83.robome.web.auth.validation.RegisterValidator;
import com.github.adrian83.robome.web.common.ExceptionHandler;
import com.github.adrian83.robome.web.common.Response;
import com.github.adrian83.robome.web.common.Routes;
import com.github.adrian83.robome.web.common.Security;
import com.github.adrian83.robome.web.common.Validation;
import com.google.inject.Inject;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class AuthController extends AllDirectives {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

  public static final String AUTH = "auth";
  public static final String LOGIN = "login";
  public static final String REGISTER = "register";
  public static final String CHECK = "check";

  private static final LoginValidator LOGIN_VALIDATOR = new LoginValidator();
  private static final RegisterValidator REGISTER_VALIDATOR = new RegisterValidator();

  private UserService userService;
  private ExceptionHandler exceptionHandler;
  private JwtAuthorizer jwtAuthorizer;
  private Response response;
  private Security security;
  private Routes routes;

  @Inject
  public AuthController(
      UserService userService,
      JwtAuthorizer jwtAuthorizer,
      ExceptionHandler exceptionHandler,
      Response response,
      Routes routes,
      Security security) {
    this.jwtAuthorizer = jwtAuthorizer;
    this.userService = userService;
    this.exceptionHandler = exceptionHandler;
    this.security = security;
    this.response = response;
    this.routes = routes;
  }

  public Route createRoute() {
    return route(
        options(routes.prefixPrefixSlash(AUTH, LOGIN, handleLoginOptionsRequest())),
        post(routes.prefixPrefixFormSlash(AUTH, LOGIN, Login.class, loginAction)),
        options(routes.prefixPrefixSlash(AUTH, REGISTER, handleRegisterOptionsRequest())),
        post(routes.prefixPrefixFormSlash(AUTH, REGISTER, Register.class, registerAction)),
        get(routes.prefixPrefixSlash(AUTH, CHECK, security.jwtSecured(this::isSignedIn))),
        options(routes.prefixPrefixSlash(AUTH, CHECK, handleCheckOptionsRequest())));
  }

  Function<Class<Register>, Route> registerAction =
      (Class<Register> clazz) -> security.unsecured(clazz, this::registerUser);

  Function<Class<Login>, Route> loginAction =
      (Class<Login> clazz) -> security.unsecured(clazz, this::loginUser);

  private Route loginUser(Login login) {

    LOGGER.info("Signing in user: {}", login);

    CompletableFuture<HttpResponse> responseF =
        CompletableFuture.completedFuture(login)
            .thenApply(form -> Validation.validate(form, LOGIN_VALIDATOR))
            .thenCompose(form -> userService.findUserByEmail(form.getEmail()))
            .thenApply(maybeUser -> userWithPasswordExists(maybeUser, login.getPassword()))
            .thenApply(jwtAuthorizer::createJWTToken)
            .thenApply(token -> response.response200(security.jwt(token)))
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route registerUser(Register register) {

    LOGGER.info("Registering new user: {}", register);

    CompletableFuture<HttpResponse> responseF =
        CompletableFuture.completedFuture(register)
            .thenApply(form -> Validation.validate(form, REGISTER_VALIDATOR))
            .thenApply(this::toUser)
            .thenCompose(userService::saveUser)
            .thenApply(done -> response.response201())
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route isSignedIn(CompletionStage<Optional<User>> maybeUserF) {

    LOGGER.info("Checking if user is logged in");

    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(user -> response.response200())
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private User toUser(Register form) {
    return new User(form.getEmail(), hashPassword(form.getPassword()), DEFAULT_USER_ROLES);
  }

  private Route handleRegisterOptionsRequest() {
    return complete(response.response200(POST));
  }

  private Route handleLoginOptionsRequest() {
    return complete(response.response200(POST));
  }

  private Route handleCheckOptionsRequest() {
    return complete(response.response200(GET));
  }
}
