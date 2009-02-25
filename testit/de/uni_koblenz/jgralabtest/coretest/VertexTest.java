package de.uni_koblenz.jgralabtest.coretest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralabtest.schemas.vertextest.AbstractSuperNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.DoubleSubNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.Link;
import de.uni_koblenz.jgralabtest.schemas.vertextest.LinkBack;
import de.uni_koblenz.jgralabtest.schemas.vertextest.SubLink;
import de.uni_koblenz.jgralabtest.schemas.vertextest.SubNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.SuperNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestGraph;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestSchema;

public class VertexTest {

	private VertexTestGraph graph;
	private Random rand;

	/**
	 * Creates a new graph for each test.
	 */
	@Before
	public void setUp() {
		graph = VertexTestSchema.instance().createVertexTestGraph(100, 100);
		rand = new Random(System.currentTimeMillis());
	}

	/*
	 * Test of the Interface Vertex
	 */

	// tests of the method isIncidenceListModified(long incidenceListVersion);
	/**
	 * Tests if the incidenceList wasn't modified.
	 */
	@Test
	public void isIncidenceListModifiedTest0() {
		AbstractSuperNode asn = graph.createSubNode();
		SuperNode sn = graph.createSuperNode();
		DoubleSubNode dsn = graph.createDoubleSubNode();
		long asnIncidenceListVersion = asn.getIncidenceListVersion();
		long snIncidenceListVersion = sn.getIncidenceListVersion();
		long dsnIncidenceListVersion = dsn.getIncidenceListVersion();
		assertFalse(asn.isIncidenceListModified(asnIncidenceListVersion));
		assertFalse(sn.isIncidenceListModified(snIncidenceListVersion));
		assertFalse(dsn.isIncidenceListModified(dsnIncidenceListVersion));
	}

	/**
	 * If you create and delete edges, only the incidenceLists of the involved
	 * nodes may have been modified.
	 */
	@Test
	public void isIncidenceListModifiedTest1() {
		Vertex[] nodes = new Vertex[3];
		long[] versions = new long[3];
		nodes[0] = graph.createSubNode();
		versions[0] = nodes[0].getIncidenceListVersion();
		nodes[1] = graph.createDoubleSubNode();
		versions[1] = nodes[1].getIncidenceListVersion();
		nodes[2] = graph.createSuperNode();
		versions[2] = nodes[2].getIncidenceListVersion();
		for (int i = 0; i < 1000; i++) {
			int start = rand.nextInt(2);
			int end = rand.nextInt(2) + 1;
			// create a new edge
			Link sl = graph.createLink((AbstractSuperNode) nodes[start],
					(SuperNode) nodes[end]);
			assertTrue(nodes[start].isIncidenceListModified(versions[start]));
			assertTrue(nodes[end].isIncidenceListModified(versions[end]));
			if (start != end) {
				assertFalse(nodes[6 - (start + 1) - (end + 1) - 1]
						.isIncidenceListModified(versions[6 - (start + 1)
								- (end + 1) - 1]));
			} else {
				for (int j = 0; j < 3; j++) {
					if (j != start) {
						assertFalse(nodes[j]
								.isIncidenceListModified(versions[j]));
					}
				}
			}
			// update of versions
			versions[0] = nodes[0].getIncidenceListVersion();
			versions[1] = nodes[1].getIncidenceListVersion();
			versions[2] = nodes[2].getIncidenceListVersion();
			// delete an edge
			graph.deleteEdge(sl);
			assertTrue(nodes[start].isIncidenceListModified(versions[start]));
			assertTrue(nodes[end].isIncidenceListModified(versions[end]));
			if (start != end) {
				assertFalse(nodes[6 - (start + 1) - (end + 1) - 1]
						.isIncidenceListModified(versions[6 - (start + 1)
								- (end + 1) - 1]));
			} else {
				for (int j = 0; j < 3; j++) {
					if (j != start) {
						assertFalse(nodes[j]
								.isIncidenceListModified(versions[j]));
					}
				}
			}
			// update of versions
			versions[0] = nodes[0].getIncidenceListVersion();
			versions[1] = nodes[1].getIncidenceListVersion();
			versions[2] = nodes[2].getIncidenceListVersion();
		}
	}

	// tests of the method getIncidenceListVersion()
	/**
	 * If you create and delete edges, only the incidenceListVersions of the
	 * involved nodes may have been increased.
	 */
	@Test
	public void getIncidenceListVersionTest0() {
		Vertex[] nodes = new Vertex[3];
		nodes[0] = graph.createSubNode();
		nodes[1] = graph.createDoubleSubNode();
		nodes[2] = graph.createSuperNode();
		long[] expectedVersions = new long[] { 0, 0, 0 };
		for (int i = 0; i < 1000; i++) {
			int start = rand.nextInt(2);
			int end = rand.nextInt(2) + 1;
			// create a new edge
			Link sl = graph.createLink((AbstractSuperNode) nodes[start],
					(SuperNode) nodes[end]);
			expectedVersions[start]++;
			expectedVersions[end]++;
			assertEquals(expectedVersions[0], nodes[0]
					.getIncidenceListVersion());
			assertEquals(expectedVersions[1], nodes[1]
					.getIncidenceListVersion());
			assertEquals(expectedVersions[2], nodes[2]
					.getIncidenceListVersion());
			// delete an edge
			graph.deleteEdge(sl);
			expectedVersions[start]++;
			expectedVersions[end]++;
			assertEquals(expectedVersions[0], nodes[0]
					.getIncidenceListVersion());
			assertEquals(expectedVersions[1], nodes[1]
					.getIncidenceListVersion());
			assertEquals(expectedVersions[2], nodes[2]
					.getIncidenceListVersion());
		}
	}

	// tests of the method getId()

	/**
	 * If you create several vertices and you delete one, the next vertex should
	 * get the id of the deleted vertex. If you create a further vertex it
	 * should get the next free id.
	 */
	@Test
	public void getIdTest0() {
		Vertex v = graph.createDoubleSubNode();
		assertEquals(1, v.getId());
		Vertex w = graph.createDoubleSubNode();
		assertEquals(2, w.getId());
		graph.deleteVertex(v);
		v = graph.createDoubleSubNode();
		assertEquals(1, v.getId());
		v = graph.createDoubleSubNode();
		assertEquals(3, v.getId());
	}

	// tests of the method getDegree()

