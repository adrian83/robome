package ab.java.robome.table;

import java.time.ZonedDateTime;
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

import ab.java.robome.common.time.UTCUtils;
import ab.java.robome.table.model.Table;
import ab.java.robome.table.model.TableState;
import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.alpakka.cassandra.javadsl.CassandraSink;
import akka.stream.alpakka.cassandra.javadsl.CassandraSource;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class TableRepository {

	private static final String INSERT_TABLE_STMT = "INSERT INTO robome.tables (id, name, state, "
			+ "created_at, modified_at) VALUES (?, ?, ?, ?, ?)";
	private static final String SELECT_ALL_STMT = "SELECT * FROM robome.tables";
	private static final String SELECT_BY_ID_STMT = "SELECT * FROM robome.tables WHERE id = ?";

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

			
			Date created = UTCUtils.toDate(tab.getCreatedAt());
			Date modified = UTCUtils.toDate(tab.getModifiedAt());
			
			return statement.bind(tab.getId(), tab.getName(), tab.getState().name(), created, modified);
					

		};

		Sink<Table, CompletionStage<Done>> sink = CassandraSink.create(1, preparedStatement, statementBinder, session,
				actorSystem.dispatcher());
		return sink;
	}

	public Optional<Table> getById(UUID tableId) {
		PreparedStatement preparedStatement = session.prepare(SELECT_BY_ID_STMT);
		BoundStatement bound = preparedStatement.bind(tableId);

		ResultSet r = session.execute(bound);

		Row row = r.one();
		if (row == null) {
			return Optional.empty();
		}

		UUID id = row.get("id", UUID.class);
		String name = row.getString("name");
		TableState state = TableState.valueOf(row.getString("state"));
		Date created = row.getTimestamp("created_at");
		Date modified = row.getTimestamp("modified_at");

		return Optional
				.of(new Table(id, name, state, UTCUtils.toUtcLocalDate(created), UTCUtils.toUtcLocalDate(modified)));
	}

	public Source<Row, NotUsed> getAllTables() {
		final Statement stmt = new SimpleStatement(SELECT_ALL_STMT).setFetchSize(1000);
		Source<Row, NotUsed> source = CassandraSource.create(stmt, session);
		return source;
	}

}
