/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.RandomIdGenerator;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.GraphBase;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralabtest.instancetest.InstanceTest;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.minimal.Node;
import de.uni_koblenz.jgralabtest.schemas.vertextest.DoubleSubNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.Link;
import de.uni_koblenz.jgralabtest.schemas.vertextest.LinkBack;
import de.uni_koblenz.jgralabtest.schemas.vertextest.SubLink;
import de.uni_koblenz.jgralabtest.schemas.vertextest.SubNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.SuperNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestGraph;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestSchema;

@RunWith(Parameterized.class)
public class GraphBaseTest extends InstanceTest {

	public GraphBaseTest(ImplementationType implementationType, String dbURL) {
		super(implementationType, dbURL);
	}

	@Parameters
	public static Collection<Object[]> configure() {
		return getParameters();
	}

	private GraphBase g1;
	private GraphBase g2;
	private Vertex v1, v2, v3, v5, v7, v8, v9, v11;

	@Before
	public void setUp() throws CommitFailedException {
		if (implementationType == ImplementationType.DATABASE) {
			dbHandler.connectToDatabase();
			dbHandler.loadVertexTestSchemaIntoGraphDatabase();
		}
		g1 = (GraphBase) createNewGraph();
		g2 = (GraphBase) createNewGraph();
		createTransaction(g1);
		// System.out.println("Graph2 is instance of class " + g2.getClass());
		v1 = g1.createVertex(SubNode.class);
		// System.out.println("V1 is instance of class " + v1.getClass());
		v2 = g1.createVertex(SubNode.class);
		v3 = g1.createVertex(SubNode.class);
		g1.createVertex(SubNode.class);
		v5 = g1.createVertex(SuperNode.class);
		g1.createVertex(SuperNode.class);
		v7 = g1.createVertex(SuperNode.class);
		v8 = g1.createVertex(SuperNode.class);
		v9 = g1.createVertex(DoubleSubNode.class);
		// System.out.println("v9= " + v9);
		g1.createVertex(DoubleSubNode.class);
		v11 = g1.createVertex(DoubleSubNode.class);
		g1.createVertex(DoubleSubNode.class);
		commit(g1);
	}

	private ArrayList<String> graphIdsInUse = new ArrayList<String>();

	/**
	 * 
	 * @return
	 */
	private VertexTestGraph createNewGraph() {
		VertexTestGraph out = null;
		switch (implementationType) {
		case STANDARD:
			out = VertexTestSchema.instance().createVertexTestGraph();
			break;
		case TRANSACTION:
			out = VertexTestSchema.instance()
					.createVertexTestGraphWithTransactionSupport();
			break;
		case DATABASE:
			out = createVertexTestGraphWithDatabaseSupport();
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}
		graphIdsInUse.add(out.getId());
		return out;
	}

	private VertexTestGraph createVertexTestGraphWithDatabaseSupport() {
		String id = RandomIdGenerator.generateId();
		while (graphIdsInUse.contains(id)) {
			id = RandomIdGenerator.generateId();
		}
		graphIdsInUse.add(id);
		return dbHandler.createVertexTestGraphWithDatabaseSupport(id, 1000,
				1000);
	}

	@After
	public void tearDown() {
		if (implementationType == ImplementationType.DATABASE) {
			cleanAnCloseGraphDatabase();
		}
	}

	private void cleanAnCloseGraphDatabase() {
		graphIdsInUse.clear();
		dbHandler.clearAllTables();
		dbHandler.closeGraphdatabase();
	}

	/**
	 * Asserts true if the edgeListVersion has changed. Returns the new
	 * edgeListVersion.
	 * 
	 * @param elv1
	 *            the edgeListVersion before the transaction.
	 * @return the edgeListVersion after the transaction.
	 * @throws CommitFailedException
	 *             should not happen.
	 */
	private long checkIfEdgeListVersionChanged(long elv1)
			throws CommitFailedException {
		long out;
		createReadOnlyTransaction(g1);
		assertTrue(elv1 < g1.getEdgeListVersion());
		out = g1.getEdgeListVersion();
		commit(g1);
		return out;
	}

	/**
	 * Asserts true if the edgeListVersion has not changed.
	 * 
	 * @param elv1
	 *            the edgeListVersion before the transaction.
	 * @throws CommitFailedException
	 *             should not happen.
	 */
	private void checkIfEdgeListVersionRemained(long elv1)
			throws CommitFailedException {
		createReadOnlyTransaction(g1);
		assertTrue(elv1 == g1.getEdgeListVersion());
		commit(g1);
	}

