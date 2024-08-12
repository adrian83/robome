package com.github.adrian83.robome.web.common.routes;

import java.util.function.Supplier;

import akka.http.javadsl.server.Route;

public class PrefixRoute extends AbsRoute implements Supplier<Route> {

    private final Route route; 

    public PrefixRoute(String[] path, Route route) {
        super(path);
        this.route = route;
    }

    public PrefixRoute(String path, Route route) {
        super(path);
        this.route = route;
    }

    @Override
    public Route get() {
        if (emptyPath()) {
            return pathEndOrSingleSlash(() -> route);
        }

        if (startsWithParameter()) {
            throw new IllegalStateException("path should not contain any parameter");
        }

        return pathPrefix(pathHead(), new PrefixRoute(pathTail(), route));
    }
}
