package com.github.adrian83.robome.web.common.routes;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.adrian83.robome.common.function.TriFunction;

import akka.http.javadsl.server.Route;

public class TwoParamsAndFormRoute<T> extends AbsFormRoute<T> implements Supplier<Route> {

    private TriFunction<String, String, Class<T>, Route> action;

    public TwoParamsAndFormRoute(String[] path, Class<T> clazz, TriFunction<String, String, Class<T>, Route> action) {
	super(path, clazz);
	this.action = action;
    }

    public TwoParamsAndFormRoute(String path, Class<T> clazz, TriFunction<String, String, Class<T>, Route> action) {
	super(path, clazz);
	this.action = action;
    }

    @Override
    public Route get() {

	if (emptyPath()) {
	    throw new IllegalStateException("path should contains one parameter");
	}

	var newPath = this.pathTail();

	if (startsWithParameter()) {
	    Function<String, BiFunction<String, Class<T>, Route>> newFunc = (
		    String var1) -> (String var2, Class<T> clz) -> action.apply(var1, var2, clz);
	    return pathPrefix(segment(),
		    var1 -> new OneParamAndFormRoute<T>(newPath, this.getClazz(), newFunc.apply(var1)).get());
	}

	return pathPrefix(pathHead(), new TwoParamsAndFormRoute<T>(newPath, this.getClazz(), action));
    }
}
