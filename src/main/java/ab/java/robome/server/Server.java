package ab.java.robome.server;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import ab.java.robome.RobomeModule;
import ab.java.robome.web.table.TableController;

import java.util.concurrent.CompletionStage;

import com.google.inject.Guice;
import com.google.inject.Injector;


public class Server {
	
	 

  public static void main(String[] args) throws Exception {
	  
	 Injector injector = Guice.createInjector(new RobomeModule());
	 
	 TableController tableController = injector.getInstance(TableController.class);
	  

    ActorSystem system = injector.getInstance(ActorSystem.class);
    ActorMaterializer materializer = injector.getInstance(ActorMaterializer.class);
    
    
    final Http http = Http.get(system);
    ConnectHttp connect = injector.getInstance(ConnectHttp.class);


    final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = tableController.createRoute().flow(system, materializer);
    final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow, connect, materializer);


    
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        binding
        .thenCompose(ServerBinding::unbind) 
        .thenAccept(unbound -> system.terminate()); 
    }));


  }


}