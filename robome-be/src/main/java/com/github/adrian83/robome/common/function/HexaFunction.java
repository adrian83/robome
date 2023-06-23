package com.github.adrian83.robome.common.function;

@FunctionalInterface
public interface HexaFunction<P, R, S, T, U, V, W> {
    W apply(P param1, R param2, S param3, T param4, U param5, V param6);
}
