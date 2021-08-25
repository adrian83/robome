package com.github.adrian83.robome.web.health;

import static com.github.adrian83.robome.web.common.http.HttpMethod.GET;
import static com.github.adrian83.robome.web.common.http.HttpMethod.POST;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.web.common.Response;
import com.github.adrian83.robome.web.common.routes.PrefixRoute;
import com.github.adrian83.robome.web.health.model.AppStatus;
import com.google.inject.Inject;

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class HealthController extends AllDirectives {

  private static final Logger LOGGER = LoggerFactory.getLogger(HealthController.class);

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
    LOGGER.info("Checking application status: {}", OK);
    return complete(response.jsonFromObject(new AppStatus(OK)));
  }
}
