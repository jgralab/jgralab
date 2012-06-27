package de.uni_koblenz.jgralabtest.greql2.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.Query;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlEvaluatorImpl;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;

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
		return new GreqlEvaluatorImpl(Query.createQuery(query), datagraph,
				new GreqlEnvironmentAdapter()).getResult();
	}

	@SuppressWarnings("unchecked")
	private <SC extends GraphElementClass<SC, IC>, IC extends GraphElement<SC, IC>> boolean isInstanceOf(
			GraphElement<SC, IC> v, String type) {
		return v.isInstanceOf((SC) datagraph.getSchema()
				.getAttributedElementClass(type));
	}

	private String createContainmentMessage(GraphElement<?, ?> v,
			boolean showWordNot) {
		return (v instanceof Vertex ? "v" : "e") + v.getId() + " of type "
				+ v.getAttributedElementClass().getQualifiedName() + " is "
				+ (showWordNot ? "not" : "") + " contained.";
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

	/*
	 * VertexTypeSubgraph
	 */

	@Test
	public void testVertexTypeSubgraph_vertices() {
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery("import junctions.*; on vertexTypeSubgraph{Plaza}(): V{}");
		for (Vertex v : datagraph.vertices()) {
			if (isInstanceOf(v, "junctions.Plaza")) {
				assertTrue(createContainmentMessage(v, true),
						ergSet.contains(v));
			} else {
				assertFalse(createContainmentMessage(v, false),
						ergSet.contains(v));
			}
		}
	}

	@Test
	public void testVertexTypeSubgraph_edges() {
		@SuppressWarnings("unchecked")
		Set<Edge> ergSet = (Set<Edge>) evaluateQuery("import junctions.*; on vertexTypeSubgraph{Plaza}(): E{}");
		for (Edge e : datagraph.edges()) {
			if (isInstanceOf(e.getAlpha(), "junctions.Plaza")
					&& isInstanceOf(e.getOmega(), "junctions.Plaza")) {
				assertTrue(createContainmentMessage(e, true),
						ergSet.contains(e));
			} else {
				assertFalse(createContainmentMessage(e, false),
						ergSet.contains(e));
			}
		}
	}

	/*
	 * EdgeTypeSubgraph
	 */

	@Test
	public void testEdgeTypeSubgraph_edges() {
		@SuppressWarnings("unchecked")
		Set<Edge> ergSet = (Set<Edge>) evaluateQuery("import connections.*; on edgeTypeSubgraph{Connection}(): E{}");
		for (Edge e : datagraph.edges()) {
			if (isInstanceOf(e, "connections.Connection")) {
				assertTrue(createContainmentMessage(e, true),
						ergSet.contains(e));
			} else {
				assertFalse(createContainmentMessage(e, false),
						ergSet.contains(e));
			}
		}
	}

	@Test
	public void testEdgeTypeSubgraph_vertices() {
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery("import connections.*; on edgeTypeSubgraph{Connection}(): V{}");
		for (Vertex v : datagraph.vertices()) {
			if (v.getDegree((EdgeClass) datagraph.getSchema()
					.getAttributedElementClass("connections.Connection")) > 0) {
				assertTrue(createContainmentMessage(v, true),
						ergSet.contains(v));
			} else {
				assertFalse(createContainmentMessage(v, false),
						ergSet.contains(v));
			}
		}
	}
}
