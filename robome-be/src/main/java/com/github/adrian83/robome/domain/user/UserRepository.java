package com.github.adrian83.robome.domain.user;

import static com.github.adrian83.robome.common.Time.toDate;
import static com.github.adrian83.robome.common.Time.toUtcLocalDate;
import static com.github.adrian83.robome.domain.user.model.Role.fromString;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.github.adrian83.robome.domain.user.model.Role;
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
      "INSERT INTO robome.users (id, email, "
          + "password_hash, "
          + "roles, created_at, modified_at) VALUES (?, ?, ?, ?, ?, ?)";

  private static final String ID_COL = "id";
  private static final String EMAIL_COL = "email";
  private static final String PASS_HASH_COL = "password_hash";
  private static final String ROLES_COL = "roles";
  private static final String CREATED_AT_COL = "created_at";
  private static final String MODIFIED_AT_COL = "modified_at";

  private Session session;

  private PreparedStatement findByEmailStmt;
  private PreparedStatement insterUserStmt;

  @Inject
  public UserRepository(Session session) {
    this.session = session;

    findByEmailStmt = session.prepare(SELECT_USER_BY_EMAIL);
    insterUserStmt = session.prepare(INSERT_USER_STMT);
  }

  public Source<Optional<User>, NotUsed> getByEmail(String email) {
    BoundStatement boundStmt = findByEmailStmt.bind(email);
    ResultSet r = session.execute(boundStmt);
    return Source.single(Optional.ofNullable(r.one()).map(this::fromRow));
  }

  private User fromRow(Row row) {
    return new User(
        row.get(ID_COL, UUID.class),
        row.getString(EMAIL_COL),
        row.getString(PASS_HASH_COL),
        // fromStringList(row.getList("roles", String.class)),
        fromString(row.getString(ROLES_COL)),
        toUtcLocalDate(row.getTimestamp(CREATED_AT_COL)),
        toUtcLocalDate(row.getTimestamp(MODIFIED_AT_COL)));
  }

  public Sink<User, CompletionStage<Done>> saveUser() {
    BiFunction<User, PreparedStatement, BoundStatement> statementBinder =
        (user, statement) ->
            statement.bind(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                // toStringList(user.getRoles()),
                Role.toString(user.getRoles()),
                toDate(user.getCreatedAt()),
                toDate(user.getModifiedAt()));

    return CassandraSink.create(1, insterUserStmt, statementBinder, session);
  }
}
