package com.github.adrian83.robome.web.common;

import static akka.http.javadsl.marshallers.jackson.Jackson.unmarshaller;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.exception.TokenNotFoundException;
import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.web.common.http.HttpHeader;
import com.github.adrian83.robome.web.common.request.Parameter0Request;
import com.github.adrian83.robome.web.common.request.Parameter1Request;
import com.github.adrian83.robome.web.common.request.Parameter2Request;
import com.github.adrian83.robome.web.common.request.Parameter2WithBodyRequest;
import com.github.adrian83.robome.web.common.request.Parameter3Request;
import com.github.adrian83.robome.web.common.request.Parameter3WithBodyRequest;
import com.github.adrian83.robome.web.common.request.Parameter4Request;
import com.github.adrian83.robome.web.common.request.Parameter4WithBodyRequest;
import com.google.inject.Inject;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.headers.RawHeader;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class Security extends AllDirectives {

    private static final String AUTHORIZATION = HttpHeader.AUTHORIZATION.getText();

    private static final RuntimeException TOKEN_NOT_FOUND_EXCEPTION = new TokenNotFoundException(
	    "security token cannot be found");

    protected Authentication authentication;
    private ExceptionHandler exceptionHandler;

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

    public Route secured(Parameter0Request logic) {
	Function<UserData, Route> userToRoute = userData -> handleExceptions(logic.apply(userData));
	return withUserFromAuthHeader(userToRoute);
    }

    public <T> Route secured(T param, Parameter1Request<T> logic) {
	Function<UserData, Route> userToRoute = userData -> handleExceptions(logic.apply(userData, param));
	return withUserFromAuthHeader(userToRoute);
    }

    public <T, P> Route secured(T p1, P p2, Parameter2Request<T, P> logic) {
	Function<UserData, Route> userToRoute = userData -> handleExceptions(logic.apply(userData, p1, p2));
	return withUserFromAuthHeader(userToRoute);
    }

    public <T, P, R> Route secured(T p1, P p2, R p3, Parameter3Request<T, P, R> logic) {
	Function<UserData, Route> userToRoute = userData -> handleExceptions(logic.apply(userData, p1, p2, p3));
	return withUserFromAuthHeader(userToRoute);
    }

    public <T, P, R, S> Route secured(T p1, P p2, R p3, Class<S> clazz, Parameter3WithBodyRequest<T, P, R, S> logic) {
	Function<UserData, Route> userToRoute = userData -> entity(unmarshaller(clazz),
		form -> handleExceptions(logic.apply(userData, p1, p2, p3, form)));
	return withUserFromAuthHeader(userToRoute);
    }

    public <T> Route secured(Class<T> clazz, Parameter1Request<T> logic) {
	Function<UserData, Route> userToRoute = userData -> entity(unmarshaller(clazz),
		form -> handleExceptions(logic.apply(userData, form)));
	return withUserFromAuthHeader(userToRoute);
    }

    public <T, P> Route secured(P param, Class<T> clazz, Parameter2Request<P, T> logic) {
	Function<UserData, Route> userToRoute = userData -> entity(unmarshaller(clazz),
		form -> handleExceptions(logic.apply(userData, param, form)));
	return withUserFromAuthHeader(userToRoute);
    }

    public <P, R, S> Route secured(P p1, R p2, Class<S> clazz, Parameter2WithBodyRequest<P, R, S> logic) {
	Function<UserData, Route> apply = userData -> entity(unmarshaller(clazz),
		form -> handleExceptions(logic.apply(userData, p1, p2, form)));
	return withUserFromAuthHeader(apply);
    }

    public <P, R, S, T> Route secured(P p1, R p2, S p3, T p4, Parameter4Request<P, R, S, T> logic) {
	Function<UserData, Route> apply = userData -> handleExceptions(logic.apply(userData, p1, p2, p3, p4));
	return withUserFromAuthHeader(apply);
    }

    public <P, R, S, T, U> Route secured(P p1, R p2, S p3, T p4, Class<U> clazz,
	    Parameter4WithBodyRequest<P, R, S, T, U> logic) {
	var unmarshaller = unmarshaller(clazz);
	Function<UserData, Route> apply = userData -> entity(unmarshaller,
		form -> handleExceptions(logic.apply(userData, p1, p2, p3, p4, form)));
	return withUserFromAuthHeader(apply);
    }

    public <T> Route unsecured(Class<T> clazz, Function<T, CompletionStage<HttpResponse>> logic) {
	return entity(unmarshaller(clazz), form -> handleExceptions(logic.apply(form)));
    }

    private Route withUserFromAuthHeader(Function<UserData, Route> inner) {
	return optionalHeaderValueByName(AUTHORIZATION,
		maybeToken -> maybeToken.map((token) -> inner.apply(authentication.findUserByToken(token)))
			.orElseThrow(() -> TOKEN_NOT_FOUND_EXCEPTION));
    }
}
