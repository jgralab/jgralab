package de.uni_koblenz.jgralabtest.instancetest;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralabtest.schemas.minimal.Link;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.minimal.Node;
import de.uni_koblenz.jgralabtest.tools.RandomBufferGeneric;

@RunWith(Parameterized.class)
public class IncidenceListTest extends InstanceTest {

	private static final int RANDOM_TEST_AMOUNT = 100;
	private static final int NODE_COUNT = 10000;

	public IncidenceListTest(boolean transactionsEnabled) {
		super(transactionsEnabled);
	}

	@Parameters
	public static Collection<Object[]> configure() {
		return getParameters();
	}

	final int V = 4; // initial max vertex count
	final int E = 4; // initial max edge count
	final int N = 100; // created vertex count

	MinimalGraph g;
	Node[] nodes;
	private Random rnd;

	@Before
	public void setup() throws CommitFailedException {
		rnd = new Random(System.currentTimeMillis());
		g = transactionsEnabled ? MinimalSchema.instance()
				.createMinimalGraphWithTransactionSupport(V, E) : MinimalSchema
				.instance().createMinimalGraph(V, E);
		nodes = new Node[N];

		createTransaction(g);
		for (int i = 0; i < N; ++i) {
			nodes[i] = g.createNode();
		}
		commit(g);
	}

	@Test
	public void addEdgeTest() throws Exception {
		onlyTestWithoutTransactionSupport();
		// incidence lists must initially be empty
		for (int i = 0; i < N; ++i) {
			assertEquals(0, nodes[i].getDegree());
			Assert.assertNull(nodes[i].getFirstEdge());
		}

		Edge e1 = g.createLink(nodes[0], nodes[1]);
		assertEquals(e1, nodes[0].getFirstEdge());
		assertEquals(e1, nodes[0].getLastEdge());
		assertEquals(e1.getReversedEdge(), nodes[1].getFirstEdge());
		assertEquals(e1.getReversedEdge(), nodes[1].getFirstEdge());
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
		assertEquals(e1, nodes[0].getFirstEdge());
		assertEquals(e2.getReversedEdge(), nodes[0].getLastEdge());
		assertEquals(e1.getReversedEdge(), nodes[1].getFirstEdge());
		assertEquals(e1.getReversedEdge(), nodes[1].getFirstEdge());
		assertEquals(3, nodes[0].getDegree());
		assertEquals(3, nodes[0].getDegree(EdgeDirection.INOUT));
		assertEquals(2, nodes[0].getDegree(EdgeDirection.OUT));
		assertEquals(1, nodes[0].getDegree(EdgeDirection.IN));
		assertEquals("e1 e2 e-2", getISeq(nodes[0]));
		assertEquals("e-1", getISeq(nodes[1]));

		Edge e3 = g.createLink(nodes[2], nodes[0]);
		assertEquals(e3, nodes[2].getFirstEdge());
		assertEquals(e3.getReversedEdge(), nodes[0].getLastEdge());
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
		assertEquals(e4.getReversedEdge(), nodes[N - 1].getFirstEdge());
		assertEquals(e4.getReversedEdge(), nodes[N - 1].getLastEdge());
		assertEquals(e4, nodes[0].getLastEdge());
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
		onlyTestWithoutTransactionSupport();
		createRandomEdges();
		// at each vertex, shuffle edges
		for (Vertex v : g.vertices()) {
			List<Edge> il = getIncidenceList(v);
			if (il.size() < 2) {
				continue;
			}
			for (int i = 0; i < il.size() * 100; ++i) {
				int a = rnd.nextInt(il.size());
				int b = rnd.nextInt(il.size());
				while (b == a) {
					b = rnd.nextInt(il.size());
				}
				Edge ea = il.get(a);
				Edge eb = il.get(b);
				// System.out.print(getISeq(v) + " " + ea.getId() + "
				// before " + eb.getId());
				ea.putEdgeBefore(eb);
				// System.out.println(" => " + getISeq(v));
				// System.out.println(il);
				il.remove(a);
				il.add(a < b ? b - 1 : b, ea);
				checkIncidenceList(v, il);
			}
		}
	}

	@Test(expected = GraphException.class)
	public void putEdgeBeforeSelf() throws Exception {
		onlyTestWithoutTransactionSupport();
		Edge e = g.createLink(nodes[0], nodes[1]);
		e.putEdgeBefore(e);
	}

	@Test(expected = GraphException.class)
	public void putEdgeAfterSelf() throws Exception {
		onlyTestWithoutTransactionSupport();
		Edge e = g.createLink(nodes[0], nodes[1]);
		e.putEdgeAfter(e);
	}

	@Test(expected = GraphException.class)
	public void putEdgeAfterDifferentThis() throws Exception {
		onlyTestWithoutTransactionSupport();
		Edge e = g.createLink(nodes[0], nodes[1]);
		e.putEdgeAfter(e.getReversedEdge());
	}

