package de.uni_koblenz.jgralabtest.greql2.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluatorImpl;
import de.uni_koblenz.jgralab.greql2.evaluator.QueryImpl;
import de.uni_koblenz.jgralab.greql2.exception.GreqlException;
import de.uni_koblenz.jgralab.greql2.exception.UnknownTypeException;

public class PathExpressionTest {

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
	 * Tests for SimplePathDescription
	 */

	/**
	 * v19--&gt;v2
	 */
	@Test
	public void testSimplePathDescription() {
		assertTrue((Boolean) evaluateQuery("getVertex(19)-->getVertex(2)"));
	}

	/**
	 * v19&lt;--v2
	 */
	@Test
	public void testSimplePathDescription_inverseDirection() {
		assertFalse((Boolean) evaluateQuery("getVertex(19)<--getVertex(2)"));
	}

	/**
	 * v19&lt;-&gt;v2
	 */
	@Test
	public void testSimplePathDescription_BothDirections() {
		assertTrue((Boolean) evaluateQuery("getVertex(19)<->getVertex(2)"));
	}

	/**
	 * import connections.*;<br>
	 * v19&lt;-&gt;{Street}v2
	 */
	@Test
	public void testSimplePathDescription_BothDirectionsWithRestriction() {
		assertTrue((Boolean) evaluateQuery("import connections.*;\ngetVertex(19)<->{Street}getVertex(2)"));
	}

	/**
	 * import connections.*;<br>
	 * v19&lt;-&gt;{Street @thisEdge.name="A48"}v2
	 */
	@Test
	public void testSimplePathDescription_BothDirectionsWithRestrictionAndPredicate() {
		assertTrue((Boolean) evaluateQuery("import connections.*;\ngetVertex(19)<->{Street @thisEdge.name=\"A48\"}getVertex(2)"));
	}

	/**
	 * v143&lt;&gt;--v2
	 */
	@Test
	public void testSimplePathDescription_Aggregation() {
		assertTrue((Boolean) evaluateQuery("getVertex(143)<>--getVertex(153)"));
	}

	/**
	 * import localities.*;<br>
	 * v143&lt;&gt;--{ContainsLocality}v2
	 */
	@Test
	public void testSimplePathDescription_AggregationWithTypeRestriction() {
		assertTrue((Boolean) evaluateQuery("import localities.ContainsLocality;\ngetVertex(143)<>--{ContainsLocality}getVertex(153)"));
	}

	/**
	 * import localities.*;<br>
	 * v143&lt;&gt;--{ContainsLocality}v2
	 */
	@Test
	public void testSimplePathDescription_AggregationWithRoleRestriction() {
		assertTrue((Boolean) evaluateQuery("import localities.*;\ngetVertex(143)<>--{localities}getVertex(153)"));
	}

	/**
	 * import local.*;<br>
	 * v143&lt;&gt;--{ContainsLocality}v2
	 */
	@Test(expected = UnknownTypeException.class)
	public void testSimplePathDescription_WithUnknownPackage() {
		assertTrue((Boolean) evaluateQuery("import local.*;\ngetVertex(143)<>--{localities}getVertex(153)"));
	}

	/**
	 * import localities.*;<br>
	 * v143&lt;&gt;--{ContainsLocality}v2
	 */
	@Test(expected = UnknownTypeException.class)
	public void testSimplePathDescription_WithUnknownEdgeClass() {
		assertTrue((Boolean) evaluateQuery("import localities.ContainsLo;\ngetVertex(143)<>--{ContainsLocality}getVertex(153)"));
	}

	/*
	 * Tests for EdgePathDescription
	 */

	/**
	 * v19--e325-&gt;v2
	 */
	@Test
	public void testEdgePathDescription() {
		assertTrue((Boolean) evaluateQuery("getVertex(19)--getEdge(325)->getVertex(2)"));
	}

	/**
	 * v19--e1-&gt;v2
	 */
	@Test
	public void testEdgePathDescription_false() {
		assertFalse((Boolean) evaluateQuery("getVertex(19)--getEdge(1)->getVertex(2)"));
	}

