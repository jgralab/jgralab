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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.schema.GreqlSchema;
import de.uni_koblenz.jgralab.impl.InternalGraph;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.vertextest.DoubleSubNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.Link;
import de.uni_koblenz.jgralabtest.schemas.vertextest.LinkBack;
import de.uni_koblenz.jgralabtest.schemas.vertextest.SubLink;
import de.uni_koblenz.jgralabtest.schemas.vertextest.SubNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.SuperNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestGraph;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestSchema;

@RunWith(Parameterized.class)
public class GraphTest extends InstanceTest {

	public GraphTest(ImplementationType implementationType, String dbURL) {
		super(implementationType, dbURL);
	}

	@Parameters
	public static Collection<Object[]> configure() {
		return getParameters();
	}

	private VertexTestGraph g1;
	private VertexTestGraph g2;
	private Vertex v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12;
	private VertexClass subN = null, superN = null, doubleSubN = null;
	private EdgeClass link = null, subL = null, lBack = null;

	@Before
	public void setUp() {
		g1 = createNewGraph();
		g2 = createNewGraph();
		// System.out.println("Graph2 is instance of class " + g2.getClass());
		v1 = g1.createVertex(SubNode.VC);
		// System.out.println("V1 is instance of class " + v1.getClass());
		v2 = g1.createVertex(SubNode.VC);
		v3 = g1.createVertex(SubNode.VC);
		v4 = g1.createVertex(SubNode.VC);
		v5 = g1.createVertex(SuperNode.VC);
		v6 = g1.createVertex(SuperNode.VC);
		v7 = g1.createVertex(SuperNode.VC);
		v8 = g1.createVertex(SuperNode.VC);
		v9 = g1.createVertex(DoubleSubNode.VC);
		// System.out.println("v9= " + v9);
		v10 = g1.createVertex(DoubleSubNode.VC);
		v11 = g1.createVertex(DoubleSubNode.VC);
		v12 = g1.createVertex(DoubleSubNode.VC);
	}

	private ArrayList<String> graphIdsInUse = new ArrayList<>();

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

	private void getVertexClassesOfG1() {
		// get vertex- and edge-classes
		// VertexClass abstractSuperN;
		List<VertexClass> vclasses = g1.getGraphClass().getVertexClasses();
		for (VertexClass vc : vclasses) {
			// if (vc.getSimpleName().equals("AbstractSuperNode")) {
			// abstractSuperN = vc;
			// } else
			if (vc.getSimpleName().equals("SubNode")) {
				subN = vc;
			} else if (vc.getSimpleName().equals("SuperNode")) {
				superN = vc;
			} else if (vc.getSimpleName().equals("DoubleSubNode")) {
				doubleSubN = vc;
			}
		}
	}

	private void getEdgeClassesOfG1() {
		// preparations...
		List<EdgeClass> eclasses = g1.getGraphClass().getEdgeClasses();
		for (EdgeClass ec : eclasses) {
			if (ec.getSimpleName().equals("Link")) {
				link = ec;
			} else if (ec.getSimpleName().equals("SubLink")) {
				subL = ec;
			} else if (ec.getSimpleName().equals("LinkBack")) {
				lBack = ec;
			}
		}
	}

	@Test
	public void testCreateVertex() {
		Vertex v13 = g1.createVertex(SubNode.VC);
		Vertex v14 = g2.createVertex(SubNode.VC);
		Vertex v15 = g1.createVertex(SuperNode.VC);
		Vertex v16 = g2.createVertex(SuperNode.VC);
		Vertex v17 = g1.createVertex(DoubleSubNode.VC);
		Vertex v18 = g2.createVertex(DoubleSubNode.VC);

		Vertex[] graphVertices = { v1, v2, v3, v4, v5, v6, v7, v8, v9, v10,
				v11, v12, v13, v15, v17 };
		Vertex[] graph2Vertices = { v14, v16, v18 };

		// tests whether the vertex is an instance of the expected class
		assertTrue(v1 instanceof SubNode);
		assertTrue(v2 instanceof SubNode);
		assertTrue(v3 instanceof SubNode);
		assertTrue(v4 instanceof SubNode);
		assertTrue(v5 instanceof SuperNode);
		assertTrue(v6 instanceof SuperNode);
		assertTrue(v7 instanceof SuperNode);
		assertTrue(v8 instanceof SuperNode);
		assertTrue(v9 instanceof DoubleSubNode);
		assertTrue(v10 instanceof DoubleSubNode);
		assertTrue(v11 instanceof DoubleSubNode);
		assertTrue(v12 instanceof DoubleSubNode);
		assertTrue(v13 instanceof SubNode);
		assertTrue(v14 instanceof SubNode);
		assertTrue(v15 instanceof SuperNode);
		assertTrue(v16 instanceof SuperNode);
		assertTrue(v17 instanceof DoubleSubNode);
		assertTrue(v18 instanceof DoubleSubNode);

		// tests whether the graphs contain the right vertices in the right
		// order
		int i = 0;// the position of the vertex corresponding to the one
		// currently returned by the iterator
		for (Vertex v : g1.vertices()) {
			assertEquals(graphVertices[i], v);
			i++;
		}
		i = 0;
		for (Vertex v : g2.vertices()) {
			assertEquals(graph2Vertices[i], v);
			i++;
		}

	}

	@Test
	public void testCreateEdge() {
		Edge e1 = g1.createEdge(SubLink.EC, v9, v5);
		Edge e2 = g1.createEdge(SubLink.EC, v10, v6);
		Edge e3 = g1.createEdge(SubLink.EC, v12, v8);
		Edge e4 = g1.createEdge(Link.EC, v1, v5);
		Edge e5 = g1.createEdge(Link.EC, v2, v6);
		Edge e6 = g1.createEdge(Link.EC, v9, v6);
		Edge e7 = g1.createEdge(Link.EC, v10, v5);
		Edge e8 = g1.createEdge(Link.EC, v11, v6);
		Edge e9 = g1.createEdge(LinkBack.EC, v5, v1);
		Edge e10 = g1.createEdge(LinkBack.EC, v6, v2);
		Edge e11 = g1.createEdge(LinkBack.EC, v5, v9);
		Edge e12 = g1.createEdge(LinkBack.EC, v6, v10);
		Edge e13 = g1.createEdge(LinkBack.EC, v5, v12);
		Edge e14 = g1.createEdge(LinkBack.EC, v6, v10); // the
														// same
														// as
														// e12

		// tests whether the edge is an instance of the expected class
		assertTrue(e1 instanceof SubLink);
		assertTrue(e2 instanceof SubLink);
		assertTrue(e3 instanceof SubLink);
		assertTrue(e4 instanceof Link);
		assertTrue(e5 instanceof Link);
		assertTrue(e6 instanceof Link);
		assertTrue(e7 instanceof Link);
		assertTrue(e8 instanceof Link);
		assertTrue(e9 instanceof LinkBack);
		assertTrue(e10 instanceof LinkBack);
		assertTrue(e11 instanceof LinkBack);
		assertTrue(e12 instanceof LinkBack);
		assertTrue(e13 instanceof LinkBack);
		assertTrue(e14 instanceof LinkBack);
		/*
		 * tests whether the edges are part of the right graph and have been
		 * inserted in the right order
		 */
		Edge[] graphEdges = { e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11,
				e12, e13, e14 };
		int i = 0;// refers to the position of the edge which the iterator
		// currently returns
		for (Edge e : g1.edges()) {
			assertEquals(graphEdges[i], e);
			i++;
		}

		// tests whether the alpha-/ omega-vertex of the edge have been set
		// correctly
		assertEquals(v9, e1.getAlpha());
		assertEquals(v5, e1.getOmega());
		assertEquals(v10, e2.getAlpha());
		assertEquals(v6, e2.getOmega());
		assertEquals(v12, e3.getAlpha());
		assertEquals(v8, e3.getOmega());
		assertEquals(v1, e4.getAlpha());
		assertEquals(v5, e4.getOmega());
		assertEquals(v2, e5.getAlpha());
		assertEquals(v6, e5.getOmega());
		assertEquals(v9, e6.getAlpha());
		assertEquals(v6, e6.getOmega());
		assertEquals(v10, e7.getAlpha());
		assertEquals(v5, e7.getOmega());
		assertEquals(v11, e8.getAlpha());
		assertEquals(v6, e8.getOmega());
		assertEquals(v5, e9.getAlpha());
		assertEquals(v1, e9.getOmega());
		assertEquals(v6, e10.getAlpha());
		assertEquals(v2, e10.getOmega());
		assertEquals(v5, e11.getAlpha());
		assertEquals(v9, e11.getOmega());
		assertEquals(v6, e12.getAlpha());
		assertEquals(v10, e12.getOmega());
		assertEquals(v5, e13.getAlpha());
		assertEquals(v12, e13.getOmega());
		assertEquals(v6, e14.getAlpha());
		assertEquals(v10, e14.getOmega());
	}

	@Test
	public void testIsGraphModified() throws Exception {
		long gVersion1 = g1.getGraphVersion();
		long gVersion2 = g2.getGraphVersion();
		assertFalse(g1.isGraphModified(gVersion1));
		assertFalse(g2.isGraphModified(gVersion2));

		g1.createEdge(SubLink.EC, v9, v5);
		g2.createSubNode();

		assertTrue(g1.isGraphModified(gVersion1));
		assertTrue(g2.isGraphModified(gVersion2));
		gVersion1 = g1.getGraphVersion();
		gVersion2 = g2.getGraphVersion();

		Edge e1 = g1.createEdge(Link.EC, v1, v6);
		g2.createSuperNode();

		assertTrue(g1.isGraphModified(gVersion1));
		assertTrue(g2.isGraphModified(gVersion2));
		gVersion1 = g1.getGraphVersion();
		gVersion2 = g2.getGraphVersion();

		g1.createEdge(LinkBack.EC, v7, v10);
		g2.createDoubleSubNode();

		assertTrue(g1.isGraphModified(gVersion1));
		assertTrue(g2.isGraphModified(gVersion2));
		gVersion1 = g1.getGraphVersion();
		gVersion2 = g2.getGraphVersion();

		Edge e2 = g1.createEdge(SubLink.EC, v9, v5);

		assertTrue(g1.isGraphModified(gVersion1));
		assertFalse(g2.isGraphModified(gVersion2));
		gVersion1 = g1.getGraphVersion();

		g1.deleteEdge(e1);

		assertTrue(g1.isGraphModified(gVersion1));
		assertFalse(g2.isGraphModified(gVersion2));
		gVersion1 = g1.getGraphVersion();

		g1.deleteVertex(v3);

		assertTrue(g1.isGraphModified(gVersion1));
		assertFalse(g2.isGraphModified(gVersion2));
		gVersion1 = g1.getGraphVersion();

		g1.deleteEdge(e2);

		assertTrue(g1.containsVertex(v9));
		assertTrue(g1.containsVertex(v5));

		assertTrue(g1.isGraphModified(gVersion1));
		assertFalse(g2.isGraphModified(gVersion2));
		gVersion1 = g1.getGraphVersion();

		// TODO why does an exception occur here?
		g1.deleteVertex(v9);

		assertTrue(g1.isGraphModified(gVersion1));
		assertFalse(g2.isGraphModified(gVersion2));
	}

