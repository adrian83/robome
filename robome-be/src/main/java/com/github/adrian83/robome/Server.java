package com.github.adrian83.robome;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.web.activity.ActivityController;
import com.github.adrian83.robome.web.auth.AuthController;
import com.github.adrian83.robome.web.health.HealthController;
import com.github.adrian83.robome.web.stage.StageController;
import com.github.adrian83.robome.web.table.TableController;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class Server {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

	@SafeVarargs
	private static Route createRoutes(Supplier<Route>... controllers) {
		return Arrays.stream(controllers).reduce((r1, r2) -> () -> r1.get().orElse(r2.get())).get().get();
	}

	public static void main(String[] args) throws Exception {
		
		LOGGER.info("starting server");

		Injector injector = Guice.createInjector(new RobomeModule());

		TableController tableController = injector.getInstance(TableController.class);
		StageController stageController = injector.getInstance(StageController.class);
		ActivityController activityController = injector.getInstance(ActivityController.class);
		AuthController authController = injector.getInstance(AuthController.class);
		HealthController healthController = injector.getInstance(HealthController.class);

		Route route = createRoutes(
				() -> tableController.createRoute(), 
				() -> stageController.createRoute(),
				() -> activityController.createRoute(),
				() -> authController.createRoute(),
				() -> healthController.createRoute()
				);

		ActorSystem system = injector.getInstance(ActorSystem.class);
		ActorMaterializer materializer = injector.getInstance(ActorMaterializer.class);

		final Http http = Http.get(system);
		ConnectHttp connect = injector.getInstance(ConnectHttp.class);

		final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = route.flow(system, materializer);
		final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow, connect, materializer);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			binding.thenCompose(ServerBinding::unbind).thenAccept(unbound -> system.terminate());
		}));

	}

}