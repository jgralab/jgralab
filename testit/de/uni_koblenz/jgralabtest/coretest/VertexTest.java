package de.uni_koblenz.jgralabtest.coretest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.Schema;
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

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.IN,
				false));
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

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.IN,
				true));
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

		assertEquals(e, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.INOUT,
				false));
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

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.IN,
				false));
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

		assertEquals(e, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.INOUT,
				true));
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

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.IN,
				true));
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
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(
				SubLink.class, EdgeDirection.INOUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, false));

		assertEquals(e1, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.OUT,
				false));
		assertEquals(e1, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, false));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.OUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, false));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, false));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.IN,
				false));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN, false));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, false));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.IN, false));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(
				SubLink.class, EdgeDirection.IN, false));
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
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(
				SubLink.class, EdgeDirection.INOUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.INOUT, true));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class,
				EdgeDirection.OUT, true));
		assertEquals(e1, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, true));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(Link.class,
				EdgeDirection.OUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.OUT, true));
		assertEquals(null, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, true));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.IN,
				true));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN, true));
		assertEquals(null, v0.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.IN, true));
		assertEquals(null, v1.getFirstEdgeOfClass(Link.class, EdgeDirection.IN,
				true));
		assertEquals(e1.getReversedEdge(), v1.getFirstEdgeOfClass(
				SubLink.class, EdgeDirection.IN, true));
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
		assertEquals(e2.getReversedEdge(), v0.getFirstEdgeOfClass(
				LinkBack.class, EdgeDirection.INOUT, false));
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
		assertEquals(e2, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, false));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.IN,
				false));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN, false));
		assertEquals(e2.getReversedEdge(), v0.getFirstEdgeOfClass(
				LinkBack.class, EdgeDirection.IN, false));
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
		assertEquals(e2.getReversedEdge(), v0.getFirstEdgeOfClass(
				LinkBack.class, EdgeDirection.INOUT, true));
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
		assertEquals(e2, v1.getFirstEdgeOfClass(LinkBack.class,
				EdgeDirection.OUT, true));

		assertEquals(null, v0.getFirstEdgeOfClass(Link.class, EdgeDirection.IN,
				true));
		assertEquals(null, v0.getFirstEdgeOfClass(SubLink.class,
				EdgeDirection.IN, true));
		assertEquals(e2.getReversedEdge(), v0.getFirstEdgeOfClass(
				LinkBack.class, EdgeDirection.IN, true));
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
			assertEquals(firstLinkInOutFalse[0],
					vertices[0].getFirstEdgeOfClass(Link.class,
							EdgeDirection.INOUT, false));
			assertEquals(firstLinkInOutFalse[1],
					vertices[1].getFirstEdgeOfClass(Link.class,
							EdgeDirection.INOUT, false));
			assertEquals(firstLinkInOutFalse[2],
					vertices[2].getFirstEdgeOfClass(Link.class,
							EdgeDirection.INOUT, false));
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
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.OUT,
							false));
			assertEquals(firstLinkBackOutFalse[1], vertices[1]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.OUT,
							false));
			assertEquals(firstLinkBackOutFalse[2], vertices[2]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.OUT,
							false));
			assertEquals(firstSubLinkOutFalse[0], vertices[0]
					.getFirstEdgeOfClass(SubLink.class, EdgeDirection.OUT,
							false));
			assertEquals(firstSubLinkOutFalse[1], vertices[1]
					.getFirstEdgeOfClass(SubLink.class, EdgeDirection.OUT,
							false));
			assertEquals(firstSubLinkOutFalse[2], vertices[2]
					.getFirstEdgeOfClass(SubLink.class, EdgeDirection.OUT,
							false));

			assertEquals(firstLinkInFalse[0], vertices[0].getFirstEdgeOfClass(
					Link.class, EdgeDirection.IN, false));
			assertEquals(firstLinkInFalse[1], vertices[1].getFirstEdgeOfClass(
					Link.class, EdgeDirection.IN, false));
			assertEquals(firstLinkInFalse[2], vertices[2].getFirstEdgeOfClass(
					Link.class, EdgeDirection.IN, false));
			assertEquals(firstLinkBackInFalse[0], vertices[0]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.IN,
							false));
			assertEquals(firstLinkBackInFalse[1], vertices[1]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.IN,
							false));
			assertEquals(firstLinkBackInFalse[2], vertices[2]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.IN,
							false));
			assertEquals(firstSubLinkInFalse[0],
					vertices[0].getFirstEdgeOfClass(SubLink.class,
							EdgeDirection.IN, false));
			assertEquals(firstSubLinkInFalse[1],
					vertices[1].getFirstEdgeOfClass(SubLink.class,
							EdgeDirection.IN, false));
			assertEquals(firstSubLinkInFalse[2],
					vertices[2].getFirstEdgeOfClass(SubLink.class,
							EdgeDirection.IN, false));

			assertEquals(firstLinkInOutTrue[0], vertices[0]
					.getFirstEdgeOfClass(Link.class, EdgeDirection.INOUT, true));
			assertEquals(firstLinkInOutTrue[1], vertices[1]
					.getFirstEdgeOfClass(Link.class, EdgeDirection.INOUT, true));
			assertEquals(firstLinkInOutTrue[2], vertices[2]
					.getFirstEdgeOfClass(Link.class, EdgeDirection.INOUT, true));
			assertEquals(firstLinkBackInOutTrue[0], vertices[0]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.INOUT,
							true));
			assertEquals(firstLinkBackInOutTrue[1], vertices[1]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.INOUT,
							true));
			assertEquals(firstLinkBackInOutTrue[2], vertices[2]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.INOUT,
							true));
			assertEquals(firstSubLinkInOutTrue[0], vertices[0]
					.getFirstEdgeOfClass(SubLink.class, EdgeDirection.INOUT,
							true));
			assertEquals(firstSubLinkInOutTrue[1], vertices[1]
					.getFirstEdgeOfClass(SubLink.class, EdgeDirection.INOUT,
							true));
			assertEquals(firstSubLinkInOutTrue[2], vertices[2]
					.getFirstEdgeOfClass(SubLink.class, EdgeDirection.INOUT,
							true));

			assertEquals(firstLinkOutTrue[0], vertices[0].getFirstEdgeOfClass(
					Link.class, EdgeDirection.OUT, true));
			assertEquals(firstLinkOutTrue[1], vertices[1].getFirstEdgeOfClass(
					Link.class, EdgeDirection.OUT, true));
			assertEquals(firstLinkOutTrue[2], vertices[2].getFirstEdgeOfClass(
					Link.class, EdgeDirection.OUT, true));
			assertEquals(firstLinkBackOutTrue[0], vertices[0]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.OUT,
							true));
			assertEquals(firstLinkBackOutTrue[1], vertices[1]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.OUT,
							true));
			assertEquals(firstLinkBackOutTrue[2], vertices[2]
					.getFirstEdgeOfClass(LinkBack.class, EdgeDirection.OUT,
							true));
			assertEquals(firstSubLinkOutTrue[0],
					vertices[0].getFirstEdgeOfClass(SubLink.class,
							EdgeDirection.OUT, true));
			assertEquals(firstSubLinkOutTrue[1],
					vertices[1].getFirstEdgeOfClass(SubLink.class,
							EdgeDirection.OUT, true));
			assertEquals(firstSubLinkOutTrue[2],
					vertices[2].getFirstEdgeOfClass(SubLink.class,
							EdgeDirection.OUT, true));

			assertEquals(firstLinkInTrue[0], vertices[0].getFirstEdgeOfClass(
					Link.class, EdgeDirection.IN, true));
			assertEquals(firstLinkInTrue[1], vertices[1].getFirstEdgeOfClass(
					Link.class, EdgeDirection.IN, true));
			assertEquals(firstLinkInTrue[2], vertices[2].getFirstEdgeOfClass(
					Link.class, EdgeDirection.IN, true));
			assertEquals(firstLinkBackInTrue[0],
					vertices[0].getFirstEdgeOfClass(LinkBack.class,
							EdgeDirection.IN, true));
			assertEquals(firstLinkBackInTrue[1],
					vertices[1].getFirstEdgeOfClass(LinkBack.class,
							EdgeDirection.IN, true));
			assertEquals(firstLinkBackInTrue[2],
					vertices[2].getFirstEdgeOfClass(LinkBack.class,
							EdgeDirection.IN, true));
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
	@Test(expected = ConcurrentModificationException.class)
	public void incidencesTestFailFast0() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		Iterator<Edge> iter = v0.incidences().iterator();
		iter.hasNext();
		Edge e = iter.next();
		e.delete();
		iter.hasNext();
		iter.next();
	}

	/**
	 * An exception should occur if the position of the current edge is changed.
	 */
	@Test(expected = ConcurrentModificationException.class)
	public void incidencesTestFailFast1() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		Edge last = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		Iterator<Edge> iter = v0.incidences().iterator();
		iter.hasNext();
		Edge e = iter.next();
		e.putEdgeAfter(last);
		iter.hasNext();
		iter.next();
	}

	/**
	 * An exception should occur if a previous edge is deleted.
	 */
	@Test(expected = ConcurrentModificationException.class)
	public void incidencesTestFailFast2() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		Iterator<Edge> iter = v0.incidences().iterator();
		iter.hasNext();
		Edge e = iter.next();
		iter.hasNext();
		iter.next();
		e.delete();
		iter.hasNext();
		iter.next();
	}

	/**
	 * An exception should occur if a following edge is deleted.
	 */
	@Test(expected = ConcurrentModificationException.class)
	public void incidencesTestFailFast3() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		Edge last = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		Iterator<Edge> iter = v0.incidences().iterator();
		last.delete();
		iter.hasNext();
		iter.next();
	}

	/**
	 * An exception should occur if an edge is added.
	 */
	@Test(expected = ConcurrentModificationException.class)
	public void incidencesTestFailFast4() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		Iterator<Edge> iter = v0.incidences().iterator();
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		iter.hasNext();
		iter.next();
	}

	/**
	 * An exception should occur if an edge gets another alpha vertex.
	 */
	@Test(expected = ConcurrentModificationException.class)
	public void incidencesTestFailFast5() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		Edge e = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		Iterator<Edge> iter = v0.incidences().iterator();
		e.setAlpha(v1);
		iter.hasNext();
		iter.next();
	}

	// tests of the method Iterable<Edge> incidences(EdgeDirection dir);

	/**
	 * Checks if the expected incidences equals the returned incidences.
	 * 
	 * @param v
	 *            the node of which the incidences should be tested
	 * @param ec
	 *            or <code>c</code> must be <code>null</code>
	 * @param c
	 *            or <code>ec</code> must be <code>null</code>
	 * @param dir
	 *            must be != <code>null</code> if ec==null and c==null
	 * @param expectedIncidences
	 *            the expected incidences
	 */
	private void checkIncidenceList(Vertex v, EdgeClass ec,
			Class<? extends Edge> c, EdgeDirection dir,
			List<Edge> expectedIncidences) {
		int i = 0;
		if (dir == null) {
			if (ec == null) {
				for (Edge e : v.incidences(c)) {
					assertEquals(expectedIncidences.get(i), e);
					i++;
				}
			} else {
				for (Edge e : v.incidences(ec)) {
					assertEquals(expectedIncidences.get(i), e);
					i++;
				}
			}
		} else {
			if (ec != null) {
				for (Edge e : v.incidences(ec, dir)) {
					assertEquals(expectedIncidences.get(i), e);
					i++;
				}
			} else if (c != null) {
				for (Edge e : v.incidences(c, dir)) {
					assertEquals(expectedIncidences.get(i), e);
					i++;
				}
			} else {
				for (Edge e : v.incidences(dir)) {
					assertEquals(expectedIncidences.get(i), e);
					i++;
				}
			}
		}
	}

	/**
	 * Checks if a vertex has no incidences.
	 */
	@Test
	public void incidencesTestEdgeDirection0() {
		Vertex v0 = graph.createDoubleSubNode();
		checkIncidenceList(v0, null, null, EdgeDirection.INOUT,
				new LinkedList<Edge>());
		checkIncidenceList(v0, null, null, EdgeDirection.OUT,
				new LinkedList<Edge>());
		checkIncidenceList(v0, null, null, EdgeDirection.IN,
				new LinkedList<Edge>());
	}

	/**
	 * Checks if a vertex has only outgoing or ingoing incidences.
	 */
	@Test
	public void incidencesTestEdgeDirection1() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		LinkedList<Edge> v0inout = new LinkedList<Edge>();
		LinkedList<Edge> v0out = new LinkedList<Edge>();
		LinkedList<Edge> v0in = new LinkedList<Edge>();
		LinkedList<Edge> v1inout = new LinkedList<Edge>();
		LinkedList<Edge> v1out = new LinkedList<Edge>();
		LinkedList<Edge> v1in = new LinkedList<Edge>();
		Edge e = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		v0inout.add(e);
		v0out.add(e);
		v1inout.add(e.getReversedEdge());
		v1in.add(e.getReversedEdge());

		checkIncidenceList(v0, null, null, EdgeDirection.INOUT, v0inout);
		checkIncidenceList(v0, null, null, EdgeDirection.OUT, v0out);
		checkIncidenceList(v0, null, null, EdgeDirection.IN, v0in);

		checkIncidenceList(v1, null, null, EdgeDirection.INOUT, v1inout);
		checkIncidenceList(v1, null, null, EdgeDirection.OUT, v1out);
		checkIncidenceList(v1, null, null, EdgeDirection.IN, v1in);
	}

	/**
	 * Checks incidences in a manually build graph.
	 */
	@Test
	public void incidencesTestEdgeDirection2() {
		Vertex v0 = graph.createSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Vertex v2 = graph.createSuperNode();
		LinkedList<Edge> v0inout = new LinkedList<Edge>();
		LinkedList<Edge> v0out = new LinkedList<Edge>();
		LinkedList<Edge> v0in = new LinkedList<Edge>();
		LinkedList<Edge> v1inout = new LinkedList<Edge>();
		LinkedList<Edge> v1out = new LinkedList<Edge>();
		LinkedList<Edge> v1in = new LinkedList<Edge>();
		LinkedList<Edge> v2inout = new LinkedList<Edge>();
		LinkedList<Edge> v2out = new LinkedList<Edge>();
		LinkedList<Edge> v2in = new LinkedList<Edge>();
		Edge e = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		v0inout.add(e);
		v0out.add(e);
		v1inout.add(e.getReversedEdge());
		v1in.add(e.getReversedEdge());
		e = graph.createSubLink((DoubleSubNode) v1, (SuperNode) v2);
		v1inout.add(e);
		v1out.add(e);
		v2inout.add(e.getReversedEdge());
		v2in.add(e.getReversedEdge());
		e = graph.createLinkBack((SuperNode) v2, (DoubleSubNode) v1);
		v2inout.add(e);
		v2out.add(e);
		v1inout.add(e.getReversedEdge());
		v1in.add(e.getReversedEdge());
		e = graph.createLinkBack((SuperNode) v1, (DoubleSubNode) v1);
		v1inout.add(e);
		v1out.add(e);
		v1inout.add(e.getReversedEdge());
		v1in.add(e.getReversedEdge());

		checkIncidenceList(v0, null, null, EdgeDirection.INOUT, v0inout);
		checkIncidenceList(v0, null, null, EdgeDirection.OUT, v0out);
		checkIncidenceList(v0, null, null, EdgeDirection.IN, v0in);

		checkIncidenceList(v1, null, null, EdgeDirection.INOUT, v1inout);
		checkIncidenceList(v1, null, null, EdgeDirection.OUT, v1out);
		checkIncidenceList(v1, null, null, EdgeDirection.IN, v1in);

		checkIncidenceList(v2, null, null, EdgeDirection.INOUT, v2inout);
		checkIncidenceList(v2, null, null, EdgeDirection.OUT, v2out);
		checkIncidenceList(v2, null, null, EdgeDirection.IN, v2in);
	}

	/**
	 * Random test.
	 */
	@Test
	public void incidencesTestEdgeDirection3() {
		Vertex[] vertices = new Vertex[] { graph.createSubNode(),
				graph.createDoubleSubNode(), graph.createSuperNode() };
		LinkedList<LinkedList<Edge>> inout = new LinkedList<LinkedList<Edge>>();
		inout.add(new LinkedList<Edge>());
		inout.add(new LinkedList<Edge>());
		inout.add(new LinkedList<Edge>());
		LinkedList<LinkedList<Edge>> out = new LinkedList<LinkedList<Edge>>();
		out.add(new LinkedList<Edge>());
		out.add(new LinkedList<Edge>());
		out.add(new LinkedList<Edge>());
		LinkedList<LinkedList<Edge>> in = new LinkedList<LinkedList<Edge>>();
		in.add(new LinkedList<Edge>());
		in.add(new LinkedList<Edge>());
		in.add(new LinkedList<Edge>());

		for (int i = 0; i < 100; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			for (int j = 0; j < 30; j++) {
				int edge = rand.nextInt(3);
				int start = rand.nextInt(2);
				int end = rand.nextInt(2) + 1;
				Edge e = null;
				switch (edge) {
				case 0:
					e = graph.createLink((AbstractSuperNode) vertices[start],
							(SuperNode) vertices[end]);
					inout.get(start).add(e);
					out.get(start).add(e);
					inout.get(end).add(e.getReversedEdge());
					in.get(end).add(e.getReversedEdge());
					break;
				case 1:
					e = graph.createLinkBack((SuperNode) vertices[end],
							(AbstractSuperNode) vertices[start]);
					inout.get(end).add(e);
					out.get(end).add(e);
					inout.get(start).add(e.getReversedEdge());
					in.get(start).add(e.getReversedEdge());
					break;
				case 2:
					e = graph.createSubLink((DoubleSubNode) vertices[1],
							(SuperNode) vertices[end]);
					inout.get(1).add(e);
					out.get(1).add(e);
					inout.get(end).add(e.getReversedEdge());
					in.get(end).add(e.getReversedEdge());
					break;
				}
			}

			checkIncidenceList(vertices[0], null, null, EdgeDirection.INOUT,
					inout.get(0));
			checkIncidenceList(vertices[0], null, null, EdgeDirection.OUT, out
					.get(0));
			checkIncidenceList(vertices[0], null, null, EdgeDirection.IN, in
					.get(0));

			checkIncidenceList(vertices[1], null, null, EdgeDirection.INOUT,
					inout.get(1));
			checkIncidenceList(vertices[1], null, null, EdgeDirection.OUT, out
					.get(1));
			checkIncidenceList(vertices[1], null, null, EdgeDirection.IN, in
					.get(1));

			checkIncidenceList(vertices[2], null, null, EdgeDirection.INOUT,
					inout.get(2));
			checkIncidenceList(vertices[2], null, null, EdgeDirection.OUT, out
					.get(2));
			checkIncidenceList(vertices[2], null, null, EdgeDirection.IN, in
					.get(2));
		}
	}

	/**
	 * If the IN-edges are iterated the OUT-edges could not be deleted.
	 */
	@Test(expected = ConcurrentModificationException.class)
	public void inciencesTestEdgeDirectionFailFast0() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e0 = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		graph.createLinkBack((SuperNode) v1, (AbstractSuperNode) v0);
		graph.createLinkBack((SuperNode) v1, (AbstractSuperNode) v0);
		Iterator<Edge> it = v0.incidences(EdgeDirection.IN).iterator();
		e0.delete();
		it.hasNext();
		it.next();// TODO
	}

	/**
	 * If the IN-edges are iterated the OUT-edges could not be changed.
	 */
	@Test(expected = ConcurrentModificationException.class)
	public void inciencesTestEdgeDirectionFailFast1() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Edge e0 = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		graph.createLinkBack((SuperNode) v1, (AbstractSuperNode) v0);
		graph.createLinkBack((SuperNode) v1, (AbstractSuperNode) v0);
		Iterator<Edge> it = v0.incidences(EdgeDirection.IN).iterator();
		e0.setAlpha(v1);
		it.hasNext();
		it.next();// TODO
	}

	/**
	 * If the IN-edges are iterated a new OUT-edges could not be created.
	 */
	@Test(expected = ConcurrentModificationException.class)
	public void inciencesTestEdgeDirectionFailFast2() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		graph.createLinkBack((SuperNode) v1, (AbstractSuperNode) v0);
		graph.createLinkBack((SuperNode) v1, (AbstractSuperNode) v0);
		Iterator<Edge> it = v0.incidences(EdgeDirection.IN).iterator();
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		it.hasNext();
		it.next();// TODO
	}

	/**
	 * If the OUT-edges are iterated the IN-edges could not be deleted.
	 */
	@Test(expected = ConcurrentModificationException.class)
	public void inciencesTestEdgeDirectionFailFast3() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		Edge e0 = graph.createLinkBack((SuperNode) v1, (AbstractSuperNode) v0);
		graph.createLinkBack((SuperNode) v1, (AbstractSuperNode) v0);
		Iterator<Edge> it = v0.incidences(EdgeDirection.OUT).iterator();
		e0.delete();
		it.hasNext();
		it.next();// TODO
	}

	/**
	 * If the OUT-edges are iterated the IN-edges could not be changed.
	 */
	@Test(expected = ConcurrentModificationException.class)
	public void inciencesTestEdgeDirectionFailFast4() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		Edge e0 = graph.createLinkBack((SuperNode) v1, (AbstractSuperNode) v0);
		graph.createLinkBack((SuperNode) v1, (AbstractSuperNode) v0);
		Iterator<Edge> it = v0.incidences(EdgeDirection.OUT).iterator();
		e0.setAlpha(v0);
		it.hasNext();
		it.next();// TODO
	}

	/**
	 * If the OUT-edges are iterated a new IN-edges could not be created.
	 */
	@Test(expected = ConcurrentModificationException.class)
	public void inciencesTestEdgeDirectionFailFast5() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		graph.createLinkBack((SuperNode) v1, (AbstractSuperNode) v0);
		graph.createLinkBack((SuperNode) v1, (AbstractSuperNode) v0);
		Iterator<Edge> it = v0.incidences(EdgeDirection.OUT).iterator();
		graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		it.hasNext();
		it.next();// TODO
	}

	// tests of the method Iterable<Edge> incidences(EdgeClass eclass);

	/**
	 * Checks if a vertex has no incidences.
	 */
	@Test
	public void incidencesTestEdgeClass0() {
		EdgeClass[] ecs = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();
		checkIncidenceList(v0, ecs[0], null, null, new LinkedList<Edge>());
		checkIncidenceList(v0, ecs[1], null, null, new LinkedList<Edge>());
		checkIncidenceList(v0, ecs[2], null, null, new LinkedList<Edge>());
	}

	/**
	 * Checks if a vertex has only incident edges of type SubLink.
	 */
	@Test
	public void incidencesTestEdgeClass1() {
		EdgeClass[] ecs = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		LinkedList<Edge> v0link = new LinkedList<Edge>();
		LinkedList<Edge> v0sublink = new LinkedList<Edge>();
		LinkedList<Edge> v0linkback = new LinkedList<Edge>();
		LinkedList<Edge> v1link = new LinkedList<Edge>();
		LinkedList<Edge> v1sublink = new LinkedList<Edge>();
		LinkedList<Edge> v1linkback = new LinkedList<Edge>();
		Edge e = graph.createSubLink((DoubleSubNode) v0, (SuperNode) v1);
		v0link.add(e);
		v0sublink.add(e);
		v1link.add(e.getReversedEdge());
		v1sublink.add(e.getReversedEdge());

		checkIncidenceList(v0, ecs[0], null, null, v0link);
		checkIncidenceList(v0, ecs[1], null, null, v0sublink);
		checkIncidenceList(v0, ecs[2], null, null, v0linkback);

		checkIncidenceList(v1, ecs[0], null, null, v1link);
		checkIncidenceList(v1, ecs[1], null, null, v1sublink);
		checkIncidenceList(v1, ecs[2], null, null, v1linkback);
	}

	/**
	 * Checks incidences in a manually build graph.
	 */
	@Test
	public void incidencesTestEdgeClass2() {
		EdgeClass[] ecs = getEdgeClasses();
		Vertex v0 = graph.createSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Vertex v2 = graph.createSuperNode();
		LinkedList<Edge> v0link = new LinkedList<Edge>();
		LinkedList<Edge> v0sublink = new LinkedList<Edge>();
		LinkedList<Edge> v0linkback = new LinkedList<Edge>();
		LinkedList<Edge> v1link = new LinkedList<Edge>();
		LinkedList<Edge> v1sublink = new LinkedList<Edge>();
		LinkedList<Edge> v1linkback = new LinkedList<Edge>();
		LinkedList<Edge> v2link = new LinkedList<Edge>();
		LinkedList<Edge> v2sublink = new LinkedList<Edge>();
		LinkedList<Edge> v2linkback = new LinkedList<Edge>();
		Edge e = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		v0link.add(e);
		v1link.add(e.getReversedEdge());
		e = graph.createSubLink((DoubleSubNode) v1, (SuperNode) v2);
		v1link.add(e);
		v1sublink.add(e);
		v2link.add(e.getReversedEdge());
		v2sublink.add(e.getReversedEdge());
		e = graph.createLinkBack((SuperNode) v2, (DoubleSubNode) v1);
		v2linkback.add(e);
		v1linkback.add(e.getReversedEdge());
		e = graph.createLinkBack((SuperNode) v1, (DoubleSubNode) v1);
		v1linkback.add(e);
		v1linkback.add(e.getReversedEdge());

		checkIncidenceList(v0, ecs[0], null, null, v0link);
		checkIncidenceList(v0, ecs[1], null, null, v0sublink);
		checkIncidenceList(v0, ecs[2], null, null, v0linkback);

		checkIncidenceList(v1, ecs[0], null, null, v1link);
		checkIncidenceList(v1, ecs[1], null, null, v1sublink);
		checkIncidenceList(v1, ecs[2], null, null, v1linkback);

		checkIncidenceList(v2, ecs[0], null, null, v2link);
		checkIncidenceList(v2, ecs[1], null, null, v2sublink);
		checkIncidenceList(v2, ecs[2], null, null, v2linkback);
	}

	/**
	 * Random test.
	 */
	@Test
	public void incidencesTestEdgeClass3() {
		EdgeClass[] ecs = getEdgeClasses();
		Vertex[] vertices = new Vertex[] { graph.createSubNode(),
				graph.createDoubleSubNode(), graph.createSuperNode() };
		LinkedList<LinkedList<Edge>> link = new LinkedList<LinkedList<Edge>>();
		link.add(new LinkedList<Edge>());
		link.add(new LinkedList<Edge>());
		link.add(new LinkedList<Edge>());
		LinkedList<LinkedList<Edge>> sublink = new LinkedList<LinkedList<Edge>>();
		sublink.add(new LinkedList<Edge>());
		sublink.add(new LinkedList<Edge>());
		sublink.add(new LinkedList<Edge>());
		LinkedList<LinkedList<Edge>> linkback = new LinkedList<LinkedList<Edge>>();
		linkback.add(new LinkedList<Edge>());
		linkback.add(new LinkedList<Edge>());
		linkback.add(new LinkedList<Edge>());

		for (int i = 0; i < 100; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			for (int j = 0; j < 30; j++) {
				int edge = rand.nextInt(3);
				int start = rand.nextInt(2);
				int end = rand.nextInt(2) + 1;
				Edge e = null;
				switch (edge) {
				case 0:
					e = graph.createLink((AbstractSuperNode) vertices[start],
							(SuperNode) vertices[end]);
					link.get(start).add(e);
					link.get(end).add(e.getReversedEdge());
					break;
				case 1:
					e = graph.createLinkBack((SuperNode) vertices[end],
							(AbstractSuperNode) vertices[start]);
					linkback.get(end).add(e);
					linkback.get(start).add(e.getReversedEdge());
					break;
				case 2:
					e = graph.createSubLink((DoubleSubNode) vertices[1],
							(SuperNode) vertices[end]);
					link.get(1).add(e);
					sublink.get(1).add(e);
					link.get(end).add(e.getReversedEdge());
					sublink.get(end).add(e.getReversedEdge());
					break;
				}
			}

			checkIncidenceList(vertices[0], ecs[0], null, null, link.get(0));
			checkIncidenceList(vertices[0], ecs[1], null, null, sublink.get(0));
			checkIncidenceList(vertices[0], ecs[2], null, null, linkback.get(0));

			checkIncidenceList(vertices[1], ecs[0], null, null, link.get(1));
			checkIncidenceList(vertices[1], ecs[1], null, null, sublink.get(1));
			checkIncidenceList(vertices[1], ecs[2], null, null, linkback.get(1));

			checkIncidenceList(vertices[2], ecs[0], null, null, link.get(2));
			checkIncidenceList(vertices[2], ecs[1], null, null, sublink.get(2));
			checkIncidenceList(vertices[2], ecs[2], null, null, linkback.get(2));
		}
	}

	// tests of the method Iterable<Edge> incidences(Class<? extends Edge>
	// eclass);

	/**
	 * Checks if a vertex has no incidences.
	 */
	@Test
	public void incidencesTestClass0() {
		Vertex v0 = graph.createDoubleSubNode();
		checkIncidenceList(v0, null, Link.class, null, new LinkedList<Edge>());
		checkIncidenceList(v0, null, SubLink.class, null,
				new LinkedList<Edge>());
		checkIncidenceList(v0, null, LinkBack.class, null,
				new LinkedList<Edge>());
	}

	/**
	 * Checks if a vertex has only incident edges of type SubLink.
	 */
	@Test
	public void incidencesTestClass1() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		LinkedList<Edge> v0link = new LinkedList<Edge>();
		LinkedList<Edge> v0sublink = new LinkedList<Edge>();
		LinkedList<Edge> v0linkback = new LinkedList<Edge>();
		LinkedList<Edge> v1link = new LinkedList<Edge>();
		LinkedList<Edge> v1sublink = new LinkedList<Edge>();
		LinkedList<Edge> v1linkback = new LinkedList<Edge>();
		Edge e = graph.createSubLink((DoubleSubNode) v0, (SuperNode) v1);
		v0link.add(e);
		v0sublink.add(e);
		v1link.add(e.getReversedEdge());
		v1sublink.add(e.getReversedEdge());

		checkIncidenceList(v0, null, Link.class, null, v0link);
		checkIncidenceList(v0, null, SubLink.class, null, v0sublink);
		checkIncidenceList(v0, null, LinkBack.class, null, v0linkback);

		checkIncidenceList(v1, null, Link.class, null, v1link);
		checkIncidenceList(v1, null, SubLink.class, null, v1sublink);
		checkIncidenceList(v1, null, LinkBack.class, null, v1linkback);
	}

	/**
	 * Checks incidences in a manually build graph.
	 */
	@Test
	public void incidencesTestClass2() {
		Vertex v0 = graph.createSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Vertex v2 = graph.createSuperNode();
		LinkedList<Edge> v0link = new LinkedList<Edge>();
		LinkedList<Edge> v0sublink = new LinkedList<Edge>();
		LinkedList<Edge> v0linkback = new LinkedList<Edge>();
		LinkedList<Edge> v1link = new LinkedList<Edge>();
		LinkedList<Edge> v1sublink = new LinkedList<Edge>();
		LinkedList<Edge> v1linkback = new LinkedList<Edge>();
		LinkedList<Edge> v2link = new LinkedList<Edge>();
		LinkedList<Edge> v2sublink = new LinkedList<Edge>();
		LinkedList<Edge> v2linkback = new LinkedList<Edge>();
		Edge e = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		v0link.add(e);
		v1link.add(e.getReversedEdge());
		e = graph.createSubLink((DoubleSubNode) v1, (SuperNode) v2);
		v1link.add(e);
		v1sublink.add(e);
		v2link.add(e.getReversedEdge());
		v2sublink.add(e.getReversedEdge());
		e = graph.createLinkBack((SuperNode) v2, (DoubleSubNode) v1);
		v2linkback.add(e);
		v1linkback.add(e.getReversedEdge());
		e = graph.createLinkBack((SuperNode) v1, (DoubleSubNode) v1);
		v1linkback.add(e);
		v1linkback.add(e.getReversedEdge());

		checkIncidenceList(v0, null, Link.class, null, v0link);
		checkIncidenceList(v0, null, SubLink.class, null, v0sublink);
		checkIncidenceList(v0, null, LinkBack.class, null, v0linkback);

		checkIncidenceList(v1, null, Link.class, null, v1link);
		checkIncidenceList(v1, null, SubLink.class, null, v1sublink);
		checkIncidenceList(v1, null, LinkBack.class, null, v1linkback);

		checkIncidenceList(v2, null, Link.class, null, v2link);
		checkIncidenceList(v2, null, SubLink.class, null, v2sublink);
		checkIncidenceList(v2, null, LinkBack.class, null, v2linkback);
	}

	/**
	 * Random test.
	 */
	@Test
	public void incidencesTestClass3() {
		Vertex[] vertices = new Vertex[] { graph.createSubNode(),
				graph.createDoubleSubNode(), graph.createSuperNode() };
		LinkedList<LinkedList<Edge>> link = new LinkedList<LinkedList<Edge>>();
		link.add(new LinkedList<Edge>());
		link.add(new LinkedList<Edge>());
		link.add(new LinkedList<Edge>());
		LinkedList<LinkedList<Edge>> sublink = new LinkedList<LinkedList<Edge>>();
		sublink.add(new LinkedList<Edge>());
		sublink.add(new LinkedList<Edge>());
		sublink.add(new LinkedList<Edge>());
		LinkedList<LinkedList<Edge>> linkback = new LinkedList<LinkedList<Edge>>();
		linkback.add(new LinkedList<Edge>());
		linkback.add(new LinkedList<Edge>());
		linkback.add(new LinkedList<Edge>());

		for (int i = 0; i < 100; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			for (int j = 0; j < 30; j++) {
				int edge = rand.nextInt(3);
				int start = rand.nextInt(2);
				int end = rand.nextInt(2) + 1;
				Edge e = null;
				switch (edge) {
				case 0:
					e = graph.createLink((AbstractSuperNode) vertices[start],
							(SuperNode) vertices[end]);
					link.get(start).add(e);
					link.get(end).add(e.getReversedEdge());
					break;
				case 1:
					e = graph.createLinkBack((SuperNode) vertices[end],
							(AbstractSuperNode) vertices[start]);
					linkback.get(end).add(e);
					linkback.get(start).add(e.getReversedEdge());
					break;
				case 2:
					e = graph.createSubLink((DoubleSubNode) vertices[1],
							(SuperNode) vertices[end]);
					link.get(1).add(e);
					sublink.get(1).add(e);
					link.get(end).add(e.getReversedEdge());
					sublink.get(end).add(e.getReversedEdge());
					break;
				}
			}

			checkIncidenceList(vertices[0], null, Link.class, null, link.get(0));
			checkIncidenceList(vertices[0], null, SubLink.class, null, sublink
					.get(0));
			checkIncidenceList(vertices[0], null, LinkBack.class, null,
					linkback.get(0));

			checkIncidenceList(vertices[1], null, Link.class, null, link.get(1));
			checkIncidenceList(vertices[1], null, SubLink.class, null, sublink
					.get(1));
			checkIncidenceList(vertices[1], null, LinkBack.class, null,
					linkback.get(1));

			checkIncidenceList(vertices[2], null, Link.class, null, link.get(2));
			checkIncidenceList(vertices[2], null, SubLink.class, null, sublink
					.get(2));
			checkIncidenceList(vertices[2], null, LinkBack.class, null,
					linkback.get(2));
		}
	}

	// tests of the method Iterable<Edge> incidences(EdgeClass eclass,
	// EdgeDirection dir);

	/**
	 * Checks if a vertex has no incidences.
	 */
	@Test
	public void incidencesTestEdgeClassEdgeDirection0() {
		EdgeClass[] ecs = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();

		checkIncidenceList(v0, ecs[0], null, EdgeDirection.INOUT,
				new LinkedList<Edge>());
		checkIncidenceList(v0, ecs[0], null, EdgeDirection.OUT,
				new LinkedList<Edge>());
		checkIncidenceList(v0, ecs[0], null, EdgeDirection.IN,
				new LinkedList<Edge>());

		checkIncidenceList(v0, ecs[1], null, EdgeDirection.INOUT,
				new LinkedList<Edge>());
		checkIncidenceList(v0, ecs[1], null, EdgeDirection.OUT,
				new LinkedList<Edge>());
		checkIncidenceList(v0, ecs[1], null, EdgeDirection.IN,
				new LinkedList<Edge>());

		checkIncidenceList(v0, ecs[2], null, EdgeDirection.INOUT,
				new LinkedList<Edge>());
		checkIncidenceList(v0, ecs[2], null, EdgeDirection.OUT,
				new LinkedList<Edge>());
		checkIncidenceList(v0, ecs[2], null, EdgeDirection.IN,
				new LinkedList<Edge>());
	}

	/**
	 * Checks if a vertex has only incident edges of type SubLink.
	 */
	@Test
	public void incidencesTestEdgeClassEdgeDirection1() {
		EdgeClass[] ecs = getEdgeClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		LinkedList<Edge> v0linkInout = new LinkedList<Edge>();
		LinkedList<Edge> v0linkOut = new LinkedList<Edge>();
		LinkedList<Edge> v0linkIn = new LinkedList<Edge>();
		LinkedList<Edge> v0sublinkInout = new LinkedList<Edge>();
		LinkedList<Edge> v0sublinkOut = new LinkedList<Edge>();
		LinkedList<Edge> v0sublinkIn = new LinkedList<Edge>();
		LinkedList<Edge> v0linkbackInout = new LinkedList<Edge>();
		LinkedList<Edge> v0linkbackOut = new LinkedList<Edge>();
		LinkedList<Edge> v0linkbackIn = new LinkedList<Edge>();
		LinkedList<Edge> v1linkInout = new LinkedList<Edge>();
		LinkedList<Edge> v1linkOut = new LinkedList<Edge>();
		LinkedList<Edge> v1linkIn = new LinkedList<Edge>();
		LinkedList<Edge> v1sublinkInout = new LinkedList<Edge>();
		LinkedList<Edge> v1sublinkOut = new LinkedList<Edge>();
		LinkedList<Edge> v1sublinkIn = new LinkedList<Edge>();
		LinkedList<Edge> v1linkbackInout = new LinkedList<Edge>();
		LinkedList<Edge> v1linkbackOut = new LinkedList<Edge>();
		LinkedList<Edge> v1linkbackIn = new LinkedList<Edge>();
		Edge e = graph.createSubLink((DoubleSubNode) v0, (SuperNode) v1);
		v0linkInout.add(e);
		v0linkOut.add(e);
		v0sublinkInout.add(e);
		v0sublinkOut.add(e);
		v1linkInout.add(e.getReversedEdge());
		v1linkIn.add(e.getReversedEdge());
		v1sublinkInout.add(e.getReversedEdge());
		v1sublinkIn.add(e.getReversedEdge());

		checkIncidenceList(v0, ecs[0], null, EdgeDirection.INOUT, v0linkInout);
		checkIncidenceList(v0, ecs[0], null, EdgeDirection.OUT, v0linkOut);
		checkIncidenceList(v0, ecs[0], null, EdgeDirection.IN, v0linkIn);

		checkIncidenceList(v0, ecs[1], null, EdgeDirection.INOUT,
				v0sublinkInout);
		checkIncidenceList(v0, ecs[1], null, EdgeDirection.OUT, v0sublinkOut);
		checkIncidenceList(v0, ecs[1], null, EdgeDirection.IN, v0sublinkIn);

		checkIncidenceList(v0, ecs[2], null, EdgeDirection.INOUT,
				v0linkbackInout);
		checkIncidenceList(v0, ecs[2], null, EdgeDirection.OUT, v0linkbackOut);
		checkIncidenceList(v0, ecs[2], null, EdgeDirection.IN, v0linkbackIn);

		checkIncidenceList(v1, ecs[0], null, EdgeDirection.INOUT, v1linkInout);
		checkIncidenceList(v1, ecs[0], null, EdgeDirection.OUT, v1linkOut);
		checkIncidenceList(v1, ecs[0], null, EdgeDirection.IN, v1linkIn);

		checkIncidenceList(v1, ecs[1], null, EdgeDirection.INOUT,
				v1sublinkInout);
		checkIncidenceList(v1, ecs[1], null, EdgeDirection.OUT, v1sublinkOut);
		checkIncidenceList(v1, ecs[1], null, EdgeDirection.IN, v1sublinkIn);

		checkIncidenceList(v1, ecs[2], null, EdgeDirection.INOUT,
				v1linkbackInout);
		checkIncidenceList(v1, ecs[2], null, EdgeDirection.OUT, v1linkbackOut);
		checkIncidenceList(v1, ecs[2], null, EdgeDirection.IN, v1linkbackIn);
	}

	/**
	 * Checks incidences in a manually build graph.
	 */
	@Test
	public void incidencesTestEdgeClassEdgeDirection2() {
		EdgeClass[] ecs = getEdgeClasses();
		Vertex v0 = graph.createSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Vertex v2 = graph.createSuperNode();
		LinkedList<Edge> v0linkInout = new LinkedList<Edge>();
		LinkedList<Edge> v0linkOut = new LinkedList<Edge>();
		LinkedList<Edge> v0linkIn = new LinkedList<Edge>();
		LinkedList<Edge> v0sublinkInout = new LinkedList<Edge>();
		LinkedList<Edge> v0sublinkOut = new LinkedList<Edge>();
		LinkedList<Edge> v0sublinkIn = new LinkedList<Edge>();
		LinkedList<Edge> v0linkbackInout = new LinkedList<Edge>();
		LinkedList<Edge> v0linkbackOut = new LinkedList<Edge>();
		LinkedList<Edge> v0linkbackIn = new LinkedList<Edge>();
		LinkedList<Edge> v1linkInout = new LinkedList<Edge>();
		LinkedList<Edge> v1linkOut = new LinkedList<Edge>();
		LinkedList<Edge> v1linkIn = new LinkedList<Edge>();
		LinkedList<Edge> v1sublinkInout = new LinkedList<Edge>();
		LinkedList<Edge> v1sublinkOut = new LinkedList<Edge>();
		LinkedList<Edge> v1sublinkIn = new LinkedList<Edge>();
		LinkedList<Edge> v1linkbackInout = new LinkedList<Edge>();
		LinkedList<Edge> v1linkbackOut = new LinkedList<Edge>();
		LinkedList<Edge> v1linkbackIn = new LinkedList<Edge>();
		LinkedList<Edge> v2linkInout = new LinkedList<Edge>();
		LinkedList<Edge> v2linkOut = new LinkedList<Edge>();
		LinkedList<Edge> v2linkIn = new LinkedList<Edge>();
		LinkedList<Edge> v2sublinkInout = new LinkedList<Edge>();
		LinkedList<Edge> v2sublinkOut = new LinkedList<Edge>();
		LinkedList<Edge> v2sublinkIn = new LinkedList<Edge>();
		LinkedList<Edge> v2linkbackInout = new LinkedList<Edge>();
		LinkedList<Edge> v2linkbackOut = new LinkedList<Edge>();
		LinkedList<Edge> v2linkbackIn = new LinkedList<Edge>();
		Edge e = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		v0linkInout.add(e);
		v0linkOut.add(e);
		v1linkInout.add(e.getReversedEdge());
		v1linkIn.add(e.getReversedEdge());
		e = graph.createSubLink((DoubleSubNode) v1, (SuperNode) v2);
		v1linkInout.add(e);
		v1linkOut.add(e);
		v1sublinkInout.add(e);
		v1sublinkOut.add(e);
		v2linkInout.add(e.getReversedEdge());
		v2linkIn.add(e.getReversedEdge());
		v2sublinkInout.add(e.getReversedEdge());
		v2sublinkIn.add(e.getReversedEdge());
		e = graph.createLinkBack((SuperNode) v2, (DoubleSubNode) v1);
		v2linkbackInout.add(e);
		v2linkbackOut.add(e);
		v1linkbackInout.add(e.getReversedEdge());
		v1linkbackIn.add(e.getReversedEdge());
		e = graph.createLinkBack((SuperNode) v1, (DoubleSubNode) v1);
		v1linkbackInout.add(e);
		v1linkbackOut.add(e);
		v1linkbackInout.add(e.getReversedEdge());
		v1linkbackIn.add(e.getReversedEdge());

		checkIncidenceList(v0, ecs[0], null, EdgeDirection.INOUT, v0linkInout);
		checkIncidenceList(v0, ecs[0], null, EdgeDirection.OUT, v0linkOut);
		checkIncidenceList(v0, ecs[0], null, EdgeDirection.IN, v0linkIn);

		checkIncidenceList(v0, ecs[1], null, EdgeDirection.INOUT,
				v0sublinkInout);
		checkIncidenceList(v0, ecs[1], null, EdgeDirection.OUT, v0sublinkOut);
		checkIncidenceList(v0, ecs[1], null, EdgeDirection.IN, v0sublinkIn);

		checkIncidenceList(v0, ecs[2], null, EdgeDirection.INOUT,
				v0linkbackInout);
		checkIncidenceList(v0, ecs[2], null, EdgeDirection.OUT, v0linkbackOut);
		checkIncidenceList(v0, ecs[2], null, EdgeDirection.IN, v0linkbackIn);

		checkIncidenceList(v1, ecs[0], null, EdgeDirection.INOUT, v1linkInout);
		checkIncidenceList(v1, ecs[0], null, EdgeDirection.OUT, v1linkOut);
		checkIncidenceList(v1, ecs[0], null, EdgeDirection.IN, v1linkIn);

		checkIncidenceList(v1, ecs[1], null, EdgeDirection.INOUT,
				v1sublinkInout);
		checkIncidenceList(v1, ecs[1], null, EdgeDirection.OUT, v1sublinkOut);
		checkIncidenceList(v1, ecs[1], null, EdgeDirection.IN, v1sublinkIn);

		checkIncidenceList(v1, ecs[2], null, EdgeDirection.INOUT,
				v1linkbackInout);
		checkIncidenceList(v1, ecs[2], null, EdgeDirection.OUT, v1linkbackOut);
		checkIncidenceList(v1, ecs[2], null, EdgeDirection.IN, v1linkbackIn);

		checkIncidenceList(v2, ecs[0], null, EdgeDirection.INOUT, v2linkInout);
		checkIncidenceList(v2, ecs[0], null, EdgeDirection.OUT, v2linkOut);
		checkIncidenceList(v2, ecs[0], null, EdgeDirection.IN, v2linkIn);

		checkIncidenceList(v2, ecs[1], null, EdgeDirection.INOUT,
				v2sublinkInout);
		checkIncidenceList(v2, ecs[1], null, EdgeDirection.OUT, v2sublinkOut);
		checkIncidenceList(v2, ecs[1], null, EdgeDirection.IN, v2sublinkIn);

		checkIncidenceList(v2, ecs[2], null, EdgeDirection.INOUT,
				v2linkbackInout);
		checkIncidenceList(v2, ecs[2], null, EdgeDirection.OUT, v2linkbackOut);
		checkIncidenceList(v2, ecs[2], null, EdgeDirection.IN, v2linkbackIn);
	}

	/**
	 * Random test.
	 */
	@Test
	public void incidencesTestEdgeClassEdgeDirection3() {
		EdgeClass[] ecs = getEdgeClasses();
		Vertex[] vertices = new Vertex[] { graph.createSubNode(),
				graph.createDoubleSubNode(), graph.createSuperNode() };
		LinkedList<LinkedList<Edge>> linkinout = new LinkedList<LinkedList<Edge>>();
		linkinout.add(new LinkedList<Edge>());
		linkinout.add(new LinkedList<Edge>());
		linkinout.add(new LinkedList<Edge>());
		LinkedList<LinkedList<Edge>> linkout = new LinkedList<LinkedList<Edge>>();
		linkout.add(new LinkedList<Edge>());
		linkout.add(new LinkedList<Edge>());
		linkout.add(new LinkedList<Edge>());
		LinkedList<LinkedList<Edge>> linkin = new LinkedList<LinkedList<Edge>>();
		linkin.add(new LinkedList<Edge>());
		linkin.add(new LinkedList<Edge>());
		linkin.add(new LinkedList<Edge>());

		LinkedList<LinkedList<Edge>> sublinkinout = new LinkedList<LinkedList<Edge>>();
		sublinkinout.add(new LinkedList<Edge>());
		sublinkinout.add(new LinkedList<Edge>());
		sublinkinout.add(new LinkedList<Edge>());
		LinkedList<LinkedList<Edge>> sublinkout = new LinkedList<LinkedList<Edge>>();
		sublinkout.add(new LinkedList<Edge>());
		sublinkout.add(new LinkedList<Edge>());
		sublinkout.add(new LinkedList<Edge>());
		LinkedList<LinkedList<Edge>> sublinkin = new LinkedList<LinkedList<Edge>>();
		sublinkin.add(new LinkedList<Edge>());
		sublinkin.add(new LinkedList<Edge>());
		sublinkin.add(new LinkedList<Edge>());

		LinkedList<LinkedList<Edge>> linkbackinout = new LinkedList<LinkedList<Edge>>();
		linkbackinout.add(new LinkedList<Edge>());
		linkbackinout.add(new LinkedList<Edge>());
		linkbackinout.add(new LinkedList<Edge>());
		LinkedList<LinkedList<Edge>> linkbackout = new LinkedList<LinkedList<Edge>>();
		linkbackout.add(new LinkedList<Edge>());
		linkbackout.add(new LinkedList<Edge>());
		linkbackout.add(new LinkedList<Edge>());
		LinkedList<LinkedList<Edge>> linkbackin = new LinkedList<LinkedList<Edge>>();
		linkbackin.add(new LinkedList<Edge>());
		linkbackin.add(new LinkedList<Edge>());
		linkbackin.add(new LinkedList<Edge>());

		for (int i = 0; i < 100; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			for (int j = 0; j < 30; j++) {
				int edge = rand.nextInt(3);
				int start = rand.nextInt(2);
				int end = rand.nextInt(2) + 1;
				Edge e = null;
				switch (edge) {
				case 0:
					e = graph.createLink((AbstractSuperNode) vertices[start],
							(SuperNode) vertices[end]);
					linkinout.get(start).add(e);
					linkout.get(start).add(e);
					linkinout.get(end).add(e.getReversedEdge());
					linkin.get(end).add(e.getReversedEdge());
					break;
				case 1:
					e = graph.createLinkBack((SuperNode) vertices[end],
							(AbstractSuperNode) vertices[start]);
					linkbackinout.get(end).add(e);
					linkbackout.get(end).add(e);
					linkbackinout.get(start).add(e.getReversedEdge());
					linkbackin.get(start).add(e.getReversedEdge());
					break;
				case 2:
					e = graph.createSubLink((DoubleSubNode) vertices[1],
							(SuperNode) vertices[end]);
					linkinout.get(1).add(e);
					linkout.get(1).add(e);
					sublinkinout.get(1).add(e);
					sublinkout.get(1).add(e);
					linkinout.get(end).add(e.getReversedEdge());
					linkin.get(end).add(e.getReversedEdge());
					sublinkinout.get(end).add(e.getReversedEdge());
					sublinkin.get(end).add(e.getReversedEdge());
					break;
				}
			}

			checkIncidenceList(vertices[0], ecs[0], null, EdgeDirection.INOUT,
					linkinout.get(0));
			checkIncidenceList(vertices[0], ecs[0], null, EdgeDirection.OUT,
					linkout.get(0));
			checkIncidenceList(vertices[0], ecs[0], null, EdgeDirection.IN,
					linkin.get(0));

			checkIncidenceList(vertices[0], ecs[1], null, EdgeDirection.INOUT,
					sublinkinout.get(0));
			checkIncidenceList(vertices[0], ecs[1], null, EdgeDirection.OUT,
					sublinkout.get(0));
			checkIncidenceList(vertices[0], ecs[1], null, EdgeDirection.IN,
					sublinkin.get(0));

			checkIncidenceList(vertices[0], ecs[2], null, EdgeDirection.INOUT,
					linkbackinout.get(0));
			checkIncidenceList(vertices[0], ecs[2], null, EdgeDirection.OUT,
					linkbackout.get(0));
			checkIncidenceList(vertices[0], ecs[2], null, EdgeDirection.IN,
					linkbackin.get(0));

			checkIncidenceList(vertices[1], ecs[0], null, EdgeDirection.INOUT,
					linkinout.get(1));
			checkIncidenceList(vertices[1], ecs[0], null, EdgeDirection.OUT,
					linkout.get(1));
			checkIncidenceList(vertices[1], ecs[0], null, EdgeDirection.IN,
					linkin.get(1));

			checkIncidenceList(vertices[1], ecs[1], null, EdgeDirection.INOUT,
					sublinkinout.get(1));
			checkIncidenceList(vertices[1], ecs[1], null, EdgeDirection.OUT,
					sublinkout.get(1));
			checkIncidenceList(vertices[1], ecs[1], null, EdgeDirection.IN,
					sublinkin.get(1));

			checkIncidenceList(vertices[1], ecs[2], null, EdgeDirection.INOUT,
					linkbackinout.get(1));
			checkIncidenceList(vertices[1], ecs[2], null, EdgeDirection.OUT,
					linkbackout.get(1));
			checkIncidenceList(vertices[1], ecs[2], null, EdgeDirection.IN,
					linkbackin.get(1));

			checkIncidenceList(vertices[2], ecs[0], null, EdgeDirection.INOUT,
					linkinout.get(2));
			checkIncidenceList(vertices[2], ecs[0], null, EdgeDirection.OUT,
					linkout.get(2));
			checkIncidenceList(vertices[2], ecs[0], null, EdgeDirection.IN,
					linkin.get(2));

			checkIncidenceList(vertices[2], ecs[1], null, EdgeDirection.INOUT,
					sublinkinout.get(2));
			checkIncidenceList(vertices[2], ecs[1], null, EdgeDirection.OUT,
					sublinkout.get(2));
			checkIncidenceList(vertices[2], ecs[1], null, EdgeDirection.IN,
					sublinkin.get(2));

			checkIncidenceList(vertices[2], ecs[2], null, EdgeDirection.INOUT,
					linkbackinout.get(2));
			checkIncidenceList(vertices[2], ecs[2], null, EdgeDirection.OUT,
					linkbackout.get(2));
			checkIncidenceList(vertices[2], ecs[2], null, EdgeDirection.IN,
					linkbackin.get(2));
		}
	}

	// tests of the method Iterable<Edge> incidences(Class<? extends Edge>
	// eclass, EdgeDirection dir);

	/**
	 * Checks if a vertex has no incidences.
	 */
	@Test
	public void incidencesTestClassEdgeDirection0() {
		Vertex v0 = graph.createDoubleSubNode();

		checkIncidenceList(v0, null, Link.class, EdgeDirection.INOUT,
				new LinkedList<Edge>());
		checkIncidenceList(v0, null, Link.class, EdgeDirection.OUT,
				new LinkedList<Edge>());
		checkIncidenceList(v0, null, Link.class, EdgeDirection.IN,
				new LinkedList<Edge>());

		checkIncidenceList(v0, null, SubLink.class, EdgeDirection.INOUT,
				new LinkedList<Edge>());
		checkIncidenceList(v0, null, SubLink.class, EdgeDirection.OUT,
				new LinkedList<Edge>());
		checkIncidenceList(v0, null, SubLink.class, EdgeDirection.IN,
				new LinkedList<Edge>());

		checkIncidenceList(v0, null, LinkBack.class, EdgeDirection.INOUT,
				new LinkedList<Edge>());
		checkIncidenceList(v0, null, LinkBack.class, EdgeDirection.OUT,
				new LinkedList<Edge>());
		checkIncidenceList(v0, null, LinkBack.class, EdgeDirection.IN,
				new LinkedList<Edge>());
	}

	/**
	 * Checks if a vertex has only incident edges of type SubLink.
	 */
	@Test
	public void incidencesTestClassEdgeDirection1() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		LinkedList<Edge> v0linkInout = new LinkedList<Edge>();
		LinkedList<Edge> v0linkOut = new LinkedList<Edge>();
		LinkedList<Edge> v0linkIn = new LinkedList<Edge>();
		LinkedList<Edge> v0sublinkInout = new LinkedList<Edge>();
		LinkedList<Edge> v0sublinkOut = new LinkedList<Edge>();
		LinkedList<Edge> v0sublinkIn = new LinkedList<Edge>();
		LinkedList<Edge> v0linkbackInout = new LinkedList<Edge>();
		LinkedList<Edge> v0linkbackOut = new LinkedList<Edge>();
		LinkedList<Edge> v0linkbackIn = new LinkedList<Edge>();
		LinkedList<Edge> v1linkInout = new LinkedList<Edge>();
		LinkedList<Edge> v1linkOut = new LinkedList<Edge>();
		LinkedList<Edge> v1linkIn = new LinkedList<Edge>();
		LinkedList<Edge> v1sublinkInout = new LinkedList<Edge>();
		LinkedList<Edge> v1sublinkOut = new LinkedList<Edge>();
		LinkedList<Edge> v1sublinkIn = new LinkedList<Edge>();
		LinkedList<Edge> v1linkbackInout = new LinkedList<Edge>();
		LinkedList<Edge> v1linkbackOut = new LinkedList<Edge>();
		LinkedList<Edge> v1linkbackIn = new LinkedList<Edge>();
		Edge e = graph.createSubLink((DoubleSubNode) v0, (SuperNode) v1);
		v0linkInout.add(e);
		v0linkOut.add(e);
		v0sublinkInout.add(e);
		v0sublinkOut.add(e);
		v1linkInout.add(e.getReversedEdge());
		v1linkIn.add(e.getReversedEdge());
		v1sublinkInout.add(e.getReversedEdge());
		v1sublinkIn.add(e.getReversedEdge());

		checkIncidenceList(v0, null, Link.class, EdgeDirection.INOUT,
				v0linkInout);
		checkIncidenceList(v0, null, Link.class, EdgeDirection.OUT, v0linkOut);
		checkIncidenceList(v0, null, Link.class, EdgeDirection.IN, v0linkIn);

		checkIncidenceList(v0, null, SubLink.class, EdgeDirection.INOUT,
				v0sublinkInout);
		checkIncidenceList(v0, null, SubLink.class, EdgeDirection.OUT,
				v0sublinkOut);
		checkIncidenceList(v0, null, SubLink.class, EdgeDirection.IN,
				v0sublinkIn);

		checkIncidenceList(v0, null, LinkBack.class, EdgeDirection.INOUT,
				v0linkbackInout);
		checkIncidenceList(v0, null, LinkBack.class, EdgeDirection.OUT,
				v0linkbackOut);
		checkIncidenceList(v0, null, LinkBack.class, EdgeDirection.IN,
				v0linkbackIn);

		checkIncidenceList(v1, null, Link.class, EdgeDirection.INOUT,
				v1linkInout);
		checkIncidenceList(v1, null, Link.class, EdgeDirection.OUT, v1linkOut);
		checkIncidenceList(v1, null, Link.class, EdgeDirection.IN, v1linkIn);

		checkIncidenceList(v1, null, SubLink.class, EdgeDirection.INOUT,
				v1sublinkInout);
		checkIncidenceList(v1, null, SubLink.class, EdgeDirection.OUT,
				v1sublinkOut);
		checkIncidenceList(v1, null, SubLink.class, EdgeDirection.IN,
				v1sublinkIn);

		checkIncidenceList(v1, null, LinkBack.class, EdgeDirection.INOUT,
				v1linkbackInout);
		checkIncidenceList(v1, null, LinkBack.class, EdgeDirection.OUT,
				v1linkbackOut);
		checkIncidenceList(v1, null, LinkBack.class, EdgeDirection.IN,
				v1linkbackIn);
	}

	/**
	 * Checks incidences in a manually build graph.
	 */
	@Test
	public void incidencesTestClassEdgeDirection2() {
		Vertex v0 = graph.createSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		Vertex v2 = graph.createSuperNode();
		LinkedList<Edge> v0linkInout = new LinkedList<Edge>();
		LinkedList<Edge> v0linkOut = new LinkedList<Edge>();
		LinkedList<Edge> v0linkIn = new LinkedList<Edge>();
		LinkedList<Edge> v0sublinkInout = new LinkedList<Edge>();
		LinkedList<Edge> v0sublinkOut = new LinkedList<Edge>();
		LinkedList<Edge> v0sublinkIn = new LinkedList<Edge>();
		LinkedList<Edge> v0linkbackInout = new LinkedList<Edge>();
		LinkedList<Edge> v0linkbackOut = new LinkedList<Edge>();
		LinkedList<Edge> v0linkbackIn = new LinkedList<Edge>();
		LinkedList<Edge> v1linkInout = new LinkedList<Edge>();
		LinkedList<Edge> v1linkOut = new LinkedList<Edge>();
		LinkedList<Edge> v1linkIn = new LinkedList<Edge>();
		LinkedList<Edge> v1sublinkInout = new LinkedList<Edge>();
		LinkedList<Edge> v1sublinkOut = new LinkedList<Edge>();
		LinkedList<Edge> v1sublinkIn = new LinkedList<Edge>();
		LinkedList<Edge> v1linkbackInout = new LinkedList<Edge>();
		LinkedList<Edge> v1linkbackOut = new LinkedList<Edge>();
		LinkedList<Edge> v1linkbackIn = new LinkedList<Edge>();
		LinkedList<Edge> v2linkInout = new LinkedList<Edge>();
		LinkedList<Edge> v2linkOut = new LinkedList<Edge>();
		LinkedList<Edge> v2linkIn = new LinkedList<Edge>();
		LinkedList<Edge> v2sublinkInout = new LinkedList<Edge>();
		LinkedList<Edge> v2sublinkOut = new LinkedList<Edge>();
		LinkedList<Edge> v2sublinkIn = new LinkedList<Edge>();
		LinkedList<Edge> v2linkbackInout = new LinkedList<Edge>();
		LinkedList<Edge> v2linkbackOut = new LinkedList<Edge>();
		LinkedList<Edge> v2linkbackIn = new LinkedList<Edge>();
		Edge e = graph.createLink((AbstractSuperNode) v0, (SuperNode) v1);
		v0linkInout.add(e);
		v0linkOut.add(e);
		v1linkInout.add(e.getReversedEdge());
		v1linkIn.add(e.getReversedEdge());
		e = graph.createSubLink((DoubleSubNode) v1, (SuperNode) v2);
		v1linkInout.add(e);
		v1linkOut.add(e);
		v1sublinkInout.add(e);
		v1sublinkOut.add(e);
		v2linkInout.add(e.getReversedEdge());
		v2linkIn.add(e.getReversedEdge());
		v2sublinkInout.add(e.getReversedEdge());
		v2sublinkIn.add(e.getReversedEdge());
		e = graph.createLinkBack((SuperNode) v2, (DoubleSubNode) v1);
		v2linkbackInout.add(e);
		v2linkbackOut.add(e);
		v1linkbackInout.add(e.getReversedEdge());
		v1linkbackIn.add(e.getReversedEdge());
		e = graph.createLinkBack((SuperNode) v1, (DoubleSubNode) v1);
		v1linkbackInout.add(e);
		v1linkbackOut.add(e);
		v1linkbackInout.add(e.getReversedEdge());
		v1linkbackIn.add(e.getReversedEdge());

		checkIncidenceList(v0, null, Link.class, EdgeDirection.INOUT,
				v0linkInout);
		checkIncidenceList(v0, null, Link.class, EdgeDirection.OUT, v0linkOut);
		checkIncidenceList(v0, null, Link.class, EdgeDirection.IN, v0linkIn);

		checkIncidenceList(v0, null, SubLink.class, EdgeDirection.INOUT,
				v0sublinkInout);
		checkIncidenceList(v0, null, SubLink.class, EdgeDirection.OUT,
				v0sublinkOut);
		checkIncidenceList(v0, null, SubLink.class, EdgeDirection.IN,
				v0sublinkIn);

		checkIncidenceList(v0, null, LinkBack.class, EdgeDirection.INOUT,
				v0linkbackInout);
		checkIncidenceList(v0, null, LinkBack.class, EdgeDirection.OUT,
				v0linkbackOut);
		checkIncidenceList(v0, null, LinkBack.class, EdgeDirection.IN,
				v0linkbackIn);

		checkIncidenceList(v1, null, Link.class, EdgeDirection.INOUT,
				v1linkInout);
		checkIncidenceList(v1, null, Link.class, EdgeDirection.OUT, v1linkOut);
		checkIncidenceList(v1, null, Link.class, EdgeDirection.IN, v1linkIn);

		checkIncidenceList(v1, null, SubLink.class, EdgeDirection.INOUT,
				v1sublinkInout);
		checkIncidenceList(v1, null, SubLink.class, EdgeDirection.OUT,
				v1sublinkOut);
		checkIncidenceList(v1, null, SubLink.class, EdgeDirection.IN,
				v1sublinkIn);

		checkIncidenceList(v1, null, LinkBack.class, EdgeDirection.INOUT,
				v1linkbackInout);
		checkIncidenceList(v1, null, LinkBack.class, EdgeDirection.OUT,
				v1linkbackOut);
		checkIncidenceList(v1, null, LinkBack.class, EdgeDirection.IN,
				v1linkbackIn);

		checkIncidenceList(v2, null, Link.class, EdgeDirection.INOUT,
				v2linkInout);
		checkIncidenceList(v2, null, Link.class, EdgeDirection.OUT, v2linkOut);
		checkIncidenceList(v2, null, Link.class, EdgeDirection.IN, v2linkIn);

		checkIncidenceList(v2, null, SubLink.class, EdgeDirection.INOUT,
				v2sublinkInout);
		checkIncidenceList(v2, null, SubLink.class, EdgeDirection.OUT,
				v2sublinkOut);
		checkIncidenceList(v2, null, SubLink.class, EdgeDirection.IN,
				v2sublinkIn);

		checkIncidenceList(v2, null, LinkBack.class, EdgeDirection.INOUT,
				v2linkbackInout);
		checkIncidenceList(v2, null, LinkBack.class, EdgeDirection.OUT,
				v2linkbackOut);
		checkIncidenceList(v2, null, LinkBack.class, EdgeDirection.IN,
				v2linkbackIn);
	}

	/**
	 * Random test.
	 */
	@Test
	public void incidencesTestClassEdgeDirection3() {
		Vertex[] vertices = new Vertex[] { graph.createSubNode(),
				graph.createDoubleSubNode(), graph.createSuperNode() };
		LinkedList<LinkedList<Edge>> linkinout = new LinkedList<LinkedList<Edge>>();
		linkinout.add(new LinkedList<Edge>());
		linkinout.add(new LinkedList<Edge>());
		linkinout.add(new LinkedList<Edge>());
		LinkedList<LinkedList<Edge>> linkout = new LinkedList<LinkedList<Edge>>();
		linkout.add(new LinkedList<Edge>());
		linkout.add(new LinkedList<Edge>());
		linkout.add(new LinkedList<Edge>());
		LinkedList<LinkedList<Edge>> linkin = new LinkedList<LinkedList<Edge>>();
		linkin.add(new LinkedList<Edge>());
		linkin.add(new LinkedList<Edge>());
		linkin.add(new LinkedList<Edge>());

		LinkedList<LinkedList<Edge>> sublinkinout = new LinkedList<LinkedList<Edge>>();
		sublinkinout.add(new LinkedList<Edge>());
		sublinkinout.add(new LinkedList<Edge>());
		sublinkinout.add(new LinkedList<Edge>());
		LinkedList<LinkedList<Edge>> sublinkout = new LinkedList<LinkedList<Edge>>();
		sublinkout.add(new LinkedList<Edge>());
		sublinkout.add(new LinkedList<Edge>());
		sublinkout.add(new LinkedList<Edge>());
		LinkedList<LinkedList<Edge>> sublinkin = new LinkedList<LinkedList<Edge>>();
		sublinkin.add(new LinkedList<Edge>());
		sublinkin.add(new LinkedList<Edge>());
		sublinkin.add(new LinkedList<Edge>());

		LinkedList<LinkedList<Edge>> linkbackinout = new LinkedList<LinkedList<Edge>>();
		linkbackinout.add(new LinkedList<Edge>());
		linkbackinout.add(new LinkedList<Edge>());
		linkbackinout.add(new LinkedList<Edge>());
		LinkedList<LinkedList<Edge>> linkbackout = new LinkedList<LinkedList<Edge>>();
		linkbackout.add(new LinkedList<Edge>());
		linkbackout.add(new LinkedList<Edge>());
		linkbackout.add(new LinkedList<Edge>());
		LinkedList<LinkedList<Edge>> linkbackin = new LinkedList<LinkedList<Edge>>();
		linkbackin.add(new LinkedList<Edge>());
		linkbackin.add(new LinkedList<Edge>());
		linkbackin.add(new LinkedList<Edge>());

		for (int i = 0; i < 100; i++) {
			graph = VertexTestSchema.instance().createVertexTestGraph();
			for (int j = 0; j < 30; j++) {
				int edge = rand.nextInt(3);
				int start = rand.nextInt(2);
				int end = rand.nextInt(2) + 1;
				Edge e = null;
				switch (edge) {
				case 0:
					e = graph.createLink((AbstractSuperNode) vertices[start],
							(SuperNode) vertices[end]);
					linkinout.get(start).add(e);
					linkout.get(start).add(e);
					linkinout.get(end).add(e.getReversedEdge());
					linkin.get(end).add(e.getReversedEdge());
					break;
				case 1:
					e = graph.createLinkBack((SuperNode) vertices[end],
							(AbstractSuperNode) vertices[start]);
					linkbackinout.get(end).add(e);
					linkbackout.get(end).add(e);
					linkbackinout.get(start).add(e.getReversedEdge());
					linkbackin.get(start).add(e.getReversedEdge());
					break;
				case 2:
					e = graph.createSubLink((DoubleSubNode) vertices[1],
							(SuperNode) vertices[end]);
					linkinout.get(1).add(e);
					linkout.get(1).add(e);
					sublinkinout.get(1).add(e);
					sublinkout.get(1).add(e);
					linkinout.get(end).add(e.getReversedEdge());
					linkin.get(end).add(e.getReversedEdge());
					sublinkinout.get(end).add(e.getReversedEdge());
					sublinkin.get(end).add(e.getReversedEdge());
					break;
				}
			}

			checkIncidenceList(vertices[0], null, Link.class,
					EdgeDirection.INOUT, linkinout.get(0));
			checkIncidenceList(vertices[0], null, Link.class,
					EdgeDirection.OUT, linkout.get(0));
			checkIncidenceList(vertices[0], null, Link.class, EdgeDirection.IN,
					linkin.get(0));

			checkIncidenceList(vertices[0], null, SubLink.class,
					EdgeDirection.INOUT, sublinkinout.get(0));
			checkIncidenceList(vertices[0], null, SubLink.class,
					EdgeDirection.OUT, sublinkout.get(0));
			checkIncidenceList(vertices[0], null, SubLink.class,
					EdgeDirection.IN, sublinkin.get(0));

			checkIncidenceList(vertices[0], null, LinkBack.class,
					EdgeDirection.INOUT, linkbackinout.get(0));
			checkIncidenceList(vertices[0], null, LinkBack.class,
					EdgeDirection.OUT, linkbackout.get(0));
			checkIncidenceList(vertices[0], null, LinkBack.class,
					EdgeDirection.IN, linkbackin.get(0));

			checkIncidenceList(vertices[1], null, Link.class,
					EdgeDirection.INOUT, linkinout.get(1));
			checkIncidenceList(vertices[1], null, Link.class,
					EdgeDirection.OUT, linkout.get(1));
			checkIncidenceList(vertices[1], null, Link.class, EdgeDirection.IN,
					linkin.get(1));

			checkIncidenceList(vertices[1], null, SubLink.class,
					EdgeDirection.INOUT, sublinkinout.get(1));
			checkIncidenceList(vertices[1], null, SubLink.class,
					EdgeDirection.OUT, sublinkout.get(1));
			checkIncidenceList(vertices[1], null, SubLink.class,
					EdgeDirection.IN, sublinkin.get(1));

			checkIncidenceList(vertices[1], null, LinkBack.class,
					EdgeDirection.INOUT, linkbackinout.get(1));
			checkIncidenceList(vertices[1], null, LinkBack.class,
					EdgeDirection.OUT, linkbackout.get(1));
			checkIncidenceList(vertices[1], null, LinkBack.class,
					EdgeDirection.IN, linkbackin.get(1));

			checkIncidenceList(vertices[2], null, Link.class,
					EdgeDirection.INOUT, linkinout.get(2));
			checkIncidenceList(vertices[2], null, Link.class,
					EdgeDirection.OUT, linkout.get(2));
			checkIncidenceList(vertices[2], null, Link.class, EdgeDirection.IN,
					linkin.get(2));

			checkIncidenceList(vertices[2], null, SubLink.class,
					EdgeDirection.INOUT, sublinkinout.get(2));
			checkIncidenceList(vertices[2], null, SubLink.class,
					EdgeDirection.OUT, sublinkout.get(2));
			checkIncidenceList(vertices[2], null, SubLink.class,
					EdgeDirection.IN, sublinkin.get(2));

			checkIncidenceList(vertices[2], null, LinkBack.class,
					EdgeDirection.INOUT, linkbackinout.get(2));
			checkIncidenceList(vertices[2], null, LinkBack.class,
					EdgeDirection.OUT, linkbackout.get(2));
			checkIncidenceList(vertices[2], null, LinkBack.class,
					EdgeDirection.IN, linkbackin.get(2));
		}
	}

	// tests of the method boolean isValidAlpha(Edge edge);

	/**
	 * Checks some cases for true and false considering heredity.
	 */
	@Test
	public void isValidAlphaTest0() {
		Vertex v0 = graph.createSubNode();
		Vertex v1 = graph.createSuperNode();
		Vertex v2 = graph.createDoubleSubNode();
		Edge e0 = graph.createLink((AbstractSuperNode) v2, (SuperNode) v2);
		Edge e1 = graph.createSubLink((DoubleSubNode) v2, (SuperNode) v2);
		assertTrue(v0.isValidAlpha(e0));
		assertFalse(v1.isValidAlpha(e0));
		assertTrue(v2.isValidAlpha(e0));
		assertFalse(v0.isValidAlpha(e1));
		assertFalse(v1.isValidAlpha(e1));
		assertTrue(v2.isValidAlpha(e1));
	}

	// tests of the method boolean isValidOmega(Edge edge);

	/**
	 * Checks some cases for true and false.
	 */
	@Test
	public void isValidOmegaTest0() {
		Vertex v0 = graph.createSubNode();
		Vertex v1 = graph.createSuperNode();
		assertTrue(v0.isValid());
		assertTrue(v1.isValid());
		v0.delete();
		assertFalse(v0.isValid());
		assertTrue(v1.isValid());
	}

	/*
	 * Test of the Interface GraphElement
	 */

	// tests of the method Graph getGraph();
	/**
	 * Checks some cases for true and false.
	 */
	@Test
	public void getGraphTest() {
		VertexTestGraph anotherGraph = ((VertexTestSchema) graph.getSchema())
				.createVertexTestGraph();
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = anotherGraph.createDoubleSubNode();
		Vertex v2 = graph.createDoubleSubNode();
		assertEquals(graph, v0.getGraph());
		assertEquals(anotherGraph, v1.getGraph());
		assertEquals(graph, v2.getGraph());
	}

	// tests of the method void graphModified();

	/**
	 * Tests if the graphversion is increased if the method is called.
	 */
	@Test
	public void graphModifiedTest0() {
		Vertex v = graph.createDoubleSubNode();
		long graphversion = graph.getGraphVersion();
		v.graphModified();
		assertEquals(++graphversion, graph.getGraphVersion());
	}

	/**
	 * Tests if the graphversion is increased by creating a new vertex.
	 */
	@Test
	public void graphModifiedTest1() {
		long graphversion = graph.getGraphVersion();
		graph.createDoubleSubNode();
		assertEquals(++graphversion, graph.getGraphVersion());
	}

	/**
	 * Tests if the graphversion is increased by deleting a vertex.
	 */
	@Test
	public void graphModifiedTest2() {
		Vertex v = graph.createDoubleSubNode();
		long graphversion = graph.getGraphVersion();
		v.delete();
		assertEquals(++graphversion, graph.getGraphVersion());
	}

	/**
	 * Tests if the graphversion is increased by changing the attributes of a
	 * vertex.
	 */
	@Test
	public void graphModifiedTest3() {
		Vertex v = graph.createDoubleSubNode();
		long graphversion = graph.getGraphVersion();
		((DoubleSubNode) v).setNumber(4);
		assertEquals(++graphversion, graph.getGraphVersion());
	}

	/*
	 * Test of the Interface AttributedElement
	 */

	// tests of the method AttributedElementClass getAttributedElementClass();
	/**
	 * Some test cases for getAttributedElementClass
	 */
	@Test
	public void getAttributedElementClassTest() {
		VertexClass[] vertices = getVertexClasses();
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createSubNode();
		Vertex v2 = graph.createSuperNode();
		assertEquals(vertices[3], v0.getAttributedElementClass());
		assertEquals(vertices[1], v1.getAttributedElementClass());
		assertEquals(vertices[2], v2.getAttributedElementClass());
	}

	// tests of the method AttributedElementClass getAttributedElementClass();

	/**
	 * Some test cases for getM1Class
	 */
	@Test
	public void getM1ClassTest() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createSubNode();
		Vertex v2 = graph.createSuperNode();
		assertEquals(DoubleSubNode.class, v0.getM1Class());
		assertEquals(SubNode.class, v1.getM1Class());
		assertEquals(SuperNode.class, v2.getM1Class());
	}

	// tests of the method GraphClass getGraphClass();

	/**
	 * Some test cases for getGraphClass
	 */
	@Test
	public void getGraphClassTest() {
		VertexTestGraph anotherGraph = ((VertexTestSchema) graph.getSchema())
				.createVertexTestGraph();
		GraphClass gc = ((VertexTestSchema) graph.getSchema())
				.getGraphClasses().get(new QualifiedName("VertexTestGraph"));
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = anotherGraph.createDoubleSubNode();
		Vertex v2 = graph.createDoubleSubNode();
		assertEquals(gc, v0.getGraphClass());
		assertEquals(gc, v1.getGraphClass());
		assertEquals(gc, v2.getGraphClass());
	}

	// tests of the methods
	// void writeAttributeValues(GraphIO io) throws IOException,
	// GraphIOException;
	// and
	// void readAttributeValues(GraphIO io) throws GraphIOException;

	/**
	 * Test with null values.
	 */
	@Test
	public void writeReadAttributeValues0() throws GraphIOException,
			IOException {
		DoubleSubNode v0 = graph.createDoubleSubNode();
		// test of writeAttributeValues
		GraphIO.saveGraphToFile("test.tg", graph, null);
		LineNumberReader reader = new LineNumberReader(
				new FileReader("test.tg"));
		String line = "";
		String[] parts = null;
		while ((line = reader.readLine()) != null) {
			if (line.length() > 0) {
				line = line.substring(0, line.length() - 1);
			}
			parts = line.split(" ");
			if (parts[0].equals(((Integer) v0.getId()).toString())) {
				break;
			}
		}
		assertEquals("\\null", parts[3]);
		assertEquals("\\null", parts[4]);
		assertEquals("0", parts[5]);
		// test of readAttributeValues
		VertexTestGraph loadedgraph = (VertexTestGraph) GraphIO
				.loadGraphFromFile("test.tg", null);
		DoubleSubNode loadedv0 = loadedgraph.getFirstDoubleSubNode();
		assertEquals(v0.getName(), loadedv0.getName());
		assertEquals(v0.getNumber(), loadedv0.getNumber());
		assertEquals(v0.getNodeMap(), loadedv0.getNodeMap());
		// delete created file
		System.gc();
		reader.close();
		File f = new File("test.tg");
		f.delete();
	}

	/**
	 * Test with values.
	 */
	@Test
	public void writeReadAttributeValues1() throws GraphIOException,
			IOException {
		DoubleSubNode v0 = graph.createDoubleSubNode();
		v0.setName("NameVonV0");
		v0.setNumber(17);
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		map.put(1, "First");
		map.put(2, "Second");
		v0.setNodeMap(map);
		// test of writeAttributeValues
		GraphIO.saveGraphToFile("test.tg", graph, null);
		LineNumberReader reader = new LineNumberReader(
				new FileReader("test.tg"));
		String line = "";
		String[] parts = null;
		while ((line = reader.readLine()) != null) {
			if (line.length() > 0) {
				line = line.substring(0, line.length() - 1);
			}
			parts = line.split(" ");
			if (parts[0].equals(((Integer) v0.getId()).toString())) {
				break;
			}
		}
		assertEquals("\"NameVonV0\"", parts[3]);
		String mapString = map.toString();
		mapString = mapString.replace("=", " - \"");
		mapString = mapString.replace(", ", "\" ");
		mapString = mapString.replace("}", "\"}");
		String[] mapParts = mapString.split(" ");
		int i = 0;
		while (i < mapParts.length) {
			assertEquals(mapParts[i], parts[i + 4]);
			i++;
		}
		assertEquals("17", parts[i + 4]);
		// test of readAttributeValues
		VertexTestGraph loadedgraph = (VertexTestGraph) GraphIO
				.loadGraphFromFile("test.tg", null);
		DoubleSubNode loadedv0 = loadedgraph.getFirstDoubleSubNode();
		assertEquals(v0.getName(), loadedv0.getName());
		assertEquals(v0.getNumber(), loadedv0.getNumber());
		assertEquals(v0.getNodeMap(), loadedv0.getNodeMap());
		// delete created file
		System.gc();
		reader.close();
		File f = new File("test.tg");
		f.delete();
	}

	// tests of the method Object getAttribute(String name) throws
	// NoSuchFieldException;

	/**
	 * Tests if the value of the correct attribute is returned.
	 */
	@Test
	public void getAttributeTest0() throws NoSuchFieldException {
		DoubleSubNode v = graph.createDoubleSubNode();
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		v.setNodeMap(map);
		v.setName("test");
		v.setNumber(4);
		assertEquals(map, v.getAttribute("nodeMap"));
		assertEquals("test", v.getAttribute("name"));
		assertEquals(4, v.getAttribute("number"));
	}

	/**
	 * Tests if an exception is thrown if you want to get an attribute which
	 * doesn't exist.
	 */
	@Test(expected = NoSuchFieldException.class)
	public void getAttributeTest1() throws NoSuchFieldException {
		DoubleSubNode v = graph.createDoubleSubNode();
		v.getAttribute("cd");
	}

	/**
	 * Tests if an exception is thrown if you want to get an attribute with an
	 * empty name.
	 */
	@Test(expected = NoSuchFieldException.class)
	public void getAttributeTest2() throws NoSuchFieldException {
		DoubleSubNode v = graph.createDoubleSubNode();
		v.getAttribute("");
	}

	// tests of the method void setAttribute(String name, Object data) throws
	// NoSuchFieldException;

	/**
	 * Tests if an existing attribute is correct set.
	 */
	@Test
	public void setAttributeTest0() throws NoSuchFieldException {
		DoubleSubNode v = graph.createDoubleSubNode();
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		v.setAttribute("nodeMap", map);
		v.setAttribute("name", "test");
		v.setAttribute("number", 4);
		assertEquals(map, v.getAttribute("nodeMap"));
		assertEquals("test", v.getAttribute("name"));
		assertEquals(4, v.getAttribute("number"));
	}

	/**
	 * Tests if an existing attribute is set to null.
	 */
	@Test
	public void setAttributeTest1() throws NoSuchFieldException {
		DoubleSubNode v = graph.createDoubleSubNode();
		v.setAttribute("nodeMap", null);
		v.setAttribute("name", null);
		assertEquals(null, v.getAttribute("nodeMap"));
		assertEquals(null, v.getAttribute("name"));
	}

	/**
	 * Tests if an exception is thrown if you want to get an attribute which
	 * doesn't exist.
	 */
	@Test(expected = NoSuchFieldException.class)
	public void setAttributeTest2() throws NoSuchFieldException {
		DoubleSubNode v = graph.createDoubleSubNode();
		v.setAttribute("cd", "a");
	}

	/**
	 * Tests if an exception is thrown if you want to get an attribute with an
	 * empty name.
	 */
	@Test(expected = NoSuchFieldException.class)
	public void setAttributeTest3() throws NoSuchFieldException {
		DoubleSubNode v = graph.createDoubleSubNode();
		v.setAttribute("", "a");
	}

	// tests of the method Schema getSchema();

	/**
	 * Some tests.
	 */
	@Test
	public void getSchemaTest() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createSubNode();
		Vertex v2 = graph.createSuperNode();
		Schema schema = graph.getSchema();
		assertEquals(schema, v0.getSchema());
		assertEquals(schema, v1.getSchema());
		assertEquals(schema, v2.getSchema());
	}

	/*
	 * Test of the Interface Comparable
	 */

	// tests of the method int compareTo(AttributedElement a);
	/**
	 * Test if a vertex is equal to itself.
	 */
	@Test
	public void compareToTest0() {
		Vertex v0 = graph.createDoubleSubNode();
		assertEquals(0, v0.compareTo(v0));
	}

	/**
	 * Test if a vertex is smaller than another.
	 */
	@Test
	public void compareToTest1() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		assertTrue(v0.compareTo(v1) < 0);
	}

	/**
	 * Test if a vertex is greater than another.
	 */
	@Test
	public void compareToTest2() {
		Vertex v0 = graph.createDoubleSubNode();
		Vertex v1 = graph.createDoubleSubNode();
		assertTrue(v1.compareTo(v0) > 0);
	}

	/*
	 * Test of the generated methods
	 */

	// tests of the methods setName and getName
	@Test
	public void setGetNameTest0() {
		DoubleSubNode v0 = graph.createDoubleSubNode();
		v0.setName("aName");
		assertEquals("aName", v0.getName());
		v0.setName("bName");
		assertEquals("bName", v0.getName());
		v0.setName("cName");
		assertEquals("cName", v0.getName());
	}

	// tests of the methods setNumber and getNumber
	@Test
	public void setGetNumberTest0() {
		DoubleSubNode v0 = graph.createDoubleSubNode();
		v0.setNumber(0);
		assertEquals(0, v0.getNumber());
		v0.setNumber(1);
		assertEquals(1, v0.getNumber());
		v0.setNumber(-1);
		assertEquals(-1, v0.getNumber());
	}

	// tests of the methods setNodeMap and getNodeMap
	@Test
	public void setGetNodeMapTest0() {
		DoubleSubNode v0 = graph.createDoubleSubNode();
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		v0.setNodeMap(map);
		assertEquals(map, v0.getNodeMap());
		map.put(1, "first");
		v0.setNodeMap(map);
		assertEquals(map, v0.getNodeMap());
		map.put(2, "second");
		v0.setNodeMap(map);
		assertEquals(map, v0.getNodeMap());
	}

	// tests of the method addSource
	@Test
	public void addSourceTest0() {
		DoubleSubNode v0 = graph.createDoubleSubNode();
		// v0 -->{Link} v0
		Link e0 = v0.addSource(v0);
		assertEquals(v0, e0.getAlpha());
		assertEquals(v0, e0.getOmega());
		// v1 -->{Link} v0
		DoubleSubNode v1 = graph.createDoubleSubNode();
		Link e1 = v0.addSource(v1);
		assertEquals(v1, e1.getAlpha());
		assertEquals(v0, e1.getOmega());
		// v0 -->{Link} v1
		Link e2 = v1.addSource(v0);
		assertEquals(v0, e2.getAlpha());
		assertEquals(v1, e2.getOmega());
		// checks if the edges are in the edge-list of the graph
		int i = 0;
		for (Edge e : graph.edges()) {
			switch (i) {
			case 0:
				assertEquals(e0, e);
				i++;
				break;
			case 1:
				assertEquals(e1, e);
				i++;
				break;
			case 2:
				assertEquals(e2, e);
				i++;
				break;
			default:
				fail("No further edges expected!");
			}
		}
		// checks if the edges are in the incidenceList of both vertices
		i = 0;
		for (Edge e : v0.incidences()) {
			switch (i) {
			case 0:
				assertEquals(e0, e);
				i++;
				break;
			case 1:
				assertEquals(e0.getReversedEdge(), e);
				i++;
				break;
			case 2:
				assertEquals(e1.getReversedEdge(), e);
				i++;
				break;
			case 3:
				assertEquals(e2, e);
				i++;
				break;
			default:
				fail("No further edges expected!");
			}
		}
		i = 0;
		for (Edge e : v1.incidences()) {
			switch (i) {
			case 0:
				assertEquals(e1, e);
				i++;
				break;
			case 1:
				assertEquals(e2.getReversedEdge(), e);
				i++;
				break;
			default:
				fail("No further edges expected!");
			}
		}
	}

	// tests of the method removeSource
	@Test
	public void removeSourceTest0() {
		//TODO
	}

	// tests of the method addSourceb
	@Test
	public void addSourcebTest0() {
		DoubleSubNode v0 = graph.createDoubleSubNode();
		// v0 -->{LinkBack} v0
		LinkBack e0 = v0.addSourceb(v0);
		assertEquals(v0, e0.getAlpha());
		assertEquals(v0, e0.getOmega());
		// v1 -->{LinkBack} v0
		DoubleSubNode v1 = graph.createDoubleSubNode();
		LinkBack e1 = v0.addSourceb(v1);
		assertEquals(v1, e1.getAlpha());
		assertEquals(v0, e1.getOmega());
		// v0 -->{LinkBack} v1
		LinkBack e2 = v1.addSourceb(v0);
		assertEquals(v0, e2.getAlpha());
		assertEquals(v1, e2.getOmega());
		// checks if the edges are in the edge-list of the graph
		int i = 0;
		for (Edge e : graph.edges()) {
			switch (i) {
			case 0:
				assertEquals(e0, e);
				i++;
				break;
			case 1:
				assertEquals(e1, e);
				i++;
				break;
			case 2:
				assertEquals(e2, e);
				i++;
				break;
			default:
				fail("No further edges expected!");
			}
		}
		// checks if the edges are in the incidenceList of both vertices
		i = 0;
		for (Edge e : v0.incidences()) {
			switch (i) {
			case 0:
				assertEquals(e0, e);
				i++;
				break;
			case 1:
				assertEquals(e0.getReversedEdge(), e);
				i++;
				break;
			case 2:
				assertEquals(e1.getReversedEdge(), e);
				i++;
				break;
			case 3:
				assertEquals(e2, e);
				i++;
				break;
			default:
				fail("No further edges expected!");
			}
		}
		i = 0;
		for (Edge e : v1.incidences()) {
			switch (i) {
			case 0:
				assertEquals(e1, e);
				i++;
				break;
			case 1:
				assertEquals(e2.getReversedEdge(), e);
				i++;
				break;
			default:
				fail("No further edges expected!");
			}
		}
	}

	// tests of the method addSourcec
	@Test
	public void addSourcecTest0() {
		DoubleSubNode v0 = graph.createDoubleSubNode();
		// v0 -->{SubLink} v0
		SubLink e0 = v0.addSourcec(v0);
		assertEquals(v0, e0.getAlpha());
		assertEquals(v0, e0.getOmega());
		// v1 -->{SubLink} v0
		DoubleSubNode v1 = graph.createDoubleSubNode();
		SubLink e1 = v0.addSourcec(v1);
		assertEquals(v1, e1.getAlpha());
		assertEquals(v0, e1.getOmega());
		// v0 -->{SubLink} v1
		SubLink e2 = v1.addSourcec(v0);
		assertEquals(v0, e2.getAlpha());
		assertEquals(v1, e2.getOmega());
		// checks if the edges are in the edge-list of the graph
		int i = 0;
		for (Edge e : graph.edges()) {
			switch (i) {
			case 0:
				assertEquals(e0, e);
				i++;
				break;
			case 1:
				assertEquals(e1, e);
				i++;
				break;
			case 2:
				assertEquals(e2, e);
				i++;
				break;
			default:
				fail("No further edges expected!");
			}
		}
		// checks if the edges are in the incidenceList of both vertices
		i = 0;
		for (Edge e : v0.incidences()) {
			switch (i) {
			case 0:
				assertEquals(e0, e);
				i++;
				break;
			case 1:
				assertEquals(e0.getReversedEdge(), e);
				i++;
				break;
			case 2:
				assertEquals(e1.getReversedEdge(), e);
				i++;
				break;
			case 3:
				assertEquals(e2, e);
				i++;
				break;
			default:
				fail("No further edges expected!");
			}
		}
		i = 0;
		for (Edge e : v1.incidences()) {
			switch (i) {
			case 0:
				assertEquals(e1, e);
				i++;
				break;
			case 1:
				assertEquals(e2.getReversedEdge(), e);
				i++;
				break;
			default:
				fail("No further edges expected!");
			}
		}
	}

	// tests of the method addTarget
	@Test
	public void addTargetTest0() {
		DoubleSubNode v0 = graph.createDoubleSubNode();
		// v0 -->{Link} v0
		Link e0 = v0.addTarget(v0);
		assertEquals(v0, e0.getAlpha());
		assertEquals(v0, e0.getOmega());
		// v0 -->{Link} v1
		DoubleSubNode v1 = graph.createDoubleSubNode();
		Link e1 = v0.addTarget(v1);
		assertEquals(v0, e1.getAlpha());
		assertEquals(v1, e1.getOmega());
		// v1 -->{Link} v0
		Link e2 = v1.addTarget(v0);
		assertEquals(v1, e2.getAlpha());
		assertEquals(v0, e2.getOmega());
		// checks if the edges are in the edge-list of the graph
		int i = 0;
		for (Edge e : graph.edges()) {
			switch (i) {
			case 0:
				assertEquals(e0, e);
				i++;
				break;
			case 1:
				assertEquals(e1, e);
				i++;
				break;
			case 2:
				assertEquals(e2, e);
				i++;
				break;
			default:
				fail("No further edges expected!");
			}
		}
		// checks if the edges are in the incidenceList of both vertices
		i = 0;
		for (Edge e : v0.incidences()) {
			switch (i) {
			case 0:
				assertEquals(e0, e);
				i++;
				break;
			case 1:
				assertEquals(e0.getReversedEdge(), e);
				i++;
				break;
			case 2:
				assertEquals(e1, e);
				i++;
				break;
			case 3:
				assertEquals(e2.getReversedEdge(), e);
				i++;
				break;
			default:
				fail("No further edges expected!");
			}
		}
		i = 0;
		for (Edge e : v1.incidences()) {
			switch (i) {
			case 0:
				assertEquals(e1.getReversedEdge(), e);
				i++;
				break;
			case 1:
				assertEquals(e2, e);
				i++;
				break;
			default:
				fail("No further edges expected!");
			}
		}
	}

	// tests of the method addTargetb
	@Test
	public void addTargetbTest0() {
		DoubleSubNode v0 = graph.createDoubleSubNode();
		// v0 -->{LinkBack} v0
		LinkBack e0 = v0.addTargetb(v0);
		assertEquals(v0, e0.getAlpha());
		assertEquals(v0, e0.getOmega());
		// v0 -->{LinkBack} v1
		DoubleSubNode v1 = graph.createDoubleSubNode();
		LinkBack e1 = v0.addTargetb(v1);
		assertEquals(v0, e1.getAlpha());
		assertEquals(v1, e1.getOmega());
		// v1 -->{LinkBack} v0
		LinkBack e2 = v1.addTargetb(v0);
		assertEquals(v1, e2.getAlpha());
		assertEquals(v0, e2.getOmega());
		// checks if the edges are in the edge-list of the graph
		int i = 0;
		for (Edge e : graph.edges()) {
			switch (i) {
			case 0:
				assertEquals(e0, e);
				i++;
				break;
			case 1:
				assertEquals(e1, e);
				i++;
				break;
			case 2:
				assertEquals(e2, e);
				i++;
				break;
			default:
				fail("No further edges expected!");
			}
		}
		// checks if the edges are in the incidenceList of both vertices
		i = 0;
		for (Edge e : v0.incidences()) {
			switch (i) {
			case 0:
				assertEquals(e0, e);
				i++;
				break;
			case 1:
				assertEquals(e0.getReversedEdge(), e);
				i++;
				break;
			case 2:
				assertEquals(e1, e);
				i++;
				break;
			case 3:
				assertEquals(e2.getReversedEdge(), e);
				i++;
				break;
			default:
				fail("No further edges expected!");
			}
		}
		i = 0;
		for (Edge e : v1.incidences()) {
			switch (i) {
			case 0:
				assertEquals(e1.getReversedEdge(), e);
				i++;
				break;
			case 1:
				assertEquals(e2, e);
				i++;
				break;
			default:
				fail("No further edges expected!");
			}
		}
	}

	// tests of the method addTargetc
	@Test
	public void addTargetcTest0() {
		DoubleSubNode v0 = graph.createDoubleSubNode();
		// v0 -->{SubLink} v0
		SubLink e0 = v0.addTargetc(v0);
		assertEquals(v0, e0.getAlpha());
		assertEquals(v0, e0.getOmega());
		// v0 -->{SubLink} v1
		DoubleSubNode v1 = graph.createDoubleSubNode();
		SubLink e1 = v0.addTargetc(v1);
		assertEquals(v0, e1.getAlpha());
		assertEquals(v1, e1.getOmega());
		// v1 -->{SubLink} v0
		SubLink e2 = v1.addTargetc(v0);
		assertEquals(v1, e2.getAlpha());
		assertEquals(v0, e2.getOmega());
		// checks if the edges are in the edge-list of the graph
		int i = 0;
		for (Edge e : graph.edges()) {
			switch (i) {
			case 0:
				assertEquals(e0, e);
				i++;
				break;
			case 1:
				assertEquals(e1, e);
				i++;
				break;
			case 2:
				assertEquals(e2, e);
				i++;
				break;
			default:
				fail("No further edges expected!");
			}
		}
		// checks if the edges are in the incidenceList of both vertices
		i = 0;
		for (Edge e : v0.incidences()) {
			switch (i) {
			case 0:
				assertEquals(e0, e);
				i++;
				break;
			case 1:
				assertEquals(e0.getReversedEdge(), e);
				i++;
				break;
			case 2:
				assertEquals(e1, e);
				i++;
				break;
			case 3:
				assertEquals(e2.getReversedEdge(), e);
				i++;
				break;
			default:
				fail("No further edges expected!");
			}
		}
		i = 0;
		for (Edge e : v1.incidences()) {
			switch (i) {
			case 0:
				assertEquals(e1.getReversedEdge(), e);
				i++;
				break;
			case 1:
				assertEquals(e2, e);
				i++;
				break;
			default:
				fail("No further edges expected!");
			}
		}
	}

}