	@Test
	public void testGetGraphVersion() {
		long graphVersion2 = g2.getGraphVersion();
		assertEquals(0, graphVersion2);

		g2.createDoubleSubNode();

		assertTrue(graphVersion2 < g2.getGraphVersion());
		graphVersion2 = g2.getGraphVersion();

		g2.createDoubleSubNode();

		assertTrue(graphVersion2 < g2.getGraphVersion());
		graphVersion2 = g2.getGraphVersion();

		DoubleSubNode v1 = g2.createDoubleSubNode();
		g2.createDoubleSubNode();

		assertTrue(graphVersion2 < g2.getGraphVersion());
		graphVersion2 = g2.getGraphVersion();

		for (int i = 0; i < 20; i++) {
			g2.createSubNode();

			assertTrue(graphVersion2 < g2.getGraphVersion());
			graphVersion2 = g2.getGraphVersion();
		}
		assertEquals(graphVersion2, g2.getGraphVersion());

		g2.deleteVertex(v1);

		assertTrue(graphVersion2 < g2.getGraphVersion());
		graphVersion2 = g2.getGraphVersion();
	}

	@Test
	public void testContainsVertex() {
		DoubleSubNode v13 = g1.createDoubleSubNode();
		DoubleSubNode v14 = g2.createDoubleSubNode();
		SubNode v15 = g1.createSubNode();
		SubNode v16 = g2.createSubNode();
		SuperNode v17 = g1.createSuperNode();
		SuperNode v18 = g2.createSuperNode();
		Vertex v19 = g2.createVertex(DoubleSubNode.VC);
		Vertex v20 = g2.createVertex(SubNode.VC);
		Vertex v21 = g2.createVertex(SuperNode.VC);

		assertTrue(g1.containsVertex(v13));
		assertTrue(g1.containsVertex(v15));
		assertTrue(g1.containsVertex(v17));
		assertTrue(g2.containsVertex(v14));
		assertTrue(g2.containsVertex(v16));
		assertTrue(g2.containsVertex(v18));
		assertTrue(g2.containsVertex(v19));
		assertTrue(g2.containsVertex(v20));
		assertTrue(g2.containsVertex(v21));
		assertTrue(g1.containsVertex(v1));
		assertTrue(g1.containsVertex(v2));
		assertTrue(g1.containsVertex(v3));
		assertTrue(g1.containsVertex(v4));
		assertTrue(g1.containsVertex(v5));
		assertTrue(g1.containsVertex(v6));
		assertTrue(g1.containsVertex(v7));
		assertTrue(g1.containsVertex(v8));
		assertTrue(g1.containsVertex(v9));
		assertTrue(g1.containsVertex(v10));
		assertTrue(g1.containsVertex(v11));
		assertTrue(g1.containsVertex(v12));

		assertFalse(g1.containsVertex(v14));
		assertFalse(g1.containsVertex(v16));
		assertFalse(g1.containsVertex(v18));
		assertFalse(g2.containsVertex(v13));
		assertFalse(g2.containsVertex(v15));
		assertFalse(g2.containsVertex(v17));
		assertFalse(g2.containsVertex(v10));
		assertFalse(g2.containsVertex(v1));
		assertFalse(g2.containsVertex(v4));
		assertFalse(g1.containsVertex(v19));
		assertFalse(g1.containsVertex(v20));
		assertFalse(g1.containsVertex(v21));
		assertFalse(g2.containsVertex(v1));
		assertFalse(g2.containsVertex(v2));
		assertFalse(g2.containsVertex(v3));
		assertFalse(g2.containsVertex(v4));
		assertFalse(g2.containsVertex(v5));
		assertFalse(g2.containsVertex(v6));
		assertFalse(g2.containsVertex(v7));
		assertFalse(g2.containsVertex(v8));
		assertFalse(g2.containsVertex(v9));
		assertFalse(g2.containsVertex(v10));
		assertFalse(g2.containsVertex(v11));
		assertFalse(g2.containsVertex(v12));

		// deleting vertices changes the contains-information accordingly
		g1.deleteVertex(v1);

		assertFalse(g1.containsVertex(v1));

		g1.deleteVertex(v9);

		assertFalse(g1.containsVertex(v9));

		g2.deleteVertex(v14);

		assertFalse(g2.containsVertex(v14));

	}

	@Test
	public void testContainsEdge() {
		DoubleSubNode v13 = g2.createDoubleSubNode();
		DoubleSubNode v14 = g2.createDoubleSubNode();
		SubNode v15 = g2.createSubNode();
		SubNode v16 = g2.createSubNode();
		SuperNode v17 = g2.createSuperNode();
		SuperNode v18 = g2.createSuperNode();

		Edge e1 = g1.createEdge(SubLink.EC, v9, v7);
		SubLink e2 = g2.createSubLink(v13, v17);
		Edge e3 = g1.createEdge(Link.EC, v10, v5);
		Link e4 = g2.createLink(v15, v17);
		Edge e5 = g1.createEdge(LinkBack.EC, v7, v1);
		LinkBack e6 = g2.createLinkBack(v17, v13);
		Edge e7 = g1.createEdge(SubLink.EC, v10, v5);
		Edge e8 = g1.createEdge(Link.EC, v3, v7);
		Edge e9 = g1.createEdge(LinkBack.EC, v5, v9);
		Edge e10 = g1.createEdge(SubLink.EC, v9, v5);
		Edge e11 = g2.createEdge(SubLink.EC, v14, v17);
		Edge e12 = g2.createEdge(Link.EC, v16, v18);
		Edge e13 = g2.createEdge(LinkBack.EC, v18, v13);

		assertTrue(g1.containsEdge(e1));
		assertTrue(g2.containsEdge(e2));
		assertTrue(g1.containsEdge(e3));
		assertTrue(g2.containsEdge(e4));
		assertTrue(g1.containsEdge(e5));
		assertTrue(g2.containsEdge(e6));
		assertTrue(g1.containsEdge(e7));
		assertTrue(g1.containsEdge(e8));
		assertTrue(g1.containsEdge(e9));
		assertTrue(g1.containsEdge(e10));
		assertTrue(g2.containsEdge(e11));
		assertTrue(g2.containsEdge(e12));
		assertTrue(g2.containsEdge(e13));

		assertFalse(g1.containsEdge(null));
		assertFalse(g2.containsEdge(null));
		assertFalse(g2.containsEdge(e1));
		assertFalse(g1.containsEdge(e2));
		assertFalse(g2.containsEdge(e3));
		assertFalse(g1.containsEdge(e4));
		assertFalse(g2.containsEdge(e5));
		assertFalse(g1.containsEdge(e6));
		assertFalse(g2.containsEdge(e7));
		assertFalse(g2.containsEdge(e8));
		assertFalse(g2.containsEdge(e9));
		assertFalse(g2.containsEdge(e10));
		assertFalse(g1.containsEdge(e11));
		assertFalse(g1.containsEdge(e12));
		assertFalse(g1.containsEdge(e13));

		// when a vertex is deleted, the edges to which it belonged are deleted
		// as well
		e1 = g1.createEdge(SubLink.EC, v10, v12);
		Edge e14 = g1.createEdge(SubLink.EC, v9, v6);
		Edge e17 = g1.createEdge(LinkBack.EC, v8, v10);

		assertTrue(g1.containsEdge(e1));
		assertTrue(g1.containsEdge(e17));

		g1.deleteVertex(v10);

		// all edges from or to v10 do no longer exist

		assertFalse(g1.containsEdge(e1));
		assertFalse(g1.containsEdge(e3));
		assertFalse(g1.containsEdge(e7));
		assertFalse(g1.containsEdge(e17));

		// all other edges do still exist
		assertTrue(g1.containsEdge(e9));
		assertTrue(g1.containsEdge(e10));
		assertTrue(g1.containsEdge(e5));
		assertTrue(g1.containsEdge(e8));
		assertTrue(g1.containsEdge(e14));

		Edge e15 = g1.createEdge(LinkBack.EC, v6, v11);
		Edge e16 = g1.createEdge(Link.EC, v12, v8);

		assertTrue(g1.containsEdge(e15));
		// assertTrue(g1.containsEdge(e16));

		g1.deleteEdge(e5);

		assertFalse(g1.containsEdge(e5));
		assertTrue(g1.containsEdge(e8));
		assertTrue(g1.containsEdge(e14));
		assertTrue(g1.containsEdge(e15));
		// assertTrue(g1.containsEdge(e16));

		g1.deleteEdge(e16);

		assertFalse(g1.containsEdge(e16));
		assertTrue(g1.containsEdge(e8));
		assertTrue(g1.containsEdge(e14));
		assertTrue(g1.containsEdge(e15));

		g1.deleteEdge(e14);

		assertFalse(g1.containsEdge(e14));
		assertTrue(g1.containsEdge(e8));
		assertTrue(g1.containsEdge(e15));

		g1.deleteEdge(e8);

		assertFalse(g1.containsEdge(e8));
		assertTrue(g1.containsEdge(e15));

		g1.deleteEdge(e15);

		assertFalse(g1.containsEdge(e15));

	}

	@Test
	public void testDeleteVertex() {
		// TODO:
		// Removes the vertex from the vertex sequence of this graph.
		// any edges incident to the vertex are deleted
		// If the vertex is the parent of a composition, all child vertices are
		// deleted.
		// Pre: v.isValid()
		// Post: !v.isValid() && !containsVertex(v) && getVertex(v.getId()) ==
		// null
		g1.deleteVertex(v1);

		assertFalse(g1.containsVertex(v1));
		g1.deleteVertex(v2);

		assertFalse(g1.containsVertex(v2));
		g1.deleteVertex(v3);

		assertFalse(g1.containsVertex(v3));

		g1.deleteVertex(v7);

		assertFalse(g1.containsVertex(v7));

		g1.deleteVertex(v5);

		assertFalse(g1.containsVertex(v5));

		g1.deleteVertex(v6);

		assertFalse(g1.containsVertex(v6));

		g1.deleteVertex(v9);

		assertFalse(g1.containsVertex(v9));

		g1.deleteVertex(v10);

		assertFalse(g1.containsVertex(v10));

		g1.deleteVertex(v11);

		assertFalse(g1.containsVertex(v11));

	}

	// private static interface VertexTestGraphTest {
	//
	// public List<Vertex> getDeletedVertices();
	//
	// public void resetDeletedVertices();
	//
	// public List<Vertex> getAddedVertices();
	//
	// public void resetAddedVertices();
	//
	// public List<Edge> getDeletedEdges();
	//
	// public void resetDeletedEdges();
	//
	// public List<Edge> getAddedEdges();
	//
	// public void resetAddedEdges();
	// }