	// /**
	// * v19--set(e1,e325)-&gt;v2
	// */
	// @Test
	// public void testEdgePathDescription_WithSetOfEdges() {
	// assertTrue((Boolean)
	// evaluateQuery("getVertex(19)--(set(getEdge(1),getEdge(325)))->getVertex(2)"));
	// }

	/*
	 * Tests of SequentialPathDescription
	 */

	/**
	 * Test of query:<br>
	 * v155--&gt;&lt;--v14
	 */
	@Test
	public void testSequentialPathDescription_sequenceLength2() {
		assertTrue((Boolean) evaluateQuery("getVertex(155)--><--getVertex(14)"));
	}

	/**
	 * Test of query:<br>
	 * v155--&gt;v17&lt;--v14
	 */
	@Test
	public void testSequentialPathDescription_withVertexInBetween() {
		assertTrue((Boolean) evaluateQuery("getVertex(155)-->getVertex(17)<--getVertex(14)"));
	}

	/**
	 * Test of query:<br>
	 * v155&lt;-&gt;&lt;-&gt;v13
	 */
	@Test
	public void testSequentialPathDescription_bidirectional() {
		assertFalse((Boolean) evaluateQuery("getVertex(155)<-><->getVertex(13)"));
	}

	/**
	 * Test of query:<br>
	 * v155&lt;-&gt;&lt;-&gt;&lt;-&gt;v13
	 */
	@Test
	public void testSequentialPathDescription_sequenceLength3() {
		assertTrue((Boolean) evaluateQuery("getVertex(155)<-><-><->getVertex(13)"));
	}

	/**
	 * Test of query:<br>
	 * v155&lt;&lt;&gt;---&gt;v6
	 */
	@Test
	public void testSequentialPathDescription_sequenceWithAggregation() {
		assertTrue((Boolean) evaluateQuery("getVertex(155)<>---->getVertex(6)"));
	}

	/*
	 * OptionalPathDescription
	 */

	/**
	 * Test of query:<br>
	 * v153[--&gt;]136
	 */
	@Test
	public void testOptionalPathDescription_edgeNeeded() {
		assertTrue((Boolean) evaluateQuery("getVertex(153)[-->]getVertex(136)"));
	}

	/**
	 * Test of query:<br>
	 * v153[--&gt;]153
	 */
	@Test
	public void testOptionalPathDescription_edgeNotNeeded() {
		assertTrue((Boolean) evaluateQuery("getVertex(153)[-->]getVertex(153)"));
	}

	/**
	 * Test of query:<br>
	 * v136[--&gt;]136
	 */
	@Test
	public void testOptionalPathDescription_optionalLoop() {
		assertTrue((Boolean) evaluateQuery("getVertex(136)[-->]getVertex(136)"));
	}

	/**
	 * Test of query:<br>
	 * v155[&lt;-&gt;]v13
	 */
	@Test
	public void testOptionalPathDescription_notReachable() {
		assertFalse((Boolean) evaluateQuery("getVertex(155)[<->]getVertex(13)"));
	}

	/*
	 * IteratedPathDescription
	 */

	@Test
	public void testIteratedPathDescription_Star_Reflexivity() {
		assertTrue((Boolean) evaluateQuery("getVertex(153)-->*getVertex(153)"));
	}

	@Test
	public void testIteratedPathDescription_Plus_ReflexivityWithoutLoop() {
		assertFalse((Boolean) evaluateQuery("getVertex(153)-->+getVertex(153)"));
	}

	@Test
	public void testIteratedPathDescription_Star_withLoop() {
		assertTrue((Boolean) evaluateQuery("getVertex(136)-->*getVertex(136)"));
	}

	@Test
	public void testIteratedPathDescription_Plus_withLoop() {
		assertTrue((Boolean) evaluateQuery("getVertex(136)-->+getVertex(136)"));
	}

