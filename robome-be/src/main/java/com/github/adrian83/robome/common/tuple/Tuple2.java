package com.github.adrian83.robome.common.tuple;

public class Tuple2<T1, T2> {

  private T1 obj1;
  private T2 obj2;

  public Tuple2(T1 obj1, T2 obj2) {
    super();
    this.obj1 = obj1;
    this.obj2 = obj2;
  }

  public T1 getObj1() {
    return obj1;
  }

  public T2 getObj2() {
    return obj2;
  }
}
