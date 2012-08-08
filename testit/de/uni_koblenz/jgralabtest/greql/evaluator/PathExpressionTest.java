package de.uni_koblenz.jgralabtest.greql.evaluator;

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

	/*
	 * Tests for SimplePathDescription
	 */

	// TODO check with getVertex(19)--> and getVertex(19)-->getVertex(2)

	/**
	 * v19--&gt;v2
	 */
	@Test
	public void testSimplePathDescription() throws InstantiationException,
			IllegalAccessException {
		String query = "getVertex(19)-->getVertex(2)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestSimplePathDescription", "./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestSimplePathDescription");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	/**
	 * v19&lt;--v2
	 */
	@Test
	public void testSimplePathDescription_inverseDirection()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(19)<--getVertex(2)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertFalse((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestSimplePathDescription_inverseDirection",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestSimplePathDescription_inverseDirection");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertFalse((Boolean) erg);
	}

	/**
	 * v19&lt;-&gt;v2
	 */
	@Test
	public void testSimplePathDescription_BothDirections()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(19)<->getVertex(2)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestSimplePathDescription_BothDirections",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestSimplePathDescription_BothDirections");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	/**
	 * import connections.*;<br>
	 * v19&lt;-&gt;{Street}v2
	 */
	@Test
	public void testSimplePathDescription_BothDirectionsWithRestriction()
			throws InstantiationException, IllegalAccessException {
		String query = "import connections.*;\ngetVertex(19)<->{Street}getVertex(2)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator
				.generateCode(
						query,
						datagraph.getSchema(),
						"testdata.TestSimplePathDescription_BothDirectionsWithRestriction",
						"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestSimplePathDescription_BothDirectionsWithRestriction");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	/**
	 * import connections.*;<br>
	 * v19&lt;-&gt;{Street @thisEdge.name="A48"}v2
	 */
	@Test
	public void testSimplePathDescription_BothDirectionsWithRestrictionAndPredicate()
			throws InstantiationException, IllegalAccessException {
		String query = "import connections.*;\ngetVertex(19)<->{Street @thisEdge.name=\"A48\"}getVertex(2)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator
				.generateCode(
						query,
						datagraph.getSchema(),
						"testdata.TestSimplePathDescription_BothDirectionsWithRestrictionAndPredicate",
						"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestSimplePathDescription_BothDirectionsWithRestrictionAndPredicate");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	/**
	 * v143&lt;&gt;--v2
	 */
	@Test
	public void testSimplePathDescription_Aggregation()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(143)<>--getVertex(153)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestSimplePathDescription_Aggregation", "./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestSimplePathDescription_Aggregation");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	/**
	 * import localities.*;<br>
	 * v143&lt;&gt;--{ContainsLocality}v2
	 */
	@Test
	public void testSimplePathDescription_AggregationWithTypeRestriction()
			throws InstantiationException, IllegalAccessException {
		String query = "import localities.ContainsLocality;\ngetVertex(143)<>--{ContainsLocality}getVertex(153)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator
				.generateCode(
						query,
						datagraph.getSchema(),
						"testdata.TestSimplePathDescription_AggregationWithTypeRestriction",
						"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestSimplePathDescription_AggregationWithTypeRestriction");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	/**
	 * import localities.*;<br>
	 * v143&lt;&gt;--{ContainsLocality}v2
	 */
	@Test
	public void testSimplePathDescription_AggregationWithRoleRestriction()
			throws InstantiationException, IllegalAccessException {
		String query = "import localities.*;\ngetVertex(143)<>--{localities}getVertex(153)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator
				.generateCode(
						query,
						datagraph.getSchema(),
						"testdata.TestSimplePathDescription_AggregationWithRoleRestriction",
						"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestSimplePathDescription_AggregationWithRoleRestriction");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
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
		GreqlCodeGenerator
				.generateCode(
						query,
						datagraph.getSchema(),
						"testdata.TestSimplePathDescription_WithUnknownPackage_Generated",
						"./testit/");
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

		GreqlCodeGenerator
				.generateCode(
						query,
						datagraph.getSchema(),
						"testdata.TestSimplePathDescription_WithUnknownEdgeClass_Generated",
						"./testit/");
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
		String query = "getVertex(19)--getEdge(325)->getVertex(2)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestEdgePathDescription", "./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestEdgePathDescription");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	// TODO probably fix GReQL parser, the following query is correct but it can
	// not be parsed
	@Test(expected = ParsingException.class)
	public void testEdgePathDescription_withRestriction()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(19) --getEdge(325)->{@ thisEdge=getEdge(325)} getVertex(2)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestEdgePathDescription_withRestriction",
						"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestEdgePathDescription_withRestriction");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	/**
	 * v19--e1-&gt;v2
	 */
	@Test
	public void testEdgePathDescription_false() throws InstantiationException,
			IllegalAccessException {
		String query = "getVertex(19)--getEdge(1)->getVertex(2)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertFalse((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestEdgePathDescription_false", "./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestEdgePathDescription_false");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertFalse((Boolean) erg);
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
		String query = "getVertex(155)--><--getVertex(14)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestSequentialPathDescription_sequenceLength2",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestSequentialPathDescription_sequenceLength2");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	/**
	 * Test of query:<br>
	 * v155--&gt;v17&lt;--v14
	 */
	@Test
	public void testSequentialPathDescription_withVertexInBetween()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(155)-->getVertex(17)<--getVertex(14)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestSequentialPathDescription_withVertexInBetween",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestSequentialPathDescription_withVertexInBetween");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	/**
	 * Test of query:<br>
	 * v155&lt;-&gt;&lt;-&gt;v13
	 */
	@Test
	public void testSequentialPathDescription_bidirectional()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(155)<-><->getVertex(13)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertFalse((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestSequentialPathDescription_bidirectional",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestSequentialPathDescription_bidirectional");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertFalse((Boolean) erg);
	}

	/**
	 * Test of query:<br>
	 * v155&lt;-&gt;&lt;-&gt;&lt;-&gt;v13
	 */
	@Test
	public void testSequentialPathDescription_sequenceLength3()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(155)<-><-><->getVertex(13)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestSequentialPathDescription_sequenceLength3",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestSequentialPathDescription_sequenceLength3");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	/**
	 * Test of query:<br>
	 * v155&lt;&lt;&gt;---&gt;v6
	 */
	@Test
	public void testSequentialPathDescription_sequenceWithAggregation()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(155)<>---->getVertex(6)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator
				.generateCode(
						query,
						datagraph.getSchema(),
						"testdata.TestSequentialPathDescription_sequenceWithAggregation",
						"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestSequentialPathDescription_sequenceWithAggregation");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
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
		String query = "getVertex(153)[-->]getVertex(136)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestOptionalPathDescription_edgeNeeded", "./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestOptionalPathDescription_edgeNeeded");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	/**
	 * Test of query:<br>
	 * v153[--&gt;]153
	 */
	@Test
	public void testOptionalPathDescription_edgeNotNeeded()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(153)[-->]getVertex(153)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestOptionalPathDescription_edgeNotNeeded",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestOptionalPathDescription_edgeNotNeeded");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	/**
	 * Test of query:<br>
	 * v136[--&gt;]136
	 */
	@Test
	public void testOptionalPathDescription_optionalLoop()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(136)[-->]getVertex(136)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestOptionalPathDescription_optionalLoop",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestOptionalPathDescription_optionalLoop");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	/**
	 * Test of query:<br>
	 * v155[&lt;-&gt;]v13
	 */
	@Test
	public void testOptionalPathDescription_notReachable()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(155)[<->]getVertex(13)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertFalse((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestOptionalPathDescription_notReachable",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestOptionalPathDescription_notReachable");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertFalse((Boolean) erg);
	}

	/*
	 * IteratedPathDescription
	 */

	@Test
	public void testIteratedPathDescription_Star_Reflexivity()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(153)-->*getVertex(153)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestIteratedPathDescription_Star_Reflexivity",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Star_Reflexivity");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	@Test
	public void testIteratedPathDescription_Plus_ReflexivityWithoutLoop()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(153)-->+getVertex(153)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertFalse((Boolean) erg);

		GreqlCodeGenerator
				.generateCode(
						query,
						datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Plus_ReflexivityWithoutLoop",
						"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Plus_ReflexivityWithoutLoop");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertFalse((Boolean) erg);
	}

	@Test
	public void testIteratedPathDescription_Star_withLoop()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(136)-->*getVertex(136)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestIteratedPathDescription_Star_withLoop",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Star_withLoop");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	@Test
	public void testIteratedPathDescription_Plus_withLoop()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(136)-->+getVertex(136)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestIteratedPathDescription_Plus_withLoop",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Plus_withLoop");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	@Test
	public void testIteratedPathDescription_Plus_ReachableWithLoop()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(136)<->+getVertex(153)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestIteratedPathDescription_Plus_ReachableWithLoop",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Plus_ReachableWithLoop");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	@Test
	public void testIteratedPathDescription_Star()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(14)-->*getVertex(16)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestIteratedPathDescription_Star", "./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Star");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	@Test
	public void testIteratedPathDescription_Plus()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(14)-->+getVertex(16)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestIteratedPathDescription_Plus", "./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Plus");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	@Test
	public void testIteratedPathDescription_Star_FailBecauseOfDirection()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(14)<--*getVertex(16)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertFalse((Boolean) erg);

		GreqlCodeGenerator
				.generateCode(
						query,
						datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Star_FailBecauseOfDirection",
						"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Star_FailBecauseOfDirection");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertFalse((Boolean) erg);
	}

	@Test
	public void testIteratedPathDescription_Plus_FailBecauseOfDirection()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(14)<--+getVertex(16)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertFalse((Boolean) erg);

		GreqlCodeGenerator
				.generateCode(
						query,
						datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Plus_FailBecauseOfDirection",
						"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Plus_FailBecauseOfDirection");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertFalse((Boolean) erg);
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
		GreqlCodeGenerator
				.generateCode(
						query,
						datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Exponent_Reflexivity_Generated",
						"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Exponent_Reflexivity_Generated");
		generatedQuery.newInstance().execute(datagraph);
	}

	@Test
	public void testIteratedPathDescription_Exponent_TooShort()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(21)-->^1 getVertex(13)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertFalse((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestIteratedPathDescription_Exponent_TooShort",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Exponent_TooShort");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertFalse((Boolean) erg);
	}

	@Test
	public void testIteratedPathDescription_Exponent()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(21)-->^2 getVertex(13)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestIteratedPathDescription_Exponent", "./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Exponent");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	@Test
	public void testIteratedPathDescription_Exponent_TooLong()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(21)-->^3 getVertex(13)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertFalse((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestIteratedPathDescription_Exponent_TooLong",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Exponent_TooLong");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertFalse((Boolean) erg);
	}

	@Test
	public void testIteratedPathDescription_Exponent_WrongDirection()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(21)<--^2 getVertex(13)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertFalse((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestIteratedPathDescription_Exponent_WrongDirection",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Exponent_WrongDirection");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertFalse((Boolean) erg);
	}

	@Test
	public void testIteratedPathDescription_Exponent_TwoPossibleWays_TooShort()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(144)-->^1 getVertex(16)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertFalse((Boolean) erg);

		GreqlCodeGenerator
				.generateCode(
						query,
						datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Exponent_TwoPossibleWays_TooShort",
						"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Exponent_TwoPossibleWays_TooShort");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertFalse((Boolean) erg);
	}

	@Test
	public void testIteratedPathDescription_Exponent_TwoPossibleWays_ShortMatch()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(144)-->^2 getVertex(16)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator
				.generateCode(
						query,
						datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Exponent_TwoPossibleWays_ShortMatch",
						"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Exponent_TwoPossibleWays_ShortMatch");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	@Test
	public void testIteratedPathDescription_Exponent_TwoPossibleWays_BetweenBoth()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(144)-->^3 getVertex(16)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertFalse((Boolean) erg);

		GreqlCodeGenerator
				.generateCode(
						query,
						datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Exponent_TwoPossibleWays_BetweenBoth",
						"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Exponent_TwoPossibleWays_BetweenBoth");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertFalse((Boolean) erg);
	}

	@Test
	public void testIteratedPathDescription_Exponent_TwoPossibleWays_LongMatch()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(144)-->^4 getVertex(16)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator
				.generateCode(
						query,
						datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Exponent_TwoPossibleWays_LongMatch",
						"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Exponent_TwoPossibleWays_LongMatch");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	@Test
	public void testIteratedPathDescription_Exponent_TwoPossibleWays_TooLong()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(144)-->^5 getVertex(16)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertFalse((Boolean) erg);

		GreqlCodeGenerator
				.generateCode(
						query,
						datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Exponent_TwoPossibleWays_TooLong",
						"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Exponent_TwoPossibleWays_TooLong");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertFalse((Boolean) erg);
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
		GreqlCodeGenerator
				.generateCode(
						query,
						datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Exponent_withLoop0_Generated",
						"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Exponent_withLoop0_Generated");
		generatedQuery.newInstance().execute(datagraph);
	}

	@Test
	public void testIteratedPathDescription_Exponent_withLoop1()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(136)-->^1 getVertex(136)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestIteratedPathDescription_Exponent_withLoop1",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Exponent_withLoop1");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	@Test
	public void testIteratedPathDescription_Exponent_withLoop2()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(136)-->^2 getVertex(136)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestIteratedPathDescription_Exponent_withLoop2",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestIteratedPathDescription_Exponent_withLoop2");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	/*
	 * AlternativePathDescription
	 */

	@Test
	public void testAlternativePathDescription_BothFail()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(144)-->^5 |-->^3 getVertex(16)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertFalse((Boolean) erg);

		GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestAlternativePathDescription_BothFail",
						"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestAlternativePathDescription_BothFail");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertFalse((Boolean) erg);
	}

	@Test
	public void testAlternativePathDescription_FirstSucceedsSecondFails()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(144)-->^4 |-->^3 getVertex(16)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator
				.generateCode(
						query,
						datagraph.getSchema(),
						"testdata.TestAlternativePathDescription_FirstSucceedsSecondFails",
						"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestAlternativePathDescription_FirstSucceedsSecondFails");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	@Test
	public void testAlternativePathDescription_FirstFailsSecondSucceeds()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(144)-->^5 |-->^2 getVertex(16)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator
				.generateCode(
						query,
						datagraph.getSchema(),
						"testdata.TestAlternativePathDescription_FirstFailsSecondSucceeds",
						"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestAlternativePathDescription_FirstFailsSecondSucceeds");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	@Test
	public void testAlternativePathDescription_BothSucceed()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(144)-->^4 |-->^2 getVertex(16)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestAlternativePathDescription_BothSucceed",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestAlternativePathDescription_BothSucceed");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	@Test
	public void testAlternativePathDescription_OfLength3()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(144)-->^4 |-->^2 |<>-- getVertex(16)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestAlternativePathDescription_OfLength3",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestAlternativePathDescription_OfLength3");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	/*
	 * GroupPathDescription
	 */

	@Test
	public void testGroupPathDescription_OneElement()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(144)(-->^2) getVertex(16)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestGroupPathDescription_OneElement", "./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestGroupPathDescription_OneElement");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	@Test
	public void testGroupPathDescription_SeveralElements()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(144)(-->-->) getVertex(16)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestGroupPathDescription_SeveralElements",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestGroupPathDescription_SeveralElements");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	@Test
	public void testGroupPathDescription_SeveralElements_OnlyOneInGroup()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(144)-->(-->) getVertex(16)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator
				.generateCode(
						query,
						datagraph.getSchema(),
						"testdata.TestGroupPathDescription_SeveralElements_OnlyOneInGroup",
						"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestGroupPathDescription_SeveralElements_OnlyOneInGroup");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	@Test
	public void testGroupPathDescription_SeveralElements_TwoGroups()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(144)(-->)(-->) getVertex(16)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestGroupPathDescription_SeveralElements_TwoGroups",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestGroupPathDescription_SeveralElements_TwoGroups");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	@Test
	public void testGroupPathDescription_SeveralBrackets()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(144)(((-->^2))) getVertex(16)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestGroupPathDescription_SeveralBrackets",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestGroupPathDescription_SeveralBrackets");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	/*
	 * TransposedPathDescription
	 */

	@Test
	public void testTransposedPathDescription_OneEdge_false()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(144) -->^T getVertex(154)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertFalse((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestTransposedPathDescription_OneEdge_false",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestTransposedPathDescription_OneEdge_false");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertFalse((Boolean) erg);
	}

	@Test
	public void testTransposedPathDescription_OneEdge_true()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(144) (<--)^T getVertex(154)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestTransposedPathDescription_OneEdge_true",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestTransposedPathDescription_OneEdge_true");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
	}

	@Test
	public void testTransposedPathDescription_SeveralEdges()
			throws InstantiationException, IllegalAccessException {
		String query = "getVertex(144)(<--<--^3)^T getVertex(16)";
		Object erg = GreqlQuery.createQuery(query).evaluate(datagraph);
		assertTrue((Boolean) erg);

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestTransposedPathDescription_SeveralEdges",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestTransposedPathDescription_SeveralEdges");
		erg = generatedQuery.newInstance().execute(datagraph);
		assertTrue((Boolean) erg);
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

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestVertexRestriction_StartVertex", "./testit/");
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

		GreqlCodeGenerator
				.generateCode(
						query,
						datagraph.getSchema(),
						"testdata.TestVertexRestriction_StartVertex_SeveralVertexClasses",
						"./testit/");
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

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestVertexRestriction_StartVertex_Predicate",
				"./testit/");
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

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestVertexRestriction_EndVertex", "./testit/");
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

		GreqlCodeGenerator
				.generateCode(
						query,
						datagraph.getSchema(),
						"testdata.TestVertexRestriction_EndVertex_SeveralVertexClasses",
						"./testit/");
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

		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(),
				"testdata.TestVertexRestriction_EndVertex_Predicate",
				"./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(),
						"testdata.TestVertexRestriction_EndVertex_Predicate");
		erg = generatedQuery.newInstance().execute(datagraph);
		checkResultSet(erg, 154, "name", "Frankfurt am Main");
	}
}
