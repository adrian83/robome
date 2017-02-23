package ab.java.robome;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.AbstractModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.stream.ActorMaterializer;

public class RobomeModule extends AbstractModule {

	private final ActorSystem system = ActorSystem.create("robome");

	final ActorMaterializer materializer = ActorMaterializer.create(system);

	@Override
	protected void configure() {
		initializeConfig();
		initializeConnectHttp();
		initializeActorSystem();
		initializeActorMaterializer();
		initializeCassandraSession();
		initializeObjectMapper();
	}

	private void initializeConfig() {
		Config config = ConfigFactory.load();
		this.bind(Config.class).toInstance(config);
	}

	private void initializeConnectHttp() {
		Config config = ConfigFactory.load();
		ConnectHttp connect = ConnectHttp.toHost(config.getString("server.host"), config.getInt("server.port"));
		this.bind(ConnectHttp.class).toInstance(connect);
	}

	private void initializeActorSystem() {
		this.bind(ActorSystem.class).toInstance(system);
	}

	private void initializeActorMaterializer() {
		this.bind(ActorMaterializer.class).toInstance(ActorMaterializer.create(system));
	}

	private void initializeCassandraSession() {
		Config config = ConfigFactory.load();

		Session session = Cluster.builder().addContactPoint(config.getString("cassandra.host"))
				.withPort(config.getInt("cassandra.port")).build().connect();
		this.bind(Session.class).toInstance(session);
	}
	
	private void initializeObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.registerModule(new JavaTimeModule());
		this.bind(ObjectMapper.class).toInstance(mapper);
	}

}