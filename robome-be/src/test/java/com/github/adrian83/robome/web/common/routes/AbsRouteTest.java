package com.github.adrian83.robome.web.common.routes;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AbsRouteTest {

    @Test
    public void testAbsRoute() {
	// when
	var absRoute = new AbsRoute("/a/b/c/d/");

	// then
	assertThat(absRoute.pathTail()).isEqualTo(new String[] { "b", "c", "d" });
	assertThat(absRoute.emptyPath()).isFalse();
	assertThat(absRoute.startsWithParameter()).isFalse();
	assertThat(absRoute.pathHead()).isEqualTo("a");
    }

    @Test
    public void testEmptyAbsRouteCase1() {
	// when
	var absRoute = new AbsRoute("/");

	// then
	assertThat(absRoute.pathTail()).isEmpty();
	assertThat(absRoute.emptyPath()).isTrue();
	assertThat(absRoute.startsWithParameter()).isFalse();
    }

    @Test
    public void testEmptyAbsRouteCase2() {
	// when
	var absRoute = new AbsRoute("");

	// then
	assertThat(absRoute.pathTail()).isEmpty();
	assertThat(absRoute.emptyPath()).isTrue();
	assertThat(absRoute.startsWithParameter()).isFalse();
    }

    @Test
    public void testAbsRouteWithPathParameter() {
	// when
	var absRoute = new AbsRoute("/{param}/a/b");

	// then
	assertThat(absRoute.pathTail()).isEqualTo(new String[] { "a", "b" });
	assertThat(absRoute.emptyPath()).isFalse();
	assertThat(absRoute.startsWithParameter()).isTrue();
	assertThat(absRoute.pathHead()).isEqualTo("{param}");
    }
}
