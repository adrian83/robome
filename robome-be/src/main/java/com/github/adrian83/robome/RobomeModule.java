package com.github.adrian83.robome;

import static akka.http.javadsl.ConnectHttp.toHost;

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
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.marshalling.Marshaller;
import akka.stream.ActorMaterializer;

public class RobomeModule extends AbstractModule {

  private static final String ACTOR_SYSTEM_NAME = "robome";

  private static final String SERVER_HOST_KEY = "server.host";
  private static final String SERVER_PORT_KEY = "server.port";

  private static final String CASSANDRA_HOST_KEY = "cassandra.host";
  private static final String CASSANDRA_PORT_KEY = "cassandra.port";

  private final ActorSystem system = ActorSystem.create(ACTOR_SYSTEM_NAME);

  private Config config;
  
  @Override
  protected void configure() {
    initializeConfig();
    initializeConnectHttp();
    initializeActorSystem();
    initializeActorMaterializer();
    initializeCassandraSession();
    initializeObjectMapper();
    initializeMarshaller();
  }

  private void initializeConfig() {
	ConfigFactory.invalidateCaches();
	config = ConfigFactory.load();
    this.bind(Config.class).toInstance(config);
  }

  private void initializeConnectHttp() {
    var connect = toHost(config.getString(SERVER_HOST_KEY), config.getInt(SERVER_PORT_KEY));
    this.bind(ConnectHttp.class).toInstance(connect);
  }

  private void initializeActorSystem() {
    this.bind(ActorSystem.class).toInstance(system);
  }

  private void initializeActorMaterializer() {
    this.bind(ActorMaterializer.class).toInstance(ActorMaterializer.create(system));
  }

  private void initializeCassandraSession() {
    var session =
        Cluster.builder()
            .addContactPoint(config.getString(CASSANDRA_HOST_KEY))
            .withPort(config.getInt(CASSANDRA_PORT_KEY))
            .build()
            .connect();
    this.bind(Session.class).toInstance(session);
  }

  private void initializeObjectMapper() {
    var objectMapper = createObjectMapper();
    this.bind(ObjectMapper.class).toInstance(objectMapper);
  }

  private void initializeMarshaller() {
    var marshaller = Jackson.marshaller(createObjectMapper());
    this.bind(Marshaller.class).toInstance(marshaller);
  }

  private ObjectMapper createObjectMapper() {
    var mapper = new ObjectMapper();
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.registerModule(new JavaTimeModule());
    return mapper;
  }
}
