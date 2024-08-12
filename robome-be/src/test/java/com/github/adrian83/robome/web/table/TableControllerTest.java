package com.github.adrian83.robome.web.table;

import java.util.UUID;

import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.github.adrian83.robome.domain.table.TableService;
import com.github.adrian83.robome.web.common.Response;
import com.github.adrian83.robome.web.common.Security;

import akka.actor.ActorSystem;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.stream.ActorMaterializer;

public class TableControllerTest extends JUnitRouteTest {

    private final TableService tableServiceMock = Mockito.mock(TableService.class);
    private final Response responseMock = Mockito.mock(Response.class);
    private final Security securityMock = Mockito.mock(Security.class);

    private final TableController tableController = new TableController(tableServiceMock, responseMock, securityMock);

    private final ActorSystem actorSystem = ActorSystem.create("test-system");
    private final ActorMaterializer materializer = ActorMaterializer.create(actorSystem);

    @ClassRule
    public static TestKitJunitResource testkit = new TestKitJunitResource();

    @Test
    public void shoultTestOptionsWithTableIdParam() {
        // given
        var userId = UUID.randomUUID().toString();
        var tableId = UUID.randomUUID().toString();

        var path = "/users/" + userId + "/tables/" + tableId + "/";

        // TestRoute appRoute = testRoute(tableController.createRoute());
        var route = tableController.createRoute();

        // when
        // testRoute(route)
        //         .run(HttpRequest.OPTIONS(path))
        //         .assertStatusCode(200);
    }

    @Override
    public ActorSystem system() {
        return actorSystem;
    }

    @Override
    public ActorMaterializer materializer() {
        return materializer;
    }

}