	@Test
	public void testIteratedPathDescription_Plus_ReachableWithLoop() {
		assertTrue((Boolean) evaluateQuery("getVertex(136)<->+getVertex(153)"));
	}

	@Test
	public void testIteratedPathDescription_Star() {
		assertTrue((Boolean) evaluateQuery("getVertex(14)-->*getVertex(16)"));
	}

	@Test
	public void testIteratedPathDescription_Plus() {
		assertTrue((Boolean) evaluateQuery("getVertex(14)-->+getVertex(16)"));
	}

	@Test
	public void testIteratedPathDescription_Star_FailBecauseOfDirection() {
		assertFalse((Boolean) evaluateQuery("getVertex(14)<--*getVertex(16)"));
	}

	@Test
	public void testIteratedPathDescription_Plus_FailBecauseOfDirection() {
		assertFalse((Boolean) evaluateQuery("getVertex(14)<--+getVertex(16)"));
	}

	@Test(expected = GreqlException.class)
	public void testIteratedPathDescription_Exponent_Reflexivity() {
		evaluateQuery("getVertex(21)-->^0 getVertex(21)");
	}

	@Test
	public void testIteratedPathDescription_Exponent_TooShort() {
		assertFalse((Boolean) evaluateQuery("getVertex(21)-->^1 getVertex(13)"));
	}

	@Test
	public void testIteratedPathDescription_Exponent() {
		assertTrue((Boolean) evaluateQuery("getVertex(21)-->^2 getVertex(13)"));
	}

	@Test
	public void testIteratedPathDescription_Exponent_TooLong() {
		assertFalse((Boolean) evaluateQuery("getVertex(21)-->^3 getVertex(13)"));
	}

	@Test
	public void testIteratedPathDescription_Exponent_WrongDirection() {
		assertFalse((Boolean) evaluateQuery("getVertex(21)<--^2 getVertex(13)"));
	}

	@Test
	public void testIteratedPathDescription_Exponent_TwoPossibleWays_TooShort() {
		assertFalse((Boolean) evaluateQuery("getVertex(144)-->^1 getVertex(16)"));
	}

	@Test
	public void testIteratedPathDescription_Exponent_TwoPossibleWays_ShortMatch() {
		assertTrue((Boolean) evaluateQuery("getVertex(144)-->^2 getVertex(16)"));
	}

	@Test
	public void testIteratedPathDescription_Exponent_TwoPossibleWays_BetweenBoth() {
		assertFalse((Boolean) evaluateQuery("getVertex(144)-->^3 getVertex(16)"));
	}

	@Test
	public void testIteratedPathDescription_Exponent_TwoPossibleWays_LongMatch() {
		assertTrue((Boolean) evaluateQuery("getVertex(144)-->^4 getVertex(16)"));
	}

	@Test
	public void testIteratedPathDescription_Exponent_TwoPossibleWays_TooLong() {
		assertFalse((Boolean) evaluateQuery("getVertex(144)-->^5 getVertex(16)"));
	}

	@Test(expected = GreqlException.class)
	public void testIteratedPathDescription_Exponent_withLoop0() {
		evaluateQuery("getVertex(136)-->^0 getVertex(136)");
	}

	@Test
	public void testIteratedPathDescription_Exponent_withLoop1() {
		assertTrue((Boolean) evaluateQuery("getVertex(136)-->^1 getVertex(136)"));
	}

	@Test
	public void testIteratedPathDescription_Exponent_withLoop2() {
		assertTrue((Boolean) evaluateQuery("getVertex(136)-->^2 getVertex(136)"));
	}

	/*
	 * AlternativePathDescription
	 */

	@Test
	public void testAlternativePathDescription_BothFail() {
		assertFalse((Boolean) evaluateQuery("getVertex(144)-->^5 |-->^3 getVertex(16)"));
	}

	@Test
	public void testAlternativePathDescription_FirstSucceedsSecondFails() {
		assertTrue((Boolean) evaluateQuery("getVertex(144)-->^4 |-->^3 getVertex(16)"));
	}