	/**
	 * A vertex with no connected incidences has to have a degree of 0.
	 */
	@Test
	public void getDegreeTest0() {
		Vertex v = graph.createDoubleSubNode();
		assertEquals(0, v.getDegree());
	}

	/**
	 * Checks the degrees in a manually build graph.
	 */
	@Test
	public void getDegreeTest1() {
		SubNode subn = graph.createSubNode();
		DoubleSubNode dsubn = graph.createDoubleSubNode();
		DoubleSubNode dsubnWithout = graph.createDoubleSubNode();
		SuperNode supern = graph.createSuperNode();
		graph.createLink(subn, supern);
		graph.createLink(dsubn, dsubn);
		graph.createLinkBack(supern, dsubn);
		graph.createLinkBack(dsubn, subn);
		assertEquals(0, dsubnWithout.getDegree());
		assertEquals(2, subn.getDegree());
		assertEquals(2, supern.getDegree());
		assertEquals(4, dsubn.getDegree());
	}

	/**
	 * Generates a number of edges and checks the correct degrees of the
	 * vertices. After that it deletes the edges and checks the degrees again.
	 */
	@Test
	public void getDegreeTest2() {
		Vertex[] nodes = new Vertex[3];
		nodes[0] = graph.createSubNode();
		nodes[1] = graph.createDoubleSubNode();
		nodes[2] = graph.createSuperNode();
		int[] expectedDegrees = new int[] { 0, 0, 0 };
		// create new edges
		for (int i = 0; i < 1000; i++) {
			int start = rand.nextInt(2);
			int end = rand.nextInt(2) + 1;
			graph.createLink((AbstractSuperNode) nodes[start],
					(SuperNode) nodes[end]);
			expectedDegrees[start]++;
			expectedDegrees[end]++;
			assertEquals(expectedDegrees[0], nodes[0].getDegree());
			assertEquals(expectedDegrees[1], nodes[1].getDegree());
			assertEquals(expectedDegrees[2], nodes[2].getDegree());
		}
		HashMap<Vertex, Integer> vertices = new HashMap<Vertex, Integer>();
		vertices.put(nodes[0], expectedDegrees[0]);
		vertices.put(nodes[1], expectedDegrees[1]);
		vertices.put(nodes[2], expectedDegrees[2]);
		// delete the edges
		for (Link l : graph.getLinkEdges()) {
			Vertex start = l.getAlpha();
			vertices.put(start, vertices.get(start) - 1);
			Vertex end = l.getOmega();
			vertices.put(end, vertices.get(end) - 1);
			graph.deleteEdge(l);
			if (start != end) {
				assertEquals(vertices.get(start).intValue(), start.getDegree());
				assertEquals(vertices.get(end).intValue(), end.getDegree());
			} else {
				assertEquals(vertices.get(start).intValue(), start.getDegree());
			}
		}
	}

	// tests of the method getDegree(EdgeDirection orientation)

	/**
	 * A vertex with no connected incidences has to have a degree of 0 for each
	 * EdgeDirection.
	 */
	@Test
	public void getDegreeTestEdgeDirection0() {
		Vertex v = graph.createDoubleSubNode();
		assertEquals(0, v.getDegree(EdgeDirection.IN));
		assertEquals(0, v.getDegree(EdgeDirection.OUT));
		assertEquals(0, v.getDegree(EdgeDirection.INOUT));
	}

	/**
	 * Checks the degrees in a manually build graph.
	 */
	@Test
	public void getDegreeTestEdgeDirection1() {
		SubNode subn = graph.createSubNode();
		DoubleSubNode dsubn = graph.createDoubleSubNode();
		DoubleSubNode dsubnWithout = graph.createDoubleSubNode();
		SuperNode supern = graph.createSuperNode();
		graph.createLink(subn, supern);
		graph.createLink(dsubn, dsubn);
		graph.createLinkBack(supern, dsubn);
		graph.createLinkBack(dsubn, subn);
		assertEquals(0, dsubnWithout.getDegree(EdgeDirection.INOUT));
		assertEquals(0, dsubnWithout.getDegree(EdgeDirection.IN));
		assertEquals(0, dsubnWithout.getDegree(EdgeDirection.OUT));
		assertEquals(2, subn.getDegree(EdgeDirection.INOUT));
		assertEquals(1, subn.getDegree(EdgeDirection.IN));
		assertEquals(1, subn.getDegree(EdgeDirection.OUT));
		assertEquals(2, supern.getDegree(EdgeDirection.INOUT));
		assertEquals(1, supern.getDegree(EdgeDirection.IN));
		assertEquals(1, supern.getDegree(EdgeDirection.OUT));
		assertEquals(4, dsubn.getDegree(EdgeDirection.INOUT));
		assertEquals(2, dsubn.getDegree(EdgeDirection.IN));
		assertEquals(2, dsubn.getDegree(EdgeDirection.OUT));
	}

