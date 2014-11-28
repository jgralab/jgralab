/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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
package de.uni_koblenz.jgralabtest.instancetest;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Comparator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.minimal.Node;

@RunWith(Parameterized.class)
public class VertexListTest extends InstanceTest {
	private static final int VERTEX_COUNT = 10;

	public VertexListTest(ImplementationType implementationType, String dbURL) {
		super(implementationType, dbURL);
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
	public void setup() {
		switch (implementationType) {
		case STANDARD:
			g = MinimalSchema.instance().createMinimalGraph(
					ImplementationType.STANDARD, null, V, E);
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}
		for (int i = 0; i < N; ++i) {
			g.createNode();
		}
	}

	@Test
	public void addVertexTest() throws Exception {
		assertEquals(10, g.getVCount());
		assertEquals("v1 v2 v3 v4 v5 v6 v7 v8 v9 v10", getVSeq());
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
		Vertex v5 = g.getVertex(5);
		v5.putBefore(g.getVertex(6));

		assertTrue(v5.isBefore(g.getVertex(6)));
		assertEquals("v1 v2 v3 v4 v5 v6 v7 v8 v9 v10", getVSeq());

		assertTrue(v5.isAfter(g.getVertex(4)));
		assertFalse(v5.isBefore(g.getVertex(4)));

		v5.putBefore(g.getVertex(4));

		assertEquals("v1 v2 v3 v5 v4 v6 v7 v8 v9 v10", getVSeq());
		assertFalse(v5.isAfter(g.getVertex(4)));
		assertTrue(v5.isBefore(g.getVertex(4)));

		v5.putBefore(g.getVertex(10));

		assertEquals("v1 v2 v3 v4 v6 v7 v8 v9 v5 v10", getVSeq());
		assertFalse(v5.isAfter(g.getVertex(10)));
		assertTrue(v5.isBefore(g.getVertex(10)));

		assertFalse(v5.isBefore(g.getVertex(1)));
		assertTrue(g.getVertex(1).isBefore(v5));

		v5.putBefore(g.getVertex(1));

		assertEquals("v5 v1 v2 v3 v4 v6 v7 v8 v9 v10", getVSeq());
		assertTrue(v5.isBefore(g.getVertex(1)));
		assertFalse(v5.isAfter(g.getVertex(1)));
		assertTrue(g.getVertex(1).isAfter(v5));
	}

	@Test
	public void putAfterTest() throws Exception {
		Vertex v5 = g.getVertex(5);

		v5.putAfter(g.getVertex(4));

		assertEquals("v1 v2 v3 v4 v5 v6 v7 v8 v9 v10", getVSeq());

		v5.putAfter(g.getVertex(6));

		assertEquals("v1 v2 v3 v4 v6 v5 v7 v8 v9 v10", getVSeq());

		v5.putAfter(g.getVertex(10));

		assertEquals("v1 v2 v3 v4 v6 v7 v8 v9 v10 v5", getVSeq());

		v5.putAfter(g.getVertex(1));

		assertEquals("v1 v5 v2 v3 v4 v6 v7 v8 v9 v10", getVSeq());
	}

	@Test
	public void deleteVertexTest() throws Exception {
		Vertex v = g.getVertex(5);
		v.delete();

		assertFalse(v.isValid());
		assertNull(g.getVertex(5));
		assertEquals(9, g.getVCount());
		assertEquals("v1 v2 v3 v4 v6 v7 v8 v9 v10", getVSeq());

		v = g.getFirstVertex();
		v.delete();

		assertFalse(v.isValid());
		assertNull(g.getVertex(1));
		assertEquals(8, g.getVCount());
		assertEquals("v2 v3 v4 v6 v7 v8 v9 v10", getVSeq());

		v = g.getVertex(10);
		v.delete();

		assertFalse(v.isValid());
		assertNull(g.getVertex(1));
		assertEquals(7, g.getVCount());
		assertEquals("v2 v3 v4 v6 v7 v8 v9", getVSeq());

		g.createNode();

		assertEquals("v2 v3 v4 v6 v7 v8 v9 v1", getVSeq());

		g.createNode();

		assertEquals("v2 v3 v4 v6 v7 v8 v9 v1 v5", getVSeq());

		g.createNode();

		assertEquals("v2 v3 v4 v6 v7 v8 v9 v1 v5 v10", getVSeq());

		g.createNode();

		assertEquals("v2 v3 v4 v6 v7 v8 v9 v1 v5 v10 v11", getVSeq());
	}

	/**
	 * Rudimentary test for sortVertexList. It sorts the vertices in reverse
	 * order to the id and back. For transaction support it has to be tested in
	 * the same transaction, because otherwise the IDs would be changed.
	 * 
	 * @
	 */
	@Test
	public void testSortVertexList() {
		MinimalGraph g = null;
		switch (implementationType) {
		case STANDARD:
			g = MinimalSchema.instance().createMinimalGraph(
					ImplementationType.STANDARD, null, V, E);
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}

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

		g.sortVertices(comp);

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

		g.sortVertices(comp);

		i = 1;
		for (Vertex currentNode : g.vertices()) {
			assertEquals(currentNode.getId(), nodes[i++].getId());
		}

	}
}
