package com.github.adrian83.robome.domain.user;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.google.inject.Inject;

import akka.Done;
import akka.NotUsed;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class UserService {

	private UserRepository userRepository;
	private ActorMaterializer actorMaterializer;
	
	@Inject
	public UserService(UserRepository repository, ActorMaterializer actorMaterializer) {
		this.userRepository = repository;
		this.actorMaterializer = actorMaterializer;
	}
	
	public CompletionStage<Done> saveUser(User newUser) {
		Source<User, NotUsed> source = Source.single(newUser);
		Sink<User, CompletionStage<Done>> sink = userRepository.saveUser();
		return source.runWith(sink, actorMaterializer);
	}
	
	public CompletionStage<Optional<User>> findUserByEmail(String email) {
		return userRepository.getByEmail(email)
				.runWith(Sink.head(), actorMaterializer);
	}
}
