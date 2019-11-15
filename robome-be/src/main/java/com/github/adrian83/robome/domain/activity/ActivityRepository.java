package com.github.adrian83.robome.domain.activity;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.github.adrian83.robome.common.time.TimeUtils;
import com.github.adrian83.robome.domain.activity.model.ActivityEntity;
import com.github.adrian83.robome.domain.activity.model.ActivityKey;
import com.github.adrian83.robome.domain.activity.model.ActivityState;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.domain.user.model.User;
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
  private static final String DELETE_BY_ID_STMT = "DELETE FROM robome.activities WHERE table_id = ? AND stage_id = ? AND activity_id = ? AND user_id = ?";
  private static final String UPDATE_STMT =
	      "UPDATE robome.activities SET name = ?, state = ?, modified_at = ? WHERE table_id = ? AND stage_id = ? AND activity_id = ? AND user_id = ?";
  
  private Session session;

  @Inject
  public ActivityRepository(Session session) {
    this.session = session;
  }

  public Sink<ActivityEntity, CompletionStage<Done>> saveActivity() {

    PreparedStatement preparedStatement = session.prepare(INSERT_ACTIVITY_STMT);

    BiFunction<ActivityEntity, PreparedStatement, BoundStatement> statementBinder =
        (activity, statement) ->
            statement.bind(
                activity.getKey().getActivityId(),
                activity.getKey().getStageId(),
                activity.getKey().getTableId(),
                activity.getUserId(),
                activity.getName(),
                activity.getState().name(),
                TimeUtils.toDate(activity.getCreatedAt()),
                TimeUtils.toDate(activity.getModifiedAt()));

    return CassandraSink.create(1, preparedStatement, statementBinder, session);
  }
  
  public Sink<ActivityEntity, CompletionStage<Done>> updateActivity(ActivityEntity activity) {

	    PreparedStatement preparedStatement = session.prepare(UPDATE_STMT);
	    BiFunction<ActivityEntity, PreparedStatement, BoundStatement> boundStmt =
	        (act, stmt) ->
	            stmt.bind(
	            		act.getName(),
	            		act.getState().name(),
	                TimeUtils.toDate(act.getModifiedAt()),
	                act.getKey().getTableId(),
	                act.getKey().getStageId(),
	                act.getKey().getActivityId(),
	                act.getUserId());
	    return CassandraSink.create(1, preparedStatement, boundStmt, session);
	  }

  public Sink<ActivityKey, CompletionStage<Done>> deleteActivity(ActivityKey activityId, UUID userId) {
	    PreparedStatement preparedStatement = session.prepare(DELETE_BY_ID_STMT);
	    BiFunction<ActivityKey, PreparedStatement, BoundStatement> boundStmt =
	        (actKey, stmt) -> stmt.bind(actKey.getTableId(), actKey.getStageId(), actKey.getActivityId(), userId);
	    return CassandraSink.create(1, preparedStatement, boundStmt, session);
	  }
  
  public Source<ActivityEntity, NotUsed> getStageActivities(UUID userId, StageKey stageId) {
	  
    PreparedStatement preparedStatement =
        session.prepare(SELECT_ACTIVITIES_BY_TABLE_ID_AND_STAGE_ID_STMT);
    BoundStatement bound =
        preparedStatement.bind(stageId.getTableId(), stageId.getStageId(), userId);

    return Source.from(
        session.execute(bound).all().stream().map(this::fromRow).collect(Collectors.toList()));
  }

  public Source<Optional<ActivityEntity>, NotUsed> getById(ActivityKey activityId, User user) {
    PreparedStatement preparedStatement = session.prepare(SELECT_ACTIVITY_BY_ID_STMT);
    BoundStatement bound =
        preparedStatement.bind(user.getId(), 
            activityId.getTableId(), activityId.getStageId(), activityId.getActivityId());

    ResultSet r = session.execute(bound);

    Row row = r.one();
    if (row == null) {
      return Source.single(Optional.empty());
    }

    ActivityEntity activity = fromRow(row);
    return Source.single(Optional.of(activity));
  }

  private ActivityEntity fromRow(Row row) {

    ActivityKey id =
        new ActivityKey(
            row.get("table_id", UUID.class),
            row.get("stage_id", UUID.class),
            row.get("activity_id", UUID.class));

    return new ActivityEntity(
        id,
        row.get("user_id", UUID.class),
        row.getString("name"),
        ActivityState.valueOf(row.getString("state")),
        TimeUtils.toUtcLocalDate(row.getTimestamp("created_at")),
        TimeUtils.toUtcLocalDate(row.getTimestamp("modified_at")));
  }
}
