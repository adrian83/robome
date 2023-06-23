package com.github.adrian83.robome.web.common.routes;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.adrian83.robome.common.function.TriFunction;

import akka.http.javadsl.server.Route;

public class ThreeParamsRoute extends AbsRoute implements Supplier<Route> {

    private TriFunction<String, String, String, Route> action;

    public ThreeParamsRoute(String[] path, TriFunction<String, String, String, Route> action) {
	super(path);
	this.action = action;
    }

    public ThreeParamsRoute(String path, TriFunction<String, String, String, Route> action) {
	super(path);
	this.action = action;
    }

    @Override
    public Route get() {

	if (emptyPath()) {
	    throw new IllegalStateException("path should contains one parameter");
	}

	var newPath = pathTail();

	if (startsWithParameter()) {
	    Function<String, BiFunction<String, String, Route>> newFunc = (
		    String var1) -> (String var2, String var3) -> action.apply(var1, var2, var3);
	    return pathPrefix(segment(), var1 -> new TwoParamsRoute(newPath, newFunc.apply(var1)).get());
	}

	return pathPrefix(pathHead(), new ThreeParamsRoute(newPath, action));
    }
}
