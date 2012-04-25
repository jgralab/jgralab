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
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluatorImpl;
import de.uni_koblenz.jgralab.greql2.evaluator.QueryImpl;
import de.uni_koblenz.jgralab.schema.GraphElementClass;

public class ResidualEvaluatorTest {

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
	 * VertexSetExpression
	 */

	@Test
	public void testVertexSetExpression_allVertices() {
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery("V");
		assertEquals(datagraph.getVCount(), ergSet.size());
		for (Vertex v : datagraph.vertices()) {
			assertTrue(createContainmentMessage(v, true), ergSet.contains(v));
		}
	}

	@Test
	public void testVertexSetExpression_verticesOfOneType() {
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery("import junctions.*;V{Crossroad}");
		for (Vertex v : datagraph.vertices()) {
			if (isInstanceOf(v, "junctions.Crossroad")) {
				assertTrue(createContainmentMessage(v, true),
						ergSet.contains(v));
			} else {
				assertFalse(createContainmentMessage(v, false),
						ergSet.contains(v));
			}
		}
	}

	@Test
	public void testVertexSetExpression_verticesOfExactlyOneType() {
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery("import junctions.*;V{Crossroad!}");
		for (Vertex v : datagraph.vertices()) {
			if (isInstanceOf(v, "junctions.Crossroad")
					&& !isInstanceOf(v, "junctions.Plaza")
					&& !isInstanceOf(v, "junctions.Roundabout")) {
				assertTrue(createContainmentMessage(v, true),
						ergSet.contains(v));
			} else {
				assertFalse(createContainmentMessage(v, false),
						ergSet.contains(v));
			}
		}
	}

	@Test
	public void testVertexSetExpression_verticesOfNotOneType() {
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery("import junctions.*;V{^Crossroad}");
		for (Vertex v : datagraph.vertices()) {
			if (!isInstanceOf(v, "junctions.Crossroad")) {
				assertTrue(createContainmentMessage(v, true),
						ergSet.contains(v));
			} else {
				assertFalse(createContainmentMessage(v, false),
						ergSet.contains(v));
			}
		}
	}

	@Test
	public void testVertexSetExpression_verticesOfNotExactlyOneType() {
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery("import junctions.*;V{^Crossroad!}");
		for (Vertex v : datagraph.vertices()) {
			if (!(isInstanceOf(v, "junctions.Crossroad")
					&& !isInstanceOf(v, "junctions.Plaza") && !isInstanceOf(v,
						"junctions.Roundabout"))) {
				assertTrue(createContainmentMessage(v, true),
						ergSet.contains(v));
			} else {
				assertFalse(createContainmentMessage(v, false),
						ergSet.contains(v));
			}
		}
	}

	@Test
	public void testVertexSetExpression_verticesOfOneTypeButNotASubtype() {
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery("import junctions.*;V{Crossroad,^Plaza}");
		for (Vertex v : datagraph.vertices()) {
			if (isInstanceOf(v, "junctions.Crossroad")
					&& !isInstanceOf(v, "junctions.Plaza")) {
				assertTrue(createContainmentMessage(v, true),
						ergSet.contains(v));
			} else {
				assertFalse(createContainmentMessage(v, false),
						ergSet.contains(v));
			}
		}
	}

	@Test
	public void testVertexSetExpression_verticesOfNotASubtypeButOneType() {
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery("import junctions.*;V{^Plaza,Crossroad}");
		for (Vertex v : datagraph.vertices()) {
			if (isInstanceOf(v, "junctions.Crossroad")
					&& !isInstanceOf(v, "junctions.Plaza")) {
				assertTrue(createContainmentMessage(v, true),
						ergSet.contains(v));
			} else {
				assertFalse(createContainmentMessage(v, false),
						ergSet.contains(v));
			}
		}
	}

	// @Test
	// public void testVertexSetExpression_verticesOfNotOneTypeButASubtype() {
	// @SuppressWarnings("unchecked")
	// Set<Vertex> ergSet = (Set<Vertex>)
	// evaluateQuery("import junctions.*;V{^Crossroad,Plaza}");
	// for (Vertex v : datagraph.vertices()) {
	// if (!isInstanceOf(v, "junctions.Crossroad")
	// || isInstanceOf(v, "junctions.Plaza")) {
	// assertTrue(createContainmentMessage(v, true),
	// ergSet.contains(v));
	// } else {
	// assertFalse(createContainmentMessage(v, false),
	// ergSet.contains(v));
	// }
	// }
	// }
	//
	// @Test
	// public void testVertexSetExpression_verticesOfASubtypeNotButOneType() {
	// @SuppressWarnings("unchecked")
	// Set<Vertex> ergSet = (Set<Vertex>)
	// evaluateQuery("import junctions.*;V{Plaza,^Crossroad}");
	// for (Vertex v : datagraph.vertices()) {
	// if (!isInstanceOf(v, "junctions.Crossroad")
	// || isInstanceOf(v, "junctions.Plaza")) {
	// assertTrue(createContainmentMessage(v, true),
	// ergSet.contains(v));
	// } else {
	// assertFalse(createContainmentMessage(v, false),
	// ergSet.contains(v));
	// }
	// }
	// }

	/*
	 * EdgeSetExpression
	 */

