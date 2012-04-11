package de.uni_koblenz.jgralabtest.greql2.evaluator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluatorImpl;
import de.uni_koblenz.jgralab.greql2.evaluator.QueryImpl;
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

}
