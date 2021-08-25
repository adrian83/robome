package com.github.adrian83.robome.domain.stage;

import static akka.stream.alpakka.cassandra.CassandraWriteSettings.defaults;
import static com.github.adrian83.robome.common.Time.toInstant;
import static com.github.adrian83.robome.common.Time.toUtcLocalDate;
import static com.github.adrian83.robome.domain.stage.model.StageState.valueOf;

import java.util.UUID;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.github.adrian83.robome.domain.stage.model.StageEntity;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.google.inject.Inject;

import akka.NotUsed;
import akka.japi.Function2;
import akka.stream.alpakka.cassandra.javadsl.CassandraFlow;
import akka.stream.alpakka.cassandra.javadsl.CassandraSession;
import akka.stream.alpakka.cassandra.javadsl.CassandraSource;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;

public class StageRepository {

  private static final String INSERT_STAGE_STMT =
      "INSERT INTO robome.stages (stage_id, table_id, user_id, title, state, created_at, modified_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
  private static final String SELECT_STAGE_BY_ID_STMT =
      "SELECT * FROM robome.stages WHERE table_id = ? AND stage_id = ? AND user_id = ? ALLOW FILTERING";
  private static final String SELECT_STAGES_BY_TABLE_ID_STMT =
      "SELECT * FROM robome.stages WHERE table_id = ? AND user_id = ? ALLOW FILTERING";
  private static final String UPDATE_STMT =
      "UPDATE robome.stages SET title = ?, state = ?, modified_at = ? WHERE table_id = ? AND stage_id = ? AND user_id = ?";
  private static final String DELETE_BY_ID_STMT =
      "DELETE FROM robome.stages WHERE table_id = ? AND stage_id = ? AND user_id = ?";

  private static final String TABLE_ID_COL = "table_id";
  private static final String STAGE_ID_COL = "stage_id";
  private static final String USER_ID_COL = "user_id";
  private static final String TITLE_COL = "title";
  private static final String STATE_COL = "state";
  private static final String CREATED_AT_COL = "created_at";
  private static final String MODIFIED_AT_COL = "modified_at";

  private CassandraSession session;

  @Inject
  public StageRepository(CassandraSession session) {
    this.session = session;
  }

  public Flow<StageEntity, StageEntity, NotUsed> saveStage() {
    Function2<StageEntity, PreparedStatement, BoundStatement> statementBinder =
        (stage, prepStmt) ->
            prepStmt.bind(
                stage.key().stageId(),
                stage.key().tableId(),
                stage.userId(),
                stage.title(),
                stage.state().name(),
                toInstant(stage.createdAt()),
                toInstant(stage.modifiedAt()));

    return CassandraFlow.create(session, defaults(), INSERT_STAGE_STMT, statementBinder);
  }

  public Flow<StageEntity, StageEntity, NotUsed> updateStage() {
    Function2<StageEntity, PreparedStatement, BoundStatement> statementBinder =
        (stage, prepStmt) ->
            prepStmt.bind(
                stage.title(),
                stage.state().name(),
                toInstant(stage.modifiedAt()),
                stage.key().tableId(),
                stage.key().stageId(),
                stage.userId());

    return CassandraFlow.create(session, defaults(), UPDATE_STMT, statementBinder);
  }

  public Flow<StageKey, StageKey, NotUsed> deleteStage(UUID userId) {
    Function2<StageKey, PreparedStatement, BoundStatement> statementBinder =
        (key, prepStmt) -> prepStmt.bind(key.tableId(), key.stageId(), userId);

    return CassandraFlow.create(session, defaults(), DELETE_BY_ID_STMT, statementBinder);
  }

  public Source<StageEntity, NotUsed> getById(StageKey stageKey, UUID userID) {
    Statement<?> stmt =
        SimpleStatement.newInstance(
            SELECT_STAGE_BY_ID_STMT, stageKey.tableId(), stageKey.stageId(), userID);

    return CassandraSource.create(session, stmt).map(this::fromRow);
  }

  public Source<StageEntity, NotUsed> getTableStages(TableKey key, UUID userId) {
    Statement<?> stmt =
        SimpleStatement.newInstance(SELECT_STAGES_BY_TABLE_ID_STMT, key.tableId(), userId);

    return CassandraSource.create(session, stmt).map(this::fromRow);
  }

  private StageEntity fromRow(Row row) {
    var key = new StageKey(row.get(TABLE_ID_COL, UUID.class), row.get(STAGE_ID_COL, UUID.class));

    return new StageEntity(
        key,
        row.get(USER_ID_COL, UUID.class),
        row.getString(TITLE_COL),
        valueOf(row.getString(STATE_COL)),
        toUtcLocalDate(row.getInstant(MODIFIED_AT_COL)),
        toUtcLocalDate(row.getInstant(CREATED_AT_COL)));
  }
}
