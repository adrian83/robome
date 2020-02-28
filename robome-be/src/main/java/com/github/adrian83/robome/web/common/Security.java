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
import com.github.adrian83.robome.util.http.Header;
import com.google.inject.Inject;

import akka.http.javadsl.model.headers.RawHeader;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class Security extends AllDirectives {

  private static final String AUTHORIZATION = Header.AUTHORIZATION.getText();

  protected JwtAuthorizer jwtAuthorizer;

  @Inject
  public Security(JwtAuthorizer jwtAuthorizer) {
    this.jwtAuthorizer = jwtAuthorizer;
  }

  public Route authorized(
      Optional<String> maybeJwtToken, Function<CompletionStage<Optional<User>>, Route> inner) {
    return procedeIfValidToken(maybeJwtToken, inner::apply);
  }

  public Route procedeIfValidToken(
      Optional<String> maybeJwtToken, Function<CompletionStage<Optional<User>>, Route> inner) {

    var maybeUserF =
        completedStage(maybeJwtToken)
            .thenApply((maybeToken) -> maybeToken.map(jwtAuthorizer::emailFromJwsToken))
            .thenCompose(jwtAuthorizer::findUser);

    return inner.apply(maybeUserF);
  }

  public RawHeader jwt(String token) {
    return RawHeader.create(AUTHORIZATION, token);
  }

  public Route jwtSecured(Function<CompletionStage<Optional<User>>, Route> logic) {
    return optionalHeaderValueByName(AUTHORIZATION, jwtToken -> secured(jwtToken, logic));
  }

  public <T> Route jwtSecured(
      T param, BiFunction<CompletionStage<Optional<User>>, T, Route> logic) {

    return optionalHeaderValueByName(
        AUTHORIZATION, jwtToken -> secured(jwtToken, (userData) -> logic.apply(userData, param)));
  }

  public <T, P> Route jwtSecured(
      T param1, P param2, TriFunction<CompletionStage<Optional<User>>, T, P, Route> logic) {

    return optionalHeaderValueByName(
        AUTHORIZATION,
        jwtToken -> secured(jwtToken, (userData) -> logic.apply(userData, param1, param2)));
  }

  public <T, P, R> Route jwtSecured(
      T param1,
      P param2,
      R param3,
      TetraFunction<CompletionStage<Optional<User>>, T, P, R, Route> logic) {

    return optionalHeaderValueByName(
        AUTHORIZATION,
        jwtToken -> secured(jwtToken, (userData) -> logic.apply(userData, param1, param2, param3)));
  }

  public <T, P, R, S> Route jwtSecured(
      T param1,
      P param2,
      R param3,
      Class<S> clazz,
      PentaFunction<CompletionStage<Optional<User>>, T, P, R, S, Route> logic) {

    return optionalHeaderValueByName(
        AUTHORIZATION,
        jwtToken ->
            authorized(
                jwtToken,
                userData ->
                    entity(
                        unmarshaller(clazz),
                        form -> logic.apply(userData, param1, param2, param3, form))));
  }

  public <T> Route jwtSecured(
      Class<T> clazz, BiFunction<CompletionStage<Optional<User>>, T, Route> logic) {

    return optionalHeaderValueByName(
        AUTHORIZATION,
        jwtToken ->
            authorized(
                jwtToken,
                userData -> entity(unmarshaller(clazz), form -> logic.apply(userData, form))));
  }

  public <T, P> Route jwtSecured(
      P param, Class<T> clazz, TriFunction<CompletionStage<Optional<User>>, P, T, Route> logic) {

    return optionalHeaderValueByName(
        AUTHORIZATION,
        jwtToken ->
            authorized(
                jwtToken,
                userData ->
                    entity(unmarshaller(clazz), form -> logic.apply(userData, param, form))));
  }

  public <T, P, R> Route jwtSecured(
      P param1,
      R param2,
      Class<T> clazz,
      TetraFunction<CompletionStage<Optional<User>>, P, R, T, Route> logic) {

    return optionalHeaderValueByName(
        AUTHORIZATION,
        jwtToken ->
            authorized(
                jwtToken,
                userData ->
                    entity(
                        unmarshaller(clazz), form -> logic.apply(userData, param1, param2, form))));
  }

  public Route secured(
      Optional<String> jwtToken, Function<CompletionStage<Optional<User>>, Route> logic) {

    return authorized(jwtToken, logic);
  }

  public <T> Route unsecured(Class<T> clazz, Function<T, Route> logic) {
    return entity(unmarshaller(clazz), form -> logic.apply(form));
  }
}
