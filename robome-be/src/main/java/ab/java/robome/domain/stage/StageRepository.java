package ab.java.robome.domain.stage;

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

import ab.java.robome.common.time.TimeUtils;
import ab.java.robome.domain.stage.model.Stage;
import ab.java.robome.domain.stage.model.StageId;
import ab.java.robome.domain.table.model.TableState;
import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.alpakka.cassandra.javadsl.CassandraSink;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class StageRepository {

	private static final String INSERT_STAGE_STMT = "INSERT INTO robome.stages (stage_id, table_id, name, state, "
			+ "created_at, modified_at) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String SELECT_STAGE_BY_ID_STMT = "SELECT * FROM robome.stages WHERE table_id = ? AND stage_id = ?";
	private static final String SELECT_STAGES_BY_TABLE_ID_STMT = "SELECT * FROM robome.stages WHERE table_id = ?";

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
			Date created = TimeUtils.toDate(stage.getCreatedAt());
			Date modified = TimeUtils.toDate(stage.getModifiedAt());
			return statement.bind(stage.getStageId().getStageId(), stage.getStageId().getTableId(), stage.getName(),
					stage.getState().name(), created, modified);
		};

		Sink<Stage, CompletionStage<Done>> sink = CassandraSink.create(1, preparedStatement, statementBinder, session,
				actorSystem.dispatcher());
		return sink;
	}

	public Source<Stage, NotUsed> getTableStages(UUID tableUuid) {
		PreparedStatement preparedStatement = session.prepare(SELECT_STAGES_BY_TABLE_ID_STMT);
		BoundStatement bound = preparedStatement.bind(tableUuid);

		return Source.from(session.execute(bound).all().stream().map(this::fromRow).collect(Collectors.toList()));
	}

	public Source<Optional<Stage>, NotUsed> getById(StageId stageId) {
		PreparedStatement preparedStatement = session.prepare(SELECT_STAGE_BY_ID_STMT);
		BoundStatement bound = preparedStatement.bind(stageId.getTableId(), stageId.getStageId());

		ResultSet r = session.execute(bound);

		Row row = r.one();
		if (row == null) {
			return Source.single(Optional.empty());
		}

		Stage stage = fromRow(row);
		return Source.single(Optional.of(stage));
	}

	private Stage fromRow(Row row) {
		StageId id = new StageId(row.get("table_id", UUID.class), row.get("stage_id", UUID.class));

		return new Stage(id, row.getString("name"), TableState.valueOf(row.getString("state")),
				TimeUtils.toUtcLocalDate(row.getTimestamp("created_at")),
				TimeUtils.toUtcLocalDate(row.getTimestamp("modified_at")));

	}

}
