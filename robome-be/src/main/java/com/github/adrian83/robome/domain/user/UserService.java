package com.github.adrian83.robome.domain.user;

import static java.util.concurrent.CompletableFuture.completedStage;

import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.domain.common.exception.EmailAlreadyInUseException;
import com.github.adrian83.robome.domain.user.model.User;
import com.google.inject.Inject;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

  private UserRepository userRepository;
  private ActorSystem actorSystem;

  @Inject
  public UserService(UserRepository repository, ActorSystem actorSystem) {
    this.userRepository = repository;
    this.actorSystem = actorSystem;
  }

  public CompletionStage<User> saveUser(User newUser) {
    LOGGER.info("Persisting new user: {}", newUser);

    return userRepository
        .getByEmail(newUser.email())
        .runWith(Sink.head(), actorSystem)
        .thenApply(
            (user) -> {
              throw new EmailAlreadyInUseException("Email already in use");
            })
        .exceptionally(
            t -> {
              if (t instanceof CompletionException) {
                var cause = t.getCause();
                if (cause instanceof EmailAlreadyInUseException) {
                  throw new EmailAlreadyInUseException("Email already in use");
                }
              }
              return true;
            })
        .thenCompose(
            e -> {
              var flow =
                  userRepository
                      .saveUser()
                      .mapMaterializedValue(notUsed -> completedStage(newUser));

              return Source.single(newUser).via(flow).runWith(Sink.head(), actorSystem);
            });
  }

  public CompletionStage<Optional<User>> findUserByEmail(String email) {
    // log.info("Looking for a user with email: {}", email);

    return userRepository.getByEmail(email).runWith(Sink.headOption(), actorSystem);
  }
}
