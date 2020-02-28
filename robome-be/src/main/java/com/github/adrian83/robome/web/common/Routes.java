package com.github.adrian83.robome.web.common;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.adrian83.robome.util.function.TetraFunction;
import com.github.adrian83.robome.util.function.TriFunction;

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class Routes extends AllDirectives {

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
    Function<String, Route> funcWithSlash =
        (String var1) -> pathEndOrSingleSlash(() -> action.apply(var1));
    return prefixVar(prefix, funcWithSlash);
  }

  private Supplier<Route> prefixVarPrefix(
      String prefix1, String prefix2, Function<String, Route> action) {
    Route route =
        pathPrefix(
            prefix1,
            () -> pathPrefix(segment(), (var1) -> pathPrefix(prefix2, () -> action.apply(var1))));
    return () -> route;
  }

  public Supplier<Route> prefixVarPrefixSlash(
      String prefix1, String prefix2, Function<String, Route> action) {
    Function<String, Route> funcWithSlash =
        (String var1) -> pathEndOrSingleSlash(() -> action.apply(var1));
    return prefixVarPrefix(prefix1, prefix2, funcWithSlash);
  }

  private <T> Supplier<Route> prefixVarForm(
      String prefix, Class<T> clazz, BiFunction<String, Class<T>, Route> action) {
    Route route = pathPrefix(prefix, () -> pathPrefix(segment(), val -> action.apply(val, clazz)));
    return () -> route;
  }

  public <T> Supplier<Route> prefixVarFormSlash(
      String prefix, Class<T> clz, BiFunction<String, Class<T>, Route> action) {
    BiFunction<String, Class<T>, Route> funcWithSlash =
        (String val, Class<T> clazz) -> pathEndOrSingleSlash(() -> action.apply(val, clazz));
    return prefixVarForm(prefix, clz, funcWithSlash);
  }

  private <T> Supplier<Route> prefixForm(
      String prefix, Class<T> clazz, Function<Class<T>, Route> action) {
    Route route = pathPrefix(prefix, () -> pathEndOrSingleSlash(() -> action.apply(clazz)));
    return () -> route;
  }

  public <T> Supplier<Route> prefixFormSlash(
      String prefix, Class<T> clz, Function<Class<T>, Route> action) {
    Function<Class<T>, Route> funcWithSlash =
        (Class<T> clazz) -> pathEndOrSingleSlash(() -> action.apply(clazz));
    return prefixForm(prefix, clz, funcWithSlash);
  }

  private <T> Supplier<Route> prefixPrefixForm(
      String prefix1, String prefix2, Class<T> clazz, Function<Class<T>, Route> action) {
    return () -> pathPrefix(prefix1, () -> pathPrefix(prefix2, () -> action.apply(clazz)));
  }

  public <T> Supplier<Route> prefixPrefixFormSlash(
      String prefix1, String prefix2, Class<T> clz, Function<Class<T>, Route> action) {
    Function<Class<T>, Route> funcWithSlash =
        (Class<T> clazz) -> pathEndOrSingleSlash(() -> action.apply(clazz));
    return prefixPrefixForm(prefix1, prefix2, clz, funcWithSlash);
  }

  public Supplier<Route> prefixVarPrefixVar(
      String prefix1, String prefix2, BiFunction<String, String, Route> action) {
    Function<String, Function<String, Route>> funcWithSlash =
        (String var1) -> (String var2) -> action.apply(var1, var2);
    Function<String, Route> jj =
        (String var2) -> prefixVarSlash(prefix2, funcWithSlash.apply(var2)).get();
    return prefixVar(prefix1, jj);
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
    Function<String, BiFunction<String, String, Route>> funcWithSlash =
        (String var1) -> (String var2, String var3) -> action.apply(var1, var2, var3);
    Function<String, Route> jj =
        (String var1) -> prefixVarPrefixVarSlash(prefix2, prefix3, funcWithSlash.apply(var1)).get();
    return prefixVar(prefix1, jj);
  }

  private <T> Supplier<Route> prefixVarPrefixForm(
      String prefix1, String prefix2, Class<T> clazz, BiFunction<String, Class<T>, Route> action) {
    Function<String, Function<Class<T>, Route>> func =
        (String var1) -> (Class<T> clz) -> action.apply(var1, clz);
    Function<String, Route> gg =
        (String var1) -> prefixForm(prefix2, clazz, func.apply(var1)).get();
    return prefixVar(prefix1, gg);
  }

  public <T> Supplier<Route> prefixVarPrefixFormSlash(
      String prefix1, String prefix2, Class<T> clazz, BiFunction<String, Class<T>, Route> action) {
    BiFunction<String, Class<T>, Route> wrapedAction =
        (String var1, Class<T> clz) -> pathEndOrSingleSlash(() -> action.apply(var1, clz));
    return prefixVarPrefixForm(prefix1, prefix2, clazz, wrapedAction);
  }

  private <T> Supplier<Route> prefixVarPrefixVarForm(
      String prefix1,
      String prefix2,
      Class<T> clazz,
      TriFunction<String, String, Class<T>, Route> action) {
    Function<String, BiFunction<String, Class<T>, Route>> func =
        (String var1) -> (String var2, Class<T> clz) -> action.apply(var1, var2, clz);
    Function<String, Route> gg =
        (String var1) -> prefixVarForm(prefix2, clazz, func.apply(var1)).get();
    return prefixVar(prefix1, gg);
  }

  public <T> Supplier<Route> prefixVarPrefixVarFormSlash(
      String prefix1,
      String prefix2,
      Class<T> clazz,
      TriFunction<String, String, Class<T>, Route> action) {
    TriFunction<String, String, Class<T>, Route> wrapedAction =
        (String var1, String var2, Class<T> clz) ->
            pathEndOrSingleSlash(() -> action.apply(var1, var2, clz));
    return prefixVarPrefixVarForm(prefix1, prefix2, clazz, wrapedAction);
  }

  public <T> Supplier<Route> prefixVarPrefixVarPrefixFormSlash(
      String prefix1,
      String prefix2,
      String prefix3,
      Class<T> clazz,
      TriFunction<String, String, Class<T>, Route> action) {
    Function<String, BiFunction<String, Class<T>, Route>> func =
        (String var1) ->
            (String var2, Class<T> clz) ->
                pathEndOrSingleSlash(() -> action.apply(var1, var2, clz));
    Function<String, Route> ggg =
        (String var1) -> prefixVarPrefixFormSlash(prefix2, prefix3, clazz, func.apply(var1)).get();
    return prefixVar(prefix1, ggg);
  }

  public Supplier<Route> prefixVarPrefixVarPrefixSlash(
      String prefix1, String prefix2, String prefix3, BiFunction<String, String, Route> action) {
    Function<String, Function<String, Route>> func =
        (String var1) -> (String var2) -> pathEndOrSingleSlash(() -> action.apply(var1, var2));
    Function<String, Route> ggg =
        (String var2) -> prefixVarPrefix(prefix2, prefix3, func.apply(var2)).get();
    return prefixVar(prefix1, ggg);
  }

  public <T> Supplier<Route> prefixVarPrefixVarPrefixVarFormSlash(
      String prefix1,
      String prefix2,
      String prefix3,
      Class<T> clazz,
      TetraFunction<String, String, String, Class<T>, Route> action) {
    Function<String, TriFunction<String, String, Class<T>, Route>> f =
        (String var1) ->
            (String var2, String var3, Class<T> clz) -> action.apply(var1, var2, var3, clz);
    Function<String, Route> ff =
        (String var1) -> prefixVarPrefixVarFormSlash(prefix2, prefix3, clazz, f.apply(var1)).get();
    return prefixVar(prefix1, ff);
  }
}
