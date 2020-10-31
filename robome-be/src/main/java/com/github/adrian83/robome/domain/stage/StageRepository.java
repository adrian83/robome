package com.github.adrian83.robome.domain.stage;

import static com.github.adrian83.robome.common.Time.toDate;
import static com.github.adrian83.robome.common.Time.toUtcLocalDate;
import static com.github.adrian83.robome.domain.stage.model.StageState.valueOf;
import static java.util.stream.Collectors.toList;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.github.adrian83.robome.domain.stage.model.StageEntity;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.google.inject.Inject;

import akka.Done;
import akka.NotUsed;
import akka.stream.alpakka.cassandra.javadsl.CassandraSink;
import akka.stream.javadsl.Sink;
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

  private Session session;

  private Sink<StageEntity, CompletionStage<Done>> saveStageSink;
  private Sink<StageEntity, CompletionStage<Done>> updateStageSink;

  private PreparedStatement findStageByIdStmt;
  private PreparedStatement findStagesByTableStmt;
  private PreparedStatement deleteStageStmt;

  @Inject
  public StageRepository(Session session) {
    this.session = session;

    var insertStageStmt = session.prepare(INSERT_STAGE_STMT);
    saveStageSink = CassandraSink.create(1, insertStageStmt, this::bindInsertedStage, session);

    var updateStageStmt = session.prepare(UPDATE_STMT);
    updateStageSink = CassandraSink.create(1, updateStageStmt, this::bindUpdatedStage, session);

    findStageByIdStmt = session.prepare(SELECT_STAGE_BY_ID_STMT);
    findStagesByTableStmt = session.prepare(SELECT_STAGES_BY_TABLE_ID_STMT);
    deleteStageStmt = session.prepare(DELETE_BY_ID_STMT);
  }

  public Sink<StageEntity, CompletionStage<Done>> saveStage() {
    return saveStageSink;
  }

  public Sink<StageEntity, CompletionStage<Done>> updateStage() {
    return updateStageSink;
  }

  public Sink<StageKey, CompletionStage<Done>> deleteStage(UUID userId) {
    BiFunction<StageKey, PreparedStatement, BoundStatement> boundStmt =
        (stgKey, stmt) -> stmt.bind(stgKey.getTableId(), stgKey.getStageId(), userId);

    return CassandraSink.create(1, deleteStageStmt, boundStmt, session);
  }

  public Source<StageEntity, NotUsed> getTableStages(UUID userID, UUID tableUuid) {
    var bound = findStagesByTableStmt.bind(tableUuid, userID);
    var stages = session.execute(bound).all().stream().map(this::fromRow).collect(toList());
    return Source.from(stages);
  }

  public Source<Optional<StageEntity>, NotUsed> getById(UUID userID, StageKey stageKey) {
    var bound = findStageByIdStmt.bind(stageKey.getTableId(), stageKey.getStageId(), userID);

    var result = session.execute(bound);
    return Source.single(result)
        .map(ResultSet::one)
        .map(Optional::ofNullable)
        .map(mayneRow -> mayneRow.map(this::fromRow));
  }

  private StageEntity fromRow(Row row) {
    var key =
        StageKey.builder()
            .tableId(row.get(TABLE_ID_COL, UUID.class))
            .stageId(row.get(STAGE_ID_COL, UUID.class))
            .build();

    return new StageEntity(
        key,
        row.get(USER_ID_COL, UUID.class),
        row.getString(TITLE_COL),
        valueOf(row.getString(STATE_COL)),
        toUtcLocalDate(row.getTimestamp(CREATED_AT_COL)),
        toUtcLocalDate(row.getTimestamp(MODIFIED_AT_COL)));
  }

  private BoundStatement bindInsertedStage(StageEntity stage, PreparedStatement statement) {
    return statement.bind(
        stage.getKey().getStageId(),
        stage.getKey().getTableId(),
        stage.getUserId(),
        stage.getTitle(),
        stage.getState().name(),
        toDate(stage.getCreatedAt()),
        toDate(stage.getModifiedAt()));
  }

  private BoundStatement bindUpdatedStage(StageEntity stg, PreparedStatement stmt) {
    return stmt.bind(
        stg.getTitle(),
        stg.getState().name(),
        toDate(stg.getModifiedAt()),
        stg.getKey().getTableId(),
        stg.getKey().getStageId(),
        stg.getUserId());
  }
}
