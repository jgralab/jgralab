package de.uni_koblenz.jgralabtest.greql.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.exception.GreqlException;
import de.uni_koblenz.jgralab.greql.exception.ParsingException;
import de.uni_koblenz.jgralab.greql.exception.UnknownTypeException;
import de.uni_koblenz.jgralab.greql.executable.ExecutableQuery;
import de.uni_koblenz.jgralab.greql.executable.GreqlCodeGenerator;
import de.uni_koblenz.jgralab.greql.types.PathSystem;

public class PathSystemTest {

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

	private void compareResultsOfQuery(String query, String classname)
			throws InstantiationException, IllegalAccessException {
		PathSystem result1 = (PathSystem) GreqlQuery.createQuery(query)
				.evaluate(datagraph);
		assertNotNull(result1);

		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(), classname);
		PathSystem result2 = (PathSystem) generatedQuery.newInstance().execute(
				datagraph);
		assertNotNull(result2);

		assertEquals(result1, result2);
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
		compareResultsOfQuery("pathSystem(getVertex(19),-->)",
				"testdata.TestSimplePathDescription");
	}

	/**
	 * v19&lt;--v2
	 */
	@Test
	public void testSimplePathDescription_inverseDirection()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(19),<--)",
				"testdata.TestSimplePathDescription_inverseDirection");
	}

	/**
	 * v19&lt;-&gt;v2
	 */
	@Test
	public void testSimplePathDescription_BothDirections()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(19),<->)",
				"testdata.TestSimplePathDescription_BothDirections");
	}

	/**
	 * import connections.*;<br>
	 * v19&lt;-&gt;{Street}v2
	 */
	@Test
	public void testSimplePathDescription_BothDirectionsWithRestriction()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery(
				"import connections.*;\npathSystem(getVertex(19),<->{Street})",
				"testdata.TestSimplePathDescription_BothDirectionsWithRestriction");
	}

	/**
	 * import connections.*;<br>
	 * v19&lt;-&gt;{Street @thisEdge.name="A48"}v2
	 */
	@Test
	public void testSimplePathDescription_BothDirectionsWithRestrictionAndPredicate()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery(
				"import connections.*;\npathSystem(getVertex(19),<->{Street @thisEdge.name=\"A48\"})",
				"testdata.TestSimplePathDescription_BothDirectionsWithRestrictionAndPredicate");
	}

	/**
	 * v143&lt;&gt;--v2
	 */
	@Test
	public void testSimplePathDescription_Aggregation()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(143),<>--)",
				"testdata.TestSimplePathDescription_Aggregation");
	}

	/**
	 * import localities.*;<br>
	 * v143&lt;&gt;--{ContainsLocality}v2
	 */
	@Test
	public void testSimplePathDescription_AggregationWithTypeRestriction()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery(
				"import localities.ContainsLocality;\npathSystem(getVertex(143),<>--{ContainsLocality})",
				"testdata.TestSimplePathDescription_AggregationWithTypeRestriction");
	}

	/**
	 * import localities.*;<br>
	 * v143&lt;&gt;--{ContainsLocality}v2
	 */
	@Test
	public void testSimplePathDescription_AggregationWithRoleRestriction()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery(
				"import localities.*;\npathSystem(getVertex(143),<>--{localities})",
				"testdata.TestSimplePathDescription_AggregationWithRoleRestriction");
	}

	/**
	 * import local.*;<br>
	 * v143&lt;&gt;--{ContainsLocality}v2
	 */
	@Test(expected = UnknownTypeException.class)
	public void testSimplePathDescription_WithUnknownPackage() {
		String query = "import local.*;\npathSystem(getVertex(143),<>--{localities})";
		GreqlQuery.createQuery(query).evaluate(datagraph);
	}

	/**
	 * import local.*;<br>
	 * v143&lt;&gt;--{ContainsLocality}v2
	 */
	@Test(expected = UnknownTypeException.class)
	public void testSimplePathDescription_WithUnknownPackage_Generated()
			throws InstantiationException, IllegalAccessException {
		String query = "import local.*;\npathSystem(getVertex(143),<>--{localities})";

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
		String query = "import localities.ContainsLo;\npathSystem(getVertex(143),<>--{ContainsLocality})";
		GreqlQuery.createQuery(query).evaluate(datagraph);
	}

	/**
	 * import localities.*;<br>
	 * v143&lt;&gt;--{ContainsLocality}v2
	 */
	@Test(expected = UnknownTypeException.class)
	public void testSimplePathDescription_WithUnknownEdgeClass_Generated()
			throws InstantiationException, IllegalAccessException {
		String query = "import localities.ContainsLo;\npathSystem(getVertex(143),<>--{ContainsLocality})";

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
		compareResultsOfQuery("pathSystem(getVertex(19),--getEdge(325)->)",
				"testdata.TestEdgePathDescription");
	}

	// TODO probably fix GReQL parser, the following query is correct but it can
	// not be parsed
	@Test(expected = ParsingException.class)
	public void testEdgePathDescription_withRestriction()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery(
				"pathSystem(getVertex(19),--getEdge(325)->{@ thisEdge=getEdge(325)})",
				"testdata.TestEdgePathDescription_withRestriction");
	}

	/**
	 * v19--e1-&gt;v2
	 */
	@Test
	public void testEdgePathDescription_false() throws InstantiationException,
			IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(19),--getEdge(1)->)",
				"testdata.TestEdgePathDescription_false");
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
		compareResultsOfQuery("pathSystem(getVertex(155),--><--)",
				"testdata.TestSequentialPathDescription_sequenceLength2");
	}

	/**
	 * Test of query:<br>
	 * v155--&gt;v17&lt;--v14
	 */
	@Test
	public void testSequentialPathDescription_withVertexInBetween()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(155),-->getVertex(17)<--)",
				"testdata.TestSequentialPathDescription_withVertexInBetween");
	}

	/**
	 * Test of query:<br>
	 * v155&lt;-&gt;&lt;-&gt;v13
	 */
	@Test
	public void testSequentialPathDescription_bidirectional()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(155),<-><->)",
				"testdata.TestSequentialPathDescription_bidirectional");
	}

	/**
	 * Test of query:<br>
	 * v155&lt;-&gt;&lt;-&gt;&lt;-&gt;v13
	 */
	@Test
	public void testSequentialPathDescription_sequenceLength3()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(155),<-><-><->)",
				"testdata.TestSequentialPathDescription_sequenceLength3");
	}

	/**
	 * Test of query:<br>
	 * v155&lt;&lt;&gt;---&gt;v6
	 */
	@Test
	public void testSequentialPathDescription_sequenceWithAggregation()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(155),<>---->)",
				"testdata.TestSequentialPathDescription_sequenceWithAggregation");
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
		compareResultsOfQuery("pathSystem(getVertex(153),[-->])",
				"testdata.TestOptionalPathDescription_edgeNeeded");
	}

	/**
	 * Test of query:<br>
	 * v153[--&gt;]153
	 */
	@Test
	public void testOptionalPathDescription_edgeNotNeeded()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(153),[-->])",
				"testdata.TestOptionalPathDescription_edgeNotNeeded");
	}

	/**
	 * Test of query:<br>
	 * v136[--&gt;]136
	 */
	@Test
	public void testOptionalPathDescription_optionalLoop()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(136),[-->])",
				"testdata.TestOptionalPathDescription_optionalLoop");
	}

	/**
	 * Test of query:<br>
	 * v155[&lt;-&gt;]v13
	 */
	@Test
	public void testOptionalPathDescription_notReachable()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(155),[<->])",
				"testdata.TestOptionalPathDescription_notReachable");
	}

	/*
	 * IteratedPathDescription
	 */

	@Test
	public void testIteratedPathDescription_Star_Reflexivity()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(153),-->*)",
				"testdata.TestIteratedPathDescription_Star_Reflexivity");
	}

	@Test
	public void testIteratedPathDescription_Plus_ReflexivityWithoutLoop()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(153),-->+)",
				"testdata.TestIteratedPathDescription_Plus_ReflexivityWithoutLoop");
	}

	@Test
	public void testIteratedPathDescription_Star_withLoop()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(136),-->*)",
				"testdata.TestIteratedPathDescription_Star_withLoop");
	}

	@Test
	public void testIteratedPathDescription_Plus_withLoop()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(136),-->+)",
				"testdata.TestIteratedPathDescription_Plus_withLoop");
	}

	@Test
	public void testIteratedPathDescription_Plus_ReachableWithLoop()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(136),<->+)",
				"testdata.TestIteratedPathDescription_Plus_ReachableWithLoop");
	}

	@Test
	public void testIteratedPathDescription_Star()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(14),-->*)",
				"testdata.TestIteratedPathDescription_Star");
	}

	@Test
	public void testIteratedPathDescription_Plus()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(14),-->+)",
				"testdata.TestIteratedPathDescription_Plus");
	}

	@Test
	public void testIteratedPathDescription_Star_FailBecauseOfDirection()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(14),<--*)",
				"testdata.TestIteratedPathDescription_Star_FailBecauseOfDirection");
	}

	@Test
	public void testIteratedPathDescription_Plus_FailBecauseOfDirection()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(14),<--+)",
				"testdata.TestIteratedPathDescription_Plus_FailBecauseOfDirection");
	}

	@Test(expected = GreqlException.class)
	public void testIteratedPathDescription_Exponent_Reflexivity() {
		String query = "pathSystem(getVertex(21),-->^0 )";
		GreqlQuery.createQuery(query).evaluate(datagraph);
	}

	@Test(expected = GreqlException.class)
	public void testIteratedPathDescription_Exponent_Reflexivity_Generated()
			throws InstantiationException, IllegalAccessException {
		String query = "pathSystem(getVertex(21),-->^0 )";
		String classname = "testdata.TestIteratedPathDescription_Exponent_Reflexivity_Generated";
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(), classname);
		generatedQuery.newInstance().execute(datagraph);
	}

	@Test
	public void testIteratedPathDescription_Exponent_TooShort()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(21),-->^1)",
				"testdata.TestIteratedPathDescription_Exponent_TooShort");
	}

	@Test
	public void testIteratedPathDescription_Exponent()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(21),-->^2)",
				"testdata.TestIteratedPathDescription_Exponent");
	}

	@Test
	public void testIteratedPathDescription_Exponent_TooLong()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(21),-->^3)",
				"testdata.TestIteratedPathDescription_Exponent_TooLong");
	}

	@Test
	public void testIteratedPathDescription_Exponent_WrongDirection()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(21),<--^2)",
				"testdata.TestIteratedPathDescription_Exponent_WrongDirection");
	}

	@Test
	public void testIteratedPathDescription_Exponent_TwoPossibleWays_TooShort()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(144),-->^1)",
				"testdata.TestIteratedPathDescription_Exponent_TwoPossibleWays_TooShort");
	}

	@Test
	public void testIteratedPathDescription_Exponent_TwoPossibleWays_ShortMatch()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(144),-->^2)",
				"testdata.TestIteratedPathDescription_Exponent_TwoPossibleWays_ShortMatch");
	}

	@Test
	public void testIteratedPathDescription_Exponent_TwoPossibleWays_BetweenBoth()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(144),-->^3)",
				"testdata.TestIteratedPathDescription_Exponent_TwoPossibleWays_BetweenBoth");
	}

	@Test
	public void testIteratedPathDescription_Exponent_TwoPossibleWays_LongMatch()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(144),-->^4)",
				"testdata.TestIteratedPathDescription_Exponent_TwoPossibleWays_LongMatch");
	}

	@Test
	public void testIteratedPathDescription_Exponent_TwoPossibleWays_TooLong()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(144),-->^5)",
				"testdata.TestIteratedPathDescription_Exponent_TwoPossibleWays_TooLong");
	}

	@Test(expected = GreqlException.class)
	public void testIteratedPathDescription_Exponent_withLoop0() {
		String query = "pathSystem(getVertex(136),-->^0)";
		GreqlQuery.createQuery(query).evaluate(datagraph);
	}

	@Test(expected = GreqlException.class)
	public void testIteratedPathDescription_Exponent_withLoop0_Generated()
			throws InstantiationException, IllegalAccessException {
		String query = "pathSystem(getVertex(136),-->^0 )";
		String classname = "testdata.TestIteratedPathDescription_Exponent_withLoop0_Generated";
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(), classname);
		generatedQuery.newInstance().execute(datagraph);
	}

	@Test
	public void testIteratedPathDescription_Exponent_withLoop1()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(136),-->^1)",
				"testdata.TestIteratedPathDescription_Exponent_withLoop1");
	}

	@Test
	public void testIteratedPathDescription_Exponent_withLoop2()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(136),-->^2)",
				"testdata.TestIteratedPathDescription_Exponent_withLoop2");
	}

	/*
	 * AlternativePathDescription
	 */

	@Test
	public void testAlternativePathDescription_BothFail()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(144),-->^5 |-->^3)",
				"testdata.TestAlternativePathDescription_BothFail");
	}

	@Test
	public void testAlternativePathDescription_FirstSucceedsSecondFails()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(144),-->^4 |-->^3)",
				"testdata.TestAlternativePathDescription_FirstSucceedsSecondFails");
	}

	@Test
	public void testAlternativePathDescription_FirstFailsSecondSucceeds()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(144),-->^5 |-->^2)",
				"testdata.TestAlternativePathDescription_FirstFailsSecondSucceeds");
	}

	@Test
	public void testAlternativePathDescription_BothSucceed()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(144),-->^4 |-->^2)",
				"testdata.TestAlternativePathDescription_BothSucceed");
	}

	@Test
	public void testAlternativePathDescription_OfLength3()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(144),-->^4 |-->^2 |<>--)",
				"testdata.TestAlternativePathDescription_OfLength3");
	}

	/*
	 * GroupPathDescription
	 */

	@Test
	public void testGroupPathDescription_OneElement()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(144),(-->^2))",
				"testdata.TestGroupPathDescription_OneElement");
	}

	@Test
	public void testGroupPathDescription_SeveralElements()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(144),(-->-->))",
				"testdata.TestGroupPathDescription_SeveralElements");
	}

	@Test
	public void testGroupPathDescription_SeveralElements_OnlyOneInGroup()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(144),-->(-->))",
				"testdata.TestGroupPathDescription_SeveralElements_OnlyOneInGroup");
	}

	@Test
	public void testGroupPathDescription_SeveralElements_TwoGroups()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(144),(-->)(-->))",
				"testdata.TestGroupPathDescription_SeveralElements_TwoGroups");
	}

	@Test
	public void testGroupPathDescription_SeveralBrackets()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(144),(((-->^2))))",
				"testdata.TestGroupPathDescription_SeveralBrackets");
	}

	/*
	 * TransposedPathDescription
	 */

	@Test
	public void testTransposedPathDescription_OneEdge_false()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(144),-->^T)",
				"testdata.TestTransposedPathDescription_OneEdge_false");
	}

	@Test
	public void testTransposedPathDescription_OneEdge_true()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(144),(<--)^T)",
				"testdata.TestTransposedPathDescription_OneEdge_true");
	}

	@Test
	public void testTransposedPathDescription_SeveralEdges()
			throws InstantiationException, IllegalAccessException {
		compareResultsOfQuery("pathSystem(getVertex(144),(<--<--^3)^T)",
				"testdata.TestTransposedPathDescription_SeveralEdges");
	}

	/*
	 * VertexRestriction
	 */

	@Test
	public void testVertexRestriction() throws InstantiationException,
			IllegalAccessException {
		String query = "import localities.*;"
				+ "pathSystem(getVertex(154),<--&{County})";
		Object erg1 = GreqlQuery.createQuery(query).evaluate(datagraph);

		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestVertexRestriction_StartVertex");
		Object erg2 = generatedQuery.newInstance().execute(datagraph);
		assertEquals(erg1, erg2);
	}

	@Test
	public void testVertexRestriction_SeveralVertexClasses()
			throws InstantiationException, IllegalAccessException {
		String query = "import localities.*;"
				+ "pathSystem(getVertex(154),<--&{County,City})";
		Object erg1 = GreqlQuery.createQuery(query).evaluate(datagraph);

		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestVertexRestriction_StartVertex_SeveralVertexClasses");
		Object erg2 = generatedQuery.newInstance().execute(datagraph);
		assertEquals(erg1, erg2);
	}

	@Test
	public void testVertexRestriction_Predicate()
			throws InstantiationException, IllegalAccessException {
		String query = "import localities.*;"
				+ "pathSystem(getVertex(154),<--&{County @ thisVertex.name=\"Hessen\"})";
		Object erg1 = GreqlQuery.createQuery(query).evaluate(datagraph);

		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestVertexRestriction_Predicate");
		Object erg2 = generatedQuery.newInstance().execute(datagraph);
		assertEquals(erg1, erg2);
	}
}
