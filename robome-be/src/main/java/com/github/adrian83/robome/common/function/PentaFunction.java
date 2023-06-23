package com.github.adrian83.robome.common.function;

@FunctionalInterface
public interface PentaFunction<P, R, S, T, U, V> {
    V apply(P param1, R param2, S param3, T param4, U param5);
}
