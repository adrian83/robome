package com.github.adrian83.robome.web.auth;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.model.LoginRequest;
import com.github.adrian83.robome.auth.model.RegisterRequest;
import com.github.adrian83.robome.domain.user.model.User;
import com.github.adrian83.robome.util.http.HttpMethod;
import com.github.adrian83.robome.web.auth.model.Login;
import com.github.adrian83.robome.web.auth.model.Register;
import com.github.adrian83.robome.web.common.ExceptionHandler;
import com.github.adrian83.robome.web.common.Response;
import com.github.adrian83.robome.web.common.Security;
import com.github.adrian83.robome.web.common.Validation;
import com.github.adrian83.robome.web.common.routes.FormRoute;
import com.github.adrian83.robome.web.common.routes.PrefixRoute;
import com.google.inject.Inject;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthController extends AllDirectives {

  private ExceptionHandler exceptionHandler;
  private Response response;
  private Security security;
  private Authentication authentication;

  @Inject
  public AuthController(
      Authentication authentication,
      ExceptionHandler exceptionHandler,
      Response response,
      Security security) {
    this.authentication = authentication;
    this.exceptionHandler = exceptionHandler;
    this.security = security;
    this.response = response;
  }

  public Route createRoute() {
    return route(
        post(
            new FormRoute<Login>(
                "/auth/login/", Login.class, (clz) -> security.unsecured(clz, this::loginUser))),
        options(new PrefixRoute("/auth/login/", complete(response.response200(HttpMethod.POST)))),
        post(
            new FormRoute<Register>(
                "/auth/register/",
                Register.class,
                (clz) -> security.unsecured(clz, this::registerUser))),
        options(
            new PrefixRoute("/auth/register/", complete(response.response200(HttpMethod.POST)))),
        get(new PrefixRoute("/auth/check/", security.jwtSecured(this::isSignedIn))),
        options(new PrefixRoute("/auth/check/", complete(response.response200(HttpMethod.GET)))));
  }

  private Route loginUser(Login login) {
    log.info("Signing in user: {}", login);

    CompletableFuture<HttpResponse> responseF =
        CompletableFuture.completedFuture(login)
            .thenApply(Validation::validate)
            .thenApply(v -> toLoginRequest(login))
            .thenCompose(authentication::findUserWithPassword)
            .thenApply(authentication::createAuthToken)
            .thenApply(security::createAuthHeader)
            .thenApply(response::response200)
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route registerUser(Register register) {
    log.info("Registering new user: {}", register);

    CompletableFuture<HttpResponse> responseF =
        CompletableFuture.completedFuture(register)
            .thenApply(Validation::validate)
            .thenApply(v -> toRegisterRequest(register))
            .thenCompose(authentication::registerUser)
            .thenApply(done -> response.response201())
            .exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private Route isSignedIn(CompletionStage<User> userF) {
    log.info("Checking if user is logged in");

    CompletionStage<HttpResponse> responseF =
        userF.thenApply(user -> response.response200()).exceptionally(exceptionHandler::handle);

    return completeWithFuture(responseF);
  }

  private LoginRequest toLoginRequest(Login form) {
    return LoginRequest.builder().email(form.getEmail()).password(form.getPassword()).build();
  }

  private RegisterRequest toRegisterRequest(Register form) {
    return RegisterRequest.builder().email(form.getEmail()).password(form.getPassword()).build();
  }
}
