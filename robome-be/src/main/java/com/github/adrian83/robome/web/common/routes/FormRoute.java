package com.github.adrian83.robome.web.common.routes;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class FormRoute<T> extends AllDirectives implements Supplier<Route> {

  private Function<Class<T>, Route> action;
  private String[] path;
  private Class<T> clazz;

  public FormRoute(String[] path, Class<T> clazz, Function<Class<T>, Route> action) {
    this.clazz = clazz;
    this.path = path;
    this.action = action;
  }

  private boolean isParam(String pathElem) {
    return pathElem.charAt(0) == '{';
  }

  @Override
  public Route get() {

    if (path.length == 0) {
      return pathEndOrSingleSlash(() -> action.apply(clazz));
    }

    if (isParam(path[0])) {
      throw new IllegalStateException("path should not contain any parameter");
    }

    var newPath = Arrays.copyOfRange(path, 1, path.length);
    return pathPrefix(path[0], new FormRoute<T>(newPath, clazz, action));
  }
}
