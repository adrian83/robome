package com.github.adrian83.robome.web.common;

import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.exception.TokenNotFoundException;
import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.web.common.http.HttpHeader;
import com.github.adrian83.robome.web.common.request.Parameter1Request;
import com.github.adrian83.robome.web.common.request.Parameter1WithBodyRequest;
import com.google.inject.Inject;

import static akka.http.javadsl.marshallers.jackson.Jackson.unmarshaller;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.headers.RawHeader;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class Security extends AllDirectives {

    private static final String AUTHORIZATION = HttpHeader.AUTHORIZATION.getText();

    private static final RuntimeException TOKEN_NOT_FOUND_EXCEPTION = new TokenNotFoundException(
            "security token cannot be found");

    protected Authentication authentication;
    private final ExceptionHandler exceptionHandler;

    @Inject
    public Security(Authentication authentication, ExceptionHandler exceptionHandler) {
        this.authentication = authentication;
        this.exceptionHandler = exceptionHandler;
    }

    public RawHeader createAuthHeader(String token) {
        return RawHeader.create(AUTHORIZATION, token);
    }

    private Route handleExceptions(CompletionStage<HttpResponse> respF) {
        return completeWithFuture(respF.exceptionally(exceptionHandler::handle));
    }

    public Route secured2(Map<String, String> pathParams, Parameter1Request<Map<String, String>> logic) {
        Function<UserData, Route> userToRoute = userData -> handleExceptions(logic.apply(userData, pathParams));
        return withUserFromAuthHeader(userToRoute);
    }

    public <S> Route secured2(Map<String, String> pathParams, Class<S> clazz, Parameter1WithBodyRequest<Map<String, String>, S> logic) {
        Function<UserData, Route> apply = userData -> entity(unmarshaller(clazz), form -> handleExceptions(logic.apply(userData, pathParams, form)));
        return withUserFromAuthHeader(apply);
    }

    public <T> Route unsecured(Class<T> clazz, Function<T, CompletionStage<HttpResponse>> logic) {
        return entity(unmarshaller(clazz), form -> handleExceptions(logic.apply(form)));
    }

    private Route withUserFromAuthHeader(Function<UserData, Route> inner) {
        return optionalHeaderValueByName(AUTHORIZATION, maybeToken -> maybeToken.map((token) -> inner.apply(authentication.findUserByToken(token)))
                .orElseThrow(() -> TOKEN_NOT_FOUND_EXCEPTION));
    }
}
