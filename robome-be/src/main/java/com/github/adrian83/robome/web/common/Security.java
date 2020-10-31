package com.github.adrian83.robome.web.common;

import static akka.http.javadsl.marshallers.jackson.Jackson.unmarshaller;
import static java.util.concurrent.CompletableFuture.completedStage;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.auth.exception.UserNotAuthenticatedException;
import com.github.adrian83.robome.domain.user.UserService;
import com.github.adrian83.robome.domain.user.model.User;
import com.github.adrian83.robome.util.function.PentaFunction;
import com.github.adrian83.robome.util.function.TetraFunction;
import com.github.adrian83.robome.util.function.TriFunction;
import com.github.adrian83.robome.util.http.HttpHeader;
import com.google.inject.Inject;

import akka.http.javadsl.model.headers.RawHeader;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class Security extends AllDirectives {

  private static final String AUTHORIZATION = HttpHeader.AUTHORIZATION.getText();

  protected JwtAuthorizer jwtAuthorizer;
  protected UserService userService;

  @Inject
  public Security(JwtAuthorizer jwtAuthorizer, UserService userService) {
    this.jwtAuthorizer = jwtAuthorizer;
    this.userService = userService;
  }

  public RawHeader jwt(String token) {
    return RawHeader.create(AUTHORIZATION, token);
  }

  public Route jwtSecured(Function<CompletionStage<User>, Route> logic) {
    return withUserFromAuthHeader(logic);
  }

  public <T> Route jwtSecured(T param, BiFunction<CompletionStage<User>, T, Route> logic) {
    Function<CompletionStage<User>, Route> apply = (userF) -> logic.apply(userF, param);
    return withUserFromAuthHeader(apply);
  }

  public <T, P> Route secured(T p1, P p2, TriFunction<CompletionStage<User>, T, P, Route> logic) {
    Function<CompletionStage<User>, Route> apply = (userF) -> logic.apply(userF, p1, p2);
    return withUserFromAuthHeader(apply);
  }

  public <T, P, R> Route secured(
      T p1, P p2, R p3, TetraFunction<CompletionStage<User>, T, P, R, Route> logic) {
    Function<CompletionStage<User>, Route> apply = (userF) -> logic.apply(userF, p1, p2, p3);
    return withUserFromAuthHeader(apply);
  }

  public <T, P, R, S> Route secured(
      T p1,
      P p2,
      R p3,
      Class<S> clazz,
      PentaFunction<CompletionStage<User>, T, P, R, S, Route> logic) {

    Function<CompletionStage<User>, Route> apply =
        (userF) -> entity(unmarshaller(clazz), form -> logic.apply(userF, p1, p2, p3, form));

    return withUserFromAuthHeader(apply);
  }

  public <T> Route secured(Class<T> clazz, BiFunction<CompletionStage<User>, T, Route> logic) {
    Function<CompletionStage<User>, Route> apply =
        (userF) -> entity(unmarshaller(clazz), form -> logic.apply(userF, form));
    return withUserFromAuthHeader(apply);
  }

  public <T, P> Route jwtSecured(
      P param, Class<T> clazz, TriFunction<CompletionStage<User>, P, T, Route> logic) {
    Function<CompletionStage<User>, Route> apply =
        (userF) -> entity(unmarshaller(clazz), form -> logic.apply(userF, param, form));
    return withUserFromAuthHeader(apply);
  }

  public <T> Route secured(
      String p1,
      String p2,
      Class<T> clazz,
      TetraFunction<CompletionStage<User>, String, String, T, Route> logic) {

    Function<CompletionStage<User>, Route> apply =
        (userF) -> entity(unmarshaller(clazz), form -> logic.apply(userF, p1, p2, form));

    return withUserFromAuthHeader(apply);
  }

  public <T> Route unsecured(Class<T> clazz, Function<T, Route> logic) {
    return entity(unmarshaller(clazz), form -> logic.apply(form));
  }

  private Route withUserFromAuthHeader(Function<CompletionStage<User>, Route> inner) {
    return optionalHeaderValueByName(
        AUTHORIZATION,
        maybeToken ->
            maybeToken
                .map((token) -> procedeIfValidToken(token, inner::apply))
                .orElseThrow(
                    () -> new UserNotAuthenticatedException("security token cannot be found")));
  }

  private Route procedeIfValidToken(String token, Function<CompletionStage<User>, Route> inner) {
    var userF =
        completedStage(token)
            .thenApply(jwtAuthorizer::emailFromToken)
            .thenCompose(
                maybeEmail ->
                    maybeEmail
                        .map(userService::findUserByEmail)
                        .orElse(CompletableFuture.completedFuture(Optional.empty())))
            .thenApply(Authentication::userExists);

    return inner.apply(userF);
  }
}
