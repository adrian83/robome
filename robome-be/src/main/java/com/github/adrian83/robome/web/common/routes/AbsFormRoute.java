package com.github.adrian83.robome.web.common.routes;

import lombok.Getter;

@Getter
public class AbsFormRoute<T> extends AbsRoute {

  private Class<T> clazz;

  public AbsFormRoute(String[] path, Class<T> clazz) {
    super(path);
    this.clazz = clazz;
  }

  public AbsFormRoute(String path, Class<T> clazz) {
    super(path);
    this.clazz = clazz;
  }
}
