package com.github.adrian83.robome.domain.activity;

import static com.github.adrian83.robome.common.Time.toDate;
import static com.github.adrian83.robome.common.Time.toUtcLocalDate;
import static com.github.adrian83.robome.domain.activity.model.ActivityState.valueOf;
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
import com.github.adrian83.robome.domain.activity.model.ActivityEntity;
import com.github.adrian83.robome.domain.activity.model.ActivityKey;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.google.inject.Inject;

import akka.Done;
import akka.NotUsed;
import akka.stream.alpakka.cassandra.javadsl.CassandraSink;
import akka.stream.javadsl.Sink;
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

  private Session session;

  private Sink<ActivityEntity, CompletionStage<Done>> saveActivitySink;
  private Sink<ActivityEntity, CompletionStage<Done>> updateActivitySink;

  private PreparedStatement deleteActivityStmt;
  private PreparedStatement findActivitiesByStageStmt;
  private PreparedStatement findActivityByIdStmt;

  @Inject
  public ActivityRepository(Session session) {
    this.session = session;

    var saveActivityStmt = session.prepare(INSERT_ACTIVITY_STMT);
    saveActivitySink = CassandraSink.create(1, saveActivityStmt, this::bindInsertedStage, session);

    var updateActivityStmt = session.prepare(UPDATE_STMT);
    updateActivitySink =
        CassandraSink.create(1, updateActivityStmt, this::bindUpdatedStage, session);

    deleteActivityStmt = session.prepare(DELETE_BY_ID_STMT);
    findActivitiesByStageStmt = session.prepare(SELECT_ACTIVITIES_BY_TABLE_ID_AND_STAGE_ID_STMT);
    findActivityByIdStmt = session.prepare(SELECT_ACTIVITY_BY_ID_STMT);
  }

  public Sink<ActivityEntity, CompletionStage<Done>> saveActivity() {
    return saveActivitySink;
  }

  public Sink<ActivityEntity, CompletionStage<Done>> updateActivity() {
    return updateActivitySink;
  }

  public Sink<ActivityKey, CompletionStage<Done>> deleteActivity(UUID userId) {
    BiFunction<ActivityKey, PreparedStatement, BoundStatement> boundStmt =
        (key, stmt) -> stmt.bind(key.getTableId(), key.getStageId(), key.getActivityId(), userId);
    return CassandraSink.create(1, deleteActivityStmt, boundStmt, session);
  }

  public Source<ActivityEntity, NotUsed> getStageActivities(UUID userId, StageKey key) {
    var bound = findActivitiesByStageStmt.bind(key.getTableId(), key.getStageId(), userId);
    var entities = session.execute(bound).all().stream().map(this::fromRow).collect(toList());
    return Source.from(entities);
  }

  public Source<Optional<ActivityEntity>, NotUsed> getById(ActivityKey activityKey, UUID userId) {
    BoundStatement bound =
        findActivityByIdStmt.bind(
            userId,
            activityKey.getTableId(),
            activityKey.getStageId(),
            activityKey.getActivityId());

    return Optional.ofNullable(session.execute(bound))
        .map(ResultSet::one)
        .map(this::fromRow)
        .map(Optional::of)
        .map(Source::single)
        .orElse(Source.single(Optional.empty()));
  }

  private ActivityEntity fromRow(Row row) {
    var key =
        ActivityKey.builder()
            .tableId(row.get(TABLE_ID_COL, UUID.class))
            .stageId(row.get(STAGE_ID_COL, UUID.class))
            .activityId(row.get(ACTIVITY_ID_COL, UUID.class))
            .build();

    return new ActivityEntity(
        key,
        row.get(USER_ID_COL, UUID.class),
        row.getString(NAME_COL),
        valueOf(row.getString(STATE_COL)),
        toUtcLocalDate(row.getTimestamp(CREATED_AT_COL)),
        toUtcLocalDate(row.getTimestamp(MODIFIED_AT_COL)));
  }

  private BoundStatement bindInsertedStage(ActivityEntity activity, PreparedStatement statement) {
    return statement.bind(
        activity.getKey().getActivityId(),
        activity.getKey().getStageId(),
        activity.getKey().getTableId(),
        activity.getUserId(),
        activity.getName(),
        activity.getState().name(),
        toDate(activity.getCreatedAt()),
        toDate(activity.getModifiedAt()));
  }

  private BoundStatement bindUpdatedStage(ActivityEntity activity, PreparedStatement statement) {
    return statement.bind(
        activity.getName(),
        activity.getState().name(),
        toDate(activity.getModifiedAt()),
        activity.getKey().getTableId(),
        activity.getKey().getStageId(),
        activity.getKey().getActivityId(),
        activity.getUserId());
  }
}
