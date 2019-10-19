package com.github.adrian83.robome.web.health;

import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.common.web.ExceptionHandler;
import com.github.adrian83.robome.common.web.Response;
import com.github.adrian83.robome.web.common.AbstractController;
import com.github.adrian83.robome.web.common.Routes;
import com.github.adrian83.robome.web.health.model.AppStatus;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.http.javadsl.server.Route;

public class HealthController extends AbstractController {

  public static final String HEALTH = "health";

  private Routes routes;
  
  @Inject
  public HealthController(
      JwtAuthorizer jwtAuthorizer,
      Config config,
      ExceptionHandler exceptionHandler,
      Response response,
      Routes routes) {
    super(jwtAuthorizer, exceptionHandler, config, response);
    this.routes = routes;
  }

  public Route createRoute() {
    return route(get(routes.prefixSlash(HEALTH, createAppStatus())));
  }

  private Route createAppStatus() {
    return complete(responseProducer.jsonFromObject(new AppStatus("OK")));
  }
}
