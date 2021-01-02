package com.github.adrian83.robome.common.tuple;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Tuple3<T1, T2, T3> extends Tuple2<T1, T2> {
  private T3 obj3;
}
