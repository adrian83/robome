package com.github.adrian83.robome.web.common.routes;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class PrefixRouteTest {

    @Test
    public void shouldThrowExceptionIfPathContainsParameter() {
        // given
        var expectedRoute = new MyAppFragment().createRoute();

        var prefixRoute = new PrefixRoute("{param}/prefix2/", expectedRoute);

        // when & then
        assertThatThrownBy(() -> prefixRoute.get()).isInstanceOf(IllegalStateException.class);
    }

    private class MyAppFragment extends AllDirectives {

        public Route createRoute() {
            return get(() -> complete("Fragments of imagination"));
        }
    }
}
