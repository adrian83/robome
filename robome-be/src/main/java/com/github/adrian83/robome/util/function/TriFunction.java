package com.github.adrian83.robome.util.function;

@FunctionalInterface
public interface TriFunction<R, S, T, U> {

  U apply(R param1, S param2, T param3);
}