	// private static class VertexTestGraphImplDelegator {
	// private final List<Vertex> deletedVertices;
	// private final List<Vertex> addedVertices;
	// private final List<Edge> deletedEdges;
	// private final List<Edge> addedEdges;
	// private final VertexTestGraph g;
	//
	// public VertexTestGraphImplDelegator(VertexTestGraph g) {
	// this.g = g;
	// deletedVertices = new ArrayList<Vertex>();
	// addedVertices = new ArrayList<Vertex>();
	// deletedEdges = new ArrayList<Edge>();
	// addedEdges = new ArrayList<Edge>();
	// }
	//
	// public void edgeAdded(Edge e) {
	// addedEdges.add(e);
	// // test if Edge has already been added at this point
	// assertTrue(g.containsEdge(e));
	// }
	//
	// public void edgeDeleted(Edge e) {
	// deletedEdges.add(e);
	// // test if Edge has not been deleted yet
	// assertTrue(g.containsEdge(e));
	// }
	//
	// public void vertexAdded(Vertex v) {
	// addedVertices.add(v);
	// // test if Vertex has already been added at this point
	// assertTrue(g.containsVertex(v));
	// }
	//
	// public void vertexDeleted(Vertex v) {
	// deletedVertices.add(v);
	// // test if Vertex has not been deleted yet
	// assertTrue(g.containsVertex(v));
	// }
	//
	// public List<Vertex> getDeletedVertices() {
	// return deletedVertices;
	// }
	//
	// public void resetDeletedVertices() {
	// deletedVertices.clear();
	// }
	//
	// public List<Vertex> getAddedVertices() {
	// return addedVertices;
	// }
	//
	// public void resetAddedVertices() {
	// addedVertices.clear();
	// }
	//
	// public List<Edge> getDeletedEdges() {
	// return deletedEdges;
	// }
	//
	// public void resetDeletedEdges() {
	// deletedEdges.clear();
	// }
	//
	// public List<Edge> getAddedEdges() {
	// return addedEdges;
	// }
	//
	// public void resetAddedEdges() {
	// addedEdges.clear();
	// }
	//
	// }

	// public static class VertexTestGraphImplTest
	// extends
	// de.uni_koblenz.jgralabtest.schemas.vertextest.impl.std.VertexTestGraphImpl
	// implements VertexTestGraphTest {
	//
	// private VertexTestGraphImplDelegator delegator;
	//
	// public VertexTestGraphImplTest() {
	// super();
	// createDelegator();
	// }
	//
	// public VertexTestGraphImplTest(int vMax, int eMax) {
	// super(vMax, eMax);
	// createDelegator();
	// }
	//
	// public VertexTestGraphImplTest(String id, int vMax, int eMax) {
	// super(id, vMax, eMax);
	// createDelegator();
	// }
	//
	// public VertexTestGraphImplTest(String id) {
	// super(id);
	// createDelegator();
	// }
	//
	// private void createDelegator() {
	// delegator = new VertexTestGraphImplDelegator(this);
	// }
	//
	// @Override
	// public void edgeAdded(Edge e) {
	// delegator.edgeAdded(e);
	// }
	//
	// @Override
	// public void edgeDeleted(Edge e) {
	// delegator.edgeDeleted(e);
	// }
	//
	// @Override
	// public void vertexAdded(Vertex v) {
	// delegator.vertexAdded(v);
	// }
	//
	// @Override
	// public void vertexDeleted(Vertex v) {
	// delegator.vertexDeleted(v);
	// }
	//
	// @Override
	// public List<Edge> getAddedEdges() {
	// return delegator.getAddedEdges();
	// }
	//
	// @Override
	// public List<Vertex> getAddedVertices() {
	// return delegator.getAddedVertices();
	// }
	//
	// @Override
	// public List<Edge> getDeletedEdges() {
	// return delegator.getDeletedEdges();
	// }
	//
	// @Override
	// public List<Vertex> getDeletedVertices() {
	// return delegator.getDeletedVertices();
	// }
	//
	// @Override
	// public void resetAddedEdges() {
	// delegator.resetAddedEdges();
	// }
	//
	// @Override
	// public void resetAddedVertices() {
	// delegator.resetAddedVertices();
	// }
	//
	// @Override
	// public void resetDeletedEdges() {
	// delegator.resetDeletedEdges();
	// }
	//
	// @Override
	// public void resetDeletedVertices() {
	// delegator.resetDeletedVertices();
	//
	// }
	//
	// }
	//
	// public static class VertexTestGraphImplTestWithTransactionSupport
	// extends
	// de.uni_koblenz.jgralabtest.schemas.vertextest.impl.trans.VertexTestGraphImpl
	// implements VertexTestGraphTest {
	//
	// private VertexTestGraphImplDelegator delegator;
	//
	// public VertexTestGraphImplTestWithTransactionSupport() {
	// super();
	// createDelegator();
	// }
	//
	// public VertexTestGraphImplTestWithTransactionSupport(int vMax, int eMax)
	// {
	// super(vMax, eMax);
	// createDelegator();
	// }
	//
	// public VertexTestGraphImplTestWithTransactionSupport(String id,
	// int vMax, int eMax) {
	// super(id, vMax, eMax);
	// createDelegator();
	// }
	//
	// public VertexTestGraphImplTestWithTransactionSupport(String id) {
	// super(id);
	// createDelegator();
	// }
	//
	// private void createDelegator() {
	// delegator = new VertexTestGraphImplDelegator(this);
	// }
	//
	// @Override
	// public void edgeAdded(Edge e) {
	// delegator.edgeAdded(e);
	// }
	//
	// @Override
	// public void edgeDeleted(Edge e) {
	// delegator.edgeDeleted(e);
	// }
	//
	// @Override
	// public void vertexAdded(Vertex v) {
	// delegator.vertexAdded(v);
	// }
	//
	// @Override
	// public void vertexDeleted(Vertex v) {
	// delegator.vertexDeleted(v);
	// }
	//
	// @Override
	// public List<Edge> getAddedEdges() {
	// return delegator.getAddedEdges();
	// }
	//
	// @Override
	// public List<Vertex> getAddedVertices() {
	// return delegator.getAddedVertices();
	// }
	//
	// @Override
	// public List<Edge> getDeletedEdges() {
	// return delegator.getDeletedEdges();
	// }
	//
	// @Override
	// public List<Vertex> getDeletedVertices() {
	// return delegator.getDeletedVertices();
	// }
	//
	// @Override
	// public void resetAddedEdges() {
	// delegator.resetAddedEdges();
	// }
	//
	// @Override
	// public void resetAddedVertices() {
	// delegator.resetAddedVertices();
	// }
	//
	// @Override
	// public void resetDeletedEdges() {
	// delegator.resetDeletedEdges();
	// }
	//
	// @Override
	// public void resetDeletedVertices() {
	// delegator.resetDeletedVertices();
	//
	// }
	// }

	// @Test
	// public void testVertexDeleted() {
	//
	// // set new class for GraphFactory
	// setTestGraphClasses();
	//
	// VertexTestGraph g = transactionsEnabled ? VertexTestSchema.instance()
	// .createVertexTestGraphWithTransactionSupport()
	// : VertexTestSchema.instance().createVertexTestGraph();
	// // create a simple graph
	// // SubNode v1 = g.createSubNode();
	// SuperNode v2 = g.createSuperNode();
	// DoubleSubNode v3 = g.createDoubleSubNode();
	// SuperNode v4 = g.createSuperNode();
	//
	// g.createLink(v1, v2);
	// g.createLinkBack(v2, v1);
	// g.createSubLink(v3, v4);
	// g.createLinkBack(v4, v1);
	// //
	// // reset list of deleted vertices
	// ((VertexTestGraphTest) g).resetDeletedVertices();
	//
	// // delete v1
	// // g.deleteVertex(v1);
	// //
	// // obtain list of deleted vertices
	// List<Vertex> deletedVertices = ((VertexTestGraphTest) g)
	// .getDeletedVertices();
	//
	// // test if vertexDeleted was invoked only once
	// assertEquals(1, deletedVertices.size());
	// // test if the correct vertex was affected
	// assertTrue(v1 == deletedVertices.get(0));
	// ((VertexTestGraphTest) g).resetDeletedVertices();
	//
	// // delete vertex v3 and check if method is also called for implicitly
	// // deleted vertex v4
	// // g.deleteVertex(v3);
	// //
	// // obtain list of affected graph elements
	// deletedVertices = ((VertexTestGraphTest) g).getDeletedVertices();
	//
	// // test if vertexDeleted was invoked twice
	// assertEquals(2, deletedVertices.size());
	// // test if the two correct vertices were affected and the methods were
	// // called in the correct order.
	// assertTrue(v3 == deletedVertices.get(0));
	// assertTrue(v4 == deletedVertices.get(1));
	//
	// // reset implementation class for graph factory
	// resetGraphClasses();
	//
	// }

	// private void resetGraphClasses() {
	// VertexTestSchema
	// .instance()
	// .getGraphFactory()
	// .setGraphImplementationClass(
	// VertexTestGraph.VC,
	// de.uni_koblenz.jgralabtest.schemas.vertextest.impl.std.VertexTestGraphImpl.VC);
	// VertexTestSchema
	// .instance()
	// .getGraphFactory()
	// .setGraphTransactionImplementationClass(
	// VertexTestGraph.VC,
	// de.uni_koblenz.jgralabtest.schemas.vertextest.impl.trans.VertexTestGraphImpl.VC);
	//
	// }

	// private void setTestGraphClasses() {
	// VertexTestSchema.instance().getGraphFactory()
	// .setGraphImplementationClass(VertexTestGraph.VC,
	// VertexTestGraphImplTest.VC);
	// VertexTestSchema.instance().getGraphFactory()
	// .setGraphTransactionImplementationClass(VertexTestGraph.VC,
	// VertexTestGraphImplTestWithTransactionSupport.VC);
	// }

	// @Test
	// public void testVertexAdded() {
	// setTestGraphClasses();
	//
	// VertexTestGraph g = transactionsEnabled ? VertexTestSchema.instance()
	// .createVertexTestGraphWithTransactionSupport()
	// : VertexTestSchema.instance().createVertexTestGraph();
	//
	// ((VertexTestGraphTest) g).resetAddedVertices();
	//
	// // create a vertex
	// // SubNode v1 = g.createSubNode();
	// //
	// // obtain list of added vertices
	// List<Vertex> addedVertices = ((VertexTestGraphTest) g)
	// .getAddedVertices();
	//
	// // test if method was invoked exactly once
	// assertEquals(1, addedVertices.size());
	// // test if the correct vertex was affected
	// assertTrue(v1 == addedVertices.get(0));
	//
	// resetGraphClasses();
	//
	// }

