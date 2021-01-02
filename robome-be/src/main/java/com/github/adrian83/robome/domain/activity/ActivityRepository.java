package com.github.adrian83.robome.domain.activity;

import static akka.stream.alpakka.cassandra.CassandraWriteSettings.defaults;
import static com.github.adrian83.robome.common.Time.toInstant;
import static com.github.adrian83.robome.common.Time.toUtcLocalDate;
import static com.github.adrian83.robome.domain.activity.model.ActivityState.valueOf;

import java.util.UUID;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.github.adrian83.robome.domain.activity.model.ActivityEntity;
import com.github.adrian83.robome.domain.activity.model.ActivityKey;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.google.inject.Inject;

import akka.NotUsed;
import akka.japi.Function2;
import akka.stream.alpakka.cassandra.javadsl.CassandraFlow;
import akka.stream.alpakka.cassandra.javadsl.CassandraSession;
import akka.stream.alpakka.cassandra.javadsl.CassandraSource;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;

public class ActivityRepository {

  private static final String INSERT_ACTIVITY_STMT =
      "INSERT INTO robome.activities (activity_id, stage_id, table_id, user_id, name, state, "
          + "created_at, modified_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
  private static final String SELECT_ACTIVITY_BY_ID_STMT =
      "SELECT * FROM robome.activities WHERE user_id = ? AND table_id = ? AND stage_id = ? AND activity_id = ?";
  private static final String SELECT_ACTIVITIES_BY_TABLE_ID_AND_STAGE_ID_STMT =
      "SELECT * FROM robome.activities WHERE table_id = ? AND stage_id = ? AND user_id = ? ALLOW FILTERING";
  private static final String DELETE_BY_ID_STMT =
      "DELETE FROM robome.activities WHERE table_id = ? AND stage_id = ? AND activity_id = ? AND user_id = ?";
  private static final String UPDATE_STMT =
      "UPDATE robome.activities SET name = ?, state = ?, modified_at = ? WHERE table_id = ? AND stage_id = ? AND activity_id = ? AND user_id = ?";

  private static final String TABLE_ID_COL = "table_id";
  private static final String STAGE_ID_COL = "stage_id";
  private static final String ACTIVITY_ID_COL = "activity_id";
  private static final String USER_ID_COL = "user_id";
  private static final String NAME_COL = "name";
  private static final String STATE_COL = "state";
  private static final String CREATED_AT_COL = "created_at";
  private static final String MODIFIED_AT_COL = "modified_at";

  private CassandraSession session;

  @Inject
  public ActivityRepository(CassandraSession session) {
    this.session = session;
  }

  public Flow<ActivityEntity, ActivityEntity, NotUsed> saveActivity() {
    Function2<ActivityEntity, PreparedStatement, BoundStatement> statementBinder =
        (activity, prepStmt) ->
            prepStmt.bind(
                activity.getKey().getActivityId(),
                activity.getKey().getStageId(),
                activity.getKey().getTableId(),
                activity.getUserId(),
                activity.getName(),
                activity.getState().name(),
                toInstant(activity.getCreatedAt()),
                toInstant(activity.getModifiedAt()));

    return CassandraFlow.create(session, defaults(), INSERT_ACTIVITY_STMT, statementBinder);
  }

  public Flow<ActivityEntity, ActivityEntity, NotUsed> updateActivity() {
    Function2<ActivityEntity, PreparedStatement, BoundStatement> statementBinder =
        (activity, prepStmt) ->
            prepStmt.bind(
                activity.getName(),
                activity.getState().name(),
                toInstant(activity.getModifiedAt()),
                activity.getKey().getTableId(),
                activity.getKey().getStageId(),
                activity.getKey().getActivityId(),
                activity.getUserId());

    return CassandraFlow.create(session, defaults(), UPDATE_STMT, statementBinder);
  }

  public Flow<ActivityKey, ActivityKey, NotUsed> deleteActivity(UUID userId) {
    Function2<ActivityKey, PreparedStatement, BoundStatement> statementBinder =
        (key, prepStmt) ->
            prepStmt.bind(key.getTableId(), key.getStageId(), key.getActivityId(), userId);

    return CassandraFlow.create(session, defaults(), DELETE_BY_ID_STMT, statementBinder);
  }

  public Source<ActivityEntity, NotUsed> getById(ActivityKey activityKey, UUID userId) {
    Statement<?> stmt =
        SimpleStatement.newInstance(
            SELECT_ACTIVITY_BY_ID_STMT,
            userId,
            activityKey.getTableId(),
            activityKey.getStageId(),
            activityKey.getActivityId());

    return CassandraSource.create(session, stmt).map(this::fromRow);
  }

  public Source<ActivityEntity, NotUsed> getStageActivities(StageKey key, UUID userId) {
    Statement<?> stmt =
        SimpleStatement.newInstance(
            SELECT_ACTIVITIES_BY_TABLE_ID_AND_STAGE_ID_STMT,
            key.getTableId(),
            key.getStageId(),
            userId);

    return CassandraSource.create(session, stmt).map(this::fromRow);
  }

  private ActivityEntity fromRow(Row row) {
    var key =
        ActivityKey.builder()
            .tableId(row.get(TABLE_ID_COL, UUID.class))
            .stageId(row.get(STAGE_ID_COL, UUID.class))
            .activityId(row.get(ACTIVITY_ID_COL, UUID.class))
            .build();

    return ActivityEntity.builder()
        .key(key)
        .userId(row.get(USER_ID_COL, UUID.class))
        .name(row.getString(NAME_COL))
        .state(valueOf(row.getString(STATE_COL)))
        .modifiedAt(toUtcLocalDate(row.getInstant(MODIFIED_AT_COL)))
        .createdAt(toUtcLocalDate(row.getInstant(CREATED_AT_COL)))
        .build();
  }
}