	@Test
	public void testAlternativePathDescription_FirstFailsSecondSucceeds() {
		assertTrue((Boolean) evaluateQuery("getVertex(144)-->^5 |-->^2 getVertex(16)"));
	}

	@Test
	public void testAlternativePathDescription_BothSucceed() {
		assertTrue((Boolean) evaluateQuery("getVertex(144)-->^4 |-->^2 getVertex(16)"));
	}

	@Test
	public void testAlternativePathDescription_OfLength3() {
		assertTrue((Boolean) evaluateQuery("getVertex(144)-->^4 |-->^2 |<>-- getVertex(16)"));
	}

	/*
	 * GroupPathDescription
	 */

	@Test
	public void testGroupPathDescription_OneElement() {
		assertTrue((Boolean) evaluateQuery("getVertex(144)(-->^2) getVertex(16)"));
	}

	@Test
	public void testGroupPathDescription_SeveralElements() {
		assertTrue((Boolean) evaluateQuery("getVertex(144)(-->-->) getVertex(16)"));
	}

	@Test
	public void testGroupPathDescription_SeveralElements_OnlyOneInGroup() {
		assertTrue((Boolean) evaluateQuery("getVertex(144)-->(-->) getVertex(16)"));
	}

	@Test
	public void testGroupPathDescription_SeveralElements_TwoGroups() {
		assertTrue((Boolean) evaluateQuery("getVertex(144)(-->)(-->) getVertex(16)"));
	}

	@Test
	public void testGroupPathDescription_SeveralBrackets() {
		assertTrue((Boolean) evaluateQuery("getVertex(144)(((-->^2))) getVertex(16)"));
	}

	/*
	 * VertexRestriction
	 */

	@Test
	public void testVertexRestriction_StartVertex() {
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery("import localities.*;"
				+ "{County}&--> getVertex(154)");
		assertFalse(ergSet.isEmpty());
		assertEquals(1, ergSet.size());
		for (Vertex v : ergSet) {
			assertEquals(144, v.getId());
		}
	}

	@Test
	public void testVertexRestriction_StartVertex_SeveralVertexClasses() {
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery("import localities.*;"
				+ "{County,City}&--> getVertex(154)");
		assertFalse(ergSet.isEmpty());
		assertEquals(1, ergSet.size());
		for (Vertex v : ergSet) {
			assertEquals(144, v.getId());
		}
	}

	@Test
	public void testVertexRestriction_StartVertex_Predicate() {
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery("import localities.*;"
				+ "{County @ thisVertex.name=\"Hessen\"}&--> getVertex(154)");
		assertFalse(ergSet.isEmpty());
		assertEquals(1, ergSet.size());
		for (Vertex v : ergSet) {
			assertEquals(144, v.getId());
			assertEquals("Hessen", v.getAttribute("name"));
		}
	}

	@Test
	public void testVertexRestriction_EndVertex() {
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery("import localities.*;"
				+ "getVertex(144)--> &{City}");
		assertFalse(ergSet.isEmpty());
		assertEquals(1, ergSet.size());
		for (Vertex v : ergSet) {
			assertEquals(154, v.getId());
		}
	}

	@Test
	public void testVertexRestriction_EndVertex_SeveralVertexClasses() {
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery("import localities.*;"
				+ "getVertex(144)--> &{City,County}");
		assertFalse(ergSet.isEmpty());
		assertEquals(1, ergSet.size());
		for (Vertex v : ergSet) {
			assertEquals(154, v.getId());
		}
	}

	@Test
	public void testVertexRestriction_EndVertex_Predicate() {
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery("import localities.*;"
				+ "getVertex(144)--> &{City @ thisVertex.name=\"Frankfurt am Main\"}");
		assertFalse(ergSet.isEmpty());
		assertEquals(1, ergSet.size());
		for (Vertex v : ergSet) {
			assertEquals(154, v.getId());
			assertEquals("Frankfurt am Main", v.getAttribute("name"));
		}
	}
}
