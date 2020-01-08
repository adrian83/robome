package com.github.adrian83.robome.domain.user;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.domain.common.exception.EmailAlreadyInUseException;
import com.github.adrian83.robome.domain.user.model.User;
import com.google.inject.Inject;

import akka.Done;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class UserService {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

	private UserRepository userRepository;
	private ActorMaterializer actorMaterializer;
	
	@Inject
	public UserService(UserRepository repository, ActorMaterializer actorMaterializer) {
		this.userRepository = repository;
		this.actorMaterializer = actorMaterializer;
	}
	
	public CompletionStage<Done> saveUser(User newUser) {
		
		LOGGER.info("Persisting new user: {}", newUser);
		

		CompletableFuture<User> userFuture = CompletableFuture.completedFuture(newUser);
		
		userRepository.getByEmail(newUser.getEmail()).runWith(Sink.head(), actorMaterializer)
		.thenApply((maybeUser) -> maybeUser
				.map((user) -> userFuture.completeExceptionally(new EmailAlreadyInUseException("Email already in use"))));
		
		var userSource = Source.fromCompletionStage(userFuture);
		
		Sink<User, CompletionStage<Done>> sink = userRepository.saveUser();
		return userSource.runWith(sink, actorMaterializer);
	}
	
	public CompletionStage<Optional<User>> findUserByEmail(String email) {
		
		LOGGER.info("Looking for a user with email: {}", email);
		
		return userRepository.getByEmail(email)
				.runWith(Sink.head(), actorMaterializer);
	}
}
