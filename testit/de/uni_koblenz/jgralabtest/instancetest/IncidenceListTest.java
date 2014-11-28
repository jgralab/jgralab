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
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.impl.InternalVertex;
import de.uni_koblenz.jgralabtest.schemas.minimal.Link;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.minimal.Node;
import de.uni_koblenz.jgralabtest.tools.RandomBufferGeneric;

@RunWith(Parameterized.class)
public class IncidenceListTest extends InstanceTest {

	private static final int EDGES_PER_NODE = 10;
	private static final int RANDOM_TEST_AMOUNT = 2;
	private static final int NODE_COUNT = 10;

	public IncidenceListTest(ImplementationType implementationType, String dbURL) {
		super(implementationType, dbURL);
	}

	@Parameters
	public static Collection<Object[]> configure() {
		return getParameters();
	}

	final int V = 4; // initial max vertex count
	final int E = 4; // initial max edge count
	final int N = NODE_COUNT; // created vertex count

	MinimalGraph g;
	Node[] nodes;
	private Random rnd;

	@Before
	public void setup() {
		rnd = new Random(System.currentTimeMillis());
		switch (implementationType) {
		case STANDARD:
			g = MinimalSchema.instance().createMinimalGraph(
					ImplementationType.STANDARD, null, V, E);
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}

		nodes = new Node[N];

		for (int i = 0; i < N; ++i) {
			nodes[i] = g.createNode();
		}
	}

	@Test
	public void addEdgeTest() throws Exception {
		// incidence lists must initially be empty
		for (int i = 0; i < N; ++i) {
			assertEquals(0, nodes[i].getDegree());
			Assert.assertNull(nodes[i].getFirstIncidence());
		}

		Edge e1 = g.createLink(nodes[0], nodes[1]);

		assertEquals(e1, nodes[0].getFirstIncidence());
		assertEquals(e1, nodes[0].getLastIncidence());
		assertEquals(e1.getReversedEdge(), nodes[1].getFirstIncidence());
		assertEquals(e1.getReversedEdge(), nodes[1].getLastIncidence());
		assertEquals(1, nodes[0].getDegree());
		assertEquals(1, nodes[0].getDegree(EdgeDirection.INOUT));
		assertEquals(1, nodes[0].getDegree(EdgeDirection.OUT));
		assertEquals(0, nodes[0].getDegree(EdgeDirection.IN));
		assertEquals(1, nodes[1].getDegree());
		assertEquals(1, nodes[1].getDegree(EdgeDirection.INOUT));
		assertEquals(0, nodes[1].getDegree(EdgeDirection.OUT));
		assertEquals(1, nodes[1].getDegree(EdgeDirection.IN));
		assertEquals("e1", getISeq(nodes[0]));
		assertEquals("e-1", getISeq(nodes[1]));

		// loop
		Edge e2 = g.createLink(nodes[0], nodes[0]);

		assertEquals(e1, nodes[0].getFirstIncidence());
		assertEquals(e2.getReversedEdge(), nodes[0].getLastIncidence());
		assertEquals(e1.getReversedEdge(), nodes[1].getFirstIncidence());
		assertEquals(e1.getReversedEdge(), nodes[1].getLastIncidence());
		assertEquals(3, nodes[0].getDegree());
		assertEquals(3, nodes[0].getDegree(EdgeDirection.INOUT));
		assertEquals(2, nodes[0].getDegree(EdgeDirection.OUT));
		assertEquals(1, nodes[0].getDegree(EdgeDirection.IN));
		assertEquals("e1 e2 e-2", getISeq(nodes[0]));
		assertEquals("e-1", getISeq(nodes[1]));

		Edge e3 = g.createLink(nodes[2], nodes[0]);

		assertEquals(e3, nodes[2].getFirstIncidence());
		assertEquals(e3.getReversedEdge(), nodes[0].getLastIncidence());
		assertEquals(4, nodes[0].getDegree());
		assertEquals(4, nodes[0].getDegree(EdgeDirection.INOUT));
		assertEquals(2, nodes[0].getDegree(EdgeDirection.OUT));
		assertEquals(2, nodes[0].getDegree(EdgeDirection.IN));
		assertEquals(1, nodes[2].getDegree());
		assertEquals(1, nodes[2].getDegree(EdgeDirection.INOUT));
		assertEquals(1, nodes[2].getDegree(EdgeDirection.OUT));
		assertEquals(0, nodes[2].getDegree(EdgeDirection.IN));
		assertEquals("e1 e2 e-2 e-3", getISeq(nodes[0]));
		assertEquals("e-1", getISeq(nodes[1]));
		assertEquals("e3", getISeq(nodes[2]));

		Edge e4 = g.createLink(nodes[0], nodes[N - 1]);

		assertEquals(e4.getReversedEdge(), nodes[N - 1].getFirstIncidence());
		assertEquals(e4.getReversedEdge(), nodes[N - 1].getLastIncidence());
		assertEquals(e4, nodes[0].getLastIncidence());
		assertEquals(5, nodes[0].getDegree());
		assertEquals(5, nodes[0].getDegree(EdgeDirection.INOUT));
		assertEquals(3, nodes[0].getDegree(EdgeDirection.OUT));
		assertEquals(2, nodes[0].getDegree(EdgeDirection.IN));
		assertEquals(1, nodes[N - 1].getDegree());
		assertEquals(1, nodes[N - 1].getDegree(EdgeDirection.INOUT));
		assertEquals(0, nodes[N - 1].getDegree(EdgeDirection.OUT));
		assertEquals(1, nodes[N - 1].getDegree(EdgeDirection.IN));
		assertEquals("e1 e2 e-2 e-3 e4", getISeq(nodes[0]));
		assertEquals("e-1", getISeq(nodes[1]));
		assertEquals("e3", getISeq(nodes[2]));
		assertEquals("e-4", getISeq(nodes[N - 1]));
	}