	/**
	 * Generates a number of different edges and checks the correct degrees of
	 * the vertices considering the different EdgeDirections. After that it
	 * deletes the edges and checks the degrees again.
	 */
	@Test
	public void getDegreeTestEdgeDirection2() {
		Vertex[] nodes = new Vertex[3];
		nodes[0] = graph.createSubNode();
		nodes[1] = graph.createDoubleSubNode();
		nodes[2] = graph.createSuperNode();
		int[] expectedInOut = new int[] { 0, 0, 0 };
		int[] expectedIn = new int[] { 0, 0, 0 };
		int[] expectedOut = new int[] { 0, 0, 0 };
		// create new edges
		for (int i = 0; i < 1000; i++) {
			// decides which edge should be created
			int edge = rand.nextInt(2);
			int start = rand.nextInt(2);
			int end = rand.nextInt(2) + 1;
			if (edge == 0) {
				// create a Link
				graph.createLink((AbstractSuperNode) nodes[start],
						(SuperNode) nodes[end]);
				expectedInOut[start]++;
				expectedOut[start]++;
				expectedInOut[end]++;
				expectedIn[end]++;
			} else {
				// create a LinkBack
				graph.createLinkBack((SuperNode) nodes[end],
						(AbstractSuperNode) nodes[start]);
				expectedInOut[end]++;
				expectedOut[end]++;
				expectedInOut[start]++;
				expectedIn[start]++;
			}
			assertEquals(expectedInOut[0], nodes[0]
					.getDegree(EdgeDirection.INOUT));
			assertEquals(expectedInOut[1], nodes[1]
					.getDegree(EdgeDirection.INOUT));
			assertEquals(expectedInOut[2], nodes[2]
					.getDegree(EdgeDirection.INOUT));
			assertEquals(expectedIn[0], nodes[0].getDegree(EdgeDirection.IN));
			assertEquals(expectedIn[1], nodes[1].getDegree(EdgeDirection.IN));
			assertEquals(expectedIn[2], nodes[2].getDegree(EdgeDirection.IN));
			assertEquals(expectedOut[0], nodes[0].getDegree(EdgeDirection.OUT));
			assertEquals(expectedOut[1], nodes[1].getDegree(EdgeDirection.OUT));
			assertEquals(expectedOut[2], nodes[2].getDegree(EdgeDirection.OUT));
		}
		// delete the edges
		HashMap<Vertex, Integer> numbers = new HashMap<Vertex, Integer>();
		numbers.put(nodes[0], 0);
		numbers.put(nodes[1], 1);
		numbers.put(nodes[2], 2);
		for (int i = graph.getFirstEdgeInGraph().getId(); i < graph.getECount(); i++) {
			Edge e = graph.getEdge(i);
			int start = numbers.get(e.getAlpha());
			int end = numbers.get(e.getOmega());
			expectedInOut[start]--;
			expectedInOut[end]--;
			expectedIn[end]--;
			expectedOut[start]--;
			graph.deleteEdge(e);
			assertEquals(expectedInOut[0], nodes[0]
					.getDegree(EdgeDirection.INOUT));
			assertEquals(expectedInOut[1], nodes[1]
					.getDegree(EdgeDirection.INOUT));
			assertEquals(expectedInOut[2], nodes[2]
					.getDegree(EdgeDirection.INOUT));
			assertEquals(expectedIn[0], nodes[0].getDegree(EdgeDirection.IN));
			assertEquals(expectedIn[1], nodes[1].getDegree(EdgeDirection.IN));
			assertEquals(expectedIn[2], nodes[2].getDegree(EdgeDirection.IN));
			assertEquals(expectedOut[0], nodes[0].getDegree(EdgeDirection.OUT));
			assertEquals(expectedOut[1], nodes[1].getDegree(EdgeDirection.OUT));
			assertEquals(expectedOut[2], nodes[2].getDegree(EdgeDirection.OUT));
		}
	}

	// tests of the method getDegree(EdgeClass ec)

	/**
	 * A vertex with no connected incidences has to have a degree of 0 for each
	 * EdgeClass.
	 */
	@Test
	public void getDegreeTestEdgeClass0() {
		Vertex v = graph.createDoubleSubNode();
		testVertexForEdgeClass(v, 0, 0, 0);
	}

	/**
	 * Checks the degrees in a manually build graph.
	 */
	@Test
	public void getDegreeTestEdgeClass1() {
		SubNode subn = graph.createSubNode();
		DoubleSubNode dsubn = graph.createDoubleSubNode();
		DoubleSubNode dsubnWithout = graph.createDoubleSubNode();
		SuperNode supern = graph.createSuperNode();
		graph.createLink(subn, supern);
		graph.createLink(dsubn, dsubn);
		graph.createLinkBack(supern, dsubn);
		graph.createLinkBack(dsubn, subn);
		graph.createSubLink(dsubn, supern);
		testVertexForEdgeClass(dsubnWithout, 0, 0, 0);
		testVertexForEdgeClass(subn, 1, 0, 1);
		testVertexForEdgeClass(dsubn, 3, 1, 2);
		testVertexForEdgeClass(supern, 2, 1, 1);
	}

	/**
	 * Generates a number of different edges and checks the correct degrees of
	 * the vertices considering the different Edgeclasses. After that it deletes
	 * the edges and checks the degrees again.
	 */
	@Test
	public void getDegreeTestEdgeClass2() {
		Vertex[] nodes = new Vertex[3];
		nodes[0] = graph.createSubNode();
		nodes[1] = graph.createDoubleSubNode();
		nodes[2] = graph.createSuperNode();
		int[] expectedLink = new int[] { 0, 0, 0 };
		int[] expectedLinkBack = new int[] { 0, 0, 0 };
		int[] expectedSubLink = new int[] { 0, 0, 0 };
		// create new edges
		for (int i = 0; i < 1000; i++) {
			// decides which edge should be created
			int edge = rand.nextInt(3);
			int start = rand.nextInt(2);
			int end = rand.nextInt(2) + 1;
			if (edge == 0) {
				// create a Link
				graph.createLink((AbstractSuperNode) nodes[start],
						(SuperNode) nodes[end]);
				expectedLink[start]++;
				expectedLink[end]++;
			} else if (edge == 1) {
				// create a LinkBack
				graph.createLinkBack((SuperNode) nodes[end],
						(AbstractSuperNode) nodes[start]);
				expectedLinkBack[start]++;
				expectedLinkBack[end]++;
			} else {
				// create a SubLink
				start = 1;
				graph.createSubLink((DoubleSubNode) nodes[start],
						(SuperNode) nodes[end]);
				expectedLink[start]++;
				expectedLink[end]++;
				expectedSubLink[start]++;
				expectedSubLink[end]++;
			}
			testVertexForEdgeClass(nodes[0], expectedLink[0],
					expectedSubLink[0], expectedLinkBack[0]);
			testVertexForEdgeClass(nodes[1], expectedLink[1],
					expectedSubLink[1], expectedLinkBack[1]);
			testVertexForEdgeClass(nodes[2], expectedLink[2],
					expectedSubLink[2], expectedLinkBack[2]);
		}
		// delete the edges
		HashMap<Vertex, Integer> numbers = new HashMap<Vertex, Integer>();
		numbers.put(nodes[0], 0);
		numbers.put(nodes[1], 1);
		numbers.put(nodes[2], 2);
		for (int i = graph.getFirstEdgeInGraph().getId(); i < graph.getECount(); i++) {
			Edge e = graph.getEdge(i);
			int start = numbers.get(e.getAlpha());
			int end = numbers.get(e.getOmega());
			if (e instanceof SubLink) {
				expectedLink[start]--;
				expectedLink[end]--;
				expectedSubLink[start]--;
				expectedSubLink[end]--;
			} else if (e instanceof LinkBack) {
				expectedLinkBack[start]--;
				expectedLinkBack[end]--;
			} else {
				expectedLink[start]--;
				expectedLink[end]--;
			}
			graph.deleteEdge(e);
			testVertexForEdgeClass(nodes[0], expectedLink[0],
					expectedSubLink[0], expectedLinkBack[0]);
			testVertexForEdgeClass(nodes[1], expectedLink[1],
					expectedSubLink[1], expectedLinkBack[1]);
			testVertexForEdgeClass(nodes[2], expectedLink[2],
					expectedSubLink[2], expectedLinkBack[2]);
		}
	}