	@Test(expected = GraphException.class)
	public void putEdgeBeforeDifferentThis() throws Exception {
		onlyTestWithoutTransactionSupport();
		Edge e = g.createLink(nodes[0], nodes[1]);
		e.putEdgeBefore(e.getReversedEdge());
	}

	@Test
	public void putEdgeAfterTest() throws Exception {
		onlyTestWithoutTransactionSupport();
		createRandomEdges();
		// at each vertex, shuffle edges 1000 times
		for (Vertex v : g.vertices()) {
			List<Edge> il = getIncidenceList(v);
			if (il.size() < 2) {
				continue;
			}
			for (int i = 0; i < il.size() * 100; ++i) {
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
				ea.putEdgeAfter(eb);
				// System.out.println(" => " + getISeq(v));
				// System.out.println(il);
				il.remove(a);
				il.add(a < b ? b : b + 1, ea);
				checkIncidenceList(v, il);
			}
		}
	}

	private List<Edge> getIncidenceList(Vertex n) {
		List<Edge> il = new ArrayList<Edge>();
		for (Edge e : n.incidences()) {
			il.add(e);
		}
		checkIncidenceList(n, il);
		return il;
	}

	private void createRandomEdges() {
		for (int i = 1; i < 20 * N; ++i) {
			g.createLink(nodes[rnd.nextInt(N)], nodes[rnd.nextInt(N)]);
		}
	}

	private void checkIncidenceList(Vertex v, List<Edge> expectedIincidences) {
		assertEquals(expectedIincidences.size(), v.getDegree());

		// check forward pointers
		{
			Edge e = v.getFirstEdge();
			for (int i = 0; i < expectedIincidences.size(); ++i) {
				assertNotNull(e);
				assertEquals(expectedIincidences.get(i), e);
				e = e.getNextEdge();
			}
			assertNull(e);
		}

		// check backward pointers
		{
			Edge e = v.getLastEdge();
			for (int i = expectedIincidences.size() - 1; i >= 0; --i) {
				assertNotNull(e);
				assertEquals(expectedIincidences.get(i), e);
				e = e.getPrevEdge();
			}
			assertNull(e);
		}
	}

	@Test
	public void deleteEdgeTest() throws Exception {
		onlyTestWithoutTransactionSupport();
		createRandomEdges();

		int eCount = g.getECount();

		for (Vertex v : nodes) {
			List<Edge> incidenceList = getIncidenceList(v);

			// calculate the in/out degree of that node
			int inDegree = 0, outDegree = 0;
			for (Edge e : incidenceList) {
				if (e.isNormal()) {
					outDegree++;
				} else {
					inDegree++;
				}
			}
			Edge edgeToDelete;
			while (!incidenceList.isEmpty()) {
				// choose a random incident edge and remove it from the list of
				// incidences
				edgeToDelete = incidenceList.remove(rnd.nextInt(incidenceList
						.size()));
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
		onlyTestWithoutTransactionSupport();
		MinimalGraph g = transactionsEnabled ? MinimalSchema.instance()
				.createMinimalGraphWithTransactionSupport(V, E) : MinimalSchema
				.instance().createMinimalGraph(V, E);
		Node[] nodes = new Node[NODE_COUNT];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = g.createNode();
		}

		// create edges from node 0 to all others
		List<Link> links = new ArrayList<Link>();
		for (int i = 1; i < nodes.length; i++) {
			links.add(g.createLink(nodes[0], nodes[i]));
		}

		final GraphMarker<Integer> marker = new GraphMarker<Integer>(g);
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

		// test if list is not modified if sorting is called when they are
		// already sorted
		markInOrder(links, marker);
		long version = nodes[0].getIncidenceListVersion();
		nodes[0].sortIncidences(comp);
		assertEquals(version, nodes[0].getIncidenceListVersion());
		checkInOrder(nodes, links);

		// test if sorting works if order is reversed
		markInverse(links, marker);
		nodes[0].sortIncidences(comp);
		assertTrue(version < nodes[0].getIncidenceListVersion());
		checkInverse(nodes, links);

		// reset state and check if it is correct
		markInOrder(links, marker);
		version = nodes[0].getIncidenceListVersion();
		nodes[0].sortIncidences(comp);
		assertTrue(version < nodes[0].getIncidenceListVersion());
		checkInOrder(nodes, links);

		// random tests
		for (int i = 0; i < RANDOM_TEST_AMOUNT; i++) {
			List<Link> randomOrder = copyAndMix(links);
			markInOrder(randomOrder, marker);
			nodes[0].sortIncidences(comp);
			checkInOrder(nodes, randomOrder);
		}

	}

	private List<Link> copyAndMix(List<Link> original) {
		List<Link> copy = new ArrayList<Link>(original.size());
		RandomBufferGeneric<Link> mixer = new RandomBufferGeneric<Link>(
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
