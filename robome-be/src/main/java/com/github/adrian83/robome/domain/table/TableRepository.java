package com.github.adrian83.robome.domain.table;

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
import com.github.adrian83.robome.common.time.TimeUtils;
import com.github.adrian83.robome.domain.table.model.Table;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.table.model.TableState;
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
      "DELETE * FROM robome.tables WHERE table_id = ? AND user_id = ?";
  private static final String UPDATE_STMT =
      "UPDATE robome.tables SET title = ?, description = ?, state = ?, modified_at = ? WHERE table_id = ?";

  private Session session;

  @Inject
  public TableRepository(Session session) {
    this.session = session;
  }

  public Sink<Table, CompletionStage<Done>> saveTable() {

    PreparedStatement preparedStatement = session.prepare(INSERT_TABLE_STMT);
    return CassandraSink.create(1, preparedStatement, this::createInserBoundStatement, session);
  }

  public Sink<TableKey, CompletionStage<Done>> deleteTable(TableKey tableId, UUID userId) {

    PreparedStatement preparedStatement = session.prepare(DELETE_BY_ID_STMT);
    BiFunction<TableKey, PreparedStatement, BoundStatement> boundStmt =
        (tabId, stmt) -> stmt.bind(tabId.getTableId(), userId);
    return CassandraSink.create(1, preparedStatement, boundStmt, session);
  }

  public Sink<Table, CompletionStage<Done>> updateTable(Table table) {

    PreparedStatement preparedStatement = session.prepare(UPDATE_STMT);
    BiFunction<Table, PreparedStatement, BoundStatement> boundStmt =
        (tab, stmt) ->
            stmt.bind(
                tab.getTitle(),
                tab.getDescription(),
                tab.getState().name(),
                TimeUtils.toDate(tab.getModifiedAt()),
                table.getKey().getTableId().toString());
    return CassandraSink.create(1, preparedStatement, boundStmt, session);
  }

  public Source<Optional<Table>, NotUsed> getById(UUID userId, UUID tableId) {
    PreparedStatement preparedStatement = session.prepare(SELECT_BY_ID_STMT);
    BoundStatement bound = preparedStatement.bind(userId, tableId);
    ResultSet result = session.execute(bound);
    return Source.single(result)
        .map(ResultSet::one)
        .map(Optional::ofNullable)
        .map(mayneRow -> mayneRow.map(this::fromRow));
  }

  public Source<Table, NotUsed> getAllTables() {
    Statement preparedStatement = new SimpleStatement(SELECT_ALL_STMT);
    return CassandraSource.create(preparedStatement, session).map(this::fromRow);
  }

  public Source<Table, NotUsed> getUserTables(UUID userId) {
    PreparedStatement preparedStatement = session.prepare(SELECT_BY_EMAIL_STMT);
    BoundStatement bound = preparedStatement.bind(userId);
    return CassandraSource.create(bound, session).map(this::fromRow);
  }

  private BoundStatement createInserBoundStatement(Table table, PreparedStatement preparedStmt) {

    return preparedStmt.bind(
        table.getKey().getTableId(),
        table.getUserId(),
        table.getTitle(),
        table.getDescription(),
        table.getState().name(),
        TimeUtils.toDate(table.getCreatedAt()),
        TimeUtils.toDate(table.getModifiedAt()));
  }

  private Table fromRow(Row row) {

    return new Table(
        new TableKey(row.get("table_id", UUID.class)),
        row.getUUID("user_id"),
        row.getString("title"),
        row.getString("description"),
        TableState.valueOf(row.getString("state")),
        TimeUtils.toUtcLocalDate(row.getTimestamp("created_at")),
        TimeUtils.toUtcLocalDate(row.getTimestamp("modified_at")));
  }
}