	/**
	 * Tests if a Vertex has the expected degree considering the EdgeClass.
	 *
	 * @param forNode
	 *            the Vertex, which degrees should be tested
	 * @param expectedLink
	 *            the expected number of incident Links
	 * @param expectedSubLink
	 *            the expected number of incident SubLinks
	 * @param expectedLinkBack
	 *            the expected number of incident LinkBacks
	 */
	private void testVertexForEdgeClass(Vertex forNode, int expectedLink,
			int expectedSubLink, int expectedLinkBack) {
		List<EdgeClass> a = graph.getSchema()
				.getEdgeClassesInTopologicalOrder();
		assertEquals(expectedLink, forNode.getDegree(a.get(4)));
		assertEquals(expectedSubLink, forNode.getDegree(a.get(5)));
		assertEquals(expectedLinkBack, forNode.getDegree(a.get(3)));
	}

	// tests of the method getDegree(Class<? extends Edge> ec)

	/**
	 * A vertex with no connected incidences has to have a degree of 0 for each
	 * Class extends Edge.
	 */
	@Test
	public void getDegreeTestClass0() {
		Vertex v = graph.createDoubleSubNode();
		assertEquals(0, v.getDegree(Link.class));
		assertEquals(0, v.getDegree(SubLink.class));
		assertEquals(0, v.getDegree(LinkBack.class));
	}

	/**
	 * Checks the degrees in a manually build graph.
	 */
	@Test
	public void getDegreeTestClass1() {
		SubNode subn = graph.createSubNode();
		DoubleSubNode dsubn = graph.createDoubleSubNode();
		DoubleSubNode dsubnWithout = graph.createDoubleSubNode();
		SuperNode supern = graph.createSuperNode();
		graph.createLink(subn, supern);
		graph.createLink(dsubn, dsubn);
		graph.createLinkBack(supern, dsubn);
		graph.createLinkBack(dsubn, subn);
		graph.createSubLink(dsubn, supern);
		assertEquals(0, dsubnWithout.getDegree(Link.class));
		assertEquals(0, dsubnWithout.getDegree(LinkBack.class));
		assertEquals(0, dsubnWithout.getDegree(SubLink.class));
		assertEquals(1, subn.getDegree(Link.class));
		assertEquals(1, subn.getDegree(LinkBack.class));
		assertEquals(0, subn.getDegree(SubLink.class));
		assertEquals(3, dsubn.getDegree(Link.class));
		assertEquals(2, dsubn.getDegree(LinkBack.class));
		assertEquals(1, dsubn.getDegree(SubLink.class));
		assertEquals(2, supern.getDegree(Link.class));
		assertEquals(1, supern.getDegree(LinkBack.class));
		assertEquals(1, supern.getDegree(SubLink.class));
	}

	/**
	 * Generates a number of different edges and checks the correct degrees of
	 * the vertices considering the different Classes. After that it deletes the
	 * edges and checks the degrees again.
	 */
	@Test
	public void getDegreeTestClass2() {
		Vertex[] nodes = new Vertex[3];
		nodes[0] = graph.createSubNode();
		nodes[1] = graph.createDoubleSubNode();
		nodes[2] = graph.createSuperNode();
		int[] expectedLink = new int[] { 0, 0, 0 };
		int[] expectedLinkBack = new int[] { 0, 0, 0 };
		int[] expectedSubLink = new int[] { 0, 0, 0 };
		// create new edges
		for (int i = 0; i < 1000; i++) {
			// decides which edge should be created
			int edge = rand.nextInt(3);
			int start = rand.nextInt(2);
			int end = rand.nextInt(2) + 1;
			if (edge == 0) {
				// create a Link
				graph.createLink((AbstractSuperNode) nodes[start],
						(SuperNode) nodes[end]);
				expectedLink[start]++;
				expectedLink[end]++;
			} else if (edge == 1) {
				// create a LinkBack
				graph.createLinkBack((SuperNode) nodes[end],
						(AbstractSuperNode) nodes[start]);
				expectedLinkBack[start]++;
				expectedLinkBack[end]++;
			} else {
				// create a SubLink
				start = 1;
				graph.createSubLink((DoubleSubNode) nodes[start],
						(SuperNode) nodes[end]);
				expectedLink[start]++;
				expectedLink[end]++;
				expectedSubLink[start]++;
				expectedSubLink[end]++;
			}
			assertEquals(expectedLink[0], nodes[0].getDegree(Link.class));
			assertEquals(expectedLink[1], nodes[1].getDegree(Link.class));
			assertEquals(expectedLink[2], nodes[2].getDegree(Link.class));
			assertEquals(expectedLinkBack[0], nodes[0]
					.getDegree(LinkBack.class));
			assertEquals(expectedLinkBack[1], nodes[1]
					.getDegree(LinkBack.class));
			assertEquals(expectedLinkBack[2], nodes[2]
					.getDegree(LinkBack.class));
			assertEquals(expectedSubLink[0], nodes[0].getDegree(SubLink.class));
			assertEquals(expectedSubLink[1], nodes[1].getDegree(SubLink.class));
			assertEquals(expectedSubLink[2], nodes[2].getDegree(SubLink.class));
		}
		// delete the edges
		HashMap<Vertex, Integer> numbers = new HashMap<Vertex, Integer>();
		numbers.put(nodes[0], 0);
		numbers.put(nodes[1], 1);
		numbers.put(nodes[2], 2);
		for (int i = graph.getFirstEdgeInGraph().getId(); i < graph.getECount(); i++) {
			Edge e = graph.getEdge(i);
			int start = numbers.get(e.getAlpha());
			int end = numbers.get(e.getOmega());
			if (e instanceof SubLink) {
				expectedLink[start]--;
				expectedLink[end]--;
				expectedSubLink[start]--;
				expectedSubLink[end]--;
			} else if (e instanceof LinkBack) {
				expectedLinkBack[start]--;
				expectedLinkBack[end]--;
			} else {
				expectedLink[start]--;
				expectedLink[end]--;
			}
			graph.deleteEdge(e);
			assertEquals(expectedLink[0], nodes[0].getDegree(Link.class));
			assertEquals(expectedLink[1], nodes[1].getDegree(Link.class));
			assertEquals(expectedLink[2], nodes[2].getDegree(Link.class));
			assertEquals(expectedLinkBack[0], nodes[0]
					.getDegree(LinkBack.class));
			assertEquals(expectedLinkBack[1], nodes[1]
					.getDegree(LinkBack.class));
			assertEquals(expectedLinkBack[2], nodes[2]
					.getDegree(LinkBack.class));
			assertEquals(expectedSubLink[0], nodes[0].getDegree(SubLink.class));
			assertEquals(expectedSubLink[1], nodes[1].getDegree(SubLink.class));
			assertEquals(expectedSubLink[2], nodes[2].getDegree(SubLink.class));
		}
	}

