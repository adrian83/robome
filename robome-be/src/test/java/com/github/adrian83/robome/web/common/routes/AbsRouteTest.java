package com.github.adrian83.robome.web.common.routes;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class AbsRouteTest {

  @Test
  public void testAbsRoute() {
    // when
    var absRoute = new AbsRoute("/a/b/c/d/");

    // then
    assertArrayEquals(new String[] {"b", "c", "d"}, absRoute.pathTail());
    assertFalse(absRoute.emptyPath());
    assertFalse(absRoute.startsWithParameter());
    assertEquals("a", absRoute.pathHead());
  }
  
  @Test
  public void testEmptyAbsRouteCase1() {
    // when
    var absRoute = new AbsRoute("/");

    // then
    assertArrayEquals(new String[] {}, absRoute.pathTail());
    assertTrue(absRoute.emptyPath());
    assertFalse(absRoute.startsWithParameter());
  }
  
  @Test
  public void testEmptyAbsRouteCase2() {
    // when
    var absRoute = new AbsRoute("");

    // then
    assertArrayEquals(new String[] {}, absRoute.pathTail());
    assertTrue(absRoute.emptyPath());
    assertFalse(absRoute.startsWithParameter());
  }
  
  @Test
  public void testAbsRouteWithPathParameter() {
    // when
    var absRoute = new AbsRoute("/{param}/a/b");

    // then
    assertArrayEquals(new String[] {"a", "b"}, absRoute.pathTail());
    assertFalse(absRoute.emptyPath());
    assertTrue(absRoute.startsWithParameter());
    assertEquals("{param}", absRoute.pathHead());
  }
  
}
