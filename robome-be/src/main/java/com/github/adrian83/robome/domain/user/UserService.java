package com.github.adrian83.robome.domain.user;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.domain.common.exception.EmailAlreadyInUseException;
import com.github.adrian83.robome.domain.user.model.User;
import com.google.inject.Inject;

import akka.Done;
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

  public CompletionStage<Done> saveUser(User newUser) {
    log.info("Persisting new user: {}", newUser);

    CompletableFuture<User> userFuture = CompletableFuture.completedFuture(newUser);

    userRepository
        .getByEmail(newUser.getEmail())
        .runWith(Sink.head(), actorSystem)
        .thenApply(
            (maybeUser) ->
                maybeUser.map(
                    (user) ->
                        userFuture.completeExceptionally(
                            new EmailAlreadyInUseException("Email already in use"))));

    Sink<User, CompletionStage<Done>> sink = userRepository.saveUser();

    return Source.lazyCompletionStage(() -> userFuture).runWith(sink, actorSystem);
  }

  public CompletionStage<Optional<User>> findUserByEmail(String email) {
    log.info("Looking for a user with email: {}", email);

    return userRepository.getByEmail(email).runWith(Sink.head(), actorSystem);
  }
}
