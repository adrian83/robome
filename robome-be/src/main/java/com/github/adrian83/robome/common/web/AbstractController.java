package com.github.adrian83.robome.common.web;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.domain.user.User;
import com.github.adrian83.robome.util.function.TetraFunction;
import com.github.adrian83.robome.util.function.TriFunction;
import com.github.adrian83.robome.util.http.Header;
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

	protected AbstractController(JwtAuthorizer jwtAuthorizer, ExceptionHandler exceptionHandler, Config config,
			Response responseProducer) {
		this.jwtAuthorizer = jwtAuthorizer;
		this.exceptionHandler = exceptionHandler;
		this.config = config;
		this.responseProducer = responseProducer;
	}

	protected Location locationFor(String... pathElems) {
		return Location.create(Arrays.stream(pathElems).collect(Collectors.joining("/")));
	}

	protected ContentType json() {
		return ContentType.create(ContentTypes.APPLICATION_JSON);
	}

	protected RawHeader jwt(String token) {
		return RawHeader.create(Header.AUTHORIZATION.getText(), token);
	}

	protected String corsOrigin() {
		return config.getString(CORS_ORIGIN_KEY);
	}

	protected List<HttpHeader> headers(HttpHeader... headers) {
		return Arrays.asList(headers);
	}

	protected Route jwtSecured(Function<CompletionStage<Optional<User>>, Route> logic) {
		return optionalHeaderValueByName(Header.AUTHORIZATION.getText(), jwtToken -> secured(jwtToken, logic));
	}

	protected <T> Route jwtSecured(T param, BiFunction<CompletionStage<Optional<User>>, T, Route> logic) {
		return optionalHeaderValueByName(Header.AUTHORIZATION.getText(),
				jwtToken -> secured(jwtToken, (userData) -> logic.apply(userData, param)));
	}

	protected <T, P> Route jwtSecured(T param1, P param2,
			TriFunction<CompletionStage<Optional<User>>, T, P, Route> logic) {
		return optionalHeaderValueByName(Header.AUTHORIZATION.getText(),
				jwtToken -> secured(jwtToken, (userData) -> logic.apply(userData, param1, param2)));
	}

	protected <T, P, R> Route jwtSecured(T param1, P param2, R param3,
			TetraFunction<CompletionStage<Optional<User>>, T, P, R, Route> logic) {
		return optionalHeaderValueByName(Header.AUTHORIZATION.getText(),
				jwtToken -> secured(jwtToken, (userData) -> logic.apply(userData, param1, param2, param3)));
	}

	protected <T> Route jwtSecured(Class<T> clazz, BiFunction<CompletionStage<Optional<User>>, T, Route> logic) {
		return optionalHeaderValueByName(Header.AUTHORIZATION.getText(), jwtToken -> jwtAuthorizer.authorized(jwtToken,
				userData -> entity(Jackson.unmarshaller(clazz), form -> logic.apply(userData, form))));
	}

	protected <T, P> Route jwtSecured(P param, Class<T> clazz,
			TriFunction<CompletionStage<Optional<User>>, P, T, Route> logic) {
		return optionalHeaderValueByName(Header.AUTHORIZATION.getText(), jwtToken -> jwtAuthorizer.authorized(jwtToken,
				userData -> entity(Jackson.unmarshaller(clazz), form -> logic.apply(userData, param, form))));
	}

	protected <T, P, R> Route jwtSecured(P param1, R param2, Class<T> clazz,
			TetraFunction<CompletionStage<Optional<User>>, P, R, T, Route> logic) {
		return optionalHeaderValueByName(Header.AUTHORIZATION.getText(), jwtToken -> jwtAuthorizer.authorized(jwtToken,
				userData -> entity(Jackson.unmarshaller(clazz), form -> logic.apply(userData, param1, param2, form))));
	}

	protected Route secured(Optional<String> jwtToken, Function<CompletionStage<Optional<User>>, Route> logic) {
		return jwtAuthorizer.authorized(jwtToken, logic);
	}
	
	protected <T> Route unsecured(Class<T> clazz, Function<T, Route> logic) {
		return  entity(Jackson.unmarshaller(clazz), form -> logic.apply(form));
	}

	//pathEndOrSingleSlash
	
	protected Supplier<Route> prefix(String prefix, Route route) {
		return () -> pathPrefix(prefix, () -> pathEndOrSingleSlash(() -> route));
	}

	protected Supplier<Route> prefixPrefix(String prefix1, String prefix2, Route route) {
		return prefix(prefix1, prefix(prefix2, route).get());
	}
	
	protected Supplier<Route> prefixVar(String prefix, Function<String, Route> action) {
		Route route = pathPrefix(prefix, () -> pathPrefix(segment(), action));
		return () -> route;
	}

	protected <T> Supplier<Route> prefixVarForm(String prefix, Class<T> clazz,  BiFunction<String, Class<T>, Route> action) {
		Route route = pathPrefix(prefix, () -> pathPrefix(segment(), val -> action.apply(val, clazz)));
		return () -> route;
	}
	
	protected <T> Supplier<Route> prefixForm(String prefix, Class<T> clazz, Function<Class<T>, Route> action) {
		Route route = pathPrefix(prefix, () -> action.apply(clazz));
		return () -> route;
	}
	
	protected <T> Supplier<Route> prefixPrefixForm(String prefix1, String prefix2, Class<T> clazz, Function<Class<T>, Route> action) {
		Supplier<Route> sup1 = prefixForm(prefix2, clazz, action);
		return prefix(prefix1, sup1.get());
	}

	protected Supplier<Route> varPrefix(String prefix, Function<String, Route> action) {
		Route route = pathPrefix(segment(), val -> pathPrefix(prefix, () -> action.apply(val)));
		return () -> route;
	}

	protected <T> Supplier<Route> varPrefixForm(String prefix, Class<T> clazz,
			BiFunction<String, Class<T>, Route> action) {

		Function<String, Function<Class<T>, Route>> f = (String val) -> (Class<T> c) -> action.apply(val, c);

		Route route = pathPrefix(segment(), val -> prefixForm(prefix, clazz, f.apply(val)).get());
		return () -> route;
	}

	protected Supplier<Route> prefixVarPrefix(String prefix1, String prefix2, Function<String, Route> action) {
		Route route = pathPrefix(prefix1, varPrefix(prefix2, action));
		return () -> route;
	}

	protected <T> Supplier<Route> prefixVarPrefixForm(String prefix1, String prefix2, Class<T> clazz,
			BiFunction<String, Class<T>, Route> action) {
		Route route = pathPrefix(prefix1, varPrefixForm(prefix2, clazz, action));
		return () -> route;
	}

	protected Supplier<Route> varPrefixVar(String prefix, BiFunction<String, String, Route> action) {

		Function<String, Function<String, Route>> f = (String val1) -> (String val2) -> action.apply(val1, val2);

		Route route = pathPrefix(segment(), val -> prefixVar(prefix, f.apply(val)).get());
		return () -> route;
	}

	protected Supplier<Route> varPrefixVarPrefix(String prefix1, String prefix2,
			BiFunction<String, String, Route> action) {

		Function<String, Function<String, Route>> f = (String val1) -> (String val2) -> action.apply(val1, val2);

		Route route = pathPrefix(segment(), val1 -> prefixVarPrefix(prefix1, prefix2, f.apply(val1)).get());
		return () -> route;
	}

	protected <T> Supplier<Route> varPrefixVarPrefixForm(String prefix1, String prefix2, Class<T> clazz,
			TriFunction<String, String, Class<T>, Route> action) {

		Function<String, BiFunction<String, Class<T>, Route>> f = (
				String val1) -> (String val2, Class<T> c) -> action.apply(val1, val2, c);

		Route route = pathPrefix(segment(), val1 -> prefixVarPrefixForm(prefix1, prefix2, clazz, f.apply(val1)).get());
		return () -> route;
	}

	protected Supplier<Route> prefixVarPrefixVar(String prefix1, String prefix2,
			BiFunction<String, String, Route> action) {

		Route route = pathPrefix(prefix1, varPrefixVar(prefix2, action));
		return () -> route;
	}

	protected Supplier<Route> varPrefixVarPrefixVar(String prefix1, String prefix2,
			TriFunction<String, String, String, Route> action) {

		Function<String, BiFunction<String, String, Route>> f = (
				String val1) -> (String val2, String val3) -> action.apply(val1, val2, val3);

		Route route = pathPrefix(segment(), val -> prefixVarPrefixVar(prefix1, prefix2, f.apply(val)).get());
		return () -> route;
	}

	protected Supplier<Route> prefixVarPrefixVarPrefix(String prefix1, String prefix2, String prefix3,
			BiFunction<String, String, Route> action) {

		Route route = pathPrefix(prefix1, varPrefixVarPrefix(prefix2, prefix3, action));
		return () -> route;
	}

	protected <T> Supplier<Route> prefixVarPrefixVarPrefixForm(String prefix1, String prefix2, String prefix3,
			Class<T> clazz, TriFunction<String, String, Class<T>, Route> action) {

		Route route = pathPrefix(prefix1, varPrefixVarPrefixForm(prefix2, prefix3, clazz, action));
		return () -> route;
	}

	protected Supplier<Route> prefixVarPrefixVarPrefixVar(String prefix1, String prefix2, String prefix3,
			TriFunction<String, String, String, Route> action) {

		Route route = pathPrefix(prefix1, varPrefixVarPrefixVar(prefix2, prefix3, action));
		return () -> route;
	}
}
