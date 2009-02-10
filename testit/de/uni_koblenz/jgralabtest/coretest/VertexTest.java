package de.uni_koblenz.jgralabtest.coretest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralabtest.schemas.vertextest.*;

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
		AbstractSuperNode asn = (AbstractSuperNode) graph.createSubNode();
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
		nodes[0] = (AbstractSuperNode) graph.createSubNode();
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
		nodes[0] = (AbstractSuperNode) graph.createSubNode();
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
	 * Generates a number of edges and checks the correct degrees of the
	 * vertices. After that it deletes the edges and checks the degrees again.
	 */
	@Test
	public void getDegreeTest1() {
		Vertex[] nodes = new Vertex[3];
		nodes[0] = (AbstractSuperNode) graph.createSubNode();
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
				assertEquals(vertices.get(start), start.getDegree());
				assertEquals(vertices.get(end), end.getDegree());
			} else {
				assertEquals(vertices.get(start), start.getDegree());
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
	 * Generates a number of different edges and checks the correct degrees of
	 * the vertices considering the different EdgeDirections. After that it
	 * deletes the edges and checks the degrees again.
	 */
	@Test
	public void getDegreeTestEdgeDirection1() {
		Vertex[] nodes = new Vertex[3];
		nodes[0] = (AbstractSuperNode) graph.createSubNode();
		nodes[1] = graph.createDoubleSubNode();
		nodes[2] = graph.createSuperNode();
		int[] expectedInOut = new int[] { 0, 0, 0 };
		int[] expectedIn = new int[] { 0, 0, 0 };
		int[] expectedOut = new int[] { 0, 0, 0 };
		// create new edges
		for (int i = 0; i < 1000; i++) {
			//decides which edge should be created
			int edge=rand.nextInt(1);
			int start = rand.nextInt(2);
			int end = rand.nextInt(2) + 1;
			if(edge==0){
				//create a Link
				graph.createLink((AbstractSuperNode) nodes[start],
						(SuperNode) nodes[end]);
				expectedInOut[start]++;
				expectedOut[start]++;
				expectedInOut[end]++;
				expectedIn[end]++;
			}else{
				//create a LinkBack
				graph.createLinkBack((SuperNode) nodes[end],
						(AbstractSuperNode) nodes[start]);
				expectedInOut[end]++;
				expectedOut[end]++;
				expectedInOut[start]++;
				expectedIn[start]++;
			}
			assertEquals(expectedInOut[0], nodes[0].getDegree(EdgeDirection.INOUT));
			assertEquals(expectedInOut[1], nodes[1].getDegree(EdgeDirection.INOUT));
			assertEquals(expectedInOut[2], nodes[2].getDegree(EdgeDirection.INOUT));
			assertEquals(expectedIn[0], nodes[0].getDegree(EdgeDirection.IN));
			assertEquals(expectedIn[1], nodes[1].getDegree(EdgeDirection.IN));
			assertEquals(expectedIn[2], nodes[2].getDegree(EdgeDirection.IN));
			assertEquals(expectedOut[0], nodes[0].getDegree(EdgeDirection.OUT));
			assertEquals(expectedOut[1], nodes[1].getDegree(EdgeDirection.OUT));
			assertEquals(expectedOut[2], nodes[2].getDegree(EdgeDirection.OUT));
		}
		//delete the edges
		HashMap<Vertex,Integer> numbers=new HashMap<Vertex,Integer>();
		numbers.put(nodes[0], 0);
		numbers.put(nodes[1], 1);
		numbers.put(nodes[2], 2);
		for(int i=graph.getFirstEdgeInGraph().getId();i<graph.getECount();i++){
			Edge e=graph.getEdge(i);
			int start=numbers.get(e.getAlpha());
			int end=numbers.get(e.getOmega());
			expectedInOut[start]--;
			expectedInOut[end]--;
			expectedIn[end]--;
			expectedOut[start]--;
			graph.deleteEdge(e);
			assertEquals(expectedInOut[0], nodes[0].getDegree(EdgeDirection.INOUT));
			assertEquals(expectedInOut[1], nodes[1].getDegree(EdgeDirection.INOUT));
			assertEquals(expectedInOut[2], nodes[2].getDegree(EdgeDirection.INOUT));
			assertEquals(expectedIn[0], nodes[0].getDegree(EdgeDirection.IN));
			assertEquals(expectedIn[1], nodes[1].getDegree(EdgeDirection.IN));
			assertEquals(expectedIn[2], nodes[2].getDegree(EdgeDirection.IN));
			assertEquals(expectedOut[0], nodes[0].getDegree(EdgeDirection.OUT));
			assertEquals(expectedOut[1], nodes[1].getDegree(EdgeDirection.OUT));
			assertEquals(expectedOut[2], nodes[2].getDegree(EdgeDirection.OUT));
		}
	}
	
	// tests of the method getDegree(Class<? extends Edge> ec)

	/**
	 * A vertex with no connected incidences has to have a degree of 0 for each
	 * EdgeClass.
	 */
	@Test
	public void getDegreeTestClass0() {
		Vertex v = graph.createDoubleSubNode();
		assertEquals(0, v.getDegree(Link.class));
		assertEquals(0, v.getDegree(LinkBack.class));
	}

	/**
	 * Generates a number of different edges and checks the correct degrees of
	 * the vertices considering the different Edgeclasses. After that it
	 * deletes the edges and checks the degrees again.
	 */
	@Test
	public void getDegreeTestClass1() {
		//TODO
		Vertex[] nodes = new Vertex[3];
		nodes[0] = (AbstractSuperNode) graph.createSubNode();
		nodes[1] = graph.createDoubleSubNode();
		nodes[2] = graph.createSuperNode();
		int[] expectedLink = new int[] { 0, 0, 0 };
		int[] expectedLinkBack = new int[] { 0, 0, 0 };
		// create new edges
		for (int i = 0; i < 1000; i++) {
			//decides which edge should be created
			int edge=rand.nextInt(1);
			int start = rand.nextInt(2);
			int end = rand.nextInt(2) + 1;
			if(edge==0){
				//create a Link
				graph.createLink((AbstractSuperNode) nodes[start],
						(SuperNode) nodes[end]);
				expectedLink[start]++;
				expectedLink[end]++;
			}else{
				//create a LinkBack
				graph.createLinkBack((SuperNode) nodes[end],
						(AbstractSuperNode) nodes[start]);
				expectedLinkBack[start]++;
				expectedLinkBack[end]++;
			}
			assertEquals(expectedLink[0], nodes[0].getDegree(Link.class));
			assertEquals(expectedLink[1], nodes[1].getDegree(Link.class));
			assertEquals(expectedLink[2], nodes[2].getDegree(Link.class));
			assertEquals(expectedLinkBack[0], nodes[0].getDegree(LinkBack.class));
			assertEquals(expectedLinkBack[1], nodes[1].getDegree(LinkBack.class));
			assertEquals(expectedLinkBack[2], nodes[2].getDegree(LinkBack.class));
		}
		//delete the edges
		HashMap<Vertex,Integer> numbers=new HashMap<Vertex,Integer>();
		numbers.put(nodes[0], 0);
		numbers.put(nodes[1], 1);
		numbers.put(nodes[2], 2);
		for(int i=graph.getFirstEdgeInGraph().getId();i<graph.getECount();i++){
			Edge e=graph.getEdge(i);
			int start=numbers.get(e.getAlpha());
			int end=numbers.get(e.getOmega());
			if(e instanceof Link){
				expectedLink[start]--;
				expectedLink[end]--;
			}else{
				expectedLinkBack[start]--;
				expectedLinkBack[end]--;
			}
			graph.deleteEdge(e);
			assertEquals(expectedLink[0], nodes[0].getDegree(Link.class));
			assertEquals(expectedLink[1], nodes[1].getDegree(Link.class));
			assertEquals(expectedLink[2], nodes[2].getDegree(Link.class));
			assertEquals(expectedLinkBack[0], nodes[0].getDegree(LinkBack.class));
			assertEquals(expectedLinkBack[1], nodes[1].getDegree(LinkBack.class));
			assertEquals(expectedLinkBack[2], nodes[2].getDegree(LinkBack.class));
		}
	}
}