	private MinimalGraph createMinimalGraph() {
		MinimalGraph g3 = null;
		switch (implementationType) {
		case STANDARD:
			g3 = MinimalSchema.instance().createMinimalGraph();
			break;
		case TRANSACTION:
			g3 = MinimalSchema.instance()
					.createMinimalGraphWithTransactionSupport();
			break;
		case DATABASE:
			dbHandler.loadMinimalSchemaIntoGraphDatabase();
			g3 = dbHandler.createMinimalGraphWithDatabaseSupport("GraphTest");
			graphIdsInUse.add(g3.getId());
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}
		return g3;
	}

	@Test
	public void testDefragment() {
		if (implementationType == ImplementationType.TRANSACTION) {
			try {
				g1.defragment();
				fail("Defragmentation with transaction support should throw an UnsupportedOperationException.");
			} catch (UnsupportedOperationException e) {
				// as expected
			}
		} else if (implementationType == ImplementationType.DATABASE) {
			try {
				g1.defragment();
				fail("Defragmentation with database support should throw an UnsupportedOperationException.");
			} catch (UnsupportedOperationException e) {
				// as expected
			}
		} else {
			/*
			 * Testen der defragment()-Methode: Ein Vorher-Nachher Abbild von
			 * Vertex- Referenzen sammeln und vergleichen, genauso mit
			 * Kantenseq. Inzidenzen sind nicht betroffen (von defragment()
			 * zumindest das, was einfach zu testen ist); Dafür bedarf es einen
			 * Graph, indem gelöscht wurde und dadurch Lücken entstanden sind,
			 * sodass defragment() zum Einsatz kommen kann
			 */

			// create a graph and create several vertices and edges
			MinimalGraph g = createMinimalGraph();
			Node[] vertices = new Node[20];

			// create nodes
			for (int i = 0; i < 20; i++) {
				vertices[i] = g.createNode();
			}
			// test if all ids from 1 to 20 have been assigned in correct order
			for (int i = 0; i < 20; i++) {
				assertEquals(vertices[i], g.getVertex(i + 1));
			}

			// delete vertices from id 11 to 15
			for (int i = 11; i <= 15; i++) {
				g.getVertex(i).delete();
			}
			// defragment the graph
			((GraphBase) g).defragment();
			// check if the maximum id is 15
			assertEquals(15, g.getVCount());
			for (int i = 1; i <= 15; i++) {
				assertNotNull(g.getVertex(i));
			}
			for (int i = 16; i <= 20; i++) {
				assertNull(g.getVertex(i));
			}

			// do the same for edges
			de.uni_koblenz.jgralabtest.schemas.minimal.Link[] links = new de.uni_koblenz.jgralabtest.schemas.minimal.Link[20];

			g = createMinimalGraph();
			// create nodes
			for (int i = 0; i < 20; i++) {
				vertices[i] = g.createNode();
			}

			// create links (resulting graph is a ring)
			for (int i = 0; i < 20; i++) {
				links[i] = g.createLink((Node) g.getVertex(i + 1), (Node) g
						.getVertex(i % 20 + 1));
			}
			// test if all ids from 1 to 20 have been assigned in correct order
			for (int i = 0; i < 20; i++) {
				assertEquals(links[i], g.getEdge(i + 1));
			}

			// delete edges from id 11 to 15
			for (int i = 11; i <= 15; i++) {
				g.getEdge(i).delete();
			}

			// defragment the graph
			((GraphBase) g).defragment();
			// check if the maximum id is 15
			assertEquals(15, g.getECount());
			for (int i = 1; i <= 15; i++) {
				assertNotNull(g.getEdge(i));
			}
			for (int i = 16; i <= 20; i++) {
				assertNull(g.getEdge(i));
			}

		}

	}

