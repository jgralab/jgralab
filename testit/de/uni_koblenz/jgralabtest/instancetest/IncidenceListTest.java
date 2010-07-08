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
import de.uni_koblenz.jgralab.trans.CommitFailedException;
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

	public IncidenceListTest(ImplementationType implementationType) {
		super(implementationType);
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
	public void setup() throws CommitFailedException {
		rnd = new Random(System.currentTimeMillis());
		switch (implementationType) {
		case STANDARD:
			g = MinimalSchema.instance().createMinimalGraph(V, E);
			break;
		case TRANSACTION:
			g = MinimalSchema.instance()
					.createMinimalGraphWithTransactionSupport(V, E);
			break;
		case SAVEMEM:
			g = MinimalSchema.instance().createMinimalGraphWithSavememSupport(
					V, E);
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}

		nodes = new Node[N];

		createTransaction(g);
		for (int i = 0; i < N; ++i) {
			nodes[i] = g.createNode();
		}
		commit(g);
	}

	@Test
	public void addEdgeTest() throws Exception {
		// incidence lists must initially be empty
		createReadOnlyTransaction(g);
		for (int i = 0; i < N; ++i) {
			assertEquals(0, nodes[i].getDegree());
			Assert.assertNull(nodes[i].getFirstEdge());
		}
		commit(g);

		createTransaction(g);
		Edge e1 = g.createLink(nodes[0], nodes[1]);
		commit(g);

		createReadOnlyTransaction(g);
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
		commit(g);

		// loop
		createTransaction(g);
		Edge e2 = g.createLink(nodes[0], nodes[0]);
		commit(g);

		createReadOnlyTransaction(g);
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
		commit(g);

		createTransaction(g);
		Edge e3 = g.createLink(nodes[2], nodes[0]);
		commit(g);

		createReadOnlyTransaction(g);
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
		commit(g);

		createTransaction(g);
		Edge e4 = g.createLink(nodes[0], nodes[N - 1]);
		commit(g);

		createReadOnlyTransaction(g);
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
		commit(g);
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
		// TODO remove when problem is resolved
		// if(implementationType == ImplementationType.SAVEMEM){
		// fail("testcase creates an infinite loop.");
		// }
		createTransaction(g);
		createRandomEdges();
		commit(g);

		// at each vertex, shuffle edges

		createReadOnlyTransaction(g);
		List<Vertex> vertexList = getVertexList();
		commit(g);

		for (Vertex v : vertexList) {
			createReadOnlyTransaction(g);
			List<Edge> il = getIncidenceList(v);
			commit(g);
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
				createTransaction(g);
				ea.putEdgeBefore(eb);
				commit(g);
				// System.out.println(" => " + getISeq(v));
				// System.out.println(il);
				il.remove(a);
				il.add(a < b ? b - 1 : b, ea);
				createReadOnlyTransaction(g);
				checkIncidenceList(v, il);
				commit(g);
			}
		}
	}

	private List<Vertex> getVertexList() {
		List<Vertex> vertexList = new LinkedList<Vertex>();
		for (Vertex v : g.vertices()) {
			vertexList.add(v);
		}
		return vertexList;
	}

	@Test
	public void putEdgeAfterTest() throws Exception {
		createTransaction(g);
		createRandomEdges();
		commit(g);
		// at each vertex, shuffle edges 1000 times
		createReadOnlyTransaction(g);
		List<Vertex> vertexList = getVertexList();
		commit(g);
		for (Vertex v : vertexList) {
			createReadOnlyTransaction(g);
			List<Edge> il = getIncidenceList(v);
			commit(g);
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
				createTransaction(g);
				ea.putEdgeAfter(eb);
				commit(g);
				// System.out.println(" => " + getISeq(v));
				// System.out.println(il);
				il.remove(a);
				il.add(a < b ? b : b + 1, ea);
				createReadOnlyTransaction(g);
				checkIncidenceList(v, il);
				commit(g);
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
		for (int i = 1; i < EDGES_PER_NODE * N; ++i) {
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
		// onlyTestWithoutTransactionSupport();
		// TODO find out why this test takes so long
		createTransaction(g);
		createRandomEdges();
		commit(g);
		createReadOnlyTransaction(g);
		int eCount = g.getECount();
		commit(g);

		for (Vertex v : nodes) {
			createReadOnlyTransaction(g);
			List<Edge> incidenceList = getIncidenceList(v);
			commit(g);

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
				createTransaction(g);
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
				commit(g);
				// now delete it
				createTransaction(g);
				edgeToDelete.delete();
				commit(g);
				eCount--;

				createReadOnlyTransaction(g);
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
				commit(g);
			}
		}
	}

	@Test
	public void testSortIncidences() throws CommitFailedException {
		MinimalGraph g = null;
		switch (implementationType) {
		case STANDARD:
			g = MinimalSchema.instance().createMinimalGraph(V, E);
			break;
		case TRANSACTION:
			g = MinimalSchema.instance()
					.createMinimalGraphWithTransactionSupport(V, E);
			break;
		case SAVEMEM:
			g = MinimalSchema.instance().createMinimalGraphWithSavememSupport(
					V, E);
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}

		Node[] nodes = new Node[NODE_COUNT];

		createTransaction(g);
		Node isolated = g.createNode();
		commit(g);

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

		if (implementationType == ImplementationType.TRANSACTION) {
			try {
				isolated.sortIncidences(comp);
				fail();
			} catch (UnsupportedOperationException e) {
				// as expected
			}
		} else {
			createTransaction(g);
			for (int i = 0; i < nodes.length; i++) {
				nodes[i] = g.createNode();
			}
			commit(g);
			// create edges from node 0 to all others
			List<Link> links = new ArrayList<Link>();
			createTransaction(g);
			for (int i = 1; i < nodes.length; i++) {
				links.add(g.createLink(nodes[0], nodes[i]));
			}
			commit(g);

			// test if list is not modified if sorting is called when they are
			// already sorted
			markInOrder(links, marker);

			createTransaction(g);
			long version = nodes[0].getIncidenceListVersion();
			nodes[0].sortIncidences(comp);
			commit(g);

			createReadOnlyTransaction(g);
			assertEquals(version, nodes[0].getIncidenceListVersion());
			checkInOrder(nodes, links);
			commit(g);

			// test if sorting works if order is reversed
			markInverse(links, marker);
			createTransaction(g);
			nodes[0].sortIncidences(comp);
			commit(g);

			createReadOnlyTransaction(g);
			// assertTrue(version < nodes[0].getIncidenceListVersion());
			checkInverse(nodes, links);
			commit(g);

			// reset state and check if it is correct
			markInOrder(links, marker);
			createTransaction(g);
			version = nodes[0].getIncidenceListVersion();
			nodes[0].sortIncidences(comp);
			commit(g);

			createReadOnlyTransaction(g);
			assertTrue(version < nodes[0].getIncidenceListVersion());
			checkInOrder(nodes, links);
			commit(g);

			// random tests
			for (int i = 0; i < RANDOM_TEST_AMOUNT; i++) {
				List<Link> randomOrder = copyAndMix(links);
				markInOrder(randomOrder, marker);
				createTransaction(g);
				nodes[0].sortIncidences(comp);
				commit(g);

				createReadOnlyTransaction(g);
				checkInOrder(nodes, randomOrder);
				commit(g);
			}

			// check if the sorting succeeds if the vertex is isolated
			isolated.sortIncidences(comp);
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
