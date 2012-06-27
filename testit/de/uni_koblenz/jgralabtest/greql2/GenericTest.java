/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.junit.Before;
import org.junit.BeforeClass;
import org.pcollections.PCollection;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.Query;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.greql.evaluator.QueryImpl;
import de.uni_koblenz.jgralab.greql.optimizer.DefaultOptimizer;
import de.uni_koblenz.jgralab.greql.optimizer.Optimizer;
import de.uni_koblenz.jgralab.greql.parser.GreqlParser;
import de.uni_koblenz.jgralab.greql.serialising.GreqlSerializer;
import de.uni_koblenz.jgralab.greql.types.Undefined;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.minimal.Node;

public class GenericTest {

	protected static final double DELTA = 0.00000001;

	public enum TestVersion {
		GREQL_GRAPH, ROUTE_MAP_GRAPH, CYCLIC_GRAPH, TREE_GRAPH
	};

	protected static int airportCount, crossroadCount, countyCount,
			uncontainedCrossroadCount, localityCount, footpathCount,
			plazaCount, townCount;

	private TestVersion defaultVersion = TestVersion.ROUTE_MAP_GRAPH;

	@BeforeClass
	public static void globalSetUp() throws Exception {
		GenericTest test = new GenericTest();
		airportCount = test.queryInteger("count(V{junctions.Airport})");
		townCount = test.queryInteger("count(V{localities.Town})");
		crossroadCount = test.queryInteger("count(V{junctions.Crossroad})");
		countyCount = test.queryInteger("count(V{localities.County})");
		footpathCount = test.queryInteger("count(E{connections.Footpath})");
		plazaCount = test.queryInteger("count(V{junctions.Plaza})");
		localityCount = test.queryInteger("count(V{localities.Locality})");
		queryUncontainedCrossroadCount(test);
		JGraLab.setLogLevel(Level.OFF);
	}

	private int queryInteger(String query) throws Exception {
		return ((Integer) evalTestQuery(query)).intValue();
	}

	private static void queryUncontainedCrossroadCount(GenericTest test)
			throws Exception {
		String queryString = "sum(from r:V{junctions.Crossroad} report depth(pathSystem(r, <--{localities.ContainsCrossroad})) end)";
		Object result = test.evalTestQuery(queryString);

		uncontainedCrossroadCount = crossroadCount
				- ((Number) result).intValue();
	}

	protected void assertQueryIsUndefined(String query) throws Exception {
		Object result = evalTestQuery(query);
		assertEquals(Undefined.UNDEFINED, result);
	}

	protected void assertQueryEquals(String query, boolean expectedValue)
			throws Exception {
		Object result = evalTestQuery(query);
		assertEquals(expectedValue, ((Boolean) result).booleanValue());
	}

	protected void assertQueryIsTrue(String query) throws Exception {
		assertQueryEquals(query, true);
	}

	protected void assertQueryIsFalse(String query) throws Exception {
		assertQueryEquals(query, false);
	}

	protected void assertQueryEquals(String query, int expectedValue)
			throws Exception {
		Object result = evalTestQuery(query);
		assertEquals(expectedValue, ((Integer) result).intValue());
	}

	protected void assertQueryEquals(String query, long expectedValue)
			throws Exception {
		Object result = evalTestQuery(query);
		assertEquals(expectedValue, ((Long) result).longValue());
	}

	protected void assertQueryEquals(String query, double expectedValue)
			throws Exception {
		Object result = evalTestQuery(query);
		assertEquals(expectedValue, ((Double) result).doubleValue(), DELTA);
	}

	protected void assertQueryEquals(String query, String expectedValue)
			throws Exception {
		Object result = evalTestQuery(query);
		assertEquals(expectedValue, result.toString());
	}

	protected void assertQueryEqualsQuery(String query,
			String expectedResultAsQuery) throws Exception {
		Object result = evalTestQuery(query);
		Object expectedResult = evalTestQuery(expectedResultAsQuery);
		try {
			if (!result.equals(expectedResult)) {
				System.out.println(result);
				System.out.println(expectedResult);
				System.out.println(result instanceof PVector);
			}
			assertEquals(expectedResult, result);

		} catch (AssertionError ex) {
			if ((result instanceof PCollection)
					&& (expectedResult instanceof PCollection)) {
				PCollection<?> col = (PCollection<?>) result;
				PCollection<?> col2 = (PCollection<?>) expectedResult;
				col = col.minusAll(col2);
				System.out.println("O L D +++++");
				System.out.println(col);
				System.out.println("N E W +++++");
				System.out.println(col2);
				System.out.println("E N D +++++");
			}
			throw ex;
		}
	}

	protected void assertQueryEquals(String query, Enum<?> expectedValue)
			throws Exception {
		Object result = evalTestQuery(query);
		assertEquals(expectedValue, (result));
	}

	protected void assertQueryEquals(String query, List<?> expectedValue)
			throws Exception {
		Object result = evalTestQuery(query);

		List<?> list = toList((PCollection<?>) result);
		assertEquals(expectedValue, list);
	}

