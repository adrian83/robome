package com.github.adrian83.robome.web.common;

import static akka.http.javadsl.marshallers.jackson.Jackson.unmarshaller;
import static java.util.concurrent.CompletableFuture.completedStage;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.adrian83.robome.auth.JwtAuthorizer;
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

  @Inject
  public Security(JwtAuthorizer jwtAuthorizer) {
    this.jwtAuthorizer = jwtAuthorizer;
  }

  public RawHeader jwt(String token) {
    return RawHeader.create(AUTHORIZATION, token);
  }

  public Route jwtSecured(Function<CompletionStage<Optional<User>>, Route> logic) {
    return withUserFromAuthHeader(logic);
  }

  public <T> Route jwtSecured(
      T param, BiFunction<CompletionStage<Optional<User>>, T, Route> logic) {

    Function<CompletionStage<Optional<User>>, Route> apply =
        (maybeUserF) -> logic.apply(maybeUserF, param);

    return withUserFromAuthHeader(apply);
  }

  public <T, P> Route jwtSecured(
      T param1, P param2, TriFunction<CompletionStage<Optional<User>>, T, P, Route> logic) {

    Function<CompletionStage<Optional<User>>, Route> apply =
        (maybeUserF) -> logic.apply(maybeUserF, param1, param2);

    return withUserFromAuthHeader(apply);
  }

  public <T, P, R> Route jwtSecured(
      T param1,
      P param2,
      R param3,
      TetraFunction<CompletionStage<Optional<User>>, T, P, R, Route> logic) {

    Function<CompletionStage<Optional<User>>, Route> apply =
        (maybeUserF) -> logic.apply(maybeUserF, param1, param2, param3);

    return withUserFromAuthHeader(apply);
  }

  public <T, P, R, S> Route jwtSecured(
      T param1,
      P param2,
      R param3,
      Class<S> clazz,
      PentaFunction<CompletionStage<Optional<User>>, T, P, R, S, Route> logic) {

    Function<CompletionStage<Optional<User>>, Route> apply =
        (maybeUserF) ->
            entity(
                unmarshaller(clazz), form -> logic.apply(maybeUserF, param1, param2, param3, form));

    return withUserFromAuthHeader(apply);
  }

  public <T> Route jwtSecured(
      Class<T> clazz, BiFunction<CompletionStage<Optional<User>>, T, Route> logic) {

    Function<CompletionStage<Optional<User>>, Route> apply =
        (maybeUserF) -> entity(unmarshaller(clazz), form -> logic.apply(maybeUserF, form));

    return withUserFromAuthHeader(apply);
  }

  public <T, P> Route jwtSecured(
      P param, Class<T> clazz, TriFunction<CompletionStage<Optional<User>>, P, T, Route> logic) {

    Function<CompletionStage<Optional<User>>, Route> apply =
        (maybeUserF) -> entity(unmarshaller(clazz), form -> logic.apply(maybeUserF, param, form));

    return withUserFromAuthHeader(apply);
  }

  public <T> Route jwtSecured(
      String param1,
      String param2,
      Class<T> clazz,
      TetraFunction<CompletionStage<Optional<User>>, String, String, T, Route> logic) {

    Function<CompletionStage<Optional<User>>, Route> apply =
        (maybeUserF) ->
            entity(unmarshaller(clazz), form -> logic.apply(maybeUserF, param1, param2, form));

    return withUserFromAuthHeader(apply);
  }

  public <T> Route unsecured(Class<T> clazz, Function<T, Route> logic) {
    return entity(unmarshaller(clazz), form -> logic.apply(form));
  }

  private Route withUserFromAuthHeader(Function<CompletionStage<Optional<User>>, Route> inner) {
    return optionalHeaderValueByName(
        AUTHORIZATION, jwtToken -> procedeIfValidToken(jwtToken, inner::apply));
  }

  private Route procedeIfValidToken(
      Optional<String> maybeJwtToken, Function<CompletionStage<Optional<User>>, Route> inner) {

    var maybeUserF =
        completedStage(maybeJwtToken)
            .thenApply((maybeToken) -> maybeToken.flatMap(jwtAuthorizer::emailFromJwsToken))
            .thenCompose(jwtAuthorizer::findUser);

    return inner.apply(maybeUserF);
  }
}
