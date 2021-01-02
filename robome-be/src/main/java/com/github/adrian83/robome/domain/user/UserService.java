package com.github.adrian83.robome.domain.user;

import static java.util.concurrent.CompletableFuture.completedStage;

import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.domain.common.exception.EmailAlreadyInUseException;
import com.github.adrian83.robome.domain.user.model.User;
import com.google.inject.Inject;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserService {

  private UserRepository userRepository;
  private ActorSystem actorSystem;

  @Inject
  public UserService(UserRepository repository, ActorSystem actorSystem) {
    this.userRepository = repository;
    this.actorSystem = actorSystem;
  }

  public CompletionStage<User> saveUser(User newUser) {
    log.info("Persisting new user: {}", newUser);

    return userRepository
        .getByEmail(newUser.getEmail())
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

  public CompletionStage<User> findUserByEmail(String email) {
    // log.info("Looking for a user with email: {}", email);

    return userRepository.getByEmail(email).runWith(Sink.head(), actorSystem);
  }
}
