package com.github.adrian83.robome.web.auth;

import static com.github.adrian83.robome.auth.Authentication.hashPassword;
import static com.github.adrian83.robome.domain.user.model.Role.DEFAULT_USER_ROLES;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.domain.user.UserService;
import com.github.adrian83.robome.domain.user.model.User;
import com.github.adrian83.robome.util.http.HttpMethod;
import com.github.adrian83.robome.util.tuple.Tuple2;
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

  public static final Tuple2<String, String> LOGIN_PATH = new Tuple2<>(AUTH, LOGIN);
  public static final Tuple2<String, String> REGISTER_PATH = new Tuple2<>(AUTH, REGISTER);
  public static final Tuple2<String, String> CHECK_PATH = new Tuple2<>(AUTH, CHECK);

  private static final LoginValidator LOGIN_VALIDATOR = new LoginValidator();
  private static final RegisterValidator REGISTER_VALIDATOR = new RegisterValidator();

  private UserService userService;
  private ExceptionHandler exceptionHandler;
  private JwtAuthorizer jwtAuthorizer;
  private Response response;
  private Security security;
  private Routes routes;
  private Authentication authentication;

  @Inject
  public AuthController(
      Authentication authentication,
      UserService userService,
      JwtAuthorizer jwtAuthorizer,
      ExceptionHandler exceptionHandler,
      Response response,
      Routes routes,
      Security security) {
    this.authentication = authentication;
    this.jwtAuthorizer = jwtAuthorizer;
    this.userService = userService;
    this.exceptionHandler = exceptionHandler;
    this.security = security;
    this.response = response;
    this.routes = routes;
  }

  public Route createRoute() {
    return route(
        options(routes.prefixPrefixSlash(LOGIN_PATH, handleLoginOptionsRequest())),
        post(routes.prefixPrefixFormSlash(LOGIN_PATH, Login.class, this::loginAction)),
        options(routes.prefixPrefixSlash(REGISTER_PATH, handleRegisterOptionsRequest())),
        post(routes.prefixPrefixFormSlash(REGISTER_PATH, Register.class, this::registerAction)),
        get(routes.prefixPrefixSlash(CHECK_PATH, security.jwtSecured(this::isSignedIn))),
        options(routes.prefixPrefixSlash(CHECK_PATH, handleCheckOptionsRequest())));
  }

  private Route registerAction(Class<Register> clazz) {
    return security.unsecured(clazz, this::registerUser);
  }

  private Route loginAction(Class<Login> clazz) {
    return security.unsecured(clazz, this::loginUser);
  }

  private Route loginUser(Login login) {
    LOGGER.info("Signing in user: {}", login);

    CompletableFuture<HttpResponse> responseF =
        CompletableFuture.completedFuture(login)
            .thenApply(form -> Validation.validate(form, LOGIN_VALIDATOR))
            .thenCompose(
                form -> authentication.findUserWithPassword(form.getEmail(), form.getPassword()))
            .thenApply(jwtAuthorizer::createToken)
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

  private Route isSignedIn(CompletionStage<User> userF) {
    LOGGER.info("Checking if user is logged in");

    CompletionStage<HttpResponse> responseF =
        userF.thenApply(user -> response.response200()).exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private User toUser(Register form) {
    return new User(form.getEmail(), hashPassword(form.getPassword()), DEFAULT_USER_ROLES);
  }

  private Route handleRegisterOptionsRequest() {
    return complete(response.response200(HttpMethod.POST));
  }

  private Route handleLoginOptionsRequest() {
    return complete(response.response200(HttpMethod.POST));
  }

  private Route handleCheckOptionsRequest() {
    return complete(response.response200(HttpMethod.GET));
  }
}
