package ab.java.robome.domain.user;

import java.util.Optional;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.inject.Inject;


import ab.java.robome.domain.user.model.ImmutableUser;
import ab.java.robome.domain.user.model.User;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;

public class UserRepository {
	
	private static final String SELECT_USER_BY_EMAIL = "";
	
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
	
}
