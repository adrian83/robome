package com.github.adrian83.robome.common.tuple;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString()
@EqualsAndHashCode()
public class Tuple2<T1, T2> {
  private T1 obj1;
  private T2 obj2;
}
