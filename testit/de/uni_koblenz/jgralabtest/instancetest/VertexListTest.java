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

import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.minimal.Node;

@RunWith(Parameterized.class)
public class VertexListTest extends InstanceTest {
	private static final int VERTEX_COUNT = 10;

	public VertexListTest(ImplementationType implementationType) {
		super(implementationType);
	}

	@Parameters
	public static Collection<Object[]> configure() {
		return getParameters();
	}

	final int V = 4;
	final int E = 4;
	final int N = 10;
	MinimalGraph g;

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
			g = this.createMinimalGraphInDatabase();
			break;
		case SAVEMEM:
			g = MinimalSchema.instance().createMinimalGraph(V, E);
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}
		createTransaction(g);
		for (int i = 0; i < N; ++i) {
			g.createNode();
		}
		commit(g);
	}

	private MinimalGraph createMinimalGraphInDatabase() {
		super.connectToDatabase();
		super.loadMinimalSchemaIntoGraphDatabase();
		return super.createMinimalGraphWithDatabaseSupport("VertexListTest");
	}

	@After
	public void tearDown() {
		if (implementationType == ImplementationType.DATABASE) {
			this.cleanAndCloseGraphDatabase();
		}
	}

	private void cleanAndCloseGraphDatabase() {
		super.cleanDatabaseOfTestGraph(g);
		// super.cleanDatabaseOfTestSchema(MinimalSchema.instance());
		super.closeGraphdatabase();
	}

	@Test
	public void addVertexTest() throws Exception {
		createReadOnlyTransaction(g);
		assertEquals(10, g.getVCount());
		assertEquals("v1 v2 v3 v4 v5 v6 v7 v8 v9 v10", getVSeq());
		commit(g);
	}

	private String getVSeq() {
		StringBuilder sb = new StringBuilder();
		for (Vertex v : g.vertices()) {
			sb.append('v').append(v.getId()).append(' ');
		}
		return sb.toString().trim();
	}

	@Test
	public void putBeforeTest() throws Exception {
		createTransaction(g);
		Vertex v5 = g.getVertex(5);
		v5.putBefore(g.getVertex(6));
		commit(g);

		createReadOnlyTransaction(g);
		assertTrue(v5.isBefore(g.getVertex(6)));
		assertEquals("v1 v2 v3 v4 v5 v6 v7 v8 v9 v10", getVSeq());

		assertTrue(v5.isAfter(g.getVertex(4)));
		assertFalse(v5.isBefore(g.getVertex(4)));
		commit(g);

		createTransaction(g);
		v5.putBefore(g.getVertex(4));
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("v1 v2 v3 v5 v4 v6 v7 v8 v9 v10", getVSeq());
		assertFalse(v5.isAfter(g.getVertex(4)));
		assertTrue(v5.isBefore(g.getVertex(4)));
		commit(g);

		createTransaction(g);
		v5.putBefore(g.getVertex(10));
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("v1 v2 v3 v4 v6 v7 v8 v9 v5 v10", getVSeq());
		assertFalse(v5.isAfter(g.getVertex(10)));
		assertTrue(v5.isBefore(g.getVertex(10)));

		assertFalse(v5.isBefore(g.getVertex(1)));
		assertTrue(g.getVertex(1).isBefore(v5));
		commit(g);

		createTransaction(g);
		v5.putBefore(g.getVertex(1));
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("v5 v1 v2 v3 v4 v6 v7 v8 v9 v10", getVSeq());
		assertTrue(v5.isBefore(g.getVertex(1)));
		assertFalse(v5.isAfter(g.getVertex(1)));
		assertTrue(g.getVertex(1).isAfter(v5));
		commit(g);
	}

	@Test
	public void putAfterTest() throws Exception {
		createTransaction(g);
		Vertex v5 = g.getVertex(5);

		v5.putAfter(g.getVertex(4));
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("v1 v2 v3 v4 v5 v6 v7 v8 v9 v10", getVSeq());
		commit(g);

		createTransaction(g);
		v5.putAfter(g.getVertex(6));
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("v1 v2 v3 v4 v6 v5 v7 v8 v9 v10", getVSeq());
		commit(g);

		createTransaction(g);
		v5.putAfter(g.getVertex(10));
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("v1 v2 v3 v4 v6 v7 v8 v9 v10 v5", getVSeq());
		commit(g);

		createTransaction(g);
		v5.putAfter(g.getVertex(1));
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("v1 v5 v2 v3 v4 v6 v7 v8 v9 v10", getVSeq());
		commit(g);
	}

	@Test
	public void deleteVertexTest() throws Exception {
		createTransaction(g);
		Vertex v = g.getVertex(5);
		v.delete();
		commit(g);

		createReadOnlyTransaction(g);
		assertFalse(v.isValid());
		assertEquals(null, g.getVertex(5));
		assertEquals(9, g.getVCount());
		assertEquals("v1 v2 v3 v4 v6 v7 v8 v9 v10", getVSeq());
		commit(g);

		createTransaction(g);
		v = g.getFirstVertex();
		v.delete();
		commit(g);

		createReadOnlyTransaction(g);
		assertFalse(v.isValid());
		assertEquals(null, g.getVertex(1));
		assertEquals(8, g.getVCount());
		assertEquals("v2 v3 v4 v6 v7 v8 v9 v10", getVSeq());
		commit(g);

		createTransaction(g);
		v = g.getVertex(10);
		v.delete();
		commit(g);

		createReadOnlyTransaction(g);
		assertFalse(v.isValid());
		assertEquals(null, g.getVertex(1));
		assertEquals(7, g.getVCount());
		assertEquals("v2 v3 v4 v6 v7 v8 v9", getVSeq());
		commit(g);

		createTransaction(g);
		g.createNode();
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("v2 v3 v4 v6 v7 v8 v9 v1", getVSeq());
		commit(g);

		createTransaction(g);
		g.createNode();
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("v2 v3 v4 v6 v7 v8 v9 v1 v5", getVSeq());
		commit(g);

		createTransaction(g);
		g.createNode();
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("v2 v3 v4 v6 v7 v8 v9 v1 v5 v10", getVSeq());
		commit(g);

		createTransaction(g);
		g.createNode();
		commit(g);

		createReadOnlyTransaction(g);
		assertEquals("v2 v3 v4 v6 v7 v8 v9 v1 v5 v10 v11", getVSeq());
		commit(g);
	}
	
	/**
	 * Rudimentary test for sortVertexList. It sorts the vertices in reverse order to
	 * the id and back. For transaction support it has to be tested in the same
	 * transaction, because otherwise the IDs would be changed.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void testSortVertexList() throws CommitFailedException {
		MinimalGraph g = null;
		switch (implementationType) {
		case STANDARD:
			g = MinimalSchema.instance().createMinimalGraph(V, E);
			break;
		case TRANSACTION:
			g = MinimalSchema.instance()
					.createMinimalGraphWithTransactionSupport(V, E);
			break;
		case DATABASE:
			g = this.createMinimalGraphWithDatabaseSupport(
					"IncidenceListTest.testSortIncidences", V, E);
			break;
		case SAVEMEM:
			g = MinimalSchema.instance().createMinimalGraphWithSavememSupport(
					V, E);
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}

		createTransaction(g);
		Node[] nodes = new Node[VERTEX_COUNT + 1];
		for (int i = 1; i < nodes.length; i++) {
			nodes[i] = g.createNode();
		}

		int i = 1;
		for (Vertex currentNode : g.vertices()) {
			assertEquals(currentNode.getId(), nodes[i++].getId());
		}

		Comparator<Vertex> comp = new Comparator<Vertex>() {

			@Override
			public int compare(Vertex o1, Vertex o2) {
				return Double.compare(o2.getId(), o1.getId());
			}

		};

		g.sortVertexList(comp);

		i = VERTEX_COUNT;
		for (Vertex currentNode : g.vertices()) {
			assertEquals(currentNode.getId(), nodes[i--].getId());
		}

		comp = new Comparator<Vertex>() {

			@Override
			public int compare(Vertex o1, Vertex o2) {
				return Double.compare(o1.getId(), o2.getId());
			}

		};

		g.sortVertexList(comp);

		i = 1;
		for (Vertex currentNode : g.vertices()) {
			assertEquals(currentNode.getId(), nodes[i++].getId());
		}
		commit(g);

	}
}