	// tests of the method getDegree(EdgeClass ec, boolean noSubClasses)

	/**
	 * A vertex with no connected incidences has to have a degree of 0 for each
	 * EdgeClass.
	 */
	@Test
	public void getDegreeTestEdgeClassBoolean0() {
		Vertex v = graph.createDoubleSubNode();
		testVertexForEdgeClassSubClass(v, 0, 0, 0);
	}

	/**
	 * Checks the degrees in a manually build graph.
	 */
	@Test
	public void getDegreeTestEdgeClassBoolean1() {
		SubNode subn = graph.createSubNode();
		DoubleSubNode dsubn = graph.createDoubleSubNode();
		DoubleSubNode dsubnWithout = graph.createDoubleSubNode();
		SuperNode supern = graph.createSuperNode();
		graph.createLink(subn, supern);
		graph.createLink(dsubn, dsubn);
		graph.createLinkBack(supern, dsubn);
		graph.createLinkBack(dsubn, subn);
		graph.createSubLink(dsubn, supern);
		testVertexForEdgeClassSubClass(dsubnWithout, 0, 0, 0);
		testVertexForEdgeClassSubClass(subn, 1, 0, 1);
		testVertexForEdgeClassSubClass(dsubn, 3, 1, 2);
		testVertexForEdgeClassSubClass(supern, 2, 1, 1);
	}

	/**
	 * Checks the degrees in a manually build graph, which has only SubLinks.
	 */
	@Test
	public void getDegreeTestEdgeClassBoolean2() {
		DoubleSubNode dsubn = graph.createDoubleSubNode();
		SuperNode supern = graph.createSuperNode();
		graph.createSubLink(dsubn, supern);
		graph.createSubLink(dsubn, dsubn);
		testVertexForEdgeClassSubClass(dsubn, 3, 3, 0);
		testVertexForEdgeClassSubClass(supern, 1, 1, 0);
	}

	/**
	 * Generates a number of different edges and checks the correct degrees of
	 * the vertices considering the different Edgeclasses and their subclasses.
	 * After that it deletes the edges and checks the degrees again.
	 */
	@Test
	public void getDegreeTestEdgeClassBoolean3() {
		Vertex[] nodes = new Vertex[3];
		nodes[0] = graph.createSubNode();
		nodes[1] = graph.createDoubleSubNode();
		nodes[2] = graph.createSuperNode();
		int[] expectedLink = new int[] { 0, 0, 0 };
		int[] expectedLinkBack = new int[] { 0, 0, 0 };
		int[] expectedSubLink = new int[] { 0, 0, 0 };
		// create new edges
		for (int i = 0; i < 1000; i++) {
			// decides which edge should be created
			int edge = rand.nextInt(3);
			int start = rand.nextInt(2);
			int end = rand.nextInt(2) + 1;
			if (edge == 0) {
				// create a Link
				graph.createLink((AbstractSuperNode) nodes[start],
						(SuperNode) nodes[end]);
				expectedLink[start]++;
				expectedLink[end]++;
			} else if (edge == 1) {
				// create a LinkBack
				graph.createLinkBack((SuperNode) nodes[end],
						(AbstractSuperNode) nodes[start]);
				expectedLinkBack[start]++;
				expectedLinkBack[end]++;
			} else {
				// create a SubLink
				start = 1;
				graph.createSubLink((DoubleSubNode) nodes[start],
						(SuperNode) nodes[end]);
				expectedLink[start]++;
				expectedLink[end]++;
				expectedSubLink[start]++;
				expectedSubLink[end]++;
			}
			testVertexForEdgeClassSubClass(nodes[0], expectedLink[0],
					expectedSubLink[0], expectedLinkBack[0]);
			testVertexForEdgeClassSubClass(nodes[1], expectedLink[1],
					expectedSubLink[1], expectedLinkBack[1]);
			testVertexForEdgeClassSubClass(nodes[2], expectedLink[2],
					expectedSubLink[2], expectedLinkBack[2]);
		}
		// delete the edges
		HashMap<Vertex, Integer> numbers = new HashMap<Vertex, Integer>();
		numbers.put(nodes[0], 0);
		numbers.put(nodes[1], 1);
		numbers.put(nodes[2], 2);
		for (int i = graph.getFirstEdgeInGraph().getId(); i < graph.getECount(); i++) {
			Edge e = graph.getEdge(i);
			int start = numbers.get(e.getAlpha());
			int end = numbers.get(e.getOmega());
			if (e instanceof SubLink) {
				expectedLink[start]--;
				expectedLink[end]--;
				expectedSubLink[start]--;
				expectedSubLink[end]--;
			} else if (e instanceof LinkBack) {
				expectedLinkBack[start]--;
				expectedLinkBack[end]--;
			} else {
				expectedLink[start]--;
				expectedLink[end]--;
			}
			graph.deleteEdge(e);
			testVertexForEdgeClassSubClass(nodes[0], expectedLink[0],
					expectedSubLink[0], expectedLinkBack[0]);
			testVertexForEdgeClassSubClass(nodes[1], expectedLink[1],
					expectedSubLink[1], expectedLinkBack[1]);
			testVertexForEdgeClassSubClass(nodes[2], expectedLink[2],
					expectedSubLink[2], expectedLinkBack[2]);
		}
	}

