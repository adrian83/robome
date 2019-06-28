package com.github.adrian83.robome.common.web;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.adrian83.robome.auth.AuthContext;
import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.util.function.TetraFunction;
import com.github.adrian83.robome.util.function.TriFunction;
import com.typesafe.config.Config;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.headers.ContentType;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.model.headers.RawHeader;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class AbstractController extends AllDirectives {
	
	
	protected static final String CORS_ORIGIN_KEY = "cors.origin";
	
	protected JwtAuthorizer jwtAuthorizer;
	protected ExceptionHandler exceptionHandler;
	protected Response responseProducer;
	
	protected Config config;
	
	protected AbstractController(JwtAuthorizer jwtAuthorizer, ExceptionHandler exceptionHandler, Config config, Response responseProducer) {
		this.jwtAuthorizer = jwtAuthorizer;
		this.exceptionHandler = exceptionHandler;
		this.config = config;
		this.responseProducer = responseProducer;
	}

	protected Location locationFor(String ... pathElems) {
		return  Location.create(Arrays.stream(pathElems).collect(Collectors.joining("/")));
	}
	
	protected ContentType json() {
		return ContentType.create(ContentTypes.APPLICATION_JSON);
	}
	
	protected RawHeader jwt(String token) {
		return RawHeader.create(com.github.adrian83.robome.util.http.HttpHeader.AUTHORIZATION.getText(), token);
	}
	
	protected String corsOrigin() {
		return config.getString(CORS_ORIGIN_KEY);
	}
	
	protected List<HttpHeader> headers(HttpHeader ...headers) {
		return Arrays.asList(headers);
	}
	


	

	
	protected Route jwtSecured(Function<AuthContext, Route> logic) {
		return optionalHeaderValueByName(com.github.adrian83.robome.util.http.HttpHeader.AUTHORIZATION.getText(), jwtToken -> secured(jwtToken, logic));
	}
	
	protected <T> Route jwtSecured(T param, BiFunction<AuthContext, T, Route> logic) {
		return optionalHeaderValueByName(com.github.adrian83.robome.util.http.HttpHeader.AUTHORIZATION.getText(), jwtToken -> secured(jwtToken, (userData) -> logic.apply(userData, param)));
	}
	
	protected <T, P> Route jwtSecured(T param1, P param2,  TriFunction<AuthContext, T, P, Route> logic) {
		return optionalHeaderValueByName(com.github.adrian83.robome.util.http.HttpHeader.AUTHORIZATION.getText(), jwtToken -> secured(jwtToken, (userData) -> logic.apply(userData, param1, param2)));
	}
	
	protected <T, P, R> Route jwtSecured(T param1, P param2, R param3, TetraFunction<AuthContext, T, P, R, Route> logic) {
		return optionalHeaderValueByName(com.github.adrian83.robome.util.http.HttpHeader.AUTHORIZATION.getText(), jwtToken -> secured(jwtToken, (userData) -> logic.apply(userData, param1, param2, param3)));
	}
	
	protected <T> Route jwtSecured(Class<T> clazz, BiFunction<AuthContext, T, Route> logic) {
		return optionalHeaderValueByName(com.github.adrian83.robome.util.http.HttpHeader.AUTHORIZATION.getText(),
				jwtToken -> jwtAuthorizer.authorized(jwtToken,
						userData -> entity(Jackson.unmarshaller(clazz),
								form -> logic.apply(userData, form))));
	}
	
	protected <T, P> Route jwtSecured(P param, Class<T> clazz, TriFunction<AuthContext, P, T, Route> logic) {
		return optionalHeaderValueByName(com.github.adrian83.robome.util.http.HttpHeader.AUTHORIZATION.getText(),
				jwtToken -> jwtAuthorizer.authorized(jwtToken,
						userData -> entity(Jackson.unmarshaller(clazz),
								form -> logic.apply(userData, param, form))));
	}
	
	protected <T, P, R> Route jwtSecured(P param1, R param2, Class<T> clazz, TetraFunction<AuthContext, P, R, T, Route> logic) {
		return optionalHeaderValueByName(com.github.adrian83.robome.util.http.HttpHeader.AUTHORIZATION.getText(),
				jwtToken -> jwtAuthorizer.authorized(jwtToken,
						userData -> entity(Jackson.unmarshaller(clazz),
								form -> logic.apply(userData, param1, param2, form))));
	}
	
	protected Route secured(Optional<String> jwtToken, Function<AuthContext, Route> logic) {
		return jwtAuthorizer.authorized(jwtToken, logic);
	}
	
}
