package de.uni_koblenz.jgralabtest.greql.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pcollections.POrderedSet;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.exception.GreqlException;
import de.uni_koblenz.jgralab.greql.exception.ParsingException;
import de.uni_koblenz.jgralab.greql.exception.UnknownTypeException;
import de.uni_koblenz.jgralab.greql.executable.ExecutableQuery;
import de.uni_koblenz.jgralab.greql.executable.GreqlCodeGenerator;

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

	private void compareReachableVerticesResults(String query, String classname)
			throws InstantiationException, IllegalAccessException {
		@SuppressWarnings("unchecked")
		POrderedSet<Vertex> result1 = (POrderedSet<Vertex>) GreqlQuery
				.createQuery(query).evaluate(datagraph);
		assertNotNull(result1);

		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(), classname);
		@SuppressWarnings("unchecked")
		POrderedSet<Vertex> result2 = (POrderedSet<Vertex>) generatedQuery
				.newInstance().execute(datagraph);
		assertNotNull(result2);

		assertEquals(result1.size(), result2.size());
		for (int i = 0; i < result1.size(); i++) {
			assertEquals(result1.get(i), result2.get(i));
		}
	}

	private void compareReachabilityResults(String query,
			boolean expectedResult, String classname)
			throws InstantiationException, IllegalAccessException {
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		if (expectedResult) {
			assertTrue((Boolean) erg);
		} else {
			assertFalse((Boolean) erg);
		}

		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(), classname);
		erg = generatedQuery.newInstance().execute(datagraph);
		if (expectedResult) {
			assertTrue((Boolean) erg);
		} else {
			assertFalse((Boolean) erg);
		}
	}

	/*
	 * Tests for SimplePathDescription
	 */

	/**
	 * v19--&gt;v2
	 */
	@Test
	public void testSimplePathDescription() throws InstantiationException,
			IllegalAccessException {
		String classname = "testdata.TestSimplePathDescription";
		compareReachabilityResults("getVertex(19)-->getVertex(2)", true,
				classname);
		compareReachableVerticesResults("getVertex(19)-->", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(19),getVertex(2),-->)", true, classname
						+ "_3");
		compareReachableVerticesResults("reachableVertices(getVertex(19),-->)",
				classname + "_4");
	}

	/**
	 * v19&lt;--v2
	 */
	@Test
	public void testSimplePathDescription_inverseDirection()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestSimplePathDescription_inverseDirection";
		compareReachabilityResults("getVertex(19)<--getVertex(2)", false,
				classname);
		compareReachableVerticesResults("getVertex(19)<--", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(19),getVertex(2),<--)", false, classname
						+ "_3");
		compareReachableVerticesResults("reachableVertices(getVertex(19),<--)",
				classname + "_4");
	}

	/**
	 * v19&lt;-&gt;v2
	 */
	@Test
	public void testSimplePathDescription_BothDirections()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestSimplePathDescription_BothDirections";
		compareReachabilityResults("getVertex(19)<->getVertex(2)", true,
				classname);
		compareReachableVerticesResults("getVertex(19)<->", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(19),getVertex(2),<->)", true, classname
						+ "_3");
		compareReachableVerticesResults("reachableVertices(getVertex(19),<->)",
				classname + "_4");
	}

	/**
	 * import connections.*;<br>
	 * v19&lt;-&gt;{Street}v2
	 */
	@Test
	public void testSimplePathDescription_BothDirectionsWithRestriction()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestSimplePathDescription_BothDirectionsWithRestriction";
		compareReachabilityResults(
				"import connections.*;\ngetVertex(19)<->{Street}getVertex(2)",
				true, classname);
		compareReachableVerticesResults(
				"import connections.*;\ngetVertex(19)<->{Street}", classname
						+ "_2");
		compareReachabilityResults(
				"import connections.*;\nisReachable(getVertex(19),getVertex(2),<->{Street})",
				true, classname + "_3");
		compareReachableVerticesResults(
				"import connections.*;\nreachableVertices(getVertex(19),<->{Street})",
				classname + "_4");
	}

	/**
	 * import connections.*;<br>
	 * v19&lt;-&gt;{Street @thisEdge.name="A48"}v2
	 */
	@Test
	public void testSimplePathDescription_BothDirectionsWithRestrictionAndPredicate()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestSimplePathDescription_BothDirectionsWithRestrictionAndPredicate";
		compareReachabilityResults(
				"import connections.*;\ngetVertex(19)<->{Street @thisEdge.name=\"A48\"}getVertex(2)",
				true, classname);
		compareReachableVerticesResults(
				"import connections.*;\ngetVertex(19)<->{Street @thisEdge.name=\"A48\"}",
				classname + "_2");
		compareReachabilityResults(
				"import connections.*;\nisReachable(getVertex(19),getVertex(2),<->{Street @thisEdge.name=\"A48\"})",
				true, classname + "_3");
		compareReachableVerticesResults(
				"import connections.*;\nreachableVertices(getVertex(19),<->{Street @thisEdge.name=\"A48\"})",
				classname + "_4");
	}

	/**
	 * v143&lt;&gt;--v2
	 */
	@Test
	public void testSimplePathDescription_Aggregation()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestSimplePathDescription_Aggregation";
		compareReachabilityResults("getVertex(143)<>--getVertex(153)", true,
				classname);
		compareReachableVerticesResults("getVertex(143)<>--", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(143),getVertex(153),<>--)", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(143),<>--)", classname + "_4");
	}

	/**
	 * import localities.*;<br>
	 * v143&lt;&gt;--{ContainsLocality}v2
	 */
	@Test
	public void testSimplePathDescription_AggregationWithTypeRestriction()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestSimplePathDescription_AggregationWithTypeRestriction";
		compareReachabilityResults(
				"import localities.ContainsLocality;\ngetVertex(143)<>--{ContainsLocality}getVertex(153)",
				true, classname);
		compareReachableVerticesResults(
				"import localities.ContainsLocality;\ngetVertex(143)<>--{ContainsLocality}",
				classname + "_2");
		compareReachabilityResults(
				"import localities.ContainsLocality;\nisReachable(getVertex(143),getVertex(153),<>--{ContainsLocality})",
				true, classname + "_3");
		compareReachableVerticesResults(
				"import localities.ContainsLocality;\nreachableVertices(getVertex(143),<>--{ContainsLocality})",
				classname + "_4");
	}

	/**
	 * import localities.*;<br>
	 * v143&lt;&gt;--{ContainsLocality}v2
	 */
	@Test
	public void testSimplePathDescription_AggregationWithRoleRestriction()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestSimplePathDescription_AggregationWithRoleRestriction";
		compareReachabilityResults(
				"import localities.*;\ngetVertex(143)<>--{localities}getVertex(153)",
				true, classname);
		compareReachableVerticesResults(
				"import localities.*;\ngetVertex(143)<>--{localities}",
				classname + "_2");
		compareReachabilityResults(
				"import localities.*;\nisReachable(getVertex(143),getVertex(153),<>--{localities})",
				true, classname + "_3");
		compareReachableVerticesResults(
				"import localities.*;\nreachableVertices(getVertex(143),<>--{localities})",
				classname + "_4");
	}

	/**
	 * import localities.*;<br>
	 * v143&lt;&gt;--{ContainsLocality}v2
	 */
	@Test
	public void testSimplePathDescription_AggregationWithRoleRestriction_fails()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestSimplePathDescription_AggregationWithRoleRestriction_fails";
		compareReachabilityResults(
				"import localities.*;\ngetVertex(143)<>--{locality}getVertex(153)",
				false, classname);
		compareReachableVerticesResults(
				"import localities.*;\ngetVertex(143)<>--{locality}", classname
						+ "_2");
		compareReachabilityResults(
				"import localities.*;\nisReachable(getVertex(143),getVertex(153),<>--{locality})",
				false, classname + "_3");
		compareReachableVerticesResults(
				"import localities.*;\nreachableVertices(getVertex(143),<>--{locality})",
				classname + "_4");
	}

	/**
	 * import local.*;<br>
	 * v143&lt;&gt;--{ContainsLocality}v2
	 */
	@Test(expected = UnknownTypeException.class)
	public void testSimplePathDescription_WithUnknownPackage() {
		String query = "import local.*;\ngetVertex(143)<>--{localities}getVertex(153)";
		GreqlQuery.createQuery(query).evaluate(datagraph);
	}

	/**
	 * import local.*;<br>
	 * v143&lt;&gt;--{ContainsLocality}v2
	 */
	@Test(expected = UnknownTypeException.class)
	public void testSimplePathDescription_WithUnknownPackage_Generated()
			throws InstantiationException, IllegalAccessException {
		String query = "import local.*;\ngetVertex(143)<>--{localities}getVertex(153)";

		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestSimplePathDescription_WithUnknownPackage_Generated");
		generatedQuery.newInstance().execute(datagraph);
	}

	/**
	 * import localities.*;<br>
	 * v143&lt;&gt;--{ContainsLocality}v2
	 */
	@Test(expected = UnknownTypeException.class)
	public void testSimplePathDescription_WithUnknownEdgeClass() {
		String query = "import localities.ContainsLo;\ngetVertex(143)<>--{ContainsLocality}getVertex(153)";
		GreqlQuery.createQuery(query).evaluate(datagraph);
	}

	/**
	 * import localities.*;<br>
	 * v143&lt;&gt;--{ContainsLocality}v2
	 */
	@Test(expected = UnknownTypeException.class)
	public void testSimplePathDescription_WithUnknownEdgeClass_Generated()
			throws InstantiationException, IllegalAccessException {
		String query = "import localities.ContainsLo;\ngetVertex(143)<>--{ContainsLocality}getVertex(153)";

		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestSimplePathDescription_WithUnknownEdgeClass_Generated");
		generatedQuery.newInstance().execute(datagraph);
	}

	/*
	 * Tests for EdgePathDescription
	 */

	/**
	 * v19--e325-&gt;v2
	 */
	@Test
	public void testEdgePathDescription() throws InstantiationException,
			IllegalAccessException {
		String classname = "testdata.TestEdgePathDescription";
		compareReachabilityResults("getVertex(19)--getEdge(325)->getVertex(2)",
				true, classname);
		compareReachableVerticesResults("getVertex(19)--getEdge(325)->",
				classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(19),getVertex(2),--getEdge(325)->)",
				true, classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(19),--getEdge(325)->)", classname
						+ "_4");
	}

	// TODO probably fix GReQL parser, the following query is correct but it can
	// not be parsed
	@Test(expected = ParsingException.class)
	public void testEdgePathDescription_withRestriction()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestEdgePathDescription_withRestriction";
		compareReachabilityResults(
				"getVertex(19) --getEdge(325)->{@ thisEdge=getEdge(325)} getVertex(2)",
				true, classname);
		compareReachableVerticesResults(
				"getVertex(19) --getEdge(325)->{@ thisEdge=getEdge(325)}",
				classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(19), getVertex(2),  --getEdge(325)->{@ thisEdge=getEdge(325)})",
				true, classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(19), --getEdge(325)->{@ thisEdge=getEdge(325)})",
				classname + "_4");
	}

	/**
	 * v19--e1-&gt;v2
	 */
	@Test
	public void testEdgePathDescription_false() throws InstantiationException,
			IllegalAccessException {
		String classname = "testdata.TestEdgePathDescription_false";
		compareReachabilityResults("getVertex(19)--getEdge(1)->getVertex(2)",
				false, classname);
		compareReachableVerticesResults("getVertex(19)--getEdge(1)->",
				classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(19),getVertex(2),--getEdge(1)->)",
				false, classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(19),--getEdge(1)->)", classname
						+ "_4");
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
	public void testSequentialPathDescription_sequenceLength2()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestSequentialPathDescription_sequenceLength2";
		compareReachabilityResults("getVertex(155)--><--getVertex(14)", true,
				classname);
		compareReachableVerticesResults("getVertex(155)--><--", classname
				+ "_2");
		compareReachabilityResults(
				"isReachable(getVertex(155),getVertex(14),--><--)", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(155),--><--)", classname + "_4");
	}

	/**
	 * Test of query:<br>
	 * v155--&gt;v17&lt;--v14
	 */
	@Test
	public void testSequentialPathDescription_withVertexInBetween()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestSequentialPathDescription_withVertexInBetween";
		compareReachabilityResults(
				"getVertex(155)-->getVertex(17)<--getVertex(14)", true,
				classname);
		compareReachableVerticesResults("getVertex(155)-->getVertex(17)<--",
				classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(155),getVertex(14),-->getVertex(17)<--)",
				true, classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(155),-->getVertex(17)<--)",
				classname + "_4");
	}

	/**
	 * Test of query:<br>
	 * v155&lt;-&gt;&lt;-&gt;v13
	 */
	@Test
	public void testSequentialPathDescription_bidirectional()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestSequentialPathDescription_bidirectional";
		compareReachabilityResults("getVertex(155)<-><->getVertex(13)", false,
				classname);
		compareReachableVerticesResults("getVertex(155)<-><->", classname
				+ "_2");
		compareReachabilityResults(
				"isReachable(getVertex(155),getVertex(13),<-><->)", false,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(155),<-><->)", classname + "_4");
	}

	/**
	 * Test of query:<br>
	 * v155&lt;-&gt;&lt;-&gt;&lt;-&gt;v13
	 */
	@Test
	public void testSequentialPathDescription_sequenceLength3()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestSequentialPathDescription_sequenceLength3";
		compareReachabilityResults("getVertex(155)<-><-><->getVertex(13)",
				true, classname);
		compareReachableVerticesResults("getVertex(155)<-><-><->", classname
				+ "_2");
		compareReachabilityResults(
				"isReachable(getVertex(155),getVertex(13),<-><-><->)", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(155),<-><-><->)", classname + "_4");
	}

	/**
	 * Test of query:<br>
	 * v155&lt;&lt;&gt;---&gt;v6
	 */
	@Test
	public void testSequentialPathDescription_sequenceWithAggregation()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestSequentialPathDescription_sequenceWithAggregation";
		compareReachabilityResults("getVertex(155)<>---->getVertex(6)", true,
				classname);
		compareReachableVerticesResults("getVertex(155)<>---->", classname
				+ "_2");
		compareReachabilityResults(
				"isReachable(getVertex(155),getVertex(6),<>---->)", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(155),<>---->)", classname + "_4");
	}

	/*
	 * OptionalPathDescription
	 */

	/**
	 * Test of query:<br>
	 * v153[--&gt;]136
	 */
	@Test
	public void testOptionalPathDescription_edgeNeeded()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestOptionalPathDescription_edgeNeeded";
		compareReachabilityResults("getVertex(153)[-->]getVertex(136)", true,
				classname);
		compareReachableVerticesResults("getVertex(153)[-->]", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(153),getVertex(136),[-->])", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(153),[-->])", classname + "_4");
	}

	/**
	 * Test of query:<br>
	 * v153[--&gt;]153
	 */
	@Test
	public void testOptionalPathDescription_edgeNotNeeded()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestOptionalPathDescription_edgeNotNeeded";
		compareReachabilityResults("getVertex(153)[-->]getVertex(153)", true,
				classname);
		compareReachableVerticesResults("getVertex(153)[-->]", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(153),getVertex(153),[-->])", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(153),[-->])", classname + "_4");
	}

	/**
	 * Test of query:<br>
	 * v136[--&gt;]136
	 */
	@Test
	public void testOptionalPathDescription_optionalLoop()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestOptionalPathDescription_optionalLoop";
		compareReachabilityResults("getVertex(136)[-->]getVertex(136)", true,
				classname);
		compareReachableVerticesResults("getVertex(136)[-->]", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(136),getVertex(136),[-->])", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(136),[-->])", classname + "_4");
	}

	/**
	 * Test of query:<br>
	 * v155[&lt;-&gt;]v13
	 */
	@Test
	public void testOptionalPathDescription_notReachable()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestOptionalPathDescription_notReachable";
		compareReachabilityResults("getVertex(155)[<->]getVertex(13)", false,
				classname);
		compareReachableVerticesResults("getVertex(155)[<->]", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(155),getVertex(13),[<->])", false,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(155),[<->])", classname + "_4");
	}

	/*
	 * IteratedPathDescription
	 */

	@Test
	public void testIteratedPathDescription_Star_Reflexivity()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestIteratedPathDescription_Star_Reflexivity";
		compareReachabilityResults("getVertex(153)-->*getVertex(153)", true,
				classname);
		compareReachableVerticesResults("getVertex(153)-->*", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(153),getVertex(153),-->*)", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(153),-->*)", classname + "_4");
	}

	@Test
	public void testIteratedPathDescription_Plus_ReflexivityWithoutLoop()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestIteratedPathDescription_Plus_ReflexivityWithoutLoop";
		compareReachabilityResults("getVertex(153)-->+getVertex(153)", false,
				classname);
		compareReachableVerticesResults("getVertex(153)-->+", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(153),getVertex(153),-->+)", false,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(153),-->+)", classname + "_4");
	}

	@Test
	public void testIteratedPathDescription_Star_withLoop()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestIteratedPathDescription_Star_withLoop";
		compareReachabilityResults("getVertex(136)-->*getVertex(136)", true,
				classname);
		compareReachableVerticesResults("getVertex(136)-->*", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(136),getVertex(136),-->*)", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(136),-->*)", classname + "_4");
	}

	@Test
	public void testIteratedPathDescription_Plus_withLoop()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestIteratedPathDescription_Plus_withLoop";
		compareReachabilityResults("getVertex(136)-->+getVertex(136)", true,
				classname);
		compareReachableVerticesResults("getVertex(136)-->+", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(136),getVertex(136),-->+)", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(136),-->+)", classname + "_4");
	}

	@Test
	public void testIteratedPathDescription_Plus_ReachableWithLoop()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestIteratedPathDescription_Plus_ReachableWithLoop";
		compareReachabilityResults("getVertex(136)<->+getVertex(153)", true,
				classname);
		compareReachableVerticesResults("getVertex(136)<->+", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(136),getVertex(153),<->+)", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(136),<->+)", classname + "_4");
	}

	@Test
	public void testIteratedPathDescription_Star()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestIteratedPathDescription_Star";
		compareReachabilityResults("getVertex(14)-->*getVertex(16)", true,
				classname);
		compareReachableVerticesResults("getVertex(14)-->*", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(14),getVertex(16),-->*)", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(14),-->*)", classname + "_4");
	}

	@Test
	public void testIteratedPathDescription_Plus()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestIteratedPathDescription_Plus";
		compareReachabilityResults("getVertex(14)-->+getVertex(16)", true,
				classname);
		compareReachableVerticesResults("getVertex(14)-->+", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(14),getVertex(16),-->+)", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(14),-->+)", classname + "_4");
	}

	@Test
	public void testIteratedPathDescription_Star_FailBecauseOfDirection()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestIteratedPathDescription_Star_FailBecauseOfDirection";
		compareReachabilityResults("getVertex(14)<--*getVertex(16)", false,
				classname);
		compareReachableVerticesResults("getVertex(14)<--*", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(14),getVertex(16),<--*)", false,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(14),<--*)", classname + "_4");
	}

	@Test
	public void testIteratedPathDescription_Plus_FailBecauseOfDirection()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestIteratedPathDescription_Plus_FailBecauseOfDirection";
		compareReachabilityResults("getVertex(14)<--+getVertex(16)", false,
				classname);
		compareReachableVerticesResults("getVertex(14)<--+", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(14),getVertex(16),<--+)", false,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(14),<--+)", classname + "_4");
	}

	@Test(expected = GreqlException.class)
	public void testIteratedPathDescription_Exponent_Reflexivity() {
		String query = "getVertex(21)-->^0 getVertex(21)";
		GreqlQuery.createQuery(query).evaluate(datagraph);
	}

	@Test(expected = GreqlException.class)
	public void testIteratedPathDescription_Exponent_Reflexivity_Generated()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(21)-->^0 getVertex(21)";
		String classname = "testdata.TestIteratedPathDescription_Exponent_Reflexivity_Generated";
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(), classname);
		generatedQuery.newInstance().execute(datagraph);
	}

	@Test
	public void testIteratedPathDescription_Exponent_TooShort()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestIteratedPathDescription_Exponent_TooShort";
		compareReachabilityResults("getVertex(21)-->^1 getVertex(13)", false,
				classname);
		compareReachableVerticesResults("getVertex(21)-->^1", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(21),getVertex(13),-->^1 )", false,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(21),-->^1)", classname + "_4");
	}

	@Test
	public void testIteratedPathDescription_Exponent()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestIteratedPathDescription_Exponent";
		compareReachabilityResults("getVertex(21)-->^2 getVertex(13)", true,
				classname);
		compareReachableVerticesResults("getVertex(21)-->^2", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(21),getVertex(13),-->^2 )", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(21),-->^2)", classname + "_4");
	}

	@Test
	public void testIteratedPathDescription_Exponent_TooLong()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestIteratedPathDescription_Exponent_TooLong";
		compareReachabilityResults("getVertex(21)-->^3 getVertex(13)", false,
				classname);
		compareReachableVerticesResults("getVertex(21)-->^3", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(21),getVertex(13),-->^3 )", false,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(21),-->^3)", classname + "_4");
	}

	@Test
	public void testIteratedPathDescription_Exponent_WrongDirection()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestIteratedPathDescription_Exponent_WrongDirection";
		compareReachabilityResults("getVertex(21)<--^2 getVertex(13)", false,
				classname);
		compareReachableVerticesResults("getVertex(21)<--^2", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(21),getVertex(13),<--^2 )", false,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(21),<--^2)", classname + "_4");
	}

	@Test
	public void testIteratedPathDescription_Exponent_TwoPossibleWays_TooShort()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestIteratedPathDescription_Exponent_TwoPossibleWays_TooShort";
		compareReachabilityResults("getVertex(144)-->^1 getVertex(16)", false,
				classname);
		compareReachableVerticesResults("getVertex(144)-->^1", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(144),getVertex(16),-->^1 )", false,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(144),-->^1)", classname + "_4");
	}

	@Test
	public void testIteratedPathDescription_Exponent_TwoPossibleWays_ShortMatch()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestIteratedPathDescription_Exponent_TwoPossibleWays_ShortMatch";
		compareReachabilityResults("getVertex(144)-->^2 getVertex(16)", true,
				classname);
		compareReachableVerticesResults("getVertex(144)-->^2", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(144),getVertex(16),-->^2 )", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(144),-->^2)", classname + "_4");
	}

	@Test
	public void testIteratedPathDescription_Exponent_TwoPossibleWays_BetweenBoth()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestIteratedPathDescription_Exponent_TwoPossibleWays_BetweenBoth";
		compareReachabilityResults("getVertex(144)-->^3 getVertex(16)", false,
				classname);
		compareReachableVerticesResults("getVertex(144)-->^3", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(144),getVertex(16),-->^3 )", false,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(144),-->^3)", classname + "_4");
	}

	@Test
	public void testIteratedPathDescription_Exponent_TwoPossibleWays_LongMatch()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestIteratedPathDescription_Exponent_TwoPossibleWays_LongMatch";
		compareReachabilityResults("getVertex(144)-->^4 getVertex(16)", true,
				classname);
		compareReachableVerticesResults("getVertex(144)-->^4", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(144),getVertex(16),-->^4 )", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(144),-->^4)", classname + "_4");
	}

	@Test
	public void testIteratedPathDescription_Exponent_TwoPossibleWays_TooLong()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestIteratedPathDescription_Exponent_TwoPossibleWays_TooLong";
		compareReachabilityResults("getVertex(144)-->^5 getVertex(16)", false,
				classname);
		compareReachableVerticesResults("getVertex(144)-->^5", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(144),getVertex(16),-->^5 )", false,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(144),-->^5)", classname + "_4");
	}

	@Test(expected = GreqlException.class)
	public void testIteratedPathDescription_Exponent_withLoop0() {
		String query = "getVertex(136)-->^0 getVertex(136)";
		GreqlQuery.createQuery(query).evaluate(datagraph);
	}

	@Test(expected = GreqlException.class)
	public void testIteratedPathDescription_Exponent_withLoop0_Generated()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(136)-->^0 getVertex(136)";
		String classname = "testdata.TestIteratedPathDescription_Exponent_withLoop0_Generated";
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(), classname);
		generatedQuery.newInstance().execute(datagraph);
	}

	@Test
	public void testIteratedPathDescription_Exponent_withLoop1()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestIteratedPathDescription_Exponent_withLoop1";
		compareReachabilityResults("getVertex(136)-->^1 getVertex(136)", true,
				classname);
		compareReachableVerticesResults("getVertex(136)-->^1", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(136),getVertex(136),-->^1 )", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(136),-->^1)", classname + "_4");
	}

	@Test
	public void testIteratedPathDescription_Exponent_withLoop2()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestIteratedPathDescription_Exponent_withLoop2";
		compareReachabilityResults("getVertex(136)-->^2 getVertex(136)", true,
				classname);
		compareReachableVerticesResults("getVertex(136)-->^2", classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(136),getVertex(136),-->^2 )", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(136),-->^2)", classname + "_4");
	}

	/*
	 * AlternativePathDescription
	 */

	@Test
	public void testAlternativePathDescription_BothFail()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestAlternativePathDescription_BothFail";
		compareReachabilityResults("getVertex(144)-->^5 |-->^3 getVertex(16)",
				false, classname);
		compareReachableVerticesResults("getVertex(144)-->^5 |-->^3", classname
				+ "_2");
		compareReachabilityResults(
				"isReachable(getVertex(144),getVertex(16),-->^5 |-->^3 )",
				false, classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(144),-->^5 |-->^3)", classname
						+ "_4");
	}

	@Test
	public void testAlternativePathDescription_FirstSucceedsSecondFails()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestAlternativePathDescription_FirstSucceedsSecondFails";
		compareReachabilityResults("getVertex(144)-->^4 |-->^3 getVertex(16)",
				true, classname);
		compareReachableVerticesResults("getVertex(144)-->^4 |-->^3", classname
				+ "_2");
		compareReachabilityResults(
				"isReachable(getVertex(144),getVertex(16),-->^4 |-->^3 )",
				true, classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(144),-->^4 |-->^3)", classname
						+ "_4");
	}

	@Test
	public void testAlternativePathDescription_FirstFailsSecondSucceeds()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestAlternativePathDescription_FirstFailsSecondSucceeds";
		compareReachabilityResults("getVertex(144)-->^5 |-->^2 getVertex(16)",
				true, classname);
		compareReachableVerticesResults("getVertex(144)-->^5 |-->^2", classname
				+ "_2");
		compareReachabilityResults(
				"isReachable(getVertex(144),getVertex(16),-->^5 |-->^2 )",
				true, classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(144),-->^5 |-->^2)", classname
						+ "_4");
	}

	@Test
	public void testAlternativePathDescription_BothSucceed()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestAlternativePathDescription_BothSucceed";
		compareReachabilityResults("getVertex(144)-->^4 |-->^2 getVertex(16)",
				true, classname);
		compareReachableVerticesResults("getVertex(144)-->^4 |-->^2", classname
				+ "_2");
		compareReachabilityResults(
				"isReachable(getVertex(144),getVertex(16),-->^4 |-->^2 )",
				true, classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(144),-->^4 |-->^2)", classname
						+ "_4");
	}

	@Test
	public void testAlternativePathDescription_OfLength3()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestAlternativePathDescription_OfLength3";
		compareReachabilityResults(
				"getVertex(144)-->^4 |-->^2 |<>-- getVertex(16)", true,
				classname);
		compareReachableVerticesResults("getVertex(144)-->^4 |-->^2 |<>--",
				classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(144),getVertex(16),-->^4 |-->^2 |<>-- )",
				true, classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(144),-->^4 |-->^2 |<>--)",
				classname + "_4");
	}

	/*
	 * GroupPathDescription
	 */

	@Test
	public void testGroupPathDescription_OneElement()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestGroupPathDescription_OneElement";
		compareReachabilityResults("getVertex(144)(-->^2) getVertex(16)", true,
				classname);
		compareReachableVerticesResults("getVertex(144)(-->^2)", classname
				+ "_2");
		compareReachabilityResults(
				"isReachable(getVertex(144),getVertex(16),(-->^2) )", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(144),(-->^2))", classname + "_4");
	}

	@Test
	public void testGroupPathDescription_SeveralElements()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestGroupPathDescription_SeveralElements";
		compareReachabilityResults("getVertex(144)(-->-->) getVertex(16)",
				true, classname);
		compareReachableVerticesResults("getVertex(144)(-->-->)", classname
				+ "_2");
		compareReachabilityResults(
				"isReachable(getVertex(144),getVertex(16),(-->-->) )", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(144),(-->-->))", classname + "_4");
	}

	@Test
	public void testGroupPathDescription_SeveralElements_OnlyOneInGroup()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestGroupPathDescription_SeveralElements_OnlyOneInGroup";
		compareReachabilityResults("getVertex(144)-->(-->) getVertex(16)",
				true, classname);
		compareReachableVerticesResults("getVertex(144)-->(-->)", classname
				+ "_2");
		compareReachabilityResults(
				"isReachable(getVertex(144),getVertex(16),-->(-->) )", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(144),-->(-->))", classname + "_4");
	}

	@Test
	public void testGroupPathDescription_SeveralElements_TwoGroups()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestGroupPathDescription_SeveralElements_TwoGroups";
		compareReachabilityResults("getVertex(144)(-->)(-->) getVertex(16)",
				true, classname);
		compareReachableVerticesResults("getVertex(144)(-->)(-->)", classname
				+ "_2");
		compareReachabilityResults(
				"isReachable(getVertex(144),getVertex(16),(-->)(-->) )", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(144),(-->)(-->))", classname
						+ "_4");
	}

	@Test
	public void testGroupPathDescription_SeveralBrackets()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestGroupPathDescription_SeveralBrackets";
		compareReachabilityResults("getVertex(144)(((-->^2))) getVertex(16)",
				true, classname);
		compareReachableVerticesResults("getVertex(144)(((-->^2)))", classname
				+ "_2");
		compareReachabilityResults(
				"isReachable(getVertex(144),getVertex(16),(((-->^2))) )", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(144),(((-->^2))))", classname
						+ "_4");
	}

	/*
	 * TransposedPathDescription
	 */

	@Test
	public void testTransposedPathDescription_OneEdge_false()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestTransposedPathDescription_OneEdge_false";
		compareReachabilityResults("getVertex(144) -->^-1 getVertex(154)",
				false, classname);
		compareReachableVerticesResults("getVertex(144) -->^-1", classname
				+ "_2");
		compareReachabilityResults(
				"isReachable(getVertex(144),getVertex(154), -->^-1 )", false,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(144), -->^-1)", classname + "_4");
	}

	@Test
	public void testTransposedPathDescription_OneEdge_true()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestTransposedPathDescription_OneEdge_true";
		compareReachabilityResults("getVertex(144) (<--)^-1 getVertex(154)",
				true, classname);
		compareReachableVerticesResults("getVertex(144) (<--)^-1", classname
				+ "_2");
		compareReachabilityResults(
				"isReachable(getVertex(144),getVertex(154), (<--)^-1 )", true,
				classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(144), (<--)^-1)", classname + "_4");
	}

	@Test
	public void testTransposedPathDescription_SeveralEdges()
			throws InstantiationException, IllegalAccessException {
		String classname = "testdata.TestTransposedPathDescription_SeveralEdges";
		compareReachabilityResults("getVertex(144)(<--<--^3)^-1 getVertex(16)",
				true, classname);
		compareReachableVerticesResults("getVertex(144)(<--<--^3)^-1",
				classname + "_2");
		compareReachabilityResults(
				"isReachable(getVertex(144),getVertex(16),(<--<--^3)^-1 )",
				true, classname + "_3");
		compareReachableVerticesResults(
				"reachableVertices(getVertex(144),(<--<--^3)^-1)", classname
						+ "_4");
	}

	/*
	 * VertexRestriction
	 */

	public void checkResultSet(Object set, int id, String attributeName,
			Object attributeValue) {
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) set;
		assertFalse(ergSet.isEmpty());
		assertEquals(1, ergSet.size());
		for (Vertex v : ergSet) {
			assertEquals(id, v.getId());
			if (attributeName != null) {
				assertEquals(attributeValue, v.getAttribute(attributeName));
			}
		}
	}

	@Test
	public void testVertexRestriction_StartVertex()
			throws InstantiationException, IllegalAccessException {
		String query = "import localities.*;" + "{County}&--> getVertex(154)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		checkResultSet(erg, 144, null, null);

		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestVertexRestriction_StartVertex");
		erg = generatedQuery.newInstance().execute(datagraph);
		checkResultSet(erg, 144, null, null);
	}

	@Test
	public void testVertexRestriction_StartVertex_SeveralVertexClasses()
			throws InstantiationException, IllegalAccessException {
		String query = "import localities.*;"
				+ "{County,City}&--> getVertex(154)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		checkResultSet(erg, 144, null, null);

		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestVertexRestriction_StartVertex_SeveralVertexClasses");
		erg = generatedQuery.newInstance().execute(datagraph);
		checkResultSet(erg, 144, null, null);
	}

	@Test
	public void testVertexRestriction_StartVertex_Predicate()
			throws InstantiationException, IllegalAccessException {
		String query = "import localities.*;"
				+ "{County @ thisVertex.name=\"Hessen\"}&--> getVertex(154)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		checkResultSet(erg, 144, "name", "Hessen");

		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestVertexRestriction_StartVertex_Predicate");
		erg = generatedQuery.newInstance().execute(datagraph);
		checkResultSet(erg, 144, "name", "Hessen");
	}

	@Test
	public void testVertexRestriction_EndVertex()
			throws InstantiationException, IllegalAccessException {
		String query = "import localities.*;" + "getVertex(144)--> &{City}";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		checkResultSet(erg, 154, null, null);

		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestVertexRestriction_EndVertex");
		erg = generatedQuery.newInstance().execute(datagraph);
		checkResultSet(erg, 154, null, null);
	}

	@Test
	public void testVertexRestriction_EndVertex_SeveralVertexClasses()
			throws InstantiationException, IllegalAccessException {
		String query = "import localities.*;"
				+ "getVertex(144)--> &{City,County}";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		checkResultSet(erg, 154, null, null);

		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestVertexRestriction_EndVertex_SeveralVertexClasses");
		erg = generatedQuery.newInstance().execute(datagraph);
		checkResultSet(erg, 154, null, null);
	}

	@Test
	public void testVertexRestriction_EndVertex_Predicate()
			throws InstantiationException, IllegalAccessException {
		String query = "import localities.*;"
				+ "getVertex(144)--> &{City @ thisVertex.name=\"Frankfurt am Main\"}";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		checkResultSet(erg, 154, "name", "Frankfurt am Main");

		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestVertexRestriction_EndVertex_Predicate");
		erg = generatedQuery.newInstance().execute(datagraph);
		checkResultSet(erg, 154, "name", "Frankfurt am Main");
	}
}
