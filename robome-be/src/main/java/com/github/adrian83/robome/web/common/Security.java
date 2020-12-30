package com.github.adrian83.robome.web.common;

import static akka.http.javadsl.marshallers.jackson.Jackson.unmarshaller;

import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.exception.TokenNotFoundException;
import com.github.adrian83.robome.common.function.PentaFunction;
import com.github.adrian83.robome.common.function.TetraFunction;
import com.github.adrian83.robome.common.function.TriFunction;
import com.github.adrian83.robome.domain.user.UserService;
import com.github.adrian83.robome.domain.user.model.User;
import com.github.adrian83.robome.web.common.http.HttpHeader;
import com.google.inject.Inject;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.headers.RawHeader;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class Security extends AllDirectives {

  private static final String AUTHORIZATION = HttpHeader.AUTHORIZATION.getText();

  private static final RuntimeException TOKEN_NOT_FOUND_EXCEPTION =
      new TokenNotFoundException("security token cannot be found");

  protected UserService userService;
  protected Authentication authentication;
  private ExceptionHandler exceptionHandler;

  @Inject
  public Security(UserService userService, Authentication authentication, ExceptionHandler exceptionHandler) {
    this.userService = userService;
    this.authentication = authentication;
    this.exceptionHandler = exceptionHandler;
  }

  public RawHeader createAuthHeader(String token) {
    return RawHeader.create(AUTHORIZATION, token);
  }
  
  private Route handleExceptions(CompletionStage<HttpResponse> respF) {
	  return completeWithFuture(respF.exceptionally(exceptionHandler::handle));
  }

  public Route secured(Function<CompletionStage<User>, CompletionStage<HttpResponse>> logic) {

    Function<CompletionStage<User>, Route> userFToRoute =
        (userF) -> handleExceptions(logic.apply(userF));

    return withUserFromAuthHeader(userFToRoute);
  }

  public <T> Route secured(
      T param, BiFunction<CompletionStage<User>, T, CompletionStage<HttpResponse>> logic) {

    Function<CompletionStage<User>, Route> userFToRoute =
        (userF) -> handleExceptions(logic.apply(userF, param));

    return withUserFromAuthHeader(userFToRoute);
  }

  public <T, P> Route secured(
      T param1,
      P param2,
      TriFunction<CompletionStage<User>, T, P, CompletionStage<HttpResponse>> logic) {

    Function<CompletionStage<User>, Route> userFToRoute =
        (userF) -> handleExceptions(logic.apply(userF, param1, param2));

    return withUserFromAuthHeader(userFToRoute);
  }

  public <T, P, R> Route secured(
      T p1,
      P p2,
      R p3,
      TetraFunction<CompletionStage<User>, T, P, R, CompletionStage<HttpResponse>> logic) {

    Function<CompletionStage<User>, Route> userFToRoute =
        (userF) -> handleExceptions(logic.apply(userF, p1, p2, p3));

    return withUserFromAuthHeader(userFToRoute);
  }

  public <T, P, R, S> Route secured(
      T param1,
      P param2,
      R param3,
      Class<S> clazz,
      PentaFunction<CompletionStage<User>, T, P, R, S, CompletionStage<HttpResponse>> logic) {

    Function<CompletionStage<User>, Route> userFToRoute =
        (userF) ->
            entity(
                unmarshaller(clazz),
                form -> handleExceptions(logic.apply(userF, param1, param2, param3, form)));

    return withUserFromAuthHeader(userFToRoute);
  }

  public <T> Route secured(
      Class<T> clazz, BiFunction<CompletionStage<User>, T, CompletionStage<HttpResponse>> logic) {

    Function<CompletionStage<User>, Route> userFToRoute =
        (userF) ->
            entity(unmarshaller(clazz), form -> handleExceptions(logic.apply(userF, form)));

    return withUserFromAuthHeader(userFToRoute);
  }

  public <T, P> Route secured(
      P param,
      Class<T> clazz,
      TriFunction<CompletionStage<User>, P, T, CompletionStage<HttpResponse>> logic) {

    Function<CompletionStage<User>, Route> userFToRoute =
        (userF) ->
            entity(
                unmarshaller(clazz), form -> handleExceptions(logic.apply(userF, param, form)));

    return withUserFromAuthHeader(userFToRoute);
  }

  public <T> Route secured(
      String p1,
      String p2,
      Class<T> clazz,
      TetraFunction<CompletionStage<User>, String, String, T, CompletionStage<HttpResponse>>
          logic) {

    Function<CompletionStage<User>, Route> apply =
        (userF) ->
            entity(
                unmarshaller(clazz), form -> handleExceptions(logic.apply(userF, p1, p2, form)));

    return withUserFromAuthHeader(apply);
  }

  public <T> Route unsecured(Class<T> clazz, Function<T, CompletionStage<HttpResponse>> logic) {
    return entity(unmarshaller(clazz), form -> handleExceptions(logic.apply(form)));
  }

  private Route withUserFromAuthHeader(Function<CompletionStage<User>, Route> inner) {
    return optionalHeaderValueByName(
        AUTHORIZATION,
        maybeToken ->
            maybeToken
                .map((token) -> procedeIfValidToken(token, inner::apply))
                .orElseThrow(() -> TOKEN_NOT_FOUND_EXCEPTION));
  }

  private Route procedeIfValidToken(String token, Function<CompletionStage<User>, Route> inner) {
    var userF = authentication.findUserByToken(token);
    return inner.apply(userF);
  }
}