	@Test
	public void testDeleteEdge() {
		/*
		 * TODO apparently one cannot delete ALL edges of the same type (here:
		 * SubLink) after deleting a vertex?
		 */
		// TODO faults => assertions
		Link e1 = g1.createEdge(Link.EC, v1, v6);
		Link e2 = g1.createEdge(Link.EC, v11, v5);
		Link e3 = g1.createEdge(Link.EC, v2, v8);
		SubLink e4 = g1.createEdge(SubLink.EC, v11, v6);
		SubLink e5 = g1.createEdge(SubLink.EC, v12, v5);
		LinkBack e6 = g1.createEdge(LinkBack.EC, v6, v11);
		LinkBack e7 = g1.createEdge(LinkBack.EC, v5, v2);
		LinkBack e8 = g1.createEdge(LinkBack.EC, v8, v1);
		LinkBack e9 = g1.createEdge(LinkBack.EC, v5, v10);
		Link e10 = g1.createEdge(Link.EC, v12, v7);
		SubLink e11 = g1.createEdge(SubLink.EC, v10, v6);
		SubLink e12 = g1.createEdge(SubLink.EC, v9, v7);

		// SubLink e10 = graph2.createEdge(SubLink.VC, v9,
		// v6);
		int id = e12.getId();

		g1.deleteEdge(e12);

		assertFalse(e12.isValid());
		assertFalse(g1.containsEdge(e12));
		assertNull(g1.getEdge(id));

		id = e1.getId();

		g1.deleteEdge(e1);

		assertFalse(e1.isValid());
		assertFalse(g1.containsEdge(e1));
		assertNull(g1.getEdge(id));

		id = e2.getId();

		g1.deleteEdge(e2);

		assertFalse(e2.isValid());
		assertFalse(g1.containsEdge(e2));
		assertNull(g1.getEdge(id));

		id = e7.getId();

		g1.deleteEdge(e7);

		assertFalse(e7.isValid());
		assertFalse(g1.containsEdge(e7));
		assertNull(g1.getEdge(id));

		id = e3.getId();

		g1.deleteEdge(e3);

		assertFalse(e3.isValid());
		assertFalse(g1.containsEdge(e3));
		assertNull(g1.getEdge(id));

		id = e9.getId();

		g1.deleteEdge(e9);

		assertFalse(e9.isValid());
		assertFalse(g1.containsEdge(e9));
		assertNull(g1.getEdge(id));

		id = e4.getId();

		g1.deleteEdge(e4);

		assertFalse(e4.isValid());
		assertFalse(g1.containsEdge(e4));
		assertNull(g1.getEdge(id));

		id = e10.getId();

		g1.deleteEdge(e10);

		assertFalse(e10.isValid());
		assertFalse(g1.containsEdge(e10));
		assertNull(g1.getEdge(id));

		id = e5.getId();

		g1.deleteEdge(e5);

		assertFalse(e5.isValid());
		assertFalse(g1.containsEdge(e5));
		assertNull(g1.getEdge(id));

		id = e8.getId();

		g1.deleteEdge(e8);

		assertFalse(e8.isValid());
		assertFalse(g1.containsEdge(e8));
		assertNull(g1.getEdge(id));

		id = e11.getId();

		g1.deleteEdge(e11);

		assertFalse(e11.isValid());
		assertFalse(g1.containsEdge(e11));
		assertNull(g1.getEdge(id));

		id = e6.getId();

		g1.deleteEdge(e6);

		assertFalse(e6.isValid());
		assertFalse(g1.containsEdge(e6));
		assertNull(g1.getEdge(id));
		// border cases

		// faults
		// TODO
		// cannot try to delete an edge which has never been created?
		// graph.deleteEdge(e10);

	}

	// @Test
	// public void testEdgeDeleted() {
	// // set new class for GraphFactory
	// setTestGraphClasses();
	//
	// VertexTestGraph g = transactionsEnabled ? VertexTestSchema.instance()
	// .createVertexTestGraphWithTransactionSupport()
	// : VertexTestSchema.instance().createVertexTestGraph();
	// // create a simple graph
	// // SubNode v1 = g.createSubNode();
	// SuperNode v2 = g.createSuperNode();
	//
	// Link e1 = g.createLink(v1, v2);
	//
	// //
	// // reset list of deleted edges
	// ((VertexTestGraphTest) g).resetDeletedEdges();
	//
	// // delete e1
	// // g.deleteEdge(e1);
	// //
	// // obtain list of deleted edges
	// List<Edge> deletedEdges = ((VertexTestGraphTest) g).getDeletedEdges();
	//
	// // test if edgeDeleted was invoked only once
	// assertEquals(1, deletedEdges.size());
	// // test if the correct vertex was affected
	// assertTrue(e1 == deletedEdges.get(0));
	// ((VertexTestGraphTest) g).resetDeletedVertices();
	//
	// // reset implementation class for graph factory
	// resetGraphClasses();
	//
	// }

	// @Test
	// public void testEdgeAdded() {
	// setTestGraphClasses();
	//
	// VertexTestGraph g = transactionsEnabled ? VertexTestSchema.instance()
	// .createVertexTestGraphWithTransactionSupport()
	// : VertexTestSchema.instance().createVertexTestGraph();
	//
	// ((VertexTestGraphTest) g).resetAddedEdges();
	//
	// // create a simple graph
	// // SubNode v1 = g.createSubNode();
	// SuperNode v2 = g.createSuperNode();
	//
	// Link e1 = g.createLink(v1, v2);
	//
	// //
	// // obtain list of added edges
	// List<Edge> addedEdges = ((VertexTestGraphTest) g).getAddedEdges();
	//
	// // test if method was invoked exactly once
	// assertEquals(1, addedEdges.size());
	// // test if the correct vertex was affected
	// assertTrue(e1 == addedEdges.get(0));
	//
	// resetGraphClasses();
	// }

	@Test
	public void testGetFirstVertex() {
		assertEquals(v1, g1.getFirstVertex());
		assertNull(g2.getFirstVertex());

		SubNode v13 = g2.createSubNode();
		g2.createSuperNode();

		assertEquals(v13, g2.getFirstVertex());

		g2.createDoubleSubNode();
		g2.createSubNode();

		assertEquals(v13, g2.getFirstVertex());

		g1.createDoubleSubNode();

		assertEquals(v1, g1.getFirstVertex());

	}

	@Test
	public void testGetLastVertex() {

		// border cases
		assertEquals(v12, g1.getLastVertex());

		assertNull(g2.getLastVertex());

		Vertex v13 = g1.createVertex(SubNode.VC);

		assertEquals(v13, g1.getLastVertex());

		// normal cases
		Vertex v14 = g1.createVertex(SubNode.VC);

		assertEquals(v14, g1.getLastVertex());

		Vertex v15 = g1.createVertex(SubNode.VC);

		assertEquals(v15, g1.getLastVertex());

		Vertex v16 = g1.createVertex(SubNode.VC);

		assertEquals(v16, g1.getLastVertex());

		Vertex v17 = g1.createVertex(SuperNode.VC);

		assertEquals(v17, g1.getLastVertex());

		Vertex v18 = g1.createVertex(SuperNode.VC);

		assertEquals(v18, g1.getLastVertex());

		Vertex v19 = g1.createVertex(SuperNode.VC);

		assertEquals(v19, g1.getLastVertex());

		Vertex v20 = g1.createVertex(SuperNode.VC);

		assertEquals(v20, g1.getLastVertex());

		Vertex v21 = g1.createVertex(DoubleSubNode.VC);

		assertEquals(v21, g1.getLastVertex());

		Vertex v22 = g1.createVertex(DoubleSubNode.VC);

		assertEquals(v22, g1.getLastVertex());

		Vertex v23 = g1.createVertex(DoubleSubNode.VC);

		assertEquals(v23, g1.getLastVertex());

		Vertex v24 = g1.createVertex(DoubleSubNode.VC);

		assertEquals(v24, g1.getLastVertex());

	}

	@Test
	public void testGetFirstVertexOfClass() {

		assertNull(g2.getFirstVertex(SubNode.VC));
		assertNull(g2.getFirstVertex(SuperNode.VC));
		assertNull(g2.getFirstVertex(DoubleSubNode.VC));

		Vertex v13 = g2.createVertex(SubNode.VC);

		assertEquals(v13, g2.getFirstVertex(SubNode.VC));
		assertNull(g2.getFirstVertex(SuperNode.VC));
		assertNull(g2.getFirstVertex(DoubleSubNode.VC));

		Vertex v14 = g2.createVertex(SuperNode.VC);

		assertEquals(v13, g2.getFirstVertex(SubNode.VC));
		assertEquals(v14, g2.getFirstVertex(SuperNode.VC));
		assertNull(g2.getFirstVertex(DoubleSubNode.VC));

		Vertex v15 = g2.createVertex(SubNode.VC);

		assertEquals(v13, g2.getFirstVertex(SubNode.VC));
		assertEquals(v14, g2.getFirstVertex(SuperNode.VC));
		assertNull(g2.getFirstVertex(DoubleSubNode.VC));

		Vertex v16 = g2.createVertex(SubNode.VC);

		assertEquals(v13, g2.getFirstVertex(SubNode.VC));
		assertEquals(v14, g2.getFirstVertex(SuperNode.VC));
		assertNull(g2.getFirstVertex(DoubleSubNode.VC));

		Vertex v17 = g2.createVertex(DoubleSubNode.VC);

		assertEquals(v13, g2.getFirstVertex(SubNode.VC));
		assertEquals(v14, g2.getFirstVertex(SuperNode.VC));
		assertEquals(v17, g2.getFirstVertex(DoubleSubNode.VC));

		Vertex v18 = g2.createVertex(SuperNode.VC);

		assertEquals(v13, g2.getFirstVertex(SubNode.VC));
		assertEquals(v14, g2.getFirstVertex(SuperNode.VC));
		assertEquals(v17, g2.getFirstVertex(DoubleSubNode.VC));

		Vertex v19 = g2.createVertex(SubNode.VC);

		assertEquals(v13, g2.getFirstVertex(SubNode.VC));
		assertEquals(v14, g2.getFirstVertex(SuperNode.VC));
		assertEquals(v17, g2.getFirstVertex(DoubleSubNode.VC));

		Vertex v20 = g2.createVertex(DoubleSubNode.VC);

		assertEquals(v13, g2.getFirstVertex(SubNode.VC));
		assertEquals(v14, g2.getFirstVertex(SuperNode.VC));
		assertEquals(v17, g2.getFirstVertex(DoubleSubNode.VC));

		g2.deleteVertex(v14);

		assertEquals(v13, g2.getFirstVertex(SubNode.VC));
		assertEquals(v17, g2.getFirstVertex(SuperNode.VC));
		assertEquals(v17, g2.getFirstVertex(DoubleSubNode.VC));

		Vertex v21 = g2.createVertex(DoubleSubNode.VC);

		assertEquals(v13, g2.getFirstVertex(SubNode.VC));
		assertEquals(v17, g2.getFirstVertex(SuperNode.VC));
		assertEquals(v17, g2.getFirstVertex(DoubleSubNode.VC));

		g2.deleteVertex(v16);

		assertEquals(v13, g2.getFirstVertex(SubNode.VC));
		assertEquals(v17, g2.getFirstVertex(SuperNode.VC));
		assertEquals(v17, g2.getFirstVertex(DoubleSubNode.VC));

		g2.createVertex(SuperNode.VC);

		assertEquals(v13, g2.getFirstVertex(SubNode.VC));
		assertEquals(v17, g2.getFirstVertex(SuperNode.VC));
		assertEquals(v17, g2.getFirstVertex(DoubleSubNode.VC));

		g2.createVertex(DoubleSubNode.VC);

		assertEquals(v13, g2.getFirstVertex(SubNode.VC));
		assertEquals(v17, g2.getFirstVertex(SuperNode.VC));
		assertEquals(v17, g2.getFirstVertex(DoubleSubNode.VC));

		g2.deleteVertex(v13);

		assertEquals(v15, g2.getFirstVertex(SubNode.VC));
		assertEquals(v17, g2.getFirstVertex(SuperNode.VC));
		assertEquals(v17, g2.getFirstVertex(DoubleSubNode.VC));

		g2.deleteVertex(v17);

		assertEquals(v15, g2.getFirstVertex(SubNode.VC));
		assertEquals(v18, g2.getFirstVertex(SuperNode.VC));
		assertEquals(v20, g2.getFirstVertex(DoubleSubNode.VC));

		g2.deleteVertex(v20);

		assertEquals(v15, g2.getFirstVertex(SubNode.VC));
		assertEquals(v18, g2.getFirstVertex(SuperNode.VC));
		assertEquals(v21, g2.getFirstVertex(DoubleSubNode.VC));

		g2.createVertex(SuperNode.VC);

		assertEquals(v15, g2.getFirstVertex(SubNode.VC));
		assertEquals(v18, g2.getFirstVertex(SuperNode.VC));
		assertEquals(v21, g2.getFirstVertex(DoubleSubNode.VC));

		g2.deleteVertex(v15);

		assertEquals(v19, g2.getFirstVertex(SubNode.VC));
		assertEquals(v18, g2.getFirstVertex(SuperNode.VC));
		assertEquals(v21, g2.getFirstVertex(DoubleSubNode.VC));

	}

