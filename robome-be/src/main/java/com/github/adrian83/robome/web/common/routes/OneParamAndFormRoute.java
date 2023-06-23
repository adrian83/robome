package com.github.adrian83.robome.web.common.routes;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import akka.http.javadsl.server.Route;

public class OneParamAndFormRoute<T> extends AbsFormRoute<T> implements Supplier<Route> {

    private BiFunction<String, Class<T>, Route> action;

    public OneParamAndFormRoute(String[] path, Class<T> clazz, BiFunction<String, Class<T>, Route> action) {
	super(path, clazz);
	this.action = action;
    }

    public OneParamAndFormRoute(String path, Class<T> clazz, BiFunction<String, Class<T>, Route> action) {
	super(path, clazz);
	this.action = action;
    }

    @Override
    public Route get() {
	if (emptyPath()) {
	    throw new IllegalStateException("path should contains one parameter");
	}

	var newPath = pathTail();

	if (startsWithParameter()) {
	    Function<String, Function<Class<T>, Route>> newFunc = (
		    String var1) -> (Class<T> clz) -> action.apply(var1, clz);
	    return pathPrefix(segment(), var1 -> new FormRoute<T>(newPath, getClazz(), newFunc.apply(var1)).get());
	}

	return pathPrefix(pathHead(), new OneParamAndFormRoute<T>(newPath, getClazz(), action));
    }
}
