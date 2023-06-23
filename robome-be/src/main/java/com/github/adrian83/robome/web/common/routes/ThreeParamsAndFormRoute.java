package com.github.adrian83.robome.web.common.routes;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.util.function.Function;
import java.util.function.Supplier;

import com.github.adrian83.robome.common.function.TetraFunction;
import com.github.adrian83.robome.common.function.TriFunction;

import akka.http.javadsl.server.Route;

public class ThreeParamsAndFormRoute<T> extends AbsFormRoute<T> implements Supplier<Route> {

    private TetraFunction<String, String, String, Class<T>, Route> action;

    public ThreeParamsAndFormRoute(String[] path, Class<T> clazz,
	    TetraFunction<String, String, String, Class<T>, Route> action) {
	super(path, clazz);
	this.action = action;
    }

    public ThreeParamsAndFormRoute(String path, Class<T> clazz,
	    TetraFunction<String, String, String, Class<T>, Route> action) {
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
	    Function<String, TriFunction<String, String, Class<T>, Route>> newFunc = (
		    String var1) -> (String var2, String var3, Class<T> clz) -> action.apply(var1, var2, var3, clz);
	    return pathPrefix(segment(),
		    var1 -> new TwoParamsAndFormRoute<T>(newPath, getClazz(), newFunc.apply(var1)).get());
	}

	return pathPrefix(pathHead(), new ThreeParamsAndFormRoute<T>(newPath, getClazz(), action));
    }
}
