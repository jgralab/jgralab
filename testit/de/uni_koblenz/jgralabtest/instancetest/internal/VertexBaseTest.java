/*
* JGraLab - The Java Graph Laboratory
*
* Copyright (C) 2006-2012 Institute for Software Technology
*                         University of Koblenz-Landau, Germany
*                         ist@uni-koblenz.de
*
* For bug reports, documentation and further information, visit
*
*                         https://github.com/jgralab/jgralab
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

package de.uni_koblenz.jgralabtest.instancetest.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.RandomIdGenerator;
import de.uni_koblenz.jgralab.impl.InternalGraph;
import de.uni_koblenz.jgralab.impl.InternalVertex;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralabtest.instancetest.InstanceTest;
import de.uni_koblenz.jgralabtest.schemas.vertextest.AbstractSuperNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.Link;
import de.uni_koblenz.jgralabtest.schemas.vertextest.SuperNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestGraph;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestSchema;

@RunWith(Parameterized.class)
public class VertexBaseTest extends InstanceTest {

	private static final int ITERATIONS = 25;

	public VertexBaseTest(ImplementationType implementationType, String dbURL) {
		super(implementationType, dbURL);
	}

	@Parameters
	public static Collection<Object[]> configure() {
		return getParameters();
	}

	private VertexTestGraph vtg;
	private InternalGraph g;
	private Random rand;

	/**
	 * Creates a new graph for each test.
	 */
	@Before
	public void setUp() {
		if (implementationType == ImplementationType.DATABASE) {
			dbHandler.connectToDatabase();
			dbHandler.loadVertexTestSchemaIntoGraphDatabase();
		}
		vtg = createNewGraph();
		g = (InternalGraph) vtg;
		rand = new Random(System.currentTimeMillis());
	}

	@After
	public void tearDown() {
		if (implementationType == ImplementationType.DATABASE) {
			cleanAndCloseGraphDatabase();
		}
	}

	private void cleanAndCloseGraphDatabase() {
		// dbHandler.cleanDatabaseOfTestGraph("VertexTest");
		// dbHandler.cleanDatabaseOfTestGraph("anotherGraph");
		// for (int i = 0; i < ITERATIONS; i++) {
		// dbHandler.cleanDatabaseOfTestGraph("VertexTest" + i);
		// }
		// for (String id : graphIdsInUse) {
		// dbHandler.cleanDatabaseOfTestGraph(id);
		// }
		// // this.cleanDatabaseOfTestSchema(g.getSchema());
		dbHandler.clearAllTables();
		dbHandler.closeGraphdatabase();
	}

	/*
	 * Test of the Interface Vertex
	 */

	private VertexTestGraph createNewGraph() {
		VertexTestGraph graph = null;
		switch (implementationType) {
		case STANDARD:
			graph = VertexTestSchema.instance().createVertexTestGraph(
					ImplementationType.STANDARD);
			break;
		case TRANSACTION:
			graph = VertexTestSchema.instance().createVertexTestGraph(
					ImplementationType.TRANSACTION);
			break;
		case DATABASE:
			graph = createVertexTestGraphWithDatabaseSupport();
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}
		return graph;
	}

	private ArrayList<String> graphIdsInUse = new ArrayList<String>();

	private VertexTestGraph createVertexTestGraphWithDatabaseSupport() {
		String id = RandomIdGenerator.generateId();
		while (graphIdsInUse.contains(id)) {
			id = RandomIdGenerator.generateId();
		}
		graphIdsInUse.add(id);
		VertexTestGraph graph = dbHandler
				.createVertexTestGraphWithDatabaseSupport(id, 100, 100);
		return graph;
	}

	// tests of the method getNextVertex();
	// (tested in LoadTest, too)

	// tests of the method getIncidenceListVersion()
	/**
	 * If you create and delete edges, only the incidenceListVersions of the
	 * involved nodes may have been increased.
	 *
	 * @throws CommitFailedException
	 */
	@Test
	public void getIncidenceListVersionTest0() throws CommitFailedException {
		createTransaction(g);
		InternalVertex[] nodes = new InternalVertex[3];
		nodes[0] = (InternalVertex) vtg.createSubNode();
		nodes[1] = (InternalVertex) vtg.createDoubleSubNode();
		nodes[2] = (InternalVertex) vtg.createSuperNode();
		commit(g);
		long[] expectedVersions = new long[] { 0, 0, 0 };
		for (int i = 0; i < ITERATIONS; i++) {
			int start = rand.nextInt(2);
			int end = rand.nextInt(2) + 1;
			// create a new edge
			createTransaction(g);
			Link sl = vtg.createLink((AbstractSuperNode) nodes[start],
					(SuperNode) nodes[end]);
			expectedVersions[start]++;
			expectedVersions[end]++;
			commit(g);
			createReadOnlyTransaction(g);
			assertEquals(expectedVersions[0],
					nodes[0].getIncidenceListVersion());
			assertEquals(expectedVersions[1],
					nodes[1].getIncidenceListVersion());
			assertEquals(expectedVersions[2],
					nodes[2].getIncidenceListVersion());
			commit(g);
			// delete an edge
			createTransaction(g);
			g.deleteEdge(sl);
			expectedVersions[start]++;
			expectedVersions[end]++;
			commit(g);
			createReadOnlyTransaction(g);
			assertEquals(expectedVersions[0],
					nodes[0].getIncidenceListVersion());
			assertEquals(expectedVersions[1],
					nodes[1].getIncidenceListVersion());
			assertEquals(expectedVersions[2],
					nodes[2].getIncidenceListVersion());
			commit(g);
		}
	}

	/*
	 * Test of the Interface Vertex
	 */

	// tests of the method isIncidenceListModified(long incidenceListVersion);
	/**
	 * Tests if the incidenceList wasn't modified.
	 *
	 * @throws CommitFailedException
	 */
	@Test
	public void isIncidenceListModifiedTest0() throws CommitFailedException {
		createTransaction(g);
		InternalVertex asn = (InternalVertex) vtg.createSubNode();
		InternalVertex sn = (InternalVertex) vtg.createSuperNode();
		InternalVertex dsn = (InternalVertex) vtg.createDoubleSubNode();
		commit(g);
		createReadOnlyTransaction(g);
		long asnIncidenceListVersion = asn.getIncidenceListVersion();
		long snIncidenceListVersion = sn.getIncidenceListVersion();
		long dsnIncidenceListVersion = dsn.getIncidenceListVersion();
		assertFalse(asn.isIncidenceListModified(asnIncidenceListVersion));
		assertFalse(sn.isIncidenceListModified(snIncidenceListVersion));
		assertFalse(dsn.isIncidenceListModified(dsnIncidenceListVersion));
		commit(g);
	}

	/*
	 * Test of the Interface Vertex
	 */

	/**
	 * If you create and delete edges, only the incidenceLists of the involved
	 * nodes may have been modified.
	 *
	 * @throws CommitFailedException
	 */
	@Test
	public void isIncidenceListModifiedTest1() throws CommitFailedException {
		createTransaction(g);
		InternalVertex[] nodes = new InternalVertex[3];
		long[] versions = new long[3];
		nodes[0] = (InternalVertex) vtg.createSubNode();
		versions[0] = nodes[0].getIncidenceListVersion();
		nodes[1] = (InternalVertex) vtg.createDoubleSubNode();
		versions[1] = nodes[1].getIncidenceListVersion();
		nodes[2] = (InternalVertex) vtg.createSuperNode();
		versions[2] = nodes[2].getIncidenceListVersion();
		commit(g);
		for (int i = 0; i < ITERATIONS; i++) {
			int start = rand.nextInt(2);
			int end = rand.nextInt(2) + 1;
			// create a new edge
			createTransaction(g);
			Link sl = vtg.createLink((AbstractSuperNode) nodes[start],
					(SuperNode) nodes[end]);
			commit(g);
			createReadOnlyTransaction(g);
			assertTrue(nodes[start].isIncidenceListModified(versions[start]));
			assertTrue(nodes[end].isIncidenceListModified(versions[end]));
			if (start != end) {
				assertFalse(nodes[6 - (start + 1) - (end + 1) - 1]
						.isIncidenceListModified(versions[6 - (start + 1)
								- (end + 1) - 1]));
			} else {
				for (int j = 0; j < 3; j++) {
					if (j != start) {
						assertFalse(nodes[j]
								.isIncidenceListModified(versions[j]));
					}
				}
			}
			// update of versions
			versions[0] = nodes[0].getIncidenceListVersion();
			versions[1] = nodes[1].getIncidenceListVersion();
			versions[2] = nodes[2].getIncidenceListVersion();
			commit(g);

			// delete an edge
			createTransaction(g);
			g.deleteEdge(sl);
			commit(g);

			createReadOnlyTransaction(g);
			assertTrue(nodes[start].isIncidenceListModified(versions[start]));
			assertTrue(nodes[end].isIncidenceListModified(versions[end]));
			if (start != end) {
				assertFalse(nodes[6 - (start + 1) - (end + 1) - 1]
						.isIncidenceListModified(versions[6 - (start + 1)
								- (end + 1) - 1]));
			} else {
				for (int j = 0; j < 3; j++) {
					if (j != start) {
						assertFalse(nodes[j]
								.isIncidenceListModified(versions[j]));
					}
				}
			}
			// update of versions
			versions[0] = nodes[0].getIncidenceListVersion();
			versions[1] = nodes[1].getIncidenceListVersion();
			versions[2] = nodes[2].getIncidenceListVersion();
			commit(g);
		}
	}

}
