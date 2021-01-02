package com.github.adrian83.robome.domain.user;

import static akka.stream.alpakka.cassandra.CassandraWriteSettings.defaults;
import static com.github.adrian83.robome.common.Time.toInstant;
import static com.github.adrian83.robome.common.Time.toUtcLocalDate;
import static com.github.adrian83.robome.domain.user.model.Role.fromString;

import java.util.UUID;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.github.adrian83.robome.domain.user.model.Role;
import com.github.adrian83.robome.domain.user.model.User;
import com.google.inject.Inject;

import akka.NotUsed;
import akka.japi.Function2;
import akka.stream.alpakka.cassandra.javadsl.CassandraFlow;
import akka.stream.alpakka.cassandra.javadsl.CassandraSession;
import akka.stream.alpakka.cassandra.javadsl.CassandraSource;
import akka.stream.javadsl.Flow;
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

  private CassandraSession session;

  @Inject
  public UserRepository(CassandraSession session) {
    this.session = session;
  }

  public Source<User, NotUsed> getByEmail(String email) {
    Statement<?> stmt = SimpleStatement.newInstance(SELECT_USER_BY_EMAIL, email);

    return CassandraSource.create(session, stmt).map(this::fromRow);
  }

  public Flow<User, User, NotUsed> saveUser() {
    Function2<User, PreparedStatement, BoundStatement> statementBinder =
        (user, prepStmt) ->
            prepStmt.bind(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                Role.toString(user.getRoles()),
                toInstant(user.getCreatedAt()),
                toInstant(user.getModifiedAt()));

    return CassandraFlow.create(session, defaults(), INSERT_USER_STMT, statementBinder);
  }

  private User fromRow(Row row) {
    return User.builder()
        .id(row.get(ID_COL, UUID.class))
        .email(row.getString(EMAIL_COL))
        .passwordHash(row.getString(PASS_HASH_COL))
        .roles(fromString(row.getString(ROLES_COL)))
        .modifiedAt(toUtcLocalDate(row.getInstant(MODIFIED_AT_COL)))
        .createdAt(toUtcLocalDate(row.getInstant(CREATED_AT_COL)))
        .build();
  }
}