	private String getISeq(Vertex v) {
		StringBuilder sb = new StringBuilder();
		for (Edge e : v.incidences()) {
			sb.append('e').append(e.getId()).append(' ');
		}
		return sb.toString().trim();
	}

	@Test
	public void putEdgeBeforeTest() throws Exception {
		createRandomEdges();

		// at each vertex, shuffle edges

		List<Vertex> vertexList = getVertexList();

		for (Vertex v : vertexList) {
			List<Edge> il = getIncidenceList(v);
			if (il.size() < 2) {
				continue;
			}
			int runAmount = il.size() * 5;
			for (int i = 0; i < runAmount; ++i) {
				int a = rnd.nextInt(il.size());
				int b = rnd.nextInt(il.size());
				while (b == a) {
					b = rnd.nextInt(il.size());
				}
				Edge ea = il.get(a);
				Edge eb = il.get(b);
				// System.out.print(getISeq(v) + " " + ea.getId() + "
				// before " + eb.getId());
				ea.putIncidenceBefore(eb);
				// System.out.println(" => " + getISeq(v));
				// System.out.println(il);
				il.remove(a);
				il.add(a < b ? b - 1 : b, ea);
				checkIncidenceList(v, il);
			}
		}
	}

	private List<Vertex> getVertexList() {
		List<Vertex> vertexList = new LinkedList<>();
		for (Vertex v : g.vertices()) {
			vertexList.add(v);
		}
		return vertexList;
	}

	@Test
	public void putEdgeAfterTest() throws Exception {
		createRandomEdges();
		// at each vertex, shuffle edges 1000 times
		List<Vertex> vertexList = getVertexList();
		for (Vertex v : vertexList) {
			List<Edge> il = getIncidenceList(v);
			if (il.size() < 2) {
				continue;
			}
			for (int i = 0; i < il.size() * 5; ++i) {
				int a = rnd.nextInt(il.size());
				int b = rnd.nextInt(il.size());
				while (b == a) {
					b = rnd.nextInt(il.size());
				}
				Edge ea = il.get(a);
				Edge eb = il.get(b);
				// System.out.print(getISeq(v) + " " + ea.getId() + "
				// after "
				// + eb.getId());
				ea.putIncidenceAfter(eb);
				// System.out.println(" => " + getISeq(v));
				// System.out.println(il);
				il.remove(a);
				il.add(a < b ? b : b + 1, ea);
				checkIncidenceList(v, il);
			}
		}
	}

	private List<Edge> getIncidenceList(Vertex n) {
		List<Edge> il = new ArrayList<>();
		for (Edge e : n.incidences()) {
			il.add(e);
		}
		checkIncidenceList(n, il);
		return il;
	}

	private void createRandomEdges() {
		for (int i = 1; i < EDGES_PER_NODE * N; ++i) {
			g.createLink(nodes[rnd.nextInt(N)], nodes[rnd.nextInt(N)]);
		}
	}

	private void checkIncidenceList(Vertex v, List<Edge> expectedIincidences) {
		assertEquals(expectedIincidences.size(), v.getDegree());

		// check forward pointers
		{
			Edge e = v.getFirstIncidence();
			for (int i = 0; i < expectedIincidences.size(); ++i) {
				assertNotNull(e);
				assertEquals(expectedIincidences.get(i), e);
				e = e.getNextIncidence();
			}
			assertNull(e);
		}

		// check backward pointers
		{
			Edge e = v.getLastIncidence();
			for (int i = expectedIincidences.size() - 1; i >= 0; --i) {
				assertNotNull(e);
				assertEquals(expectedIincidences.get(i), e);
				e = e.getPrevIncidence();
			}
			assertNull(e);
		}
	}

