package com.github.adrian83.robome.domain.table;

import java.util.UUID;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import static com.github.adrian83.robome.common.Time.toInstant;
import static com.github.adrian83.robome.common.Time.toUtcLocalDate;
import com.github.adrian83.robome.domain.table.model.TableEntity;
import com.github.adrian83.robome.domain.table.model.TableKey;
import static com.github.adrian83.robome.domain.table.model.TableState.valueOf;
import com.google.inject.Inject;

import akka.NotUsed;
import akka.japi.Function2;
import static akka.stream.alpakka.cassandra.CassandraWriteSettings.defaults;
import akka.stream.alpakka.cassandra.javadsl.CassandraFlow;
import akka.stream.alpakka.cassandra.javadsl.CassandraSession;
import akka.stream.alpakka.cassandra.javadsl.CassandraSource;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;

public class TableRepository {

    private static final String INSERT_TABLE_STMT = "INSERT INTO robome.tables (table_id, user_id, title, description, state, created_at, modified_at) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_ALL_STMT = "SELECT * FROM robome.tables";
    private static final String SELECT_BY_EMAIL_STMT = "SELECT * FROM robome.tables WHERE user_id = ?";
    private static final String SELECT_BY_ID_STMT = "SELECT * FROM robome.tables WHERE user_id = ? AND table_id = ?";
    private static final String DELETE_BY_ID_STMT = "DELETE FROM robome.tables WHERE table_id = ? AND user_id = ?";
    private static final String UPDATE_STMT = "UPDATE robome.tables SET title = ?, description = ?, state = ?, modified_at = ? WHERE table_id = ? AND user_id = ?";

    private static final String TABLE_ID_COL = "table_id";
    private static final String USER_ID_COL = "user_id";
    private static final String TITLE_COL = "title";
    private static final String DESCRIPTION_COL = "description";
    private static final String STATE_COL = "state";
    private static final String CREATED_AT_COL = "created_at";
    private static final String MODIFIED_AT_COL = "modified_at";

    private final CassandraSession session;

    @Inject
    public TableRepository(CassandraSession session) {
        this.session = session;
    }

    public Flow<TableEntity, TableEntity, NotUsed> saveTable() {
        Function2<TableEntity, PreparedStatement, BoundStatement> statementBinder = (table, prepStmt) -> prepStmt.bind(
                table.key().tableId(), table.key().userId(), table.title(), table.description(), table.state().name(),
                toInstant(table.createdAt()), toInstant(table.modifiedAt()));
        return CassandraFlow.create(session, defaults(), INSERT_TABLE_STMT, statementBinder);
    }

    public Flow<TableEntity, TableEntity, NotUsed> updateTable() {
        Function2<TableEntity, PreparedStatement, BoundStatement> statementBinder = (table, prepStmt) -> prepStmt.bind(
                table.title(), table.description(), table.state().name(), toInstant(table.modifiedAt()),
                table.key().tableId(), table.key().userId());
        return CassandraFlow.create(session, defaults(), UPDATE_STMT, statementBinder);
    }

    public Flow<TableKey, TableKey, NotUsed> deleteTable(UUID userId) {
        Function2<TableKey, PreparedStatement, BoundStatement> statementBinder = (key, prepStmt) -> prepStmt
                .bind(key.tableId(), userId);
        return CassandraFlow.create(session, defaults(), DELETE_BY_ID_STMT, statementBinder);
    }

    public Source<TableEntity, NotUsed> getById(UUID userId, UUID tableId) {
        Statement<?> stmt = SimpleStatement.newInstance(SELECT_BY_ID_STMT, userId, tableId);
        return CassandraSource.create(session, stmt).map(this::fromRow);
    }

    public Source<TableEntity, NotUsed> getUserTables(UUID userId) {
        Statement<?> stmt = SimpleStatement.newInstance(SELECT_BY_EMAIL_STMT, userId);
        return CassandraSource.create(session, stmt).map(this::fromRow);
    }

    public Source<TableEntity, NotUsed> getAllTables() {
        Statement<?> stmt = SimpleStatement.newInstance(SELECT_ALL_STMT);
        return CassandraSource.create(session, stmt).map(this::fromRow);
    }

    private TableEntity fromRow(Row row) {
        var key = new TableKey(row.get(USER_ID_COL, UUID.class), row.get(TABLE_ID_COL, UUID.class));
        return new TableEntity(key, row.getString(TITLE_COL), row.getString(DESCRIPTION_COL),
                valueOf(row.getString(STATE_COL)), toUtcLocalDate(row.getInstant(MODIFIED_AT_COL)),
                toUtcLocalDate(row.getInstant(CREATED_AT_COL)));
    }
}
