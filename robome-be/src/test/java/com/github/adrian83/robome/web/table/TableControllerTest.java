package com.github.adrian83.robome.web.table;

import java.util.UUID;

import org.mockito.Mockito;

import com.github.adrian83.robome.domain.table.TableService;
import com.github.adrian83.robome.web.common.Response;
import com.github.adrian83.robome.web.common.Security;

import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;

public class TableControllerTest extends JUnitRouteTest {

  private TableService tableServiceMock = Mockito.mock(TableService.class);
  private Response responseMock = Mockito.mock(Response.class);
  private Security securityMock = Mockito.mock(Security.class);

  private TableController tableController =
      new TableController(tableServiceMock, responseMock, securityMock);

  private TestRoute appRoute = testRoute(tableController.createRoute());

  
  //@Test
  public void shoultTestOptionsWithTableIdParam() {

    // when
    appRoute
        .run(
            HttpRequest.OPTIONS(
                "/users/"
                    + UUID.randomUUID().toString()
                    + "/tables/"
                    + UUID.randomUUID().toString()
                    + "/"))
        .assertStatusCode(200)
        .assertEntity("Fragments of imagination");
  }
//
//  @Override
//  public Materializer materializer() {
//    return Materializer.createMaterializer(system);
//  }
//
//  @Override
//  public ActorSystem system() {
//    return system;
//  }
}
