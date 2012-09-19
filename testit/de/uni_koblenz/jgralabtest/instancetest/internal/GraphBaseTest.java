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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.InternalGraph;
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

	private InternalGraph g1;
	private InternalGraph g2;
	private Vertex v2, v3, v5, v7, v8, v11;

	@Before
	public void setUp() {
		g1 = (InternalGraph) createNewGraph();
		g2 = (InternalGraph) createNewGraph();
		// System.out.println("Graph2 is instance of class " + g2.getClass());
		g1.createVertex(SubNode.VC);
		// System.out.println("V1 is instance of class " + v1.getClass());
		v2 = g1.createVertex(SubNode.VC);
		v3 = g1.createVertex(SubNode.VC);
		g1.createVertex(SubNode.VC);
		v5 = g1.createVertex(SuperNode.VC);
		g1.createVertex(SuperNode.VC);
		v7 = g1.createVertex(SuperNode.VC);
		v8 = g1.createVertex(SuperNode.VC);
		g1.createVertex(DoubleSubNode.VC);
		// System.out.println("v9= " + v9);
		g1.createVertex(DoubleSubNode.VC);
		v11 = g1.createVertex(DoubleSubNode.VC);
		g1.createVertex(DoubleSubNode.VC);
	}

	private HashSet<String> graphIdsInUse = new HashSet<String>();

	/**
	 * 
	 * @return
	 */
	private VertexTestGraph createNewGraph() {
		VertexTestGraph out = null;
		switch (implementationType) {
		case STANDARD:
			out = VertexTestSchema.instance().createVertexTestGraph(
					ImplementationType.STANDARD);
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}
		graphIdsInUse.add(out.getId());
		return out;
	}

	/**
	 * Asserts true if the edgeListVersion has changed. Returns the new
	 * edgeListVersion.
	 * 
	 * @param elv1
	 *            the edgeListVersion before the transaction.
	 * @return the edgeListVersion after the transaction. @ * should not happen.
	 */
	private long checkIfEdgeListVersionChanged(long elv1) {
		long out;
		assertTrue(elv1 < g1.getEdgeListVersion());
		out = g1.getEdgeListVersion();
		return out;
	}

	/**
	 * Asserts true if the edgeListVersion has not changed.
	 * 
	 * @param elv1
	 *            the edgeListVersion before the transaction. @ * should not
	 *            happen.
	 */
	private void checkIfEdgeListVersionRemained(long elv1) {
		assertTrue(elv1 == g1.getEdgeListVersion());
	}

	private MinimalGraph createMinimalGraph() {
		MinimalGraph g3 = null;
		switch (implementationType) {
		case STANDARD:
			g3 = MinimalSchema.instance().createMinimalGraph(
					ImplementationType.STANDARD);
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}
		return g3;
	}

	@Test
	public void testDefragment() {
		/*
		 * Testen der defragment()-Methode: Ein Vorher-Nachher Abbild von
		 * Vertex- Referenzen sammeln und vergleichen, genauso mit Kantenseq.
		 * Inzidenzen sind nicht betroffen (von defragment() zumindest das, was
		 * einfach zu testen ist); Dafür bedarf es einen Graph, indem gelöscht
		 * wurde und dadurch Lücken entstanden sind, sodass defragment() zum
		 * Einsatz kommen kann
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
		((InternalGraph) g).defragment();
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
			links[i] = g.createLink((Node) g.getVertex(i + 1),
					(Node) g.getVertex((i % 20) + 1));
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
		((InternalGraph) g).defragment();
		// check if the maximum id is 15
		assertEquals(15, g.getECount());
		for (int i = 1; i <= 15; i++) {
			assertNotNull(g.getEdge(i));
		}
		for (int i = 16; i <= 20; i++) {
			assertNull(g.getEdge(i));
		}
	}

	// TODO continue here
	@Test
	public void testGetEdgeListVersion() throws Exception {
		// preparations...
		Vertex v1 = g1.createVertex(SubNode.VC);
		Vertex v2 = g1.createVertex(SubNode.VC);
		Vertex v3 = g1.createVertex(SubNode.VC);
		Vertex v4 = g1.createVertex(SubNode.VC);
		Vertex v5 = g1.createVertex(SuperNode.VC);
		Vertex v6 = g1.createVertex(SuperNode.VC);
		Vertex v7 = g1.createVertex(SuperNode.VC);
		Vertex v8 = g1.createVertex(SuperNode.VC);
		Vertex v9 = g1.createVertex(DoubleSubNode.VC);
		Vertex v10 = g1.createVertex(DoubleSubNode.VC);
		Vertex v11 = g1.createVertex(DoubleSubNode.VC);
		Vertex v12 = g1.createVertex(DoubleSubNode.VC);

		long elv1;
		// border cases
		elv1 = g1.getEdgeListVersion();
		assertEquals(0, elv1);
		assertEquals(0, g2.getEdgeListVersion());

		Edge e1 = g1.createEdge(SubLink.EC, v9, v7);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.deleteEdge(e1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		// normal cases
		g1.createEdge(SubLink.EC, v10, v5);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(SubLink.EC, v10, v6);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(SubLink.EC, v10, v7);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(SubLink.EC, v10, v8);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(SubLink.EC, v11, v5);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(SubLink.EC, v11, v6);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(SubLink.EC, v11, v7);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(SubLink.EC, v11, v8);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(SubLink.EC, v12, v5);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(SubLink.EC, v12, v6);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(SubLink.EC, v12, v7);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		Edge e3 = g1.createEdge(SubLink.EC, v12, v8);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(SubLink.EC, v9, v6);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(SubLink.EC, v9, v7);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(SubLink.EC, v9, v8);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.deleteEdge(e3);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		// when deleting a vertex, incident edges are also deleted and the
		// edgeListVersion changes.
		Vertex v13 = g1.createVertex(DoubleSubNode.VC);
		Vertex v14 = g1.createVertex(DoubleSubNode.VC);
		g1.createEdge(Link.EC, v13, v14);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.deleteVertex(v13);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		// when deleting a vertex with degree=0, the edgeListVersion should
		// remain unchanged.
		g1.deleteVertex(v14);

		// same
		checkIfEdgeListVersionRemained(elv1);

		g1.createEdge(Link.EC, v1, v5);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(Link.EC, v2, v5);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(Link.EC, v3, v5);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(Link.EC, v4, v5);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(Link.EC, v10, v5);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(Link.EC, v11, v5);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(Link.EC, v12, v5);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		// v6 does not exist anymore
		g1.createEdge(Link.EC, v1, v6);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(Link.EC, v1, v7);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(Link.EC, v1, v8);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		Edge e4 = g1.createEdge(Link.EC, v3, v7);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(Link.EC, v11, v8);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(LinkBack.EC, v5, v1);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(LinkBack.EC, v6, v2);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		Edge e5 = g1.createEdge(LinkBack.EC, v7, v3);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(LinkBack.EC, v8, v4);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(LinkBack.EC, v8, v9);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(LinkBack.EC, v7, v10);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.createEdge(LinkBack.EC, v6, v11);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		Edge e6 = g1.createEdge(LinkBack.EC, v5, v12);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.deleteEdge(e4);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.deleteEdge(e5);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		g1.deleteEdge(e6);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		// reordering edges does change the edgeListVersion
		Edge e7 = g1.createEdge(SubLink.EC, v9, v5);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		Edge e8 = g1.createEdge(SubLink.EC, v12, v7);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		Edge e9 = g1.createEdge(SubLink.EC, v11, v6);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		e7.putBeforeEdge(e9);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		e8.putBeforeEdge(e7);

		// same
		checkIfEdgeListVersionRemained(elv1);

		e9.putAfterEdge(e8);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		e8.putAfterEdge(e7);

		elv1 = checkIfEdgeListVersionChanged(elv1);

		// changing attributes does not change the edgeListVersion
		e7.setAttribute("anInt", 22);

		// same
		checkIfEdgeListVersionRemained(elv1);

		e8.setAttribute("anInt", 203);

		// same
		checkIfEdgeListVersionRemained(elv1);

		e9.setAttribute("anInt", 2209);

		// same
		checkIfEdgeListVersionRemained(elv1);

		e7.setAttribute("anInt", 15);

		// same
		checkIfEdgeListVersionRemained(elv1);

	}

	@Test
	public void testGetVertexListVersion() {

		// border cases
		long vertexListVersion2 = g2.getVertexListVersion();
		// assertEquals(0, vertexListVersion2);

		Vertex v13 = g2.createVertex(SuperNode.VC);

		assertTrue(vertexListVersion2 < g2.getVertexListVersion());
		vertexListVersion2 = g2.getVertexListVersion();

		// normal cases
		long vertexListVersion1 = g1.getVertexListVersion();
		// assertEquals(12, vertexListVersion1); with transactions enabled it is
		// not 12

		g1.createVertex(SubNode.VC);

		assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		vertexListVersion1 = g1.getVertexListVersion();

		g2.createVertex(SuperNode.VC);

		assertTrue(vertexListVersion2 < g2.getVertexListVersion());
		vertexListVersion2 = g2.getVertexListVersion();

		g2.createVertex(DoubleSubNode.VC);

		assertTrue(vertexListVersion2 < g2.getVertexListVersion());
		vertexListVersion2 = g2.getVertexListVersion();

		for (int i = 4; i < 100; i++) {
			g2.createVertex(SuperNode.VC);

			assertTrue(vertexListVersion2 < g2.getVertexListVersion());
			vertexListVersion2 = g2.getVertexListVersion();
		}

		g2.createVertex(DoubleSubNode.VC);

		assertTrue(vertexListVersion2 < g2.getVertexListVersion());
		vertexListVersion2 = g2.getVertexListVersion();

		// tests whether the version changes correctly if vertices are deleted
		g2.deleteVertex(v13);

		assertTrue(vertexListVersion2 < g2.getVertexListVersion());
		vertexListVersion2 = g2.getVertexListVersion();

		for (int i = 14; i < 31; i += 3) {
			g1.createVertex(DoubleSubNode.VC);

			assertTrue(vertexListVersion1 < g1.getVertexListVersion());
			vertexListVersion1 = g1.getVertexListVersion();

			g1.createVertex(SubNode.VC);

			assertTrue(vertexListVersion1 < g1.getVertexListVersion());
			vertexListVersion1 = g1.getVertexListVersion();

			g1.createVertex(SuperNode.VC);

			assertTrue(vertexListVersion1 < g1.getVertexListVersion());
			vertexListVersion1 = g1.getVertexListVersion();
		}

		Vertex v14 = g1.createVertex(SuperNode.VC);

		assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		vertexListVersion1 = g1.getVertexListVersion();

		Vertex v15 = g1.createVertex(DoubleSubNode.VC);

		assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		vertexListVersion1 = g1.getVertexListVersion();

		// TODO deleted vertices should not be used for new edges
		// // g1.deleteVertex(v15);
		// //
		// // assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		// vertexListVersion1 = g1.getVertexListVersion();
		//
		// // g1.deleteVertex(v14);
		// //
		// // assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		// vertexListVersion1 = g1.getVertexListVersion();
		//
		// makes sure that editing edges does not change the vertexList
		g1.createEdge(SubLink.EC, v15, v14);

		assertEquals(vertexListVersion1, g1.getVertexListVersion());

		g1.createEdge(LinkBack.EC, v14, v15);

		assertEquals(vertexListVersion1, g1.getVertexListVersion());

		// reordering the vertices does change the vertexListVersion
		v3.putAfter(v7);

		assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		vertexListVersion1 = g1.getVertexListVersion();

		v5.putBefore(v2);

		assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		vertexListVersion1 = g1.getVertexListVersion();

		v5.putAfter(v3);

		assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		vertexListVersion1 = g1.getVertexListVersion();

		v7.putBefore(v2);

		assertTrue(vertexListVersion1 < g1.getVertexListVersion());
		vertexListVersion1 = g1.getVertexListVersion();

		v7.putBefore(v2);// v7 is already before v2

		assertEquals(vertexListVersion1, g1.getVertexListVersion());

		// changing attributes of vertices does not change the vertexListVersion
		v5.setAttribute("number", 17);

		assertEquals(vertexListVersion1, g1.getVertexListVersion());

		v8.setAttribute("number", 42);

		assertEquals(vertexListVersion1, g1.getVertexListVersion());

		v7.setAttribute("number", 2);

		assertEquals(vertexListVersion1, g1.getVertexListVersion());

		v5.setAttribute("number", 15);

		assertEquals(vertexListVersion1, g1.getVertexListVersion());

	}

	@Test
	public void testIsEdgeListModified() {
		// preparations...
		Vertex v13 = g2.createVertex(SubNode.VC);
		Vertex v14 = g2.createVertex(SubNode.VC);
		Vertex v15 = g2.createVertex(SubNode.VC);
		Vertex v16 = g2.createVertex(SubNode.VC);
		Vertex v17 = g2.createVertex(SuperNode.VC);
		Vertex v18 = g2.createVertex(SuperNode.VC);
		Vertex v19 = g2.createVertex(SuperNode.VC);
		Vertex v20 = g2.createVertex(SuperNode.VC);
		Vertex v21 = g2.createVertex(DoubleSubNode.VC);
		Vertex v22 = g2.createVertex(DoubleSubNode.VC);
		Vertex v23 = g2.createVertex(DoubleSubNode.VC);
		Vertex v24 = g2.createVertex(DoubleSubNode.VC);

		// border cases
		long edgeListVersion1 = g1.getEdgeListVersion();
		long edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g1.isEdgeListModified(edgeListVersion1));
		assertFalse(g2.isEdgeListModified(edgeListVersion2));

		g1.createEdge(SubLink.EC, v11, v7);
		Edge e1 = g2.createEdge(Link.EC, v15, v19);
		assertTrue(g1.isEdgeListModified(edgeListVersion1));
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion1 = g1.getEdgeListVersion();
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g1.isEdgeListModified(edgeListVersion1));
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		g2.deleteEdge(e1);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));

		// normal cases
		int ecount = g2.getECount();
		g2.createEdge(LinkBack.EC, v19, v15);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		assertEquals(ecount + 1, g2.getECount());
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		g2.createEdge(Link.EC, v15, v19);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		Edge e2 = g2.createEdge(SubLink.EC, v23, v19);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		g2.createEdge(Link.EC, v16, v20);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		Edge e3 = g2.createEdge(Link.EC, v23, v20);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		g2.createEdge(Link.EC, v24, v19);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		g2.createEdge(LinkBack.EC, v20, v16);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		Edge e4 = g2.createEdge(SubLink.EC, v24, v20);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		g2.deleteEdge(e2);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		g2.createEdge(LinkBack.EC, v19, v23);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		g2.createEdge(LinkBack.EC, v20, v24);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		g2.deleteEdge(e4);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		g2.deleteEdge(e3);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		Edge e5 = g2.createEdge(SubLink.EC, v21, v17);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		Edge e6 = g2.createEdge(Link.EC, v13, v18);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));
		Edge e7 = g2.createEdge(LinkBack.EC, v17, v14);
		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));

		g2.createEdge(Link.EC, v22, v18);

		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));

		// adding vertices does not affect the edgeList
		Vertex v25 = g2.createVertex(DoubleSubNode.VC);

		assertFalse(g2.isEdgeListModified(edgeListVersion2));

		Vertex v26 = g2.createVertex(SuperNode.VC);

		assertFalse(g2.isEdgeListModified(edgeListVersion2));

		g2.deleteVertex(v20);

		assertTrue(g2.isEdgeListModified(edgeListVersion2));

		// reordering edges does change the edgeList
		e6.putBeforeEdge(e5);

		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));

		e5.putAfterEdge(e6);

		assertFalse(g2.isEdgeListModified(edgeListVersion2));

		e5.putAfterEdge(e7);

		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));

		// changing the attributes of an edge does not change the edgeList
		Edge e8 = g2.createEdge(SubLink.EC, v25, v26);

		assertTrue(g2.isEdgeListModified(edgeListVersion2));
		edgeListVersion2 = g2.getEdgeListVersion();
		assertFalse(g2.isEdgeListModified(edgeListVersion2));

		e8.setAttribute("anInt", 2);

		assertFalse(g2.isEdgeListModified(edgeListVersion2));

		e8.setAttribute("anInt", -41);

		assertFalse(g2.isEdgeListModified(edgeListVersion2));

		e8.setAttribute("anInt", 1024);

		assertFalse(g2.isEdgeListModified(edgeListVersion2));

		e8.setAttribute("anInt", 15);

		assertFalse(g2.isEdgeListModified(edgeListVersion2));

	}

	@Test
	public void testIsLoading() {
		// TODO how do I get isLoading to return true
		assertFalse(g1.isLoading());
		assertFalse(g2.isLoading());
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
	public void testIsVertexListModified() {
		// border cases
		long vListVersion1 = g1.getVertexListVersion();
		long vListVersion2 = g2.getVertexListVersion();

		assertFalse(g1.isVertexListModified(vListVersion1));
		assertFalse(g2.isVertexListModified(vListVersion2));
		Vertex v1 = g1.createVertex(DoubleSubNode.VC);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		Vertex v2 = g1.createVertex(SuperNode.VC);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		// makes sure that changing edges does not affect the vertexList
		g1.createEdge(SubLink.EC, v1, v2);
		assertFalse(g1.isVertexListModified(vListVersion1));
		g1.createEdge(Link.EC, v1, v2);
		assertFalse(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		g1.deleteVertex(v2);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));

		// normal cases
		for (int i = 0; i < 21; i++) {
			g1.createVertex(SubNode.VC);

			assertTrue(g1.isVertexListModified(vListVersion1));
			vListVersion1 = g1.getVertexListVersion();
			assertFalse(g1.isVertexListModified(vListVersion1));
		}

		g1.deleteVertex(v1);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));

		for (int i = 0; i < 12; i++) {
			g1.createVertex(SuperNode.VC);

			assertTrue(g1.isVertexListModified(vListVersion1));
			vListVersion1 = g1.getVertexListVersion();
			assertFalse(g1.isVertexListModified(vListVersion1));
		}
		vListVersion1 = g1.getVertexListVersion();

		Vertex v3 = g1.createVertex(SubNode.VC);

		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		Vertex v4 = g1.createVertex(SuperNode.VC);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));

		// if the order of the vertices is changed the vertexList is modified
		v3.putAfter(v4);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		v3.putAfter(v4);// v3 is already after v4
		assertFalse(g1.isVertexListModified(vListVersion1));
		v3.putBefore(v4);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		Vertex v5 = g1.createVertex(DoubleSubNode.VC);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		v5.putBefore(v3);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));
		v4.putAfter(v5);
		assertTrue(g1.isVertexListModified(vListVersion1));
		vListVersion1 = g1.getVertexListVersion();
		assertFalse(g1.isVertexListModified(vListVersion1));

		// if attributes of vertices are changed this does not affect the
		// vertexList
		v4.setAttribute("number", 5);
		assertFalse(g1.isVertexListModified(vListVersion1));
		v4.setAttribute("number", 42);
		assertFalse(g1.isVertexListModified(vListVersion1));
	}

	@Test
	public void testSetId() {
		g1.setId("alpha");
		assertEquals("alpha", g1.getId());
		g1.setId("1265");
		assertEquals("1265", g1.getId());
		g1.setId("007");
		assertEquals("007", g1.getId());
		g1.setId("r2d2");
		assertEquals("r2d2", g1.getId());
		g1.setId("answer:42");
		assertEquals("answer:42", g1.getId());
		g1.setId("1506");
		assertEquals("1506", g1.getId());
		g1.setId("june15");
		assertEquals("june15", g1.getId());
		g1.setId("bang");
		assertEquals("bang", g1.getId());
		g1.setId("22now");
		assertEquals("22now", g1.getId());
		g1.setId("hjkutzbv");
		assertEquals("hjkutzbv", g1.getId());
		g1.setId("54rdcg9");
		assertEquals("54rdcg9", g1.getId());
		g1.setId(".k,oibt");
		assertEquals(".k,oibt", g1.getId());
	}

}
