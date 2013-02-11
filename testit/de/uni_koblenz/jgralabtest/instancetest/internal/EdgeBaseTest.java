/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphException;
import de.uni_koblenz.jgralab.impl.InternalVertex;
import de.uni_koblenz.jgralabtest.instancetest.InstanceTest;
import de.uni_koblenz.jgralabtest.schemas.vertextest.AbstractSuperNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.DoubleSubNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.Link;
import de.uni_koblenz.jgralabtest.schemas.vertextest.LinkBack;
import de.uni_koblenz.jgralabtest.schemas.vertextest.SubLink;
import de.uni_koblenz.jgralabtest.schemas.vertextest.SubNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.SuperNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestGraph;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestSchema;

@RunWith(Parameterized.class)
public class EdgeBaseTest extends InstanceTest {

	private static final int RANDOM_VERTEX_COUNT = 10;

	public EdgeBaseTest(ImplementationType implementationType, String dbURL) {
		super(implementationType, dbURL);
	}

	@Parameters
	public static Collection<Object[]> configure() {
		return getParameters();
	}

	private VertexTestGraph g;
	private Random rand;

	/**
	 * Creates a new graph for each test.
	 */
	@Before
	public void setUp() {
		switch (implementationType) {
		case STANDARD:
			g = VertexTestSchema.instance().createVertexTestGraph(
					ImplementationType.STANDARD);
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}

		rand = new Random(System.currentTimeMillis());
	}

	/*
	 * Test of the Interface Edge
	 */

	/**
	 * Tests if the incident edges of <code>c</code> equals the edges of
	 * <code>incidentEdges</code>.
	 * 
	 * @param v
	 * @param incidentEdges
	 *            @
	 */
	private void testIncidenceList(Vertex v, Edge... incidentEdges) {
		Iterable<Edge> incidences;
		assertEquals(incidentEdges.length, v.getDegree());
		incidences = v.incidences();

		int i = 0;
		for (Edge e : incidences) {
			assertEquals(incidentEdges[i], e);
			i++;
		}
	}

	/**
	 * Creates a random graph and returns an 2-dim ArrayList ret.get(0) =
	 * incident edges of v1 ret.get(1) = incident edges of v2 ret.get(2) =
	 * incident edges of v3
	 * 
	 * @return ret @
	 */
	private ArrayList<ArrayList<Edge>> createRandomGraph() {
		ArrayList<ArrayList<Edge>> ret = new ArrayList<ArrayList<Edge>>(6);
		ret.add(new ArrayList<Edge>());
		ret.add(new ArrayList<Edge>());
		ret.add(new ArrayList<Edge>());

		Vertex[] nodes = new Vertex[] { g.createSubNode(),
				g.createDoubleSubNode(), g.createSuperNode() };
		for (int i = 0; i < RANDOM_VERTEX_COUNT; i++) {
			int edge = rand.nextInt(3);
			switch (edge) {
			case 0:
				int start = rand.nextInt(2);
				int end = rand.nextInt(2) + 1;
				Edge e = g.createLink((AbstractSuperNode) nodes[start],
						(SuperNode) nodes[end]);
				ret.get(start).add(e);
				ret.get(end).add(e.getReversedEdge());
				break;
			case 1:
				start = 1;
				end = rand.nextInt(2) + 1;
				e = g.createSubLink((DoubleSubNode) nodes[start],
						(SuperNode) nodes[end]);
				ret.get(start).add(e);
				ret.get(end).add(e.getReversedEdge());
				break;
			case 2:
				start = rand.nextInt(2) + 1;
				end = rand.nextInt(2);
				e = g.createLinkBack((SuperNode) nodes[start],
						(AbstractSuperNode) nodes[end]);
				ret.get(start).add(e);
				ret.get(end).add(e.getReversedEdge());
				break;
			}
		}
		return ret;
	}