	protected void assertQueryEquals(String query, Object expectedValue)
			throws Exception {
		Object result = evalTestQuery(query);
		assertEquals(expectedValue, result);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<?> toList(PCollection<?> collection) {
		ArrayList list = new ArrayList();
		for (Object value : collection) {
			list.add(value);
		}
		return list;
	}

	protected void expectException(String query,
			Class<? extends Exception> exception) {
		try {
			Object value = evalTestQuery(query);
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
		return (exception != null)
				&& (exception.getClass().equals(exceptionClass) || doesExceptionTypesEqual(
						exceptionClass, exception.getCause()));
	}

	/**
	 * Print the query syntax graphs (unoptimized, optimized with one specific
	 * optimizer, and optimized by the default optimizer) to user.home.
	 */
	public static boolean DEBUG_SYNTAXGRAPHS = true;

	private Graph cyclicGraph = null;
	private Graph tree = null;

	private static Graph testGraph, oldTestGraph;

	private static GreqlEnvironment environment = new GreqlEnvironmentAdapter();

	public static Query query = new QueryImpl(null);

	@Before
	public void setUp() throws Exception {
		environment.setVariable("nix", 133);
		environment.setVariable("FOO", "Currywurst");
	}

	protected void setBoundVariable(String varName, Object val) {
		environment.setVariable(varName, val);
	}

	protected Object getBoundVariable(String varName) {
		return environment.getVariable(varName);
	}

	protected Graph getTestGraph(TestVersion version) throws Exception {

		switch (version) {
		case GREQL_GRAPH:
			return createGreqlTestGraph();
		case ROUTE_MAP_GRAPH:
			return createTestGraph();
		case CYCLIC_GRAPH:
			return getCyclicTestGraph();
		case TREE_GRAPH:
			return getTestTree();
		default:
			throw new RuntimeException("Unsupported enum.");
		}
	}

	protected Graph getTestTree() {
		if (tree == null) {
			tree = createTestTree();
		}
		return tree;
	}

	protected Graph getCyclicTestGraph() {
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

	public static Graph createTestGraph() throws Exception {
		// TODO Does this singleton conflicts with some tests? Do tests change
		// this test graph?
		// This singleton should prevent spending to much time in GraphIO.
		if (testGraph == null) {
			testGraph = GraphIO.loadGraphFromFile(
					"testit/testgraphs/greqltestgraph.tg", null);
		}

		return testGraph;
	}

	protected Graph createCyclicTestGraph() {
		MinimalSchema s = MinimalSchema.instance();
		MinimalGraph g = s.createMinimalGraph(ImplementationType.STANDARD);
		Node[] v = new Node[10];
		for (int i = 0; i < 10; i++) {
			v[i] = g.createNode();
		}
		for (int i = 0; i < 10; i++) {
			g.createLink(v[i], v[(i + 1) % 10]);
		}
		return g;
	}

	protected Graph createTestTree() {
		// create a binary tree where v[0] is the root
		MinimalSchema s = MinimalSchema.instance();
		MinimalGraph g = s.createMinimalGraph(ImplementationType.STANDARD);
		Node[] v = new Node[15];
		for (int i = 0; i < 15; i++) {
			v[i] = g.createNode();
		}
		for (int i = 0; i < ((v.length - 1) / 2); i++) {
			g.createLink(v[i], v[(i * 2) + 1]);
			g.createLink(v[i], v[(i * 2) + 2]);
		}
		return g;
	}

	protected Object evalTestQuery(String functionName, String query,
			Graph datagraph) throws Exception {
		return evalTestQuery(functionName, query, null, datagraph);
	}

	protected Object evalTestQuery(String functionName, String query)
			throws Exception {
		return evalTestQuery(functionName, query, TestVersion.GREQL_GRAPH);
	}

	protected Object evalTestQuery(String query) throws Exception {
		return evalTestQueryNoMessage(query, defaultVersion);
	}

	public TestVersion getDefaultTestVersion() {
		return defaultVersion;
	}

	public void setDefaultTestVersion(TestVersion defaultVersion) {
		this.defaultVersion = defaultVersion;
	}

	protected Object evalTestQuery(String functionName, String query,
			Optimizer optimizer) throws Exception {

		return evalTestQuery(functionName, query, optimizer,
				TestVersion.GREQL_GRAPH);
	}

	protected Object evalTestQuery(String functionName, String query,
			TestVersion version) throws Exception {
		return evalTestQuery(functionName, query, null, getTestGraph(version));
	}

	protected Object evalTestQueryNoMessage(String query, TestVersion version)
			throws Exception {
		return evalQuery(query, null, getTestGraph(version));
	}

	protected Object evalTestQuery(String functionName, String query,
			Optimizer optimizer, TestVersion version) throws Exception {

		return evalTestQuery(functionName, query, optimizer,
				getTestGraph(version));
	}

	protected Object evalTestQuery(String functionName, String query,
			Optimizer optimizer, Graph datagraph) throws Exception {
		GenericTest.query = new QueryImpl(query, optimizer);
		GenericTest.query.setUseSavedOptimizedSyntaxGraph(false);

		Object result = GenericTest.query.evaluate(datagraph, environment);

		if (DEBUG_SYNTAXGRAPHS) {
			printDebuggingSyntaxGraph(optimizer);
		}

		return result;
	}

	protected Object evalQuery(String query, Optimizer optimizer,
			Graph datagraph) throws Exception {
		Query queryObj = new QueryImpl(query, optimizer);
		queryObj.setUseSavedOptimizedSyntaxGraph(false);

		return queryObj.evaluate(datagraph, environment);
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

		System.out
				.println(GreqlSerializer.serializeGraph(query.getQueryGraph()));
		try {
			Tg2Dot.convertGraph(query.getQueryGraph(), dotFileName, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected Object getNthValue(PCollection<?> col, int n) {
		Iterator<?> iter = col.iterator();
		int i = 0;
		while (iter.hasNext()) {
			Object value = iter.next();
			if (i == n) {
				return value;
			}
			i++;
		}
		return null;
	}

	protected void printResult(Object result) throws Exception {
		System.out.println("Result is: " + result);
		if (result instanceof PCollection) {
			System.out.println("Collection size is: "
					+ ((PCollection<?>) result).size());
		}
	}

}
