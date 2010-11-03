package de.uni_koblenz.jgralab.impl.db;

import static junit.framework.Assert.fail;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestGraph;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestSchema;

public abstract class ImplTest {

	protected String url = "postgresql://localhost:5432/graphdatabase_test/";
	protected String userName = "postgres";
	protected String password = "energizer";

	protected VertexTestGraph vertexTestGraph;

	protected VertexTestGraph createVertexTestGraphWithDatabaseSupport(
			String id, int vMax, int eMax) {
		try {
			GraphDatabase database = GraphDatabase.openGraphDatabase(url,
					userName, password);
			if (!database.contains(VertexTestSchema.instance()))
				GraphIO.loadSchemaIntoGraphDatabase(
						"testit/testschemas/VertexTestSchema.tg", database);
			return VertexTestSchema.instance()
					.createVertexTestGraphWithDatabaseSupport(id, vMax, eMax,
							database);
		} catch (Exception exception) {
			exception.printStackTrace();
			fail("Could not create test graph in database.");
			return null;
		}
	}

	protected void cleanDatabaseOfTestGraph(Graph graph) {
		try {
			GraphDatabase database = GraphDatabase.openGraphDatabase(url,
					userName, password);
			if (database.containsGraph(graph.getId()))
				database.delete((DatabasePersistableGraph) graph);
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			fail("Could not delete test graph from database.");
		}
	}
}
