package com.github.adrian83.robome.domain.activity;

import java.util.Date;
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
import com.github.adrian83.robome.domain.stage.StageId;
import com.github.adrian83.robome.domain.table.TableState;
import com.google.inject.Inject;

import akka.Done;
import akka.NotUsed;
import akka.stream.alpakka.cassandra.javadsl.CassandraSink;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class ActivityRepository {

	private static final String INSERT_ACTIVITY_STMT = "INSERT INTO robome.activities (activity_id, stage_id, table_id, name, state, "
			+ "created_at, modified_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
	private static final String SELECT_ACTIVITY_BY_ID_STMT = "SELECT * FROM robome.activities WHERE table_id = ? AND stage_id = ? AND activity_id = ?";
	private static final String SELECT_ACTIVITIES_BY_TABLE_ID_AND_STAGE_ID_STMT = "SELECT * FROM robome.activities WHERE table_id = ? AND stage_id = ?";

	private Session session;

	@Inject
	public ActivityRepository(Session session) {
		this.session = session;
	}

	public Sink<Activity, CompletionStage<Done>> saveActivity() {
		PreparedStatement preparedStatement = session.prepare(INSERT_ACTIVITY_STMT);

		BiFunction<Activity, PreparedStatement, BoundStatement> statementBinder = (activity, statement) -> {
			Date created = TimeUtils.toDate(activity.getCreatedAt());
			Date modified = TimeUtils.toDate(activity.getModifiedAt());
			return statement.bind(activity.getId().getActivityId(), activity.getId().getStageId(),
					activity.getId().getTableId(), activity.getName(), activity.getState().name(), created, modified);
		};

		Sink<Activity, CompletionStage<Done>> sink = CassandraSink.create(1, preparedStatement, statementBinder,
				session);

		return sink;
	}

	public Source<Activity, NotUsed> getStageActivities(StageId stageId) {
		PreparedStatement preparedStatement = session.prepare(SELECT_ACTIVITIES_BY_TABLE_ID_AND_STAGE_ID_STMT);
		BoundStatement bound = preparedStatement.bind(stageId.getTableId(), stageId.getStageId());

		return Source.from(session.execute(bound).all().stream().map(this::fromRow).collect(Collectors.toList()));
	}

	public Source<Optional<Activity>, NotUsed> getById(ActivityId activityId) {
		PreparedStatement preparedStatement = session.prepare(SELECT_ACTIVITY_BY_ID_STMT);
		BoundStatement bound = preparedStatement.bind(activityId.getTableId(), activityId.getStageId(),
				activityId.getActivityId());

		ResultSet r = session.execute(bound);

		Row row = r.one();
		if (row == null) {
			return Source.single(Optional.empty());
		}

		Activity activity = fromRow(row);
		return Source.single(Optional.of(activity));
	}

	private Activity fromRow(Row row) {

		ActivityId id = new ActivityId(row.get("table_id", UUID.class), row.get("stage_id", UUID.class),
				row.get("activity_id", UUID.class));

		return new Activity(id, row.getString("name"), TableState.valueOf(row.getString("state")),
				TimeUtils.toUtcLocalDate(row.getTimestamp("created_at")),
				TimeUtils.toUtcLocalDate(row.getTimestamp("modified_at")));
	}

}
