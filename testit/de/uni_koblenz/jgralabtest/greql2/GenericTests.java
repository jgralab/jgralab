/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */

package de.uni_koblenz.jgralabtest.greql2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.greql2.SerializableGreql2;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.optimizer.DefaultOptimizer;
import de.uni_koblenz.jgralab.greql2.optimizer.Optimizer;
import de.uni_koblenz.jgralab.greql2.parser.GreqlParser;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.minimal.Node;

public class GenericTests {

	protected static final double DELTA = 0.00000001;

	public enum TestVersion {
		GREQL_GRAPH, CITY_MAP_GRAPH
	};

	protected static int airportCount, crossroadCount, countyCount,
			uncontainedCrossroadCount;

	@BeforeClass
	public static void globalSetUp() throws Exception {
		GenericTests test = new GenericTests();
		queryAirportCount(test);
		queryCrossroadCount(test);
		queryCountyCount(test);
		queryUncontainedCrossroadCount(test);
	}

	private static void queryAirportCount(GenericTests test) throws Exception {
		String queryString = "count(V{junctions.Airport})";
		JValue result = test.evalTestQuery("static Query", queryString,
				TestVersion.CITY_MAP_GRAPH);
		airportCount = result.toInteger();
	}

	private static void queryCrossroadCount(GenericTests test) throws Exception {
		String queryString = "count(V{junctions.Crossroad})";
		JValue result = test.evalTestQuery("static Query", queryString,
				TestVersion.CITY_MAP_GRAPH);
		crossroadCount = result.toInteger();
	}

	private static void queryCountyCount(GenericTests test) throws Exception {
		String queryString = "count(V{localities.County})";
		JValue result = test.evalTestQuery("static Query", queryString,
				TestVersion.CITY_MAP_GRAPH);
		countyCount = result.toInteger();
	}

	private static void queryUncontainedCrossroadCount(GenericTests test)
			throws Exception {
		String queryString = "sum(from r:V{junctions.Crossroad} report depth(pathSystem(r, <--{localities.ContainsCrossroad})) end)";
		JValue result = test.evalTestQuery("static Query", queryString,
				TestVersion.CITY_MAP_GRAPH);

		uncontainedCrossroadCount = crossroadCount
				- result.toDouble().intValue();
	}

	protected void assertQueryEqualsNull(String query) throws Exception {
		JValue result = evalTestQuery(query);
		assertEquals(null, result.toObject());
	}

	protected void assertQueryEquals(String query, boolean expectedValue)
			throws Exception {
		JValue result = evalTestQuery(query);
		assertEquals(expectedValue, result.toBoolean().booleanValue());
	}

	protected void assertQueryEquals(String query, int expectedValue)
			throws Exception {
		JValue result = evalTestQuery(query);
		assertEquals(expectedValue, result.toInteger().intValue());
	}

	protected void assertQueryEquals(String query, long expectedValue)
			throws Exception {
		JValue result = evalTestQuery(query);
		assertEquals(expectedValue, result.toLong().longValue());
	}

	protected void assertQueryEquals(String query, double expectedValue)
			throws Exception {
		JValue result = evalTestQuery(query);
		assertEquals(expectedValue, result.toDouble().doubleValue(), DELTA);
	}

	protected void assertQueryEquals(String query, String expectedValue)
			throws Exception {
		JValue result = evalTestQuery(query);
		assertEquals(expectedValue, result.toString());
	}

	protected void assertQueryEqualsQuery(String query,
			String expectedResultAsQuery) throws Exception {
		JValue result = evalTestQuery(query);
		JValue expectedResult = evalTestQuery(expectedResultAsQuery);
		assertEquals(expectedResult, result);
	}

	protected void assertQueryEquals(String query, Enum<?> expectedValue)
			throws Exception {
		JValue result = evalTestQuery(query);
		assertEquals(expectedValue, result.toEnum());
	}

