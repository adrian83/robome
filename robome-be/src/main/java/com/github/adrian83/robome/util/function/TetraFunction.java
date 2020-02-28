package com.github.adrian83.robome.util.function;

@FunctionalInterface
public interface TetraFunction<P, R, S, T, U> {

  U apply(P param1, R param2, S param3, T param4);
}
