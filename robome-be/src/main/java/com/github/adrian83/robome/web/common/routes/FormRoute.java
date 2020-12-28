package com.github.adrian83.robome.web.common.routes;

import java.util.function.Function;
import java.util.function.Supplier;

import akka.http.javadsl.server.Route;

public class FormRoute<T> extends AbsFormRoute<T> implements Supplier<Route> {

  private Function<Class<T>, Route> action;

  public FormRoute(String[] path, Class<T> clazz, Function<Class<T>, Route> action) {
    super(path, clazz);
    this.action = action;
  }

  public FormRoute(String path, Class<T> clazz, Function<Class<T>, Route> action) {
	    super(path, clazz);
	    this.action = action;
	  }
  
  @Override
  public Route get() {
    if (emptyPath()) {
      return pathEndOrSingleSlash(() -> action.apply(getClazz()));
    }

    if (startsWithParameter()) {
      throw new IllegalStateException("path should not contain any parameter");
    }

    return pathPrefix(pathHead(), new FormRoute<T>(pathTail(), getClazz(), action));
  }
}
