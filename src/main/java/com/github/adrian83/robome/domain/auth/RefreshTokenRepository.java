package com.github.adrian83.robome.domain.auth;

import java.util.UUID;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.github.adrian83.robome.auth.model.RefreshToken;
import com.google.inject.Inject;

import akka.NotUsed;
import akka.japi.Function2;
import static akka.stream.alpakka.cassandra.CassandraWriteSettings.defaults;
import akka.stream.alpakka.cassandra.javadsl.CassandraFlow;
import akka.stream.alpakka.cassandra.javadsl.CassandraSession;
import akka.stream.alpakka.cassandra.javadsl.CassandraSource;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;

public class RefreshTokenRepository {

    private static final String TABLE = "robome.refresh_tokens";

    private static final String SAVE_STMT = String.format(
            "INSERT INTO %s (token_id, user_id, refresh_token, token_family, issued_at, expires_at) VALUES (?, ?, ?, ?, ?, ?)",
            TABLE);

    private static final String GET_BY_TOKEN_STMT = String.format(
            "SELECT token_id, user_id, refresh_token, token_family, issued_at, expires_at FROM %s WHERE refresh_token = ? LIMIT 1",
            TABLE);

    private static final String DELETE_BY_USER_STMT = String.format(
            "DELETE FROM %s WHERE user_id = ?",
            TABLE);

    private static final String DELETE_BY_FAMILY_STMT = String.format(
            "DELETE FROM %s WHERE token_family = ?",
            TABLE);

    private final CassandraSession session;

    @Inject
    public RefreshTokenRepository(CassandraSession session) {
        this.session = session;
    }

    public Flow<RefreshToken, RefreshToken, NotUsed> saveToken() {
        Function2<RefreshToken, PreparedStatement, BoundStatement> statementBinder = (token, prepStmt) -> prepStmt.bind(
                token.tokenId(),
                token.userId(),
                token.refreshToken(),
                token.tokenFamily(),
                token.issuedAt(),
                token.expiresAt());
        return CassandraFlow.create(session, defaults(), SAVE_STMT, statementBinder);
    }

    public Source<RefreshToken, NotUsed> findByToken(String token) {
        Statement<?> stmt = SimpleStatement.newInstance(GET_BY_TOKEN_STMT, token);
        return CassandraSource.create(session, stmt).map(this::fromRow);
    }

    public Flow<UUID, UUID, NotUsed> deleteByUserId() {
        Function2<UUID, PreparedStatement, BoundStatement> statementBinder = (userId, prepStmt) -> prepStmt.bind(userId);
        return CassandraFlow.create(session, defaults(), DELETE_BY_USER_STMT, statementBinder);
    }

    public Flow<UUID, UUID, NotUsed> deleteByFamily() {
        Function2<UUID, PreparedStatement, BoundStatement> statementBinder = (family, prepStmt) -> prepStmt.bind(family);
        return CassandraFlow.create(session, defaults(), DELETE_BY_FAMILY_STMT, statementBinder);
    }

    private RefreshToken fromRow(Row row) {
        return new RefreshToken(
                row.getUuid("token_id"),
                row.getUuid("user_id"),
                row.getString("refresh_token"),
                row.getUuid("token_family"),
                row.getInstant("issued_at"),
                row.getInstant("expires_at"));
    }
}
