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
package de.uni_koblenz.jgralabtest.instancetest;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Comparator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralabtest.schemas.minimal.Link;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.minimal.Node;

@RunWith(Parameterized.class)
public class EdgeListTest extends InstanceTest {
	private static final int V = 4;
	private static final int E = 4;
	private static final int N = 10;
	private static final int EDGE_COUNT = 10;
	private MinimalGraph g;

	@Parameters
	public static Collection<Object[]> configure() {
		return getParameters();
	}

	public EdgeListTest(ImplementationType implementationType) {
		super(implementationType);
	}

	@Before
	public void setup() throws CommitFailedException {
		switch (implementationType) {
		case STANDARD:
			g = MinimalSchema.instance().createMinimalGraph(V, E);
			break;
		case TRANSACTION:
			g = MinimalSchema.instance()
					.createMinimalGraphWithTransactionSupport(V, E);
			break;
		case DATABASE:
			g = this.createMinimalGraphWithDatabaseSupport();
			break;
		case SAVEMEM:
			g = MinimalSchema.instance().createMinimalGraphWithSavememSupport();
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}
		createTransaction(g);
		for (int i = 0; i < N; ++i) {
			g.createNode();
		}
		for (int i = 0; i < N; ++i) {
			g.createLink((Node) g.getVertex(i + 1), (Node) g.getVertex((i + 1)
					% N + 1));
		}
		commit(g);
	}

	private MinimalGraph createMinimalGraphWithDatabaseSupport() {
		dbHandler.connectToDatabase();
		dbHandler.loadMinimalSchemaIntoGraphDatabase();
		return dbHandler.createMinimalGraphWithDatabaseSupport("EdgeListTest",
				V, E);
	}

	@After
	public void tearDown() {
		if (implementationType == ImplementationType.DATABASE) {
			// dbHandler.cleanDatabaseOfTestGraph(g);
			// // dbHandler.cleanDatabaseOfTestSchema(MinimalSchema.instance());
			dbHandler.clearAllTables();
			dbHandler.closeGraphdatabase();
		}
	}

	@Test
	public void addEdgeTest() throws Exception {
		createReadOnlyTransaction(g);
		assertEquals(10, g.getECount());
		assertEquals("e1 e2 e3 e4 e5 e6 e7 e8 e9 e10", getESeq());
		for (Vertex v : g.vertices()) {
			assertEquals(2, v.getDegree());
			assertEquals(1, v.getDegree(EdgeDirection.IN));
			assertEquals(1, v.getDegree(EdgeDirection.OUT));
		}
		commit(g);
	}

	private String getESeq() {
		StringBuilder sb = new StringBuilder();
		for (Edge e : g.edges()) {
			sb.append('e').append(e.getId()).append(' ');
		}
		return sb.toString().trim();
	}

	@Test
	public void putBeforeTest() throws Exception {
		// TODO remove when problem is resolved
		// if(implementationType == ImplementationType.SAVEMEM){
		// fail("testcase creates an infinite loop.");
		// }
		createReadOnlyTransaction(g);
		Edge e5 = g.getEdge(5).getReversedEdge();
		commit(g);

		createTransaction(g);
		e5.putBeforeEdge(g.getEdge(6));
		commit(g);

		createReadOnlyTransaction(g);
		assertTrue(e5.isBeforeEdge(g.getEdge(6)));
		assertEquals("e1 e2 e3 e4 e5 e6 e7 e8 e9 e10", getESeq());
		assertTrue(e5.isAfterEdge(g.getEdge(4)));
		assertFalse(e5.isBeforeEdge(g.getEdge(4)));
		commit(g);

		createTransaction(g);
		e5.putBeforeEdge(g.getEdge(4));
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("e1 e2 e3 e5 e4 e6 e7 e8 e9 e10", getESeq());
		assertFalse(e5.isAfterEdge(g.getEdge(4)));
		assertTrue(e5.isBeforeEdge(g.getEdge(4)));
		commit(g);

		createTransaction(g);
		e5.putBeforeEdge(g.getEdge(10).getReversedEdge());
		// e5.putBeforeInGraph(g.getEdge(10));
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("e1 e2 e3 e4 e6 e7 e8 e9 e5 e10", getESeq());
		assertFalse(e5.isAfterEdge(g.getEdge(10)));
		assertTrue(e5.isBeforeEdge(g.getEdge(10)));
		assertFalse(e5.isBeforeEdge(g.getEdge(1)));
		assertTrue(g.getEdge(1).isBeforeEdge(e5));
		commit(g);

		createTransaction(g);
		e5.putBeforeEdge(g.getEdge(1));
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("e5 e1 e2 e3 e4 e6 e7 e8 e9 e10", getESeq());
		assertTrue(e5.isBeforeEdge(g.getEdge(1)));
		assertFalse(e5.isAfterEdge(g.getEdge(1)));
		assertTrue(g.getEdge(1).isAfterEdge(e5));
		commit(g);

	}

