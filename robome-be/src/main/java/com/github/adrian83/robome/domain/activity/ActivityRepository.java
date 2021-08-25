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
                activity.key().activityId(),
                activity.key().stageId(),
                activity.key().tableId(),
                activity.userId(),
                activity.name(),
                activity.state().name(),
                toInstant(activity.createdAt()),
                toInstant(activity.modifiedAt()));

    return CassandraFlow.create(session, defaults(), INSERT_ACTIVITY_STMT, statementBinder);
  }

  public Flow<ActivityEntity, ActivityEntity, NotUsed> updateActivity() {
    Function2<ActivityEntity, PreparedStatement, BoundStatement> statementBinder =
        (activity, prepStmt) ->
            prepStmt.bind(
                activity.name(),
                activity.state().name(),
                toInstant(activity.modifiedAt()),
                activity.key().tableId(),
                activity.key().stageId(),
                activity.key().activityId(),
                activity.userId());

    return CassandraFlow.create(session, defaults(), UPDATE_STMT, statementBinder);
  }

  public Flow<ActivityKey, ActivityKey, NotUsed> deleteActivity(UUID userId) {
    Function2<ActivityKey, PreparedStatement, BoundStatement> statementBinder =
        (key, prepStmt) -> prepStmt.bind(key.tableId(), key.stageId(), key.activityId(), userId);

    return CassandraFlow.create(session, defaults(), DELETE_BY_ID_STMT, statementBinder);
  }

  public Source<ActivityEntity, NotUsed> getById(ActivityKey activityKey, UUID userId) {
    Statement<?> stmt =
        SimpleStatement.newInstance(
            SELECT_ACTIVITY_BY_ID_STMT,
            userId,
            activityKey.tableId(),
            activityKey.stageId(),
            activityKey.activityId());

    return CassandraSource.create(session, stmt).map(this::fromRow);
  }

  public Source<ActivityEntity, NotUsed> getStageActivities(StageKey key, UUID userId) {
    Statement<?> stmt =
        SimpleStatement.newInstance(
            SELECT_ACTIVITIES_BY_TABLE_ID_AND_STAGE_ID_STMT,
            key.tableId(),
            key.stageId(),
            userId);

    return CassandraSource.create(session, stmt).map(this::fromRow);
  }

  private ActivityEntity fromRow(Row row) {
    var key =
        new ActivityKey(
            row.get(TABLE_ID_COL, UUID.class),
            row.get(STAGE_ID_COL, UUID.class),
            row.get(ACTIVITY_ID_COL, UUID.class));

    return new ActivityEntity(
        key,
        row.get(USER_ID_COL, UUID.class),
        row.getString(NAME_COL),
        valueOf(row.getString(STATE_COL)),
        toUtcLocalDate(row.getInstant(MODIFIED_AT_COL)),
        toUtcLocalDate(row.getInstant(CREATED_AT_COL)));
  }
}
