package com.github.adrian83.robome.web.health;

import static com.github.adrian83.robome.util.http.HttpMethod.GET;
import static com.github.adrian83.robome.util.http.HttpMethod.POST;

import com.github.adrian83.robome.common.web.Response;
import com.github.adrian83.robome.web.common.Routes;
import com.github.adrian83.robome.web.health.model.AppStatus;
import com.google.inject.Inject;

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class HealthController extends AllDirectives {

  public static final String HEALTH = "health";

  private Response response;
  private Routes routes;

  @Inject
  public HealthController(Response response, Routes routes) {
    this.routes = routes;
    this.response = response;
  }

  public Route createRoute() {
    return route(
        get(routes.prefixSlash(HEALTH, createAppStatus())),
        options(routes.prefixSlash(HEALTH, handleOptionsRequest())));
  }

  private Route createAppStatus() {
    return complete(response.jsonFromObject(new AppStatus("OK")));
  }

  private Route handleOptionsRequest() {
    return complete(response.response200(GET, POST));
  }
}