	/**
	 * Tests if a Vertex has the expected degree considering the EdgeClass and
	 * SubClasses.
	 *
	 * @param forNode
	 *            the Vertex, which degrees should be tested
	 * @param expectedLink
	 *            the expected number of incident Links
	 * @param expectedSubLink
	 *            the expected number of incident SubLinks
	 * @param expectedLinkBack
	 *            the expected number of incident LinkBacks
	 */
	private void testVertexForEdgeClassSubClass(Vertex forNode,
			int expectedLink, int expectedSubLink, int expectedLinkBack) {
		List<EdgeClass> a = graph.getSchema()
				.getEdgeClassesInTopologicalOrder();
		assertEquals(expectedLink - expectedSubLink, forNode.getDegree(
				a.get(4), true));
		assertEquals(expectedLink, forNode.getDegree(a.get(4), false));
		assertEquals(expectedSubLink, forNode.getDegree(a.get(5), true));
		assertEquals(expectedSubLink, forNode.getDegree(a.get(5), false));
		assertEquals(expectedLinkBack, forNode.getDegree(a.get(3), true));
		assertEquals(expectedLinkBack, forNode.getDegree(a.get(3), false));
	}

	// tests of the method getDegree(Class<? extends Edge> ec, boolean
	// noSubClasses)

	/**
	 * A vertex with no connected incidences has to have a degree of 0 for each
	 * Class extends Edge.
	 */
	@Test
	public void getDegreeTestClassBoolean0() {
		Vertex v = graph.createDoubleSubNode();
		testVertexForClassSubClass(v, 0, 0, 0);
	}

	/**
	 * Checks the degrees in a manually build graph.
	 */
	@Test
	public void getDegreeTestClassBoolean1() {
		SubNode subn = graph.createSubNode();
		DoubleSubNode dsubn = graph.createDoubleSubNode();
		DoubleSubNode dsubnWithout = graph.createDoubleSubNode();
		SuperNode supern = graph.createSuperNode();
		graph.createLink(subn, supern);
		graph.createLink(dsubn, dsubn);
		graph.createLinkBack(supern, dsubn);
		graph.createLinkBack(dsubn, subn);
		graph.createSubLink(dsubn, supern);
		testVertexForClassSubClass(dsubnWithout, 0, 0, 0);
		testVertexForClassSubClass(subn, 1, 0, 1);
		testVertexForClassSubClass(dsubn, 3, 1, 2);
	}

	/**
	 * Checks the degrees in a manually build graph, which has only SubLinks.
	 */
	@Test
	public void getDegreeTestClassBoolean2() {
		DoubleSubNode dsubn = graph.createDoubleSubNode();
		SuperNode supern = graph.createSuperNode();
		graph.createSubLink(dsubn, supern);
		graph.createSubLink(dsubn, dsubn);
		testVertexForClassSubClass(dsubn, 3, 3, 0);
		testVertexForClassSubClass(supern, 1, 1, 0);
	}

	/**
	 * Generates a number of different edges and checks the correct degrees of
	 * the vertices considering the different Classes and Subclasses. After that
	 * it deletes the edges and checks the degrees again.
	 */
	@Test
	public void getDegreeTestClassBoolean3() {
		Vertex[] nodes = new Vertex[3];
		nodes[0] = graph.createSubNode();
		nodes[1] = graph.createDoubleSubNode();
		nodes[2] = graph.createSuperNode();
		int[] expectedLink = new int[] { 0, 0, 0 };
		int[] expectedLinkBack = new int[] { 0, 0, 0 };
		int[] expectedSubLink = new int[] { 0, 0, 0 };
		// create new edges
		for (int i = 0; i < 1000; i++) {
			// decides which edge should be created
			int edge = rand.nextInt(3);
			int start = rand.nextInt(2);
			int end = rand.nextInt(2) + 1;
			if (edge == 0) {
				// create a Link
				graph.createLink((AbstractSuperNode) nodes[start],
						(SuperNode) nodes[end]);
				expectedLink[start]++;
				expectedLink[end]++;
			} else if (edge == 1) {
				// create a LinkBack
				graph.createLinkBack((SuperNode) nodes[end],
						(AbstractSuperNode) nodes[start]);
				expectedLinkBack[start]++;
				expectedLinkBack[end]++;
			} else {
				// create a SubLink
				start = 1;
				graph.createSubLink((DoubleSubNode) nodes[start],
						(SuperNode) nodes[end]);
				expectedLink[start]++;
				expectedLink[end]++;
				expectedSubLink[start]++;
				expectedSubLink[end]++;
			}
			testVertexForEdgeClassSubClass(nodes[0], expectedLink[0],
					expectedSubLink[0], expectedLinkBack[0]);
			testVertexForEdgeClassSubClass(nodes[1], expectedLink[1],
					expectedSubLink[1], expectedLinkBack[1]);
			testVertexForEdgeClassSubClass(nodes[2], expectedLink[2],
					expectedSubLink[2], expectedLinkBack[2]);
		}
		// delete the edges
		HashMap<Vertex, Integer> numbers = new HashMap<Vertex, Integer>();
		numbers.put(nodes[0], 0);
		numbers.put(nodes[1], 1);
		numbers.put(nodes[2], 2);
		for (int i = graph.getFirstEdgeInGraph().getId(); i < graph.getECount(); i++) {
			Edge e = graph.getEdge(i);
			int start = numbers.get(e.getAlpha());
			int end = numbers.get(e.getOmega());
			if (e instanceof SubLink) {
				expectedLink[start]--;
				expectedLink[end]--;
				expectedSubLink[start]--;
				expectedSubLink[end]--;
			} else if (e instanceof LinkBack) {
				expectedLinkBack[start]--;
				expectedLinkBack[end]--;
			} else {
				expectedLink[start]--;
				expectedLink[end]--;
			}
			graph.deleteEdge(e);
			testVertexForEdgeClassSubClass(nodes[0], expectedLink[0],
					expectedSubLink[0], expectedLinkBack[0]);
			testVertexForEdgeClassSubClass(nodes[1], expectedLink[1],
					expectedSubLink[1], expectedLinkBack[1]);
			testVertexForEdgeClassSubClass(nodes[2], expectedLink[2],
					expectedSubLink[2], expectedLinkBack[2]);
		}
	}