	protected void assertQueryEquals(String query, JValue expectedValue)
			throws Exception {
		JValue result = evalTestQuery(query);
		assertEquals(expectedValue, result);
	}

	protected void assertQueryEquals(String query, List<?> expectedValue)
			throws Exception {
		JValue result = evalTestQuery(query);

		List<?> list = toList(result.toCollection());
		assertEquals(expectedValue, list);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<?> toList(JValueCollection collection) {
		ArrayList list = new ArrayList();
		for (JValue value : collection) {
			list.add(value.toObject());
		}
		return list;
	}

	protected void expectException(String query,
			Class<? extends Exception> exception) {
		try {
			JValue value = evalTestQuery(query);
			fail("This test should fail. Instead the query could be evaluated to: "
					+ value);
		} catch (Exception ex) {
			if (!doesExceptionTypesEqual(exception, ex)) {
				throw new RuntimeException("Expected \"" + exception.getName()
						+ "\" but instead caught \"" + ex.getClass().getName()
						+ "\".", ex);
			}
		}
	}

	private boolean doesExceptionTypesEqual(
			Class<? extends Exception> exceptionClass, Throwable exception) {
		return exception != null
				&& (exception.getClass().equals(exceptionClass) || doesExceptionTypesEqual(
						exceptionClass, exception.getCause()));
	}

	/**
	 * Print the query syntax graphs (unoptimized, optimized with one specific
	 * optimizer, and optimized by the default optimizer) to user.home.
	 */
	public static boolean DEBUG_SYNTAXGRAPHS = false;

	private Graph cyclicGraph = null;
	private Graph tree = null;

	private static Graph testGraph, oldTestGraph;

	protected void printTestFunctionHeader(String functionName) {
		System.out
				.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		System.out.println("START     " + functionName);
		System.out
				.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
	}

	protected void printTestFunctionFooter(String functionName) {
		System.out
				.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		System.out.println("END       " + functionName);
		System.out
				.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	private static GreqlEvaluator eval = new GreqlEvaluator((String) null,
			null, null);

	@Before
	public void setUp() throws Exception {
		eval.setVariable("nix", new JValueImpl(133));
		eval.setVariable("FOO", new JValueImpl("Currywurst"));
	}

	protected void setBoundVariable(String varName, JValue val) {
		eval.setVariable(varName, val);
	}

	protected JValue getBoundVariable(String varName) {
		return eval.getVariable(varName);
	}

	protected Graph getTestGraph(TestVersion version) throws Exception {

		if (version == TestVersion.GREQL_GRAPH) {
			return createGreqlTestGraph();
		} else {
			return createTestGraph();
		}
	}

	protected Graph getTestTree() throws Exception {
		if (tree == null) {
			tree = createTestTree();
		}
		return tree;
	}

	protected Graph getCyclicTestGraph() throws Exception {
		if (cyclicGraph == null) {
			cyclicGraph = createCyclicTestGraph();
		}
		return cyclicGraph;
	}

	protected Graph createGreqlTestGraph() {
		if (oldTestGraph == null) {
			String query = "from i:c report i end where d:=\"nada\", c:=b, b:=a, a:=\"Mensaessen\"";
			oldTestGraph = GreqlParser.parse(query);
		}
		return oldTestGraph;
	}

	private Graph createTestGraph() throws Exception {
		// TODO Does this singleton conflicts with some tests? Do tests change
		// this test graph?
		// This singleton should prevent spending to much time in GraphIO.
		if (testGraph == null) {
			testGraph = GraphIO.loadGraphFromFileWithStandardSupport(
					"testit/testgraphs/greqltestgraph.tg", null);
		}

		return testGraph;
	}

	protected Graph createCyclicTestGraph() throws Exception {
		MinimalSchema s = MinimalSchema.instance();
		MinimalGraph g = s.createMinimalGraph(10, 10);
		Node[] v = new Node[10];
		for (int i = 0; i < 10; i++) {
			v[i] = g.createNode();
		}
		for (int i = 0; i < 10; i++) {
			g.createLink(v[i], v[(i + 1) % 10]);
		}
		return g;
	}

	protected Graph createTestTree() throws Exception {
		// create a binary tree where v[0] is the root
		MinimalSchema s = MinimalSchema.instance();
		MinimalGraph g = s.createMinimalGraph(10, 10);
		Node[] v = new Node[15];
		for (int i = 0; i < 15; i++) {
			v[i] = g.createNode();
		}
		for (int i = 0; i < (v.length - 1) / 2; i++) {
			g.createLink(v[i], v[i * 2 + 1]);
			g.createLink(v[i], v[i * 2 + 2]);
			System.out.println("[" + i + ", " + (i * 2 + 1) + ", "
					+ (i * 2 + 2) + "]");
		}
		return g;
	}

	protected JValue evalTestQuery(String functionName, String query,
			Graph datagraph) throws Exception {
		return evalTestQuery(functionName, query, null, datagraph);
	}

	protected JValue evalTestQuery(String functionName, String query)
			throws Exception {
		return evalTestQuery(functionName, query, TestVersion.GREQL_GRAPH);
	}

	protected JValue evalTestQuery(String query) throws Exception {
		return evalTestQuery("", query, TestVersion.GREQL_GRAPH);
	}

	protected JValue evalTestQuery(String functionName, String query,
			Optimizer optimizer) throws Exception {

		return evalTestQuery(functionName, query, optimizer,
				TestVersion.GREQL_GRAPH);
	}

	protected JValue evalTestQuery(String functionName, String query,
			TestVersion version) throws Exception {
		return evalTestQuery(functionName, query, null, getTestGraph(version));
	}

	protected JValue evalTestQuery(String functionName, String query,
			Optimizer optimizer, TestVersion version) throws Exception {

		return evalTestQuery(functionName, query, optimizer,
				getTestGraph(version));
	}

	protected JValue evalTestQuery(String functionName, String query,
			Optimizer optimizer, Graph datagraph) throws Exception {
		printTestFunctionHeader(functionName);
		eval.setQuery(query);
		eval.setDatagraph(datagraph);
		eval.setUseSavedOptimizedSyntaxGraph(false);

		setOptimizer(optimizer);

		// when optimizing turn on logging, too.
		eval.startEvaluation(eval.isOptimize(), true);

		if (DEBUG_SYNTAXGRAPHS) {
			printDebuggingSyntaxGraph(optimizer);
		}

		printTestFunctionFooter(functionName);

		JValue result = eval.getEvaluationResult();
		eval.printEvaluationTimes();
		return result;
	}

	private void setOptimizer(Optimizer optimizer) {
		if (optimizer != null) {
			eval.setOptimize(true);
			eval.setOptimizer(optimizer);
		} else {
			eval.setOptimize(false);
		}
	}

	private void printDebuggingSyntaxGraph(Optimizer optimizer) {
		String dotFileName = System.getProperty("user.home") + File.separator;
		if (optimizer != null) {
			System.out.println("Optimized Query:");
			if (optimizer instanceof DefaultOptimizer) {
				dotFileName += "default-optimized-query.dot";
			} else {
				dotFileName += "optimized-query.dot";
			}
		} else {
			System.out.println("Unoptimized Query:");
			dotFileName += "unoptimized-query.dot";
		}
		System.out.println(((SerializableGreql2) eval.getSyntaxGraph())
				.serialize());
		Tg2Dot.convertGraph(eval.getSyntaxGraph(), dotFileName, true);
	}

	protected JValue getNthValue(JValueCollection col, int n) {
		Iterator<JValue> iter = col.iterator();
		int i = 0;
		while (iter.hasNext()) {
			JValue value = iter.next();
			if (i == n) {
				return value;
			}
			i++;
		}
		return null;
	}

	protected void printResult(JValue result) throws Exception {
		System.out.println("Result is: " + result);
		if (result.isCollection()) {
			System.out.println("Collection size is: "
					+ result.toCollection().size());
		}
	}

}
