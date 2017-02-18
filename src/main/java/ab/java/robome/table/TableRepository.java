package ab.java.robome.table;

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
import com.datastax.driver.core.utils.UUIDs;
import com.google.inject.Inject;

import ab.java.robome.table.model.NewTable;
import ab.java.robome.table.model.Table;
import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.alpakka.cassandra.javadsl.CassandraSink;
import akka.stream.alpakka.cassandra.javadsl.CassandraSource;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class TableRepository {

	private static final String INSERT_TABLE_STMT = "INSERT INTO robome.table (id, name) VALUES (?, ?)";
	private static final String SELECT_ALL_STMT = "SELECT * FROM robome.table";
	//private static final String SELECT_BY_ID_STMT = "SELECT * FROM robome.table WHERE id = ?";

	private Session session;
	private ActorSystem actorSystem;

	@Inject
	public TableRepository(Session session, ActorSystem actorSystem) {
		this.session = session;
		this.actorSystem = actorSystem;
	}

	public Sink<NewTable, CompletionStage<Done>> saveTable() {
		PreparedStatement preparedStatement = session.prepare(INSERT_TABLE_STMT);

		BiFunction<NewTable, PreparedStatement, BoundStatement> statementBinder = (tab, statement) -> {
			return statement.bind(UUIDs.random(), tab.getName());
		};

		Sink<NewTable, CompletionStage<Done>> sink = CassandraSink.create(1, preparedStatement, statementBinder,
				session, actorSystem.dispatcher());
		return sink;
	}
	
	public Optional<Table> getById(UUID tableId) {
		PreparedStatement preparedStatement = session.prepare(INSERT_TABLE_STMT);
		BoundStatement bound = preparedStatement.bind(tableId);
		
		ResultSet r = session.execute(bound);
		
		Row row = r.one();
		if(row == null){
			return Optional.empty();
		}
		
		UUID id = row.get("id", UUID.class);
		String name = row.getString("name");
		return Optional.of(new Table(name,id));
	}

	public Source<Row, NotUsed> getAllTables() {
		final Statement stmt = new SimpleStatement(SELECT_ALL_STMT).setFetchSize(1000);
		Source<Row, NotUsed> source = CassandraSource.create(stmt, session);
		return source;
	}

}
