package com.github.adrian83.robome.domain.table;

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
import com.github.adrian83.robome.common.time.TimeUtils;
import com.github.adrian83.robome.domain.table.Table;
import com.google.inject.Inject;

import akka.Done;
import akka.NotUsed;
import akka.stream.alpakka.cassandra.javadsl.CassandraSink;
import akka.stream.alpakka.cassandra.javadsl.CassandraSource;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class TableRepository {

	private static final String INSERT_TABLE_STMT = "INSERT INTO robome.tables (table_id, "
			+ "user_id, title, description, state, created_at, modified_at) " + "VALUES (?, ?, ?, ?, ?, ?, ?)";
	private static final String SELECT_ALL_STMT = "SELECT * FROM robome.tables";
	private static final String SELECT_BY_EMAIL_STMT = "SELECT * FROM robome.tables WHERE user_id = ?";
	private static final String SELECT_BY_ID_STMT = "SELECT * FROM robome.tables WHERE user_id = ? AND table_id = ?";

	private Session session;

	@Inject
	public TableRepository(Session session) {
		this.session = session;
	}

	public Sink<Table, CompletionStage<Done>> saveTable() {
		PreparedStatement preparedStatement = session.prepare(INSERT_TABLE_STMT);

		BiFunction<Table, PreparedStatement, BoundStatement> statementBinder = (tab, statement) -> {
			Date created = TimeUtils.toDate(tab.getCreatedAt());
			Date modified = TimeUtils.toDate(tab.getModifiedAt());
			return statement.bind(tab.getId().getTableId(), tab.getUserId(), tab.getTitle(), tab.getDescription(),
					tab.getState().name(), created, modified);
		};

		Sink<Table, CompletionStage<Done>> sink = CassandraSink.create(1, preparedStatement, statementBinder, session);
		return sink;
	}

	public Source<Optional<Table>, NotUsed> getById(UUID userId, UUID tableId) {
		PreparedStatement preparedStatement = session.prepare(SELECT_BY_ID_STMT);
		BoundStatement bound = preparedStatement.bind(userId, tableId);
		ResultSet r = session.execute(bound);
		return Source.single(Optional.ofNullable(r.one()).map(this::fromRow));
	}

	public Source<Table, NotUsed> getAllTables() {
		final Statement stmt = new SimpleStatement(SELECT_ALL_STMT);
		Source<Row, NotUsed> source = CassandraSource.create(stmt, session);
		return source.map(this::fromRow);
	}

	private Table fromRow(Row row) {

		TableId id = new TableId(row.get("table_id", UUID.class));

		return new Table(id, row.getUUID("user_id"), row.getString("title"), row.getString("description"),
				TableState.valueOf(row.getString("state")), TimeUtils.toUtcLocalDate(row.getTimestamp("created_at")),
				TimeUtils.toUtcLocalDate(row.getTimestamp("modified_at")));
	}

	public Source<Table, NotUsed> getUserTables(UUID userId) {
		PreparedStatement preparedStatement = session.prepare(SELECT_BY_EMAIL_STMT);
		BoundStatement bound = preparedStatement.bind(userId);
		Source<Row, NotUsed> source = CassandraSource.create(bound, session);
		return source.map(this::fromRow);

	}

}
