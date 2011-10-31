package de.uni_koblenz.jgralabtest.instancetest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.TraversalContext;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralab.impl.InternalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.Link;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.minimal.Node;

/**
 * This class tests various methods from Graph, Vertex, and Edge with respect to
 * the traversal context in the graph.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
@RunWith(Parameterized.class)
public class TraversalContextTest extends InstanceTest {

	public TraversalContextTest(ImplementationType implementationType,
			String dbURL) {
		super(implementationType, dbURL);
	}

	private static final String ID = "TraversalContext";

	@Parameters
	public static Collection<Object[]> configure() {
		return getParameters();
	}

	private MinimalGraph graph;
	private InternalGraph iGraph;
	private TraversalContext alwaysTrue, alwaysFalse, subgraph1, subgraph2;

	@Before
	public void setUp() throws Exception {
		// create graph

		switch (implementationType) {
		case STANDARD:
			graph = MinimalSchema.instance().createMinimalGraph();
			break;
		case TRANSACTION:
			graph = MinimalSchema.instance()
					.createMinimalGraphWithTransactionSupport();
			break;
		case DATABASE:
			graph = createMinimalGraphWithDatabaseSupport();
			break;
		}

		createGraphAndSubgraph();

		iGraph = (InternalGraph) graph;

		createDefaultTCs();
		createReadOnlyTransaction(graph);
	}

	private Node[] v;
	private Link[] e;
	private Link[] re;

	private void createGraphAndSubgraph() throws Exception {
		createTransaction(graph);
		// vertices
		v = new Node[10];
		v[1] = graph.createNode();
		v[2] = graph.createNode();
		v[3] = graph.createNode();
		v[4] = graph.createNode();
		v[5] = graph.createNode();
		v[6] = graph.createNode();
		v[7] = graph.createNode();
		v[8] = graph.createNode();
		v[9] = graph.createNode();

		// edges
		e = new Link[21];
		e[1] = graph.createLink(v[1], v[2]);
		e[2] = graph.createLink(v[1], v[3]);
		e[3] = graph.createLink(v[1], v[5]);
		e[4] = graph.createLink(v[2], v[4]);
		e[5] = graph.createLink(v[2], v[5]);
		e[6] = graph.createLink(v[3], v[4]);
		e[7] = graph.createLink(v[3], v[5]);
		e[8] = graph.createLink(v[4], v[5]);
		e[9] = graph.createLink(v[5], v[6]);
		e[10] = graph.createLink(v[5], v[7]);
		e[11] = graph.createLink(v[5], v[8]);
		e[12] = graph.createLink(v[5], v[9]);
		e[13] = graph.createLink(v[6], v[1]);
		e[14] = graph.createLink(v[6], v[2]);
		e[15] = graph.createLink(v[7], v[1]);
		e[16] = graph.createLink(v[7], v[3]);
		e[17] = graph.createLink(v[8], v[3]);
		e[18] = graph.createLink(v[8], v[4]);
		e[19] = graph.createLink(v[9], v[2]);
		e[20] = graph.createLink(v[9], v[4]);
		re = new Link[21];
		for (int i = 1; i < e.length; i++) {
			re[i] = (Link) e[i].getReversedEdge();
		}
		commit(graph);

		createReadOnlyTransaction(graph);
		SubGraphMarker subgraph1 = new SubGraphMarker(graph);
		SubGraphMarker subgraph2 = new SubGraphMarker(graph);

		for (int i = 1; i < e.length; i++) {
			subgraph1.mark(e[i]);
			subgraph2.mark(e[i]);
		}
		subgraph1.removeMark(v[6]);
		subgraph1.removeMark(v[7]);
		subgraph1.removeMark(v[8]);
		subgraph1.removeMark(v[9]);

		subgraph2.removeMark(v[1]);
		subgraph2.removeMark(v[5]);

		this.subgraph1 = subgraph1;
		this.subgraph2 = subgraph2;
		commit(graph);
	}

	private void createDefaultTCs() {
		// TCs
		alwaysTrue = new TraversalContext() {

			@Override
			public boolean containsVertex(Vertex v) {
				return iGraph.vSeqContainsVertex(v);
			}

			@Override
			public boolean containsGraphElement(GraphElement e) {
				return e.getGraph() == iGraph;
			}

			@Override
			public boolean containsEdge(Edge e) {
				return iGraph.eSeqContainsEdge(e);
			}
		};

		alwaysFalse = new TraversalContext() {

			@Override
			public boolean containsVertex(Vertex v) {
				return false;
			}

			@Override
			public boolean containsGraphElement(GraphElement e) {
				return false;
			}

			@Override
			public boolean containsEdge(Edge e) {
				return false;
			}
		};
	}

	private MinimalGraph createMinimalGraphWithDatabaseSupport() {
		dbHandler.connectToDatabase();
		dbHandler.loadMinimalSchemaIntoGraphDatabase();
		return dbHandler.createMinimalGraphWithDatabaseSupport(ID);
	}

	@After
	public void tearDown() throws Exception {
		if (implementationType == ImplementationType.DATABASE) {
			cleanAndCloseGraphDatabase();
		}
		commit(graph);
	}

	/*
	 * Start with testing the methods for setting the TC for and getting the TC
	 * from the graph
	 */

	@Test
	public void testSetAndGetTraversalContext() throws Exception {
		TraversalContext oldTC;
		oldTC = graph.setTraversalContext(alwaysTrue);
		assertTrue(oldTC == null);
		assertTrue(graph.getTraversalContext() == alwaysTrue);
		oldTC = graph.setTraversalContext(alwaysFalse);
		assertTrue(oldTC == alwaysTrue);
		assertTrue(graph.getTraversalContext() == alwaysFalse);
		oldTC = graph.setTraversalContext(subgraph1);
		assertTrue(oldTC == alwaysFalse);
		assertTrue(graph.getTraversalContext() == subgraph1);
		oldTC = graph.setTraversalContext(subgraph2);
		assertTrue(oldTC == subgraph1);
		assertTrue(graph.getTraversalContext() == subgraph2);
		oldTC = graph.setTraversalContext(null);
		assertTrue(oldTC == subgraph2);
		assertTrue(graph.getTraversalContext() == null);
	}

	/*
	 * Testing vertex related methods in Graph and Vertex
	 */

	@Test
	public void testGetFirstVertex() throws Exception {
		// with not TC (or TC with always true), the first vertex is the first
		// vertex in Vseq
		assertEquals(v[1], graph.getFirstVertex());
		graph.setTraversalContext(alwaysTrue);
		assertEquals(v[1], graph.getFirstVertex());

		// with TC returning always false, there is no first vertex (null)
		graph.setTraversalContext(alwaysFalse);
		assertNull(graph.getFirstVertex());

		// with subgraph where the first vertex is missing, the second vertex is
		// the first one in TC
		graph.setTraversalContext(subgraph1);
		assertEquals(v[1], graph.getFirstVertex());

		graph.setTraversalContext(subgraph2);
		assertEquals(v[2], graph.getFirstVertex());
	}

	@Test
	public void testGetLastVertex() throws Exception {
		// with no TC (or TC with always true), the last vertex is the last
		// vertex in Vseq
		assertEquals(v[9], graph.getLastVertex());
		graph.setTraversalContext(alwaysTrue);
		assertEquals(v[9], graph.getLastVertex());

		// with TC returning always false, there is no last vertex (null)
		graph.setTraversalContext(alwaysFalse);
		assertNull(graph.getLastVertex());

		// with TC where the last few vertices are missing, the last
		// vertex is determined by the TC
		graph.setTraversalContext(subgraph1);
		assertEquals(v[5], graph.getLastVertex());

		graph.setTraversalContext(subgraph2);
		assertEquals(v[9], graph.getLastVertex());
	}

	@Test
	public void testGetVCount() throws Exception {
		// with no TC (or TC with always true), the vertex count is the length
		// of Vseq
		assertEquals(9, graph.getVCount());
		graph.setTraversalContext(alwaysTrue);
		assertEquals(9, graph.getVCount());

		// with TC returning always false, the vertex count is 0
		graph.setTraversalContext(alwaysFalse);
		assertEquals(0, graph.getVCount());

		// with 4 vertices missing, the vertex count is 5
		graph.setTraversalContext(subgraph1);
		assertEquals(5, graph.getVCount());

		// with one vertex missing, the vertex count is 8
		graph.setTraversalContext(subgraph2);
		assertEquals(7, graph.getVCount());
	}

	@Test
	public void testGetNextAndPrevVertex() throws Exception {
		// no TC and always true TC
		assertEquals(v[2], v[1].getNextVertex());
		assertNull(v[1].getPrevVertex());

		assertEquals(v[3], v[2].getNextVertex());
		assertEquals(v[1], v[2].getPrevVertex());

		assertEquals(v[4], v[3].getNextVertex());
		assertEquals(v[2], v[3].getPrevVertex());

		assertEquals(v[5], v[4].getNextVertex());
		assertEquals(v[3], v[4].getPrevVertex());

		assertEquals(v[6], v[5].getNextVertex());
		assertEquals(v[4], v[5].getPrevVertex());

		assertEquals(v[7], v[6].getNextVertex());
		assertEquals(v[5], v[6].getPrevVertex());

		assertEquals(v[8], v[7].getNextVertex());
		assertEquals(v[6], v[7].getPrevVertex());

		assertEquals(v[9], v[8].getNextVertex());
		assertEquals(v[7], v[8].getPrevVertex());

		assertNull(v[9].getNextVertex());
		assertEquals(v[8], v[9].getPrevVertex());

		graph.setTraversalContext(alwaysTrue);
		assertEquals(v[2], v[1].getNextVertex());
		assertNull(v[1].getPrevVertex());

		assertEquals(v[3], v[2].getNextVertex());
		assertEquals(v[1], v[2].getPrevVertex());

		assertEquals(v[4], v[3].getNextVertex());
		assertEquals(v[2], v[3].getPrevVertex());

		assertEquals(v[5], v[4].getNextVertex());
		assertEquals(v[3], v[4].getPrevVertex());

		assertEquals(v[6], v[5].getNextVertex());
		assertEquals(v[4], v[5].getPrevVertex());

		assertEquals(v[7], v[6].getNextVertex());
		assertEquals(v[5], v[6].getPrevVertex());

		assertEquals(v[8], v[7].getNextVertex());
		assertEquals(v[6], v[7].getPrevVertex());

		assertEquals(v[9], v[8].getNextVertex());
		assertEquals(v[7], v[8].getPrevVertex());

		assertNull(v[9].getNextVertex());
		assertEquals(v[8], v[9].getPrevVertex());

		// always false TC doesn't make sense here

		// subgraph1 (v6 to v9 gone)

		graph.setTraversalContext(subgraph1);
		assertEquals(v[2], v[1].getNextVertex());
		assertNull(v[1].getPrevVertex());

		assertEquals(v[3], v[2].getNextVertex());
		assertEquals(v[1], v[2].getPrevVertex());

		assertEquals(v[4], v[3].getNextVertex());
		assertEquals(v[2], v[3].getPrevVertex());

		assertEquals(v[5], v[4].getNextVertex());
		assertEquals(v[3], v[4].getPrevVertex());

		assertNull(v[5].getNextVertex());
		assertEquals(v[4], v[5].getPrevVertex());

		// subgraph2 (v1 and v5 gone)
		graph.setTraversalContext(subgraph2);

		assertEquals(v[3], v[2].getNextVertex());
		assertNull(v[2].getPrevVertex());

		assertEquals(v[4], v[3].getNextVertex());
		assertEquals(v[2], v[3].getPrevVertex());

		assertEquals(v[6], v[4].getNextVertex());
		assertEquals(v[3], v[4].getPrevVertex());

		assertEquals(v[7], v[6].getNextVertex());
		assertEquals(v[4], v[6].getPrevVertex());

		assertEquals(v[8], v[7].getNextVertex());
		assertEquals(v[6], v[7].getPrevVertex());

		assertEquals(v[9], v[8].getNextVertex());
		assertEquals(v[7], v[8].getPrevVertex());

		assertNull(v[9].getNextVertex());
		assertEquals(v[8], v[9].getPrevVertex());
	}

	@Test
	public void testVertices() throws Exception {
		List<Vertex> listAllTrue = new LinkedList<Vertex>();
		for (int i = 1; i < v.length; i++) {
			listAllTrue.add(v[i]);
		}
		testVerticesWithTC(listAllTrue, null);
		testVerticesWithTC(listAllTrue, alwaysTrue);

		// empty list
		List<Vertex> listAllFalse = new LinkedList<Vertex>();
		testVerticesWithTC(listAllFalse, alwaysFalse);

		// subgraph1
		List<Vertex> list1 = new LinkedList<Vertex>();
		list1.add(v[1]);
		list1.add(v[2]);
		list1.add(v[3]);
		list1.add(v[4]);
		list1.add(v[5]);
		testVerticesWithTC(list1, subgraph1);

		// subgraph2
		List<Vertex> list2 = new LinkedList<Vertex>();
		list2.add(v[2]);
		list2.add(v[3]);
		list2.add(v[4]);
		list2.add(v[6]);
		list2.add(v[7]);
		list2.add(v[8]);
		list2.add(v[9]);
		testVerticesWithTC(list2, subgraph2);
	}

	private void testVerticesWithTC(List<Vertex> controlList,
			TraversalContext tc) {
		graph.setTraversalContext(tc);
		List<Vertex> vertices = new LinkedList<Vertex>();
		for (Vertex v : graph.vertices()) {
			vertices.add(v);
		}
		assertListsEqual(controlList, vertices);
	}

	/*
	 * Testing edge related methods in Graph and Edge: getECount, edges,
	 * getNextEdge, getPrevEdge
	 */

	@Test
	public void testGetFirstEdge() throws Exception {
		// with not TC (or TC with always true), the first edge is the first
		// vertex in Eseq
		assertEquals(e[1], graph.getFirstEdge());
		graph.setTraversalContext(alwaysTrue);
		assertEquals(e[1], graph.getFirstEdge());

		// with TC returning always false, there is no first edge (null)
		graph.setTraversalContext(alwaysFalse);
		assertNull(graph.getFirstEdge());

		// with TC where the first few edges are missing, the first edge
		// is determined by the TC
		graph.setTraversalContext(subgraph1);
		assertEquals(e[1], graph.getFirstEdge());

		graph.setTraversalContext(subgraph2);
		assertEquals(e[4], graph.getFirstEdge());
	}

	@Test
	public void testGetLastEdge() throws Exception {
		// with no TC (or TC with always true), the last edge is the last edge
		// in Eseq
		assertEquals(e[20], graph.getLastEdge());
		graph.setTraversalContext(alwaysTrue);
		assertEquals(e[20], graph.getLastEdge());

		// with TC returning always false, there is no last edge
		graph.setTraversalContext(alwaysFalse);
		assertNull(graph.getLastEdge());

		// with TC where the last few edges are missing, the last edge is
		// determined by the TC
		graph.setTraversalContext(subgraph1);
		assertEquals(e[8], graph.getLastEdge());

		graph.setTraversalContext(subgraph2);
		assertEquals(e[20], graph.getLastEdge());
	}

	@Test
	public void testGetECount() throws Exception {
		// with no TC (or TC with always true), the edge count is the length
		// of Eseq
		assertEquals(20, graph.getECount());
		graph.setTraversalContext(alwaysTrue);
		assertEquals(20, graph.getECount());

		// with TC returning always false, the edge count is 0
		graph.setTraversalContext(alwaysFalse);
		assertEquals(0, graph.getECount());

		// subgraph1
		graph.setTraversalContext(subgraph1);
		assertEquals(8, graph.getECount());

		// subgraph2
		graph.setTraversalContext(subgraph2);
		assertEquals(8, graph.getECount());
	}

	@Test
	public void testGetNextAndPrevEdge() throws Exception {
		// no TC and always true TC
		assertEquals(e[2], e[1].getNextEdge());
		assertNull(e[1].getPrevEdge());

		assertEquals(e[3], e[2].getNextEdge());
		assertEquals(e[1], e[2].getPrevEdge());

		assertEquals(e[4], e[3].getNextEdge());
		assertEquals(e[2], e[3].getPrevEdge());

		assertEquals(e[5], e[4].getNextEdge());
		assertEquals(e[3], e[4].getPrevEdge());

		assertEquals(e[6], e[5].getNextEdge());
		assertEquals(e[4], e[5].getPrevEdge());

		assertEquals(e[7], e[6].getNextEdge());
		assertEquals(e[5], e[6].getPrevEdge());

		assertEquals(e[8], e[7].getNextEdge());
		assertEquals(e[6], e[7].getPrevEdge());

		assertEquals(e[9], e[8].getNextEdge());
		assertEquals(e[7], e[8].getPrevEdge());

		assertEquals(e[10], e[9].getNextEdge());
		assertEquals(e[8], e[9].getPrevEdge());

		assertEquals(e[11], e[10].getNextEdge());
		assertEquals(e[9], e[10].getPrevEdge());

		assertEquals(e[12], e[11].getNextEdge());
		assertEquals(e[10], e[11].getPrevEdge());

		assertEquals(e[13], e[12].getNextEdge());
		assertEquals(e[11], e[12].getPrevEdge());

		assertEquals(e[14], e[13].getNextEdge());
		assertEquals(e[12], e[13].getPrevEdge());

		assertEquals(e[15], e[14].getNextEdge());
		assertEquals(e[13], e[14].getPrevEdge());

		assertEquals(e[16], e[15].getNextEdge());
		assertEquals(e[14], e[15].getPrevEdge());

		assertEquals(e[17], e[16].getNextEdge());
		assertEquals(e[15], e[16].getPrevEdge());

		assertEquals(e[18], e[17].getNextEdge());
		assertEquals(e[16], e[17].getPrevEdge());

		assertEquals(e[19], e[18].getNextEdge());
		assertEquals(e[17], e[18].getPrevEdge());

		assertEquals(e[20], e[19].getNextEdge());
		assertEquals(e[18], e[19].getPrevEdge());

		assertNull(e[20].getNextEdge());
		assertEquals(e[19], e[20].getPrevEdge());

		graph.setTraversalContext(alwaysTrue);

		assertEquals(e[2], e[1].getNextEdge());
		assertNull(e[1].getPrevEdge());

		assertEquals(e[3], e[2].getNextEdge());
		assertEquals(e[1], e[2].getPrevEdge());

		assertEquals(e[4], e[3].getNextEdge());
		assertEquals(e[2], e[3].getPrevEdge());

		assertEquals(e[5], e[4].getNextEdge());
		assertEquals(e[3], e[4].getPrevEdge());

		assertEquals(e[6], e[5].getNextEdge());
		assertEquals(e[4], e[5].getPrevEdge());

		assertEquals(e[7], e[6].getNextEdge());
		assertEquals(e[5], e[6].getPrevEdge());

		assertEquals(e[8], e[7].getNextEdge());
		assertEquals(e[6], e[7].getPrevEdge());

		assertEquals(e[9], e[8].getNextEdge());
		assertEquals(e[7], e[8].getPrevEdge());

		assertEquals(e[10], e[9].getNextEdge());
		assertEquals(e[8], e[9].getPrevEdge());

		assertEquals(e[11], e[10].getNextEdge());
		assertEquals(e[9], e[10].getPrevEdge());

		assertEquals(e[12], e[11].getNextEdge());
		assertEquals(e[10], e[11].getPrevEdge());

		assertEquals(e[13], e[12].getNextEdge());
		assertEquals(e[11], e[12].getPrevEdge());

		assertEquals(e[14], e[13].getNextEdge());
		assertEquals(e[12], e[13].getPrevEdge());

		assertEquals(e[15], e[14].getNextEdge());
		assertEquals(e[13], e[14].getPrevEdge());

		assertEquals(e[16], e[15].getNextEdge());
		assertEquals(e[14], e[15].getPrevEdge());

		assertEquals(e[17], e[16].getNextEdge());
		assertEquals(e[15], e[16].getPrevEdge());

		assertEquals(e[18], e[17].getNextEdge());
		assertEquals(e[16], e[17].getPrevEdge());

		assertEquals(e[19], e[18].getNextEdge());
		assertEquals(e[17], e[18].getPrevEdge());

		assertEquals(e[20], e[19].getNextEdge());
		assertEquals(e[18], e[19].getPrevEdge());

		assertNull(e[20].getNextEdge());
		assertEquals(e[19], e[20].getPrevEdge());

		// always false TC doesn't make sense here

		// subgraph1 (v6 to v9 gone)
		graph.setTraversalContext(subgraph1);
		assertEquals(e[2], e[1].getNextEdge());
		assertNull(e[1].getPrevEdge());

		assertEquals(e[3], e[2].getNextEdge());
		assertEquals(e[1], e[2].getPrevEdge());

		assertEquals(e[4], e[3].getNextEdge());
		assertEquals(e[2], e[3].getPrevEdge());

		assertEquals(e[5], e[4].getNextEdge());
		assertEquals(e[3], e[4].getPrevEdge());

		assertEquals(e[6], e[5].getNextEdge());
		assertEquals(e[4], e[5].getPrevEdge());

		assertEquals(e[7], e[6].getNextEdge());
		assertEquals(e[5], e[6].getPrevEdge());

		assertEquals(e[8], e[7].getNextEdge());
		assertEquals(e[6], e[7].getPrevEdge());

		assertNull(e[8].getNextEdge());
		assertEquals(e[7], e[8].getPrevEdge());

		// subgraph2 (v1 and v5 gone)
		graph.setTraversalContext(subgraph2);
		assertEquals(e[6], e[4].getNextEdge());
		assertNull(e[4].getPrevEdge());

		assertEquals(e[14], e[6].getNextEdge());
		assertEquals(e[4], e[6].getPrevEdge());

		assertEquals(e[16], e[14].getNextEdge());
		assertEquals(e[6], e[14].getPrevEdge());

		assertEquals(e[17], e[16].getNextEdge());
		assertEquals(e[14], e[16].getPrevEdge());

		assertEquals(e[18], e[17].getNextEdge());
		assertEquals(e[16], e[17].getPrevEdge());

		assertEquals(e[19], e[18].getNextEdge());
		assertEquals(e[17], e[18].getPrevEdge());

		assertEquals(e[20], e[19].getNextEdge());
		assertEquals(e[18], e[19].getPrevEdge());

		assertNull(e[20].getNextEdge());
		assertEquals(e[19], e[20].getPrevEdge());
	}

	@Test
	public void testEdges() {
		List<Edge> listAllTrue = new LinkedList<Edge>();
		for (int i = 1; i < e.length; i++) {
			listAllTrue.add(e[i]);
		}
		testEdgesWithTC(listAllTrue, null);
		testEdgesWithTC(listAllTrue, alwaysTrue);

		List<Edge> listAllFalse = new LinkedList<Edge>();
		testEdgesWithTC(listAllFalse, alwaysFalse);

		List<Edge> list1 = new LinkedList<Edge>();
		list1.add(e[1]);
		list1.add(e[2]);
		list1.add(e[3]);
		list1.add(e[4]);
		list1.add(e[5]);
		list1.add(e[6]);
		list1.add(e[7]);
		list1.add(e[8]);
		testEdgesWithTC(list1, subgraph1);

		List<Edge> list2 = new LinkedList<Edge>();
		list2.add(e[4]);
		list2.add(e[6]);
		list2.add(e[14]);
		list2.add(e[16]);
		list2.add(e[17]);
		list2.add(e[18]);
		list2.add(e[19]);
		list2.add(e[20]);
		testEdgesWithTC(list2, subgraph2);

	}

	private void testEdgesWithTC(List<Edge> controlList, TraversalContext tc) {
		graph.setTraversalContext(tc);
		List<Edge> edges = new LinkedList<Edge>();
		for (Edge e : graph.edges()) {
			edges.add(e);
		}
		assertListsEqual(controlList, edges);
	}

	/*
	 * Testing incidence related methods in Vertex and Edge: incidences(all
	 * variants)
	 */
	@Test
	public void testGetDegree() throws Exception {
		// no TC and all true
		assertEquals(5, v[1].getDegree());
		assertEquals(5, v[2].getDegree());
		assertEquals(5, v[3].getDegree());
		assertEquals(5, v[4].getDegree());
		assertEquals(8, v[5].getDegree());
		assertEquals(3, v[6].getDegree());
		assertEquals(3, v[7].getDegree());
		assertEquals(3, v[8].getDegree());
		assertEquals(3, v[9].getDegree());

		graph.setTraversalContext(alwaysTrue);
		assertEquals(5, v[1].getDegree());
		assertEquals(5, v[2].getDegree());
		assertEquals(5, v[3].getDegree());
		assertEquals(5, v[4].getDegree());
		assertEquals(8, v[5].getDegree());
		assertEquals(3, v[6].getDegree());
		assertEquals(3, v[7].getDegree());
		assertEquals(3, v[8].getDegree());
		assertEquals(3, v[9].getDegree());

		// subgraph1
		graph.setTraversalContext(subgraph1);
		assertEquals(3, v[1].getDegree());
		assertEquals(3, v[2].getDegree());
		assertEquals(3, v[3].getDegree());
		assertEquals(3, v[4].getDegree());
		assertEquals(4, v[5].getDegree());

		// subgraph2
		graph.setTraversalContext(subgraph2);
		assertEquals(3, v[2].getDegree());
		assertEquals(3, v[3].getDegree());
		assertEquals(4, v[4].getDegree());
		assertEquals(1, v[6].getDegree());
		assertEquals(1, v[7].getDegree());
		assertEquals(2, v[8].getDegree());
		assertEquals(2, v[9].getDegree());
	}

	@Test
	public void testGetFirstAndLastIncidence() throws Exception {
		// no TC or all true
		assertEquals(e[1], v[1].getFirstIncidence());
		assertEquals(re[15], v[1].getLastIncidence());

		assertEquals(re[1], v[2].getFirstIncidence());
		assertEquals(re[19], v[2].getLastIncidence());

		assertEquals(re[2], v[3].getFirstIncidence());
		assertEquals(re[17], v[3].getLastIncidence());

		assertEquals(re[4], v[4].getFirstIncidence());
		assertEquals(re[20], v[4].getLastIncidence());

		assertEquals(re[3], v[5].getFirstIncidence());
		assertEquals(e[12], v[5].getLastIncidence());

		assertEquals(re[9], v[6].getFirstIncidence());
		assertEquals(e[14], v[6].getLastIncidence());

		assertEquals(re[10], v[7].getFirstIncidence());
		assertEquals(e[16], v[7].getLastIncidence());

		assertEquals(re[11], v[8].getFirstIncidence());
		assertEquals(e[18], v[8].getLastIncidence());

		assertEquals(re[12], v[9].getFirstIncidence());
		assertEquals(e[20], v[9].getLastIncidence());

		graph.setTraversalContext(alwaysTrue);

		assertEquals(e[1], v[1].getFirstIncidence());
		assertEquals(re[15], v[1].getLastIncidence());

		assertEquals(re[1], v[2].getFirstIncidence());
		assertEquals(re[19], v[2].getLastIncidence());

		assertEquals(re[2], v[3].getFirstIncidence());
		assertEquals(re[17], v[3].getLastIncidence());

		assertEquals(re[4], v[4].getFirstIncidence());
		assertEquals(re[20], v[4].getLastIncidence());

		assertEquals(re[3], v[5].getFirstIncidence());
		assertEquals(e[12], v[5].getLastIncidence());

		assertEquals(re[9], v[6].getFirstIncidence());
		assertEquals(e[14], v[6].getLastIncidence());

		assertEquals(re[10], v[7].getFirstIncidence());
		assertEquals(e[16], v[7].getLastIncidence());

		assertEquals(re[11], v[8].getFirstIncidence());
		assertEquals(e[18], v[8].getLastIncidence());

		assertEquals(re[12], v[9].getFirstIncidence());
		assertEquals(e[20], v[9].getLastIncidence());

		// subgraph1
		graph.setTraversalContext(subgraph1);
		assertEquals(e[1], v[1].getFirstIncidence());
		assertEquals(e[3], v[1].getLastIncidence());

		assertEquals(re[1], v[2].getFirstIncidence());
		assertEquals(e[5], v[2].getLastIncidence());

		assertEquals(re[2], v[3].getFirstIncidence());
		assertEquals(e[7], v[3].getLastIncidence());

		assertEquals(re[4], v[4].getFirstIncidence());
		assertEquals(e[8], v[4].getLastIncidence());

		assertEquals(re[3], v[5].getFirstIncidence());
		assertEquals(re[8], v[5].getLastIncidence());

		// subgraph2
		graph.setTraversalContext(subgraph2);

		assertEquals(e[4], v[2].getFirstIncidence());
		assertEquals(re[19], v[2].getLastIncidence());

		assertEquals(e[6], v[3].getFirstIncidence());
		assertEquals(re[17], v[3].getLastIncidence());

		assertEquals(re[4], v[4].getFirstIncidence());
		assertEquals(re[20], v[4].getLastIncidence());

		assertEquals(e[14], v[6].getFirstIncidence());
		assertEquals(e[14], v[6].getLastIncidence());

		// v7 and v8 are border cases with only one incident edge
		assertEquals(e[16], v[7].getFirstIncidence());
		assertEquals(e[16], v[7].getLastIncidence());

		assertEquals(e[17], v[8].getFirstIncidence());
		assertEquals(e[18], v[8].getLastIncidence());

		assertEquals(e[19], v[9].getFirstIncidence());
		assertEquals(e[20], v[9].getLastIncidence());

		// border case isolated vertex
		graph.setTraversalContext(null);
		// create subgraph with isolated v1 (removing all its incidences)
		SubGraphMarker subgraph3 = new SubGraphMarker(graph);
		for (Vertex v : graph.vertices()) {
			subgraph3.mark(v);
		}
		for (Edge e : graph.edges()) {
			subgraph3.mark(e);
		}
		// remove v1 and incidences from subgraph
		subgraph3.removeMark(v[1]);
		// add v1 without incidences to subgraph
		subgraph3.mark(v[1]);
		graph.setTraversalContext(subgraph3);
		assertNull(v[1].getFirstIncidence());
		assertNull(v[1].getLastIncidence());
	}

	@Test
	public void testGetNextAndPrevIncidence() {
		// no TC or always true

		// v2
		assertEquals(e[4], re[1].getNextIncidence());
		assertNull(re[1].getPrevIncidence());

		assertEquals(e[5], e[4].getNextIncidence());
		assertEquals(re[1], e[4].getPrevIncidence());

		assertEquals(re[14], e[5].getNextIncidence());
		assertEquals(e[4], e[5].getPrevIncidence());

		assertEquals(re[19], re[14].getNextIncidence());
		assertEquals(e[5], re[14].getPrevIncidence());

		assertNull(re[19].getNextIncidence());
		assertEquals(re[14], re[19].getPrevIncidence());

		// v6
		assertEquals(e[13], re[9].getNextIncidence());
		assertNull(re[9].getPrevIncidence());

		assertEquals(e[14], e[13].getNextIncidence());
		assertEquals(re[9], e[13].getPrevIncidence());

		assertNull(e[14].getNextIncidence());
		assertEquals(e[13], e[14].getPrevIncidence());

		graph.setTraversalContext(alwaysTrue);
		// v2
		assertEquals(e[4], re[1].getNextIncidence());
		assertNull(re[1].getPrevIncidence());

		assertEquals(e[5], e[4].getNextIncidence());
		assertEquals(re[1], e[4].getPrevIncidence());

		assertEquals(re[14], e[5].getNextIncidence());
		assertEquals(e[4], e[5].getPrevIncidence());

		assertEquals(re[19], re[14].getNextIncidence());
		assertEquals(e[5], re[14].getPrevIncidence());

		assertNull(re[19].getNextIncidence());
		assertEquals(re[14], re[19].getPrevIncidence());

		// v6
		assertEquals(e[13], re[9].getNextIncidence());
		assertNull(re[9].getPrevIncidence());

		assertEquals(e[14], e[13].getNextIncidence());
		assertEquals(re[9], e[13].getPrevIncidence());

		assertNull(e[14].getNextIncidence());
		assertEquals(e[13], e[14].getPrevIncidence());

		// subgraph1
		graph.setTraversalContext(subgraph1);
		// v2
		assertEquals(e[4], re[1].getNextIncidence());
		assertNull(re[1].getPrevIncidence());

		assertEquals(e[5], e[4].getNextIncidence());
		assertEquals(re[1], e[4].getPrevIncidence());

		assertNull(e[5].getNextIncidence());
		assertEquals(e[4], e[5].getPrevIncidence());

		// subgraph2
		graph.setTraversalContext(subgraph2);
		// v2
		assertEquals(re[14], e[4].getNextIncidence());
		assertNull(e[4].getPrevIncidence());

		assertEquals(re[19], re[14].getNextIncidence());
		assertEquals(e[4], re[14].getPrevIncidence());

		assertNull(re[19].getNextIncidence());
		assertEquals(re[14], re[19].getPrevIncidence());

		// v6 (only one incidence)
		assertNull(e[14].getNextIncidence());
		assertNull(e[14].getPrevIncidence());
	}

	@Test
	public void testIncidences() throws Exception {
		// no TC or all true
		// v2
		List<Edge> incidenceList = new LinkedList<Edge>();
		incidenceList.add(re[1]);
		incidenceList.add(e[4]);
		incidenceList.add(e[5]);
		incidenceList.add(re[14]);
		incidenceList.add(re[19]);
		testIncidencesWithTC(v[2], incidenceList, null);
		testIncidencesWithTC(v[2], incidenceList, alwaysTrue);

		// v6
		incidenceList.clear();
		incidenceList.add(re[9]);
		incidenceList.add(e[13]);
		incidenceList.add(e[14]);
		testIncidencesWithTC(v[6], incidenceList, null);
		testIncidencesWithTC(v[6], incidenceList, alwaysTrue);

		// subgraph1
		// v2
		incidenceList.clear();
		incidenceList.add(re[1]);
		incidenceList.add(e[4]);
		incidenceList.add(e[5]);
		testIncidencesWithTC(v[2], incidenceList, subgraph1);

		// subgraph2
		// v2
		incidenceList.clear();
		incidenceList.add(e[4]);
		incidenceList.add(re[14]);
		incidenceList.add(re[19]);
		testIncidencesWithTC(v[2], incidenceList, subgraph2);

		// v6
		incidenceList.clear();
		incidenceList.add(e[14]);
		testIncidencesWithTC(v[6], incidenceList, subgraph2);
	}

	private void testIncidencesWithTC(Vertex v, List<Edge> controlList,
			TraversalContext tc) {
		graph.setTraversalContext(tc);
		List<Edge> edges = new LinkedList<Edge>();
		for (Edge e : v.incidences()) {
			edges.add(e);
		}
		assertListsEqual(controlList, edges);
	}

	/*
	 * TODO test reachableVertices (all variants), adjacences
	 */

	/*
	 * Other methods to test: GraphIO.saveGraphToStream
	 */

	// global helpers

	private void assertListsEqual(List<?> list1, List<?> list2) {
		Iterator<?> iter1 = list1.iterator();
		Iterator<?> iter2 = list2.iterator();
		while (iter1.hasNext()) {
			assertTrue("First list is longer: should be " + list1.size()
					+ " but was " + list2.size(), iter2.hasNext());
			assertEquals(iter1.next(), iter2.next());
		}
		assertFalse("Second list is longer: should be " + list1.size()
				+ " but was " + list2.size(), iter2.hasNext());
	}

	private void cleanAndCloseGraphDatabase() {
		dbHandler.clearAllTables();
		dbHandler.closeGraphdatabase();
	}
}
