package com.github.adrian83.robome.domain.user;

import static com.github.adrian83.robome.common.time.TimeUtils.toDate;
import static com.github.adrian83.robome.common.time.TimeUtils.toUtcLocalDate;
import static com.github.adrian83.robome.domain.user.model.Role.fromStringList;
import static com.github.adrian83.robome.domain.user.model.Role.toStringList;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.github.adrian83.robome.domain.user.model.User;
import com.google.inject.Inject;

import akka.Done;
import akka.NotUsed;
import akka.stream.alpakka.cassandra.javadsl.CassandraSink;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class UserRepository {

  private static final String SELECT_USER_BY_EMAIL = "SELECT * FROM robome.users WHERE email = ?";
  private static final String INSERT_USER_STMT =
      "INSERT INTO robome.users (id, email, password_hash, "
          + "roles, created_at, modified_at) VALUES (?, ?, ?, ?, ?, ?)";

  private Session session;

  @Inject
  public UserRepository(Session session) {
    this.session = session;
  }

  public Source<Optional<User>, NotUsed> getByEmail(String email) {
    PreparedStatement preparedStatement = session.prepare(SELECT_USER_BY_EMAIL);
    BoundStatement bound = preparedStatement.bind(email);
    ResultSet r = session.execute(bound);
    return Source.single(Optional.ofNullable(r.one()).map(this::fromRow));
  }

  private User fromRow(Row row) {
    return new User(
        row.get("id", UUID.class),
        row.getString("email"),
        row.getString("password_hash"),
        fromStringList(row.getList("roles", String.class)),
        toUtcLocalDate(row.getTimestamp("created_at")),
        toUtcLocalDate(row.getTimestamp("modified_at")));
  }

  public Sink<User, CompletionStage<Done>> saveUser() {
    PreparedStatement preparedStatement = session.prepare(INSERT_USER_STMT);

    BiFunction<User, PreparedStatement, BoundStatement> statementBinder =
        (user, statement) ->
            statement.bind(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                toStringList(user.getRoles()),
                toDate(user.getCreatedAt()),
                toDate(user.getModifiedAt()));

    return CassandraSink.create(1, preparedStatement, statementBinder, session);
  }
}
