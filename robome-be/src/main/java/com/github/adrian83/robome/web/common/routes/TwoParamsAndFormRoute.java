package com.github.adrian83.robome.web.common.routes;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.adrian83.robome.util.function.TriFunction;

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class TwoParamsAndFormRoute<T> extends AllDirectives implements Supplier<Route> {

  private TriFunction<String, String, Class<T>, Route> action;
  private String[] path;
  private Class<T> clazz;

  public TwoParamsAndFormRoute(
      String[] path, Class<T> clazz, TriFunction<String, String, Class<T>, Route> action) {
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
        throw new IllegalStateException("path should contains one parameter");
    }

    var newPath = Arrays.copyOfRange(path, 1, path.length);

    if (isParam(path[0])) {
      Function<String, BiFunction<String, Class<T>, Route>> newFunc =
          (String var1) -> (String var2, Class<T> clz) -> action.apply(var1, var2, clz);
      return pathPrefix(
          segment(), var1 -> new OneParamAndFormRoute<T>(newPath, clazz, newFunc.apply(var1)).get());
    }

    return pathPrefix(path[0], new TwoParamsAndFormRoute<T>(newPath, clazz, action));
  }
}
