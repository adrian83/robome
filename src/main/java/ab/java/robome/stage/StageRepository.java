package ab.java.robome.stage;

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
import com.google.inject.Inject;

import ab.java.robome.common.time.TimeUtils;
import ab.java.robome.stage.model.ImmutableStage;
import ab.java.robome.stage.model.ImmutableStageId;
import ab.java.robome.stage.model.Stage;
import ab.java.robome.stage.model.StageId;
import ab.java.robome.table.model.TableState;
import akka.Done;
import akka.actor.ActorSystem;
import akka.stream.alpakka.cassandra.javadsl.CassandraSink;
import akka.stream.javadsl.Sink;

public class StageRepository {

	private static final String INSERT_STAGE_STMT = "INSERT INTO robome.stages (id, table_id, name, state, "
			+ "created_at, modified_at) VALUES (?, ?, ?, ?, ?, ?)";
	//private static final String SELECT_TABLE_STAGES_STMT = "SELECT * FROM robome.stages WHERE table_id = ?";
	private static final String SELECT_STAGE_BY_ID_STMT = "SELECT * FROM robome.stages WHERE table_id = ? AND id = ?";
	

	private Session session;
	private ActorSystem actorSystem;

	@Inject
	public StageRepository(Session session, ActorSystem actorSystem) {
		this.session = session;
		this.actorSystem = actorSystem;
	}

	public Sink<Stage, CompletionStage<Done>> saveStage() {
		PreparedStatement preparedStatement = session.prepare(INSERT_STAGE_STMT);
		
		BiFunction<Stage, PreparedStatement, BoundStatement> statementBinder = (stage, statement) -> {
			Date created = TimeUtils.toDate(stage.createdAt());
			Date modified = TimeUtils.toDate(stage.modifiedAt());
			return statement.bind(stage.stageId().id(), stage.stageId().tableId(), stage.name(), stage.state().name(), created, modified);
		};

		Sink<Stage, CompletionStage<Done>> sink = CassandraSink.create(1, preparedStatement, statementBinder, session,
				actorSystem.dispatcher());
		return sink;
	}

	public Optional<Stage> getById(StageId stageId) {
		PreparedStatement preparedStatement = session.prepare(SELECT_STAGE_BY_ID_STMT);
		BoundStatement bound = preparedStatement.bind(stageId.tableId(), stageId.id());

		ResultSet r = session.execute(bound);

		Row row = r.one();
		if (row == null) {
			return Optional.empty();
		}
		
		StageId id = ImmutableStageId.builder()
				.id(row.get("id", UUID.class))
				.tableId(row.get("table_id", UUID.class))
				.build();
		
		Stage stage = ImmutableStage.builder()
				.stageId(id)
				.name(row.getString("name"))
				.state(TableState.valueOf(row.getString("state")))
				.createdAt(TimeUtils.toUtcLocalDate(row.getTimestamp("created_at")))
				.modifiedAt(TimeUtils.toUtcLocalDate(row.getTimestamp("modified_at")))
				.build();

		return Optional.of(stage);
	}


	
	
}