	@Test
	public void deleteEdgeTest() throws Exception {
		// onlyTestWithoutTransactionSupport();
		// TODO find out why this test takes so long
		createRandomEdges();
		int eCount = g.getECount();

		for (Vertex v : nodes) {
			List<Edge> incidenceList = getIncidenceList(v);

			// calculate the in/out degree of that node
			// TODO why not use internal degree method?
			int inDegree = 0, outDegree = 0;
			for (Edge e : incidenceList) {
				if (e.isNormal()) {
					outDegree++;
				} else {
					inDegree++;
				}
			}

			while (!incidenceList.isEmpty()) {
				// choose a random incident edge and remove it from the list of
				// incidences
				Edge edgeToDelete = incidenceList.remove(rnd
						.nextInt(incidenceList.size()));
				// if the edge is a loop, we have to remove the other end, too.
				if (edgeToDelete.isNormal()) {
					outDegree--;
					if (edgeToDelete.getAlpha() == edgeToDelete.getOmega()) {
						incidenceList.remove(edgeToDelete.getReversedEdge());
						inDegree--;
					}
				} else {
					inDegree--;
					if (edgeToDelete.getAlpha() == edgeToDelete.getOmega()) {
						incidenceList.remove(edgeToDelete.getNormalEdge());
						outDegree--;
					}
				}
				// now delete it
				edgeToDelete.delete();
				eCount--;

				// The graph must not contain that edge anymore
				assertFalse(g.containsEdge(edgeToDelete));
				// The edge count must be adjusted
				assertEquals(eCount, g.getECount());
				// Check the degree of the current vertex
				assertEquals(incidenceList.size(), v.getDegree());
				// this just tests the test's degree calculation (paranoia is
				// good)
				assertEquals(incidenceList.size(), inDegree + outDegree);
				assertEquals(inDegree, v.getDegree(EdgeDirection.IN));
				assertEquals(outDegree, v.getDegree(EdgeDirection.OUT));
				// The incidences should have the same order
				assertEquals(incidenceList, getIncidenceList(v));
			}
		}
	}

	@Test
	public void testSortIncidences() {
		Node[] nodes = new Node[NODE_COUNT];

		Node isolated = g.createNode();

		final GraphMarker<Integer> marker = new GraphMarker<>(g);
		Comparator<Edge> comp = new Comparator<Edge>() {

			@Override
			public int compare(Edge o1, Edge o2) {
				Integer mark1 = marker.getMark(o1);
				Integer mark2 = marker.getMark(o2);
				if ((mark1 == null) && (mark2 == null)) {
					return 0;
				}
				if (mark1 == null) {
					return -1;
				}
				if (mark2 == null) {
					return 1;
				}
				return Double.compare(mark1.doubleValue(), mark2.doubleValue());
			}
		};

		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = g.createNode();
		}
		// create edges from node 0 to all others
		List<Link> links = new ArrayList<>();
		for (int i = 1; i < nodes.length; i++) {
			links.add(g.createLink(nodes[0], nodes[i]));
		}

		// test if list is not modified if sorting is called when they are
		// already sorted
		markInOrder(links, marker);

		long version = ((InternalVertex) nodes[0]).getIncidenceListVersion();
		nodes[0].sortIncidences(comp);

		assertEquals(version,
				((InternalVertex) nodes[0]).getIncidenceListVersion());
		checkInOrder(nodes, links);

		// test if sorting works if order is reversed
		markInverse(links, marker);
		nodes[0].sortIncidences(comp);

		// assertTrue(version < nodes[0].getIncidenceListVersion());
		checkInverse(nodes, links);

		// reset state and check if it is correct
		markInOrder(links, marker);
		version = ((InternalVertex) nodes[0]).getIncidenceListVersion();
		nodes[0].sortIncidences(comp);

		assertTrue(version < ((InternalVertex) nodes[0])
				.getIncidenceListVersion());
		checkInOrder(nodes, links);

		// random tests
		for (int i = 0; i < RANDOM_TEST_AMOUNT; i++) {
			List<Link> randomOrder = copyAndMix(links);
			markInOrder(randomOrder, marker);
			nodes[0].sortIncidences(comp);

			checkInOrder(nodes, randomOrder);
		}

		// check if the sorting succeeds if the vertex is isolated
		isolated.sortIncidences(comp);
	}

	private List<Link> copyAndMix(List<Link> original) {
		List<Link> copy = new ArrayList<>(original.size());
		RandomBufferGeneric<Link> mixer = new RandomBufferGeneric<>(
				original.size());
		for (Link current : original) {
			mixer.put(current);
		}
		while (!mixer.isEmpty()) {
			copy.add(mixer.getNext());
		}
		return copy;
	}

	private void checkInOrder(Node[] nodes, List<Link> links) {
		int i = 0;
		for (Edge current : nodes[0].incidences()) {
			assertEquals(links.get(i), current);
			i++;
		}
	}

	private void checkInverse(Node[] nodes, List<Link> links) {
		int size = links.size();
		int i = 0;
		for (Edge current : nodes[0].incidences()) {
			assertEquals(links.get(size - (i + 1)), current);
			i++;
		}
	}

	private void markInOrder(List<Link> links, final GraphMarker<Integer> marker) {
		for (int i = 0; i < links.size(); i++) {
			marker.mark(links.get(i), i);
		}
	}

	private void markInverse(List<Link> links, final GraphMarker<Integer> marker) {
		for (int i = 0; i < links.size(); i++) {
			marker.mark(links.get(i), links.size() - i);
		}
	}

}