	@Test
	public void testGetFirstVertexOfClass3() {
		// preparations
		getVertexClassesOfG1();

		assertNull(g2.getFirstVertex(subN));
		assertNull(g2.getFirstVertex(superN));
		assertNull(g2.getFirstVertex(doubleSubN));

		SuperNode v13 = g2.createSuperNode();

		assertNull(g2.getFirstVertex(subN));
		assertEquals(v13, g2.getFirstVertex(superN));
		assertNull(g2.getFirstVertex(doubleSubN));

		SubNode v14 = g2.createSubNode();
		assertEquals(v14, g2.getFirstVertex(subN));
		assertEquals(v13, g2.getFirstVertex(superN));
		assertNull(g2.getFirstVertex(doubleSubN));

		SuperNode v15 = g2.createSuperNode();

		assertEquals(v14, g2.getFirstVertex(subN));
		assertEquals(v13, g2.getFirstVertex(superN));
		assertNull(g2.getFirstVertex(doubleSubN));

		DoubleSubNode v16 = g2.createDoubleSubNode();

		assertEquals(v14, g2.getFirstVertex(subN));
		assertEquals(v13, g2.getFirstVertex(superN));
		assertEquals(v16, g2.getFirstVertex(doubleSubN));

		g2.deleteVertex(v13);

		assertEquals(v14, g2.getFirstVertex(subN));
		assertEquals(v15, g2.getFirstVertex(superN));
		assertEquals(v16, g2.getFirstVertex(doubleSubN));

		DoubleSubNode v17 = g2.createDoubleSubNode();

		assertEquals(v14, g2.getFirstVertex(subN));
		assertEquals(v15, g2.getFirstVertex(superN));
		assertEquals(v16, g2.getFirstVertex(doubleSubN));

		SubNode v18 = g2.createSubNode();

		assertEquals(v14, g2.getFirstVertex(subN));
		assertEquals(v15, g2.getFirstVertex(superN));
		assertEquals(v16, g2.getFirstVertex(doubleSubN));

		g2.deleteVertex(v14);

		assertEquals(v16, g2.getFirstVertex(subN));
		assertEquals(v15, g2.getFirstVertex(superN));
		assertEquals(v16, g2.getFirstVertex(doubleSubN));

		g2.deleteVertex(v16);

		assertEquals(v17, g2.getFirstVertex(subN));
		assertEquals(v15, g2.getFirstVertex(superN));
		assertEquals(v17, g2.getFirstVertex(doubleSubN));

		g2.deleteVertex(v17);

		assertEquals(v18, g2.getFirstVertex(subN));
		assertEquals(v15, g2.getFirstVertex(superN));
		assertNull(g2.getFirstVertex(doubleSubN));

		DoubleSubNode v19 = g2.createDoubleSubNode();

		assertEquals(v18, g2.getFirstVertex(subN));
		assertEquals(v15, g2.getFirstVertex(superN));
		assertEquals(v19, g2.getFirstVertex(doubleSubN));

		g2.deleteVertex(v18);

		assertEquals(v19, g2.getFirstVertex(subN));
		assertEquals(v15, g2.getFirstVertex(superN));
		assertEquals(v19, g2.getFirstVertex(doubleSubN));

		SubNode v20 = g2.createSubNode();

		assertEquals(v19, g2.getFirstVertex(subN));
		assertEquals(v15, g2.getFirstVertex(superN));
		assertEquals(v19, g2.getFirstVertex(doubleSubN));

		SuperNode v21 = g2.createSuperNode();

		assertEquals(v19, g2.getFirstVertex(subN));
		assertEquals(v15, g2.getFirstVertex(superN));
		assertEquals(v19, g2.getFirstVertex(doubleSubN));

		g2.deleteVertex(v19);

		assertEquals(v20, g2.getFirstVertex(subN));
		assertEquals(v15, g2.getFirstVertex(superN));
		assertNull(g2.getFirstVertex(doubleSubN));

		g2.deleteVertex(v20);

		assertNull(g2.getFirstVertex(subN));
		assertEquals(v15, g2.getFirstVertex(superN));
		assertNull(g2.getFirstVertex(doubleSubN));

		SubNode v22 = g2.createSubNode();

		assertEquals(v22, g2.getFirstVertex(subN));
		assertEquals(v15, g2.getFirstVertex(superN));
		assertNull(g2.getFirstVertex(doubleSubN));

		g2.deleteVertex(v15);

		assertEquals(v22, g2.getFirstVertex(subN));
		assertEquals(v21, g2.getFirstVertex(superN));
		assertNull(g2.getFirstVertex(doubleSubN));

	}

	@Test
	public void testGetFirstEdgeInGraph() {
		Vertex v13 = g1.createVertex(SuperNode.VC);

		assertNull(g1.getFirstEdge());

		Edge e1 = g1.createEdge(Link.EC, v3, v6);

		assertEquals(e1, g1.getFirstEdge());

		Edge e2 = g1.createEdge(Link.EC, v3, v13);

		assertEquals(e1, g1.getFirstEdge());

		Edge e3 = g1.createEdge(LinkBack.EC, v7, v11);

		assertEquals(e1, g1.getFirstEdge());

		g1.deleteEdge(e1);

		assertEquals(e2, g1.getFirstEdge());

		g1.deleteEdge(e2);

		assertEquals(e3, g1.getFirstEdge());

		Edge e4 = g1.createEdge(SubLink.EC, v10, v8);

		assertEquals(e3, g1.getFirstEdge());

		g1.deleteEdge(e3);

		assertEquals(e4, g1.getFirstEdge());

		Edge e5 = g1.createEdge(LinkBack.EC, v8, v3);

		assertEquals(e4, g1.getFirstEdge());

		Edge e6 = g1.createEdge(Link.EC, v2, v5);

		assertEquals(e4, g1.getFirstEdge());

		g1.deleteEdge(e4);

		assertEquals(e5, g1.getFirstEdge());

		Edge e7 = g1.createEdge(LinkBack.EC, v13, v1);

		assertEquals(e5, g1.getFirstEdge());

		g1.deleteEdge(e5);

		assertEquals(e6, g1.getFirstEdge());

		Edge e8 = g1.createEdge(SubLink.EC, v9, v6);

		assertEquals(e6, g1.getFirstEdge());

		g1.deleteEdge(e7);

		assertEquals(e6, g1.getFirstEdge());

		g1.deleteEdge(e6);

		assertEquals(e8, g1.getFirstEdge());

		g1.deleteEdge(e8);

		assertNull(g1.getFirstEdge());

	}

	@Test
	public void testGetLastEdgeInGraph() {
		Vertex v13 = g1.createVertex(SuperNode.VC);

		assertNull(g1.getLastEdge());

		Edge e1 = g1.createEdge(Link.EC, v3, v6);

		assertEquals(e1, g1.getLastEdge());

		Edge e2 = g1.createEdge(Link.EC, v3, v13);

		assertEquals(e2, g1.getLastEdge());

		Edge e3 = g1.createEdge(LinkBack.EC, v7, v11);

		assertEquals(e3, g1.getLastEdge());

		g1.deleteEdge(e3);

		assertEquals(e2, g1.getLastEdge());

		Edge e4 = g1.createEdge(SubLink.EC, v10, v8);

		assertEquals(e4, g1.getLastEdge());

		Edge e5 = g1.createEdge(LinkBack.EC, v8, v3);

		assertEquals(e5, g1.getLastEdge());

		Edge e6 = g1.createEdge(Link.EC, v9, v5);

		assertEquals(e6, g1.getLastEdge());

		Edge e7 = g1.createEdge(SubLink.EC, v11, v7);

		assertEquals(e7, g1.getLastEdge());

		g1.deleteEdge(e7);

		assertEquals(e6, g1.getLastEdge());

		g1.deleteEdge(e6);

		assertEquals(e5, g1.getLastEdge());

		Edge e8 = g1.createEdge(Link.EC, v1, v13);

		assertEquals(e8, g1.getLastEdge());

		g1.deleteEdge(e5);

		assertEquals(e8, g1.getLastEdge());

		Edge e9 = g1.createEdge(LinkBack.EC, v6, v2);

		assertEquals(e9, g1.getLastEdge());

		g1.deleteEdge(e9);

		assertEquals(e8, g1.getLastEdge());

		g1.deleteEdge(e8);

		assertEquals(e4, g1.getLastEdge());

		g1.deleteEdge(e4);

		assertEquals(e2, g1.getLastEdge());

		g1.deleteEdge(e2);

		assertEquals(e1, g1.getLastEdge());

		g1.deleteEdge(e1);

		assertNull(g1.getLastEdge());

	}

