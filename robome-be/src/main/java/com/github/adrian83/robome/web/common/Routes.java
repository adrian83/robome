package com.github.adrian83.robome.web.common;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.adrian83.robome.util.function.TetraFunction;
import com.github.adrian83.robome.util.function.TriFunction;

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public final class Routes extends AllDirectives {

  private Supplier<Route> prefix(String prefix, Route route) {
    return () -> pathPrefix(prefix, () -> route);
  }

  public Supplier<Route> prefixSlash(String prefix, Route route) {
    return prefix(prefix, pathEndOrSingleSlash(() -> route));
  }

  private Supplier<Route> prefixPrefix(String prefix1, String prefix2, Route route) {
    return () -> pathPrefix(prefix1, () -> pathPrefix(prefix2, () -> route));
  }

  public Supplier<Route> prefixPrefixSlash(String prefix1, String prefix2, Route route) {
    return prefixPrefix(prefix1, prefix2, pathEndOrSingleSlash(() -> route));
  }

  private Supplier<Route> prefixVar(String prefix, Function<String, Route> action) {
    return () -> pathPrefix(prefix, () -> pathPrefix(segment(), (var1) -> action.apply(var1)));
  }

  public Supplier<Route> prefixVarSlash(String prefix, Function<String, Route> action) {
    Function<String, Route> inner = (String var1) -> pathEndOrSingleSlash(() -> action.apply(var1));
    return prefixVar(prefix, inner);
  }

  private Supplier<Route> prefixVarPrefix(
      String prefix1, String prefix2, Function<String, Route> action) {
    return () ->
        pathPrefix(
            prefix1,
            () -> pathPrefix(segment(), (var1) -> pathPrefix(prefix2, () -> action.apply(var1))));
  }

  public Supplier<Route> prefixVarPrefixSlash(
      String prefix1, String prefix2, Function<String, Route> action) {
    Function<String, Route> inner = (String var1) -> pathEndOrSingleSlash(() -> action.apply(var1));
    return prefixVarPrefix(prefix1, prefix2, inner);
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
      String prefix1, String prefix2, Class<T> clazz, Function<Class<T>, Route> action) {
    return () -> pathPrefix(prefix1, () -> pathPrefix(prefix2, () -> action.apply(clazz)));
  }

  public <T> Supplier<Route> prefixPrefixFormSlash(
      String prefix1, String prefix2, Class<T> clazz, Function<Class<T>, Route> action) {
    Function<Class<T>, Route> inner =
        (Class<T> clz) -> pathEndOrSingleSlash(() -> action.apply(clz));
    return prefixPrefixForm(prefix1, prefix2, clazz, inner);
  }

  public Supplier<Route> prefixVarPrefixVar(
      String prefix1, String prefix2, BiFunction<String, String, Route> action) {
    Function<String, Function<String, Route>> curried =
        (String var1) -> (String var2) -> action.apply(var1, var2);
    Function<String, Route> inner =
        (String var2) -> prefixVarSlash(prefix2, curried.apply(var2)).get();
    return prefixVar(prefix1, inner);
  }

  public Supplier<Route> prefixVarPrefixVarSlash(
      String prefix1, String prefix2, BiFunction<String, String, Route> action) {
    BiFunction<String, String, Route> wrapedAction =
        (String prf1, String prf2) -> pathEndOrSingleSlash(() -> action.apply(prf1, prf2));
    return prefixVarPrefixVar(prefix1, prefix2, wrapedAction);
  }

  public Supplier<Route> prefixVarPrefixVarPrefixVarSlash(
      String prefix1,
      String prefix2,
      String prefix3,
      TriFunction<String, String, String, Route> action) {
    Function<String, BiFunction<String, String, Route>> curried =
        (String var1) -> (String var2, String var3) -> action.apply(var1, var2, var3);
    Function<String, Route> inner =
        (String var1) -> prefixVarPrefixVarSlash(prefix2, prefix3, curried.apply(var1)).get();
    return prefixVar(prefix1, inner);
  }

  public <T> Supplier<Route> prefixVarPrefixFormSlash(
      String prefix1, String prefix2, Class<T> clazz, BiFunction<String, Class<T>, Route> action) {
    Function<String, Function<Class<T>, Route>> curried =
        (String var1) -> (Class<T> clz) -> action.apply(var1, clz);
    Function<String, Route> inner =
        (String var1) -> prefixFormSlash(prefix2, clazz, curried.apply(var1)).get();
    return prefixVar(prefix1, inner);
  }

  public <T> Supplier<Route> prefixVarPrefixVarFormSlash(
      String prefix1,
      String prefix2,
      Class<T> clazz,
      TriFunction<String, String, Class<T>, Route> action) {
    Function<String, BiFunction<String, Class<T>, Route>> curried =
        (String var1) -> (String var2, Class<T> clz) -> action.apply(var1, var2, clz);
    Function<String, Route> inner =
        (String var1) -> prefixVarFormSlash(prefix2, clazz, curried.apply(var1)).get();
    return prefixVar(prefix1, inner);
  }

  public <T> Supplier<Route> prefixVarPrefixVarPrefixFormSlash(
      String prefix1,
      String prefix2,
      String prefix3,
      Class<T> clazz,
      TriFunction<String, String, Class<T>, Route> action) {
    Function<String, BiFunction<String, Class<T>, Route>> curried =
        (String var1) ->
            (String var2, Class<T> clz) ->
                pathEndOrSingleSlash(() -> action.apply(var1, var2, clz));
    Function<String, Route> inner =
        (String var1) ->
            prefixVarPrefixFormSlash(prefix2, prefix3, clazz, curried.apply(var1)).get();
    return prefixVar(prefix1, inner);
  }

  public Supplier<Route> prefixVarPrefixVarPrefixSlash(
      String prefix1, String prefix2, String prefix3, BiFunction<String, String, Route> action) {
    Function<String, Function<String, Route>> curried =
        (String var1) -> (String var2) -> pathEndOrSingleSlash(() -> action.apply(var1, var2));
    Function<String, Route> inner =
        (String var2) -> prefixVarPrefix(prefix2, prefix3, curried.apply(var2)).get();
    return prefixVar(prefix1, inner);
  }

  public <T> Supplier<Route> prefixVarPrefixVarPrefixVarFormSlash(
      String prefix1,
      String prefix2,
      String prefix3,
      Class<T> clazz,
      TetraFunction<String, String, String, Class<T>, Route> action) {
    Function<String, TriFunction<String, String, Class<T>, Route>> curried =
        (String var1) ->
            (String var2, String var3, Class<T> clz) -> action.apply(var1, var2, var3, clz);
    Function<String, Route> inner =
        (String var1) ->
            prefixVarPrefixVarFormSlash(prefix2, prefix3, clazz, curried.apply(var1)).get();
    return prefixVar(prefix1, inner);
  }
}
