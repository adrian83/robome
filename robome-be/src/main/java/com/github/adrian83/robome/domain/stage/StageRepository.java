package com.github.adrian83.robome.domain.stage;

import static java.util.stream.Collectors.toList;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.github.adrian83.robome.common.time.TimeUtils;
import com.github.adrian83.robome.domain.stage.model.StageEntity;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.domain.stage.model.StageState;
import com.google.inject.Inject;

import akka.Done;
import akka.NotUsed;
import akka.stream.alpakka.cassandra.javadsl.CassandraSink;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class StageRepository {

  private static final String TABLE_ID_FIELD = "table_id";
  private static final String STAGE_ID_FIELD = "stage_id";

  private static final String INSERT_STAGE_STMT =
      "INSERT INTO robome.stages (stage_id, table_id, user_id, title, state, created_at, modified_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
  private static final String SELECT_STAGE_BY_ID_STMT =
      "SELECT * FROM robome.stages WHERE table_id = ? AND stage_id = ? AND user_id = ? ALLOW FILTERING";
  private static final String SELECT_STAGES_BY_TABLE_ID_STMT =
      "SELECT * FROM robome.stages WHERE table_id = ? AND user_id = ? ALLOW FILTERING";
  private static final String UPDATE_STMT =
	      "UPDATE robome.stages SET title = ?, state = ?, modified_at = ? WHERE table_id = ? AND stage_id = ? AND user_id = ?";
private static final String DELETE_BY_ID_STMT = "DELETE FROM robome.stages WHERE table_id = ? AND stage_id = ? AND user_id = ?";
  
  private Session session;

  @Inject
  public StageRepository(Session session) {
    this.session = session;
  }

  public Sink<StageEntity, CompletionStage<Done>> saveStage() {
    PreparedStatement preparedStatement = session.prepare(INSERT_STAGE_STMT);

    BiFunction<StageEntity, PreparedStatement, BoundStatement> statementBinder =
        (stage, statement) -> {
          Date created = TimeUtils.toDate(stage.getCreatedAt());
          Date modified = TimeUtils.toDate(stage.getModifiedAt());
          return statement.bind(
              stage.getKey().getStageId(),
              stage.getKey().getTableId(),
              stage.getUserId(),
              stage.getTitle(),
              stage.getState().name(),
              created,
              modified);
        };

    return CassandraSink.create(1, preparedStatement, statementBinder, session);
  }

  public Sink<StageEntity, CompletionStage<Done>> updateStage(StageEntity stage) {

	    PreparedStatement preparedStatement = session.prepare(UPDATE_STMT);
	    BiFunction<StageEntity, PreparedStatement, BoundStatement> boundStmt =
	        (stg, stmt) ->
	            stmt.bind(
	            		stg.getTitle(),
	            		stg.getState().name(),
	                TimeUtils.toDate(stg.getModifiedAt()),
	                stg.getKey().getTableId(),
	                stg.getKey().getStageId(),
	                stg.getUserId());
	    return CassandraSink.create(1, preparedStatement, boundStmt, session);
	  }
  
  public Sink<StageKey, CompletionStage<Done>> deleteStage(StageKey stageKey, UUID userId) {

	    PreparedStatement preparedStatement = session.prepare(DELETE_BY_ID_STMT);
	    BiFunction<StageKey, PreparedStatement, BoundStatement> boundStmt =
	        (stgKey, stmt) -> stmt.bind(stgKey.getTableId(), stgKey.getStageId(), userId);
	    return CassandraSink.create(1, preparedStatement, boundStmt, session);
	  }
  
  public Source<StageEntity, NotUsed> getTableStages(UUID userID, UUID tableUuid) {
    var preparedStatement = session.prepare(SELECT_STAGES_BY_TABLE_ID_STMT);
    var bound = preparedStatement.bind(tableUuid, userID);
    var stages = session.execute(bound).all().stream().map(this::fromRow).collect(toList());
    return Source.from(stages);
  }

  public Source<Optional<StageEntity>, NotUsed> getById(UUID userID, StageKey stageKey) {

    var preparedStatement = session.prepare(SELECT_STAGE_BY_ID_STMT);
    var bound = preparedStatement.bind(stageKey.getTableId(), stageKey.getStageId(), userID);
    var result = session.execute(bound);
    return Source.single(result)
        .map(ResultSet::one)
        .map(Optional::ofNullable)
        .map(mayneRow -> mayneRow.map(this::fromRow));
  }

  private StageEntity fromRow(Row row) {

    var id = new StageKey(row.get(TABLE_ID_FIELD, UUID.class), row.get(STAGE_ID_FIELD, UUID.class));
    return new StageEntity(
        id,
        row.get("user_id", UUID.class),
        row.getString("title"),
        StageState.valueOf(row.getString("state")),
        TimeUtils.toUtcLocalDate(row.getTimestamp("created_at")),
        TimeUtils.toUtcLocalDate(row.getTimestamp("modified_at")));
  }
}
