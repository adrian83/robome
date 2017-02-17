package ab.java.robome.table;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.google.inject.Inject;

import akka.NotUsed;
import akka.stream.alpakka.cassandra.javadsl.CassandraSource;
import akka.stream.javadsl.Source;

public class TableRepository {

	private Session session;
	
	@Inject
	public TableRepository(Session session) {
	}
	
	
	public Source<Row,NotUsed> blabla() {
		final Statement stmt = new SimpleStatement("SELECT * FROM akka_stream_java_test.test").setFetchSize(20);
	//	final CompletionStage<List<Row>> rows = CassandraSource.create(stmt, session)
	//			  .runWith(Sink.seq(), actorMaterializer);
		
		Source<Row, NotUsed> source = CassandraSource.create(stmt, session);
		return source;
	}
	
	
}
