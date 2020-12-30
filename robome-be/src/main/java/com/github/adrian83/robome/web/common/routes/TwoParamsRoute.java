package com.github.adrian83.robome.web.common.routes;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import akka.http.javadsl.server.Route;

public class TwoParamsRoute extends AbsRoute implements Supplier<Route> {

  private BiFunction<String, String, Route> action;

  public TwoParamsRoute(String[] path, BiFunction<String, String, Route> action) {
    super(path);
    this.action = action;
  }

  public TwoParamsRoute(String path, BiFunction<String, String, Route> action) {
    super(path);
    this.action = action;
  }

  @Override
  public Route get() {

    if (emptyPath()) {
      throw new IllegalStateException("path should contains one parameter");
    }

    var newPath = this.pathTail();

    if (startsWithParameter()) {
      Function<String, Function<String, Route>> newFunc =
          (String var1) -> (String var2) -> action.apply(var1, var2);
      return pathPrefix(segment(), var1 -> new OneParamRoute(newPath, newFunc.apply(var1)).get());
    }

    return pathPrefix(pathHead(), new TwoParamsRoute(newPath, action));
  }
}
