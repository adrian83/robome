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
import com.github.adrian83.robome.domain.activity.model.Activity;
import com.github.adrian83.robome.domain.activity.model.ActivityKey;
import com.github.adrian83.robome.domain.activity.model.ActivityState;
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
      "SELECT * FROM robome.activities WHERE table_id = ? AND stage_id = ? AND activity_id = ?";
  private static final String SELECT_ACTIVITIES_BY_TABLE_ID_AND_STAGE_ID_STMT =
      "SELECT * FROM robome.activities WHERE table_id = ? AND stage_id = ? AND user_id = ? ALLOW FILTERING";

  private Session session;

  @Inject
  public ActivityRepository(Session session) {
    this.session = session;
  }

  public Sink<Activity, CompletionStage<Done>> saveActivity() {

    PreparedStatement preparedStatement = session.prepare(INSERT_ACTIVITY_STMT);

    BiFunction<Activity, PreparedStatement, BoundStatement> statementBinder =
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

  public Source<Activity, NotUsed> getStageActivities(UUID userId, StageKey stageId) {
	  
    PreparedStatement preparedStatement =
        session.prepare(SELECT_ACTIVITIES_BY_TABLE_ID_AND_STAGE_ID_STMT);
    BoundStatement bound =
        preparedStatement.bind(stageId.getTableId(), stageId.getStageId(), userId);

    return Source.from(
        session.execute(bound).all().stream().map(this::fromRow).collect(Collectors.toList()));
  }

  public Source<Optional<Activity>, NotUsed> getById(ActivityKey activityId) {
    PreparedStatement preparedStatement = session.prepare(SELECT_ACTIVITY_BY_ID_STMT);
    BoundStatement bound =
        preparedStatement.bind(
            activityId.getTableId(), activityId.getStageId(), activityId.getActivityId());

    ResultSet r = session.execute(bound);

    Row row = r.one();
    if (row == null) {
      return Source.single(Optional.empty());
    }

    Activity activity = fromRow(row);
    return Source.single(Optional.of(activity));
  }

  private Activity fromRow(Row row) {

    ActivityKey id =
        new ActivityKey(
            row.get("table_id", UUID.class),
            row.get("stage_id", UUID.class),
            row.get("activity_id", UUID.class));

    return new Activity(
        id,
        row.get("user_id", UUID.class),
        row.getString("name"),
        ActivityState.valueOf(row.getString("state")),
        TimeUtils.toUtcLocalDate(row.getTimestamp("created_at")),
        TimeUtils.toUtcLocalDate(row.getTimestamp("modified_at")));
  }
}
