package com.github.adrian83.robome.domain.user;

import static java.util.concurrent.CompletableFuture.completedStage;

import java.util.Optional;
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

	return userRepository.getByEmail(newUser.email()).runWith(Sink.headOption(), actorSystem)
		.thenApply((maybeUser) -> {
		    if (maybeUser.isPresent())
			throw new EmailAlreadyInUseException("Email already in use");
		    return newUser;
		}).thenCompose(this::storeUser);
    }

    public CompletionStage<Optional<User>> findUserByEmail(String email) {
	return userRepository.getByEmail(email).runWith(Sink.headOption(), actorSystem);
    }

    private CompletionStage<User> storeUser(User user) {
	var flow = userRepository.saveUser().mapMaterializedValue(notUsed -> completedStage(user));
	return Source.single(user).via(flow).runWith(Sink.head(), actorSystem);
    }
}
