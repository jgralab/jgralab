package de.uni_koblenz.jgralabtest.instancetest;

import static org.junit.Assert.fail;

import java.sql.SQLException;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.impl.db.DatabasePersistableGraph;
import de.uni_koblenz.jgralab.impl.db.GraphDatabase;
import de.uni_koblenz.jgralab.impl.db.GraphDatabaseException;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestGraph;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestSchema;

public class GraphDatabaseHandler {

	// TODO change to system property
	private static final String url = "jdbc:postgresql://jgtest:secret@localhost:5432/jgtest";

	protected GraphDatabase graphDatabase;

	public GraphDatabaseHandler() {

	}

	public void connectToDatabase() {
		try {
			graphDatabase = GraphDatabase.openGraphDatabase(url);
		} catch (GraphDatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void closeGraphdatabase() {
		try {
			this.graphDatabase.close();
		} catch (GraphDatabaseException e) {
			e.printStackTrace();
		}
	}

	public void loadVertexTestSchemaIntoGraphDatabase() {
		try {
			if (!this.graphDatabase.contains(VertexTestSchema.instance())) {
				this.loadTestSchemaIntoGraphDatabase("testit/testschemas/VertexTestSchema.tg");
			}
		} catch (GraphDatabaseException e) {
			e.printStackTrace();
		}
	}

	public void loadMinimalSchemaIntoGraphDatabase() {
		try {
			if (!this.graphDatabase.contains(MinimalSchema.instance())) {
				GraphIO.loadSchemaIntoGraphDatabase(
						"testit/testschemas/MinimalSchema.tg",
						this.graphDatabase);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadTestSchemaIntoGraphDatabase(String file) {
		try {
			GraphIO.loadSchemaIntoGraphDatabase(file, graphDatabase);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not load " + file + " into graph database.");
		}
	}

	public MinimalGraph createMinimalGraphWithDatabaseSupport(String id) {
		return this.createMinimalGraphWithDatabaseSupport(id, 1000, 1000);
	}

	public MinimalGraph createMinimalGraphWithDatabaseSupport(String id,
			int vMax, int eMax) {
		try {
			return MinimalSchema.instance()
					.createMinimalGraphWithDatabaseSupport(id, vMax, eMax,
							this.graphDatabase);
		} catch (Exception exception) {
			exception.printStackTrace();
			fail("Could not create test graph.");
			return null;
		}
	}

	public VertexTestGraph createVertexTestGraphWithDatabaseSupport(String id) {
		return this.createVertexTestGraphWithDatabaseSupport(id, 1000, 1000);
	}

	public VertexTestGraph createVertexTestGraphWithDatabaseSupport(String id,
			int vMax, int eMax) {
		try {
			return VertexTestSchema.instance()
					.createVertexTestGraphWithDatabaseSupport(id, vMax, eMax,
							this.graphDatabase);
		} catch (Exception exception) {
			exception.printStackTrace();
			fail("Could not create test graph");
			return null;
		}
	}

	public VertexTestGraph loadVertexTestGraphWithDatabaseSupport(String id) {
		try {
			return (VertexTestGraph) GraphIO.loadGraphFromDatabase(id,
					graphDatabase);
		} catch (Exception exception) {
			exception.printStackTrace();
			fail("Could not load graph from database.");
			exception.printStackTrace();
		}
		return null;
	}

	public void cleanDatabaseOfTestGraph(String id) {
		try {
			if (graphDatabase.containsGraph(id)) {
				graphDatabase.deleteGraph(id);
			}
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			fail("Could not delete test graph from database.");
		}
	}

	public void cleanDatabaseOfTestGraph(Graph testGraph) {
		try {
			if (graphDatabase.containsGraph(testGraph.getId())) {
				graphDatabase.delete((DatabasePersistableGraph) testGraph);
			}
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			fail("Could not delete test graph from database.");
		}
	}

	public void cleanDatabaseOfTestSchema(Schema schema) {
		try {
			if (graphDatabase.containsSchema(schema.getPackagePrefix(),
					schema.getName())) {
				graphDatabase.deleteSchema(schema.getPackagePrefix(),
						schema.getName());
			}
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			fail("Could not delete test schema from database.");
		}
	}

	public void clearAllTables() {
		try {
			graphDatabase.clearAllTables();
		} catch (SQLException exception) {
			exception.printStackTrace();
			fail("Could not clear all tables.");
		}
	}

	public GraphDatabase getGraphDatabase() {
		return graphDatabase;
	}

	public static void main(String[] args) throws GraphDatabaseException {
		GraphDatabaseHandler handler = new GraphDatabaseHandler();
		handler.connectToDatabase();
		handler.graphDatabase.applyDbSchema();
		handler.graphDatabase.optimizeForGraphTraversal();
	}

}
