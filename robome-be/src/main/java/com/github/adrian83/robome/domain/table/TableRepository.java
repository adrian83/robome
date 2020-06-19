package com.github.adrian83.robome.domain.table;

import static com.github.adrian83.robome.common.Time.toDate;
import static com.github.adrian83.robome.common.Time.toUtcLocalDate;
import static com.github.adrian83.robome.domain.table.model.TableState.valueOf;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.github.adrian83.robome.domain.table.model.TableEntity;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.google.inject.Inject;

import akka.Done;
import akka.NotUsed;
import akka.stream.alpakka.cassandra.javadsl.CassandraSink;
import akka.stream.alpakka.cassandra.javadsl.CassandraSource;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class TableRepository {

  private static final String INSERT_TABLE_STMT =
      "INSERT INTO robome.tables (table_id, user_id, title, description, state, created_at, modified_at) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?)";
  private static final String SELECT_ALL_STMT = "SELECT * FROM robome.tables";
  private static final String SELECT_BY_EMAIL_STMT =
      "SELECT * FROM robome.tables WHERE user_id = ?";
  private static final String SELECT_BY_ID_STMT =
      "SELECT * FROM robome.tables WHERE user_id = ? AND table_id = ?";
  private static final String DELETE_BY_ID_STMT =
      "DELETE FROM robome.tables WHERE table_id = ? AND user_id = ?";
  private static final String UPDATE_STMT =
      "UPDATE robome.tables SET title = ?, description = ?, state = ?, modified_at = ? WHERE table_id = ? AND user_id = ?";

  private static final String TABLE_ID_COL = "table_id";
  private static final String USER_ID_COL = "user_id";
  private static final String TITLE_COL = "title";
  private static final String DESCRIPTION_COL = "description";
  private static final String STATE_COL = "state";
  private static final String CREATED_AT_COL = "created_at";
  private static final String MODIFIED_AT_COL = "modified_at";

  private Session session;

  @Inject
  public TableRepository(Session session) {
    this.session = session;
  }

  public Sink<TableEntity, CompletionStage<Done>> saveTable() {
    PreparedStatement preparedStatement = session.prepare(INSERT_TABLE_STMT);
    return CassandraSink.create(1, preparedStatement, this::createInserBoundStatement, session);
  }

  public Sink<TableKey, CompletionStage<Done>> deleteTable(UUID userId) {
    PreparedStatement preparedStatement = session.prepare(DELETE_BY_ID_STMT);
    BiFunction<TableKey, PreparedStatement, BoundStatement> boundStmt =
        (tabId, stmt) -> stmt.bind(tabId.getTableId(), userId);
    return CassandraSink.create(1, preparedStatement, boundStmt, session);
  }

  public Sink<TableEntity, CompletionStage<Done>> updateTable(TableEntity table) {
    PreparedStatement preparedStatement = session.prepare(UPDATE_STMT);
    BiFunction<TableEntity, PreparedStatement, BoundStatement> boundStmt =
        (tab, stmt) ->
            stmt.bind(
                tab.getTitle(),
                tab.getDescription(),
                tab.getState().name(),
                toDate(tab.getModifiedAt()),
                table.getKey().getTableId(),
                table.getUserId());
    return CassandraSink.create(1, preparedStatement, boundStmt, session);
  }

  public Source<Optional<TableEntity>, NotUsed> getById(UUID userId, UUID tableId) {
    BoundStatement bound = session.prepare(SELECT_BY_ID_STMT).bind(userId, tableId);
    ResultSet result = session.execute(bound);
    return Source.single(result)
        .map(ResultSet::one)
        .map(Optional::ofNullable)
        .map(mayneRow -> mayneRow.map(this::fromRow));
  }

  public Source<TableEntity, NotUsed> getAllTables() {
    Statement preparedStatement = new SimpleStatement(SELECT_ALL_STMT);
    return CassandraSource.create(preparedStatement, session).map(this::fromRow);
  }

  public Source<TableEntity, NotUsed> getUserTables(UUID userId) {
    PreparedStatement preparedStatement = session.prepare(SELECT_BY_EMAIL_STMT);
    BoundStatement bound = preparedStatement.bind(userId);
    return CassandraSource.create(bound, session).map(this::fromRow);
  }

  private BoundStatement createInserBoundStatement(
      TableEntity table, PreparedStatement preparedStmt) {
    return preparedStmt.bind(
        table.getKey().getTableId(),
        table.getUserId(),
        table.getTitle(),
        table.getDescription(),
        table.getState().name(),
        toDate(table.getCreatedAt()),
        toDate(table.getModifiedAt()));
  }

  private TableEntity fromRow(Row row) {
    return new TableEntity(
        new TableKey(row.get(TABLE_ID_COL, UUID.class)),
        row.getUUID(USER_ID_COL),
        row.getString(TITLE_COL),
        row.getString(DESCRIPTION_COL),
        valueOf(row.getString(STATE_COL)),
        toUtcLocalDate(row.getTimestamp(CREATED_AT_COL)),
        toUtcLocalDate(row.getTimestamp(MODIFIED_AT_COL)));
  }
}