	/**
	 * Tests if a Vertex has the expected degree considering the Classes
	 * extending Edge and SubClasses.
	 *
	 * @param forNode
	 *            the Vertex, which degrees should be tested
	 * @param expectedLink
	 *            the expected number of incident Links
	 * @param expectedSubLink
	 *            the expected number of incident SubLinks
	 * @param expectedLinkBack
	 *            the expected number of incident LinkBacks
	 */
	private void testVertexForClassSubClass(Vertex forNode, int expectedLink,
			int expectedSubLink, int expectedLinkBack) {
		assertEquals(expectedLink - expectedSubLink, forNode.getDegree(
				Link.class, true));
		assertEquals(expectedLink, forNode.getDegree(Link.class, false));
		assertEquals(expectedSubLink, forNode.getDegree(SubLink.class, true));
		assertEquals(expectedSubLink, forNode.getDegree(SubLink.class, false));
		assertEquals(expectedLinkBack, forNode.getDegree(LinkBack.class, true));
		assertEquals(expectedLinkBack, forNode.getDegree(LinkBack.class, false));
	}

	// tests of the method getDegree(EdgeClass ec, EdgeDirection orientation)

	/**
	 * A vertex with no connected incidences has to have a degree of 0 for each
	 * EdgeClass.
	 */
	@Test
	public void getDegreeTestEdgeClassEdgeDirection0() {
		Vertex v = graph.createDoubleSubNode();
		testVertexForEdgeClassEdgeDirection(v, 0, 0, 0, EdgeDirection.INOUT);
		testVertexForEdgeClassEdgeDirection(v, 0, 0, 0, EdgeDirection.IN);
		testVertexForEdgeClassEdgeDirection(v, 0, 0, 0, EdgeDirection.OUT);
	}

	/**
	 * Checks the degrees in a manually build graph.
	 */
	@Test
	public void getDegreeTestEdgeClassEdgeDirection1() {
		SubNode subn = graph.createSubNode();
		DoubleSubNode dsubn = graph.createDoubleSubNode();
		DoubleSubNode dsubnWithout = graph.createDoubleSubNode();
		SuperNode supern = graph.createSuperNode();
		graph.createLink(subn, supern);
		graph.createLink(dsubn, dsubn);
		graph.createSubLink(dsubn, supern);
		graph.createLinkBack(supern, dsubn);
		graph.createLinkBack(dsubn, subn);
		testVertexForEdgeClassEdgeDirection(dsubnWithout, 0, 0, 0,
				EdgeDirection.INOUT);
		testVertexForEdgeClassEdgeDirection(dsubnWithout, 0, 0, 0,
				EdgeDirection.IN);
		testVertexForEdgeClassEdgeDirection(dsubnWithout, 0, 0, 0,
				EdgeDirection.OUT);
		testVertexForEdgeClassEdgeDirection(subn, 1, 0, 1, EdgeDirection.INOUT);
		testVertexForEdgeClassEdgeDirection(subn, 0, 0, 1, EdgeDirection.IN);
		testVertexForEdgeClassEdgeDirection(subn, 1, 0, 0, EdgeDirection.OUT);
		testVertexForEdgeClassEdgeDirection(dsubn, 3, 1, 2, EdgeDirection.INOUT);
		testVertexForEdgeClassEdgeDirection(dsubn, 1, 0, 1, EdgeDirection.IN);
		testVertexForEdgeClassEdgeDirection(dsubn, 2, 1, 1, EdgeDirection.OUT);
		testVertexForEdgeClassEdgeDirection(supern, 2, 1, 1,
				EdgeDirection.INOUT);
		testVertexForEdgeClassEdgeDirection(supern, 2, 1, 0, EdgeDirection.IN);
		testVertexForEdgeClassEdgeDirection(supern, 0, 0, 1, EdgeDirection.OUT);
	}

	/**
	 * Checks the degrees in a manually build graph, which has only one
	 * LinkBack.
	 */
	@Test
	public void getDegreeTestEdgeClassEdgeDirection2() {
		SuperNode dsubn = graph.createSuperNode();
		AbstractSuperNode supern = graph.createSubNode();
		graph.createLinkBack(dsubn, supern);
		testVertexForEdgeClassEdgeDirection(dsubn, 0, 0, 0, EdgeDirection.IN);
		testVertexForEdgeClassEdgeDirection(dsubn, 0, 0, 1, EdgeDirection.OUT);
		testVertexForEdgeClassEdgeDirection(dsubn, 0, 0, 1, EdgeDirection.INOUT);
		testVertexForEdgeClassEdgeDirection(supern, 0, 0, 1, EdgeDirection.IN);
		testVertexForEdgeClassEdgeDirection(supern, 0, 0, 0, EdgeDirection.OUT);
		testVertexForEdgeClassEdgeDirection(supern, 0, 0, 1,
				EdgeDirection.INOUT);
	}

