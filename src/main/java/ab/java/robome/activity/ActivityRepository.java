package ab.java.robome.activity;

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
import com.google.inject.Inject;

import ab.java.robome.activity.model.Activity;
import ab.java.robome.activity.model.ActivityId;
import ab.java.robome.activity.model.ImmutableActivity;
import ab.java.robome.activity.model.ImmutableActivityId;
import ab.java.robome.common.time.TimeUtils;
import ab.java.robome.stage.model.StageId;
import ab.java.robome.table.model.TableState;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.alpakka.cassandra.javadsl.CassandraSink;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class ActivityRepository {

	private static final String INSERT_ACTIVITY_STMT = "INSERT INTO robome.activities (id, stage_id, table_id, name, state, "
			+ "created_at, modified_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
	private static final String SELECT_ACTIVITY_BY_ID_STMT = "SELECT * FROM robome.activities WHERE table_id = ? AND stage_id = ? AND id = ?";
	private static final String SELECT_ACTIVITIES_BY_TABLE_ID_AND_STAGE_ID_STMT = "SELECT * FROM robome.activities WHERE table_id = ? AND stage_id = ?";

	private Session session;
	private ActorSystem actorSystem;

	@Inject
	public ActivityRepository(Session session, ActorSystem actorSystem) {
		this.session = session;
		this.actorSystem = actorSystem;
	}

	public Sink<Activity, CompletionStage<Done>> saveActivity() {
		PreparedStatement preparedStatement = session.prepare(INSERT_ACTIVITY_STMT);
		
		BiFunction<Activity, PreparedStatement, BoundStatement> statementBinder = (activity, statement) -> {
			Date created = TimeUtils.toDate(activity.createdAt());
			Date modified = TimeUtils.toDate(activity.modifiedAt());
			return statement.bind(activity.id().id(), activity.id().stageId(), activity.id().tableId(), activity.name(), activity.state().name(), created, modified);
		};

		Sink<Activity, CompletionStage<Done>> sink = CassandraSink.create(1, preparedStatement, statementBinder, session,
				actorSystem.dispatcher());
		return sink;
	}
	
	public Source<Activity, NotUsed> getStageActivities(StageId stageId) {
		PreparedStatement preparedStatement = session.prepare(SELECT_ACTIVITIES_BY_TABLE_ID_AND_STAGE_ID_STMT);
		BoundStatement bound = preparedStatement.bind(stageId.tableId(), stageId.id());
		
		return Source.from(session.execute(bound)
				.all()
				.stream()
				.map(this::fromRow)
				.collect(Collectors.toList()));
	}

	public Source<Optional<Activity>, NotUsed> getById(ActivityId activityId) {
		PreparedStatement preparedStatement = session.prepare(SELECT_ACTIVITY_BY_ID_STMT);
		BoundStatement bound = preparedStatement.bind(activityId.tableId(), activityId.stageId(), activityId.id());

		ResultSet r = session.execute(bound);

		Row row = r.one();
		if (row == null) {
			return Source.single(Optional.empty());
		}

		Activity activity = fromRow(row);
		return Source.single( Optional.of(activity));
	}

	private Activity fromRow(Row row) {
		ActivityId id = ImmutableActivityId.builder()
				.id(row.get("id", UUID.class))
				.stageId(row.get("stage_id", UUID.class))
				.tableId(row.get("table_id", UUID.class))
				.build();
		
		Activity activity = ImmutableActivity.builder()
				.id(id)
				.name(row.getString("name"))
				.state(TableState.valueOf(row.getString("state")))
				.createdAt(TimeUtils.toUtcLocalDate(row.getTimestamp("created_at")))
				.modifiedAt(TimeUtils.toUtcLocalDate(row.getTimestamp("modified_at")))
				.build();
		return activity;
	}
	

	
	
}
