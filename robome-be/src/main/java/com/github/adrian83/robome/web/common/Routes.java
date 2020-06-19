package com.github.adrian83.robome.web.common;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.adrian83.robome.util.function.TetraFunction;
import com.github.adrian83.robome.util.function.TriFunction;
import com.github.adrian83.robome.util.tuple.Tuple2;
import com.github.adrian83.robome.util.tuple.Tuple3;

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public final class Routes extends AllDirectives {

  private Supplier<Route> prefix(String prefix, Route route) {
    return () -> pathPrefix(prefix, () -> route);
  }

  public Supplier<Route> prefixSlash(String prefix, Route route) {
    return prefix(prefix, pathEndOrSingleSlash(() -> route));
  }

  private Supplier<Route> prefixPrefix(Tuple2<String, String> prefixes, Route route) {
    return () -> pathPrefix(prefixes.getObj1(), () -> pathPrefix(prefixes.getObj2(), () -> route));
  }

  public Supplier<Route> prefixPrefixSlash(Tuple2<String, String> prefixes, Route route) {
    return prefixPrefix(prefixes, pathEndOrSingleSlash(() -> route));
  }

  private Supplier<Route> prefixVar(String prefix, Function<String, Route> action) {
    return () -> pathPrefix(prefix, () -> pathPrefix(segment(), (var1) -> action.apply(var1)));
  }

  public Supplier<Route> prefixVarSlash(String prefix, Function<String, Route> action) {

    Function<String, Route> inner = (String var1) -> pathEndOrSingleSlash(() -> action.apply(var1));

    return prefixVar(prefix, inner);
  }

  private Supplier<Route> prefixVarPrefix(
      Tuple2<String, String> prefixes, Function<String, Route> action) {

    return () ->
        pathPrefix(
            prefixes.getObj1(),
            () ->
                pathPrefix(
                    segment(), (var1) -> pathPrefix(prefixes.getObj2(), () -> action.apply(var1))));
  }

  public Supplier<Route> prefixVarPrefixSlash(
      Tuple2<String, String> prefixes, Function<String, Route> action) {

    Function<String, Route> inner = (String var1) -> pathEndOrSingleSlash(() -> action.apply(var1));

    return prefixVarPrefix(prefixes, inner);
  }

  private <T> Supplier<Route> prefixVarForm(
      String prefix, Class<T> clazz, BiFunction<String, Class<T>, Route> action) {

    return () -> pathPrefix(prefix, () -> pathPrefix(segment(), var1 -> action.apply(var1, clazz)));
  }

  public <T> Supplier<Route> prefixVarFormSlash(
      String prefix, Class<T> clazz, BiFunction<String, Class<T>, Route> action) {

    BiFunction<String, Class<T>, Route> inner =
        (String var1, Class<T> clz) -> pathEndOrSingleSlash(() -> action.apply(var1, clz));

    return prefixVarForm(prefix, clazz, inner);
  }

  private <T> Supplier<Route> prefixForm(
      String prefix, Class<T> clazz, Function<Class<T>, Route> action) {

    return () -> pathPrefix(prefix, () -> pathEndOrSingleSlash(() -> action.apply(clazz)));
  }

  public <T> Supplier<Route> prefixFormSlash(
      String prefix, Class<T> clazz, Function<Class<T>, Route> action) {

    Function<Class<T>, Route> inner =
        (Class<T> clz) -> pathEndOrSingleSlash(() -> action.apply(clz));

    return prefixForm(prefix, clazz, inner);
  }

  private <T> Supplier<Route> prefixPrefixForm(
      Tuple2<String, String> prefixes, Class<T> clazz, Function<Class<T>, Route> action) {

    return () ->
        pathPrefix(
            prefixes.getObj1(), () -> pathPrefix(prefixes.getObj2(), () -> action.apply(clazz)));
  }

  public <T> Supplier<Route> prefixPrefixFormSlash(
      Tuple2<String, String> prefixes, Class<T> clazz, Function<Class<T>, Route> action) {

    Function<Class<T>, Route> inner =
        (Class<T> clz) -> pathEndOrSingleSlash(() -> action.apply(clz));

    return prefixPrefixForm(prefixes, clazz, inner);
  }

  public Supplier<Route> prefixVarPrefixVar(
      Tuple2<String, String> prefixes, BiFunction<String, String, Route> action) {

    Function<String, Function<String, Route>> curried =
        (String var1) -> (String var2) -> action.apply(var1, var2);

    Function<String, Route> inner =
        (String var2) -> prefixVarSlash(prefixes.getObj2(), curried.apply(var2)).get();

    return prefixVar(prefixes.getObj1(), inner);
  }

  public Supplier<Route> prefixVarPrefixVarSlash(
      Tuple2<String, String> prefixes, BiFunction<String, String, Route> action) {

    BiFunction<String, String, Route> wrapedAction =
        (String prf1, String prf2) -> pathEndOrSingleSlash(() -> action.apply(prf1, prf2));

    return prefixVarPrefixVar(prefixes, wrapedAction);
  }

  public Supplier<Route> prefixVarPrefixVarPrefixVarSlash(
      Tuple3<String, String, String> prefixes, TriFunction<String, String, String, Route> action) {

    Function<String, BiFunction<String, String, Route>> curried =
        (String var1) -> (String var2, String var3) -> action.apply(var1, var2, var3);

    Tuple2<String, String> prefixes2 = new Tuple2<>(prefixes.getObj2(), prefixes.getObj3());

    Function<String, Route> inner =
        (String var1) -> prefixVarPrefixVarSlash(prefixes2, curried.apply(var1)).get();

    return prefixVar(prefixes.getObj1(), inner);
  }

  public <T> Supplier<Route> prefixVarPrefixFormSlash(
      Tuple2<String, String> prefixes, Class<T> clazz, BiFunction<String, Class<T>, Route> action) {

    Function<String, Function<Class<T>, Route>> curried =
        (String var1) -> (Class<T> clz) -> action.apply(var1, clz);

    Function<String, Route> inner =
        (String var1) -> prefixFormSlash(prefixes.getObj2(), clazz, curried.apply(var1)).get();

    return prefixVar(prefixes.getObj1(), inner);
  }

  public <T> Supplier<Route> prefixVarPrefixVarFormSlash(
      Tuple2<String, String> prefixes,
      Class<T> clazz,
      TriFunction<String, String, Class<T>, Route> action) {

    Function<String, BiFunction<String, Class<T>, Route>> curried =
        (String var1) -> (String var2, Class<T> clz) -> action.apply(var1, var2, clz);

    Function<String, Route> inner =
        (String var1) -> prefixVarFormSlash(prefixes.getObj2(), clazz, curried.apply(var1)).get();

    return prefixVar(prefixes.getObj1(), inner);
  }

  public <T> Supplier<Route> prefixVarPrefixVarPrefixFormSlash(
      Tuple3<String, String, String> prefixes,
      Class<T> clazz,
      TriFunction<String, String, Class<T>, Route> action) {

    Function<String, BiFunction<String, Class<T>, Route>> curried =
        (String var1) ->
            (String var2, Class<T> clz) ->
                pathEndOrSingleSlash(() -> action.apply(var1, var2, clz));

    Tuple2<String, String> prefixes2 = new Tuple2<>(prefixes.getObj2(), prefixes.getObj3());

    Function<String, Route> inner =
        (String var1) -> prefixVarPrefixFormSlash(prefixes2, clazz, curried.apply(var1)).get();

    return prefixVar(prefixes.getObj1(), inner);
  }

  public Supplier<Route> prefixVarPrefixVarPrefixSlash(
      Tuple3<String, String, String> prefixes, BiFunction<String, String, Route> action) {

    Function<String, Function<String, Route>> curried =
        (String var1) -> (String var2) -> pathEndOrSingleSlash(() -> action.apply(var1, var2));

    Tuple2<String, String> prefixes2 = new Tuple2<>(prefixes.getObj2(), prefixes.getObj3());

    Function<String, Route> inner =
        (String var2) -> prefixVarPrefix(prefixes2, curried.apply(var2)).get();

    return prefixVar(prefixes.getObj1(), inner);
  }

  public <T> Supplier<Route> prefixVarPrefixVarPrefixVarFormSlash(
      Tuple3<String, String, String> prefixes,
      Class<T> clazz,
      TetraFunction<String, String, String, Class<T>, Route> action) {

    Function<String, TriFunction<String, String, Class<T>, Route>> curried =
        (String var1) ->
            (String var2, String var3, Class<T> clz) -> action.apply(var1, var2, var3, clz);

    Tuple2<String, String> prefixes2 = new Tuple2<>(prefixes.getObj2(), prefixes.getObj3());

    Function<String, Route> inner =
        (String var1) -> prefixVarPrefixVarFormSlash(prefixes2, clazz, curried.apply(var1)).get();

    return prefixVar(prefixes.getObj1(), inner);
  }
}
