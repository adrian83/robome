package ab.java.robome.domain.table;

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
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.google.inject.Inject;

import ab.java.robome.common.time.TimeUtils;
import ab.java.robome.domain.table.model.ImmutableTable;
import ab.java.robome.domain.table.model.ImmutableTableId;
import ab.java.robome.domain.table.model.Table;
import ab.java.robome.domain.table.model.TableId;
import ab.java.robome.domain.table.model.TableState;
import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.alpakka.cassandra.javadsl.CassandraSink;
import akka.stream.alpakka.cassandra.javadsl.CassandraSource;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class TableRepository {

	private static final String INSERT_TABLE_STMT = "INSERT INTO robome.tables (table_id, title, state, "
			+ "created_at, modified_at) VALUES (?, ?, ?, ?, ?)";
	private static final String SELECT_ALL_STMT = "SELECT * FROM robome.tables";
	private static final String SELECT_BY_ID_STMT = "SELECT * FROM robome.tables WHERE table_id = ?";

	private Session session;
	private ActorSystem actorSystem;

	@Inject
	public TableRepository(Session session, ActorSystem actorSystem) {
		this.session = session;
		this.actorSystem = actorSystem;
	}

	public Sink<Table, CompletionStage<Done>> saveTable() {
		PreparedStatement preparedStatement = session.prepare(INSERT_TABLE_STMT);
		
		BiFunction<Table, PreparedStatement, BoundStatement> statementBinder = (tab, statement) -> {
			Date created = TimeUtils.toDate(tab.createdAt());
			Date modified = TimeUtils.toDate(tab.modifiedAt());
			return statement.bind(tab.id().tableId(), tab.title(), tab.state().name(), created, modified);
		};

		Sink<Table, CompletionStage<Done>> sink = CassandraSink.create(1, preparedStatement, statementBinder, session,
				actorSystem.dispatcher());
		return sink;
	}

	public Source<Optional<Table>,NotUsed> getById(UUID tableId) {
		PreparedStatement preparedStatement = session.prepare(SELECT_BY_ID_STMT);
		BoundStatement bound = preparedStatement.bind(tableId);
		ResultSet r = session.execute(bound);
		return Source.single(Optional.ofNullable(r.one()).map(this::fromRow));
	}

	public Source<Table, NotUsed> getAllTables() {
		final Statement stmt = new SimpleStatement(SELECT_ALL_STMT);
		Source<Row, NotUsed> source = CassandraSource.create(stmt, session);
		return source.map(this::fromRow);
	}
	
	private Table fromRow(Row row) {
		
		TableId id = ImmutableTableId.builder()
				.tableId(row.get("table_id", UUID.class))
				.build();
		
		Table table = ImmutableTable.builder()
				.id(id)
				.title(row.getString("title"))
				.state(TableState.valueOf(row.getString("state")))
				.createdAt(TimeUtils.toUtcLocalDate(row.getTimestamp("created_at")))
				.modifiedAt(TimeUtils.toUtcLocalDate(row.getTimestamp("modified_at")))
				.build();
		return table;
	}

}
