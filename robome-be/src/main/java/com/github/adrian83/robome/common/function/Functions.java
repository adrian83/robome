package com.github.adrian83.robome.common.function;

import java.util.function.Consumer;
import java.util.function.Function;

public class Functions {

    public static <T> Function<T, T> use(Consumer<T> c) {
        return (e) -> {
            c.accept(e);
            return e;
        };
    }
}
