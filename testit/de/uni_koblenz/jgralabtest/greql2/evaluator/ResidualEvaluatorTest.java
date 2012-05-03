package de.uni_koblenz.jgralabtest.greql2.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pcollections.PVector;

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
import de.uni_koblenz.jgralab.greql2.exception.UnknownTypeException;
import de.uni_koblenz.jgralab.greql2.types.Table;
import de.uni_koblenz.jgralab.greql2.types.Tuple;
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

	/*
	 * TypeIdEvaluator
	 */

	@Test(expected = UnknownTypeException.class)
	public void testTypeId_UnknownType() {
		evaluateQuery("import junctions.*;V{UnknownType}");
	}

	/*
	 * QuantifiedExpressionEvaluator
	 */

	@Test
	public void testQuantifiedExpressionEvaluator_forall_true() {
		assertTrue((Boolean) evaluateQuery("forall n:list(1..9)@n>0"));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_forall_false() {
		assertFalse((Boolean) evaluateQuery("forall n:list(1..9)@n<0"));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_forall_false_onlyone() {
		assertFalse((Boolean) evaluateQuery("forall n:list(1..9)@n<9"));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_forall_withNonBooleanPredicate() {
		assertTrue((Boolean) evaluateQuery("forall n:list(1..9)@V{}"));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_eixtst_true() {
		assertTrue((Boolean) evaluateQuery("exists n:list(1..9)@n>0"));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_eixtst_onlyone() {
		assertTrue((Boolean) evaluateQuery("exists n:list(1..9)@n>8"));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_exists_false() {
		assertFalse((Boolean) evaluateQuery("exists n:list(1..9)@n<0"));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_exists_withNonBooleanPredicate() {
		assertTrue((Boolean) evaluateQuery("exists n:list(1..9)@V{}"));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_eixtstExactly_true() {
		assertTrue((Boolean) evaluateQuery("exists! n:list(1..9)@n=5"));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_existsExactly_false_severalExists() {
		assertFalse((Boolean) evaluateQuery("exists! n:list(1..9)@n>0"));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_existsExactly_false_noneExists() {
		assertFalse((Boolean) evaluateQuery("exists! n:list(1..9)@n<0"));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_existsExactly_withNonBooleanPredicate() {
		assertFalse((Boolean) evaluateQuery("exists! n:list(1..9)@V{}"));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_existsExactly_withNonBooleanPredicate_OnlyOneElem() {
		assertTrue((Boolean) evaluateQuery("exists! n:list(1..1)@V{}"));
	}

	/*
	 * ConditionalExpression
	 */

	@Test
	public void testConditionalExpressionEvaluator_true() {
		assertEquals(1, evaluateQuery("1=1?1:2"));
	}

	@Test
	public void testConditionalExpressionEvaluator_false() {
		assertEquals(2, evaluateQuery("1=2?1:2"));
	}

	/*
	 * FWRExpression
	 */

	@Test
	public void testFWRExpression_reportSet() {
		Set<?> ergSet = (Set<?>) evaluateQuery("from n:list(1..3) with true reportSet n end");
		assertEquals(3, ergSet.size());
		assertTrue(ergSet.contains(1));
		assertTrue(ergSet.contains(2));
		assertTrue(ergSet.contains(3));
	}

	@Test
	public void testFWRExpression_reportSetN() {
		Set<?> ergSet = (Set<?>) evaluateQuery("from n:list(10..100) with true reportSetN 10: n end");
		assertEquals(10, ergSet.size());
		for (int i = 10; i < 20; i++) {
			assertTrue(ergSet.contains(i));
		}
	}

	@Test
	public void testFWRExpression_reportList() {
		List<?> ergList = (List<?>) evaluateQuery("from n:list(1..3) with true reportList n end");
		assertEquals(3, ergList.size());
		assertEquals(1, ergList.get(0));
		assertEquals(2, ergList.get(1));
		assertEquals(3, ergList.get(2));
	}

	@Test
	public void testFWRExpression_reportListN() {
		List<?> ergList = (List<?>) evaluateQuery("from n:list(10..100) with true reportListN 10: n end");
		assertEquals(10, ergList.size());
		for (int i = 0; i < ergList.size(); i++) {
			assertEquals(i + 10, ergList.get(i));
		}
	}

	@Test
	public void testFWRExpression_reportMap() {
		Map<?, ?> ergMap = (Map<?, ?>) evaluateQuery("from n:list(1,2) with true reportMap n->getVertex(n) end");
		assertEquals(2, ergMap.size());
		assertEquals(datagraph.getVertex(1), ergMap.get(1));
		assertEquals(datagraph.getVertex(2), ergMap.get(2));
	}

	@Test
	public void testFWRExpression_reportMapN() {
		Map<?, ?> ergMap = (Map<?, ?>) evaluateQuery("from n:list(1..100) with true reportMapN 10: n->getVertex(n) end");
		assertEquals(10, ergMap.size());
		for (int i = 1; i <= 10; i++) {
			assertEquals(datagraph.getVertex(i), ergMap.get(i));
		}
	}

	@Test
	public void testFWRExpression_reportTable_oneNamedColumn() {
		Table<?> ergTable = (Table<?>) evaluateQuery("from n:list(1..3) report n as \"Column1\" end");
		assertEquals(3, ergTable.size());
		assertEquals(1, ergTable.get(0));
		assertEquals(2, ergTable.get(1));
		assertEquals(3, ergTable.get(2));
		PVector<String> titles = ergTable.getTitles();
		assertEquals(1, titles.size());
		assertEquals("Column1", titles.get(0));
	}

	@Test
	public void testFWRExpression_reportTable_twoNamedColumns() {
		Table<?> ergTable = (Table<?>) evaluateQuery("from x:list(1..3) report x as \"Column1\", x*x as \"Column2\" end");
		assertEquals(3, ergTable.size());
		for (int i = 0; i < ergTable.size(); i++) {
			Tuple ergTuple = (Tuple) ergTable.get(i);
			int x = i + 1;
			assertEquals(x, ergTuple.get(0));
			assertEquals(x * x, ergTuple.get(1));
		}
		PVector<String> titles = ergTable.getTitles();
		assertEquals(2, titles.size());
		assertEquals("Column1", titles.get(0));
		assertEquals("Column2", titles.get(1));
	}

	@Test
	public void testFWRExpression_reportTable_twoVariablesAndThreeColumns() {
		Table<?> ergTable = (Table<?>) evaluateQuery("from n,m:list(1..3) reportTable n,m,n*m end");
		assertEquals(3, ergTable.size());
		for (int i = 0; i < ergTable.size(); i++) {
			int n = i + 1;
			Tuple ergLine = (Tuple) ergTable.get(i);
			assertEquals(n, ergLine.get(0));
			for (int j = 1; j < ergLine.size(); j++) {
				int m = j;
				assertEquals("check result for " + n + "*" + m, n * m,
						ergLine.get(j));
			}
		}
		PVector<String> titles = ergTable.getTitles();
		assertEquals(4, titles.size());
		assertEquals("", titles.get(0));
		assertEquals("1", titles.get(1));
		assertEquals("2", titles.get(2));
		assertEquals("3", titles.get(3));
	}

	/*
	 * Missing FunctionApplication tests
	 */

	@Test
	public void testFunctionApplication_withEvaluatorParam() {
		assertTrue((Boolean) evaluateQuery("isReachable(getVertex(1),getVertex(2),<->*)"));
	}

	@Test
	public void testFunctionApplication_callSameFunctionSeveralTimes() {
		assertFalse((Boolean) evaluateQuery("and(isReachable(getVertex(1),getVertex(23),-->),isReachable(getVertex(1),getVertex(2),-->))"));
	}

}
