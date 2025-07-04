package com.github.adrian83.robome.web.common.routes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static akka.http.javadsl.server.Directives.pathPrefix;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;

public class RouteSupplier implements Supplier<Route> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteSupplier.class);

    private final List<String> pathParts;
    private final Function<Map<String, String>, Route> handler;

    public RouteSupplier(String pathTemplate, Function<Map<String, String>, Route> handler) {
        LOGGER.info("Creating RouteSupplier for path: {}", pathTemplate);
        this.pathParts = Arrays.asList(pathTemplate.split("/"))
                .stream()
                .filter(e -> !e.isEmpty())
                .map(String::strip)
                .toList();
        this.handler = handler;
    }

    @Override
    public Route get() {
        return buildRecursive(pathParts, new HashMap<>(), handler);
    }

    private Route buildRecursive(List<String> pathParts, Map<String, String> params, Function<Map<String, String>, Route> handler) {
        LOGGER.info("Building route for path parts: {}, with params: {}", pathParts, params);
        if (pathParts.isEmpty()) {
            return handler.apply(params);
        }

        String current = pathParts.get(0);
        List<String> rest = pathParts.subList(1, pathParts.size());

        LOGGER.info(current + " is current path element, rest: " + rest);

        return getParamName(current)
                .map(paramName -> pathPrefix(PathMatchers.segment(), value -> buildRecursive(rest, appendKeyValue(params, paramName, value), handler)))
                .orElseGet(() -> pathPrefix(current, () -> buildRecursive(rest, params, handler)));
    }

    private Map<String, String> appendKeyValue(Map<String, String> params, String key, String value) {
        Map<String, String> newParams = new HashMap<>(params);
        newParams.put(key, value);
        return newParams;
    }

    private Optional<String> getParamName(String pathElem) {
        if (isParam(pathElem)) {
            return Optional.of(pathElem.substring(1, pathElem.length() - 1));
        }
        return Optional.empty();
    }

    private boolean isParam(String pathElem) {
        return pathElem.charAt(0) == '{';
    }

}
