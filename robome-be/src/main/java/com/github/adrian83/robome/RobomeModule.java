package com.github.adrian83.robome;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.AbstractModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBuilder;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.marshalling.Marshaller;
import akka.stream.alpakka.cassandra.CassandraSessionSettings;
import akka.stream.alpakka.cassandra.javadsl.CassandraSession;
import akka.stream.alpakka.cassandra.javadsl.CassandraSessionRegistry;

public class RobomeModule extends AbstractModule {

  private static final String ACTOR_SYSTEM_NAME = "robome";

  private static final String SERVER_HOST_KEY = "server.host";
  private static final String SERVER_PORT_KEY = "server.port";

  private final ActorSystem system = ActorSystem.create(ACTOR_SYSTEM_NAME);

  private Config config;

  @Override
  protected void configure() {
    initializeConfig();
    initializeServerBuilder();
    initializeActorSystem();
    initializeCassandraSession();
    initializeObjectMapper();
    initializeMarshaller();
  }

  private void initializeConfig() {
    ConfigFactory.invalidateCaches();
    config = ConfigFactory.load();
    this.bind(Config.class).toInstance(config);
  }

  private void initializeServerBuilder() {
    final Http http = Http.get(system);
    var server =
        http.newServerAt(config.getString(SERVER_HOST_KEY), config.getInt(SERVER_PORT_KEY));

    this.bind(ServerBuilder.class).toInstance(server);
  }

  private void initializeActorSystem() {
    this.bind(ActorSystem.class).toInstance(system);
  }

  private void initializeCassandraSession() {
    var sessionSettings = CassandraSessionSettings.create();
    var cassandraSession = CassandraSessionRegistry.get(system).sessionFor(sessionSettings);

    this.bind(CassandraSession.class).toInstance(cassandraSession);
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
    mapper.registerModule(new Jdk8Module());
    return mapper;
  }
}
