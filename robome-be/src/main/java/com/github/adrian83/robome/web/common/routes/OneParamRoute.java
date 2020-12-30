package com.github.adrian83.robome.web.common.routes;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.util.function.Function;
import java.util.function.Supplier;

import akka.http.javadsl.server.Route;

public class OneParamRoute extends AbsRoute implements Supplier<Route> {

  private Function<String, Route> action;

  public OneParamRoute(String[] path, Function<String, Route> action) {
    super(path);
    this.action = action;
  }

  public OneParamRoute(String path, Function<String, Route> action) {
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
      Function<String, Route> newFunc = (String var1) -> action.apply(var1);
      return pathPrefix(segment(), var1 -> new PrefixRoute(newPath, newFunc.apply(var1)).get());
    }

    return pathPrefix(pathHead(), new OneParamRoute(newPath, action));
  }
}