	/**
	 * Generates a number of different edges and checks the correct degrees of
	 * the vertices considering the different Edgeclasses and their
	 * EdgeDirections. After that it deletes the edges and checks the degrees
	 * again.
	 */
	@Test
	public void getDegreeTestEdgeClassEdgeDirection3() {
		Vertex[] nodes = new Vertex[3];
		nodes[0] = graph.createSubNode();
		nodes[1] = graph.createDoubleSubNode();
		nodes[2] = graph.createSuperNode();
		int[] expectedLinkIn = new int[] { 0, 0, 0 };
		int[] expectedLinkOut = new int[] { 0, 0, 0 };
		int[] expectedLinkBackIn = new int[] { 0, 0, 0 };
		int[] expectedLinkBackOut = new int[] { 0, 0, 0 };
		int[] expectedSubLinkIn = new int[] { 0, 0, 0 };
		int[] expectedSubLinkOut = new int[] { 0, 0, 0 };
		// create new edges
		for (int i = 0; i < 1000; i++) {
			// decides which edge should be created
			int edge = rand.nextInt(3);
			int start = rand.nextInt(2);
			int end = rand.nextInt(2) + 1;
			if (edge == 0) {
				// create a Link
				graph.createLink((AbstractSuperNode) nodes[start],
						(SuperNode) nodes[end]);
				expectedLinkOut[start]++;
				expectedLinkIn[end]++;
			} else if (edge == 1) {
				// create a LinkBack
				graph.createLinkBack((SuperNode) nodes[end],
						(AbstractSuperNode) nodes[start]);
				expectedLinkBackOut[end]++;
				expectedLinkBackIn[start]++;
			} else {
				// create a SubLink
				start = 1;
				graph.createSubLink((DoubleSubNode) nodes[start],
						(SuperNode) nodes[end]);
				expectedLinkOut[start]++;
				expectedLinkIn[end]++;
				expectedSubLinkOut[start]++;
				expectedSubLinkIn[end]++;
			}
			testVertexForEdgeClassEdgeDirection(nodes[0], expectedLinkIn[0],
					expectedSubLinkIn[0], expectedLinkBackIn[0],
					EdgeDirection.IN);
			testVertexForEdgeClassEdgeDirection(nodes[0], expectedLinkOut[0],
					expectedSubLinkOut[0], expectedLinkBackOut[0],
					EdgeDirection.OUT);
			testVertexForEdgeClassEdgeDirection(nodes[0], expectedLinkIn[0]
					+ expectedLinkOut[0], expectedSubLinkIn[0]
					+ expectedSubLinkOut[0], expectedLinkBackIn[0]
					+ expectedLinkBackOut[0], EdgeDirection.INOUT);
			testVertexForEdgeClassEdgeDirection(nodes[1], expectedLinkIn[1],
					expectedSubLinkIn[1], expectedLinkBackIn[1],
					EdgeDirection.IN);
			testVertexForEdgeClassEdgeDirection(nodes[1], expectedLinkOut[1],
					expectedSubLinkOut[1], expectedLinkBackOut[1],
					EdgeDirection.OUT);
			testVertexForEdgeClassEdgeDirection(nodes[1], expectedLinkIn[1]
					+ expectedLinkOut[1], expectedSubLinkIn[1]
					+ expectedSubLinkOut[1], expectedLinkBackIn[1]
					+ expectedLinkBackOut[1], EdgeDirection.INOUT);
			testVertexForEdgeClassEdgeDirection(nodes[2], expectedLinkIn[2],
					expectedSubLinkIn[2], expectedLinkBackIn[2],
					EdgeDirection.IN);
			testVertexForEdgeClassEdgeDirection(nodes[2], expectedLinkOut[2],
					expectedSubLinkOut[2], expectedLinkBackOut[2],
					EdgeDirection.OUT);
			testVertexForEdgeClassEdgeDirection(nodes[2], expectedLinkIn[2]
					+ expectedLinkOut[2], expectedSubLinkIn[2]
					+ expectedSubLinkOut[2], expectedLinkBackIn[2]
					+ expectedLinkBackOut[2], EdgeDirection.INOUT);
		}
		// delete the edges
		HashMap<Vertex, Integer> numbers = new HashMap<Vertex, Integer>();
		numbers.put(nodes[0], 0);
		numbers.put(nodes[1], 1);
		numbers.put(nodes[2], 2);
		for (int i = graph.getFirstEdgeInGraph().getId(); i < graph.getECount(); i++) {
			Edge e = graph.getEdge(i);
			int start = numbers.get(e.getAlpha());
			int end = numbers.get(e.getOmega());
			if (e instanceof SubLink) {
				expectedLinkOut[start]--;
				expectedLinkIn[end]--;
				expectedSubLinkOut[start]--;
				expectedSubLinkIn[end]--;
			} else if (e instanceof LinkBack) {
				expectedLinkBackOut[start]--;
				expectedLinkBackIn[end]--;
			} else {
				expectedLinkOut[start]--;
				expectedLinkIn[end]--;
			}
			graph.deleteEdge(e);
			testVertexForEdgeClassEdgeDirection(nodes[0], expectedLinkIn[0],
					expectedSubLinkIn[0], expectedLinkBackIn[0],
					EdgeDirection.IN);
			testVertexForEdgeClassEdgeDirection(nodes[0], expectedLinkOut[0],
					expectedSubLinkOut[0], expectedLinkBackOut[0],
					EdgeDirection.OUT);
			testVertexForEdgeClassEdgeDirection(nodes[0], expectedLinkIn[0]
					+ expectedLinkOut[0], expectedSubLinkIn[0]
					+ expectedSubLinkOut[0], expectedLinkBackIn[0]
					+ expectedLinkBackOut[0], EdgeDirection.INOUT);
			testVertexForEdgeClassEdgeDirection(nodes[1], expectedLinkIn[1],
					expectedSubLinkIn[1], expectedLinkBackIn[1],
					EdgeDirection.IN);
			testVertexForEdgeClassEdgeDirection(nodes[1], expectedLinkOut[1],
					expectedSubLinkOut[1], expectedLinkBackOut[1],
					EdgeDirection.OUT);
			testVertexForEdgeClassEdgeDirection(nodes[1], expectedLinkIn[1]
					+ expectedLinkOut[1], expectedSubLinkIn[1]
					+ expectedSubLinkOut[1], expectedLinkBackIn[1]
					+ expectedLinkBackOut[1], EdgeDirection.INOUT);
			testVertexForEdgeClassEdgeDirection(nodes[2], expectedLinkIn[2],
					expectedSubLinkIn[2], expectedLinkBackIn[2],
					EdgeDirection.IN);
			testVertexForEdgeClassEdgeDirection(nodes[2], expectedLinkOut[2],
					expectedSubLinkOut[2], expectedLinkBackOut[2],
					EdgeDirection.OUT);
			testVertexForEdgeClassEdgeDirection(nodes[2], expectedLinkIn[2]
					+ expectedLinkOut[2], expectedSubLinkIn[2]
					+ expectedSubLinkOut[2], expectedLinkBackIn[2]
					+ expectedLinkBackOut[2], EdgeDirection.INOUT);
		}
	}

	/**
	 * Tests if a Vertex has the expected degree considering the EdgeClass and
	 * the EdgeDirection.
	 *
	 * @param forNode
	 *            the Vertex, which degrees should be tested
	 * @param expectedLink
	 *            the expected number of incident Links
	 * @param expectedSubLink
	 *            the expected number of incident SubLinks
	 * @param expectedLinkBack
	 *            the expected number of incident LinkBacks
	 * @param direction
	 *            the direction of the incidences
	 */
	private void testVertexForEdgeClassEdgeDirection(Vertex forNode,
			int expectedLink, int expectedSubLink, int expectedLinkBack,
			EdgeDirection direction) {
		List<EdgeClass> a = graph.getSchema()
				.getEdgeClassesInTopologicalOrder();
		assertEquals(expectedLink, forNode.getDegree(a.get(4), direction));
		assertEquals(expectedSubLink, forNode.getDegree(a.get(5), direction));
		assertEquals(expectedLinkBack, forNode.getDegree(a.get(3), direction));
	}
}
