package de.uni_koblenz.jgralabtest.greql2.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluatorImpl;
import de.uni_koblenz.jgralab.greql2.evaluator.QueryImpl;

public class SubgraphEvaluatorTest {

	private static Graph datagraph;

	@BeforeClass
	public static void setUpBeforeClass() throws GraphIOException {
		datagraph = GraphIO.loadGraphFromFile(
				"./testit/testgraphs/greqltestgraph.tg",
				ImplementationType.STANDARD, null);
	}

	@AfterClass
	public static void tearDownAfterClass() {
		datagraph = null;
	}

	private Object evaluateQuery(String query) {
		return new GreqlEvaluatorImpl(new QueryImpl(query), datagraph,
				new GreqlEnvironmentAdapter()).getResult();
	}

	/*
	 * VertexSetSubgraph
	 */

	@Test
	public void testVertexSetSubgraph_empty() {
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery("on vertexSetSubgraph(set()): V{}");
		assertTrue(ergSet.isEmpty());
	}

	@Test
	public void testVertexSetSubgraph_oneVertex() {
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery("on vertexSetSubgraph(set(getVertex(1))): V{}");
		assertEquals(1, ergSet.size());
		assertTrue(ergSet.contains(datagraph.getVertex(1)));
	}

	@Test
	public void testVertexSetSubgraph_twoVertices() {
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery("on vertexSetSubgraph(set(getVertex(1),getVertex(23))): V{}");
		assertEquals(2, ergSet.size());
		assertTrue(ergSet.contains(datagraph.getVertex(1)));
		assertTrue(ergSet.contains(datagraph.getVertex(23)));
	}

	@Test
	public void testVertexSetSubgraph_empty_noEdge() {
		@SuppressWarnings("unchecked")
		Set<Edge> ergSet = (Set<Edge>) evaluateQuery("on vertexSetSubgraph(set()): E{}");
		assertTrue(ergSet.isEmpty());
	}

	@Test
	public void testVertexSetSubgraph_oneVertex_noEdge() {
		@SuppressWarnings("unchecked")
		Set<Edge> ergSet = (Set<Edge>) evaluateQuery("on vertexSetSubgraph(set(getVertex(1))): E{}");
		assertTrue(ergSet.isEmpty());
	}

	@Test
	public void testVertexSetSubgraph_twoVertices_oneEdge() {
		@SuppressWarnings("unchecked")
		Set<Edge> ergSet = (Set<Edge>) evaluateQuery("on vertexSetSubgraph(set(getVertex(1),getVertex(23))): E{}");
		assertEquals(1, ergSet.size());
		assertTrue(ergSet.contains(datagraph.getEdge(135)));
	}

	/*
	 * EdgeSetSubgraph
	 */

	@Test
	public void testEdgeSetSubgraph_empty() {
		@SuppressWarnings("unchecked")
		Set<Edge> ergSet = (Set<Edge>) evaluateQuery("on edgeSetSubgraph(set()): E{}");
		assertTrue(ergSet.isEmpty());
	}

	@Test
	public void testEdgeSetSubgraph_oneEdge() {
		@SuppressWarnings("unchecked")
		Set<Edge> ergSet = (Set<Edge>) evaluateQuery("on edgeSetSubgraph(set(getEdge(333))): E{}");
		assertEquals(1, ergSet.size());
		assertTrue(ergSet.contains(datagraph.getEdge(333)));
	}

	@Test
	public void testEdgeSetSubgraph_twoEdges() {
		@SuppressWarnings("unchecked")
		Set<Edge> ergSet = (Set<Edge>) evaluateQuery("on edgeSetSubgraph(set(getEdge(333),getEdge(334))): E{}");
		assertEquals(2, ergSet.size());
		assertTrue(ergSet.contains(datagraph.getEdge(333)));
		assertTrue(ergSet.contains(datagraph.getEdge(334)));
	}

	@Test
	public void testEdgeSetSubgraph_empty_twoVertices() {
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery("on edgeSetSubgraph(set()): V{}");
		assertTrue(ergSet.isEmpty());
	}

	@Test
	public void testEdgeSetSubgraph_oneEdge_twoVertices() {
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery("on edgeSetSubgraph(set(getEdge(333))): V{}");
		assertEquals(2, ergSet.size());
		assertTrue(ergSet.contains(datagraph.getVertex(4)));
		assertTrue(ergSet.contains(datagraph.getVertex(5)));
	}

	@Test
	public void testEdgeSetSubgraph_twoEdges_threeVertices() {
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery("on edgeSetSubgraph(set(getEdge(333),getEdge(334))): V{}");
		assertEquals(3, ergSet.size());
		assertTrue(ergSet.contains(datagraph.getVertex(4)));
		assertTrue(ergSet.contains(datagraph.getVertex(5)));
		assertTrue(ergSet.contains(datagraph.getVertex(12)));
	}

	// TODO EdgeSetType EdgeVertexType
}
