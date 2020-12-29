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

  private Response response;
  private Security security;
  private Authentication authentication;

  @Inject
  public AuthController(Authentication authentication, Response response, Security security) {
    this.authentication = authentication;
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
        get(new PrefixRoute("/auth/check/", security.secured(this::isSignedIn))),
        options(new PrefixRoute("/auth/check/", complete(response.response200(HttpMethod.GET)))));
  }

  private CompletionStage<HttpResponse> loginUser(Login login) {
    log.info("Signing in user: {}", login);

    return CompletableFuture.completedFuture(login)
        .thenApply(Validation::validate)
        .thenApply(v -> toLoginRequest(login))
        .thenCompose(authentication::findUserWithPassword)
        .thenApply(authentication::createAuthToken)
        .thenApply(security::createAuthHeader)
        .thenApply(response::response200);
  }

  private CompletionStage<HttpResponse> registerUser(Register register) {
    log.info("Registering new user: {}", register);

    return CompletableFuture.completedFuture(register)
        .thenApply(Validation::validate)
        .thenApply(v -> toRegisterRequest(register))
        .thenCompose(authentication::registerUser)
        .thenApply(done -> response.response201());
  }

  private CompletionStage<HttpResponse> isSignedIn(CompletionStage<User> userF) {
    log.info("Checking if user is logged in");

    return userF.thenApply(user -> response.response200());
  }

  private LoginRequest toLoginRequest(Login form) {
    return LoginRequest.builder().email(form.getEmail()).password(form.getPassword()).build();
  }

  private RegisterRequest toRegisterRequest(Register form) {
    return RegisterRequest.builder().email(form.getEmail()).password(form.getPassword()).build();
  }
}
