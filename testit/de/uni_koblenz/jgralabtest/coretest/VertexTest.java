package de.uni_koblenz.jgralabtest.coretest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;
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
		EdgeClass[] ecs = getEdgeClasses();
		assertEquals(expectedLink, forNode.getDegree(ecs[0]));
		assertEquals(expectedSubLink, forNode.getDegree(ecs[1]));
		assertEquals(expectedLinkBack, forNode.getDegree(ecs[2]));
	}

	/**
	 * Creates an array of the EdgeClasses.
	 * 
	 * @return {Link, SubLink, LinkBack}
	 */
	private EdgeClass[] getEdgeClasses() {
		EdgeClass[] ecs = new EdgeClass[3];
		List<EdgeClass> a = graph.getSchema()
				.getEdgeClassesInTopologicalOrder();
		for (EdgeClass ec : a) {
			if (ec.getSimpleName().equals("Link")) {
				ecs[0] = ec;
			} else if (ec.getSimpleName().equals("SubLink")) {
				ecs[1] = ec;
			}
			if (ec.getSimpleName().equals("LinkBack")) {
				ecs[2] = ec;
			}
		}
		return ecs;
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
		EdgeClass[] ecs = getEdgeClasses();
		assertEquals(expectedLink - expectedSubLink, forNode.getDegree(ecs[0],
				true));
		assertEquals(expectedLink, forNode.getDegree(ecs[0], false));
		assertEquals(expectedSubLink, forNode.getDegree(ecs[1], true));
		assertEquals(expectedSubLink, forNode.getDegree(ecs[1], false));
		assertEquals(expectedLinkBack, forNode.getDegree(ecs[2], true));
		assertEquals(expectedLinkBack, forNode.getDegree(ecs[2], false));
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
		EdgeClass[] ecs = getEdgeClasses();
		assertEquals(expectedLink, forNode.getDegree(ecs[0], direction));
		assertEquals(expectedSubLink, forNode.getDegree(ecs[1], direction));
		assertEquals(expectedLinkBack, forNode.getDegree(ecs[2], direction));
	}

	// tests of the method getDegree(Class<? extends Edge> ec, EdgeDirection
	// orientation)

	/**
	 * A vertex with no connected incidences has to have a degree of 0 for each
	 * Class.
	 */
	@Test
	public void getDegreeTestClassEdgeDirection0() {
		Vertex v = graph.createDoubleSubNode();
		testVertexForClassEdgeDirection(v, 0, 0, 0, EdgeDirection.INOUT);
		testVertexForClassEdgeDirection(v, 0, 0, 0, EdgeDirection.IN);
		testVertexForClassEdgeDirection(v, 0, 0, 0, EdgeDirection.OUT);
	}

	/**
	 * Checks the degrees in a manually build graph.
	 */
	@Test
	public void getDegreeTestClassEdgeDirection1() {
		SubNode subn = graph.createSubNode();
		DoubleSubNode dsubn = graph.createDoubleSubNode();
		DoubleSubNode dsubnWithout = graph.createDoubleSubNode();
		SuperNode supern = graph.createSuperNode();
		graph.createLink(subn, supern);
		graph.createLink(dsubn, dsubn);
		graph.createSubLink(dsubn, supern);
		graph.createLinkBack(supern, dsubn);
		graph.createLinkBack(dsubn, subn);
		testVertexForClassEdgeDirection(dsubnWithout, 0, 0, 0,
				EdgeDirection.INOUT);
		testVertexForClassEdgeDirection(dsubnWithout, 0, 0, 0, EdgeDirection.IN);
		testVertexForClassEdgeDirection(dsubnWithout, 0, 0, 0,
				EdgeDirection.OUT);
		testVertexForClassEdgeDirection(subn, 1, 0, 1, EdgeDirection.INOUT);
		testVertexForClassEdgeDirection(subn, 0, 0, 1, EdgeDirection.IN);
		testVertexForClassEdgeDirection(subn, 1, 0, 0, EdgeDirection.OUT);
		testVertexForClassEdgeDirection(dsubn, 3, 1, 2, EdgeDirection.INOUT);
		testVertexForClassEdgeDirection(dsubn, 1, 0, 1, EdgeDirection.IN);
		testVertexForClassEdgeDirection(dsubn, 2, 1, 1, EdgeDirection.OUT);
		testVertexForClassEdgeDirection(supern, 2, 1, 1, EdgeDirection.INOUT);
		testVertexForClassEdgeDirection(supern, 2, 1, 0, EdgeDirection.IN);
		testVertexForClassEdgeDirection(supern, 0, 0, 1, EdgeDirection.OUT);
	}

	/**
	 * Checks the degrees in a manually build graph, which has only one
	 * LinkBack.
	 */
	@Test
	public void getDegreeTestClassEdgeDirection2() {
		SuperNode dsubn = graph.createSuperNode();
		AbstractSuperNode supern = graph.createSubNode();
		graph.createLinkBack(dsubn, supern);
		testVertexForClassEdgeDirection(dsubn, 0, 0, 0, EdgeDirection.IN);
		testVertexForClassEdgeDirection(dsubn, 0, 0, 1, EdgeDirection.OUT);
		testVertexForClassEdgeDirection(dsubn, 0, 0, 1, EdgeDirection.INOUT);
		testVertexForClassEdgeDirection(supern, 0, 0, 1, EdgeDirection.IN);
		testVertexForClassEdgeDirection(supern, 0, 0, 0, EdgeDirection.OUT);
		testVertexForClassEdgeDirection(supern, 0, 0, 1, EdgeDirection.INOUT);
	}

	/**
	 * Generates a number of different edges and checks the correct degrees of
	 * the vertices considering the different Classes and their EdgeDirections.
	 * After that it deletes the edges and checks the degrees again.
	 */
	@Test
	public void getDegreeTestClassEdgeDirection3() {
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
			testVertexForClassEdgeDirection(nodes[0], expectedLinkIn[0],
					expectedSubLinkIn[0], expectedLinkBackIn[0],
					EdgeDirection.IN);
			testVertexForClassEdgeDirection(nodes[0], expectedLinkOut[0],
					expectedSubLinkOut[0], expectedLinkBackOut[0],
					EdgeDirection.OUT);
			testVertexForClassEdgeDirection(nodes[0], expectedLinkIn[0]
					+ expectedLinkOut[0], expectedSubLinkIn[0]
					+ expectedSubLinkOut[0], expectedLinkBackIn[0]
					+ expectedLinkBackOut[0], EdgeDirection.INOUT);
			testVertexForClassEdgeDirection(nodes[1], expectedLinkIn[1],
					expectedSubLinkIn[1], expectedLinkBackIn[1],
					EdgeDirection.IN);
			testVertexForClassEdgeDirection(nodes[1], expectedLinkOut[1],
					expectedSubLinkOut[1], expectedLinkBackOut[1],
					EdgeDirection.OUT);
			testVertexForClassEdgeDirection(nodes[1], expectedLinkIn[1]
					+ expectedLinkOut[1], expectedSubLinkIn[1]
					+ expectedSubLinkOut[1], expectedLinkBackIn[1]
					+ expectedLinkBackOut[1], EdgeDirection.INOUT);
			testVertexForClassEdgeDirection(nodes[2], expectedLinkIn[2],
					expectedSubLinkIn[2], expectedLinkBackIn[2],
					EdgeDirection.IN);
			testVertexForClassEdgeDirection(nodes[2], expectedLinkOut[2],
					expectedSubLinkOut[2], expectedLinkBackOut[2],
					EdgeDirection.OUT);
			testVertexForClassEdgeDirection(nodes[2], expectedLinkIn[2]
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
			testVertexForClassEdgeDirection(nodes[0], expectedLinkIn[0],
					expectedSubLinkIn[0], expectedLinkBackIn[0],
					EdgeDirection.IN);
			testVertexForClassEdgeDirection(nodes[0], expectedLinkOut[0],
					expectedSubLinkOut[0], expectedLinkBackOut[0],
					EdgeDirection.OUT);
			testVertexForClassEdgeDirection(nodes[0], expectedLinkIn[0]
					+ expectedLinkOut[0], expectedSubLinkIn[0]
					+ expectedSubLinkOut[0], expectedLinkBackIn[0]
					+ expectedLinkBackOut[0], EdgeDirection.INOUT);
			testVertexForClassEdgeDirection(nodes[1], expectedLinkIn[1],
					expectedSubLinkIn[1], expectedLinkBackIn[1],
					EdgeDirection.IN);
			testVertexForClassEdgeDirection(nodes[1], expectedLinkOut[1],
					expectedSubLinkOut[1], expectedLinkBackOut[1],
					EdgeDirection.OUT);
			testVertexForClassEdgeDirection(nodes[1], expectedLinkIn[1]
					+ expectedLinkOut[1], expectedSubLinkIn[1]
					+ expectedSubLinkOut[1], expectedLinkBackIn[1]
					+ expectedLinkBackOut[1], EdgeDirection.INOUT);
			testVertexForClassEdgeDirection(nodes[2], expectedLinkIn[2],
					expectedSubLinkIn[2], expectedLinkBackIn[2],
					EdgeDirection.IN);
			testVertexForClassEdgeDirection(nodes[2], expectedLinkOut[2],
					expectedSubLinkOut[2], expectedLinkBackOut[2],
					EdgeDirection.OUT);
			testVertexForClassEdgeDirection(nodes[2], expectedLinkIn[2]
					+ expectedLinkOut[2], expectedSubLinkIn[2]
					+ expectedSubLinkOut[2], expectedLinkBackIn[2]
					+ expectedLinkBackOut[2], EdgeDirection.INOUT);
		}
	}

	/**
	 * Tests if a Vertex has the expected degree considering the Class and the
	 * EdgeDirection.
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
	private void testVertexForClassEdgeDirection(Vertex forNode,
			int expectedLink, int expectedSubLink, int expectedLinkBack,
			EdgeDirection direction) {
		assertEquals(expectedLink, forNode.getDegree(Link.class, direction));
		assertEquals(expectedSubLink, forNode.getDegree(SubLink.class,
				direction));
		assertEquals(expectedLinkBack, forNode.getDegree(LinkBack.class,
				direction));
	}

	// tests of the method getDegree(EdgeClass ec, EdgeDirection orientation,
	// boolean noSubClasses)

	/**
	 * A vertex with no connected incidences has to have a degree of 0 for each
	 * EdgeClass.
	 */
	@Test
	public void getDegreeTestEdgeClassEdgeDirectionBoolean0() {
		Vertex v = graph.createDoubleSubNode();
		testVertexForEdgeClassEdgeDirectionBoolean(v, 0, 0, 0,
				EdgeDirection.INOUT);
		testVertexForEdgeClassEdgeDirectionBoolean(v, 0, 0, 0, EdgeDirection.IN);
		testVertexForEdgeClassEdgeDirectionBoolean(v, 0, 0, 0,
				EdgeDirection.OUT);
	}

	/**
	 * Checks the degrees in a manually build graph.
	 */
	@Test
	public void getDegreeTestEdgeClassEdgeDirectionBoolean1() {
		SubNode subn = graph.createSubNode();
		DoubleSubNode dsubn = graph.createDoubleSubNode();
		DoubleSubNode dsubnWithout = graph.createDoubleSubNode();
		SuperNode supern = graph.createSuperNode();
		graph.createLink(subn, supern);
		graph.createLink(dsubn, dsubn);
		graph.createSubLink(dsubn, supern);
		graph.createLinkBack(supern, dsubn);
		graph.createLinkBack(dsubn, subn);
		testVertexForEdgeClassEdgeDirectionBoolean(dsubnWithout, 0, 0, 0,
				EdgeDirection.INOUT);
		testVertexForEdgeClassEdgeDirectionBoolean(dsubnWithout, 0, 0, 0,
				EdgeDirection.IN);
		testVertexForEdgeClassEdgeDirectionBoolean(dsubnWithout, 0, 0, 0,
				EdgeDirection.OUT);
		testVertexForEdgeClassEdgeDirectionBoolean(subn, 1, 0, 1,
				EdgeDirection.INOUT);
		testVertexForEdgeClassEdgeDirectionBoolean(subn, 0, 0, 1,
				EdgeDirection.IN);
		testVertexForEdgeClassEdgeDirectionBoolean(subn, 1, 0, 0,
				EdgeDirection.OUT);
		testVertexForEdgeClassEdgeDirectionBoolean(dsubn, 3, 1, 2,
				EdgeDirection.INOUT);
		testVertexForEdgeClassEdgeDirectionBoolean(dsubn, 1, 0, 1,
				EdgeDirection.IN);
		testVertexForEdgeClassEdgeDirectionBoolean(dsubn, 2, 1, 1,
				EdgeDirection.OUT);
		testVertexForEdgeClassEdgeDirectionBoolean(supern, 2, 1, 1,
				EdgeDirection.INOUT);
		testVertexForEdgeClassEdgeDirectionBoolean(supern, 2, 1, 0,
				EdgeDirection.IN);
		testVertexForEdgeClassEdgeDirectionBoolean(supern, 0, 0, 1,
				EdgeDirection.OUT);
	}

	/**
	 * Checks the degrees in a manually build graph, which has only one
	 * LinkBack.
	 */
	@Test
	public void getDegreeTestEdgeClassEdgeDirectionBoolean2() {
		SuperNode dsubn = graph.createSuperNode();
		AbstractSuperNode supern = graph.createSubNode();
		graph.createLinkBack(dsubn, supern);
		testVertexForEdgeClassEdgeDirectionBoolean(dsubn, 0, 0, 0,
				EdgeDirection.IN);
		testVertexForEdgeClassEdgeDirectionBoolean(dsubn, 0, 0, 1,
				EdgeDirection.OUT);
		testVertexForEdgeClassEdgeDirectionBoolean(dsubn, 0, 0, 1,
				EdgeDirection.INOUT);
		testVertexForEdgeClassEdgeDirectionBoolean(supern, 0, 0, 1,
				EdgeDirection.IN);
		testVertexForEdgeClassEdgeDirectionBoolean(supern, 0, 0, 0,
				EdgeDirection.OUT);
		testVertexForEdgeClassEdgeDirectionBoolean(supern, 0, 0, 1,
				EdgeDirection.INOUT);
	}

	/**
	 * Checks the degrees in a manually build graph, which has only one Link.
	 */
	@Test
	public void getDegreeTestEdgeClassEdgeDirectionBoolean3() {
		SuperNode dsubn = graph.createSuperNode();
		AbstractSuperNode supern = graph.createSubNode();
		graph.createLink(supern, dsubn);
		testVertexForEdgeClassEdgeDirectionBoolean(dsubn, 1, 0, 0,
				EdgeDirection.IN);
		testVertexForEdgeClassEdgeDirectionBoolean(dsubn, 0, 0, 0,
				EdgeDirection.OUT);
		testVertexForEdgeClassEdgeDirectionBoolean(dsubn, 1, 0, 0,
				EdgeDirection.INOUT);
		testVertexForEdgeClassEdgeDirectionBoolean(supern, 0, 0, 0,
				EdgeDirection.IN);
		testVertexForEdgeClassEdgeDirectionBoolean(supern, 1, 0, 0,
				EdgeDirection.OUT);
		testVertexForEdgeClassEdgeDirectionBoolean(supern, 1, 0, 0,
				EdgeDirection.INOUT);
	}

	/**
	 * Generates a number of different edges and checks the correct degrees of
	 * the vertices considering the different Edgeclasses, their EdgeDirections
	 * and their SubClasses. After that it deletes the edges and checks the
	 * degrees again.
	 */
	@Test
	public void getDegreeTestEdgeClassEdgeDirectionBoolean5() {
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
			testVertexForEdgeClassEdgeDirectionBoolean(nodes[0],
					expectedLinkIn[0], expectedSubLinkIn[0],
					expectedLinkBackIn[0], EdgeDirection.IN);
			testVertexForEdgeClassEdgeDirectionBoolean(nodes[0],
					expectedLinkOut[0], expectedSubLinkOut[0],
					expectedLinkBackOut[0], EdgeDirection.OUT);
			testVertexForEdgeClassEdgeDirectionBoolean(nodes[0],
					expectedLinkIn[0] + expectedLinkOut[0],
					expectedSubLinkIn[0] + expectedSubLinkOut[0],
					expectedLinkBackIn[0] + expectedLinkBackOut[0],
					EdgeDirection.INOUT);
			testVertexForEdgeClassEdgeDirectionBoolean(nodes[1],
					expectedLinkIn[1], expectedSubLinkIn[1],
					expectedLinkBackIn[1], EdgeDirection.IN);
			testVertexForEdgeClassEdgeDirectionBoolean(nodes[1],
					expectedLinkOut[1], expectedSubLinkOut[1],
					expectedLinkBackOut[1], EdgeDirection.OUT);
			testVertexForEdgeClassEdgeDirectionBoolean(nodes[1],
					expectedLinkIn[1] + expectedLinkOut[1],
					expectedSubLinkIn[1] + expectedSubLinkOut[1],
					expectedLinkBackIn[1] + expectedLinkBackOut[1],
					EdgeDirection.INOUT);
			testVertexForEdgeClassEdgeDirectionBoolean(nodes[2],
					expectedLinkIn[2], expectedSubLinkIn[2],
					expectedLinkBackIn[2], EdgeDirection.IN);
			testVertexForEdgeClassEdgeDirectionBoolean(nodes[2],
					expectedLinkOut[2], expectedSubLinkOut[2],
					expectedLinkBackOut[2], EdgeDirection.OUT);
			testVertexForEdgeClassEdgeDirectionBoolean(nodes[2],
					expectedLinkIn[2] + expectedLinkOut[2],
					expectedSubLinkIn[2] + expectedSubLinkOut[2],
					expectedLinkBackIn[2] + expectedLinkBackOut[2],
					EdgeDirection.INOUT);
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
			testVertexForEdgeClassEdgeDirectionBoolean(nodes[0],
					expectedLinkIn[0], expectedSubLinkIn[0],
					expectedLinkBackIn[0], EdgeDirection.IN);
			testVertexForEdgeClassEdgeDirectionBoolean(nodes[0],
					expectedLinkOut[0], expectedSubLinkOut[0],
					expectedLinkBackOut[0], EdgeDirection.OUT);
			testVertexForEdgeClassEdgeDirectionBoolean(nodes[0],
					expectedLinkIn[0] + expectedLinkOut[0],
					expectedSubLinkIn[0] + expectedSubLinkOut[0],
					expectedLinkBackIn[0] + expectedLinkBackOut[0],
					EdgeDirection.INOUT);
			testVertexForEdgeClassEdgeDirectionBoolean(nodes[1],
					expectedLinkIn[1], expectedSubLinkIn[1],
					expectedLinkBackIn[1], EdgeDirection.IN);
			testVertexForEdgeClassEdgeDirectionBoolean(nodes[1],
					expectedLinkOut[1], expectedSubLinkOut[1],
					expectedLinkBackOut[1], EdgeDirection.OUT);
			testVertexForEdgeClassEdgeDirectionBoolean(nodes[1],
					expectedLinkIn[1] + expectedLinkOut[1],
					expectedSubLinkIn[1] + expectedSubLinkOut[1],
					expectedLinkBackIn[1] + expectedLinkBackOut[1],
					EdgeDirection.INOUT);
			testVertexForEdgeClassEdgeDirectionBoolean(nodes[2],
					expectedLinkIn[2], expectedSubLinkIn[2],
					expectedLinkBackIn[2], EdgeDirection.IN);
			testVertexForEdgeClassEdgeDirectionBoolean(nodes[2],
					expectedLinkOut[2], expectedSubLinkOut[2],
					expectedLinkBackOut[2], EdgeDirection.OUT);
			testVertexForEdgeClassEdgeDirectionBoolean(nodes[2],
					expectedLinkIn[2] + expectedLinkOut[2],
					expectedSubLinkIn[2] + expectedSubLinkOut[2],
					expectedLinkBackIn[2] + expectedLinkBackOut[2],
					EdgeDirection.INOUT);
		}
	}

	/**
	 * Tests if a Vertex has the expected degree considering the EdgeClass, the
	 * EdgeDirection and the Subclasses.
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
	private void testVertexForEdgeClassEdgeDirectionBoolean(Vertex forNode,
			int expectedLink, int expectedSubLink, int expectedLinkBack,
			EdgeDirection direction) {
		EdgeClass[] ecs = getEdgeClasses();
		assertEquals(expectedLink, forNode.getDegree(ecs[0], direction, false));
		assertEquals(expectedLink - expectedSubLink, forNode.getDegree(ecs[0],
				direction, true));
		assertEquals(expectedSubLink, forNode.getDegree(ecs[1], direction,
				false));
		assertEquals(expectedSubLink, forNode
				.getDegree(ecs[1], direction, true));
		assertEquals(expectedLinkBack, forNode.getDegree(ecs[2], direction,
				false));
		assertEquals(expectedLinkBack, forNode.getDegree(ecs[2], direction,
				true));
	}

	// tests of the method getDegree(Class<? extends Edge> ec, EdgeDirection
	// orientation, boolean noSubClasses)

	/**
	 * A vertex with no connected incidences has to have a degree of 0 for each
	 * Class.
	 */
	@Test
	public void getDegreeTestClassEdgeDirectionBoolean0() {
		Vertex v = graph.createDoubleSubNode();
		testVertexForClassEdgeDirectionBoolean(v, 0, 0, 0, EdgeDirection.INOUT);
		testVertexForClassEdgeDirectionBoolean(v, 0, 0, 0, EdgeDirection.IN);
		testVertexForClassEdgeDirectionBoolean(v, 0, 0, 0, EdgeDirection.OUT);
	}

	/**
	 * Checks the degrees in a manually build graph.
	 */
	@Test
	public void getDegreeTestClassEdgeDirectionBoolean1() {
		SubNode subn = graph.createSubNode();
		DoubleSubNode dsubn = graph.createDoubleSubNode();
		DoubleSubNode dsubnWithout = graph.createDoubleSubNode();
		SuperNode supern = graph.createSuperNode();
		graph.createLink(subn, supern);
		graph.createLink(dsubn, dsubn);
		graph.createSubLink(dsubn, supern);
		graph.createLinkBack(supern, dsubn);
		graph.createLinkBack(dsubn, subn);
		testVertexForClassEdgeDirectionBoolean(dsubnWithout, 0, 0, 0,
				EdgeDirection.INOUT);
		testVertexForClassEdgeDirectionBoolean(dsubnWithout, 0, 0, 0,
				EdgeDirection.IN);
		testVertexForClassEdgeDirectionBoolean(dsubnWithout, 0, 0, 0,
				EdgeDirection.OUT);
		testVertexForClassEdgeDirectionBoolean(subn, 1, 0, 1,
				EdgeDirection.INOUT);
		testVertexForClassEdgeDirectionBoolean(subn, 0, 0, 1, EdgeDirection.IN);
		testVertexForClassEdgeDirectionBoolean(subn, 1, 0, 0, EdgeDirection.OUT);
		testVertexForClassEdgeDirectionBoolean(dsubn, 3, 1, 2,
				EdgeDirection.INOUT);
		testVertexForClassEdgeDirectionBoolean(dsubn, 1, 0, 1, EdgeDirection.IN);
		testVertexForClassEdgeDirectionBoolean(dsubn, 2, 1, 1,
				EdgeDirection.OUT);
		testVertexForClassEdgeDirectionBoolean(supern, 2, 1, 1,
				EdgeDirection.INOUT);
		testVertexForClassEdgeDirectionBoolean(supern, 2, 1, 0,
				EdgeDirection.IN);
		testVertexForClassEdgeDirectionBoolean(supern, 0, 0, 1,
				EdgeDirection.OUT);
	}

	/**
	 * Checks the degrees in a manually build graph, which has only one
	 * LinkBack.
	 */
	@Test
	public void getDegreeTestClassEdgeDirectionBoolean2() {
		SuperNode dsubn = graph.createSuperNode();
		AbstractSuperNode supern = graph.createSubNode();
		graph.createLinkBack(dsubn, supern);
		testVertexForClassEdgeDirectionBoolean(dsubn, 0, 0, 0, EdgeDirection.IN);
		testVertexForClassEdgeDirectionBoolean(dsubn, 0, 0, 1,
				EdgeDirection.OUT);
		testVertexForClassEdgeDirectionBoolean(dsubn, 0, 0, 1,
				EdgeDirection.INOUT);
		testVertexForClassEdgeDirectionBoolean(supern, 0, 0, 1,
				EdgeDirection.IN);
		testVertexForClassEdgeDirectionBoolean(supern, 0, 0, 0,
				EdgeDirection.OUT);
		testVertexForClassEdgeDirectionBoolean(supern, 0, 0, 1,
				EdgeDirection.INOUT);
	}

	/**
	 * Checks the degrees in a manually build graph, which has only one Link.
	 */
	@Test
	public void getDegreeTestClassEdgeDirectionBoolean3() {
		SuperNode dsubn = graph.createSuperNode();
		AbstractSuperNode supern = graph.createSubNode();
		graph.createLink(supern, dsubn);
		testVertexForClassEdgeDirectionBoolean(dsubn, 1, 0, 0, EdgeDirection.IN);
		testVertexForClassEdgeDirectionBoolean(dsubn, 0, 0, 0,
				EdgeDirection.OUT);
		testVertexForClassEdgeDirectionBoolean(dsubn, 1, 0, 0,
				EdgeDirection.INOUT);
		testVertexForClassEdgeDirectionBoolean(supern, 0, 0, 0,
				EdgeDirection.IN);
		testVertexForClassEdgeDirectionBoolean(supern, 1, 0, 0,
				EdgeDirection.OUT);
		testVertexForClassEdgeDirectionBoolean(supern, 1, 0, 0,
				EdgeDirection.INOUT);
	}

	/**
	 * Generates a number of different edges and checks the correct degrees of
	 * the vertices considering the different Classes, their EdgeDirections and
	 * their SubClasses. After that it deletes the edges and checks the degrees
	 * again.
	 */
	@Test
	public void getDegreeTestClassEdgeDirectionBoolean5() {
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
			testVertexForClassEdgeDirectionBoolean(nodes[0], expectedLinkIn[0],
					expectedSubLinkIn[0], expectedLinkBackIn[0],
					EdgeDirection.IN);
			testVertexForClassEdgeDirectionBoolean(nodes[0],
					expectedLinkOut[0], expectedSubLinkOut[0],
					expectedLinkBackOut[0], EdgeDirection.OUT);
			testVertexForClassEdgeDirectionBoolean(nodes[0], expectedLinkIn[0]
					+ expectedLinkOut[0], expectedSubLinkIn[0]
					+ expectedSubLinkOut[0], expectedLinkBackIn[0]
					+ expectedLinkBackOut[0], EdgeDirection.INOUT);
			testVertexForClassEdgeDirectionBoolean(nodes[1], expectedLinkIn[1],
					expectedSubLinkIn[1], expectedLinkBackIn[1],
					EdgeDirection.IN);
			testVertexForClassEdgeDirectionBoolean(nodes[1],
					expectedLinkOut[1], expectedSubLinkOut[1],
					expectedLinkBackOut[1], EdgeDirection.OUT);
			testVertexForClassEdgeDirectionBoolean(nodes[1], expectedLinkIn[1]
					+ expectedLinkOut[1], expectedSubLinkIn[1]
					+ expectedSubLinkOut[1], expectedLinkBackIn[1]
					+ expectedLinkBackOut[1], EdgeDirection.INOUT);
			testVertexForClassEdgeDirectionBoolean(nodes[2], expectedLinkIn[2],
					expectedSubLinkIn[2], expectedLinkBackIn[2],
					EdgeDirection.IN);
			testVertexForClassEdgeDirectionBoolean(nodes[2],
					expectedLinkOut[2], expectedSubLinkOut[2],
					expectedLinkBackOut[2], EdgeDirection.OUT);
			testVertexForClassEdgeDirectionBoolean(nodes[2], expectedLinkIn[2]
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
			testVertexForClassEdgeDirectionBoolean(nodes[0], expectedLinkIn[0],
					expectedSubLinkIn[0], expectedLinkBackIn[0],
					EdgeDirection.IN);
			testVertexForClassEdgeDirectionBoolean(nodes[0],
					expectedLinkOut[0], expectedSubLinkOut[0],
					expectedLinkBackOut[0], EdgeDirection.OUT);
			testVertexForClassEdgeDirectionBoolean(nodes[0], expectedLinkIn[0]
					+ expectedLinkOut[0], expectedSubLinkIn[0]
					+ expectedSubLinkOut[0], expectedLinkBackIn[0]
					+ expectedLinkBackOut[0], EdgeDirection.INOUT);
			testVertexForClassEdgeDirectionBoolean(nodes[1], expectedLinkIn[1],
					expectedSubLinkIn[1], expectedLinkBackIn[1],
					EdgeDirection.IN);
			testVertexForClassEdgeDirectionBoolean(nodes[1],
					expectedLinkOut[1], expectedSubLinkOut[1],
					expectedLinkBackOut[1], EdgeDirection.OUT);
			testVertexForClassEdgeDirectionBoolean(nodes[1], expectedLinkIn[1]
					+ expectedLinkOut[1], expectedSubLinkIn[1]
					+ expectedSubLinkOut[1], expectedLinkBackIn[1]
					+ expectedLinkBackOut[1], EdgeDirection.INOUT);
			testVertexForClassEdgeDirectionBoolean(nodes[2], expectedLinkIn[2],
					expectedSubLinkIn[2], expectedLinkBackIn[2],
					EdgeDirection.IN);
			testVertexForClassEdgeDirectionBoolean(nodes[2],
					expectedLinkOut[2], expectedSubLinkOut[2],
					expectedLinkBackOut[2], EdgeDirection.OUT);
			testVertexForClassEdgeDirectionBoolean(nodes[2], expectedLinkIn[2]
					+ expectedLinkOut[2], expectedSubLinkIn[2]
					+ expectedSubLinkOut[2], expectedLinkBackIn[2]
					+ expectedLinkBackOut[2], EdgeDirection.INOUT);
		}
	}

	/**
	 * Tests if a Vertex has the expected degree considering the Class, the
	 * EdgeDirection and the Subclasses.
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
	private void testVertexForClassEdgeDirectionBoolean(Vertex forNode,
			int expectedLink, int expectedSubLink, int expectedLinkBack,
			EdgeDirection direction) {
		assertEquals(expectedLink, forNode.getDegree(Link.class, direction,
				false));
		assertEquals(expectedLink - expectedSubLink, forNode.getDegree(
				Link.class, direction, true));
		assertEquals(expectedSubLink, forNode.getDegree(SubLink.class,
				direction, false));
		assertEquals(expectedSubLink, forNode.getDegree(SubLink.class,
				direction, true));
		assertEquals(expectedLinkBack, forNode.getDegree(LinkBack.class,
				direction, false));
		assertEquals(expectedLinkBack, forNode.getDegree(LinkBack.class,
				direction, true));
	}

	// tests of the method getPrevVertex();

	/**
	 * Tests the method if there is only one Vertex in the graph.
	 */
	@Test
	public void getPrevVertexTest0() {
		Vertex v = graph.createSuperNode();
		assertEquals(null, v.getPrevVertex());
	}

	/**
	 * Tests the correctness in a manually build graph.
	 */
	@Test
	public void getPrevVertexTest1() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createSubNode();
		Vertex v2 = graph.createSuperNode();
		Vertex v3 = graph.createSuperNode();
		Vertex v4 = graph.createDoubleSubNode();
		assertEquals(v3, v4.getPrevVertex());
		assertEquals(v2, v3.getPrevVertex());
		assertEquals(v1, v2.getPrevVertex());
		assertEquals(v0, v1.getPrevVertex());
		assertEquals(null, v0.getPrevVertex());
	}

	/**
	 * Tests the correctness in an random graph.
	 */
	@Test
	public void getPrevVertexTest2() {
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph(100, 100);
			Vertex[] vertices = new Vertex[30];
			// Create Vertices
			for (int j = 0; j < vertices.length; j++) {
				vertices[j] = graph.createDoubleSubNode();
			}
			// Check correctness
			for (int j = vertices.length - 1; j >= 0; j--) {
				assertEquals(j == 0 ? null : vertices[j - 1], vertices[j]
						.getPrevVertex());
			}
		}
	}

	// tests of the method getNextVertex();
	// (tested in LoadTest, too)

	/**
	 * Tests the method if there is only one Vertex in the graph.
	 */
	@Test
	public void getNextVertexTest0() {
		Vertex v = graph.createSuperNode();
		assertEquals(null, v.getNextVertex());
	}

	/**
	 * Tests the correctness in a manually build graph.
	 */
	@Test
	public void getNextVertexTest1() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createSubNode();
		Vertex v2 = graph.createSuperNode();
		Vertex v3 = graph.createSuperNode();
		Vertex v4 = graph.createDoubleSubNode();
		assertEquals(v1, v0.getNextVertex());
		assertEquals(v2, v1.getNextVertex());
		assertEquals(v3, v2.getNextVertex());
		assertEquals(v4, v3.getNextVertex());
		assertEquals(null, v4.getNextVertex());
	}

	/**
	 * Tests the correctness in an random graph.
	 */
	@Test
	public void getNextVertexTest2() {
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph(100, 100);
			Vertex[] vertices = new Vertex[30];
			// Create Vertices
			for (int j = 0; j < vertices.length; j++) {
				vertices[j] = graph.createDoubleSubNode();
			}
			// Check correctness
			for (int j = 0; j < vertices.length; j++) {
				assertEquals(j == vertices.length - 1 ? null : vertices[j + 1],
						vertices[j].getNextVertex());
			}
		}
	}

	// tests of the method getNextVertex(VertexClass aVertexClass);

	/**
	 * Creates an array <code>ret</code> of all VertexClasses.<br>
	 * ret[0]=AbstractSuperNode<br>
	 * ret[1]=SubNode<br>
	 * ret[2]=SuperNode<br>
	 * ret[3]=DoubleSubNode
	 * 
	 * @return an array <code>ret</code> of all VertexClasses
	 */
	private VertexClass[] getVertexClasses() {
		List<VertexClass> vclasses = graph.getSchema()
				.getVertexClassesInTopologicalOrder();
		VertexClass[] vcret = new VertexClass[4];
		for (VertexClass vc : vclasses) {
			if (vc.getSimpleName().equals("AbstractSuperNode")) {
				vcret[0] = vc;
			} else if (vc.getSimpleName().equals("SubNode")) {
				vcret[1] = vc;
			} else if (vc.getSimpleName().equals("SuperNode")) {
				vcret[2] = vc;
			} else if (vc.getSimpleName().equals("DoubleSubNode")) {
				vcret[3] = vc;
			}
		}
		return vcret;
	}

	/**
	 * Tests if there is only one vertex in the graph.
	 */
	@Test
	public void getNextVertexTestVertexClass0() {
		VertexClass[] vertices = getVertexClasses();
		Vertex v = graph.createSubNode();
		assertEquals(null, v.getNextVertexOfClass(vertices[0]));
		assertEquals(null, v.getNextVertexOfClass(vertices[1]));
		assertEquals(null, v.getNextVertexOfClass(vertices[2]));
		assertEquals(null, v.getNextVertexOfClass(vertices[3]));
	}

	/**
	 * The next vertex is an instance of a class which is a subclass of another
	 * vertexclass.
	 */
	@Test
	public void getNextVertexTestVertexClass1() {
		VertexClass[] vertices = getVertexClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createSubNode();
		assertEquals(v1, v0.getNextVertexOfClass(vertices[0]));
		assertEquals(v1, v0.getNextVertexOfClass(vertices[1]));
		assertEquals(null, v0.getNextVertexOfClass(vertices[2]));
		assertEquals(null, v0.getNextVertexOfClass(vertices[3]));
	}

	/**
	 * The next vertex is an instance of a class which is a subclass of tow
	 * other vertexclasses.
	 */
	@Test
	public void getNextVertexTestVertexClass2() {
		VertexClass[] vertices = getVertexClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		assertEquals(v1, v0.getNextVertexOfClass(vertices[0]));
		assertEquals(v1, v0.getNextVertexOfClass(vertices[1]));
		assertEquals(v1, v0.getNextVertexOfClass(vertices[2]));
		assertEquals(v1, v0.getNextVertexOfClass(vertices[3]));
	}

	/**
	 * Test in a manually build graph: SubNode SuperNode DoubleSubNode SuperNode
	 * SubNode SuperNode DoubleSubNode
	 */
	@Test
	public void getNextVertexTestVertexClass3() {
		VertexClass[] vertices = getVertexClasses();
		Vertex v0 = graph.createSubNode();
		Vertex v1 = graph.createSuperNode();
		Vertex v2 = graph.createDoubleSubNode();
		Vertex v3 = graph.createSuperNode();
		Vertex v4 = graph.createSubNode();
		Vertex v5 = graph.createSuperNode();
		Vertex v6 = graph.createDoubleSubNode();

		assertEquals(v2, v0.getNextVertexOfClass(vertices[0]));
		assertEquals(v2, v0.getNextVertexOfClass(vertices[1]));
		assertEquals(v1, v0.getNextVertexOfClass(vertices[2]));
		assertEquals(v2, v0.getNextVertexOfClass(vertices[3]));

		assertEquals(v2, v1.getNextVertexOfClass(vertices[0]));
		assertEquals(v2, v1.getNextVertexOfClass(vertices[1]));
		assertEquals(v2, v1.getNextVertexOfClass(vertices[2]));
		assertEquals(v2, v1.getNextVertexOfClass(vertices[3]));

		assertEquals(v4, v2.getNextVertexOfClass(vertices[0]));
		assertEquals(v4, v2.getNextVertexOfClass(vertices[1]));
		assertEquals(v3, v2.getNextVertexOfClass(vertices[2]));
		assertEquals(v6, v2.getNextVertexOfClass(vertices[3]));

		assertEquals(v4, v3.getNextVertexOfClass(vertices[0]));
		assertEquals(v4, v3.getNextVertexOfClass(vertices[1]));
		assertEquals(v5, v3.getNextVertexOfClass(vertices[2]));
		assertEquals(v6, v3.getNextVertexOfClass(vertices[3]));

		assertEquals(v6, v4.getNextVertexOfClass(vertices[0]));
		assertEquals(v6, v4.getNextVertexOfClass(vertices[1]));
		assertEquals(v5, v4.getNextVertexOfClass(vertices[2]));
		assertEquals(v6, v4.getNextVertexOfClass(vertices[3]));

		assertEquals(v6, v5.getNextVertexOfClass(vertices[0]));
		assertEquals(v6, v5.getNextVertexOfClass(vertices[1]));
		assertEquals(v6, v5.getNextVertexOfClass(vertices[2]));
		assertEquals(v6, v5.getNextVertexOfClass(vertices[3]));

		assertEquals(null, v6.getNextVertexOfClass(vertices[0]));
		assertEquals(null, v6.getNextVertexOfClass(vertices[1]));
		assertEquals(null, v6.getNextVertexOfClass(vertices[2]));
		assertEquals(null, v6.getNextVertexOfClass(vertices[3]));
	}

	/**
	 * RandomTests
	 */
	@Test
	public void getNextVertexTestVertexClass4() {
		VertexClass[] vClasses = getVertexClasses();
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			// all vertices in the graph
			Vertex[] vertices = new Vertex[20];
			// the next AbstractSuperNode for the i-th Vertex
			Vertex[] nextAbstractSuperNode = new Vertex[vertices.length];
			// the position of the last Vertex which has a nextApstractSuperNode
			int lastAbstractSuperNode = -1;
			Vertex[] nextSubNode = new Vertex[vertices.length];
			int lastSubNode = -1;
			Vertex[] nextSuperNode = new Vertex[vertices.length];
			int lastSuperNode = -1;
			Vertex[] nextDoubleSubNode = new Vertex[vertices.length];
			int lastDoubleSubNode = -1;
			for (int j = 0; j < vertices.length; j++) {
				// create vertices
				int vclass = rand.nextInt(3);
				switch (vclass) {
				case 0:
					vertices[j] = graph.createSubNode();
					// all vertices until j(exclusive) have the new vertex as
					// next AbstractSuperNode
					while (lastAbstractSuperNode < j) {
						if (lastAbstractSuperNode >= 0) {
							nextAbstractSuperNode[lastAbstractSuperNode] = vertices[j];
						}
						lastAbstractSuperNode++;
					}
					// all vertices until j(exclusive) have the new vertex as
					// next SubNode
					while (lastSubNode < j) {
						if (lastSubNode >= 0) {
							nextSubNode[lastSubNode] = vertices[j];
						}
						lastSubNode++;
					}
					break;
				case 1:
					vertices[j] = graph.createSuperNode();
					// all vertices until j(exclusive) have the new vertex as
					// next SuperNode
					while (lastSuperNode < j) {
						if (lastSuperNode >= 0) {
							nextSuperNode[lastSuperNode] = vertices[j];
						}
						lastSuperNode++;
					}
					break;
				case 2:
					vertices[j] = graph.createDoubleSubNode();
					// all vertices until j(exclusive) have the new vertex as
					// next AbstractSuperNode
					while (lastAbstractSuperNode < j) {
						if (lastAbstractSuperNode >= 0) {
							nextAbstractSuperNode[lastAbstractSuperNode] = vertices[j];
						}
						lastAbstractSuperNode++;
					}
					// all vertices until j(exclusive) have the new vertex as
					// next SubNode
					while (lastSubNode < j) {
						if (lastSubNode >= 0) {
							nextSubNode[lastSubNode] = vertices[j];
						}
						lastSubNode++;
					}
					// all vertices until j(exclusive) have the new vertex as
					// next SuperNode
					while (lastSuperNode < j) {
						if (lastSuperNode >= 0) {
							nextSuperNode[lastSuperNode] = vertices[j];
						}
						lastSuperNode++;
					}
					// all vertices until j(exclusive) have the new vertex as
					// next DoubleSubNode
					while (lastDoubleSubNode < j) {
						if (lastDoubleSubNode >= 0) {
							nextDoubleSubNode[lastDoubleSubNode] = vertices[j];
						}
						lastDoubleSubNode++;
					}
					break;
				}
			}
			// check nextVertex after creating
			for (int j = 0; j < vertices.length; j++) {
				assertEquals(nextAbstractSuperNode[j], vertices[j]
						.getNextVertexOfClass(vClasses[0]));
				assertEquals(nextSubNode[j], vertices[j]
						.getNextVertexOfClass(vClasses[1]));
				assertEquals(nextSuperNode[j], vertices[j]
						.getNextVertexOfClass(vClasses[2]));
				assertEquals(nextDoubleSubNode[j], vertices[j]
						.getNextVertexOfClass(vClasses[3]));
			}
		}
	}

	// tests of the method getNextVertex(Class<? extends Vertex>
	// aM1VertexClass);

	/**
	 * Tests if there is only one vertex in the graph.
	 */
	@Test
	public void getNextVertexTestClass0() {
		Vertex v = graph.createSubNode();
		assertEquals(null, v.getNextVertexOfClass(AbstractSuperNode.class));
		assertEquals(null, v.getNextVertexOfClass(SubNode.class));
		assertEquals(null, v.getNextVertexOfClass(SuperNode.class));
		assertEquals(null, v.getNextVertexOfClass(DoubleSubNode.class));
	}

	/**
	 * The next vertex is an instance of a class which is a subclass of another
	 * vertexclass.
	 */
	@Test
	public void getNextVertexTestClass1() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createSubNode();
		assertEquals(v1, v0.getNextVertexOfClass(AbstractSuperNode.class));
		assertEquals(v1, v0.getNextVertexOfClass(SubNode.class));
		assertEquals(null, v0.getNextVertexOfClass(SuperNode.class));
		assertEquals(null, v0.getNextVertexOfClass(DoubleSubNode.class));
	}

	/**
	 * The next vertex is an instance of a class which is a subclass of tow
	 * other vertexclasses.
	 */
	@Test
	public void getNextVertexTestClass2() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		assertEquals(v1, v0.getNextVertexOfClass(AbstractSuperNode.class));
		assertEquals(v1, v0.getNextVertexOfClass(SubNode.class));
		assertEquals(v1, v0.getNextVertexOfClass(SuperNode.class));
		assertEquals(v1, v0.getNextVertexOfClass(DoubleSubNode.class));
	}

	/**
	 * Test in a manually build graph: SubNode SuperNode DoubleSubNode SuperNode
	 * SubNode SuperNode DoubleSubNode
	 */
	@Test
	public void getNextVertexTestClass3() {
		Vertex v0 = graph.createSubNode();
		Vertex v1 = graph.createSuperNode();
		Vertex v2 = graph.createDoubleSubNode();
		Vertex v3 = graph.createSuperNode();
		Vertex v4 = graph.createSubNode();
		Vertex v5 = graph.createSuperNode();
		Vertex v6 = graph.createDoubleSubNode();

		assertEquals(v2, v0.getNextVertexOfClass(AbstractSuperNode.class));
		assertEquals(v2, v0.getNextVertexOfClass(SubNode.class));
		assertEquals(v1, v0.getNextVertexOfClass(SuperNode.class));
		assertEquals(v2, v0.getNextVertexOfClass(DoubleSubNode.class));

		assertEquals(v2, v1.getNextVertexOfClass(AbstractSuperNode.class));
		assertEquals(v2, v1.getNextVertexOfClass(SubNode.class));
		assertEquals(v2, v1.getNextVertexOfClass(SuperNode.class));
		assertEquals(v2, v1.getNextVertexOfClass(DoubleSubNode.class));

		assertEquals(v4, v2.getNextVertexOfClass(AbstractSuperNode.class));
		assertEquals(v4, v2.getNextVertexOfClass(SubNode.class));
		assertEquals(v3, v2.getNextVertexOfClass(SuperNode.class));
		assertEquals(v6, v2.getNextVertexOfClass(DoubleSubNode.class));

		assertEquals(v4, v3.getNextVertexOfClass(AbstractSuperNode.class));
		assertEquals(v4, v3.getNextVertexOfClass(SubNode.class));
		assertEquals(v5, v3.getNextVertexOfClass(SuperNode.class));
		assertEquals(v6, v3.getNextVertexOfClass(DoubleSubNode.class));

		assertEquals(v6, v4.getNextVertexOfClass(AbstractSuperNode.class));
		assertEquals(v6, v4.getNextVertexOfClass(SubNode.class));
		assertEquals(v5, v4.getNextVertexOfClass(SuperNode.class));
		assertEquals(v6, v4.getNextVertexOfClass(DoubleSubNode.class));

		assertEquals(v6, v5.getNextVertexOfClass(AbstractSuperNode.class));
		assertEquals(v6, v5.getNextVertexOfClass(SubNode.class));
		assertEquals(v6, v5.getNextVertexOfClass(SuperNode.class));
		assertEquals(v6, v5.getNextVertexOfClass(DoubleSubNode.class));

		assertEquals(null, v6.getNextVertexOfClass(AbstractSuperNode.class));
		assertEquals(null, v6.getNextVertexOfClass(SubNode.class));
		assertEquals(null, v6.getNextVertexOfClass(SuperNode.class));
		assertEquals(null, v6.getNextVertexOfClass(DoubleSubNode.class));
	}

	/**
	 * RandomTests
	 */
	@Test
	public void getNextVertexTestClass4() {
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			// all vertices in the graph
			Vertex[] vertices = new Vertex[20];
			// the next AbstractSuperNode for the i-th Vertex
			Vertex[] nextAbstractSuperNode = new Vertex[vertices.length];
			// the position of the last Vertex which has a nextApstractSuperNode
			int lastAbstractSuperNode = -1;
			Vertex[] nextSubNode = new Vertex[vertices.length];
			int lastSubNode = -1;
			Vertex[] nextSuperNode = new Vertex[vertices.length];
			int lastSuperNode = -1;
			Vertex[] nextDoubleSubNode = new Vertex[vertices.length];
			int lastDoubleSubNode = -1;
			for (int j = 0; j < vertices.length; j++) {
				// create vertices
				int vclass = rand.nextInt(3);
				switch (vclass) {
				case 0:
					vertices[j] = graph.createSubNode();
					// all vertices until j(exclusive) have the new vertex as
					// next AbstractSuperNode
					while (lastAbstractSuperNode < j) {
						if (lastAbstractSuperNode >= 0) {
							nextAbstractSuperNode[lastAbstractSuperNode] = vertices[j];
						}
						lastAbstractSuperNode++;
					}
					// all vertices until j(exclusive) have the new vertex as
					// next SubNode
					while (lastSubNode < j) {
						if (lastSubNode >= 0) {
							nextSubNode[lastSubNode] = vertices[j];
						}
						lastSubNode++;
					}
					break;
				case 1:
					vertices[j] = graph.createSuperNode();
					// all vertices until j(exclusive) have the new vertex as
					// next SuperNode
					while (lastSuperNode < j) {
						if (lastSuperNode >= 0) {
							nextSuperNode[lastSuperNode] = vertices[j];
						}
						lastSuperNode++;
					}
					break;
				case 2:
					vertices[j] = graph.createDoubleSubNode();
					// all vertices until j(exclusive) have the new vertex as
					// next AbstractSuperNode
					while (lastAbstractSuperNode < j) {
						if (lastAbstractSuperNode >= 0) {
							nextAbstractSuperNode[lastAbstractSuperNode] = vertices[j];
						}
						lastAbstractSuperNode++;
					}
					// all vertices until j(exclusive) have the new vertex as
					// next SubNode
					while (lastSubNode < j) {
						if (lastSubNode >= 0) {
							nextSubNode[lastSubNode] = vertices[j];
						}
						lastSubNode++;
					}
					// all vertices until j(exclusive) have the new vertex as
					// next SuperNode
					while (lastSuperNode < j) {
						if (lastSuperNode >= 0) {
							nextSuperNode[lastSuperNode] = vertices[j];
						}
						lastSuperNode++;
					}
					// all vertices until j(exclusive) have the new vertex as
					// next DoubleSubNode
					while (lastDoubleSubNode < j) {
						if (lastDoubleSubNode >= 0) {
							nextDoubleSubNode[lastDoubleSubNode] = vertices[j];
						}
						lastDoubleSubNode++;
					}
					break;
				}
			}
			// check nextVertex after creating
			for (int j = 0; j < vertices.length; j++) {
				assertEquals(nextAbstractSuperNode[j], vertices[j]
						.getNextVertexOfClass(AbstractSuperNode.class));
				assertEquals(nextSubNode[j], vertices[j]
						.getNextVertexOfClass(SubNode.class));
				assertEquals(nextSuperNode[j], vertices[j]
						.getNextVertexOfClass(SuperNode.class));
				assertEquals(nextDoubleSubNode[j], vertices[j]
						.getNextVertexOfClass(DoubleSubNode.class));
			}
		}
	}

	// tests of the method getNextVertex(VertexClass aVertexClass, boolean
	// noSubclasses);

	/**
	 * Tests if there is only one vertex in the graph.
	 */
	@Test
	public void getNextVertexTestVertexClassBoolean0() {
		VertexClass[] vertices = getVertexClasses();
		Vertex v = graph.createSubNode();
		assertEquals(null, v.getNextVertexOfClass(vertices[0], false));
		assertEquals(null, v.getNextVertexOfClass(vertices[0], true));
		assertEquals(null, v.getNextVertexOfClass(vertices[1], false));
		assertEquals(null, v.getNextVertexOfClass(vertices[1], true));
		assertEquals(null, v.getNextVertexOfClass(vertices[2], false));
		assertEquals(null, v.getNextVertexOfClass(vertices[2], true));
		assertEquals(null, v.getNextVertexOfClass(vertices[3], false));
		assertEquals(null, v.getNextVertexOfClass(vertices[3], true));
	}

	/**
	 * The next vertex is an instance of a class which is a subclass of another
	 * vertexclass.
	 */
	@Test
	public void getNextVertexTestVertexClassBoolean1() {
		VertexClass[] vertices = getVertexClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createSubNode();
		assertEquals(v1, v0.getNextVertexOfClass(vertices[0], false));
		assertEquals(null, v0.getNextVertexOfClass(vertices[0], true));
		assertEquals(v1, v0.getNextVertexOfClass(vertices[1], false));
		assertEquals(v1, v0.getNextVertexOfClass(vertices[1], true));
		assertEquals(null, v0.getNextVertexOfClass(vertices[2], false));
		assertEquals(null, v0.getNextVertexOfClass(vertices[2], true));
		assertEquals(null, v0.getNextVertexOfClass(vertices[3], false));
		assertEquals(null, v0.getNextVertexOfClass(vertices[3], true));
	}

	/**
	 * The next vertex is an instance of a class which is a subclass of tow
	 * other vertexclasses.
	 */
	@Test
	public void getNextVertexTestVertexClassBoolean2() {
		VertexClass[] vertices = getVertexClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		assertEquals(v1, v0.getNextVertexOfClass(vertices[0], false));
		assertEquals(null, v0.getNextVertexOfClass(vertices[0], true));
		assertEquals(v1, v0.getNextVertexOfClass(vertices[1], false));
		assertEquals(null, v0.getNextVertexOfClass(vertices[1], true));
		assertEquals(v1, v0.getNextVertexOfClass(vertices[2], false));
		assertEquals(null, v0.getNextVertexOfClass(vertices[2], true));
		assertEquals(v1, v0.getNextVertexOfClass(vertices[3], false));
		assertEquals(v1, v0.getNextVertexOfClass(vertices[3], true));
	}

	/**
	 * Test in a manually build graph: SubNode SuperNode DoubleSubNode SuperNode
	 * SubNode SuperNode DoubleSubNode
	 */
	@Test
	public void getNextVertexTestVertexClassBoolean3() {
		VertexClass[] vertices = getVertexClasses();
		Vertex v0 = graph.createSubNode();
		Vertex v1 = graph.createSuperNode();
		Vertex v2 = graph.createDoubleSubNode();
		Vertex v3 = graph.createSuperNode();
		Vertex v4 = graph.createSubNode();
		Vertex v5 = graph.createSuperNode();
		Vertex v6 = graph.createDoubleSubNode();

		assertEquals(v2, v0.getNextVertexOfClass(vertices[0], false));
		assertEquals(v2, v0.getNextVertexOfClass(vertices[1], false));
		assertEquals(v1, v0.getNextVertexOfClass(vertices[2], false));
		assertEquals(v2, v0.getNextVertexOfClass(vertices[3], false));
		assertEquals(null, v0.getNextVertexOfClass(vertices[0], true));
		assertEquals(v4, v0.getNextVertexOfClass(vertices[1], true));
		assertEquals(v1, v0.getNextVertexOfClass(vertices[2], true));
		assertEquals(v2, v0.getNextVertexOfClass(vertices[3], true));

		assertEquals(v2, v1.getNextVertexOfClass(vertices[0], false));
		assertEquals(v2, v1.getNextVertexOfClass(vertices[1], false));
		assertEquals(v2, v1.getNextVertexOfClass(vertices[2], false));
		assertEquals(v2, v1.getNextVertexOfClass(vertices[3], false));
		assertEquals(null, v1.getNextVertexOfClass(vertices[0], true));
		assertEquals(v4, v1.getNextVertexOfClass(vertices[1], true));
		assertEquals(v3, v1.getNextVertexOfClass(vertices[2], true));
		assertEquals(v2, v1.getNextVertexOfClass(vertices[3], true));

		assertEquals(v4, v2.getNextVertexOfClass(vertices[0], false));
		assertEquals(v4, v2.getNextVertexOfClass(vertices[1], false));
		assertEquals(v3, v2.getNextVertexOfClass(vertices[2], false));
		assertEquals(v6, v2.getNextVertexOfClass(vertices[3], false));
		assertEquals(null, v2.getNextVertexOfClass(vertices[0], true));
		assertEquals(v4, v2.getNextVertexOfClass(vertices[1], true));
		assertEquals(v3, v2.getNextVertexOfClass(vertices[2], true));
		assertEquals(v6, v2.getNextVertexOfClass(vertices[3], true));

		assertEquals(v4, v3.getNextVertexOfClass(vertices[0], false));
		assertEquals(v4, v3.getNextVertexOfClass(vertices[1], false));
		assertEquals(v5, v3.getNextVertexOfClass(vertices[2], false));
		assertEquals(v6, v3.getNextVertexOfClass(vertices[3], false));
		assertEquals(null, v3.getNextVertexOfClass(vertices[0], true));
		assertEquals(v4, v3.getNextVertexOfClass(vertices[1], true));
		assertEquals(v5, v3.getNextVertexOfClass(vertices[2], true));
		assertEquals(v6, v3.getNextVertexOfClass(vertices[3], true));

		assertEquals(v6, v4.getNextVertexOfClass(vertices[0], false));
		assertEquals(v6, v4.getNextVertexOfClass(vertices[1], false));
		assertEquals(v5, v4.getNextVertexOfClass(vertices[2], false));
		assertEquals(v6, v4.getNextVertexOfClass(vertices[3], false));
		assertEquals(null, v4.getNextVertexOfClass(vertices[0], true));
		assertEquals(null, v4.getNextVertexOfClass(vertices[1], true));
		assertEquals(v5, v4.getNextVertexOfClass(vertices[2], true));
		assertEquals(v6, v4.getNextVertexOfClass(vertices[3], true));

		assertEquals(v6, v5.getNextVertexOfClass(vertices[0], false));
		assertEquals(v6, v5.getNextVertexOfClass(vertices[1], false));
		assertEquals(v6, v5.getNextVertexOfClass(vertices[2], false));
		assertEquals(v6, v5.getNextVertexOfClass(vertices[3], false));
		assertEquals(null, v5.getNextVertexOfClass(vertices[0], true));
		assertEquals(null, v5.getNextVertexOfClass(vertices[1], true));
		assertEquals(null, v5.getNextVertexOfClass(vertices[2], true));
		assertEquals(v6, v5.getNextVertexOfClass(vertices[3], true));

		assertEquals(null, v6.getNextVertexOfClass(vertices[0], false));
		assertEquals(null, v6.getNextVertexOfClass(vertices[1], false));
		assertEquals(null, v6.getNextVertexOfClass(vertices[2], false));
		assertEquals(null, v6.getNextVertexOfClass(vertices[3], false));
		assertEquals(null, v6.getNextVertexOfClass(vertices[0], true));
		assertEquals(null, v6.getNextVertexOfClass(vertices[1], true));
		assertEquals(null, v6.getNextVertexOfClass(vertices[2], true));
		assertEquals(null, v6.getNextVertexOfClass(vertices[3], true));
	}

	/**
	 * RandomTests
	 */
	@Test
	public void getNextVertexTestVertexClassBoolean4() {
		VertexClass[] vClasses = getVertexClasses();
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			// all vertices in the graph
			Vertex[] vertices = new Vertex[20];
			// the next AbstractSuperNode for the i-th Vertex
			Vertex[] nextAbstractSuperNodeFalse = new Vertex[vertices.length];
			// the position of the last Vertex which has a next
			// AbstractSuperNode
			int lastAbstractSuperNodeFalse = -1;
			// the next AbstractSuperNode(no subclasses) for the i-th Vertex
			// Vertex[] nextAbstractSuperNodeTrue = new Vertex[vertices.length];
			// the position of the last Vertex which has a next
			// AbstractSuperNode(no subclasses)
			// int lastAbstractSuperNodeTrue = -1;
			Vertex[] nextSubNodeFalse = new Vertex[vertices.length];
			int lastSubNodeFalse = -1;
			Vertex[] nextSubNodeTrue = new Vertex[vertices.length];
			int lastSubNodeTrue = -1;
			Vertex[] nextSuperNodeFalse = new Vertex[vertices.length];
			int lastSuperNodeFalse = -1;
			Vertex[] nextSuperNodeTrue = new Vertex[vertices.length];
			int lastSuperNodeTrue = -1;
			Vertex[] nextDoubleSubNodeFalse = new Vertex[vertices.length];
			int lastDoubleSubNodeFalse = -1;
			Vertex[] nextDoubleSubNodeTrue = new Vertex[vertices.length];
			int lastDoubleSubNodeTrue = -1;
			for (int j = 0; j < vertices.length; j++) {
				// create vertices
				int vclass = rand.nextInt(3);
				switch (vclass) {
				case 0:
					vertices[j] = graph.createSubNode();
					// all vertices until j(exclusive) have the new vertex as
					// next AbstractSuperNode
					while (lastAbstractSuperNodeFalse < j) {
						if (lastAbstractSuperNodeFalse >= 0) {
							nextAbstractSuperNodeFalse[lastAbstractSuperNodeFalse] = vertices[j];
						}
						lastAbstractSuperNodeFalse++;
					}
					// all vertices until j(exclusive) have the new vertex as
					// next SubNode
					while (lastSubNodeFalse < j) {
						if (lastSubNodeFalse >= 0) {
							nextSubNodeFalse[lastSubNodeFalse] = vertices[j];
						}
						lastSubNodeFalse++;
					}
					// with no subclasses
					while (lastSubNodeTrue < j) {
						if (lastSubNodeTrue >= 0) {
							nextSubNodeTrue[lastSubNodeTrue] = vertices[j];
						}
						lastSubNodeTrue++;
					}
					break;
				case 1:
					vertices[j] = graph.createSuperNode();
					// all vertices until j(exclusive) have the new vertex as
					// next SuperNode
					while (lastSuperNodeFalse < j) {
						if (lastSuperNodeFalse >= 0) {
							nextSuperNodeFalse[lastSuperNodeFalse] = vertices[j];
						}
						lastSuperNodeFalse++;
					}
					// with no subclasses
					while (lastSuperNodeTrue < j) {
						if (lastSuperNodeTrue >= 0) {
							nextSuperNodeTrue[lastSuperNodeTrue] = vertices[j];
						}
						lastSuperNodeTrue++;
					}
					break;
				case 2:
					vertices[j] = graph.createDoubleSubNode();
					// all vertices until j(exclusive) have the new vertex as
					// next AbstractSuperNode
					while (lastAbstractSuperNodeFalse < j) {
						if (lastAbstractSuperNodeFalse >= 0) {
							nextAbstractSuperNodeFalse[lastAbstractSuperNodeFalse] = vertices[j];
						}
						lastAbstractSuperNodeFalse++;
					}
					// all vertices until j(exclusive) have the new vertex as
					// next SubNode
					while (lastSubNodeFalse < j) {
						if (lastSubNodeFalse >= 0) {
							nextSubNodeFalse[lastSubNodeFalse] = vertices[j];
						}
						lastSubNodeFalse++;
					}
					// all vertices until j(exclusive) have the new vertex as
					// next SuperNode
					while (lastSuperNodeFalse < j) {
						if (lastSuperNodeFalse >= 0) {
							nextSuperNodeFalse[lastSuperNodeFalse] = vertices[j];
						}
						lastSuperNodeFalse++;
					}
					// all vertices until j(exclusive) have the new vertex as
					// next DoubleSubNode
					while (lastDoubleSubNodeFalse < j) {
						if (lastDoubleSubNodeFalse >= 0) {
							nextDoubleSubNodeFalse[lastDoubleSubNodeFalse] = vertices[j];
						}
						lastDoubleSubNodeFalse++;
					}
					// with no subclasses
					while (lastDoubleSubNodeTrue < j) {
						if (lastDoubleSubNodeTrue >= 0) {
							nextDoubleSubNodeTrue[lastDoubleSubNodeTrue] = vertices[j];
						}
						lastDoubleSubNodeTrue++;
					}
					break;
				}
			}
			// check nextVertex after creating
			for (int j = 0; j < vertices.length; j++) {
				assertEquals(nextAbstractSuperNodeFalse[j], vertices[j]
						.getNextVertexOfClass(vClasses[0], false));
				assertEquals(nextSubNodeFalse[j], vertices[j]
						.getNextVertexOfClass(vClasses[1], false));
				assertEquals(nextSuperNodeFalse[j], vertices[j]
						.getNextVertexOfClass(vClasses[2], false));
				assertEquals(nextDoubleSubNodeFalse[j], vertices[j]
						.getNextVertexOfClass(vClasses[3], false));
				assertEquals(null, vertices[j].getNextVertexOfClass(
						vClasses[0], true));
				assertEquals(nextSubNodeTrue[j], vertices[j]
						.getNextVertexOfClass(vClasses[1], true));
				assertEquals(nextSuperNodeTrue[j], vertices[j]
						.getNextVertexOfClass(vClasses[2], true));
				assertEquals(nextDoubleSubNodeTrue[j], vertices[j]
						.getNextVertexOfClass(vClasses[3], true));
			}
		}
	}

	// tests of the method getNextVertex(Class<? extends Vertex> aM1VertexClass,
	// boolean
	// noSubclasses);

	/**
	 * Tests if there is only one vertex in the graph.
	 */
	@Test
	public void getNextVertexTestClassBoolean0() {
		Vertex v = graph.createSubNode();
		assertEquals(null, v.getNextVertexOfClass(AbstractSuperNode.class,
				false));
		assertEquals(null, v
				.getNextVertexOfClass(AbstractSuperNode.class, true));
		assertEquals(null, v.getNextVertexOfClass(SubNode.class, false));
		assertEquals(null, v.getNextVertexOfClass(SubNode.class, true));
		assertEquals(null, v.getNextVertexOfClass(SuperNode.class, false));
		assertEquals(null, v.getNextVertexOfClass(SuperNode.class, true));
		assertEquals(null, v.getNextVertexOfClass(DoubleSubNode.class, false));
		assertEquals(null, v.getNextVertexOfClass(DoubleSubNode.class, true));
	}

	/**
	 * The next vertex is an instance of a class which is a subclass of another
	 * vertexclass.
	 */
	@Test
	public void getNextVertexTestClassBoolean1() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createSubNode();
		assertEquals(v1, v0
				.getNextVertexOfClass(AbstractSuperNode.class, false));
		assertEquals(null, v0.getNextVertexOfClass(AbstractSuperNode.class,
				true));
		assertEquals(v1, v0.getNextVertexOfClass(SubNode.class, false));
		assertEquals(v1, v0.getNextVertexOfClass(SubNode.class, true));
		assertEquals(null, v0.getNextVertexOfClass(SuperNode.class, false));
		assertEquals(null, v0.getNextVertexOfClass(SuperNode.class, true));
		assertEquals(null, v0.getNextVertexOfClass(DoubleSubNode.class, false));
		assertEquals(null, v0.getNextVertexOfClass(DoubleSubNode.class, true));
	}

	/**
	 * The next vertex is an instance of a class which is a subclass of tow
	 * other vertexclasses.
	 */
	@Test
	public void getNextVertexTestClassBoolean2() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		assertEquals(v1, v0
				.getNextVertexOfClass(AbstractSuperNode.class, false));
		assertEquals(null, v0.getNextVertexOfClass(AbstractSuperNode.class,
				true));
		assertEquals(v1, v0.getNextVertexOfClass(SubNode.class, false));
		assertEquals(null, v0.getNextVertexOfClass(SubNode.class, true));
		assertEquals(v1, v0.getNextVertexOfClass(SuperNode.class, false));
		assertEquals(null, v0.getNextVertexOfClass(SuperNode.class, true));
		assertEquals(v1, v0.getNextVertexOfClass(DoubleSubNode.class, false));
		assertEquals(v1, v0.getNextVertexOfClass(DoubleSubNode.class, true));
	}

	/**
	 * Test in a manually build graph: SubNode SuperNode DoubleSubNode SuperNode
	 * SubNode SuperNode DoubleSubNode
	 */
	@Test
	public void getNextVertexTestClassBoolean3() {
		Vertex v0 = graph.createSubNode();
		Vertex v1 = graph.createSuperNode();
		Vertex v2 = graph.createDoubleSubNode();
		Vertex v3 = graph.createSuperNode();
		Vertex v4 = graph.createSubNode();
		Vertex v5 = graph.createSuperNode();
		Vertex v6 = graph.createDoubleSubNode();

		assertEquals(v2, v0
				.getNextVertexOfClass(AbstractSuperNode.class, false));
		assertEquals(v2, v0.getNextVertexOfClass(SubNode.class, false));
		assertEquals(v1, v0.getNextVertexOfClass(SuperNode.class, false));
		assertEquals(v2, v0.getNextVertexOfClass(DoubleSubNode.class, false));
		assertEquals(null, v0.getNextVertexOfClass(AbstractSuperNode.class,
				true));
		assertEquals(v4, v0.getNextVertexOfClass(SubNode.class, true));
		assertEquals(v1, v0.getNextVertexOfClass(SuperNode.class, true));
		assertEquals(v2, v0.getNextVertexOfClass(DoubleSubNode.class, true));

		assertEquals(v2, v1
				.getNextVertexOfClass(AbstractSuperNode.class, false));
		assertEquals(v2, v1.getNextVertexOfClass(SubNode.class, false));
		assertEquals(v2, v1.getNextVertexOfClass(SuperNode.class, false));
		assertEquals(v2, v1.getNextVertexOfClass(DoubleSubNode.class, false));
		assertEquals(null, v1.getNextVertexOfClass(AbstractSuperNode.class,
				true));
		assertEquals(v4, v1.getNextVertexOfClass(SubNode.class, true));
		assertEquals(v3, v1.getNextVertexOfClass(SuperNode.class, true));
		assertEquals(v2, v1.getNextVertexOfClass(DoubleSubNode.class, true));

		assertEquals(v4, v2
				.getNextVertexOfClass(AbstractSuperNode.class, false));
		assertEquals(v4, v2.getNextVertexOfClass(SubNode.class, false));
		assertEquals(v3, v2.getNextVertexOfClass(SuperNode.class, false));
		assertEquals(v6, v2.getNextVertexOfClass(DoubleSubNode.class, false));
		assertEquals(null, v2.getNextVertexOfClass(AbstractSuperNode.class,
				true));
		assertEquals(v4, v2.getNextVertexOfClass(SubNode.class, true));
		assertEquals(v3, v2.getNextVertexOfClass(SuperNode.class, true));
		assertEquals(v6, v2.getNextVertexOfClass(DoubleSubNode.class, true));

		assertEquals(v4, v3
				.getNextVertexOfClass(AbstractSuperNode.class, false));
		assertEquals(v4, v3.getNextVertexOfClass(SubNode.class, false));
		assertEquals(v5, v3.getNextVertexOfClass(SuperNode.class, false));
		assertEquals(v6, v3.getNextVertexOfClass(DoubleSubNode.class, false));
		assertEquals(null, v3.getNextVertexOfClass(AbstractSuperNode.class,
				true));
		assertEquals(v4, v3.getNextVertexOfClass(SubNode.class, true));
		assertEquals(v5, v3.getNextVertexOfClass(SuperNode.class, true));
		assertEquals(v6, v3.getNextVertexOfClass(DoubleSubNode.class, true));

		assertEquals(v6, v4
				.getNextVertexOfClass(AbstractSuperNode.class, false));
		assertEquals(v6, v4.getNextVertexOfClass(SubNode.class, false));
		assertEquals(v5, v4.getNextVertexOfClass(SuperNode.class, false));
		assertEquals(v6, v4.getNextVertexOfClass(DoubleSubNode.class, false));
		assertEquals(null, v4.getNextVertexOfClass(AbstractSuperNode.class,
				true));
		assertEquals(null, v4.getNextVertexOfClass(SubNode.class, true));
		assertEquals(v5, v4.getNextVertexOfClass(SuperNode.class, true));
		assertEquals(v6, v4.getNextVertexOfClass(DoubleSubNode.class, true));

		assertEquals(v6, v5
				.getNextVertexOfClass(AbstractSuperNode.class, false));
		assertEquals(v6, v5.getNextVertexOfClass(SubNode.class, false));
		assertEquals(v6, v5.getNextVertexOfClass(SuperNode.class, false));
		assertEquals(v6, v5.getNextVertexOfClass(DoubleSubNode.class, false));
		assertEquals(null, v5.getNextVertexOfClass(AbstractSuperNode.class,
				true));
		assertEquals(null, v5.getNextVertexOfClass(SubNode.class, true));
		assertEquals(null, v5.getNextVertexOfClass(SuperNode.class, true));
		assertEquals(v6, v5.getNextVertexOfClass(DoubleSubNode.class, true));

		assertEquals(null, v6.getNextVertexOfClass(AbstractSuperNode.class,
				false));
		assertEquals(null, v6.getNextVertexOfClass(SubNode.class, false));
		assertEquals(null, v6.getNextVertexOfClass(SuperNode.class, false));
		assertEquals(null, v6.getNextVertexOfClass(DoubleSubNode.class, false));
		assertEquals(null, v6.getNextVertexOfClass(AbstractSuperNode.class,
				true));
		assertEquals(null, v6.getNextVertexOfClass(SubNode.class, true));
		assertEquals(null, v6.getNextVertexOfClass(SuperNode.class, true));
		assertEquals(null, v6.getNextVertexOfClass(DoubleSubNode.class, true));
	}

	/**
	 * RandomTests
	 */
	@Test
	public void getNextVertexTestVertexBoolean4() {
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			// all vertices in the graph
			Vertex[] vertices = new Vertex[20];
			// the next AbstractSuperNode for the i-th Vertex
			Vertex[] nextAbstractSuperNodeFalse = new Vertex[vertices.length];
			// the position of the last Vertex which has a next
			// AbstractSuperNode
			int lastAbstractSuperNodeFalse = -1;
			// the next AbstractSuperNode(no subclasses) for the i-th Vertex
			// Vertex[] nextAbstractSuperNodeTrue = new Vertex[vertices.length];
			// the position of the last Vertex which has a next
			// AbstractSuperNode(no subclasses)
			// int lastAbstractSuperNodeTrue = -1;
			Vertex[] nextSubNodeFalse = new Vertex[vertices.length];
			int lastSubNodeFalse = -1;
			Vertex[] nextSubNodeTrue = new Vertex[vertices.length];
			int lastSubNodeTrue = -1;
			Vertex[] nextSuperNodeFalse = new Vertex[vertices.length];
			int lastSuperNodeFalse = -1;
			Vertex[] nextSuperNodeTrue = new Vertex[vertices.length];
			int lastSuperNodeTrue = -1;
			Vertex[] nextDoubleSubNodeFalse = new Vertex[vertices.length];
			int lastDoubleSubNodeFalse = -1;
			Vertex[] nextDoubleSubNodeTrue = new Vertex[vertices.length];
			int lastDoubleSubNodeTrue = -1;
			for (int j = 0; j < vertices.length; j++) {
				// create vertices
				int vclass = rand.nextInt(3);
				switch (vclass) {
				case 0:
					vertices[j] = graph.createSubNode();
					// all vertices until j(exclusive) have the new vertex as
					// next AbstractSuperNode
					while (lastAbstractSuperNodeFalse < j) {
						if (lastAbstractSuperNodeFalse >= 0) {
							nextAbstractSuperNodeFalse[lastAbstractSuperNodeFalse] = vertices[j];
						}
						lastAbstractSuperNodeFalse++;
					}
					// all vertices until j(exclusive) have the new vertex as
					// next SubNode
					while (lastSubNodeFalse < j) {
						if (lastSubNodeFalse >= 0) {
							nextSubNodeFalse[lastSubNodeFalse] = vertices[j];
						}
						lastSubNodeFalse++;
					}
					// with no subclasses
					while (lastSubNodeTrue < j) {
						if (lastSubNodeTrue >= 0) {
							nextSubNodeTrue[lastSubNodeTrue] = vertices[j];
						}
						lastSubNodeTrue++;
					}
					break;
				case 1:
					vertices[j] = graph.createSuperNode();
					// all vertices until j(exclusive) have the new vertex as
					// next SuperNode
					while (lastSuperNodeFalse < j) {
						if (lastSuperNodeFalse >= 0) {
							nextSuperNodeFalse[lastSuperNodeFalse] = vertices[j];
						}
						lastSuperNodeFalse++;
					}
					// with no subclasses
					while (lastSuperNodeTrue < j) {
						if (lastSuperNodeTrue >= 0) {
							nextSuperNodeTrue[lastSuperNodeTrue] = vertices[j];
						}
						lastSuperNodeTrue++;
					}
					break;
				case 2:
					vertices[j] = graph.createDoubleSubNode();
					// all vertices until j(exclusive) have the new vertex as
					// next AbstractSuperNode
					while (lastAbstractSuperNodeFalse < j) {
						if (lastAbstractSuperNodeFalse >= 0) {
							nextAbstractSuperNodeFalse[lastAbstractSuperNodeFalse] = vertices[j];
						}
						lastAbstractSuperNodeFalse++;
					}
					// all vertices until j(exclusive) have the new vertex as
					// next SubNode
					while (lastSubNodeFalse < j) {
						if (lastSubNodeFalse >= 0) {
							nextSubNodeFalse[lastSubNodeFalse] = vertices[j];
						}
						lastSubNodeFalse++;
					}
					// all vertices until j(exclusive) have the new vertex as
					// next SuperNode
					while (lastSuperNodeFalse < j) {
						if (lastSuperNodeFalse >= 0) {
							nextSuperNodeFalse[lastSuperNodeFalse] = vertices[j];
						}
						lastSuperNodeFalse++;
					}
					// all vertices until j(exclusive) have the new vertex as
					// next DoubleSubNode
					while (lastDoubleSubNodeFalse < j) {
						if (lastDoubleSubNodeFalse >= 0) {
							nextDoubleSubNodeFalse[lastDoubleSubNodeFalse] = vertices[j];
						}
						lastDoubleSubNodeFalse++;
					}
					// with no subclasses
					while (lastDoubleSubNodeTrue < j) {
						if (lastDoubleSubNodeTrue >= 0) {
							nextDoubleSubNodeTrue[lastDoubleSubNodeTrue] = vertices[j];
						}
						lastDoubleSubNodeTrue++;
					}
					break;
				}
			}
			// check nextVertex after creating
			for (int j = 0; j < vertices.length; j++) {
				assertEquals(nextAbstractSuperNodeFalse[j], vertices[j]
						.getNextVertexOfClass(AbstractSuperNode.class, false));
				assertEquals(nextSubNodeFalse[j], vertices[j]
						.getNextVertexOfClass(SubNode.class, false));
				assertEquals(nextSuperNodeFalse[j], vertices[j]
						.getNextVertexOfClass(SuperNode.class, false));
				assertEquals(nextDoubleSubNodeFalse[j], vertices[j]
						.getNextVertexOfClass(DoubleSubNode.class, false));
				assertEquals(null, vertices[j].getNextVertexOfClass(
						AbstractSuperNode.class, true));
				assertEquals(nextSubNodeTrue[j], vertices[j]
						.getNextVertexOfClass(SubNode.class, true));
				assertEquals(nextSuperNodeTrue[j], vertices[j]
						.getNextVertexOfClass(SuperNode.class, true));
				assertEquals(nextDoubleSubNodeTrue[j], vertices[j]
						.getNextVertexOfClass(DoubleSubNode.class, true));
			}
		}
	}

	// tests of the method Edge getLastEdge();
	// (tested in IncidenceListTest)

	// tests of the method Edge getFirstEdge();
	// (tested in IncidenceListTest)

	// tests of the method Edge getFirstEdge(EdgeDirection orientation);

	/**
	 * Tests if a node has no Edges
	 */
	@Test
	public void getFirstEdgeTestEdgeDirection0() {
		Vertex v0 = graph.createDoubleSubNode();
		assertEquals(null, v0.getFirstEdge(EdgeDirection.INOUT));
		assertEquals(null, v0.getFirstEdge(EdgeDirection.IN));
		assertEquals(null, v0.getFirstEdge(EdgeDirection.OUT));
	}

	/**
	 * Tests if a node has only one Edge
	 */
	@Test
	public void getFirstEdgeTestEdgeDirection1() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		assertEquals(e.getReversedEdge(), v1.getFirstEdge(EdgeDirection.INOUT));
		assertEquals(e.getReversedEdge(), v1.getFirstEdge(EdgeDirection.IN));
		assertEquals(null, v1.getFirstEdge(EdgeDirection.OUT));
		assertEquals(e, v0.getFirstEdge(EdgeDirection.INOUT));
		assertEquals(null, v0.getFirstEdge(EdgeDirection.IN));
		assertEquals(e, v0.getFirstEdge(EdgeDirection.OUT));
	}

	/**
	 * Tests if a node has two Edges with the same direction.
	 */
	@Test
	public void getFirstEdgeTestEdgeDirection2() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		assertEquals(e1.getReversedEdge(), v1.getFirstEdge(EdgeDirection.INOUT));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdge(EdgeDirection.IN));
		assertEquals(null, v1.getFirstEdge(EdgeDirection.OUT));
		assertEquals(e1, v0.getFirstEdge(EdgeDirection.INOUT));
		assertEquals(null, v0.getFirstEdge(EdgeDirection.IN));
		assertEquals(e1, v0.getFirstEdge(EdgeDirection.OUT));
	}

	/**
	 * Tests if a node has two Edges with different direction.
	 */
	@Test
	public void getFirstEdgeTestEdgeDirection3() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		Edge e2 = graph.createLinkBack((SuperNode) v1, (AbstractSuperNode) v0);
		assertEquals(e1.getReversedEdge(), v1.getFirstEdge(EdgeDirection.INOUT));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdge(EdgeDirection.IN));
		assertEquals(e2, v1.getFirstEdge(EdgeDirection.OUT));
		assertEquals(e1, v0.getFirstEdge(EdgeDirection.INOUT));
		assertEquals(e2.getReversedEdge(), v0.getFirstEdge(EdgeDirection.IN));
		assertEquals(e1, v0.getFirstEdge(EdgeDirection.OUT));
	}

	/**
	 * Tests if alpha and omega of an Edge is the same Vertex.
	 */
	@Test
	public void getFirstEdgeTestEdgeDirection4() {
		Vertex v0 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink((AbstractSuperNode) v0, (SuperNode) v0);
		assertEquals(e1, v0.getFirstEdge(EdgeDirection.INOUT));
		assertEquals(e1.getReversedEdge(), v0.getFirstEdge(EdgeDirection.IN));
		assertEquals(e1, v0.getFirstEdge(EdgeDirection.OUT));
	}

	/**
	 * Random tests
	 */
	@Test
	public void getFirstEdgeTestEdgeDirection5() {
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			Vertex[] vertices = new Vertex[] { graph.createSubNode(),
					graph.createDoubleSubNode(), graph.createSuperNode() };
			Edge[] firstInEdge = new Edge[3];
			Edge[] firstOutEdge = new Edge[3];
			Edge[] firstInOutEdge = new Edge[3];
			for (int j = 0; j < 5; j++) {
				int edgetype = rand.nextInt(3);
				int start = rand.nextInt(2);
				int end = rand.nextInt(2) + 1;
				switch (edgetype) {
				case 0:
					Edge e0 = graph.createLink(
							(AbstractSuperNode) vertices[start],
							(SuperNode) vertices[end]);
					if (firstOutEdge[start] == null) {
						firstOutEdge[start] = e0;
					}
					if (firstInOutEdge[start] == null) {
						firstInOutEdge[start] = e0;
					}
					if (firstInEdge[end] == null) {
						firstInEdge[end] = e0.getReversedEdge();
					}
					if (firstInOutEdge[end] == null && start != end) {
						firstInOutEdge[end] = e0.getReversedEdge();
					}
					break;
				case 1:
					Edge e1 = graph.createLinkBack((SuperNode) vertices[end],
							(AbstractSuperNode) vertices[start]);
					if (firstOutEdge[end] == null) {
						firstOutEdge[end] = e1;
					}
					if (firstInOutEdge[end] == null) {
						firstInOutEdge[end] = e1;
					}
					if (firstInEdge[start] == null) {
						firstInEdge[start] = e1.getReversedEdge();
					}
					if (firstInOutEdge[start] == null && start != end) {
						firstInOutEdge[start] = e1.getReversedEdge();
					}
					break;
				case 2:
					Edge e2 = graph.createSubLink((DoubleSubNode) vertices[1],
							(SuperNode) vertices[end]);
					if (firstOutEdge[1] == null) {
						firstOutEdge[1] = e2;
					}
					if (firstInOutEdge[1] == null) {
						firstInOutEdge[1] = e2;
					}
					if (firstInEdge[end] == null) {
						firstInEdge[end] = e2.getReversedEdge();
					}
					if (firstInOutEdge[end] == null && 1 != end) {
						firstInOutEdge[end] = e2.getReversedEdge();
					}
					break;
				}
			}
			assertEquals(firstInEdge[0], vertices[0]
					.getFirstEdge(EdgeDirection.IN));
			assertEquals(firstInEdge[1], vertices[1]
					.getFirstEdge(EdgeDirection.IN));
			assertEquals(firstInEdge[2], vertices[2]
					.getFirstEdge(EdgeDirection.IN));
			assertEquals(firstOutEdge[0], vertices[0]
					.getFirstEdge(EdgeDirection.OUT));
			assertEquals(firstOutEdge[1], vertices[1]
					.getFirstEdge(EdgeDirection.OUT));
			assertEquals(firstOutEdge[2], vertices[2]
					.getFirstEdge(EdgeDirection.OUT));
			assertEquals(firstInOutEdge[0], vertices[0]
					.getFirstEdge(EdgeDirection.INOUT));
			assertEquals(firstInOutEdge[1], vertices[1]
					.getFirstEdge(EdgeDirection.INOUT));
			assertEquals(firstInOutEdge[2], vertices[2]
					.getFirstEdge(EdgeDirection.INOUT));
		}
	}

	// tests of the method Edge getFirstEdgeOfClass(EdgeClass anEdgeClass);

	/**
	 * Tests if a node has no Edges
	 */
	@Test
	public void getFirstEdgeTestEdgeClass0() {
		EdgeClass[] eclasses = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[0]));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1]));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2]));
	}

	/**
	 * Tests if a node has only one Edge
	 */
	@Test
	public void getFirstEdgeTestEdgeClass1() {
		EdgeClass[] eclasses = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		assertEquals(e, v0.getFirstEdgeOfClass(eclasses[0]));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1]));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2]));
		assertEquals(e.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0]));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1]));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2]));
	}

	/**
	 * Tests if a node has an edge which extends another edge
	 */
	@Test
	public void getFirstEdgeTestEdgeClass2() {
		EdgeClass[] eclasses = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e1 = graph.createSubLink((DoubleSubNode) v0, (SuperNode) v1);
		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0]));
		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[1]));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2]));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0]));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[1]));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2]));
	}

	/**
	 * Tests if a node has two Edges.
	 */
	@Test
	public void getFirstEdgeTestEdgeClass3() {
		EdgeClass[] eclasses = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		Edge e2 = graph.createLinkBack((SuperNode) v1, (AbstractSuperNode) v0);
		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0]));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1]));
		assertEquals(e2.getReversedEdge(), v0.getFirstEdgeOfClass(eclasses[2]));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0]));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1]));
		assertEquals(e2, v1.getFirstEdgeOfClass(eclasses[2]));
	}

	/**
	 * Tests if alpha and omega of an Edge is the same Vertex.
	 */
	@Test
	public void getFirstEdgeTestEdgeClass4() {
		EdgeClass[] eclasses = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink((AbstractSuperNode) v0, (SuperNode) v0);
		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0]));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1]));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2]));
	}

	/**
	 * Random tests
	 */
	@Test
	public void getFirstEdgeTestEdgeClass5() {
		EdgeClass[] eclasses = getEdgeClasses();
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			Vertex[] vertices = new Vertex[] { graph.createSubNode(),
					graph.createDoubleSubNode(), graph.createSuperNode() };
			Edge[] firstLink = new Edge[3];
			Edge[] firstLinkBack = new Edge[3];
			Edge[] firstSubLink = new Edge[3];
			for (int j = 0; j < 5; j++) {
				int edgetype = rand.nextInt(3);
				int start = rand.nextInt(2);
				int end = rand.nextInt(2) + 1;
				switch (edgetype) {
				case 0:
					Edge e0 = graph.createLink(
							(AbstractSuperNode) vertices[start],
							(SuperNode) vertices[end]);
					if (firstLink[start] == null) {
						firstLink[start] = e0;
					}
					if (firstLink[end] == null) {
						firstLink[end] = e0.getReversedEdge();
					}
					break;
				case 1:
					Edge e1 = graph.createLinkBack((SuperNode) vertices[end],
							(AbstractSuperNode) vertices[start]);
					if (firstLinkBack[end] == null) {
						firstLinkBack[end] = e1;
					}
					if (firstLinkBack[start] == null) {
						firstLinkBack[start] = e1.getReversedEdge();
					}
					break;
				case 2:
					Edge e2 = graph.createSubLink((DoubleSubNode) vertices[1],
							(SuperNode) vertices[end]);
					if (firstLink[1] == null) {
						firstLink[1] = e2;
					}
					if (firstSubLink[1] == null) {
						firstSubLink[1] = e2;
					}
					if (firstLink[end] == null) {
						firstLink[end] = e2.getReversedEdge();
					}
					if (firstSubLink[end] == null) {
						firstSubLink[end] = e2.getReversedEdge();
					}
					break;
				}
			}
			assertEquals(firstLink[0], vertices[0]
					.getFirstEdgeOfClass(eclasses[0]));
			assertEquals(firstLink[1], vertices[1]
					.getFirstEdgeOfClass(eclasses[0]));
			assertEquals(firstLink[2], vertices[2]
					.getFirstEdgeOfClass(eclasses[0]));
			assertEquals(firstLinkBack[0], vertices[0]
					.getFirstEdgeOfClass(eclasses[2]));
			assertEquals(firstLinkBack[1], vertices[1]
					.getFirstEdgeOfClass(eclasses[2]));
			assertEquals(firstLinkBack[2], vertices[2]
					.getFirstEdgeOfClass(eclasses[2]));
			assertEquals(firstSubLink[0], vertices[0]
					.getFirstEdgeOfClass(eclasses[1]));
			assertEquals(firstSubLink[1], vertices[1]
					.getFirstEdgeOfClass(eclasses[1]));
			assertEquals(firstSubLink[2], vertices[2]
					.getFirstEdgeOfClass(eclasses[1]));
		}
	}

	// tests of the method Edge getFirstEdgeOfClass(Class<? extends Edge>
	// anEdgeClass);

	/**
	 * Tests if a node has no Edges
	 */
	@Test
	public void getFirstEdgeTestClass0() {
		Vertex v0 = graph.createDoubleSubNode();
		assertEquals(null, v0.getFirstEdgeOfClass(Link.class));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class));
	}

	/**
	 * Tests if a node has only one Edge
	 */
	@Test
	public void getFirstEdgeTestClass1() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		assertEquals(e, v0.getFirstEdgeOfClass(Link.class));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class));
		assertEquals(e.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class));
	}

	/**
	 * Tests if a node has an edge which extends another edge
	 */
	@Test
	public void getFirstEdgeTestClass2() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e1 = graph.createSubLink((DoubleSubNode) v0, (SuperNode) v1);
		assertEquals(e1, v0.getFirstEdgeOfClass(Link.class));
		assertEquals(e1, v0.getFirstEdgeOfClass(SubLink.class));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class));
		assertEquals(e1.getReversedEdge(), v1
				.getFirstEdgeOfClass(SubLink.class));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class));
	}

	/**
	 * Tests if a node has two Edges.
	 */
	@Test
	public void getFirstEdgeTestClass3() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		Edge e2 = graph.createLinkBack((SuperNode) v1, (AbstractSuperNode) v0);
		assertEquals(e1, v0.getFirstEdgeOfClass(Link.class));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class));
		assertEquals(e2.getReversedEdge(), v0
				.getFirstEdgeOfClass(LinkBack.class));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class));
		assertEquals(e2, v1.getFirstEdgeOfClass(LinkBack.class));
	}

	/**
	 * Tests if alpha and omega of an Edge is the same Vertex.
	 */
	@Test
	public void getFirstEdgeTestClass4() {
		Vertex v0 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink((AbstractSuperNode) v0, (SuperNode) v0);
		assertEquals(e1, v0.getFirstEdgeOfClass(Link.class));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class));
	}

	/**
	 * Random tests
	 */
	@Test
	public void getFirstEdgeTestClass5() {
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			Vertex[] vertices = new Vertex[] { graph.createSubNode(),
					graph.createDoubleSubNode(), graph.createSuperNode() };
			Edge[] firstLink = new Edge[3];
			Edge[] firstLinkBack = new Edge[3];
			Edge[] firstSubLink = new Edge[3];
			for (int j = 0; j < 5; j++) {
				int edgetype = rand.nextInt(3);
				int start = rand.nextInt(2);
				int end = rand.nextInt(2) + 1;
				switch (edgetype) {
				case 0:
					Edge e0 = graph.createLink(
							(AbstractSuperNode) vertices[start],
							(SuperNode) vertices[end]);
					if (firstLink[start] == null) {
						firstLink[start] = e0;
					}
					if (firstLink[end] == null) {
						firstLink[end] = e0.getReversedEdge();
					}
					break;
				case 1:
					Edge e1 = graph.createLinkBack((SuperNode) vertices[end],
							(AbstractSuperNode) vertices[start]);
					if (firstLinkBack[end] == null) {
						firstLinkBack[end] = e1;
					}
					if (firstLinkBack[start] == null) {
						firstLinkBack[start] = e1.getReversedEdge();
					}
					break;
				case 2:
					Edge e2 = graph.createSubLink((DoubleSubNode) vertices[1],
							(SuperNode) vertices[end]);
					if (firstLink[1] == null) {
						firstLink[1] = e2;
					}
					if (firstSubLink[1] == null) {
						firstSubLink[1] = e2;
					}
					if (firstLink[end] == null) {
						firstLink[end] = e2.getReversedEdge();
					}
					if (firstSubLink[end] == null) {
						firstSubLink[end] = e2.getReversedEdge();
					}
					break;
				}
			}
			assertEquals(firstLink[0], vertices[0]
					.getFirstEdgeOfClass(Link.class));
			assertEquals(firstLink[1], vertices[1]
					.getFirstEdgeOfClass(Link.class));
			assertEquals(firstLink[2], vertices[2]
					.getFirstEdgeOfClass(Link.class));
			assertEquals(firstLinkBack[0], vertices[0]
					.getFirstEdgeOfClass(LinkBack.class));
			assertEquals(firstLinkBack[1], vertices[1]
					.getFirstEdgeOfClass(LinkBack.class));
			assertEquals(firstLinkBack[2], vertices[2]
					.getFirstEdgeOfClass(LinkBack.class));
			assertEquals(firstSubLink[0], vertices[0]
					.getFirstEdgeOfClass(SubLink.class));
			assertEquals(firstSubLink[1], vertices[1]
					.getFirstEdgeOfClass(SubLink.class));
			assertEquals(firstSubLink[2], vertices[2]
					.getFirstEdgeOfClass(SubLink.class));
		}
	}

	// tests of the method Edge getFirstEdgeOfClass(EdgeClass anEdgeClass,
	// EdgeDirection orientation);

	/**
	 * Tests if a node has no Edges
	 */
	@Test
	public void getFirstEdgeTestEdgeClassEdgeDirection0() {
		EdgeClass[] eclasses = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();

		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT));

		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.OUT));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.OUT));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.OUT));

		assertEquals(null, v0
				.getFirstEdgeOfClass(eclasses[0], EdgeDirection.IN));
		assertEquals(null, v0
				.getFirstEdgeOfClass(eclasses[1], EdgeDirection.IN));
		assertEquals(null, v0
				.getFirstEdgeOfClass(eclasses[2], EdgeDirection.IN));
	}

	/**
	 * Tests if a node has only one Edge
	 */
	@Test
	public void getFirstEdgeTestEdgeClassEdgeDirection1() {
		EdgeClass[] eclasses = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);

		assertEquals(e, v0
				.getFirstEdgeOfClass(eclasses[0], EdgeDirection.INOUT));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT));
		assertEquals(e.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT));

		assertEquals(e, v0.getFirstEdgeOfClass(eclasses[0], EdgeDirection.OUT));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.OUT));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.OUT));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.OUT));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.OUT));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.OUT));

		assertEquals(null, v0
				.getFirstEdgeOfClass(eclasses[0], EdgeDirection.IN));
		assertEquals(null, v0
				.getFirstEdgeOfClass(eclasses[1], EdgeDirection.IN));
		assertEquals(null, v0
				.getFirstEdgeOfClass(eclasses[2], EdgeDirection.IN));
		assertEquals(e.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.IN));
		assertEquals(null, v1
				.getFirstEdgeOfClass(eclasses[1], EdgeDirection.IN));
		assertEquals(null, v1
				.getFirstEdgeOfClass(eclasses[2], EdgeDirection.IN));
	}

	/**
	 * Tests if a node has an edge which extends another edge
	 */
	@Test
	public void getFirstEdgeTestEdgeClassEdgeDirection2() {
		EdgeClass[] eclasses = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e1 = graph.createSubLink((DoubleSubNode) v0, (SuperNode) v1);

		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT));
		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT));

		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0], EdgeDirection.OUT));
		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[1], EdgeDirection.OUT));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.OUT));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.OUT));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.OUT));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.OUT));

		assertEquals(null, v0
				.getFirstEdgeOfClass(eclasses[0], EdgeDirection.IN));
		assertEquals(null, v0
				.getFirstEdgeOfClass(eclasses[1], EdgeDirection.IN));
		assertEquals(null, v0
				.getFirstEdgeOfClass(eclasses[2], EdgeDirection.IN));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.IN));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.IN));
		assertEquals(null, v1
				.getFirstEdgeOfClass(eclasses[2], EdgeDirection.IN));
	}

	/**
	 * Tests if a node has two Edges.
	 */
	@Test
	public void getFirstEdgeTestEdgeClassEdgeDirection3() {
		EdgeClass[] eclasses = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		Edge e2 = graph.createLinkBack((SuperNode) v1, (AbstractSuperNode) v0);

		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT));
		assertEquals(e2.getReversedEdge(), v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT));
		assertEquals(e2, v1.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT));

		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0], EdgeDirection.OUT));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.OUT));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.OUT));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.OUT));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.OUT));
		assertEquals(e2, v1.getFirstEdgeOfClass(eclasses[2], EdgeDirection.OUT));

		assertEquals(null, v0
				.getFirstEdgeOfClass(eclasses[0], EdgeDirection.IN));
		assertEquals(null, v0
				.getFirstEdgeOfClass(eclasses[1], EdgeDirection.IN));
		assertEquals(e2.getReversedEdge(), v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.IN));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.IN));
		assertEquals(null, v1
				.getFirstEdgeOfClass(eclasses[1], EdgeDirection.IN));
		assertEquals(null, v1
				.getFirstEdgeOfClass(eclasses[2], EdgeDirection.IN));
	}

	/**
	 * Tests if alpha and omega of an Edge is the same Vertex.
	 */
	@Test
	public void getFirstEdgeTestEdgeClassEdgeDirection4() {
		EdgeClass[] eclasses = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink((AbstractSuperNode) v0, (SuperNode) v0);

		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT));

		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0], EdgeDirection.OUT));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.OUT));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.OUT));

		assertEquals(e1.getReversedEdge(), v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.IN));
		assertEquals(null, v0
				.getFirstEdgeOfClass(eclasses[1], EdgeDirection.IN));
		assertEquals(null, v0
				.getFirstEdgeOfClass(eclasses[2], EdgeDirection.IN));
	}

	/**
	 * Random tests
	 */
	@Test
	public void getFirstEdgeTestEdgeClassEdgeDirection5() {
		EdgeClass[] eclasses = getEdgeClasses();
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			Vertex[] vertices = new Vertex[] { graph.createSubNode(),
					graph.createDoubleSubNode(), graph.createSuperNode() };
			Edge[] firstLinkInOut = new Edge[3];
			Edge[] firstLinkBackInOut = new Edge[3];
			Edge[] firstSubLinkInOut = new Edge[3];
			Edge[] firstLinkOut = new Edge[3];
			Edge[] firstLinkBackOut = new Edge[3];
			Edge[] firstSubLinkOut = new Edge[3];
			Edge[] firstLinkIn = new Edge[3];
			Edge[] firstLinkBackIn = new Edge[3];
			Edge[] firstSubLinkIn = new Edge[3];
			for (int j = 0; j < 5; j++) {
				int edgetype = rand.nextInt(3);
				int start = rand.nextInt(2);
				int end = rand.nextInt(2) + 1;
				switch (edgetype) {
				case 0:
					Edge e0 = graph.createLink(
							(AbstractSuperNode) vertices[start],
							(SuperNode) vertices[end]);
					if (firstLinkInOut[start] == null) {
						firstLinkInOut[start] = e0;
					}
					if (firstLinkOut[start] == null) {
						firstLinkOut[start] = e0;
					}
					if (firstLinkInOut[end] == null) {
						firstLinkInOut[end] = e0.getReversedEdge();
					}
					if (firstLinkIn[end] == null) {
						firstLinkIn[end] = e0.getReversedEdge();
					}
					break;
				case 1:
					Edge e1 = graph.createLinkBack((SuperNode) vertices[end],
							(AbstractSuperNode) vertices[start]);
					if (firstLinkBackInOut[end] == null) {
						firstLinkBackInOut[end] = e1;
					}
					if (firstLinkBackOut[end] == null) {
						firstLinkBackOut[end] = e1;
					}
					if (firstLinkBackInOut[start] == null) {
						firstLinkBackInOut[start] = e1.getReversedEdge();
					}
					if (firstLinkBackIn[start] == null) {
						firstLinkBackIn[start] = e1.getReversedEdge();
					}
					break;
				case 2:
					Edge e2 = graph.createSubLink((DoubleSubNode) vertices[1],
							(SuperNode) vertices[end]);
					if (firstLinkInOut[1] == null) {
						firstLinkInOut[1] = e2;
					}
					if (firstLinkOut[1] == null) {
						firstLinkOut[1] = e2;
					}
					if (firstSubLinkInOut[1] == null) {
						firstSubLinkInOut[1] = e2;
					}
					if (firstSubLinkOut[1] == null) {
						firstSubLinkOut[1] = e2;
					}
					if (firstLinkInOut[end] == null) {
						firstLinkInOut[end] = e2.getReversedEdge();
					}
					if (firstLinkIn[end] == null) {
						firstLinkIn[end] = e2.getReversedEdge();
					}
					if (firstSubLinkInOut[end] == null) {
						firstSubLinkInOut[end] = e2.getReversedEdge();
					}
					if (firstSubLinkIn[end] == null) {
						firstSubLinkIn[end] = e2.getReversedEdge();
					}
					break;
				}
			}
			assertEquals(firstLinkInOut[0], vertices[0].getFirstEdgeOfClass(
					eclasses[0], EdgeDirection.INOUT));
			assertEquals(firstLinkInOut[1], vertices[1].getFirstEdgeOfClass(
					eclasses[0], EdgeDirection.INOUT));
			assertEquals(firstLinkInOut[2], vertices[2].getFirstEdgeOfClass(
					eclasses[0], EdgeDirection.INOUT));
			assertEquals(firstLinkBackInOut[0], vertices[0]
					.getFirstEdgeOfClass(eclasses[2], EdgeDirection.INOUT));
			assertEquals(firstLinkBackInOut[1], vertices[1]
					.getFirstEdgeOfClass(eclasses[2], EdgeDirection.INOUT));
			assertEquals(firstLinkBackInOut[2], vertices[2]
					.getFirstEdgeOfClass(eclasses[2], EdgeDirection.INOUT));
			assertEquals(firstSubLinkInOut[0], vertices[0].getFirstEdgeOfClass(
					eclasses[1], EdgeDirection.INOUT));
			assertEquals(firstSubLinkInOut[1], vertices[1].getFirstEdgeOfClass(
					eclasses[1], EdgeDirection.INOUT));
			assertEquals(firstSubLinkInOut[2], vertices[2].getFirstEdgeOfClass(
					eclasses[1], EdgeDirection.INOUT));

			assertEquals(firstLinkOut[0], vertices[0].getFirstEdgeOfClass(
					eclasses[0], EdgeDirection.OUT));
			assertEquals(firstLinkOut[1], vertices[1].getFirstEdgeOfClass(
					eclasses[0], EdgeDirection.OUT));
			assertEquals(firstLinkOut[2], vertices[2].getFirstEdgeOfClass(
					eclasses[0], EdgeDirection.OUT));
			assertEquals(firstLinkBackOut[0], vertices[0].getFirstEdgeOfClass(
					eclasses[2], EdgeDirection.OUT));
			assertEquals(firstLinkBackOut[1], vertices[1].getFirstEdgeOfClass(
					eclasses[2], EdgeDirection.OUT));
			assertEquals(firstLinkBackOut[2], vertices[2].getFirstEdgeOfClass(
					eclasses[2], EdgeDirection.OUT));
			assertEquals(firstSubLinkOut[0], vertices[0].getFirstEdgeOfClass(
					eclasses[1], EdgeDirection.OUT));
			assertEquals(firstSubLinkOut[1], vertices[1].getFirstEdgeOfClass(
					eclasses[1], EdgeDirection.OUT));
			assertEquals(firstSubLinkOut[2], vertices[2].getFirstEdgeOfClass(
					eclasses[1], EdgeDirection.OUT));

			assertEquals(firstLinkIn[0], vertices[0].getFirstEdgeOfClass(
					eclasses[0], EdgeDirection.IN));
			assertEquals(firstLinkIn[1], vertices[1].getFirstEdgeOfClass(
					eclasses[0], EdgeDirection.IN));
			assertEquals(firstLinkIn[2], vertices[2].getFirstEdgeOfClass(
					eclasses[0], EdgeDirection.IN));
			assertEquals(firstLinkBackIn[0], vertices[0].getFirstEdgeOfClass(
					eclasses[2], EdgeDirection.IN));
			assertEquals(firstLinkBackIn[1], vertices[1].getFirstEdgeOfClass(
					eclasses[2], EdgeDirection.IN));
			assertEquals(firstLinkBackIn[2], vertices[2].getFirstEdgeOfClass(
					eclasses[2], EdgeDirection.IN));
			assertEquals(firstSubLinkIn[0], vertices[0].getFirstEdgeOfClass(
					eclasses[1], EdgeDirection.IN));
			assertEquals(firstSubLinkIn[1], vertices[1].getFirstEdgeOfClass(
					eclasses[1], EdgeDirection.IN));
			assertEquals(firstSubLinkIn[2], vertices[2].getFirstEdgeOfClass(
					eclasses[1], EdgeDirection.IN));
		}
	}

	// tests of the method Edge getFirstEdgeOfClass(Class<? extends Edge>
	// anEdgeClass, EdgeDirection orientation);

	/**
	 * Tests if a node has no Edges
	 */
	@Test
	public void getFirstEdgeTestClassEdgeDirection0() {
		Vertex v0 = graph.createDoubleSubNode();

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.INOUT));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT));

		assertEquals(null, v0
				.getFirstEdgeOfClass(Link.class, EdgeDirection.OUT));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.IN));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN));
	}

	/**
	 * Tests if a node has only one Edge
	 */
	@Test
	public void getFirstEdgeTestClassEdgeDirection1() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);

		assertEquals(e, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.INOUT));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT));
		assertEquals(e.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.INOUT));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT));

		assertEquals(e, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.OUT));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT));
		assertEquals(null, v1
				.getFirstEdgeOfClass(Link.class, EdgeDirection.OUT));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.IN));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN));
		assertEquals(e.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.IN));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN));
	}

	/**
	 * Tests if a node has an edge which extends another edge
	 */
	@Test
	public void getFirstEdgeTestClassEdgeDirection2() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e1 = graph.createSubLink((DoubleSubNode) v0, (SuperNode) v1);

		assertEquals(e1, v0
				.getFirstEdgeOfClass(Link.class, EdgeDirection.INOUT));
		assertEquals(e1, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.INOUT));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(
				SubLink.class, EdgeDirection.INOUT));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT));

		assertEquals(e1, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.OUT));
		assertEquals(e1, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT));
		assertEquals(null, v1
				.getFirstEdgeOfClass(Link.class, EdgeDirection.OUT));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.IN));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.IN));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(
				SubLink.class, EdgeDirection.IN));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN));
	}

	/**
	 * Tests if a node has two Edges.
	 */
	@Test
	public void getFirstEdgeTestClassEdgeDirection3() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		Edge e2 = graph.createLinkBack((SuperNode) v1, (AbstractSuperNode) v0);

		assertEquals(e1, v0
				.getFirstEdgeOfClass(Link.class, EdgeDirection.INOUT));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT));
		assertEquals(e2.getReversedEdge(), v0.getFirstEdgeOfClass(
				LinkBack.class, EdgeDirection.INOUT));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.INOUT));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT));
		assertEquals(e2, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT));

		assertEquals(e1, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.OUT));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT));
		assertEquals(null, v1
				.getFirstEdgeOfClass(Link.class, EdgeDirection.OUT));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT));
		assertEquals(e2, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.IN));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN));
		assertEquals(e2.getReversedEdge(), v0.getFirstEdgeOfClass(
				LinkBack.class, EdgeDirection.IN));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.IN));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN));
	}

	/**
	 * Tests if alpha and omega of an Edge is the same Vertex.
	 */
	@Test
	public void getFirstEdgeTestClassEdgeDirection4() {
		Vertex v0 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink((AbstractSuperNode) v0, (SuperNode) v0);

		assertEquals(e1, v0
				.getFirstEdgeOfClass(Link.class, EdgeDirection.INOUT));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT));

		assertEquals(e1, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.OUT));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT));

		assertEquals(e1.getReversedEdge(), v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.IN));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN));
	}

	/**
	 * Random tests
	 */
	@Test
	public void getFirstEdgeTestClassEdgeDirection5() {
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			Vertex[] vertices = new Vertex[] { graph.createSubNode(),
					graph.createDoubleSubNode(), graph.createSuperNode() };
			Edge[] firstLinkInOut = new Edge[3];
			Edge[] firstLinkBackInOut = new Edge[3];
			Edge[] firstSubLinkInOut = new Edge[3];
			Edge[] firstLinkOut = new Edge[3];
			Edge[] firstLinkBackOut = new Edge[3];
			Edge[] firstSubLinkOut = new Edge[3];
			Edge[] firstLinkIn = new Edge[3];
			Edge[] firstLinkBackIn = new Edge[3];
			Edge[] firstSubLinkIn = new Edge[3];
			for (int j = 0; j < 5; j++) {
				int edgetype = rand.nextInt(3);
				int start = rand.nextInt(2);
				int end = rand.nextInt(2) + 1;
				switch (edgetype) {
				case 0:
					Edge e0 = graph.createLink(
							(AbstractSuperNode) vertices[start],
							(SuperNode) vertices[end]);
					if (firstLinkInOut[start] == null) {
						firstLinkInOut[start] = e0;
					}
					if (firstLinkOut[start] == null) {
						firstLinkOut[start] = e0;
					}
					if (firstLinkInOut[end] == null) {
						firstLinkInOut[end] = e0.getReversedEdge();
					}
					if (firstLinkIn[end] == null) {
						firstLinkIn[end] = e0.getReversedEdge();
					}
					break;
				case 1:
					Edge e1 = graph.createLinkBack((SuperNode) vertices[end],
							(AbstractSuperNode) vertices[start]);
					if (firstLinkBackInOut[end] == null) {
						firstLinkBackInOut[end] = e1;
					}
					if (firstLinkBackOut[end] == null) {
						firstLinkBackOut[end] = e1;
					}
					if (firstLinkBackInOut[start] == null) {
						firstLinkBackInOut[start] = e1.getReversedEdge();
					}
					if (firstLinkBackIn[start] == null) {
						firstLinkBackIn[start] = e1.getReversedEdge();
					}
					break;
				case 2:
					Edge e2 = graph.createSubLink((DoubleSubNode) vertices[1],
							(SuperNode) vertices[end]);
					if (firstLinkInOut[1] == null) {
						firstLinkInOut[1] = e2;
					}
					if (firstLinkOut[1] == null) {
						firstLinkOut[1] = e2;
					}
					if (firstSubLinkInOut[1] == null) {
						firstSubLinkInOut[1] = e2;
					}
					if (firstSubLinkOut[1] == null) {
						firstSubLinkOut[1] = e2;
					}
					if (firstLinkInOut[end] == null) {
						firstLinkInOut[end] = e2.getReversedEdge();
					}
					if (firstLinkIn[end] == null) {
						firstLinkIn[end] = e2.getReversedEdge();
					}
					if (firstSubLinkInOut[end] == null) {
						firstSubLinkInOut[end] = e2.getReversedEdge();
					}
					if (firstSubLinkIn[end] == null) {
						firstSubLinkIn[end] = e2.getReversedEdge();
					}
					break;
				}
			}
			assertEquals(firstLinkInOut[0], vertices[0].getFirstEdgeOfClass(
					Link.class, EdgeDirection.INOUT));
			assertEquals(firstLinkInOut[1], vertices[1].getFirstEdgeOfClass(
					Link.class, EdgeDirection.INOUT));
			assertEquals(firstLinkInOut[2], vertices[2].getFirstEdgeOfClass(
					Link.class, EdgeDirection.INOUT));
			assertEquals(firstLinkBackInOut[0], vertices[0]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.INOUT));
			assertEquals(firstLinkBackInOut[1], vertices[1]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.INOUT));
			assertEquals(firstLinkBackInOut[2], vertices[2]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.INOUT));
			assertEquals(firstSubLinkInOut[0], vertices[0].getFirstEdgeOfClass(
					SubLink.class, EdgeDirection.INOUT));
			assertEquals(firstSubLinkInOut[1], vertices[1].getFirstEdgeOfClass(
					SubLink.class, EdgeDirection.INOUT));
			assertEquals(firstSubLinkInOut[2], vertices[2].getFirstEdgeOfClass(
					SubLink.class, EdgeDirection.INOUT));

			assertEquals(firstLinkOut[0], vertices[0].getFirstEdgeOfClass(
					Link.class, EdgeDirection.OUT));
			assertEquals(firstLinkOut[1], vertices[1].getFirstEdgeOfClass(
					Link.class, EdgeDirection.OUT));
			assertEquals(firstLinkOut[2], vertices[2].getFirstEdgeOfClass(
					Link.class, EdgeDirection.OUT));
			assertEquals(firstLinkBackOut[0], vertices[0].getFirstEdgeOfClass(
					LinkBack.class, EdgeDirection.OUT));
			assertEquals(firstLinkBackOut[1], vertices[1].getFirstEdgeOfClass(
					LinkBack.class, EdgeDirection.OUT));
			assertEquals(firstLinkBackOut[2], vertices[2].getFirstEdgeOfClass(
					LinkBack.class, EdgeDirection.OUT));
			assertEquals(firstSubLinkOut[0], vertices[0].getFirstEdgeOfClass(
					SubLink.class, EdgeDirection.OUT));
			assertEquals(firstSubLinkOut[1], vertices[1].getFirstEdgeOfClass(
					SubLink.class, EdgeDirection.OUT));
			assertEquals(firstSubLinkOut[2], vertices[2].getFirstEdgeOfClass(
					SubLink.class, EdgeDirection.OUT));

			assertEquals(firstLinkIn[0], vertices[0].getFirstEdgeOfClass(
					Link.class, EdgeDirection.IN));
			assertEquals(firstLinkIn[1], vertices[1].getFirstEdgeOfClass(
					Link.class, EdgeDirection.IN));
			assertEquals(firstLinkIn[2], vertices[2].getFirstEdgeOfClass(
					Link.class, EdgeDirection.IN));
			assertEquals(firstLinkBackIn[0], vertices[0].getFirstEdgeOfClass(
					LinkBack.class, EdgeDirection.IN));
			assertEquals(firstLinkBackIn[1], vertices[1].getFirstEdgeOfClass(
					LinkBack.class, EdgeDirection.IN));
			assertEquals(firstLinkBackIn[2], vertices[2].getFirstEdgeOfClass(
					LinkBack.class, EdgeDirection.IN));
			assertEquals(firstSubLinkIn[0], vertices[0].getFirstEdgeOfClass(
					SubLink.class, EdgeDirection.IN));
			assertEquals(firstSubLinkIn[1], vertices[1].getFirstEdgeOfClass(
					SubLink.class, EdgeDirection.IN));
			assertEquals(firstSubLinkIn[2], vertices[2].getFirstEdgeOfClass(
					SubLink.class, EdgeDirection.IN));
		}
	}

	// tests of the method Edge getFirstEdgeOfClass(EdgeClass anEdgeClass,
	// boolean noSubclasses);

	/**
	 * Tests if a node has no Edges
	 */
	@Test
	public void getFirstEdgeTestEdgeClassBoolean0() {
		EdgeClass[] eclasses = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();

		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[0], false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1], false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2], false));

		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[0], true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1], true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2], true));
	}

	/**
	 * Tests if a node has only one Edge
	 */
	@Test
	public void getFirstEdgeTestEdgeClassBoolean1() {
		EdgeClass[] eclasses = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);

		assertEquals(e, v0.getFirstEdgeOfClass(eclasses[0], false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1], false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2], false));
		assertEquals(e.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0],
				false));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1], false));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2], false));

		assertEquals(e, v0.getFirstEdgeOfClass(eclasses[0], true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1], true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2], true));
		assertEquals(e.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0],
				true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1], true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2], true));
	}

	/**
	 * Tests if a node has an edge which extends another edge
	 */
	@Test
	public void getFirstEdgeTestEdgeClassBoolean2() {
		EdgeClass[] eclasses = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e1 = graph.createSubLink((DoubleSubNode) v0, (SuperNode) v1);

		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0], false));
		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[1], false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2], false));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0],
				false));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[1],
				false));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2], false));

		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[0], true));
		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[1], true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2], true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[0], true));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[1],
				true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2], true));
	}

	/**
	 * Tests if a node has two Edges.
	 */
	@Test
	public void getFirstEdgeTestEdgeClassBoolean3() {
		EdgeClass[] eclasses = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		Edge e2 = graph.createLinkBack((SuperNode) v1, (AbstractSuperNode) v0);

		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0], false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1], false));
		assertEquals(e2.getReversedEdge(), v0.getFirstEdgeOfClass(eclasses[2],
				false));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0],
				false));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1], false));
		assertEquals(e2, v1.getFirstEdgeOfClass(eclasses[2], false));

		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0], true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1], true));
		assertEquals(e2.getReversedEdge(), v0.getFirstEdgeOfClass(eclasses[2],
				true));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0],
				true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1], true));
		assertEquals(e2, v1.getFirstEdgeOfClass(eclasses[2], true));
	}

	/**
	 * Tests if alpha and omega of an Edge is the same Vertex.
	 */
	@Test
	public void getFirstEdgeTestEdgeClassBoolean4() {
		EdgeClass[] eclasses = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink((AbstractSuperNode) v0, (SuperNode) v0);

		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0], false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1], false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2], false));

		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0], true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1], true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2], true));
	}

	/**
	 * Random tests
	 */
	@Test
	public void getFirstEdgeTestEdgeClassBoolean5() {
		EdgeClass[] eclasses = getEdgeClasses();
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			Vertex[] vertices = new Vertex[] { graph.createSubNode(),
					graph.createDoubleSubNode(), graph.createSuperNode() };
			Edge[] firstLinkFalse = new Edge[3];
			Edge[] firstLinkBackFalse = new Edge[3];
			Edge[] firstSubLinkFalse = new Edge[3];
			Edge[] firstLinkTrue = new Edge[3];
			Edge[] firstLinkBackTrue = new Edge[3];
			Edge[] firstSubLinkTrue = new Edge[3];
			for (int j = 0; j < 5; j++) {
				int edgetype = rand.nextInt(3);
				int start = rand.nextInt(2);
				int end = rand.nextInt(2) + 1;
				switch (edgetype) {
				case 0:
					Edge e0 = graph.createLink(
							(AbstractSuperNode) vertices[start],
							(SuperNode) vertices[end]);
					if (firstLinkFalse[start] == null) {
						firstLinkFalse[start] = e0;
					}
					if (firstLinkTrue[start] == null) {
						firstLinkTrue[start] = e0;
					}
					if (firstLinkFalse[end] == null) {
						firstLinkFalse[end] = e0.getReversedEdge();
					}
					if (firstLinkTrue[end] == null) {
						firstLinkTrue[end] = e0.getReversedEdge();
					}
					break;
				case 1:
					Edge e1 = graph.createLinkBack((SuperNode) vertices[end],
							(AbstractSuperNode) vertices[start]);
					if (firstLinkBackFalse[end] == null) {
						firstLinkBackFalse[end] = e1;
					}
					if (firstLinkBackTrue[end] == null) {
						firstLinkBackTrue[end] = e1;
					}
					if (firstLinkBackFalse[start] == null) {
						firstLinkBackFalse[start] = e1.getReversedEdge();
					}
					if (firstLinkBackTrue[start] == null) {
						firstLinkBackTrue[start] = e1.getReversedEdge();
					}
					break;
				case 2:
					Edge e2 = graph.createSubLink((DoubleSubNode) vertices[1],
							(SuperNode) vertices[end]);
					if (firstLinkFalse[1] == null) {
						firstLinkFalse[1] = e2;
					}
					if (firstSubLinkFalse[1] == null) {
						firstSubLinkFalse[1] = e2;
					}
					if (firstSubLinkTrue[1] == null) {
						firstSubLinkTrue[1] = e2;
					}
					if (firstLinkFalse[end] == null) {
						firstLinkFalse[end] = e2.getReversedEdge();
					}
					if (firstSubLinkFalse[end] == null) {
						firstSubLinkFalse[end] = e2.getReversedEdge();
					}
					if (firstSubLinkTrue[end] == null) {
						firstSubLinkTrue[end] = e2.getReversedEdge();
					}
					break;
				}
			}
			assertEquals(firstLinkFalse[0], vertices[0].getFirstEdgeOfClass(
					eclasses[0], false));
			assertEquals(firstLinkFalse[1], vertices[1].getFirstEdgeOfClass(
					eclasses[0], false));
			assertEquals(firstLinkFalse[2], vertices[2].getFirstEdgeOfClass(
					eclasses[0], false));
			assertEquals(firstLinkBackFalse[0], vertices[0]
					.getFirstEdgeOfClass(eclasses[2], false));
			assertEquals(firstLinkBackFalse[1], vertices[1]
					.getFirstEdgeOfClass(eclasses[2], false));
			assertEquals(firstLinkBackFalse[2], vertices[2]
					.getFirstEdgeOfClass(eclasses[2], false));
			assertEquals(firstSubLinkFalse[0], vertices[0].getFirstEdgeOfClass(
					eclasses[1], false));
			assertEquals(firstSubLinkFalse[1], vertices[1].getFirstEdgeOfClass(
					eclasses[1], false));
			assertEquals(firstSubLinkFalse[2], vertices[2].getFirstEdgeOfClass(
					eclasses[1], false));

			assertEquals(firstLinkTrue[0], vertices[0].getFirstEdgeOfClass(
					eclasses[0], true));
			assertEquals(firstLinkTrue[1], vertices[1].getFirstEdgeOfClass(
					eclasses[0], true));
			assertEquals(firstLinkTrue[2], vertices[2].getFirstEdgeOfClass(
					eclasses[0], true));
			assertEquals(firstLinkBackTrue[0], vertices[0].getFirstEdgeOfClass(
					eclasses[2], true));
			assertEquals(firstLinkBackTrue[1], vertices[1].getFirstEdgeOfClass(
					eclasses[2], true));
			assertEquals(firstLinkBackTrue[2], vertices[2].getFirstEdgeOfClass(
					eclasses[2], true));
			assertEquals(firstSubLinkTrue[0], vertices[0].getFirstEdgeOfClass(
					eclasses[1], true));
			assertEquals(firstSubLinkTrue[1], vertices[1].getFirstEdgeOfClass(
					eclasses[1], true));
			assertEquals(firstSubLinkTrue[2], vertices[2].getFirstEdgeOfClass(
					eclasses[1], true));
		}
	}

	// tests of the method Edge getFirstEdgeOfClass(Class<? extends Edge>
	// anEdgeClass, boolean noSubclasses);

	/**
	 * Tests if a node has no Edges
	 */
	@Test
	public void getFirstEdgeTestClassBoolean0() {
		Vertex v0 = graph.createDoubleSubNode();

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class, false));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class, false));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class, false));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class, true));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class, true));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class, true));
	}

	/**
	 * Tests if a node has only one Edge
	 */
	@Test
	public void getFirstEdgeTestClassBoolean1() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);

		assertEquals(e, v0.getFirstEdgeOfClass(Link.class, false));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class, false));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class, false));
		assertEquals(e.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class,
				false));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class, false));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class, false));

		assertEquals(e, v0.getFirstEdgeOfClass(Link.class, true));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class, true));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class, true));
		assertEquals(e.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class,
				true));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class, true));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class, true));
	}

	/**
	 * Tests if a node has an edge which extends another edge
	 */
	@Test
	public void getFirstEdgeTestClassBoolean2() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e1 = graph.createSubLink((DoubleSubNode) v0, (SuperNode) v1);

		assertEquals(e1, v0.getFirstEdgeOfClass(Link.class, false));
		assertEquals(e1, v0.getFirstEdgeOfClass(SubLink.class, false));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class, false));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class,
				false));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(
				SubLink.class, false));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class, false));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class, true));
		assertEquals(e1, v0.getFirstEdgeOfClass(SubLink.class, true));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class, true));
		assertEquals(null, v1.getFirstEdgeOfClass(Link.class, true));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(
				SubLink.class, true));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class, true));
	}

	/**
	 * Tests if a node has two Edges.
	 */
	@Test
	public void getFirstEdgeTestClassBoolean3() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		Edge e2 = graph.createLinkBack((SuperNode) v1, (AbstractSuperNode) v0);

		assertEquals(e1, v0.getFirstEdgeOfClass(Link.class, false));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class, false));
		assertEquals(e2.getReversedEdge(), v0.getFirstEdgeOfClass(
				LinkBack.class, false));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class,
				false));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class, false));
		assertEquals(e2, v1.getFirstEdgeOfClass(LinkBack.class, false));

		assertEquals(e1, v0.getFirstEdgeOfClass(Link.class, true));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class, true));
		assertEquals(e2.getReversedEdge(), v0.getFirstEdgeOfClass(
				LinkBack.class, true));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class,
				true));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class, true));
		assertEquals(e2, v1.getFirstEdgeOfClass(LinkBack.class, true));
	}

	/**
	 * Tests if alpha and omega of an Edge is the same Vertex.
	 */
	@Test
	public void getFirstEdgeTestClassBoolean4() {
		Vertex v0 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink((AbstractSuperNode) v0, (SuperNode) v0);

		assertEquals(e1, v0.getFirstEdgeOfClass(Link.class, false));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class, false));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class, false));

		assertEquals(e1, v0.getFirstEdgeOfClass(Link.class, true));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class, true));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class, true));
	}

	/**
	 * Random tests
	 */
	@Test
	public void getFirstEdgeTestClassBoolean5() {
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			Vertex[] vertices = new Vertex[] { graph.createSubNode(),
					graph.createDoubleSubNode(), graph.createSuperNode() };
			Edge[] firstLinkFalse = new Edge[3];
			Edge[] firstLinkBackFalse = new Edge[3];
			Edge[] firstSubLinkFalse = new Edge[3];
			Edge[] firstLinkTrue = new Edge[3];
			Edge[] firstLinkBackTrue = new Edge[3];
			Edge[] firstSubLinkTrue = new Edge[3];
			for (int j = 0; j < 5; j++) {
				int edgetype = rand.nextInt(3);
				int start = rand.nextInt(2);
				int end = rand.nextInt(2) + 1;
				switch (edgetype) {
				case 0:
					Edge e0 = graph.createLink(
							(AbstractSuperNode) vertices[start],
							(SuperNode) vertices[end]);
					if (firstLinkFalse[start] == null) {
						firstLinkFalse[start] = e0;
					}
					if (firstLinkTrue[start] == null) {
						firstLinkTrue[start] = e0;
					}
					if (firstLinkFalse[end] == null) {
						firstLinkFalse[end] = e0.getReversedEdge();
					}
					if (firstLinkTrue[end] == null) {
						firstLinkTrue[end] = e0.getReversedEdge();
					}
					break;
				case 1:
					Edge e1 = graph.createLinkBack((SuperNode) vertices[end],
							(AbstractSuperNode) vertices[start]);
					if (firstLinkBackFalse[end] == null) {
						firstLinkBackFalse[end] = e1;
					}
					if (firstLinkBackTrue[end] == null) {
						firstLinkBackTrue[end] = e1;
					}
					if (firstLinkBackFalse[start] == null) {
						firstLinkBackFalse[start] = e1.getReversedEdge();
					}
					if (firstLinkBackTrue[start] == null) {
						firstLinkBackTrue[start] = e1.getReversedEdge();
					}
					break;
				case 2:
					Edge e2 = graph.createSubLink((DoubleSubNode) vertices[1],
							(SuperNode) vertices[end]);
					if (firstLinkFalse[1] == null) {
						firstLinkFalse[1] = e2;
					}
					if (firstSubLinkFalse[1] == null) {
						firstSubLinkFalse[1] = e2;
					}
					if (firstSubLinkTrue[1] == null) {
						firstSubLinkTrue[1] = e2;
					}
					if (firstLinkFalse[end] == null) {
						firstLinkFalse[end] = e2.getReversedEdge();
					}
					if (firstSubLinkFalse[end] == null) {
						firstSubLinkFalse[end] = e2.getReversedEdge();
					}
					if (firstSubLinkTrue[end] == null) {
						firstSubLinkTrue[end] = e2.getReversedEdge();
					}
					break;
				}
			}
			assertEquals(firstLinkFalse[0], vertices[0].getFirstEdgeOfClass(
					Link.class, false));
			assertEquals(firstLinkFalse[1], vertices[1].getFirstEdgeOfClass(
					Link.class, false));
			assertEquals(firstLinkFalse[2], vertices[2].getFirstEdgeOfClass(
					Link.class, false));
			assertEquals(firstLinkBackFalse[0], vertices[0]
					.getFirstEdgeOfClass(LinkBack.class, false));
			assertEquals(firstLinkBackFalse[1], vertices[1]
					.getFirstEdgeOfClass(LinkBack.class, false));
			assertEquals(firstLinkBackFalse[2], vertices[2]
					.getFirstEdgeOfClass(LinkBack.class, false));
			assertEquals(firstSubLinkFalse[0], vertices[0].getFirstEdgeOfClass(
					SubLink.class, false));
			assertEquals(firstSubLinkFalse[1], vertices[1].getFirstEdgeOfClass(
					SubLink.class, false));
			assertEquals(firstSubLinkFalse[2], vertices[2].getFirstEdgeOfClass(
					SubLink.class, false));

			assertEquals(firstLinkTrue[0], vertices[0].getFirstEdgeOfClass(
					Link.class, true));
			assertEquals(firstLinkTrue[1], vertices[1].getFirstEdgeOfClass(
					Link.class, true));
			assertEquals(firstLinkTrue[2], vertices[2].getFirstEdgeOfClass(
					Link.class, true));
			assertEquals(firstLinkBackTrue[0], vertices[0].getFirstEdgeOfClass(
					LinkBack.class, true));
			assertEquals(firstLinkBackTrue[1], vertices[1].getFirstEdgeOfClass(
					LinkBack.class, true));
			assertEquals(firstLinkBackTrue[2], vertices[2].getFirstEdgeOfClass(
					LinkBack.class, true));
			assertEquals(firstSubLinkTrue[0], vertices[0].getFirstEdgeOfClass(
					SubLink.class, true));
			assertEquals(firstSubLinkTrue[1], vertices[1].getFirstEdgeOfClass(
					SubLink.class, true));
			assertEquals(firstSubLinkTrue[2], vertices[2].getFirstEdgeOfClass(
					SubLink.class, true));
		}
	}

	// tests of the method Edge getFirstEdgeOfClass(EdgeClass anEdgeClass,
	// EdgeDirection orientation, boolean noSubclasses);

	/**
	 * Tests if a node has no Edges
	 */
	@Test
	public void getFirstEdgeTestEdgeClassEdgeDirectionBoolean0() {
		EdgeClass[] eclasses = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();

		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT, false));

		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.OUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.OUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.OUT, false));

		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.IN, false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.IN, false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.IN, false));

		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT, true));

		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.OUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.OUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.OUT, true));

		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.IN, true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.IN, true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.IN, true));
	}

	/**
	 * Tests if a node has only one Edge
	 */
	@Test
	public void getFirstEdgeTestEdgeClassEdgeDirectionBoolean1() {
		EdgeClass[] eclasses = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);

		assertEquals(e, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT, false));
		assertEquals(e.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT, false));

		assertEquals(e, v0.getFirstEdgeOfClass(eclasses[0], EdgeDirection.OUT,
				false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.OUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.OUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.OUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.OUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.OUT, false));

		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.IN, false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.IN, false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.IN, false));
		assertEquals(e.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.IN, false));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.IN, false));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.IN, false));

		assertEquals(e, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT, true));
		assertEquals(e.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT, true));

		assertEquals(e, v0.getFirstEdgeOfClass(eclasses[0], EdgeDirection.OUT,
				true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.OUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.OUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.OUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.OUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.OUT, true));

		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.IN, true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.IN, true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.IN, true));
		assertEquals(e.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.IN, true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.IN, true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.IN, true));
	}

	/**
	 * Tests if a node has an edge which extends another edge
	 */
	@Test
	public void getFirstEdgeTestEdgeClassEdgeDirectionBoolean2() {
		EdgeClass[] eclasses = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e1 = graph.createSubLink((DoubleSubNode) v0, (SuperNode) v1);

		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT, false));
		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT, false));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT, false));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT, false));

		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0], EdgeDirection.OUT,
				false));
		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[1], EdgeDirection.OUT,
				false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.OUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.OUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.OUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.OUT, false));

		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.IN, false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.IN, false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.IN, false));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.IN, false));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.IN, false));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.IN, false));

		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT, true));
		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT, true));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT, true));

		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.OUT, true));
		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[1], EdgeDirection.OUT,
				true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.OUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.OUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.OUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.OUT, true));

		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.IN, true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.IN, true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.IN, true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.IN, true));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.IN, true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.IN, true));
	}

	/**
	 * Tests if a node has two Edges.
	 */
	@Test
	public void getFirstEdgeTestEdgeClassEdgeDirectionBoolean3() {
		EdgeClass[] eclasses = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		Edge e2 = graph.createLinkBack((SuperNode) v1, (AbstractSuperNode) v0);

		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT, false));
		assertEquals(e2.getReversedEdge(), v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT, false));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT, false));
		assertEquals(e2, v1.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT, false));

		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0], EdgeDirection.OUT,
				false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.OUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.OUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.OUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.OUT, false));
		assertEquals(e2, v1.getFirstEdgeOfClass(eclasses[2], EdgeDirection.OUT,
				false));

		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.IN, false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.IN, false));
		assertEquals(e2.getReversedEdge(), v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.IN, false));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.IN, false));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.IN, false));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.IN, false));

		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT, true));
		assertEquals(e2.getReversedEdge(), v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT, true));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT, true));
		assertEquals(e2, v1.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT, true));

		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0], EdgeDirection.OUT,
				true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.OUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.OUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.OUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.OUT, true));
		assertEquals(e2, v1.getFirstEdgeOfClass(eclasses[2], EdgeDirection.OUT,
				true));

		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.IN, true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.IN, true));
		assertEquals(e2.getReversedEdge(), v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.IN, true));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.IN, true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.IN, true));
		assertEquals(null, v1.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.IN, true));
	}

	/**
	 * Tests if alpha and omega of an Edge is the same Vertex.
	 */
	@Test
	public void getFirstEdgeTestEdgeClassEdgeDirectionBoolean4() {
		EdgeClass[] eclasses = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink((AbstractSuperNode) v0, (SuperNode) v0);

		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT, false));

		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0], EdgeDirection.OUT,
				false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.OUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.OUT, false));

		assertEquals(e1.getReversedEdge(), v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.IN, false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.IN, false));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.IN, false));

		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.INOUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.INOUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.INOUT, true));

		assertEquals(e1, v0.getFirstEdgeOfClass(eclasses[0], EdgeDirection.OUT,
				true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.OUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.OUT, true));

		assertEquals(e1.getReversedEdge(), v0.getFirstEdgeOfClass(eclasses[0],
				EdgeDirection.IN, true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[1],
				EdgeDirection.IN, true));
		assertEquals(null, v0.getFirstEdgeOfClass(eclasses[2],
				EdgeDirection.IN, true));
	}

	/**
	 * Random tests
	 */
	@Test
	public void getFirstEdgeTestEdgeClassEdgeDirectionBoolean5() {
		EdgeClass[] eclasses = getEdgeClasses();
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			Vertex[] vertices = new Vertex[] { graph.createSubNode(),
					graph.createDoubleSubNode(), graph.createSuperNode() };
			Edge[] firstLinkInOutFalse = new Edge[3];
			Edge[] firstLinkBackInOutFalse = new Edge[3];
			Edge[] firstSubLinkInOutFalse = new Edge[3];
			Edge[] firstLinkInOutTrue = new Edge[3];
			Edge[] firstLinkBackInOutTrue = new Edge[3];
			Edge[] firstSubLinkInOutTrue = new Edge[3];
			Edge[] firstLinkOutFalse = new Edge[3];
			Edge[] firstLinkBackOutFalse = new Edge[3];
			Edge[] firstSubLinkOutFalse = new Edge[3];
			Edge[] firstLinkOutTrue = new Edge[3];
			Edge[] firstLinkBackOutTrue = new Edge[3];
			Edge[] firstSubLinkOutTrue = new Edge[3];
			Edge[] firstLinkInFalse = new Edge[3];
			Edge[] firstLinkBackInFalse = new Edge[3];
			Edge[] firstSubLinkInFalse = new Edge[3];
			Edge[] firstLinkInTrue = new Edge[3];
			Edge[] firstLinkBackInTrue = new Edge[3];
			Edge[] firstSubLinkInTrue = new Edge[3];
			for (int j = 0; j < 5; j++) {
				int edgetype = rand.nextInt(3);
				int start = rand.nextInt(2);
				int end = rand.nextInt(2) + 1;
				switch (edgetype) {
				case 0:
					Edge e0 = graph.createLink(
							(AbstractSuperNode) vertices[start],
							(SuperNode) vertices[end]);
					if (firstLinkInOutFalse[start] == null) {
						firstLinkInOutFalse[start] = e0;
					}
					if (firstLinkInOutTrue[start] == null) {
						firstLinkInOutTrue[start] = e0;
					}
					if (firstLinkOutFalse[start] == null) {
						firstLinkOutFalse[start] = e0;
					}
					if (firstLinkOutTrue[start] == null) {
						firstLinkOutTrue[start] = e0;
					}
					if (firstLinkInOutFalse[end] == null) {
						firstLinkInOutFalse[end] = e0.getReversedEdge();
					}
					if (firstLinkInOutTrue[end] == null) {
						firstLinkInOutTrue[end] = e0.getReversedEdge();
					}
					if (firstLinkInFalse[end] == null) {
						firstLinkInFalse[end] = e0.getReversedEdge();
					}
					if (firstLinkInTrue[end] == null) {
						firstLinkInTrue[end] = e0.getReversedEdge();
					}
					break;
				case 1:
					Edge e1 = graph.createLinkBack((SuperNode) vertices[end],
							(AbstractSuperNode) vertices[start]);
					if (firstLinkBackInOutFalse[end] == null) {
						firstLinkBackInOutFalse[end] = e1;
					}
					if (firstLinkBackInOutTrue[end] == null) {
						firstLinkBackInOutTrue[end] = e1;
					}
					if (firstLinkBackOutFalse[end] == null) {
						firstLinkBackOutFalse[end] = e1;
					}
					if (firstLinkBackOutTrue[end] == null) {
						firstLinkBackOutTrue[end] = e1;
					}
					if (firstLinkBackInOutFalse[start] == null) {
						firstLinkBackInOutFalse[start] = e1.getReversedEdge();
					}
					if (firstLinkBackInOutTrue[start] == null) {
						firstLinkBackInOutTrue[start] = e1.getReversedEdge();
					}
					if (firstLinkBackInFalse[start] == null) {
						firstLinkBackInFalse[start] = e1.getReversedEdge();
					}
					if (firstLinkBackInTrue[start] == null) {
						firstLinkBackInTrue[start] = e1.getReversedEdge();
					}
					break;
				case 2:
					Edge e2 = graph.createSubLink((DoubleSubNode) vertices[1],
							(SuperNode) vertices[end]);
					if (firstLinkInOutFalse[1] == null) {
						firstLinkInOutFalse[1] = e2;
					}
					if (firstSubLinkInOutFalse[1] == null) {
						firstSubLinkInOutFalse[1] = e2;
					}
					if (firstSubLinkInOutTrue[1] == null) {
						firstSubLinkInOutTrue[1] = e2;
					}
					if (firstLinkOutFalse[1] == null) {
						firstLinkOutFalse[1] = e2;
					}
					if (firstSubLinkOutFalse[1] == null) {
						firstSubLinkOutFalse[1] = e2;
					}
					if (firstSubLinkOutTrue[1] == null) {
						firstSubLinkOutTrue[1] = e2;
					}
					if (firstLinkInOutFalse[end] == null) {
						firstLinkInOutFalse[end] = e2.getReversedEdge();
					}
					if (firstSubLinkInOutFalse[end] == null) {
						firstSubLinkInOutFalse[end] = e2.getReversedEdge();
					}
					if (firstSubLinkInOutTrue[end] == null) {
						firstSubLinkInOutTrue[end] = e2.getReversedEdge();
					}
					if (firstLinkInFalse[end] == null) {
						firstLinkInFalse[end] = e2.getReversedEdge();
					}
					if (firstSubLinkInFalse[end] == null) {
						firstSubLinkInFalse[end] = e2.getReversedEdge();
					}
					if (firstSubLinkInTrue[end] == null) {
						firstSubLinkInTrue[end] = e2.getReversedEdge();
					}
					break;
				}
			}
			assertEquals(firstLinkInOutFalse[0], vertices[0]
					.getFirstEdgeOfClass(eclasses[0], EdgeDirection.INOUT,
							false));
			assertEquals(firstLinkInOutFalse[1], vertices[1]
					.getFirstEdgeOfClass(eclasses[0], EdgeDirection.INOUT,
							false));
			assertEquals(firstLinkInOutFalse[2], vertices[2]
					.getFirstEdgeOfClass(eclasses[0], EdgeDirection.INOUT,
							false));
			assertEquals(firstLinkBackInOutFalse[0], vertices[0]
					.getFirstEdgeOfClass(eclasses[2], EdgeDirection.INOUT,
							false));
			assertEquals(firstLinkBackInOutFalse[1], vertices[1]
					.getFirstEdgeOfClass(eclasses[2], EdgeDirection.INOUT,
							false));
			assertEquals(firstLinkBackInOutFalse[2], vertices[2]
					.getFirstEdgeOfClass(eclasses[2], EdgeDirection.INOUT,
							false));
			assertEquals(firstSubLinkInOutFalse[0], vertices[0]
					.getFirstEdgeOfClass(eclasses[1], EdgeDirection.INOUT,
							false));
			assertEquals(firstSubLinkInOutFalse[1], vertices[1]
					.getFirstEdgeOfClass(eclasses[1], EdgeDirection.INOUT,
							false));
			assertEquals(firstSubLinkInOutFalse[2], vertices[2]
					.getFirstEdgeOfClass(eclasses[1], EdgeDirection.INOUT,
							false));

			assertEquals(firstLinkOutFalse[0], vertices[0].getFirstEdgeOfClass(
					eclasses[0], EdgeDirection.OUT, false));
			assertEquals(firstLinkOutFalse[1], vertices[1].getFirstEdgeOfClass(
					eclasses[0], EdgeDirection.OUT, false));
			assertEquals(firstLinkOutFalse[2], vertices[2].getFirstEdgeOfClass(
					eclasses[0], EdgeDirection.OUT, false));
			assertEquals(firstLinkBackOutFalse[0], vertices[0]
					.getFirstEdgeOfClass(eclasses[2], EdgeDirection.OUT, false));
			assertEquals(firstLinkBackOutFalse[1], vertices[1]
					.getFirstEdgeOfClass(eclasses[2], EdgeDirection.OUT, false));
			assertEquals(firstLinkBackOutFalse[2], vertices[2]
					.getFirstEdgeOfClass(eclasses[2], EdgeDirection.OUT, false));
			assertEquals(firstSubLinkOutFalse[0], vertices[0]
					.getFirstEdgeOfClass(eclasses[1], EdgeDirection.OUT, false));
			assertEquals(firstSubLinkOutFalse[1], vertices[1]
					.getFirstEdgeOfClass(eclasses[1], EdgeDirection.OUT, false));
			assertEquals(firstSubLinkOutFalse[2], vertices[2]
					.getFirstEdgeOfClass(eclasses[1], EdgeDirection.OUT, false));

			assertEquals(firstLinkInFalse[0], vertices[0].getFirstEdgeOfClass(
					eclasses[0], EdgeDirection.IN, false));
			assertEquals(firstLinkInFalse[1], vertices[1].getFirstEdgeOfClass(
					eclasses[0], EdgeDirection.IN, false));
			assertEquals(firstLinkInFalse[2], vertices[2].getFirstEdgeOfClass(
					eclasses[0], EdgeDirection.IN, false));
			assertEquals(firstLinkBackInFalse[0], vertices[0]
					.getFirstEdgeOfClass(eclasses[2], EdgeDirection.IN, false));
			assertEquals(firstLinkBackInFalse[1], vertices[1]
					.getFirstEdgeOfClass(eclasses[2], EdgeDirection.IN, false));
			assertEquals(firstLinkBackInFalse[2], vertices[2]
					.getFirstEdgeOfClass(eclasses[2], EdgeDirection.IN, false));
			assertEquals(firstSubLinkInFalse[0], vertices[0]
					.getFirstEdgeOfClass(eclasses[1], EdgeDirection.IN, false));
			assertEquals(firstSubLinkInFalse[1], vertices[1]
					.getFirstEdgeOfClass(eclasses[1], EdgeDirection.IN, false));
			assertEquals(firstSubLinkInFalse[2], vertices[2]
					.getFirstEdgeOfClass(eclasses[1], EdgeDirection.IN, false));

			assertEquals(firstLinkInOutTrue[0],
					vertices[0].getFirstEdgeOfClass(eclasses[0],
							EdgeDirection.INOUT, true));
			assertEquals(firstLinkInOutTrue[1],
					vertices[1].getFirstEdgeOfClass(eclasses[0],
							EdgeDirection.INOUT, true));
			assertEquals(firstLinkInOutTrue[2],
					vertices[2].getFirstEdgeOfClass(eclasses[0],
							EdgeDirection.INOUT, true));
			assertEquals(firstLinkBackInOutTrue[0],
					vertices[0].getFirstEdgeOfClass(eclasses[2],
							EdgeDirection.INOUT, true));
			assertEquals(firstLinkBackInOutTrue[1],
					vertices[1].getFirstEdgeOfClass(eclasses[2],
							EdgeDirection.INOUT, true));
			assertEquals(firstLinkBackInOutTrue[2],
					vertices[2].getFirstEdgeOfClass(eclasses[2],
							EdgeDirection.INOUT, true));
			assertEquals(firstSubLinkInOutTrue[0],
					vertices[0].getFirstEdgeOfClass(eclasses[1],
							EdgeDirection.INOUT, true));
			assertEquals(firstSubLinkInOutTrue[1],
					vertices[1].getFirstEdgeOfClass(eclasses[1],
							EdgeDirection.INOUT, true));
			assertEquals(firstSubLinkInOutTrue[2],
					vertices[2].getFirstEdgeOfClass(eclasses[1],
							EdgeDirection.INOUT, true));

			assertEquals(firstLinkOutTrue[0], vertices[0].getFirstEdgeOfClass(
					eclasses[0], EdgeDirection.OUT, true));
			assertEquals(firstLinkOutTrue[1], vertices[1].getFirstEdgeOfClass(
					eclasses[0], EdgeDirection.OUT, true));
			assertEquals(firstLinkOutTrue[2], vertices[2].getFirstEdgeOfClass(
					eclasses[0], EdgeDirection.OUT, true));
			assertEquals(firstLinkBackOutTrue[0], vertices[0]
					.getFirstEdgeOfClass(eclasses[2], EdgeDirection.OUT, true));
			assertEquals(firstLinkBackOutTrue[1], vertices[1]
					.getFirstEdgeOfClass(eclasses[2], EdgeDirection.OUT, true));
			assertEquals(firstLinkBackOutTrue[2], vertices[2]
					.getFirstEdgeOfClass(eclasses[2], EdgeDirection.OUT, true));
			assertEquals(firstSubLinkOutTrue[0], vertices[0]
					.getFirstEdgeOfClass(eclasses[1], EdgeDirection.OUT, true));
			assertEquals(firstSubLinkOutTrue[1], vertices[1]
					.getFirstEdgeOfClass(eclasses[1], EdgeDirection.OUT, true));
			assertEquals(firstSubLinkOutTrue[2], vertices[2]
					.getFirstEdgeOfClass(eclasses[1], EdgeDirection.OUT, true));

			assertEquals(firstLinkInTrue[0], vertices[0].getFirstEdgeOfClass(
					eclasses[0], EdgeDirection.IN, true));
			assertEquals(firstLinkInTrue[1], vertices[1].getFirstEdgeOfClass(
					eclasses[0], EdgeDirection.IN, true));
			assertEquals(firstLinkInTrue[2], vertices[2].getFirstEdgeOfClass(
					eclasses[0], EdgeDirection.IN, true));
			assertEquals(firstLinkBackInTrue[0], vertices[0]
					.getFirstEdgeOfClass(eclasses[2], EdgeDirection.IN, true));
			assertEquals(firstLinkBackInTrue[1], vertices[1]
					.getFirstEdgeOfClass(eclasses[2], EdgeDirection.IN, true));
			assertEquals(firstLinkBackInTrue[2], vertices[2]
					.getFirstEdgeOfClass(eclasses[2], EdgeDirection.IN, true));
			assertEquals(firstSubLinkInTrue[0], vertices[0]
					.getFirstEdgeOfClass(eclasses[1], EdgeDirection.IN, true));
			assertEquals(firstSubLinkInTrue[1], vertices[1]
					.getFirstEdgeOfClass(eclasses[1], EdgeDirection.IN, true));
			assertEquals(firstSubLinkInTrue[2], vertices[2]
					.getFirstEdgeOfClass(eclasses[1], EdgeDirection.IN, true));
		}
	}

	// tests of the method Edge getFirstEdgeOfClass(Class<? extends Edge>
	// anEdgeClass, EdgeDirection orientation, boolean noSubclasses);

	/**
	 * Tests if a node has no Edges
	 */
	@Test
	public void getFirstEdgeTestClassEdgeDirectionBoolean0() {
		Vertex v0 = graph.createDoubleSubNode();

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.INOUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, false));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.OUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, false));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.IN, false));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN, false));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, false));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, true));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.OUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, true));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.IN, true));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN, true));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, true));
	}

	/**
	 * Tests if a node has only one Edge
	 */
	@Test
	public void getFirstEdgeTestClassEdgeDirectionBoolean1() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);

		assertEquals(e, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.INOUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, false));
		assertEquals(e.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.INOUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, false));

		assertEquals(e, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.OUT,
				false));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.OUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, false));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.IN, false));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN, false));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, false));
		assertEquals(e.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.IN, false));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN, false));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, false));

		assertEquals(e, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, true));
		assertEquals(e.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, true));

		assertEquals(e, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.OUT,
				true));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.OUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, true));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.IN, true));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN, true));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, true));
		assertEquals(e.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.IN, true));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN, true));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, true));
	}

	/**
	 * Tests if a node has an edge which extends another edge
	 */
	@Test
	public void getFirstEdgeTestClassEdgeDirectionBoolean2() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e1 = graph.createSubLink((DoubleSubNode) v0, (SuperNode) v1);

		assertEquals(e1, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.INOUT, false));
		assertEquals(e1, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, false));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.INOUT, false));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, false));

		assertEquals(e1, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.OUT,
				false));
		assertEquals(e1, v0.getFirstEdgeOfClass(SubLink.class, EdgeDirection.OUT,
				false));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.OUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, false));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.IN, false));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN, false));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, false));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.IN, false));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN, false));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, false));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.INOUT, true));
		assertEquals(e1, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.INOUT, true));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, true));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.OUT, true));
		assertEquals(e1, v0.getFirstEdgeOfClass(SubLink.class, EdgeDirection.OUT,
				true));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.OUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, true));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.IN, true));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN, true));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, true));
		assertEquals(null, v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.IN, true));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN, true));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, true));
	}

	/**
	 * Tests if a node has two Edges.
	 */
	@Test
	public void getFirstEdgeTestClassEdgeDirectionBoolean3() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		Edge e2 = graph.createLinkBack((SuperNode) v1, (AbstractSuperNode) v0);

		assertEquals(e1, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.INOUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, false));
		assertEquals(e2.getReversedEdge(), v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, false));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.INOUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, false));
		assertEquals(e2, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, false));

		assertEquals(e1, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.OUT,
				false));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.OUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, false));
		assertEquals(e2, v1.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.OUT,
				false));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.IN, false));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN, false));
		assertEquals(e2.getReversedEdge(), v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, false));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.IN, false));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN, false));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, false));

		assertEquals(e1, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, true));
		assertEquals(e2.getReversedEdge(), v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, true));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, true));
		assertEquals(e2, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, true));

		assertEquals(e1, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.OUT,
				true));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.OUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, true));
		assertEquals(e2, v1.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.OUT,
				true));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.IN, true));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN, true));
		assertEquals(e2.getReversedEdge(), v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, true));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.IN, true));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN, true));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, true));
	}

	/**
	 * Tests if alpha and omega of an Edge is the same Vertex.
	 */
	@Test
	public void getFirstEdgeTestClassEdgeDirectionBoolean4() {
		Vertex v0 = graph.createDoubleSubNode();
		Edge e1 = graph.createLink((AbstractSuperNode) v0, (SuperNode) v0);

		assertEquals(e1, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.INOUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, false));

		assertEquals(e1, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.OUT,
				false));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, false));

		assertEquals(e1.getReversedEdge(), v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.IN, false));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN, false));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, false));

		assertEquals(e1, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.INOUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, true));

		assertEquals(e1, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.OUT,
				true));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, true));

		assertEquals(e1.getReversedEdge(), v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.IN, true));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN, true));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, true));
	}

	/**
	 * Random tests
	 */
	@Test
	public void getFirstEdgeTestClassEdgeDirectionBoolean5() {
		for (int i = 0; i < 1000; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			Vertex[] vertices = new Vertex[] { graph.createSubNode(),
					graph.createDoubleSubNode(), graph.createSuperNode() };
			Edge[] firstLinkInOutFalse = new Edge[3];
			Edge[] firstLinkBackInOutFalse = new Edge[3];
			Edge[] firstSubLinkInOutFalse = new Edge[3];
			Edge[] firstLinkInOutTrue = new Edge[3];
			Edge[] firstLinkBackInOutTrue = new Edge[3];
			Edge[] firstSubLinkInOutTrue = new Edge[3];
			Edge[] firstLinkOutFalse = new Edge[3];
			Edge[] firstLinkBackOutFalse = new Edge[3];
			Edge[] firstSubLinkOutFalse = new Edge[3];
			Edge[] firstLinkOutTrue = new Edge[3];
			Edge[] firstLinkBackOutTrue = new Edge[3];
			Edge[] firstSubLinkOutTrue = new Edge[3];
			Edge[] firstLinkInFalse = new Edge[3];
			Edge[] firstLinkBackInFalse = new Edge[3];
			Edge[] firstSubLinkInFalse = new Edge[3];
			Edge[] firstLinkInTrue = new Edge[3];
			Edge[] firstLinkBackInTrue = new Edge[3];
			Edge[] firstSubLinkInTrue = new Edge[3];
			for (int j = 0; j < 5; j++) {
				int edgetype = rand.nextInt(3);
				int start = rand.nextInt(2);
				int end = rand.nextInt(2) + 1;
				switch (edgetype) {
				case 0:
					Edge e0 = graph.createLink(
							(AbstractSuperNode) vertices[start],
							(SuperNode) vertices[end]);
					if (firstLinkInOutFalse[start] == null) {
						firstLinkInOutFalse[start] = e0;
					}
					if (firstLinkInOutTrue[start] == null) {
						firstLinkInOutTrue[start] = e0;
					}
					if (firstLinkOutFalse[start] == null) {
						firstLinkOutFalse[start] = e0;
					}
					if (firstLinkOutTrue[start] == null) {
						firstLinkOutTrue[start] = e0;
					}
					if (firstLinkInOutFalse[end] == null) {
						firstLinkInOutFalse[end] = e0.getReversedEdge();
					}
					if (firstLinkInOutTrue[end] == null) {
						firstLinkInOutTrue[end] = e0.getReversedEdge();
					}
					if (firstLinkInFalse[end] == null) {
						firstLinkInFalse[end] = e0.getReversedEdge();
					}
					if (firstLinkInTrue[end] == null) {
						firstLinkInTrue[end] = e0.getReversedEdge();
					}
					break;
				case 1:
					Edge e1 = graph.createLinkBack((SuperNode) vertices[end],
							(AbstractSuperNode) vertices[start]);
					if (firstLinkBackInOutFalse[end] == null) {
						firstLinkBackInOutFalse[end] = e1;
					}
					if (firstLinkBackInOutTrue[end] == null) {
						firstLinkBackInOutTrue[end] = e1;
					}
					if (firstLinkBackOutFalse[end] == null) {
						firstLinkBackOutFalse[end] = e1;
					}
					if (firstLinkBackOutTrue[end] == null) {
						firstLinkBackOutTrue[end] = e1;
					}
					if (firstLinkBackInOutFalse[start] == null) {
						firstLinkBackInOutFalse[start] = e1.getReversedEdge();
					}
					if (firstLinkBackInOutTrue[start] == null) {
						firstLinkBackInOutTrue[start] = e1.getReversedEdge();
					}
					if (firstLinkBackInFalse[start] == null) {
						firstLinkBackInFalse[start] = e1.getReversedEdge();
					}
					if (firstLinkBackInTrue[start] == null) {
						firstLinkBackInTrue[start] = e1.getReversedEdge();
					}
					break;
				case 2:
					Edge e2 = graph.createSubLink((DoubleSubNode) vertices[1],
							(SuperNode) vertices[end]);
					if (firstLinkInOutFalse[1] == null) {
						firstLinkInOutFalse[1] = e2;
					}
					if (firstSubLinkInOutFalse[1] == null) {
						firstSubLinkInOutFalse[1] = e2;
					}
					if (firstSubLinkInOutTrue[1] == null) {
						firstSubLinkInOutTrue[1] = e2;
					}
					if (firstLinkOutFalse[1] == null) {
						firstLinkOutFalse[1] = e2;
					}
					if (firstSubLinkOutFalse[1] == null) {
						firstSubLinkOutFalse[1] = e2;
					}
					if (firstSubLinkOutTrue[1] == null) {
						firstSubLinkOutTrue[1] = e2;
					}
					if (firstLinkInOutFalse[end] == null) {
						firstLinkInOutFalse[end] = e2.getReversedEdge();
					}
					if (firstSubLinkInOutFalse[end] == null) {
						firstSubLinkInOutFalse[end] = e2.getReversedEdge();
					}
					if (firstSubLinkInOutTrue[end] == null) {
						firstSubLinkInOutTrue[end] = e2.getReversedEdge();
					}
					if (firstLinkInFalse[end] == null) {
						firstLinkInFalse[end] = e2.getReversedEdge();
					}
					if (firstSubLinkInFalse[end] == null) {
						firstSubLinkInFalse[end] = e2.getReversedEdge();
					}
					if (firstSubLinkInTrue[end] == null) {
						firstSubLinkInTrue[end] = e2.getReversedEdge();
					}
					break;
				}
			}
			assertEquals(firstLinkInOutFalse[0], vertices[0]
					.getFirstEdgeOfClass(Link.class, EdgeDirection.INOUT,
							false));
			assertEquals(firstLinkInOutFalse[1], vertices[1]
					.getFirstEdgeOfClass(Link.class, EdgeDirection.INOUT,
							false));
			assertEquals(firstLinkInOutFalse[2], vertices[2]
					.getFirstEdgeOfClass(Link.class, EdgeDirection.INOUT,
							false));
			assertEquals(firstLinkBackInOutFalse[0], vertices[0]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.INOUT,
							false));
			assertEquals(firstLinkBackInOutFalse[1], vertices[1]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.INOUT,
							false));
			assertEquals(firstLinkBackInOutFalse[2], vertices[2]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.INOUT,
							false));
			assertEquals(firstSubLinkInOutFalse[0], vertices[0]
					.getFirstEdgeOfClass(SubLink.class, EdgeDirection.INOUT,
							false));
			assertEquals(firstSubLinkInOutFalse[1], vertices[1]
					.getFirstEdgeOfClass(SubLink.class, EdgeDirection.INOUT,
							false));
			assertEquals(firstSubLinkInOutFalse[2], vertices[2]
					.getFirstEdgeOfClass(SubLink.class, EdgeDirection.INOUT,
							false));

			assertEquals(firstLinkOutFalse[0], vertices[0].getFirstEdgeOfClass(
					Link.class, EdgeDirection.OUT, false));
			assertEquals(firstLinkOutFalse[1], vertices[1].getFirstEdgeOfClass(
					Link.class, EdgeDirection.OUT, false));
			assertEquals(firstLinkOutFalse[2], vertices[2].getFirstEdgeOfClass(
					Link.class, EdgeDirection.OUT, false));
			assertEquals(firstLinkBackOutFalse[0], vertices[0]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.OUT, false));
			assertEquals(firstLinkBackOutFalse[1], vertices[1]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.OUT, false));
			assertEquals(firstLinkBackOutFalse[2], vertices[2]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.OUT, false));
			assertEquals(firstSubLinkOutFalse[0], vertices[0]
					.getFirstEdgeOfClass(SubLink.class, EdgeDirection.OUT, false));
			assertEquals(firstSubLinkOutFalse[1], vertices[1]
					.getFirstEdgeOfClass(SubLink.class, EdgeDirection.OUT, false));
			assertEquals(firstSubLinkOutFalse[2], vertices[2]
					.getFirstEdgeOfClass(SubLink.class, EdgeDirection.OUT, false));

			assertEquals(firstLinkInFalse[0], vertices[0].getFirstEdgeOfClass(
					Link.class, EdgeDirection.IN, false));
			assertEquals(firstLinkInFalse[1], vertices[1].getFirstEdgeOfClass(
					Link.class, EdgeDirection.IN, false));
			assertEquals(firstLinkInFalse[2], vertices[2].getFirstEdgeOfClass(
					Link.class, EdgeDirection.IN, false));
			assertEquals(firstLinkBackInFalse[0], vertices[0]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.IN, false));
			assertEquals(firstLinkBackInFalse[1], vertices[1]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.IN, false));
			assertEquals(firstLinkBackInFalse[2], vertices[2]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.IN, false));
			assertEquals(firstSubLinkInFalse[0], vertices[0]
					.getFirstEdgeOfClass(SubLink.class, EdgeDirection.IN, false));
			assertEquals(firstSubLinkInFalse[1], vertices[1]
					.getFirstEdgeOfClass(SubLink.class, EdgeDirection.IN, false));
			assertEquals(firstSubLinkInFalse[2], vertices[2]
					.getFirstEdgeOfClass(SubLink.class, EdgeDirection.IN, false));

			assertEquals(firstLinkInOutTrue[0],
					vertices[0].getFirstEdgeOfClass(Link.class,
							EdgeDirection.INOUT, true));
			assertEquals(firstLinkInOutTrue[1],
					vertices[1].getFirstEdgeOfClass(Link.class,
							EdgeDirection.INOUT, true));
			assertEquals(firstLinkInOutTrue[2],
					vertices[2].getFirstEdgeOfClass(Link.class,
							EdgeDirection.INOUT, true));
			assertEquals(firstLinkBackInOutTrue[0],
					vertices[0].getFirstEdgeOfClass(LinkBack.class,
							EdgeDirection.INOUT, true));
			assertEquals(firstLinkBackInOutTrue[1],
					vertices[1].getFirstEdgeOfClass(LinkBack.class,
							EdgeDirection.INOUT, true));
			assertEquals(firstLinkBackInOutTrue[2],
					vertices[2].getFirstEdgeOfClass(LinkBack.class,
							EdgeDirection.INOUT, true));
			assertEquals(firstSubLinkInOutTrue[0],
					vertices[0].getFirstEdgeOfClass(SubLink.class,
							EdgeDirection.INOUT, true));
			assertEquals(firstSubLinkInOutTrue[1],
					vertices[1].getFirstEdgeOfClass(SubLink.class,
							EdgeDirection.INOUT, true));
			assertEquals(firstSubLinkInOutTrue[2],
					vertices[2].getFirstEdgeOfClass(SubLink.class,
							EdgeDirection.INOUT, true));

			assertEquals(firstLinkOutTrue[0], vertices[0].getFirstEdgeOfClass(
					Link.class, EdgeDirection.OUT, true));
			assertEquals(firstLinkOutTrue[1], vertices[1].getFirstEdgeOfClass(
					Link.class, EdgeDirection.OUT, true));
			assertEquals(firstLinkOutTrue[2], vertices[2].getFirstEdgeOfClass(
					Link.class, EdgeDirection.OUT, true));
			assertEquals(firstLinkBackOutTrue[0], vertices[0]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.OUT, true));
			assertEquals(firstLinkBackOutTrue[1], vertices[1]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.OUT, true));
			assertEquals(firstLinkBackOutTrue[2], vertices[2]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.OUT, true));
			assertEquals(firstSubLinkOutTrue[0], vertices[0]
					.getFirstEdgeOfClass(SubLink.class, EdgeDirection.OUT, true));
			assertEquals(firstSubLinkOutTrue[1], vertices[1]
					.getFirstEdgeOfClass(SubLink.class, EdgeDirection.OUT, true));
			assertEquals(firstSubLinkOutTrue[2], vertices[2]
					.getFirstEdgeOfClass(SubLink.class, EdgeDirection.OUT, true));

			assertEquals(firstLinkInTrue[0], vertices[0].getFirstEdgeOfClass(
					Link.class, EdgeDirection.IN, true));
			assertEquals(firstLinkInTrue[1], vertices[1].getFirstEdgeOfClass(
					Link.class, EdgeDirection.IN, true));
			assertEquals(firstLinkInTrue[2], vertices[2].getFirstEdgeOfClass(
					Link.class, EdgeDirection.IN, true));
			assertEquals(firstLinkBackInTrue[0], vertices[0]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.IN, true));
			assertEquals(firstLinkBackInTrue[1], vertices[1]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.IN, true));
			assertEquals(firstLinkBackInTrue[2], vertices[2]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.IN, true));
			assertEquals(firstSubLinkInTrue[0], vertices[0]
					.getFirstEdgeOfClass(SubLink.class, EdgeDirection.IN, true));
			assertEquals(firstSubLinkInTrue[1], vertices[1]
					.getFirstEdgeOfClass(SubLink.class, EdgeDirection.IN, true));
			assertEquals(firstSubLinkInTrue[2], vertices[2]
					.getFirstEdgeOfClass(SubLink.class, EdgeDirection.IN, true));
		}
	}

	// tests of the method boolean isBefore(Vertex v);
	// (tested in VertexList Test)
	
	// tests of the method void putBefore(Vertex v);
	// (tested in VertexList Test)
	
	// tests of the method boolean isAfter(Vertex v);
	// (tested in VertexList Test)
	
	// tests of the method void putAfter(Vertex v);
	// (tested in VertexList Test)
	
	// tests of the method void delete(Vertex v);
	// (tested in VertexList Test)
	
	// tests of the method Iterable<Edge> incidences();
	// (tested in VertexList Test except failfast)
	
	/**
	 * An exception should occur if the current edge is deleted.
	 */
	@Test(expected=ConcurrentModificationException.class)
	public void incidencetsTestFailFast0(){
		Vertex v0=graph.createDoubleSubNode();
		Vertex v1=graph.createDoubleSubNode();
		graph.createLink((AbstractSuperNode)v0, (SuperNode)v1);
		graph.createLink((AbstractSuperNode)v0, (SuperNode)v1);
		graph.createLink((AbstractSuperNode)v0, (SuperNode)v1);
		Iterator<Edge> iter=v0.incidences().iterator();
		iter.hasNext();
		Edge e=iter.next();
		e.delete();
		iter.hasNext();
		iter.next();
	}
	
	/**
	 * An exception should occur if the position of the current edge is changed.
	 */
	@Test(expected=ConcurrentModificationException.class)
	public void incidencetsTestFailFast1(){
		Vertex v0=graph.createDoubleSubNode();
		Vertex v1=graph.createDoubleSubNode();
		graph.createLink((AbstractSuperNode)v0, (SuperNode)v1);
		graph.createLink((AbstractSuperNode)v0, (SuperNode)v1);
		Edge last=graph.createLink((AbstractSuperNode)v0, (SuperNode)v1);
		Iterator<Edge> iter=v0.incidences().iterator();
		iter.hasNext();
		Edge e=iter.next();
		e.putEdgeAfter(last);
		iter.hasNext();
		iter.next();
	}
	
	/**
	 * An exception should occur if a previous edge is deleted.
	 */
	@Test(expected=ConcurrentModificationException.class)
	public void incidencetsTestFailFast2(){
		Vertex v0=graph.createDoubleSubNode();
		Vertex v1=graph.createDoubleSubNode();
		graph.createLink((AbstractSuperNode)v0, (SuperNode)v1);
		graph.createLink((AbstractSuperNode)v0, (SuperNode)v1);
		graph.createLink((AbstractSuperNode)v0, (SuperNode)v1);
		Iterator<Edge> iter=v0.incidences().iterator();
		iter.hasNext();
		Edge e=iter.next();
		iter.hasNext();
		iter.next();
		e.delete();
		iter.hasNext();
		iter.next();
	}
	
	/**
	 * An exception should occur if a following edge is deleted.
	 */
	@Test(expected=ConcurrentModificationException.class)
	public void incidencetsTestFailFast3(){
		Vertex v0=graph.createDoubleSubNode();
		Vertex v1=graph.createDoubleSubNode();
		graph.createLink((AbstractSuperNode)v0, (SuperNode)v1);
		graph.createLink((AbstractSuperNode)v0, (SuperNode)v1);
		Edge last=graph.createLink((AbstractSuperNode)v0, (SuperNode)v1);
		Iterator<Edge> iter=v0.incidences().iterator();
		last.delete();
		iter.hasNext();
		iter.next();
	}
	
	/**
	 * An exception should occur if an edge is added.
	 */
	@Test(expected=ConcurrentModificationException.class)
	public void incidencetsTestFailFast4(){
		Vertex v0=graph.createDoubleSubNode();
		Vertex v1=graph.createDoubleSubNode();
		graph.createLink((AbstractSuperNode)v0, (SuperNode)v1);
		graph.createLink((AbstractSuperNode)v0, (SuperNode)v1);
		graph.createLink((AbstractSuperNode)v0, (SuperNode)v1);
		Iterator<Edge> iter=v0.incidences().iterator();
		graph.createLink((AbstractSuperNode)v0, (SuperNode)v1);
		iter.hasNext();
		iter.next();
	}
	
	// tests of the method Iterable<Edge> incidences(EdgeDirection dir);
	
	
}