	@Test
	public void putAfterTest() throws Exception {
		createReadOnlyTransaction(g);
		Edge e5 = g.getEdge(5).getReversedEdge();
		commit(g);

		createTransaction(g);
		e5.putAfterEdge(g.getEdge(4));
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("e1 e2 e3 e4 e5 e6 e7 e8 e9 e10", getESeq());
		commit(g);

		createTransaction(g);
		e5.putAfterEdge(g.getEdge(6).getReversedEdge());
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("e1 e2 e3 e4 e6 e5 e7 e8 e9 e10", getESeq());
		commit(g);

		createTransaction(g);
		e5.putAfterEdge(g.getEdge(10));
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("e1 e2 e3 e4 e6 e7 e8 e9 e10 e5", getESeq());
		commit(g);

		createTransaction(g);
		e5.putAfterEdge(g.getEdge(1));
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("e1 e5 e2 e3 e4 e6 e7 e8 e9 e10", getESeq());
		commit(g);
	}

	@Test
	public void deleteEdgeTest() throws Exception {
		createReadOnlyTransaction(g);
		Edge e = g.getEdge(5);
		commit(g);

		createTransaction(g);
		e.delete();
		commit(g);

		createReadOnlyTransaction(g);
		assertFalse(e.isValid());
		assertEquals(null, g.getEdge(5));
		assertEquals(9, g.getECount());
		assertEquals(1, g.getVertex(5).getDegree());
		assertEquals(1, g.getVertex(5).getDegree(EdgeDirection.IN));
		assertEquals(0, g.getVertex(5).getDegree(EdgeDirection.OUT));
		assertEquals(1, g.getVertex(6).getDegree());
		assertEquals(0, g.getVertex(6).getDegree(EdgeDirection.IN));
		assertEquals(1, g.getVertex(6).getDegree(EdgeDirection.OUT));
		assertEquals("e1 e2 e3 e4 e6 e7 e8 e9 e10", getESeq());
		commit(g);

		createReadOnlyTransaction(g);
		e = g.getFirstEdge().getReversedEdge();
		commit(g);

		createTransaction(g);
		e.delete();
		commit(g);

		createReadOnlyTransaction(g);
		assertFalse(e.isValid());
		assertEquals(null, g.getEdge(1));
		assertEquals(8, g.getECount());
		assertEquals("e2 e3 e4 e6 e7 e8 e9 e10", getESeq());
		commit(g);

		createReadOnlyTransaction(g);
		e = g.getEdge(10);
		commit(g);

		createTransaction(g);
		e.delete();
		commit(g);

		createReadOnlyTransaction(g);
		assertFalse(e.isValid());
		assertEquals(null, g.getEdge(1));
		assertEquals(7, g.getECount());
		assertEquals("e2 e3 e4 e6 e7 e8 e9", getESeq());
		commit(g);

		createReadOnlyTransaction(g);
		Node v1 = (Node) g.getVertex(1);
		Node v2 = (Node) g.getVertex(2);
		commit(g);

		createTransaction(g);
		g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("e2 e3 e4 e6 e7 e8 e9 e1", getESeq());
		commit(g);

		createTransaction(g);
		g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("e2 e3 e4 e6 e7 e8 e9 e1 e5", getESeq());
		commit(g);

		createTransaction(g);
		g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("e2 e3 e4 e6 e7 e8 e9 e1 e5 e10", getESeq());
		commit(g);

		createTransaction(g);
		g.createLink(v1, v2);
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("e2 e3 e4 e6 e7 e8 e9 e1 e5 e10 e11", getESeq());
		commit(g);
	}

	/**
	 * Rudimentary test for sortEdgeList. It sorts the edge in reverse order to
	 * the id and back. For transaction support it has to be tested in the same
	 * transaction, because otherwise the IDs would be changed.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void testSortEdgeList() throws CommitFailedException {
		switch (implementationType) {
		case STANDARD:
			g = MinimalSchema.instance().createMinimalGraph(V, E);
			break;
		case TRANSACTION:
			g = MinimalSchema.instance()
					.createMinimalGraphWithTransactionSupport(V, E);
			break;
		case DATABASE:
			return; // because edge list sorting is not implemented for db
			// support
			// g = dbHandler.createMinimalGraphWithDatabaseSupport(
			// "IncidenceListTest.testSortIncidences", V, E);
			// break;
		case SAVEMEM:
			g = MinimalSchema.instance().createMinimalGraphWithSavememSupport(
					V, E);
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}

		createTransaction(g);
		Node n1 = g.createNode();
		Node n2 = g.createNode();
		Link[] links = new Link[EDGE_COUNT + 1];
		for (int i = 1; i < links.length; i++) {
			links[i] = (g.createLink(n1, n2));
		}

		int i = 1;
		for (Edge currentEdge : g.edges()) {
			assertEquals(currentEdge.getId(), links[i++].getId());
		}

		Comparator<Edge> comp = new Comparator<Edge>() {

			@Override
			public int compare(Edge o1, Edge o2) {
				return Double.compare(o2.getId(), o1.getId());
			}

		};

		g.sortEdges(comp);

		i = EDGE_COUNT;
		for (Edge currentEdge : g.edges()) {
			assertEquals(currentEdge.getId(), links[i--].getId());
		}

		comp = new Comparator<Edge>() {

			@Override
			public int compare(Edge o1, Edge o2) {
				return Double.compare(o1.getId(), o2.getId());
			}

		};

		g.sortEdges(comp);

		i = 1;
		for (Edge currentEdge : g.edges()) {
			assertEquals(currentEdge.getId(), links[i++].getId());
		}
		commit(g);

	}
}