	@Test
	public void testGetFirstEdgeOfClassInGraph() {
		Vertex v13 = g1.createVertex(SuperNode.VC);

		assertNull(g1.getFirstEdge(Link.EC));
		assertNull(g1.getFirstEdge(SubLink.EC));
		assertNull(g1.getFirstEdge(LinkBack.EC));

		Edge e1 = g1.createEdge(Link.EC, v3, v6);

		assertEquals(e1, g1.getFirstEdge(Link.EC));
		assertNull(g1.getFirstEdge(SubLink.EC));
		assertNull(g1.getFirstEdge(LinkBack.EC));

		Edge e2 = g1.createEdge(Link.EC, v3, v13);

		assertEquals(e1, g1.getFirstEdge(Link.EC));
		assertNull(g1.getFirstEdge(SubLink.EC));
		assertNull(g1.getFirstEdge(LinkBack.EC));

		Edge e3 = g1.createEdge(LinkBack.EC, v7, v11);

		assertEquals(e1, g1.getFirstEdge(Link.EC));
		assertNull(g1.getFirstEdge(SubLink.EC));
		assertEquals(e3, g1.getFirstEdge(LinkBack.EC));

		g1.deleteEdge(e1);

		assertEquals(e2, g1.getFirstEdge(Link.EC));
		assertNull(g1.getFirstEdge(SubLink.EC));
		assertEquals(e3, g1.getFirstEdge(LinkBack.EC));

		Edge e4 = g1.createEdge(SubLink.EC, v10, v8);

		assertEquals(e2, g1.getFirstEdge(Link.EC));
		assertEquals(e4, g1.getFirstEdge(SubLink.EC));
		assertEquals(e3, g1.getFirstEdge(LinkBack.EC));

		Edge e5 = g1.createEdge(LinkBack.EC, v8, v3);

		assertEquals(e2, g1.getFirstEdge(Link.EC));
		assertEquals(e4, g1.getFirstEdge(SubLink.EC));
		assertEquals(e3, g1.getFirstEdge(LinkBack.EC));

		g1.deleteEdge(e3);

		assertEquals(e2, g1.getFirstEdge(Link.EC));
		assertEquals(e4, g1.getFirstEdge(SubLink.EC));
		assertEquals(e5, g1.getFirstEdge(LinkBack.EC));

		Edge e6 = g1.createEdge(Link.EC, v9, v5);

		assertEquals(e2, g1.getFirstEdge(Link.EC));
		assertEquals(e4, g1.getFirstEdge(SubLink.EC));
		assertEquals(e5, g1.getFirstEdge(LinkBack.EC));

		g1.deleteEdge(e2);

		assertEquals(e4, g1.getFirstEdge(Link.EC));
		assertEquals(e4, g1.getFirstEdge(SubLink.EC));
		assertEquals(e5, g1.getFirstEdge(LinkBack.EC));

		Edge e7 = g1.createEdge(SubLink.EC, v11, v7);

		assertEquals(e4, g1.getFirstEdge(Link.EC));
		assertEquals(e4, g1.getFirstEdge(SubLink.EC));
		assertEquals(e5, g1.getFirstEdge(LinkBack.EC));

		g1.deleteEdge(e4);

		assertEquals(e6, g1.getFirstEdge(Link.EC));
		assertEquals(e7, g1.getFirstEdge(SubLink.EC));
		assertEquals(e5, g1.getFirstEdge(LinkBack.EC));

		Edge e8 = g1.createEdge(SubLink.EC, v10, v13);

		assertEquals(e6, g1.getFirstEdge(Link.EC));
		assertEquals(e7, g1.getFirstEdge(SubLink.EC));
		assertEquals(e5, g1.getFirstEdge(LinkBack.EC));

		Edge e9 = g1.createEdge(LinkBack.EC, v6, v1);

		assertEquals(e6, g1.getFirstEdge(Link.EC));
		assertEquals(e7, g1.getFirstEdge(SubLink.EC));
		assertEquals(e5, g1.getFirstEdge(LinkBack.EC));

		g1.deleteEdge(e5);

		assertEquals(e6, g1.getFirstEdge(Link.EC));
		assertEquals(e7, g1.getFirstEdge(SubLink.EC));
		assertEquals(e9, g1.getFirstEdge(LinkBack.EC));

		g1.deleteEdge(e7);

		assertEquals(e6, g1.getFirstEdge(Link.EC));
		assertEquals(e8, g1.getFirstEdge(SubLink.EC));
		assertEquals(e9, g1.getFirstEdge(LinkBack.EC));

		g1.deleteEdge(e6);

		assertEquals(e8, g1.getFirstEdge(Link.EC));
		assertEquals(e8, g1.getFirstEdge(SubLink.EC));
		assertEquals(e9, g1.getFirstEdge(LinkBack.EC));

		Edge e10 = g1.createEdge(Link.EC, v2, v7);

		assertEquals(e8, g1.getFirstEdge(Link.EC));
		assertEquals(e8, g1.getFirstEdge(SubLink.EC));
		assertEquals(e9, g1.getFirstEdge(LinkBack.EC));

		g1.deleteEdge(e8);

		assertEquals(e10, g1.getFirstEdge(Link.EC));
		assertNull(g1.getFirstEdge(SubLink.EC));
		assertEquals(e9, g1.getFirstEdge(LinkBack.EC));

		g1.deleteEdge(e10);

		assertNull(g1.getFirstEdge(Link.EC));
		assertNull(g1.getFirstEdge(SubLink.EC));
		assertEquals(e9, g1.getFirstEdge(LinkBack.EC));

		g1.deleteEdge(e9);

		assertNull(g1.getFirstEdge(Link.EC));
		assertNull(g1.getFirstEdge(SubLink.EC));
		assertNull(g1.getFirstEdge(LinkBack.EC));

	}

	@Test
	public void testGetFirstEdgeOfClassInGraph3() {
		// preparations
		getEdgeClassesOfG1();

		Vertex v13 = g1.createVertex(SuperNode.VC);

		// start tests
		assertNull(g1.getFirstEdge(link));
		assertNull(g1.getFirstEdge(subL));
		assertNull(g1.getFirstEdge(lBack));

		Edge e1 = g1.createEdge(SubLink.EC, v9, v6);

		assertEquals(e1, g1.getFirstEdge(link));
		assertEquals(e1, g1.getFirstEdge(subL));
		assertNull(g1.getFirstEdge(lBack));

		Edge e2 = g1.createEdge(Link.EC, v3, v13);

		assertEquals(e1, g1.getFirstEdge(link));
		assertEquals(e1, g1.getFirstEdge(subL));
		assertNull(g1.getFirstEdge(lBack));

		Edge e3 = g1.createEdge(LinkBack.EC, v7, v11);

		assertEquals(e1, g1.getFirstEdge(link));
		assertEquals(e1, g1.getFirstEdge(subL));
		assertEquals(e3, g1.getFirstEdge(lBack));

		g1.deleteEdge(e1);

		assertEquals(e2, g1.getFirstEdge(link));
		assertNull(g1.getFirstEdge(subL));
		assertEquals(e3, g1.getFirstEdge(lBack));

		Edge e4 = g1.createEdge(SubLink.EC, v10, v8);

		assertEquals(e2, g1.getFirstEdge(link));
		assertEquals(e4, g1.getFirstEdge(subL));
		assertEquals(e3, g1.getFirstEdge(lBack));

		g1.deleteEdge(e3);

		assertEquals(e2, g1.getFirstEdge(link));
		assertEquals(e4, g1.getFirstEdge(subL));
		assertNull(g1.getFirstEdge(lBack));

		Edge e5 = g1.createEdge(LinkBack.EC, v8, v3);

		assertEquals(e2, g1.getFirstEdge(link));
		assertEquals(e4, g1.getFirstEdge(subL));
		assertEquals(e5, g1.getFirstEdge(lBack));

		g1.deleteEdge(e2);

		assertEquals(e4, g1.getFirstEdge(link));
		assertEquals(e4, g1.getFirstEdge(subL));
		assertEquals(e5, g1.getFirstEdge(lBack));

		Edge e6 = g1.createEdge(Link.EC, v9, v5);

		assertEquals(e4, g1.getFirstEdge(link));
		assertEquals(e4, g1.getFirstEdge(subL));
		assertEquals(e5, g1.getFirstEdge(lBack));

		g1.deleteEdge(e6);

		assertEquals(e4, g1.getFirstEdge(link));
		assertEquals(e4, g1.getFirstEdge(subL));
		assertEquals(e5, g1.getFirstEdge(lBack));

		g1.deleteEdge(e4);

		assertNull(g1.getFirstEdge(link));
		assertNull(g1.getFirstEdge(subL));
		assertEquals(e5, g1.getFirstEdge(lBack));

		Edge e7 = g1.createEdge(SubLink.EC, v11, v7);

		assertEquals(e7, g1.getFirstEdge(link));
		assertEquals(e7, g1.getFirstEdge(subL));
		assertEquals(e5, g1.getFirstEdge(lBack));

		Edge e8 = g1.createEdge(SubLink.EC, v10, v13);

		assertEquals(e7, g1.getFirstEdge(link));
		assertEquals(e7, g1.getFirstEdge(subL));
		assertEquals(e5, g1.getFirstEdge(lBack));

		g1.deleteEdge(e5);

		assertEquals(e7, g1.getFirstEdge(link));
		assertEquals(e7, g1.getFirstEdge(subL));
		assertNull(g1.getFirstEdge(lBack));

		Edge e9 = g1.createEdge(LinkBack.EC, v6, v1);

		assertEquals(e7, g1.getFirstEdge(link));
		assertEquals(e7, g1.getFirstEdge(subL));
		assertEquals(e9, g1.getFirstEdge(lBack));

		Edge e10 = g1.createEdge(Link.EC, v2, v7);

		assertEquals(e7, g1.getFirstEdge(link));
		assertEquals(e7, g1.getFirstEdge(subL));
		assertEquals(e9, g1.getFirstEdge(lBack));

		g1.deleteEdge(e7);

		assertEquals(e8, g1.getFirstEdge(link));
		assertEquals(e8, g1.getFirstEdge(subL));
		assertEquals(e9, g1.getFirstEdge(lBack));

		g1.deleteEdge(e9);

		assertEquals(e8, g1.getFirstEdge(link));
		assertEquals(e8, g1.getFirstEdge(subL));
		assertNull(g1.getFirstEdge(lBack));

		g1.deleteEdge(e8);

		assertEquals(e10, g1.getFirstEdge(link));
		assertNull(g1.getFirstEdge(subL));
		assertNull(g1.getFirstEdge(lBack));

		g1.deleteEdge(e10);

		assertNull(g1.getFirstEdge(link));
		assertNull(g1.getFirstEdge(subL));
		assertNull(g1.getFirstEdge(lBack));

	}

	@Test
	public void testGetVertex() {

		Vertex v13 = g1.createVertex(SubNode.VC);
		Vertex v14 = g1.createVertex(DoubleSubNode.VC);
		Vertex v15 = g1.createVertex(SuperNode.VC);

		Vertex v16 = g2.createVertex(SubNode.VC);
		Vertex v17 = g2.createVertex(SuperNode.VC);
		Vertex v18 = g2.createVertex(DoubleSubNode.VC);

		// border cases
		assertEquals(v1, g1.getVertex(1));
		assertEquals(v16, g2.getVertex(1));
		assertNull(g1.getVertex(42));
		assertNull(g1.getVertex(33));
		assertNull(g2.getVertex(4));
		// 1000 is the highest possible value
		assertNull(g1.getVertex(1000));

		// normal cases
		assertEquals(v2, g1.getVertex(2));
		assertEquals(v3, g1.getVertex(3));
		assertEquals(v4, g1.getVertex(4));
		assertEquals(v5, g1.getVertex(5));
		assertEquals(v6, g1.getVertex(6));
		assertEquals(v7, g1.getVertex(7));
		assertEquals(v8, g1.getVertex(8));
		assertEquals(v9, g1.getVertex(9));
		assertEquals(v10, g1.getVertex(10));
		assertEquals(v11, g1.getVertex(11));
		assertEquals(v12, g1.getVertex(12));
		assertEquals(v13, g1.getVertex(13));
		assertEquals(v14, g1.getVertex(14));
		assertEquals(v15, g1.getVertex(15));
		assertEquals(v17, g2.getVertex(2));
		assertEquals(v18, g2.getVertex(3));

	}