	/**
	 * Alpha of an edge is changed to another vertex.
	 * 
	 * @
	 */
	@Test
	public void setAlphaTest0() {
		DoubleSubNode v1;
		DoubleSubNode v2;
		DoubleSubNode v3;
		Edge e1;
		long v1vers;
		long v2vers;
		long v3vers;
		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		v3 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v2);
		v1vers = ((InternalVertex) v1).getIncidenceListVersion();
		v2vers = ((InternalVertex) v2).getIncidenceListVersion();
		v3vers = ((InternalVertex) v3).getIncidenceListVersion();
		e1.setAlpha(v3);

		assertEquals(v3, e1.getAlpha());
		assertTrue(((InternalVertex) v1).isIncidenceListModified(v1vers));
		assertFalse(((InternalVertex) v2).isIncidenceListModified(v2vers));
		assertTrue(((InternalVertex) v3).isIncidenceListModified(v3vers));
		Edge reversedEdge = e1.getReversedEdge();

		testIncidenceList(v1);
		testIncidenceList(v2, reversedEdge);
		testIncidenceList(v3, e1);
	}

	/**
	 * Alpha of an edge is set to the previous alpha vertex.
	 * 
	 * @
	 */
	@Test
	public void setAlphaTest1() {
		DoubleSubNode v1;
		DoubleSubNode v2;
		Edge e1;
		long v1vers;
		long v2vers;

		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v2);
		v1vers = ((InternalVertex) v1).getIncidenceListVersion();
		v2vers = ((InternalVertex) v2).getIncidenceListVersion();
		e1.setAlpha(v1);

		assertEquals(v1, e1.getAlpha());
		assertFalse(((InternalVertex) v1).isIncidenceListModified(v1vers));
		assertFalse(((InternalVertex) v2).isIncidenceListModified(v2vers));
		Edge reversedEdge = e1.getReversedEdge();

		testIncidenceList(v1, e1);
		testIncidenceList(v2, reversedEdge);
	}

	/**
	 * Alpha of an edge is changed to the omega vertex.
	 * 
	 * @
	 */
	@Test
	public void setAlphaTest2() {
		DoubleSubNode v1;
		DoubleSubNode v2;
		Edge e1;
		long v1vers;
		long v2vers;

		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v2);

		v1vers = ((InternalVertex) v1).getIncidenceListVersion();
		v2vers = ((InternalVertex) v2).getIncidenceListVersion();

		e1.setAlpha(v2);

		assertEquals(v2, e1.getAlpha());
		assertTrue(((InternalVertex) v1).isIncidenceListModified(v1vers));
		assertTrue(((InternalVertex) v2).isIncidenceListModified(v2vers));
		Edge reversedEdge = e1.getReversedEdge();

		testIncidenceList(v1);
		testIncidenceList(v2, reversedEdge, e1);
	}

	/**
	 * Alpha of an edge is changed to another vertex. And there exists further
	 * edges.
	 * 
	 * @
	 */
	@Test
	public void setAlphaTest3() {
		DoubleSubNode v1;
		DoubleSubNode v2;
		DoubleSubNode v3;
		Edge e1;
		Edge e2;
		Edge e3;
		long v1vers;
		long v2vers;
		long v3vers;

		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		v3 = g.createDoubleSubNode();
		e1 = g.createLink(v3, v1);
		e2 = g.createLink(v1, v2);
		e3 = g.createLink(v2, v3);

		v1vers = ((InternalVertex) v1).getIncidenceListVersion();
		v2vers = ((InternalVertex) v2).getIncidenceListVersion();
		v3vers = ((InternalVertex) v3).getIncidenceListVersion();

		e2.setAlpha(v3);

		assertEquals(v3, e2.getAlpha());
		assertTrue(((InternalVertex) v1).isIncidenceListModified(v1vers));
		assertFalse(((InternalVertex) v2).isIncidenceListModified(v2vers));
		assertTrue(((InternalVertex) v3).isIncidenceListModified(v3vers));
		Edge reversedEdge = e1.getReversedEdge();
		Edge reversedEdge2 = e2.getReversedEdge();
		Edge reversedEdge3 = e3.getReversedEdge();

		testIncidenceList(v1, reversedEdge);
		testIncidenceList(v2, reversedEdge2, e3);
		testIncidenceList(v3, e1, reversedEdge3, e2);
	}

	/**
	 * An exception should occur if you try to set alpha to a vertex which type
	 * isn't allowed as an alpha vertex for that edge.
	 * 
	 * @
	 */
	@Test(expected = GraphException.class)
	public void setAlphaTest4() {
		DoubleSubNode v1 = g.createDoubleSubNode();
		SuperNode v2 = g.createSuperNode();
		Edge e1 = g.createLink(v1, v2);
		e1.setAlpha(v2);
	}

	/**
	 * Random Test
	 * 
	 * @
	 */
	@Test
	public void setAlphaTest5() {
		ArrayList<ArrayList<Edge>> incidences = createRandomGraph();
		for (int i = 0; i < RANDOM_VERTEX_COUNT; i++) {
			int edgeId = rand.nextInt(g.getECount()) + 1;
			Edge e = g.getEdge(edgeId);
			int oldAlphaId = e.getAlpha().getId();
			int newAlphaId = rand.nextInt(3) + 1;
			Vertex newAlpha = g.getVertex(newAlphaId);

			try {
				e.setAlpha(newAlpha);
				if (oldAlphaId != newAlphaId) {
					incidences.get(oldAlphaId - 1).remove(e);
					incidences.get(newAlphaId - 1).add(e);
				}

			} catch (GraphException ge) {
				if ((e instanceof SubLink)
						&& (newAlpha instanceof DoubleSubNode)) {
					fail("SubLink can have an alpha of type "
							+ newAlpha.getClass().getName());
				} else if ((e instanceof Link) && !(e instanceof SubLink)
						&& (newAlpha instanceof AbstractSuperNode)) {
					fail("Link can have an alpha of type "
							+ newAlpha.getClass().getName());
				} else if ((e instanceof LinkBack)
						&& (newAlpha instanceof SuperNode)) {
					fail("LinkBack can have an alpha of type "
							+ newAlpha.getClass().getName());
				}
			}
		}

		Vertex vertex = g.getVertex(1);
		Edge[] array = incidences.get(0).toArray(new Edge[0]);
		Vertex vertex2 = g.getVertex(2);
		Edge[] array2 = incidences.get(1).toArray(new Edge[0]);
		Vertex vertex3 = g.getVertex(3);
		Edge[] array3 = incidences.get(2).toArray(new Edge[0]);

		testIncidenceList(vertex, array);
		testIncidenceList(vertex2, array2);
		testIncidenceList(vertex3, array3);
	}

	/**
	 * Alpha of an reversedEdge is changed to another vertex.
	 * 
	 * @
	 */
	@Test
	public void setAlphaTestR0() {
		DoubleSubNode v1;
		DoubleSubNode v2;
		DoubleSubNode v3;
		Edge e1;
		long v1vers;
		long v2vers;
		long v3vers;

		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		v3 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v2).getReversedEdge();
		v1vers = ((InternalVertex) v1).getIncidenceListVersion();
		v2vers = ((InternalVertex) v2).getIncidenceListVersion();
		v3vers = ((InternalVertex) v3).getIncidenceListVersion();
		e1.setAlpha(v3);

		assertEquals(v3, e1.getAlpha());
		assertTrue(((InternalVertex) v1).isIncidenceListModified(v1vers));
		assertFalse(((InternalVertex) v2).isIncidenceListModified(v2vers));
		assertTrue(((InternalVertex) v3).isIncidenceListModified(v3vers));
		Edge reversedEdge = e1.getReversedEdge();

		testIncidenceList(v1);
		testIncidenceList(v2, e1);
		testIncidenceList(v3, reversedEdge);

	}

	// tests of the method void setOmega(Vertex v);

	/**
	 * Omega of an edge is changed to another vertex.
	 * 
	 * @
	 */
	@Test
	public void setOmegaTest0() {
		DoubleSubNode v1;
		DoubleSubNode v2;
		DoubleSubNode v3;
		Edge e1;
		long v1vers;
		long v2vers;
		long v3vers;

		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		v3 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v2);
		v1vers = ((InternalVertex) v1).getIncidenceListVersion();
		v2vers = ((InternalVertex) v2).getIncidenceListVersion();
		v3vers = ((InternalVertex) v3).getIncidenceListVersion();
		e1.setOmega(v3);

		assertEquals(v3, e1.getOmega());
		assertFalse(((InternalVertex) v1).isIncidenceListModified(v1vers));
		assertTrue(((InternalVertex) v2).isIncidenceListModified(v2vers));
		assertTrue(((InternalVertex) v3).isIncidenceListModified(v3vers));
		Edge reversedEdge = e1.getReversedEdge();

		testIncidenceList(v1, e1);
		testIncidenceList(v2);
		testIncidenceList(v3, reversedEdge);
	}

	/**
	 * Omega of an edge is set to the previous omega vertex.
	 * 
	 * @
	 */
	@Test
	public void setOmegaTest1() {
		DoubleSubNode v1;
		DoubleSubNode v2;
		Edge e1;
		long v1vers;
		long v2vers;

		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v2);
		v1vers = ((InternalVertex) v1).getIncidenceListVersion();
		v2vers = ((InternalVertex) v2).getIncidenceListVersion();
		e1.setOmega(v2);

		assertEquals(v2, e1.getOmega());
		assertFalse(((InternalVertex) v1).isIncidenceListModified(v1vers));
		assertFalse(((InternalVertex) v2).isIncidenceListModified(v2vers));
		Edge reversedEdge = e1.getReversedEdge();

		testIncidenceList(v1, e1);
		testIncidenceList(v2, reversedEdge);
	}

	/**
	 * Omega of an edge is changed to the alpha vertex.
	 * 
	 * @
	 */
	@Test
	public void setOmegaTest2() {
		DoubleSubNode v1;
		DoubleSubNode v2;
		Edge e1;
		long v1vers;
		long v2vers;

		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v2);
		v1vers = ((InternalVertex) v1).getIncidenceListVersion();
		v2vers = ((InternalVertex) v2).getIncidenceListVersion();
		e1.setOmega(v1);

		assertEquals(v1, e1.getOmega());
		assertTrue(((InternalVertex) v1).isIncidenceListModified(v1vers));
		assertTrue(((InternalVertex) v2).isIncidenceListModified(v2vers));
		Edge reversedEdge = e1.getReversedEdge();

		testIncidenceList(v1, e1, reversedEdge);
		testIncidenceList(v2);
	}

	/**
	 * Omega of an edge is changed to another vertex. And there exists further
	 * edges.
	 * 
	 * @
	 */
	@Test
	public void setOmegaTest3() {
		DoubleSubNode v1;
		DoubleSubNode v2;
		DoubleSubNode v3;
		Edge e1;
		Edge e2;
		Edge e3;
		long v1vers;
		long v2vers;
		long v3vers;

		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		v3 = g.createDoubleSubNode();
		e1 = g.createLink(v3, v1);
		e2 = g.createLink(v1, v2);
		e3 = g.createLink(v2, v3);

		v1vers = ((InternalVertex) v1).getIncidenceListVersion();
		v2vers = ((InternalVertex) v2).getIncidenceListVersion();
		v3vers = ((InternalVertex) v3).getIncidenceListVersion();

		e2.setOmega(v3);

		assertEquals(v3, e2.getOmega());
		assertFalse(((InternalVertex) v1).isIncidenceListModified(v1vers));
		assertTrue(((InternalVertex) v2).isIncidenceListModified(v2vers));
		assertTrue(((InternalVertex) v3).isIncidenceListModified(v3vers));
		Edge reversedEdge = e1.getReversedEdge();
		Edge reversedEdge2 = e2.getReversedEdge();
		Edge reversedEdge3 = e3.getReversedEdge();

		testIncidenceList(v1, reversedEdge, e2);
		testIncidenceList(v2, e3);
		testIncidenceList(v3, e1, reversedEdge3, reversedEdge2);
	}

	/**
	 * An exception should occur if you try to set omega to a vertex which type
	 * isn't allowed as an omega vertex for that edge.
	 * 
	 * @
	 */
	@Test(expected = GraphException.class)
	public void setOmegaTest4() {
		SubNode v1 = g.createSubNode();
		SuperNode v2 = g.createSuperNode();
		Edge e1 = g.createLink(v1, v2);
		e1.setOmega(v1);
	}

	/**
	 * Random Test
	 * 
	 * @
	 */
	@Test
	public void setOmegaTest5() {
		ArrayList<ArrayList<Edge>> incidences = createRandomGraph();
		for (int i = 0; i < RANDOM_VERTEX_COUNT; i++) {
			int edgeId = rand.nextInt(g.getECount()) + 1;
			Edge e = g.getEdge(edgeId);
			int oldOmegaId = e.getOmega().getId();
			int newOmegaId = rand.nextInt(3) + 1;
			Vertex newOmega = g.getVertex(newOmegaId);

			try {
				e.setOmega(newOmega);
				if (oldOmegaId != newOmegaId) {
					incidences.get(oldOmegaId - 1).remove(e.getReversedEdge());
					incidences.get(newOmegaId - 1).add(e.getReversedEdge());
				}
			} catch (GraphException ge) {
				if ((e instanceof Link) && (newOmega instanceof SuperNode)) {
					fail("Link can have an alpha of type "
							+ newOmega.getClass().getName());
				} else if ((e instanceof LinkBack)
						&& (newOmega instanceof AbstractSuperNode)) {
					fail("LinkBack can have an alpha of type "
							+ newOmega.getClass().getName());
				}
			}
		}

		Vertex vertex = g.getVertex(1);
		Edge[] array = incidences.get(0).toArray(new Edge[0]);
		Vertex vertex2 = g.getVertex(2);
		Edge[] array2 = incidences.get(1).toArray(new Edge[0]);
		Edge[] array3 = incidences.get(2).toArray(new Edge[0]);
		Vertex vertex3 = g.getVertex(3);

		testIncidenceList(vertex, array);
		testIncidenceList(vertex2, array2);
		testIncidenceList(vertex3, array3);
	}

	/**
	 * Omega of an reversedEdge is changed to another vertex.
	 * 
	 * @
	 */
	@Test
	public void setOmegaTestR0() {
		DoubleSubNode v1;
		DoubleSubNode v2;
		DoubleSubNode v3;
		Edge e1;
		long v1vers;
		long v2vers;
		long v3vers;

		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		v3 = g.createDoubleSubNode();
		e1 = g.createLink(v1, v2).getReversedEdge();
		v1vers = ((InternalVertex) v1).getIncidenceListVersion();
		v2vers = ((InternalVertex) v2).getIncidenceListVersion();
		v3vers = ((InternalVertex) v3).getIncidenceListVersion();
		e1.setOmega(v3);

		assertEquals(v3, e1.getOmega());
		assertFalse(((InternalVertex) v1).isIncidenceListModified(v1vers));
		assertTrue(((InternalVertex) v2).isIncidenceListModified(v2vers));
		assertTrue(((InternalVertex) v3).isIncidenceListModified(v3vers));
		Edge reversedEdge = e1.getReversedEdge();

		testIncidenceList(v1, reversedEdge);
		testIncidenceList(v2);
		testIncidenceList(v3, e1);
	}

	// tests of the method void setThat(Vertex v);

	/**
	 * That of an edge is changed to another vertex. And there exists further
	 * edges.
	 * 
	 * @
	 */
	@Test
	public void setThatTest3() {
		DoubleSubNode v1;
		DoubleSubNode v2;
		DoubleSubNode v3;
		Edge e1;
		Edge e2;
		Edge e3;
		long v1vers;
		long v2vers;
		long v3vers;

		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		v3 = g.createDoubleSubNode();
		e1 = g.createLink(v3, v1);
		e2 = g.createLink(v1, v2);
		e3 = g.createLink(v2, v3);

		v1vers = ((InternalVertex) v1).getIncidenceListVersion();
		v2vers = ((InternalVertex) v2).getIncidenceListVersion();
		v3vers = ((InternalVertex) v3).getIncidenceListVersion();
		Edge reversedEdge2 = e2.getReversedEdge();

		reversedEdge2.setThat(v3);

		assertEquals(v3, e2.getAlpha());
		assertTrue(((InternalVertex) v1).isIncidenceListModified(v1vers));
		assertFalse(((InternalVertex) v2).isIncidenceListModified(v2vers));
		assertTrue(((InternalVertex) v3).isIncidenceListModified(v3vers));
		Edge reversedEdge = e1.getReversedEdge();
		Edge reversedEdge3 = e3.getReversedEdge();

		testIncidenceList(v1, reversedEdge);
		testIncidenceList(v2, reversedEdge2, e3);
		testIncidenceList(v3, e1, reversedEdge3, e2);

		// test ReversedEdge
		v1vers = ((InternalVertex) v1).getIncidenceListVersion();
		v2vers = ((InternalVertex) v2).getIncidenceListVersion();
		v3vers = ((InternalVertex) v3).getIncidenceListVersion();
		e2.setThat(v3);
		assertEquals(v3, e2.getOmega());
		assertFalse(((InternalVertex) v1).isIncidenceListModified(v1vers));
		assertTrue(((InternalVertex) v2).isIncidenceListModified(v2vers));
		assertTrue(((InternalVertex) v3).isIncidenceListModified(v3vers));
		testIncidenceList(v1, reversedEdge);
		testIncidenceList(v2, e3);
		testIncidenceList(v3, e1, reversedEdge3, e2, reversedEdge2);
	}

	// tests of the method void setThis(Vertex v);

	/**
	 * This of an edge is changed to another vertex. And there exists further
	 * edges.
	 * 
	 * @
	 */
	@Test
	public void setThisTest3() {
		DoubleSubNode v1;
		DoubleSubNode v2;
		DoubleSubNode v3;
		Edge e1;
		Edge e2;
		Edge e3;
		long v1vers;
		long v2vers;
		long v3vers;
		v1 = g.createDoubleSubNode();
		v2 = g.createDoubleSubNode();
		v3 = g.createDoubleSubNode();
		e1 = g.createLink(v3, v1);
		e2 = g.createLink(v1, v2);
		e3 = g.createLink(v2, v3);
		v1vers = ((InternalVertex) v1).getIncidenceListVersion();
		v2vers = ((InternalVertex) v2).getIncidenceListVersion();
		v3vers = ((InternalVertex) v3).getIncidenceListVersion();

		e2.setThis(v3);

		assertEquals(v3, e2.getThis());
		assertTrue(((InternalVertex) v1).isIncidenceListModified(v1vers));
		assertFalse(((InternalVertex) v2).isIncidenceListModified(v2vers));
		assertTrue(((InternalVertex) v3).isIncidenceListModified(v3vers));
		Edge reversedEdge = e1.getReversedEdge();
		Edge reversedEdge2 = e2.getReversedEdge();
		Edge reversedEdge3 = e3.getReversedEdge();

		testIncidenceList(v1, reversedEdge);
		testIncidenceList(v2, reversedEdge2, e3);
		testIncidenceList(v3, e1, reversedEdge3, e2);

		// test ReversedEdge
		v1vers = ((InternalVertex) v1).getIncidenceListVersion();
		v2vers = ((InternalVertex) v2).getIncidenceListVersion();
		v3vers = ((InternalVertex) v3).getIncidenceListVersion();

		reversedEdge2.setThis(v3);

		assertEquals(v3, e2.getOmega());
		assertFalse(((InternalVertex) v1).isIncidenceListModified(v1vers));
		assertTrue(((InternalVertex) v2).isIncidenceListModified(v2vers));
		assertTrue(((InternalVertex) v3).isIncidenceListModified(v3vers));

		testIncidenceList(v1, reversedEdge);
		testIncidenceList(v2, e3);
		testIncidenceList(v3, e1, reversedEdge3, e2, reversedEdge2);

	}

	// tests of the method void putBeforeInGraph(Edge e);
	// (tested in EdgeListTest.java)

	// tests of the method void putAfterInGraph(Edge e);
	// (tested in EdgeListTest.java)

	// tests of the method void delete();
	// (tested in EdgeListTest.java)

	// tests of the method void setAlpha(Vertex v);

}
