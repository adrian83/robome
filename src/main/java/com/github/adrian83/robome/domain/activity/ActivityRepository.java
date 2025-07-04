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
import akka.stream.alpakka.cassandra.javadsl.CassandraFlow;
import akka.stream.alpakka.cassandra.javadsl.CassandraSession;
import akka.stream.alpakka.cassandra.javadsl.CassandraSource;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;

public class ActivityRepository {

    private static final String STMT_INSERT_ACTIVITY = "INSERT INTO robome.activities (user_id, activity_id, stage_id, table_id, name, state, created_at, modified_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String STMT_SELECT_ACTIVITY_BY_ID = "SELECT * FROM robome.activities WHERE user_id = ? AND table_id = ? AND stage_id = ? AND activity_id = ?";
    private static final String STMT_SELECT_ACTIVITIES_BY_TABLE_ID_AND_STAGE_ID = "SELECT * FROM robome.activities WHERE table_id = ? AND stage_id = ? AND user_id = ? ALLOW FILTERING";
    private static final String STMT_DELETE_ACTIVITY_BY_ID = "DELETE FROM robome.activities WHERE table_id = ? AND stage_id = ? AND activity_id = ? AND user_id = ?";
    private static final String STMT_UPDATE_ACTIVITY = "UPDATE robome.activities SET name = ?, state = ?, modified_at = ? WHERE table_id = ? AND stage_id = ? AND activity_id = ? AND user_id = ?";

    private static final String COL_TABLE_ID = "table_id";
    private static final String COL_STAGE_ID = "stage_id";
    private static final String COL_ACTIVITY_ID = "activity_id";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_NAME = "name";
    private static final String COL_STATE = "state";
    private static final String COL_CREATED_AT = "created_at";
    private static final String COL_MODIFIED_AT = "modified_at";

    private CassandraSession session;

    @Inject
    public ActivityRepository(CassandraSession session) {
	this.session = session;
    }

    public Flow<ActivityEntity, ActivityEntity, NotUsed> saveActivity() {
	return CassandraFlow.create(session, defaults(), STMT_INSERT_ACTIVITY, this::saveActivityStatement);
    }

    public Flow<ActivityEntity, ActivityEntity, NotUsed> updateActivity() {
	return CassandraFlow.create(session, defaults(), STMT_UPDATE_ACTIVITY, this::updateActivityStatement);
    }

    public Flow<ActivityKey, ActivityKey, NotUsed> deleteActivity(UUID userId) {
	return CassandraFlow.create(session, defaults(), STMT_DELETE_ACTIVITY_BY_ID, this::deleteActivityStatement);
    }

    public Source<ActivityEntity, NotUsed> getById(ActivityKey activityKey) {
	Statement<?> stmt = SimpleStatement.newInstance(STMT_SELECT_ACTIVITY_BY_ID, activityKey.userId(),
		activityKey.tableId(), activityKey.stageId(), activityKey.activityId());

	return CassandraSource.create(session, stmt).map(this::fromRow);
    }

    public Source<ActivityEntity, NotUsed> getStageActivities(StageKey key) {
	Statement<?> stmt = SimpleStatement.newInstance(STMT_SELECT_ACTIVITIES_BY_TABLE_ID_AND_STAGE_ID, key.tableId(),
		key.stageId(), key.userId());

	return CassandraSource.create(session, stmt).map(this::fromRow);
    }

    private ActivityEntity fromRow(Row row) {
	var key = new ActivityKey(row.get(COL_USER_ID, UUID.class), row.get(COL_TABLE_ID, UUID.class),
		row.get(COL_STAGE_ID, UUID.class), row.get(COL_ACTIVITY_ID, UUID.class));

	return new ActivityEntity(key, row.getString(COL_NAME), valueOf(row.getString(COL_STATE)),
		toUtcLocalDate(row.getInstant(COL_MODIFIED_AT)), toUtcLocalDate(row.getInstant(COL_CREATED_AT)));
    }

    private BoundStatement saveActivityStatement(ActivityEntity activity, PreparedStatement prpdStmt) {
	return prpdStmt.bind(activity.key().userId(), activity.key().activityId(), activity.key().stageId(),
		activity.key().tableId(), activity.name(), activity.state().name(), toInstant(activity.createdAt()),
		toInstant(activity.modifiedAt()));
    }

    private BoundStatement updateActivityStatement(ActivityEntity activity, PreparedStatement prpdStmt) {
	return prpdStmt.bind(activity.name(), activity.state().name(), toInstant(activity.modifiedAt()),
		activity.key().tableId(), activity.key().stageId(), activity.key().activityId(),
		activity.key().userId());
    }

    private BoundStatement deleteActivityStatement(ActivityKey key, PreparedStatement prpdStmt) {
	return prpdStmt.bind(key.tableId(), key.stageId(), key.activityId(), key.userId());
    }

}
