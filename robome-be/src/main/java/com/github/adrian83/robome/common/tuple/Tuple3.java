package com.github.adrian83.robome.common.tuple;

public class Tuple3<T1, T2, T3> extends Tuple2<T1, T2> {

  private T3 obj3;

  public Tuple3(T1 obj1, T2 obj2, T3 obj3) {
    super(obj1, obj2);
    this.obj3 = obj3;
  }

  public Tuple3(Tuple2<T1, T2> tuple2, T3 obj3) {
    this(tuple2.getObj1(), tuple2.getObj2(), obj3);
  }

  public T3 getObj3() {
    return obj3;
  }
}
