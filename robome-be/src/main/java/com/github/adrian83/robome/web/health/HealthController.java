package com.github.adrian83.robome.web.health;

import static com.github.adrian83.robome.util.http.HttpMethod.GET;
import static com.github.adrian83.robome.util.http.HttpMethod.POST;

import com.github.adrian83.robome.web.common.Response;
import com.github.adrian83.robome.web.common.routes.PrefixRoute;
import com.github.adrian83.robome.web.health.model.AppStatus;
import com.google.inject.Inject;

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class HealthController extends AllDirectives {

  public static final String HEALTH = "health";
  public static final String OK = "OK";

  private Response response;

  @Inject
  public HealthController(Response response) {
    this.response = response;
  }

  public Route createRoute() {
    return route(
        get(new PrefixRoute(HEALTH, createAppStatus())),
        options(new PrefixRoute(HEALTH, complete(response.response200(GET, POST)))));
  }

  private Route createAppStatus() {
    return complete(response.jsonFromObject(new AppStatus(OK)));
  }
}
