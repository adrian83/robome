package com.github.adrian83.robome.web.common.routes;

import java.util.Arrays;

import akka.http.javadsl.server.AllDirectives;

public class AbsRoute extends AllDirectives {

    private final String[] path;

    public AbsRoute(String[] path) {
        this.path = path;
    }

    public AbsRoute(String path) {
        this(Arrays.stream(path.split("/")).map(String::strip).filter(e -> e.length() > 0).toArray(String[]::new));
    }

    private boolean isParam(String pathElem) {
        return pathElem.charAt(0) == '{';
    }

    public boolean emptyPath() {
        return path.length == 0;
    }

    public boolean startsWithParameter() {
        return !emptyPath() && isParam(path[0]);
    }

    public String[] pathTail() {
        return emptyPath() ? new String[]{} : Arrays.copyOfRange(path, 1, path.length);
    }

    public String pathHead() {
        return path[0];
    }
}
