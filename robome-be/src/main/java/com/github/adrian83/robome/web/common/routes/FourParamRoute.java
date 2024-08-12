package com.github.adrian83.robome.web.common.routes;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.util.function.Function;
import java.util.function.Supplier;

import com.github.adrian83.robome.common.function.TetraFunction;
import com.github.adrian83.robome.common.function.TriFunction;

import akka.http.javadsl.server.Route;

public class FourParamRoute extends AbsRoute implements Supplier<Route> {

    private final TetraFunction<String, String, String, String, Route> action;

    public FourParamRoute(String[] path, TetraFunction<String, String, String, String, Route> action) {
        super(path);
        this.action = action;
    }

    public FourParamRoute(String path, TetraFunction<String, String, String, String, Route> action) {
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
            Function<String, TriFunction<String, String, String, Route>> newFunc = (
                    String var1) -> (String var2, String var3, String var4) -> action.apply(var1, var2, var3, var4);
            return pathPrefix(segment(), var1 -> new ThreeParamsRoute(newPath, newFunc.apply(var1)).get());
        }

        return pathPrefix(pathHead(), new FourParamRoute(newPath, action));
    }
}
