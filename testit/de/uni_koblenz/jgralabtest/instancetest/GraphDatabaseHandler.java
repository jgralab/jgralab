/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
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
	private static final String url = System.getProperty("jgralabtest_dbconnection");

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