	// TODO continue here
	@Test
	public void testGetEdgeListVersion() throws Exception {
		createTransaction(g1);
		// preparations...
		Vertex v1 = g1.createVertex(SubNode.class);
		Vertex v2 = g1.createVertex(SubNode.class);
		Vertex v3 = g1.createVertex(SubNode.class);
		Vertex v4 = g1.createVertex(SubNode.class);
		Vertex v5 = g1.createVertex(SuperNode.class);
		Vertex v6 = g1.createVertex(SuperNode.class);
		Vertex v7 = g1.createVertex(SuperNode.class);
		Vertex v8 = g1.createVertex(SuperNode.class);
		Vertex v9 = g1.createVertex(DoubleSubNode.class);
		Vertex v10 = g1.createVertex(DoubleSubNode.class);
		Vertex v11 = g1.createVertex(DoubleSubNode.class);
		Vertex v12 = g1.createVertex(DoubleSubNode.class);
		commit(g1);

		long elv1;
		createReadOnlyTransaction(g1);
		// border cases
		elv1 = g1.getEdgeListVersion();
		assertEquals(0, elv1);
		assertEquals(0, g2.getEdgeListVersion());
		commit(g1);

		createTransaction(g1);
		Edge e1 = g1.createEdge(SubLink.class, v9, v7);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.deleteEdge(e1);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		// normal cases
		createTransaction(g1);
		g1.createEdge(SubLink.class, v10, v5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v10, v6);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v10, v7);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v10, v8);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v11, v5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v11, v6);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v11, v7);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v11, v8);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v12, v5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v12, v6);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v12, v7);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		Edge e3 = g1.createEdge(SubLink.class, v12, v8);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v9, v6);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v9, v7);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v9, v8);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.deleteEdge(e3);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		// when deleting a vertex, incident edges are also deleted and the
		// edgeListVersion changes.
		createTransaction(g1);
		Vertex v13 = g1.createVertex(DoubleSubNode.class);
		Vertex v14 = g1.createVertex(DoubleSubNode.class);
		g1.createEdge(Link.class, v13, v14);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.deleteVertex(v13);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		// when deleting a vertex with degree=0, the edgeListVersion should
		// remain unchanged.
		createTransaction(g1);
		g1.deleteVertex(v14);
		commit(g1);

		// same
		checkIfEdgeListVersionRemained(elv1);

		createTransaction(g1);
		g1.createEdge(Link.class, v1, v5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(Link.class, v2, v5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(Link.class, v3, v5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(Link.class, v4, v5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(Link.class, v10, v5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(Link.class, v11, v5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(Link.class, v12, v5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		// v6 does not exist anymore
		createTransaction(g1);
		g1.createEdge(Link.class, v1, v6);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(Link.class, v1, v7);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(Link.class, v1, v8);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		Edge e4 = g1.createEdge(Link.class, v3, v7);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(Link.class, v11, v8);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(LinkBack.class, v5, v1);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(LinkBack.class, v6, v2);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		Edge e5 = g1.createEdge(LinkBack.class, v7, v3);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(LinkBack.class, v8, v4);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(LinkBack.class, v8, v9);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(LinkBack.class, v7, v10);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.createEdge(LinkBack.class, v6, v11);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		Edge e6 = g1.createEdge(LinkBack.class, v5, v12);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.deleteEdge(e4);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.deleteEdge(e5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		g1.deleteEdge(e6);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		// reordering edges does change the edgeListVersion
		createTransaction(g1);
		Edge e7 = g1.createEdge(SubLink.class, v9, v5);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		Edge e8 = g1.createEdge(SubLink.class, v12, v7);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		Edge e9 = g1.createEdge(SubLink.class, v11, v6);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		e7.putBeforeEdge(e9);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		e8.putBeforeEdge(e7);
		commit(g1);

		// same
		checkIfEdgeListVersionRemained(elv1);

		createTransaction(g1);
		e9.putAfterEdge(e8);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		createTransaction(g1);
		e8.putAfterEdge(e7);
		commit(g1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		// changing attributes does not change the edgeListVersion
		createTransaction(g1);
		e7.setAttribute("anInt", 22);
		commit(g1);

		// same
		checkIfEdgeListVersionRemained(elv1);

		createTransaction(g1);
		e8.setAttribute("anInt", 203);
		commit(g1);

		// same
		checkIfEdgeListVersionRemained(elv1);

		createTransaction(g1);
		e9.setAttribute("anInt", 2209);
		commit(g1);

		// same
		checkIfEdgeListVersionRemained(elv1);

		createTransaction(g1);
		e7.setAttribute("anInt", 15);
		commit(g1);

		// same
		checkIfEdgeListVersionRemained(elv1);

	}

	@Test
	public void testGetExpandedEdgeCount() throws CommitFailedException {
		// border case
		createReadOnlyTransaction(g1);
		assertEquals(2000, g1.getExpandedEdgeCount());
		commit(g1);

		// normal cases
		createTransaction(g1);
		for (int i = 0; i < 1000; i++) {
			g1.createEdge(SubLink.class, v9, v5);
		}
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals(2000, g1.getExpandedEdgeCount());
		commit(g1);

		createTransaction(g1);
		for (int i = 0; i < 1000; i++) {
			g1.createEdge(Link.class, v1, v5);
		}
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals(4000, g1.getExpandedEdgeCount());
		commit(g1);

		createTransaction(g1);
		for (int i = 0; i < 1000; i++) {
			g1.createEdge(LinkBack.class, v5, v9);
		}
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals(8000, g1.getExpandedEdgeCount());
		commit(g1);

	}

	@Test
	public void testGetExpandedVertexCount() throws CommitFailedException {
		// border case
		createReadOnlyTransaction(g1);
		assertEquals(2000, g1.getExpandedVertexCount());
		commit(g1);

		// normal cases
		createTransaction(g1);
		for (int i = 12; i < 1000; i++) {
			g1.createVertex(SubNode.class);
		}
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals(2000, g1.getExpandedVertexCount());
		commit(g1);

		createTransaction(g1);
		for (int i = 0; i < 1000; i++) {
			g1.createVertex(SuperNode.class);
		}
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals(4000, g1.getExpandedVertexCount());
		commit(g1);

		createTransaction(g1);
		for (int i = 0; i < 1000; i++) {
			g1.createVertex(DoubleSubNode.class);
		}
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals(8000, g1.getExpandedVertexCount());
		commit(g1);

	}

	@Test
	public void testGetMaxECount() throws CommitFailedException {
		createReadOnlyTransaction(g1);
		assertEquals(1000, g1.getMaxECount());
		commit(g1);

		createReadOnlyTransaction(g2);
		assertEquals(1000, g2.getMaxECount());
		commit(g2);

		MinimalGraph g3 = createMinimalGraph();

		createReadOnlyTransaction(g3);
		assertEquals(1000, ((GraphBase) g3).getMaxECount());
		commit(g3);

	}

	@Test
	public void testGetMaxVCount() throws CommitFailedException {
		createReadOnlyTransaction(g1);
		assertEquals(1000, g1.getMaxVCount());
		assertEquals(1000, g2.getMaxVCount());
		commit(g1);

		MinimalGraph g3 = createMinimalGraph();

		createReadOnlyTransaction(g3);
		assertEquals(1000, ((GraphBase) g3).getMaxVCount());
		commit(g3);

	}

	@Test
	public void testGetVertexListVersion() throws CommitFailedException {

		// border cases
		createReadOnlyTransaction(g2);
		long vertexListVersion2 = g2.getVertexListVersion();
		// assertEquals(0, vertexListVersion2);
		commit(g2);

		createTransaction(g2);
		Vertex v13 = g2.createVertex(SuperNode.class);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(vertexListVersion2 < g2.getVertexListVersion());
		vertexListVersion2 = g2.getVertexListVersion();
		commit(g2);

		// normal cases
		createReadOnlyTransaction(g1);
		long vertexListVersion1 = g1.getVertexListVersion();
		// assertEquals(12, vertexListVersion1); with transactions enabled it is
		// not 12
		commit(g1);

		createTransaction(g1);
		g1.createVertex(SubNode.class);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		vertexListVersion1 = g1.getVertexListVersion();
		commit(g1);

		createTransaction(g2);
		g2.createVertex(SuperNode.class);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(vertexListVersion2 < g2.getVertexListVersion());
		vertexListVersion2 = g2.getVertexListVersion();
		commit(g2);

		createTransaction(g2);
		g2.createVertex(DoubleSubNode.class);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(vertexListVersion2 < g2.getVertexListVersion());
		vertexListVersion2 = g2.getVertexListVersion();
		commit(g2);

		for (int i = 4; i < 100; i++) {
			createTransaction(g2);
			g2.createVertex(SuperNode.class);
			commit(g2);

			createReadOnlyTransaction(g2);
			assertTrue(vertexListVersion2 < g2.getVertexListVersion());
			vertexListVersion2 = g2.getVertexListVersion();
			commit(g2);
		}

		createTransaction(g2);
		g2.createVertex(DoubleSubNode.class);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(vertexListVersion2 < g2.getVertexListVersion());
		vertexListVersion2 = g2.getVertexListVersion();
		commit(g2);

		// tests whether the version changes correctly if vertices are deleted
		createTransaction(g2);
		g2.deleteVertex(v13);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(vertexListVersion2 < g2.getVertexListVersion());
		vertexListVersion2 = g2.getVertexListVersion();
		commit(g2);

		for (int i = 14; i < 31; i += 3) {
			createTransaction(g1);
			g1.createVertex(DoubleSubNode.class);
			commit(g1);

			createReadOnlyTransaction(g1);
			assertTrue(vertexListVersion1 < g1.getVertexListVersion());
			vertexListVersion1 = g1.getVertexListVersion();
			commit(g1);

			createTransaction(g1);
			g1.createVertex(SubNode.class);
			commit(g1);

			createReadOnlyTransaction(g1);
			assertTrue(vertexListVersion1 < g1.getVertexListVersion());
			vertexListVersion1 = g1.getVertexListVersion();
			commit(g1);

			createTransaction(g1);
			g1.createVertex(SuperNode.class);
			commit(g1);

			createReadOnlyTransaction(g1);
			assertTrue(vertexListVersion1 < g1.getVertexListVersion());
			vertexListVersion1 = g1.getVertexListVersion();
			commit(g1);
		}

		createTransaction(g1);
		Vertex v14 = g1.createVertex(SuperNode.class);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		vertexListVersion1 = g1.getVertexListVersion();
		commit(g1);

		createTransaction(g1);
		Vertex v15 = g1.createVertex(DoubleSubNode.class);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		vertexListVersion1 = g1.getVertexListVersion();
		commit(g1);

		// TODO deleted vertices should not be used for new edges
		// createTransaction(g1);
		// g1.deleteVertex(v15);
		// commit(g1);
		//
		// createReadOnlyTransaction(g1);
		// assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		// vertexListVersion1 = g1.getVertexListVersion();
		// commit(g1);

		// createTransaction(g1);
		// g1.deleteVertex(v14);
		// commit(g1);
		//
		// createReadOnlyTransaction(g1);
		// assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		// vertexListVersion1 = g1.getVertexListVersion();
		// commit(g1);

		// makes sure that editing edges does not change the vertexList
		createTransaction(g1);
		g1.createEdge(SubLink.class, v15, v14);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals(vertexListVersion1, g1.getVertexListVersion());
		commit(g1);

		createTransaction(g1);
		g1.createEdge(LinkBack.class, v14, v15);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals(vertexListVersion1, g1.getVertexListVersion());
		commit(g1);

		// reordering the vertices does change the vertexListVersion
		createTransaction(g1);
		v3.putAfter(v7);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		vertexListVersion1 = g1.getVertexListVersion();
		commit(g1);

		createTransaction(g1);
		v5.putBefore(v2);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		vertexListVersion1 = g1.getVertexListVersion();
		commit(g1);

		createTransaction(g1);
		v5.putAfter(v3);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		vertexListVersion1 = g1.getVertexListVersion();
		commit(g1);

		createTransaction(g1);
		v7.putBefore(v2);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		vertexListVersion1 = g1.getVertexListVersion();
		commit(g1);

		createTransaction(g1);
		v7.putBefore(v2);// v7 is already before v2
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals(vertexListVersion1, g1.getVertexListVersion());
		commit(g1);

		// changing attributes of vertices does not change the vertexListVersion
		createTransaction(g1);
		v5.setAttribute("number", 17);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals(vertexListVersion1, g1.getVertexListVersion());
		commit(g1);

		createTransaction(g1);
		v8.setAttribute("number", 42);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals(vertexListVersion1, g1.getVertexListVersion());
		commit(g1);

		createTransaction(g1);
		v7.setAttribute("number", 2);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals(vertexListVersion1, g1.getVertexListVersion());
		commit(g1);

		createTransaction(g1);
		v5.setAttribute("number", 15);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals(vertexListVersion1, g1.getVertexListVersion());
		commit(g1);

	}

	@Test
	public void testIsEdgeListModified() throws CommitFailedException {
		// preparations...
		createTransaction(g2);
		Vertex v13 = g2.createVertex(SubNode.class);
		Vertex v14 = g2.createVertex(SubNode.class);
		Vertex v15 = g2.createVertex(SubNode.class);
		Vertex v16 = g2.createVertex(SubNode.class);
		Vertex v17 = g2.createVertex(SuperNode.class);
		Vertex v18 = g2.createVertex(SuperNode.class);
		Vertex v19 = g2.createVertex(SuperNode.class);
		Vertex v20 = g2.createVertex(SuperNode.class);
		Vertex v21 = g2.createVertex(DoubleSubNode.class);
		Vertex v22 = g2.createVertex(DoubleSubNode.class);
		Vertex v23 = g2.createVertex(DoubleSubNode.class);
		Vertex v24 = g2.createVertex(DoubleSubNode.class);
		commit(g2);

		// border cases
		createReadOnlyTransaction(g1);
		createReadOnlyTransaction(g2);
		long edgeListVersion1 = g1.getEdgeListVersion();
		long edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g1.isEdgeListModified(edgeListVersion1));
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g1);
		commit(g2);

		createTransaction(g1);
		g1.createEdge(SubLink.class, v11, v7);
		commit(g1);

		createTransaction(g2);
		Edge e1 = g2.createEdge(Link.class, v15, v19);
		commit(g2);

		createReadOnlyTransaction(g1);
		createReadOnlyTransaction(g2);
		assertTrue(g1.isEdgeListModified(edgeListVersion1));
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion1 = g1.getEdgeListVersion();
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g1.isEdgeListModified(edgeListVersion1));
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g1);
		commit(g2);

		createTransaction(g2);
		g2.deleteEdge(e1);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		// normal cases
		createReadOnlyTransaction(g2);
		int ecount = g2.getECount();
		commit(g2);

		createTransaction(g2);
		g2.createEdge(LinkBack.class, v19, v15);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		assertEquals(ecount + 1, g2.getECount());
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		g2.createEdge(Link.class, v15, v19);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		Edge e2 = g2.createEdge(SubLink.class, v23, v19);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		g2.createEdge(Link.class, v16, v20);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		Edge e3 = g2.createEdge(Link.class, v23, v20);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		g2.createEdge(Link.class, v24, v19);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		g2.createEdge(LinkBack.class, v20, v16);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		Edge e4 = g2.createEdge(SubLink.class, v24, v20);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		g2.deleteEdge(e2);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		g2.createEdge(LinkBack.class, v19, v23);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		g2.createEdge(LinkBack.class, v20, v24);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		g2.deleteEdge(e4);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		g2.deleteEdge(e3);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		Edge e5 = g2.createEdge(SubLink.class, v21, v17);
		commit(g2);

		createTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		Edge e6 = g2.createEdge(Link.class, v13, v18);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		Edge e7 = g2.createEdge(LinkBack.class, v17, v14);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		g2.createEdge(Link.class, v22, v18);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		// adding vertices does not affect the edgeList
		createTransaction(g2);
		Vertex v25 = g2.createVertex(DoubleSubNode.class);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		Vertex v26 = g2.createVertex(SuperNode.class);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		g2.deleteVertex(v20);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		// reordering edges does change the edgeList
		createTransaction(g2);
		e6.putBeforeEdge(e5);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		e5.putAfterEdge(e6);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		e5.putAfterEdge(e7);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		// changing the attributes of an edge does not change the edgeList
		createTransaction(g2);
		Edge e8 = g2.createEdge(SubLink.class, v25, v26);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		e8.setAttribute("anInt", 2);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		e8.setAttribute("anInt", -41);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		e8.setAttribute("anInt", 1024);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

		createTransaction(g2);
		e8.setAttribute("anInt", 15);
		commit(g2);

		createReadOnlyTransaction(g2);
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		commit(g2);

	}

	@Test
	public void testIsLoading() throws CommitFailedException {
		// TODO how do I get isLoading to return true
		createReadOnlyTransaction(g1);
		createReadOnlyTransaction(g2);
		assertEquals(false, g1.isLoading());
		assertEquals(false, g2.isLoading());
		commit(g1);
		commit(g2);
		/*
		 * try{ // graph =VertexTestSchema.instance().loadVertexTestGraph(
		 * "de.uni_koblenz.VertexTestSchema.tg");
		 * 
		 * VertexTestGraph graph3 =
		 * VertexTestSchema.instance().loadVertexTestGraph
		 * ("VertexTestSchema.tg"); }catch (GraphIOException e){
		 * e.printStackTrace(); }
		 */
	}

	@Test
	public void testIsVertexListModified() throws CommitFailedException {
		// border cases
		createReadOnlyTransaction(g1);
		createReadOnlyTransaction(g2);
		long vListVersion1 = g1.getVertexListVersion();
		long vListVersion2 = g2.getVertexListVersion();
		commit(g1);
		commit(g2);

		createReadOnlyTransaction(g1);
		createReadOnlyTransaction(g2);
		assertFalse(g1.isVertexListModified(vListVersion1));
		assertFalse(g2.isVertexListModified(vListVersion2));
		commit(g1);
		commit(g2);

		createTransaction(g1);
		Vertex v1 = g1.createVertex(DoubleSubNode.class);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		createTransaction(g1);
		Vertex v2 = g1.createVertex(SuperNode.class);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		// makes sure that changing edges does not affect the vertexList
		createTransaction(g1);
		g1.createEdge(SubLink.class, v1, v2);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		createTransaction(g1);
		g1.createEdge(Link.class, v1, v2);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		createTransaction(g1);
		g1.deleteVertex(v2);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		// normal cases
		for (int i = 0; i < 21; i++) {
			createTransaction(g1);
			g1.createVertex(SubNode.class);
			commit(g1);

			createReadOnlyTransaction(g1);
			assertTrue(g1.isVertexListModified(vListVersion1));
			vListVersion1 = g1.getVertexListVersion();
			assertFalse(g1.isVertexListModified(vListVersion1));
			commit(g1);
		}

		createTransaction(g1);
		g1.deleteVertex(v1);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		for (int i = 0; i < 12; i++) {
			createTransaction(g1);
			g1.createVertex(SuperNode.class);
			commit(g1);

			createReadOnlyTransaction(g1);
			assertTrue(g1.isVertexListModified(vListVersion1));
			vListVersion1 = g1.getVertexListVersion();
			assertFalse(g1.isVertexListModified(vListVersion1));
			commit(g1);
		}
		createReadOnlyTransaction(g1);
		vListVersion1 = g1.getVertexListVersion();
		commit(g1);

		createTransaction(g1);
		Vertex v3 = g1.createVertex(SubNode.class);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		createTransaction(g1);
		Vertex v4 = g1.createVertex(SuperNode.class);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		// if the order of the vertices is changed the vertexList is modified
		createTransaction(g1);
		v3.putAfter(v4);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		commit(g1);

		createTransaction(g1);
		v3.putAfter(v4);// v3 is already after v4
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		createTransaction(g1);
		v3.putBefore(v4);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		createTransaction(g1);
		Vertex v5 = g1.createVertex(DoubleSubNode.class);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		createTransaction(g1);
		v5.putBefore(v3);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		createTransaction(g1);
		v4.putAfter(v5);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		// if attributes of vertices are changed this does not affect the
		// vertexList
		createTransaction(g1);
		v4.setAttribute("number", 5);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);

		createTransaction(g1);
		v4.setAttribute("number", 42);
		commit(g1);

		createReadOnlyTransaction(g1);
		assertFalse(g1.isVertexListModified(vListVersion1));
		commit(g1);
	}

	@Test
	public void testSetId() throws CommitFailedException {
		createTransaction(g1);
		g1.setId("alpha");
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals("alpha", g1.getId());
		commit(g1);

		createTransaction(g1);
		g1.setId("1265");
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals("1265", g1.getId());
		commit(g1);

		createTransaction(g1);
		g1.setId("007");
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals("007", g1.getId());
		commit(g1);

		createTransaction(g1);
		g1.setId("r2d2");
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals("r2d2", g1.getId());
		commit(g1);

		createTransaction(g1);
		g1.setId("answer:42");
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals("answer:42", g1.getId());
		commit(g1);

		createTransaction(g1);
		g1.setId("1506");
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals("1506", g1.getId());
		commit(g1);

		createTransaction(g1);
		g1.setId("june15");
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals("june15", g1.getId());
		commit(g1);

		createTransaction(g1);
		g1.setId("bang");
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals("bang", g1.getId());
		commit(g1);

		createTransaction(g1);
		g1.setId("22now");
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals("22now", g1.getId());
		commit(g1);

		createTransaction(g1);
		g1.setId("hjkutzbv");
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals("hjkutzbv", g1.getId());
		commit(g1);

		createTransaction(g1);
		g1.setId("54rdcg9");
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals("54rdcg9", g1.getId());
		commit(g1);

		createTransaction(g1);
		g1.setId(".k,oibt");
		commit(g1);

		createReadOnlyTransaction(g1);
		assertEquals(".k,oibt", g1.getId());
		commit(g1);

	}

}
