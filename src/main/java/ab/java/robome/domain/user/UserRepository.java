package ab.java.robome.domain.user;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.inject.Inject;

import ab.java.robome.domain.user.model.ImmutableUser;
import ab.java.robome.domain.user.model.User;
import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.alpakka.cassandra.javadsl.CassandraSink;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class UserRepository {
	
	private static final String SELECT_USER_BY_EMAIL = "";
	private static final String INSERT_USER_STMT = "";
	
	private Session session;
	private ActorSystem actorSystem;

	@Inject
	public UserRepository(Session session, ActorSystem actorSystem) {
		this.session = session;
		this.actorSystem = actorSystem;
	}

	public Source<Optional<User>, NotUsed> getByEmail(String email) {
		PreparedStatement preparedStatement = session.prepare(SELECT_USER_BY_EMAIL);
		BoundStatement bound = preparedStatement.bind(email);

		ResultSet r = session.execute(bound);

		Row row = r.one();
		if (row == null) {
			return Source.single(Optional.empty());
		}

		User user = fromRow(row);
		return Source.single(Optional.of(user));
	}

	private User fromRow(Row row) {
		
		User user = ImmutableUser.builder()
				.email(row.getString("email"))
				.passwordHash(row.getString("password_hash"))
				.build();

		return user;
	}

	public Sink<User, CompletionStage<Done>> saveUser() {
		PreparedStatement preparedStatement = session.prepare(INSERT_USER_STMT);
		
		BiFunction<User, PreparedStatement, BoundStatement> statementBinder = (user, statement) -> {
			return statement.bind(user.email(), user.passwordHash());
		};

		Sink<User, CompletionStage<Done>> sink = CassandraSink.create(1, preparedStatement, statementBinder, session,
				actorSystem.dispatcher());
		return sink;
	}
	
}