	@Test
	public void testEdgeSetExpression_allEdges() {
		@SuppressWarnings("unchecked")
		Set<Edge> ergSet = (Set<Edge>) evaluateQuery("E");
		assertEquals(datagraph.getECount(), ergSet.size());
		for (Edge e : datagraph.edges()) {
			assertTrue(createContainmentMessage(e, true), ergSet.contains(e));
		}
	}

	@Test
	public void testEdgeSetExpression_edgesOfOneType() {
		@SuppressWarnings("unchecked")
		Set<Edge> ergSet = (Set<Edge>) evaluateQuery("import connections.*;E{Street}");
		for (Edge e : datagraph.edges()) {
			if (isInstanceOf(e, "connections.Street")) {
				assertTrue(createContainmentMessage(e, true),
						ergSet.contains(e));
			} else {
				assertFalse(createContainmentMessage(e, false),
						ergSet.contains(e));
			}
		}
	}

	@Test
	public void testEdgeSetExpression_edgesOfExactlyOneType() {
		@SuppressWarnings("unchecked")
		Set<Edge> ergSet = (Set<Edge>) evaluateQuery("import connections.*;E{Street!}");
		for (Edge e : datagraph.edges()) {
			if (isInstanceOf(e, "connections.Street")
					&& !isInstanceOf(e, "connections.Highway")) {
				assertTrue(createContainmentMessage(e, true),
						ergSet.contains(e));
			} else {
				assertFalse(createContainmentMessage(e, false),
						ergSet.contains(e));
			}
		}
	}

	@Test
	public void testEdgeSetExpression_edgesOfNotOneType() {
		@SuppressWarnings("unchecked")
		Set<Edge> ergSet = (Set<Edge>) evaluateQuery("import connections.*;E{^Street}");
		for (Edge e : datagraph.edges()) {
			if (!isInstanceOf(e, "connections.Street")) {
				assertTrue(createContainmentMessage(e, true),
						ergSet.contains(e));
			} else {
				assertFalse(createContainmentMessage(e, false),
						ergSet.contains(e));
			}
		}
	}

	@Test
	public void testEdgeSetExpression_edgesOfNotExactlyOneType() {
		@SuppressWarnings("unchecked")
		Set<Edge> ergSet = (Set<Edge>) evaluateQuery("import connections.*;E{^Street!}");
		for (Edge e : datagraph.edges()) {
			if (!(isInstanceOf(e, "connections.Street") && !isInstanceOf(e,
					"connections.Highway"))) {
				assertTrue(createContainmentMessage(e, true),
						ergSet.contains(e));
			} else {
				assertFalse(createContainmentMessage(e, false),
						ergSet.contains(e));
			}
		}
	}

	@Test
	public void testEdgeSetExpression_edgesOfOneTypeButNotASubtype() {
		@SuppressWarnings("unchecked")
		Set<Edge> ergSet = (Set<Edge>) evaluateQuery("import connections.*;E{Street,^Highway}");
		for (Edge e : datagraph.edges()) {
			if (isInstanceOf(e, "connections.Street")
					&& !isInstanceOf(e, "connections.Highway")) {
				assertTrue(createContainmentMessage(e, true),
						ergSet.contains(e));
			} else {
				assertFalse(createContainmentMessage(e, false),
						ergSet.contains(e));
			}
		}
	}

	@Test
	public void testEdgeSetExpression_edgesOfNotASubtypeButOneType() {
		@SuppressWarnings("unchecked")
		Set<Edge> ergSet = (Set<Edge>) evaluateQuery("import connections.*;E{^Highway,Street}");
		for (Edge e : datagraph.edges()) {
			if (isInstanceOf(e, "connections.Street")
					&& !isInstanceOf(e, "connections.Highway")) {
				assertTrue(createContainmentMessage(e, true),
						ergSet.contains(e));
			} else {
				assertFalse(createContainmentMessage(e, false),
						ergSet.contains(e));
			}
		}
	}

	// @Test
	// public void testEdgeSetExpression_edgesOfNotOneTypeButASubtype() {
	// @SuppressWarnings("unchecked")
	// Set<Edge> ergSet = (Set<Edge>)
	// evaluateQuery("import connections.*;E{^Street,Highway}");
	// for (Edge e : datagraph.edges()) {
	// if (!isInstanceOf(e, "connections.Street")
	// || isInstanceOf(e, "connections.Highway")) {
	// assertTrue(createContainmentMessage(e, true),
	// ergSet.contains(e));
	// } else {
	// assertFalse(createContainmentMessage(e, false),
	// ergSet.contains(e));
	// }
	// }
	// }
	//
	// @Test
	// public void testEdgeSetExpression_edgesOfASubtypeNotButOneType() {
	// @SuppressWarnings("unchecked")
	// Set<Edge> ergSet = (Set<Edge>)
	// evaluateQuery("import connections.*;E{Highway,^Street}");
	// for (Edge e : datagraph.edges()) {
	// if (!isInstanceOf(e, "connections.Street")
	// || isInstanceOf(e, "connections.Highway")) {
	// assertTrue(createContainmentMessage(e, true),
	// ergSet.contains(e));
	// } else {
	// assertFalse(createContainmentMessage(e, false),
	// ergSet.contains(e));
	// }
	// }
	// }
}