	@Test
	public void testGetEdge() {
		Edge e1 = g1.createEdge(LinkBack.EC, v5, v1);
		Edge e2 = g1.createEdge(Link.EC, v2, v7);
		Edge e3 = g1.createEdge(LinkBack.EC, v8, v4);
		Edge e4 = g1.createEdge(SubLink.EC, v11, v6);
		Edge e5 = g1.createEdge(Link.EC, v2, v5);
		Edge e6 = g1.createEdge(LinkBack.EC, v7, v12);
		Edge e7 = g1.createEdge(SubLink.EC, v9, v8);
		Edge e8 = g1.createEdge(SubLink.EC, v10, v6);
		Edge e9 = g1.createEdge(Link.EC, v3, v7);
		Edge e10 = g1.createEdge(Link.EC, v3, v7);

		// border cases
		assertNull(g1.getEdge(42));
		assertNull(g1.getEdge(-42));
		assertEquals(e1, g1.getEdge(1));
		assertNull(g1.getEdge(1000));
		assertNull(g1.getEdge(-1000));

		// normal cases
		assertEquals(e2, g1.getEdge(2));
		assertEquals(e2.getReversedEdge(), g1.getEdge(-2));
		assertEquals(e3, g1.getEdge(3));
		assertEquals(e3.getReversedEdge(), g1.getEdge(-3));
		assertEquals(e4, g1.getEdge(4));
		assertEquals(e4.getReversedEdge(), g1.getEdge(-4));
		assertEquals(e5, g1.getEdge(5));
		assertEquals(e5.getReversedEdge(), g1.getEdge(-5));
		assertEquals(e6, g1.getEdge(6));
		assertEquals(e6.getReversedEdge(), g1.getEdge(-6));
		assertEquals(e7, g1.getEdge(7));
		assertEquals(e7.getReversedEdge(), g1.getEdge(-7));
		assertEquals(e8, g1.getEdge(8));
		assertEquals(e8.getReversedEdge(), g1.getEdge(-8));
		assertEquals(e9, g1.getEdge(9));
		assertEquals(e9.getReversedEdge(), g1.getEdge(-9));
		assertEquals(e10, g1.getEdge(10));
		assertEquals(e10.getReversedEdge(), g1.getEdge(-10));
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
	public void testGetVCount() {
		// border cases
		assertEquals(0, g2.getVCount());

		Vertex v1 = g2.createVertex(SubNode.VC);

		assertEquals(1, g2.getVCount());

		g2.deleteVertex(v1);

		assertEquals(0, g2.getVCount());

		g2.createVertex(SubNode.VC);

		assertEquals(1, g2.getVCount());

		// normal cases
		assertEquals(12, g1.getVCount());

		Vertex v2 = g2.createVertex(SubNode.VC);

		assertEquals(2, g2.getVCount());

		g2.createVertex(SubNode.VC);

		assertEquals(3, g2.getVCount());

		g2.deleteVertex(v2);

		assertEquals(2, g2.getVCount());

		g2.createVertex(SuperNode.VC);

		assertEquals(3, g2.getVCount());

		Vertex v3 = g2.createVertex(SuperNode.VC);

		assertEquals(4, g2.getVCount());

		g2.deleteVertex(v3);

		assertEquals(3, g2.getVCount());

		Vertex v4 = g2.createVertex(SuperNode.VC);

		assertEquals(4, g2.getVCount());

		g2.createVertex(SuperNode.VC);

		assertEquals(5, g2.getVCount());

		g2.createVertex(DoubleSubNode.VC);

		assertEquals(6, g2.getVCount());

		g2.createVertex(DoubleSubNode.VC);

		assertEquals(7, g2.getVCount());

		g2.deleteVertex(v4);

		assertEquals(6, g2.getVCount());

		g2.createVertex(DoubleSubNode.VC);

		assertEquals(7, g2.getVCount());

		g2.createVertex(DoubleSubNode.VC);

		assertEquals(8, g2.getVCount());

		for (int i = 9; i < 20; i++) {
			g2.createVertex(SuperNode.VC);

			assertEquals(i, g2.getVCount());
		}

		for (int i = 20; i < 32; i++) {
			g2.createVertex(DoubleSubNode.VC);

			assertEquals(i, g2.getVCount());
		}

		for (int i = 32; i < 42; i++) {
			g2.createVertex(SubNode.VC);

			assertEquals(i, g2.getVCount());
		}

	}

	@Test
	public void testGetECount() {
		// border cases
		assertEquals(0, g1.getECount());

		Edge e1 = g1.createEdge(LinkBack.EC, v5, v1);

		assertEquals(1, g1.getECount());

		// creating a vertex does not change the value
		g1.createVertex(DoubleSubNode.VC);

		assertEquals(1, g1.getECount());

		// when an edge is deleted, the count is decreased by 1
		g1.deleteEdge(e1);

		assertEquals(0, g1.getECount());

		// normal cases
		// creating an edge increases the value by 1
		Edge e2 = g1.createEdge(Link.EC, v2, v7);

		assertEquals(1, g1.getECount());

		Edge e3 = g1.createEdge(LinkBack.EC, v8, v4);

		assertEquals(2, g1.getECount());

		Edge e4 = g1.createEdge(SubLink.EC, v11, v6);

		assertEquals(3, g1.getECount());

		Edge e5 = g1.createEdge(Link.EC, v2, v5);

		assertEquals(4, g1.getECount());

		Edge e6 = g1.createEdge(LinkBack.EC, v7, v12);

		assertEquals(5, g1.getECount());

		Edge e7 = g1.createEdge(SubLink.EC, v9, v8);

		assertEquals(6, g1.getECount());

		Edge e8 = g1.createEdge(SubLink.EC, v10, v6);

		assertEquals(7, g1.getECount());

		Edge e9 = g1.createEdge(Link.EC, v3, v7);

		assertEquals(8, g1.getECount());

		Edge e10 = g1.createEdge(Link.EC, v3, v7);

		assertEquals(9, g1.getECount());

		// deleting edges...
		g1.deleteEdge(e2);

		assertEquals(8, g1.getECount());

		g1.deleteEdge(e3);

		assertEquals(7, g1.getECount());

		g1.deleteEdge(e4);

		assertEquals(6, g1.getECount());

		g1.deleteEdge(e5);

		assertEquals(5, g1.getECount());

		g1.deleteEdge(e6);

		assertEquals(4, g1.getECount());

		g1.deleteEdge(e7);

		assertEquals(3, g1.getECount());

		g1.deleteEdge(e8);

		assertEquals(2, g1.getECount());

		g1.deleteEdge(e9);

		assertEquals(1, g1.getECount());

		g1.deleteEdge(e10);

		assertEquals(0, g1.getECount());

	}

	@Test
	public void testEdges() {
		assertFalse(g1.edges().iterator().hasNext());

		Edge e1 = g1.createEdge(Link.EC, v3, v7);
		Edge e2 = g1.createEdge(Link.EC, v4, v8);
		Edge e3 = g1.createEdge(Link.EC, v1, v8);
		Edge e4 = g1.createEdge(SubLink.EC, v12, v5);
		Edge e5 = g1.createEdge(SubLink.EC, v10, v7);
		Edge e6 = g1.createEdge(SubLink.EC, v11, v5);
		Edge e7 = g1.createEdge(LinkBack.EC, v6, v12);
		Edge e8 = g1.createEdge(LinkBack.EC, v6, v3);
		Edge e9 = g1.createEdge(LinkBack.EC, v8, v9);

		Edge[] graphEdges = { e1, e2, e3, e4, e5, e6, e7, e8, e9 };
		int i = 0;
		for (Edge e : g1.edges()) {
			assertEquals(graphEdges[i], e);
			i++;
		}

		Edge e10 = g1.createEdge(SubLink.EC, v11, v6);
		Edge e11 = g1.createEdge(LinkBack.EC, v7, v12);
		Edge e12 = g1.createEdge(LinkBack.EC, v5, v1);
		Edge e13 = g1.createEdge(Link.EC, v12, v5);
		Edge e14 = g1.createEdge(SubLink.EC, v9, v7);
		Edge e15 = g1.createEdge(SubLink.EC, v11, v6);

		Edge[] graphEdges2 = { e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11,
				e12, e13, e14, e15 };
		i = 0;
		for (Edge e : g1.edges()) {
			assertEquals(graphEdges2[i], e);
			i++;
		}

		Edge e16 = g1.createEdge(LinkBack.EC, v5, v2);
		Edge e17 = g1.createEdge(SubLink.EC, v10, v6);
		Edge e18 = g1.createEdge(LinkBack.EC, v8, v12);
		Edge e19 = g1.createEdge(Link.EC, v1, v7);
		Edge e20 = g1.createEdge(SubLink.EC, v10, v6);
		Edge e21 = g1.createEdge(Link.EC, v3, v6);

		Edge[] graphEdges3 = { e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11,
				e12, e13, e14, e15, e16, e17, e18, e19, e20, e21 };
		i = 0;
		for (Edge e : g1.edges()) {
			assertEquals(graphEdges3[i], e);
			i++;
		}

	}

	@Test
	public void testEdges2() {
		// preparations...
		getEdgeClassesOfG1();

		assertFalse(g1.edges(link).iterator().hasNext());
		assertFalse(g1.edges(subL).iterator().hasNext());
		assertFalse(g1.edges(lBack).iterator().hasNext());

		Edge e1 = g1.createEdge(Link.EC, v3, v7);
		Edge e2 = g1.createEdge(Link.EC, v4, v8);
		Edge e3 = g1.createEdge(Link.EC, v1, v8);
		Edge e4 = g1.createEdge(SubLink.EC, v12, v5);
		Edge e5 = g1.createEdge(SubLink.EC, v10, v7);
		Edge e6 = g1.createEdge(SubLink.EC, v11, v5);
		Edge e7 = g1.createEdge(LinkBack.EC, v6, v12);
		Edge e8 = g1.createEdge(LinkBack.EC, v6, v2);
		Edge e9 = g1.createEdge(LinkBack.EC, v8, v9);

		Edge[] graphLink = { e1, e2, e3, e4, e5, e6 };
		int i = 0;
		for (Edge e : g1.edges(link)) {
			assertEquals(graphLink[i], e);
			i++;
		}

		Edge[] graphSubLink = { e4, e5, e6 };
		i = 0;
		for (Edge e : g1.edges(subL)) {
			assertEquals(graphSubLink[i], e);
			i++;
		}

		Edge[] graphLinkBack = { e7, e8, e9 };
		i = 0;
		for (Edge e : g1.edges(lBack)) {
			assertEquals(graphLinkBack[i], e);
			i++;
		}

		Edge e10 = g1.createEdge(SubLink.EC, v11, v6);
		Edge e11 = g1.createEdge(LinkBack.EC, v7, v12);
		Edge e12 = g1.createEdge(LinkBack.EC, v5, v1);
		Edge e13 = g1.createEdge(Link.EC, v12, v5);
		Edge e14 = g1.createEdge(SubLink.EC, v9, v7);
		Edge e15 = g1.createEdge(SubLink.EC, v11, v6);

		Edge[] graphLink2 = { e1, e2, e3, e4, e5, e6, e10, e13, e14, e15 };
		i = 0;
		for (Edge e : g1.edges(link)) {
			assertEquals(graphLink2[i], e);
			i++;
		}

		Edge[] graphSubLink2 = { e4, e5, e6, e10, e14, e15 };
		i = 0;
		for (Edge e : g1.edges(subL)) {
			assertEquals(graphSubLink2[i], e);
			i++;
		}

		Edge[] graphLinkBack2 = { e7, e8, e9, e11, e12 };
		i = 0;
		for (Edge e : g1.edges(lBack)) {
			assertEquals(graphLinkBack2[i], e);
			i++;
		}

	}

	@Test
	public void testEdges3() {
		Edge e1 = g1.createEdge(Link.EC, v3, v7);
		Edge e2 = g1.createEdge(Link.EC, v4, v8);
		Edge e3 = g1.createEdge(Link.EC, v1, v8);
		Edge e4 = g1.createEdge(SubLink.EC, v12, v5);
		Edge e5 = g1.createEdge(SubLink.EC, v10, v7);
		Edge e6 = g1.createEdge(SubLink.EC, v11, v5);
		Edge e7 = g1.createEdge(LinkBack.EC, v6, v12);
		Edge e8 = g1.createEdge(LinkBack.EC, v6, v3);
		Edge e9 = g1.createEdge(LinkBack.EC, v8, v9);

		Edge[] graphLink = { e1, e2, e3, e4, e5, e6 };
		int i = 0;
		for (Edge e : g1.edges(Link.EC)) {
			assertEquals(graphLink[i], e);
			i++;
		}

		Edge[] graphSubLink = { e4, e5, e6 };
		i = 0;
		for (Edge e : g1.edges(SubLink.EC)) {
			assertEquals(graphSubLink[i], e);
			i++;
		}

		Edge[] graphLinkBack = { e7, e8, e9 };
		i = 0;
		for (Edge e : g1.edges(LinkBack.EC)) {
			assertEquals(graphLinkBack[i], e);
			i++;
		}

		Edge e10 = g1.createEdge(LinkBack.EC, v5, v2);
		Edge e11 = g1.createEdge(SubLink.EC, v10, v6);
		Edge e12 = g1.createEdge(LinkBack.EC, v8, v12);
		Edge e13 = g1.createEdge(Link.EC, v1, v7);
		Edge e14 = g1.createEdge(SubLink.EC, v10, v6);
		Edge e15 = g1.createEdge(Link.EC, v3, v6);

		Edge[] graphLink2 = { e1, e2, e3, e4, e5, e6, e11, e13, e14, e15 };
		i = 0;
		for (Edge e : g1.edges(Link.EC)) {
			assertEquals(graphLink2[i], e);
			i++;
		}

		Edge[] graphSubLink2 = { e4, e5, e6, e11, e14 };
		i = 0;
		for (Edge e : g1.edges(SubLink.EC)) {
			assertEquals(graphSubLink2[i], e);
			i++;
		}

		Edge[] graphLinkBack2 = { e7, e8, e9, e10, e12 };
		i = 0;
		for (Edge e : g1.edges(LinkBack.EC)) {
			assertEquals(graphLinkBack2[i], e);
			i++;
		}

	}

	@Test
	public void testVertices() {
		assertFalse(g2.vertices().iterator().hasNext());
		assertTrue(g1.vertices().iterator().hasNext());

		Vertex[] graphVertices = { v1, v2, v3, v4, v5, v6, v7, v8, v9, v10,
				v11, v12 };

		int i = 0;
		for (Vertex v : g1.vertices()) {
			assertEquals(graphVertices[i], v);
			i++;
		}

		Vertex v13 = g1.createVertex(DoubleSubNode.VC);
		Vertex v14 = g1.createVertex(SuperNode.VC);
		Vertex v15 = g1.createVertex(SuperNode.VC);
		Vertex v16 = g1.createVertex(DoubleSubNode.VC);

		Vertex[] graphVertices2 = { v1, v2, v3, v4, v5, v6, v7, v8, v9, v10,
				v11, v12, v13, v14, v15, v16 };

		i = 0;
		for (Vertex v : g1.vertices()) {
			assertEquals(graphVertices2[i], v);
			i++;
		}

		Vertex v17 = g1.createVertex(SubNode.VC);
		Vertex v18 = g1.createVertex(DoubleSubNode.VC);
		Vertex v19 = g1.createVertex(SubNode.VC);
		Vertex v20 = g1.createVertex(SuperNode.VC);
		Vertex v21 = g1.createVertex(DoubleSubNode.VC);
		Vertex v22 = g1.createVertex(SuperNode.VC);

		Vertex[] graphVertices3 = { v1, v2, v3, v4, v5, v6, v7, v8, v9, v10,
				v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22 };

		i = 0;
		for (Vertex v : g1.vertices()) {
			assertEquals(graphVertices3[i], v);
			i++;
		}

	}

	@Test
	public void testVertices2() {
		// preparations...
		getVertexClassesOfG1();

		assertFalse(g2.vertices(subN).iterator().hasNext());
		assertFalse(g2.vertices(superN).iterator().hasNext());
		assertFalse(g2.vertices(doubleSubN).iterator().hasNext());
		assertTrue(g1.vertices(subN).iterator().hasNext());
		assertTrue(g1.vertices(superN).iterator().hasNext());
		assertTrue(g1.vertices(doubleSubN).iterator().hasNext());

		Vertex[] graphSubN = { v1, v2, v3, v4, v9, v10, v11, v12 };
		int i = 0;
		for (Vertex v : g1.vertices(subN)) {
			assertEquals(graphSubN[i], v);
			i++;
		}

		Vertex[] graphSuperN = { v5, v6, v7, v8, v9, v10, v11, v12 };
		i = 0;
		for (Vertex v : g1.vertices(superN)) {
			assertEquals(graphSuperN[i], v);
			i++;
		}

		Vertex[] graphDSN = { v9, v10, v11, v12 };
		i = 0;
		for (Vertex v : g1.vertices(doubleSubN)) {
			assertEquals(graphDSN[i], v);
			i++;
		}

		Vertex v13 = g1.createVertex(DoubleSubNode.VC);
		Vertex v14 = g1.createVertex(SubNode.VC);
		Vertex v15 = g1.createVertex(DoubleSubNode.VC);
		Vertex v16 = g1.createVertex(SuperNode.VC);
		Vertex v17 = g1.createVertex(SuperNode.VC);
		Vertex v18 = g1.createVertex(SubNode.VC);
		Vertex v19 = g1.createVertex(DoubleSubNode.VC);
		Vertex v20 = g1.createVertex(SuperNode.VC);

		Vertex[] graphSubN2 = { v1, v2, v3, v4, v9, v10, v11, v12, v13, v14,
				v15, v18, v19 };
		i = 0;
		for (Vertex v : g1.vertices(subN)) {
			assertEquals(graphSubN2[i], v);
			i++;
		}

		Vertex[] graphSuperN2 = { v5, v6, v7, v8, v9, v10, v11, v12, v13, v15,
				v16, v17, v19, v20 };
		i = 0;
		for (Vertex v : g1.vertices(superN)) {
			assertEquals(graphSuperN2[i], v);
			i++;
		}

		Vertex[] graphDSN2 = { v9, v10, v11, v12, v13, v15, v19 };
		i = 0;
		for (Vertex v : g1.vertices(doubleSubN)) {
			assertEquals(graphDSN2[i], v);
			i++;
		}

	}

	@Test
	public void testVertices3() {
		assertFalse(g2.vertices(SubNode.VC).iterator().hasNext());
		assertFalse(g2.vertices(SuperNode.VC).iterator().hasNext());
		assertFalse(g2.vertices(DoubleSubNode.VC).iterator().hasNext());

		assertTrue(g1.vertices(SubNode.VC).iterator().hasNext());
		assertTrue(g1.vertices(SuperNode.VC).iterator().hasNext());
		assertTrue(g1.vertices(DoubleSubNode.VC).iterator().hasNext());

		Vertex[] graphSubN = { v1, v2, v3, v4, v9, v10, v11, v12 };
		int i = 0;
		for (Vertex v : g1.vertices(SubNode.VC)) {
			assertEquals(graphSubN[i], v);
			i++;
		}

		Vertex[] graphSuperN = { v5, v6, v7, v8, v9, v10, v11, v12 };
		i = 0;
		for (Vertex v : g1.vertices(SuperNode.VC)) {
			assertEquals(graphSuperN[i], v);
			i++;
		}

		Vertex[] graphDoubleSubN = { v9, v10, v11, v12 };
		i = 0;
		for (Vertex v : g1.vertices(DoubleSubNode.VC)) {
			assertEquals(graphDoubleSubN[i], v);
			i++;
		}

		Vertex v13 = g1.createVertex(DoubleSubNode.VC);
		Vertex v14 = g1.createVertex(DoubleSubNode.VC);
		Vertex v15 = g1.createVertex(SuperNode.VC);
		Vertex v16 = g1.createVertex(SubNode.VC);
		Vertex v17 = g1.createVertex(SuperNode.VC);
		Vertex v18 = g1.createVertex(DoubleSubNode.VC);
		Vertex v19 = g1.createVertex(SubNode.VC);
		Vertex v20 = g1.createVertex(SuperNode.VC);

		Vertex[] graphSubN2 = { v1, v2, v3, v4, v9, v10, v11, v12, v13, v14,
				v16, v18, v19 };
		i = 0;
		for (Vertex v : g1.vertices(SubNode.VC)) {
			assertEquals(graphSubN2[i], v);
			i++;
		}

		Vertex[] graphSuperN2 = { v5, v6, v7, v8, v9, v10, v11, v12, v13, v14,
				v15, v17, v18, v20 };
		i = 0;
		for (Vertex v : g1.vertices(SuperNode.VC)) {
			assertEquals(graphSuperN2[i], v);
			i++;
		}

		Vertex[] graphDoubleSubN2 = { v9, v10, v11, v12, v13, v14, v18 };
		i = 0;
		for (Vertex v : g1.vertices(DoubleSubNode.VC)) {
			assertEquals(graphDoubleSubN2[i], v);
			i++;
		}

	}

	/**
	 * Tests if null is returned if a vertex is requested from the graph whose
	 * id is bigger than maxV
	 * 
	 * @
	 */
	@Test
	public void testGetVertexBorderCase() {
		InternalGraph g = (InternalGraph) createMinimalGraph();
		assertNull(g.getVertex(1));
		assertNull(g.getVertex(g.getMaxVCount()));
		assertNull(g.getVertex(g.getMaxVCount() + 1));
		assertNull(g.getVertex(g.getMaxVCount() + 1000));
	}

	/**
	 * Tests if null is returned if an edge is requested from the graph whose id
	 * is bigger than maxE
	 * 
	 * @
	 */
	@Test
	public void testGetEdgeBorderCase() {
		InternalGraph g = (InternalGraph) createMinimalGraph();
		assertNull(g.getEdge(1));
		assertNull(g.getEdge(g.getMaxECount()));
		assertNull(g.getEdge(g.getMaxECount() + 1));
		assertNull(g.getEdge(g.getMaxECount() + 1000));
		assertNull(g.getEdge(-1));
		assertNull(g.getEdge(-g.getMaxECount()));
		assertNull(g.getEdge(-g.getMaxECount() - 1));
		assertNull(g.getEdge(-g.getMaxECount() - 1000));
	}

	@Test
	public void isInstanceOfTest() {
		InternalGraph g = (InternalGraph) createMinimalGraph();
		assertTrue(g.isInstanceOf(g.getAttributedElementClass()));
		assertTrue(g.isInstanceOf(MinimalSchema.instance().getGraphClass()));
		assertFalse(g.isInstanceOf(GreqlSchema.instance().getGraphClass()));
	}
}
