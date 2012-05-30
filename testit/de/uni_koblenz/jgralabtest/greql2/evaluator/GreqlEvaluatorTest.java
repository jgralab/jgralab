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

package de.uni_koblenz.jgralabtest.greql2.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.Collection;

import org.junit.Test;
import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.funlib.FunLib;
import de.uni_koblenz.jgralab.greql2.optimizer.DefaultOptimizer;
import de.uni_koblenz.jgralab.greql2.optimizer.VariableDeclarationOrderOptimizer;
import de.uni_koblenz.jgralab.greql2.types.Tuple;
import de.uni_koblenz.jgralabtest.greql2.GenericTest;
import de.uni_koblenz.jgralabtest.greql2.testfunctions.IsPrime;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.RouteMap;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.connections.AirRoute;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.junctions.Airport;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.localities.County;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.localities.Locality;

public class GreqlEvaluatorTest extends GenericTest {
	static {
		FunLib.register(IsPrime.class);
	}

	private static final String[] COUNTIES = { "Berlin", "Hessen",
			"Rheinland-Pfalz" };
	private static final String[] LOCALITIES_WITHOUT_CITIES = { "Kammerforst",
			"Frankfurt-Flughafen", "Höhr-Grenzhausen",
			"Flugplatz Koblenz-Winningen", "Winningen", "Lautzenhausen",
			"Montabaur", "Flughafen Frankfurt-Hahn" };

	@Test
	public void testCombinations() throws Exception {
		String query = "from a:list(1..10), b:list(1..10), c:list(1..10), d:list(1..10) report d end";
		// TODO test seriously
		@SuppressWarnings("unused")
		Object result = evalTestQuery(query);
		// for (JValue v : result.toCollection()) {
		// System.out.println(v);
		// }
	}

	@Test
	public void testCombinations2() throws Exception {
		String createboundVars = "set(1,2,3) store as s123";
		String createboundVars2 = "set(4,5,6) store as s456 ";
		evalTestQuery(createboundVars);
		// TODO test seriously
		// System.out.println("HashMap: " + boundVars.size());
		// for (java.util.Map.Entry<String, JValue> entry :
		// boundVars.entrySet()) {
		// System.out.println("<" + entry.getKey() + "," + entry.getValue()
		// + ">");
		// }
		evalTestQuery(createboundVars2);
		String query = "using s123, s456: "
				+ // img_Action, img_State
				"from t:s123, "
				+ // keySet img_Action
				"     a:s456, "
				+ // keySet img_state
				"     m: from _m:list(t..5) reportSet _m end " + "with a < m "
				+ "reportSet t,a end";

		// TODO test seriously
		@SuppressWarnings("unused")
		Object result = evalQuery(query, null, null);
		// for (JValue v : result.toCollection()) {
		// System.out.println(v);
		// }
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateAlternativePathDescription(AlternativePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateAlternativePathDescription() throws Exception {
		String queryString = "from airport: V{junctions.Airport}, x: V with x "
				+ "(-->{localities.ContainsLocality} | -->{connections.AirRoute}) airport "
				+ "report x end";
		PVector<Vertex> result = (PVector<Vertex>) evalTestQuery(queryString);

		assertFalse(result.isEmpty());
		for (Vertex vertex : result) {
			if (!(vertex instanceof Airport || vertex instanceof County)) {
				fail();
			}
		}
	}

	@Test
	public void testEvaluateAlternativePathDescription2() throws Exception {
		String queryString = "from v:V{NamedElement} reportSet v, v.name, v (-->{^connections.Way, ^connections.AirRoute} | (-->{localities.ContainsLocality} -->{connections.AirRoute}))* end";
		PSet<Tuple> result = (PSet<Tuple>) evalTestQuery(queryString);

		for (Tuple tuple : result) {
			Vertex vertex = (Vertex) tuple.get(0);
			if (!(vertex instanceof Airport || vertex instanceof County || vertex instanceof Locality)) {
				fail();
			}
		}
		Object resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);

	}

	@Test
	public void testEvaluateExponentiatedPathDescription() throws Exception {
		String queryString = "from airport: V{junctions.Airport} with airport  <->^2 airport report airport end";
		Collection<?> result = (Collection<?>) evalTestQuery(queryString);
		assertEquals(airportCount, result.size());
		Object resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateForwardVertexSet(ForwardVertexSet,
	 * Graph)'
	 */
	@Test
	public void testEvaluateBackwardVertexSet() throws Exception {
		String queryString = "from airport: V{junctions.Airport} "
				+ "report airport <--{connections.AirRoute} <--{localities.ContainsLocality} end";
		Collection<Collection<Vertex>> result = (Collection<Collection<Vertex>>) evalTestQuery(queryString);

		assertEquals(airportCount, result.size());
		for (Collection<Vertex> collection : result) {
			if (!collection.isEmpty()) {
				for (Vertex vertex : collection) {
					setBoundVariable("x", vertex);
					assertQueryEquals(
							"using x: exists airport:V{junctions.Airport} "
									+ "@ isReachable(x, airport, <->^2)", true);
				}
			}
		}
		Object resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	@Test
	public void testReachability() throws Exception {
		String queryString = "forall e: E{IsDefinitionOf}"
				+ "  @ startVertex(e) -->{IsDefinitionOf} endVertex(e)";
		Object result = evalTestQuery("Reachability", queryString);
		assertTrue(result instanceof Boolean);
		Object resultWO = evalTestQuery("BackwardVertexSet1 (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateForwardVertexSet(ForwardVertexSet,
	 * Graph)'
	 */
	@Test
	public void testEvaluateBackwardVertexSet2() throws Exception {
		String queryString = "from w: V{junctions.Junction} report w <--{localities.ContainsCrossroad} [<--{localities.ContainsLocality}] end";
		Collection<Collection<Vertex>> result = (Collection<Collection<Vertex>>) evalTestQuery(queryString);

		assertEquals(crossroadCount + airportCount, result.size());
		for (Collection<Vertex> collection : result) {
			for (Vertex vertex : collection) {
				if (!(vertex instanceof Locality || vertex instanceof County)) {
					fail();
				}
			}
		}
		Object resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateListComprehension(ListComprehension,
	 * Graph)'
	 */
	@Test
	public void testUsing() throws Exception {
		setBoundVariable("FOO", "A String");
		String queryString = "using FOO: FOO";
		Object result = evalTestQuery("Using", queryString);
		assertEquals("A String", result);
		Object resultWO = evalTestQuery("Using", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateListComprehension(ListComprehension,
	 * Graph)'
	 */
	@Test
	public void testUsingWithFunction() throws Exception {
		setBoundVariable("FOO", new JValueImpl("A String"));
		String queryString = "using FOO: concat(FOO, \" Another String\")";
		JValue result = evalTestQuery("UsingWithFunction", queryString);
		assertEquals("A String Another String", result.toString());
		JValue resultWO = evalTestQuery("UsingWithFunction", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateListComprehension(ListComprehension,
	 * Graph)'
	 */
	@Test
	public void testUsingWithPath() throws Exception {
		String queryString = "getVertex(1) store as FOO";
		evalTestQuery("", queryString);
		queryString = "using FOO: pathSystem(FOO, (-->))";
		JValue result = evalTestQuery("UsingWithPath", queryString);
		JValue resultWO = evalTestQuery("UsingWithPath", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateListComprehension(ListComprehension,
	 * Graph)'
	 */
	@Test
	public void testPathSystem() throws Exception {
		String queryString = "pathSystem(getVertex(1), -->{Edge})";
		// String queryString = "getVertex(1) :-) -->{Edge}";
		JValue result = evalTestQuery("UsingWithPath", queryString);
		// assertEquals("A String Another String", result.toString());
		JValue resultWO = evalTestQuery("UsingWithPath", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateAlternativePathDescription(AlternativePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateComplexPathDescription() throws Exception {
		String queryString = "from county: V{localities.County}, locality: V{localities.Locality} "
				+ "with county (-->{localities.ContainsLocality}) | "
				+ "(-->{localities.HasCapital}) locality "
				+ "reportSet locality end";
		JValue result = evalTestQuery(queryString);
		assertEquals(localityCount, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateConditionalExpression(ConditionalExpression,
	 * Graph)'
	 */
	@Test
	public void testEvaluateConditionalExpression() throws Exception {
		String queryString = "1=1?1:2";
		JValue result = evalTestQuery("ConditionalExpression", queryString);
		assertEquals(1, (int) result.toInteger());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateConditionalExpression2() throws Exception {
		String queryString = "1=2?1:2";
		JValue result = evalTestQuery("ConditionalExpression2", queryString);
		assertEquals(2, (int) result.toInteger());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateConditionalExpression3() throws Exception {
		String queryString = "false ? 1 : 2";
		JValue result = evalTestQuery("ConditionalExpression3", queryString);
		assertEquals(2, (int) result.toInteger());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateEdgePathDescription(EdgePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateEdgePathDescription() throws Exception {
		String queryString = "from edge: E, origin: V{junctions.Plaza}, target: V{junctions.Crossroad} "
				+ "in eSubgraph{connections.Footpath!} with origin <-edge-> target "
				+ "report target end";
		JValue result = evalTestQuery(queryString);
		assertEquals(1, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateEdgePathDescription(EdgePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateEdgePathDescription2() throws Exception {
		String queryString = "flatten(from edge: E in eSubgraph{connections.Footpath!} report "
				+ "from origin: V{junctions.Plaza}, target: V{junctions.Crossroad} "
				+ "with origin <-edge-> target report target end end)";
		JValue result = evalTestQuery(queryString);
		assertEquals(1, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateEdgePathDescription(EdgePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateEdgePathDescription3() throws Exception {
		String queryString = "flatten(from edge: E in eSubgraph{connections.Footpath!} report "
				+ "from origin: V{junctions.Plaza}, target: V{junctions.Crossroad} "
				+ "with origin <-edge-> target report target end end)";
		String queryString2 = "flatten(from edge: E in eSubgraph{connections.Footpath!} report "
				+ "from origin: V{junctions.Plaza}, target: V{junctions.Crossroad} "
				+ "with contains(<-edge-> target, origin) report target end end)";
		JValue result = evalTestQuery(queryString);
		assertEquals(1, result.toCollection().size());
		JValue resultWO = evalTestQuery(queryString2);
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateEdgePathDescription(EdgePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateEdgePathDescription4() throws Exception {
		String queryString = "from edge: E in eSubgraph{connections.Footpath} report from plaza: V{junctions.Plaza} report plaza end end";
		JValue result = evalTestQuery(queryString);
		assertEquals(16, result.toCollection().size());
		for (JValue j : result.toCollection()) {
			assertEquals(6, j.toCollection().size());
		}
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateEdgeSetExpression(EdgeSetExpression,
	 * Graph)'
	 */
	@Test
	public void testEvaluateEdgeSetExpression() throws Exception {
		String queryString = "from footpath:E{connections.Footpath} report footpath end";
		JValue result = evalTestQuery(queryString);
		assertEquals(footpathCount, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateEdgeSubgraphExpression1() throws Exception {
		String queryString = "from footpath: E in eSubgraph{connections.Footpath} report footpath end";
		JValue result = evalTestQuery(queryString);
		assertEquals(footpathCount, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	protected Object evalQueryWithOptimizer(String queryString)
			throws Exception {
		return evalTestQuery("", queryString, new DefaultOptimizer(),
				TestVersion.ROUTE_MAP_GRAPH);
	}

	@Test
	public void testEvaluateEdgeSubgraphExpression2() throws Exception {
		String queryString = "from i: V in eSubgraph{connections.Footpath} report i end";
		PVector<Vertex> result = (PVector<Vertex>) evalTestQuery(queryString);
		assertEquals(24, result.size());
		Object resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateEdgeSubgraphExpression3() throws Exception {
		String queryString = "from i: V in eSubgraph{^connections.Footpath} report i end";
		PVector<Vertex> result = (PVector<Vertex>) evalTestQuery(queryString);
		assertEquals(156, result.size());
		Object resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateExponentiatedPathDescription1() throws Exception {
		String queryString = "from origin: V{junctions.Airport}, target: V{junctions.Airport} with origin <--{connections.AirRoute}^1 target report origin end";
		PVector<Vertex> result = (PVector<Vertex>) evalTestQuery(queryString);
		assertEquals(3, result.size());
		Object resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateExponentiatedPathDescription2() throws Exception {
		String queryString = "from origin: V{junctions.Airport}, target: V{junctions.Airport} with origin <--{connections.AirRoute}^2 target report origin end";
		JValue result = evalTestQuery(queryString);
		assertEquals(1, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateExponentiatedPathDescription3() throws Exception {
		String queryString = "from origin: V{junctions.Airport}, target: V{junctions.Airport} with origin <--{connections.AirRoute}^3 target report origin end";
		JValue result = evalTestQuery(queryString);
		assertEquals(0, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateForwardVertexSet(ForwardVertexSet,
	 * Graph)'
	 */
	@Test
	public void testEvaluateForwardVertexSet() throws Exception {
		String queryString = "from airport: V{junctions.Airport} report airport --> end";
		JValue result = evalTestQuery(queryString);
		assertEquals(airportCount, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateSequentialPathDescription(SequentialPathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateGoalRestriction1() throws Exception {
		String queryString = "from county: V{localities.County}, junction: V{junctions.Junction} with county -->{localities.ContainsLocality} & {localities.Town!} -->{localities.ContainsCrossroad} junction report junction end";
		JValue result = evalTestQuery(queryString);
		assertEquals(3, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result.toJValueSet(), resultWO.toJValueSet());
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateSequentialPathDescription(SequentialPathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateGoalRestriction2() throws Exception {
		String queryString = "from var: V{Variable}, def: V{WhereExpression} with var  -->{IsVarOf} & {DefinitionExpression} -->{IsDefinitionOf} def report var end";
		JValue result = evalTestQuery("GoalRestriction2", queryString);
		assertEquals(0, result.toCollection().size());
		JValue resultWO = evalTestQuery("GoalRestriction2 (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateSequentialPathDescription(SequentialPathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateRestrictedEdgePathDescription() throws Exception {
		String queryString = "from var: V{Variable}, def: V{WhereExpression} with var  -->{IsVarOf @ thisEdge <> def} def report var end";
		JValue result = evalTestQuery("GoalRestriction2", queryString);
		assertEquals(0, result.toCollection().size());
		JValue resultWO = evalTestQuery("GoalRestriction2 (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateGreql2Expression(Greql2Expression,
	 * Graph)'
	 */
	@Test
	public void testEvaluateGreql2Expression() throws Exception {
		String queryString = "using FOO: from i: FOO report i end";
		JValue result = evalTestQuery("EvaluateGreql2Expression", queryString);
		assertEquals(1, result.toCollection().size());
		for (JValue j : result.toCollection()) {
			assertEquals("Currywurst", j.toString());
		}
		JValue resultWO = evalTestQuery("EvaluateGreql2Expression (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateIteratedPathDescription(IteratedPathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateIteratedPathDescription1() throws Exception {
		String queryString = "from origin: V{junctions.Crossroad}, target: V{junctions.Airport} "
				+ "with origin  -->{connections.Street}* <--{localities.ContainsCrossroad} target "
				+ "report origin end";
		JValue result = evalTestQuery(queryString);
		assertEquals(180, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateIteratedPathDescription12() throws Exception {
		String queryString = "from origin: V{junctions.Crossroad}, target: V{junctions.Airport} "
				+ "with origin  -->{connections.Street}* <--{localities.ContainsCrossroad} target "
				+ "report origin end";
		JValue result = evalTestQuery(queryString);
		assertEquals(180, result.toCollection().size());

		String queryString2 = "from origin: V{junctions.Crossroad}, "
				+ " target: origin  -->{connections.Street}* <--{localities.ContainsCrossroad} &{junctions.Airport} "
				+ "report origin end";
		assertQueryEquals(queryString2, result);

		String queryString3 = "from origin: V{junctions.Crossroad}, "
				+ " target: origin  (-->{connections.Street}* <--{localities.ContainsCrossroad}) &{junctions.Airport} "
				+ "report origin end";
		assertQueryEquals(queryString3, result);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateIteratedPathDescription(IteratedPathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateIteratedPathDescription2() throws Exception {
		String queryString = "from var: V{Definition}, def: V{WhereExpression} with var -->{IsVarOf}+ -->{IsDefinitionOf} def report var end";
		JValue result = evalTestQuery("IteratedPathDescription2", queryString);
		assertEquals(0, result.toCollection().size());
		JValue resultWO = evalTestQuery("IteratedPathDescription2 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateIteratedPathDescription3() throws Exception {
		String queryString = "from origin: V{junctions.Crossroad}, target: V{junctions.Airport} "
				+ "with origin  -->{connections.Street}+ <--{localities.ContainsCrossroad} target "
				+ "report origin end";
		JValue result = evalTestQuery(queryString);
		assertEquals(177, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateIteratedPathDescription4() throws Exception {
		String queryString = "from origin: V{junctions.Crossroad}, target: V{junctions.Airport} "
				+ "with origin  -->{connections.Street}* <--{localities.ContainsCrossroad} -->{connections.AirRoute} target "
				+ "report origin end";
		JValue result = evalTestQuery(queryString);
		assertEquals(127, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateIteratedKleneePathDescriptionReflexivity()
			throws Exception {
		String queryString = "from def: V{Definition} reportSet def, def <--* end";
		JValueSet result = evalTestQuery(
				"IteratedKleneePathDescriptionReflexivity", queryString)
				.toJValueSet();

		for (JValue val : result) {
			JValueTuple tup = val.toJValueTuple();
			assertTrue(tup.get(1).toJValueSet().contains(tup.get(0)));
		}

		JValue resultWO = evalTestQuery(
				"IteratedKleneePathDescriptionReflexivity (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateIteratedKleneePathDescriptionReflexivity2()
			throws Exception {
		String queryString = "from def: V{Definition} reportSet def, def (<--{IsVarOf} <--)* end";
		JValueSet result = evalTestQuery(
				"IteratedKleneePathDescriptionReflexivity2", queryString)
				.toJValueSet();

		for (JValue val : result) {
			JValueTuple tup = val.toJValueTuple();
			assertTrue(tup.get(1).toJValueSet().contains(tup.get(0)));
		}

		JValue resultWO = evalTestQuery(
				"IteratedKleneePathDescriptionReflexivity2 (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateDefinitionExpression(DefinitionExpression,
	 * Graph)'
	 */
	@Test
	public void testEvaluateLetExpression() throws Exception {
		String queryString = "let a := 7 , b:=a, c:=b in from i:c report i end";
		JValue result = evalTestQuery("LetExpression", queryString);
		assertEquals(1, result.toCollection().size());
		JValueList list = result.toCollection().toJValueList();
		assertEquals(7, (int) list.get(0).toInteger());
		JValue resultWO = evalTestQuery("LetExpression (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateListConstruction(ListConstruction,
	 * Graph)'
	 */
	@Test
	public void testEvaluateListConstruction() throws Exception {
		String queryString = "list ( \"bratwurst\",\"currywurst\", \"steak\", \"kaenguruhfleisch\", \"spiessbraten\", \"kaenguruhfleisch\")";
		JValue result = evalTestQuery("ListConstruction", queryString);
		assertEquals(6, result.toCollection().size());
		JValueList list = result.toCollection().toJValueTuple();
		assertEquals("bratwurst", list.get(0).toString());
		assertEquals("currywurst", list.get(1).toString());
		assertEquals("steak", list.get(2).toString());
		assertEquals("kaenguruhfleisch", list.get(3).toString());
		assertEquals("spiessbraten", list.get(4).toString());
		assertEquals("kaenguruhfleisch", list.get(5).toString());
		JValue resultWO = evalTestQuery("ListConstruction (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateListRangeConstruction(ListRangeConstruction,
	 * Graph)'
	 */
	@Test
	public void testEvaluateListRangeConstruction() throws Exception {
		String queryString = "list (5..13)";
		JValue result = evalTestQuery("ListRangeConstruction", queryString);
		assertEquals(9, result.toCollection().size());
		JValueList list = result.toCollection().toJValueList();
		for (int i = 0; i < 9; i++) {
			assertEquals(i + 5, (int) list.get(i).toInteger());
		}
		JValue resultWO = evalTestQuery("ListRangeConstruction (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateListRangeConstruction2() throws Exception {
		String queryString = "list (16..6)";
		JValue result = evalTestQuery("ListRangeConstruction2", queryString);
		assertEquals(11, result.toCollection().size());
		JValueList list = result.toCollection().toJValueList();
		for (int i = 0; i < 11; i++) {
			assertEquals(6 + i, (int) list.get(i).toInteger());
		}
		JValue resultWO = evalTestQuery("ListRangeConstruction2 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateListRangeConstruction3() throws Exception {
		String queryString = "list (7..7)";
		JValue result = evalTestQuery("ListRangeConstruction3", queryString);
		assertEquals(1, result.toCollection().size());
		JValueList list = result.toCollection().toJValueList();
		assertEquals(7, (int) list.get(0).toInteger());
		JValue resultWO = evalTestQuery("ListRangeConstruction3 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateListRangeConstruction4() throws Exception {
		String queryString = "list (-6..1)";
		JValue result = evalTestQuery("ListRangeConstruction4", queryString);
		assertEquals(8, result.toCollection().size());
		JValueList list = result.toCollection().toJValueList();
		for (int i = 0; i < 8; i++) {
			assertEquals(i - 6, (int) list.get(i).toInteger());
		}
		JValue resultWO = evalTestQuery("ListRangeConstruction4 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateDependentDeclarations() throws Exception {
		String queryString = "from x:list(1..10), z:list(1..x), y:list(x..13) report isPrime(z), isPrime(z*z), isPrime(z+z*z-1) end";
		JValue result = evalTestQuery("DependentDeclarations", queryString);
		assertEquals(385, result.toCollection().size());
		result.toCollection().toJValueSet();
		JValue resultWO = evalTestQuery("DependentDeclarations (wo)",
				queryString, new VariableDeclarationOrderOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateDependentDeclarations1() throws Exception {
		String queryString = "from x:list(1..3), y:list(x..3) report x,y end";
		JValue result = evalTestQuery("DependentDeclarations1", queryString);
		assertEquals(6, result.toCollection().size());
		result.toCollection().toJValueSet();
		JValue resultWO = evalTestQuery("DependentDeclarations (wo)",
				queryString, new VariableDeclarationOrderOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateDependentDeclarations2() throws Exception {
		String queryString = "from airport: V{junctions.Airport}, destination: -->{connections.AirRoute} airport reportSet destination end";
		JValue result = evalTestQuery(queryString);
		assertEquals(airportCount - 1, result.toCollection().size());
		JValueSet set = result.toCollection().toJValueSet();
		for (Airport airport : ((RouteMap) getTestGraph(TestVersion.ROUTE_MAP_GRAPH))
				.getAirportVertices()) {
			if (airport.getDegree(AirRoute.class, EdgeDirection.OUT) != 0) {
				assertTrue(set.contains(new JValueImpl(airport)));
			}
		}
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateDependentDeclarations3() throws Exception {
		String queryString = "from airport: V{junctions.Airport}, destination: <--{connections.AirRoute} airport reportSet airport end";
		JValue result = evalTestQuery(queryString);
		assertEquals(airportCount - 1, result.toCollection().size());
		JValueSet set = result.toCollection().toJValueSet();
		for (Airport airport : ((RouteMap) getTestGraph(TestVersion.ROUTE_MAP_GRAPH))
				.getAirportVertices()) {
			if (airport.getDegree(AirRoute.class, EdgeDirection.OUT) != 0) {
				assertTrue(set.contains(new JValueImpl(airport)));
			}
		}
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateOptionalPathDescription(OptionalPathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateOptionalPathDescription() throws Exception {
		String queryString = "from junction: V{junctions.Junction}, airport: V{junctions.Airport} with junction [ -->{connections.Street} ] -->{connections.AirRoute} airport report junction end";
		JValue result = evalTestQuery(queryString);
		assertEquals(3, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateOptionalPathDescription2() throws Exception {
		String queryString = "from junction: V{junctions.Junction}, airport: V{junctions.Airport} with junction  -->{connections.Street}  [-->{connections.AirRoute}] airport report junction end";
		JValue result = evalTestQuery(queryString);
		assertEquals(0, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateEdgePathDescription(EdgePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluatePrimaryPathDescription1() throws Exception {
		String queryString = "from town: V{localities.Town}, plaza: V{junctions.Plaza} with town -->{localities.ContainsCrossroad} plaza report town end";
		JValue result = evalTestQuery(queryString);
		assertEquals(plazaCount, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateEdgePathDescription(EdgePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluatePrimaryPathDescription2() throws Exception {
		String queryString = "from town: V{localities.Town}, plaza: V{junctions.Plaza} with town --> plaza report town end";
		JValue result = evalTestQuery(queryString);
		assertEquals(plazaCount, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateEdgePathDescription(EdgePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluatePrimaryPathDescription3() throws Exception {
		String queryString = "from town: V{localities.Town}, plaza: V{junctions.Plaza} with town --> {connections.AirRoute} plaza report town end";
		JValue result = evalTestQuery(queryString);
		assertEquals(0, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateEdgePathDescription(EdgePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluatePrimaryPathDescription4() throws Exception {
		String queryString = "from origin: V{junctions.Crossroad}, target: V{junctions.Plaza} with origin <->{connections.Footpath!} target report origin end";
		JValue result = evalTestQuery(queryString);
		assertEquals(1, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateQuantifiedExpression1() throws Exception {
		JValueSet set = new JValueSet();
		set.add(new JValueImpl(false));
		set.add(new JValueImpl(true));
		set.add(new JValueImpl(false));
		setBoundVariable("FOO", set);
		String queryString = "using FOO: forall s: FOO @ s = false";
		JValue result = evalTestQuery("QuantifiedExpression1", queryString);
		assertFalse(result.toBoolean());
		JValue resultWO = evalTestQuery("QuantifiedExpression1 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateQuantifiedExpression(QuantifiedExpression,
	 * Graph)'
	 */
	@Test
	public void testEvaluateQuantifiedExpression2() throws Exception {
		JValueSet set = new JValueSet();
		set.add(new JValueImpl(false));
		set.add(new JValueImpl(true));
		set.add(new JValueImpl(false));
		setBoundVariable("FOO", set);
		String queryString = "using FOO: exists s: FOO @ s = false";
		JValue result = evalTestQuery("QuantifiedExpression2", queryString);
		assertTrue(result.toBoolean());
		JValue resultWO = evalTestQuery("QuantifiedExpression2 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateQuantifiedExpression(QuantifiedExpression,
	 * Graph)'
	 */
	@Test
	public void testEvaluateQuantifiedExpression3() throws Exception {
		JValueSet set = new JValueSet();
		set.add(new JValueImpl(false));
		set.add(new JValueImpl(true));
		set.add(new JValueImpl(false));
		setBoundVariable("FOO", set);
		String queryString = "using FOO: exists s: FOO @ s = false";
		JValue result = evalTestQuery("QuantifiedExpression3", queryString);
		assertTrue(result.toBoolean());
		JValue resultWO = evalTestQuery("QuantifiedExpression3 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateQuantifiedExpression(QuantifiedExpression,
	 * Graph)'
	 */
	@Test
	public void testEvaluateQuantifiedExpression4() throws Exception {
		JValueSet set = new JValueSet();
		set.add(new JValueImpl(2));
		set.add(new JValueImpl(1));
		set.add(new JValueImpl(3));
		set.add(new JValueImpl(4));
		setBoundVariable("FOO", set);
		String queryString = "using FOO: exists! s: FOO @ s = 3";
		JValue result = evalTestQuery("QuantifiedExpression4", queryString);
		assertTrue(result.toBoolean());
		JValue resultWO = evalTestQuery("QuantifiedExpression4 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateRecordConstruction(RecordConstruction,
	 * Graph)'
	 */
	@Test
	public void testEvaluateRecordConstruction() throws Exception {
		String queryString = "rec ( menue1:\"bratwurst\", menue2:\"currywurst\", menue3:\"steak\", menue4:\"kaenguruhfleisch\", menue5:\"spiessbraten\")";
		JValue result = evalTestQuery("RecordConstruction", queryString);
		assertEquals(5, result.toCollection().size());
		JValueRecord rec = result.toCollection().toJValueRecord();
		assertEquals("bratwurst", rec.get("menue1").toString());
		assertEquals("currywurst", rec.get("menue2").toString());
		assertEquals("steak", rec.get("menue3").toString());
		assertEquals("kaenguruhfleisch", rec.get("menue4").toString());
		assertEquals("spiessbraten", rec.get("menue5").toString());
		JValue resultWO = evalTestQuery("RecordConstruction (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateSequentialPathDescription(SequentialPathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateSequentialPathDescription1() throws Exception {
		String queryString = "from county: V{localities.County}, airport: V{junctions.Airport} with county -->{localities.ContainsLocality} -->{connections.AirRoute} airport report county end";
		JValue result = evalTestQuery(queryString);
		assertEquals(airportCount, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateSequentialPathDescription(SequentialPathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateSequentialPathDescription2() throws Exception {
		String queryString = "from var: V{Variable}, def: V{WhereExpression} with var  -->{IsBoundExprOf} -->{IsDefinitionOf} def report var end";
		JValue result = evalTestQuery("SequentialPathDescription2", queryString);
		assertEquals(0, result.toCollection().size());
		JValue resultWO = evalTestQuery("SequentialPathDescription2 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateSetComprehension(SetComprehension,
	 * Graph)'
	 */
	@Test
	public void testEvaluateSetComprehension() throws Exception {
		PVector<String> list = createListWithMeat();
		setBoundVariable("FOO", list);
		String queryString = "using FOO: from i: toSet(FOO) reportSet i end";
		JValue result = evalTestQuery("SetComprehension", queryString);
		assertEquals(8, result.toCollection().size());
		for (JValue v : list) {
			assertTrue(result.toCollection().contains(v));
		}
		JValue resultWO = evalTestQuery("SetComprehension (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateSetConstruction(SetConstruction,
	 * Graph)'
	 */
	@Test
	public void testEvaluateSetConstruction() throws Exception {
		String queryString = "set ( 1 , 2 , 3 , 7 , 34, 456, 7, 5, 455, 456, 457, 1, 2, 3 )";
		JValue result = evalTestQuery("SetConstruction", queryString);
		assertEquals(9, result.toCollection().size());
		assertTrue(result.toCollection().contains(new JValueImpl(1)));
		assertTrue(result.toCollection().contains(new JValueImpl(2)));
		assertTrue(result.toCollection().contains(new JValueImpl(3)));
		assertTrue(result.toCollection().contains(new JValueImpl(5)));
		assertTrue(result.toCollection().contains(new JValueImpl(7)));
		assertTrue(result.toCollection().contains(new JValueImpl(34)));
		assertTrue(result.toCollection().contains(new JValueImpl(455)));
		assertTrue(result.toCollection().contains(new JValueImpl(456)));
		assertTrue(result.toCollection().contains(new JValueImpl(457)));
		JValue resultWO = evalTestQuery("SetConstruction (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateSimplePathDescription(SimplePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateSimplePathDescription1() throws Exception {
		String queryString = "from county: V{localities.County}, airport: V{junctions.Airport} with county --> airport report county end";
		JValue result = evalTestQuery(queryString);
		assertEquals(airportCount, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateSimplePathDescription2() throws Exception {
		String queryString = "from var: V{Definition}, def: V{LetExpression} with var --> def report var end";
		JValue result = evalTestQuery("SimplePathDescription2", queryString);
		assertEquals(0, result.toCollection().size());
		JValue resultWO = evalTestQuery("SimplePathDescription2 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateSimplePathDescription3() throws Exception {
		String queryString = "from county: V{localities.County}, airport: V{junctions.Airport, localities.Town} with county --> airport report county end";
		JValue result = evalTestQuery(queryString);
		assertEquals(airportCount + townCount, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateAggregationPathDescription1() throws Exception {
		String queryString = "from county: V{localities.County}, loc: V{localities.Locality} with loc --<> county report loc end";
		JValue result = evalTestQuery(queryString);
		assertEquals(localityCount, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result.toJValueSet(), resultWO.toJValueSet());
	}

	@Test
	public void testEvaluateAggregationPathDescription2() throws Exception {
		String queryString = "from county: V{localities.County}, loc: V{localities.Locality} with county <>-- loc report loc end";
		JValue result = evalTestQuery(queryString);
		assertEquals(localityCount, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result.toJValueSet(), resultWO.toJValueSet());
	}

	@Test
	public void testEvaluateAggregationPathDescriptionWithRole()
			throws Exception {
		String queryString = "from var: V{Variable}, def: V{Definition} with def <>--{undefeeeinedRole} var report var end";
		JValue result = evalTestQuery("SimplePathDescription2", queryString);
		assertEquals(0, result.toCollection().size());
		JValue resultWO = evalTestQuery("SimplePathDescription2 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateStartRestriction5() throws Exception {
		String queryString1 = "from county: V{localities.County}, plaza: V{junctions.Plaza} with county -->{localities.ContainsLocality} {localities.Town} & -->{localities.ContainsCrossroad} plaza report county end";
		String queryString2 = "from county: V{localities.County}, plaza: V{junctions.Plaza} with contains(-->{localities.ContainsLocality} {localities.Town} & -->{localities.ContainsCrossroad} plaza, county) report county end";
		JValue result = evalTestQuery(queryString1);
		assertEquals(plazaCount, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString2);
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateStartGoalRestriction1() throws Exception {
		String queryString1 = "from var: V, def: V with var -->{Edge} {Definition} & --> def report var end";
		String queryString2 = "from var: V, def: V with contains(-->{Edge} {Definition} & --> def, var) report var end";
		String queryString3 = "from var: V, def: V with contains(var -->{Edge} {Definition} & -->, def) report var end";
		JValue result1 = evalTestQuery("StartRestriction6 1", queryString1);
		JValue result2 = evalTestQuery("StartRestriction6 2", queryString2);
		JValue result3 = evalTestQuery("StartRestriction6 3", queryString3);
		assertEquals(result1, result2);
		assertEquals(result1, result3);

		String queryString4 = "from var: V, def: V with var --> & {Definition} --> def report var end";
		String queryString5 = "from var: V, def: V with contains(--> & {Definition} --> def, var) report var end";
		String queryString6 = "from var: V, def: V with contains(var --> & {Definition} -->, def) report var end";
		JValue result4 = evalTestQuery("GoalRestriction6 1", queryString4);
		JValue result5 = evalTestQuery("GoalRestriction6 2", queryString5);
		JValue result6 = evalTestQuery("GoalRestriction6 3", queryString6);
		assertEquals(result4, result5);
		assertEquals(result4, result6);

		String queryString7 = "from var: V, def: V with var {Variable} & --> def report var end";
		String queryString8 = "from var: V, def: V with contains(var {Variable} & -->, def) report var end";
		String queryString9 = "from var: V, def: V with contains({Variable} & --> def, var) report var end";
		JValue result7 = evalTestQuery("StartRestriction6 4", queryString7);
		JValue result8 = evalTestQuery("StartRestriction6 5", queryString8);
		JValue result9 = evalTestQuery("StartRestriction6 6", queryString9);
		assertEquals(result7, result8);
		assertEquals(result7, result9);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateSequentialPathDescription(SequentialPathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateStartRestriction2() throws Exception {
		String queryString = "from var: V{Variable}, def: V{WhereExpression} with var  -->{IsVarOf} {DefinitionExpression} & -->{IsDefinitionOf} def report var end";
		JValue result = evalTestQuery("StartRestriction2", queryString);
		assertEquals(0, result.toCollection().size());
		JValue resultWO = evalTestQuery("StartRestriction2 (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateSequentialPathDescription(SequentialPathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateStartRestriction3() throws Exception {
		String queryString = "from var: V{Variable}, def: V{WhereExpression} with var  -->{IsVarOf} & {@1 = 2} -->{IsDefinitionOf} def  report var end";
		JValue result = evalTestQuery("StartRestriction3", queryString);
		assertEquals(0, result.toCollection().size());
		JValue resultWO = evalTestQuery("StartRestriction3 (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateSequentialPathDescription(SequentialPathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateStartRestriction4() throws Exception {
		String queryString = "from county: V{localities.County}, plaza: V{junctions.Plaza} with county -->{localities.ContainsLocality} {@ 1 = 1} & -->{localities.ContainsCrossroad} plaza report county end";
		JValue result = evalTestQuery(queryString);
		assertEquals(plazaCount, result.toCollection().size());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateTableComprehension() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from d:V{Definition} report d as \"Definition\" end";
		JValue result = evalTestQuery("TableComprehension", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("TableComprehension (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateTableComprehension2() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from d:V{Definition}, w:V{WhereExpression} with d --> w report d as \"name\", w as \"where\" end";
		JValue result = evalTestQuery("TableComprehension2", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("TableComprehension2 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateTransposedPathDescription(TransposedPathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateTransposedPathDescription() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from def: V{Definition}, whe: V{WhereExpression} with def <--{IsDefinitionOf}^T whe report def end";
		JValue result = evalTestQuery("TransposedPathDescription", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("TransposedPathDescription (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateTupleConstruction(TupleConstruction,
	 * Graph)'
	 */
	@Test
	public void testEvaluateTupleConstruction() throws Exception {
		String queryString = "tup ( \"bratwurst\",\"currywurst\", \"steak\", \"kaenguruhfleisch\", \"spiessbraten\", \"kaenguruhfleisch\")";
		JValue result = evalTestQuery("TupleConstruction", queryString);
		assertEquals(6, result.toCollection().size());
		JValueTuple tup = result.toCollection().toJValueTuple();
		assertEquals("bratwurst", tup.get(0).toString());
		assertEquals("currywurst", tup.get(1).toString());
		assertEquals("steak", tup.get(2).toString());
		assertEquals("kaenguruhfleisch", tup.get(3).toString());
		assertEquals("spiessbraten", tup.get(4).toString());
		assertEquals("kaenguruhfleisch", tup.get(5).toString());
		JValue resultWO = evalTestQuery("TupleConstruction (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateListComprehension(ListComprehension,
	 * Graph)'
	 */
	@Test
	public void testEvaluateVarTableComprehension1() throws Exception {
		JValueList list = new JValueList();
		list.add(new JValueImpl(3));
		list.add(new JValueImpl(4));
		list.add(new JValueImpl(5));
		setBoundVariable("FOO", list);
		String queryString = "using FOO: from i:FOO, j:FOO reportTable i,j,i*j end";
		JValue result = evalTestQuery("VarTableComprehension1", queryString);
		assertEquals(3, result.toCollection().size());
		for (JValue v : result.toCollection()) {
			assertEquals(4, v.toCollection().size());
		}
		JValue resultWO = evalTestQuery("VarTableComprehension1 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateVarTableComprehension2() throws Exception {
		JValueList list = new JValueList();
		list.add(new JValueImpl(3));
		list.add(new JValueImpl(4));
		list.add(new JValueImpl(5));
		setBoundVariable("FOO", list);
		String queryString = "using FOO: from i:FOO, j:FOO reportTable i,j,i*j,\"MultiplicationMatrix\" end";
		JValue result = evalTestQuery("VarTableComprehension2", queryString);
		assertEquals(3, result.toCollection().size());
		for (JValue v : result.toCollection()) {
			assertEquals(4, v.toCollection().size());
		}
		JValueTable resultTable = (JValueTable) result;
		resultTable.printTable();
		JValue resultWO = evalTestQuery("VarTableComprehension2 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateVertexSetExpression(VertexSetExpression,
	 * Graph)'
	 */
	@Test
	public void testEvaluateVertexSetExpression() throws Exception {
		String queryString = "from i: V{localities.County} report i.name end";
		JValue result = evalTestQuery(queryString);
		containsAllElements(COUNTIES, result.toCollection());

		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateEdgeSetExpression(EdgeSetExpression,
	 * Graph)'
	 */
	@Test
	public void testEvaluateVertexSubgraphExpression1() throws Exception {
		String queryString = "from i:V{NamedElement} in vSubgraph{localities.Locality, ^localities.City} report i.name end";
		JValue result = evalTestQuery(queryString);
		containsAllElements(LOCALITIES_WITHOUT_CITIES, result.toCollection());

		JValue resultWO = evalTestQuery("VertexSubgraphExpression1 (wo)",
				queryString, new DefaultOptimizer(),
				TestVersion.ROUTE_MAP_GRAPH);
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateVertexSubgraphExpression2() throws Exception {
		String queryString = "from i:V{NamedElement} in vSubgraph{^localities.Locality} report i.name end";
		JValue result = evalTestQuery(queryString);
		containsAllElements(COUNTIES, result.toCollection());

		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateVertexSubgraphExpression3() throws Exception {
		String queryString = "from i:V{localities.County} in vSubgraph{localities.County} report i.name end";
		JValue result = evalTestQuery(queryString);

		containsAllElements(COUNTIES, result.toCollection());
		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateVertexSubgraphExpression4() throws Exception {
		String queryString = "from i:V{Identifier} in vSubgraph{Definition} report i.name end";
		JValue result = evalTestQuery("VertexSubgraphExpression4", queryString);
		assertEquals(0, result.toCollection().size());
		JValue resultWO = evalTestQuery("VertexSubgraphExpression4 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateVertexSubgraphExpression5() throws Exception {
		String queryString = "from i:V{localities.Locality} in vSubgraph{^localities.City} report i.name end";
		JValue result = evalTestQuery(queryString);

		containsAllElements(LOCALITIES_WITHOUT_CITIES, result.toCollection());

		JValue resultWO = evalQueryWithOptimizer(queryString);
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateWhereExpression() throws Exception {
		String queryString = "from i:c report i end where c:=b, b:=a, a:=\"Mensaessen\"";
		JValue result = evalTestQuery("WhereExpression", queryString);
		assertEquals(1, result.toCollection().size());
		JValueList list = result.toCollection().toJValueList();
		assertEquals("Mensaessen", list.get(0).toString());
		JValue resultWO = evalTestQuery("WhereExpression (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testMultipleEvaluation() throws Exception {
		String queryString = "from i:c report i end where c:=b, b:=a, a:=\"Mensaessen\"";
		String queryString2 = "from i:c report i end where c:=b, b:=a, a:=\"Mensaessen\"";
		JValue result = evalTestQuery("WhereExpression", queryString);
		assertEquals(1, result.toCollection().size());
		JValueList list = result.toCollection().toJValueList();
		assertEquals("Mensaessen", list.get(0).toString());
		JValue resultWO = evalTestQuery("MultipleEvaluation (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
		result = evalTestQuery("MultipleEvaluation", queryString2);
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateFunctionApplication(FunctionApplication,
	 * Graph)'
	 */
	@Test
	public void testMultipleEvaluationStarts() throws Exception {
		String queryString = "from i: V{Identifier} report i.name end";
		Graph datagraph = getTestGraph(TestVersion.GREQL_GRAPH);
		GreqlEvaluator eval = new GreqlEvaluator(queryString, datagraph, null);
		eval.startEvaluation();
		eval.startEvaluation();
		eval.startEvaluation();
		eval.startEvaluation();
		eval.startEvaluation();
		eval.getResult();
		eval.startEvaluation();
		eval.startEvaluation();
		eval.startEvaluation();
		eval.startEvaluation();
		eval.startEvaluation();
	}

	/*
	 * Test method for 'greql2.evaluator.GreqlEvaluator.parseQuery(Greql2Lexer)'
	 */
	@Test
	public void testParseQuery() throws Exception {
		String queryString = "from i: V{Identifier} report i.name end";
		evalTestQuery("ParseQuery", queryString);
	}

	/*
	 * Test method for 'greql2.evaluator.GreqlEvaluator.startEvaluation()'
	 */
	@Test
	public void testStartEvaluation() throws Exception {
		String queryString = "from i: V{Identifier} report i.name end";
		JValue result = evalTestQuery("StartEvaluation", queryString);
		assertTrue(result != null);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateForwardVertexSet(ForwardVertexSet,
	 * Graph)'
	 */
	@Test
	public void testStore() throws Exception {
		String queryString = "from v: V{localities.County} report v.name end store as CountyNames";
		evalTestQuery(queryString);
		JValueList storedList = getBoundVariable("CountyNames").toCollection()
				.toJValueList();

		containsAllElements(COUNTIES, storedList);
	}

	@Test
	public void testTableComprehension() throws Exception {
		String queryString = "from x,y:list(1..10) reportTable \"X\", \"Y\", x*y end";
		JValue result = evalTestQuery("TableComprehension", queryString);
		assertTrue(result.toCollection().isJValueTable());

	}

	@Test
	public void testMapComprehension() throws Exception {
		String queryString = "from x : set(1, 2, 3, 4, 5) reportMap x -> x*x end";
		JValue result = evalTestQuery("MapComprehension", queryString);
		assertTrue(result.isMap());
		JValueMap map = result.toJValueMap();
		assertEquals(5, map.size());
		assertEquals(new JValueImpl(1), map.get(new JValueImpl(1)));
		assertEquals(new JValueImpl(4), map.get(new JValueImpl(2)));
		assertEquals(new JValueImpl(9), map.get(new JValueImpl(3)));
		assertEquals(new JValueImpl(16), map.get(new JValueImpl(4)));
		assertEquals(new JValueImpl(25), map.get(new JValueImpl(5)));
	}

	@Test
	public void testMapComprehension2() throws Exception {
		String queryString = "from x : V{localities.County} reportMap x.name -> x end";
		JValueMap map = evalTestQuery(queryString).toJValueMap();

		containsAllKeys(COUNTIES, map);
	}

	private void containsAllKeys(Object[] counties, JValueMap map) {
		assertEquals(counties.length, map.size());
		containsAllElements(counties, map.keySet());
	}

	private void containsAllElements(Object[] elements,
			JValueCollection collection) {

		JValueList list = new JValueList(collection);
		assertEquals(elements.length, list.size());
		for (Object county : elements) {
			assertTrue(list.remove(new JValueImpl(county)));
		}
		assertTrue(list.isEmpty());
	}

	@Test
	public void testMapComprehension3() throws Exception {
		// GreqlEvaluator.DEBUG_DECLARATION_ITERATIONS = true;
		String queryString = "from x : list(1..3) reportMap x -> from y : list(1..x) reportMap y -> list(y..x) end end";
		JValue result = evalTestQuery("MapComprehension3", queryString);
		JValueMap map = result.toJValueMap();

		assertEquals(3, map.size());

		JValueMap resultVal1 = map.get(new JValueImpl(1)).toJValueMap();
		assertEquals(1, resultVal1.size());
		assertEquals(1, resultVal1.get(new JValueImpl(1)).toJValueList().size());

		JValueMap resultVal2 = map.get(new JValueImpl(2)).toJValueMap();
		assertEquals(2, resultVal2.size());
		assertEquals(2, resultVal2.get(new JValueImpl(1)).toJValueList().size());
		assertEquals(1, resultVal2.get(new JValueImpl(2)).toJValueList().size());

		JValueMap resultVal3 = map.get(new JValueImpl(3)).toJValueMap();
		assertEquals(3, resultVal3.size());
		assertEquals(3, resultVal3.get(new JValueImpl(1)).toJValueList().size());
		assertEquals(2, resultVal3.get(new JValueImpl(2)).toJValueList().size());
		assertEquals(1, resultVal3.get(new JValueImpl(3)).toJValueList().size());
	}

	@Test
	public void testQueryWithoutDatagraph() throws Exception {
		String queryString = "(3 + 4) * 7";
		JValue result = evalTestQuery("QueryWithoutDatagraph", queryString,
				(Graph) null);
		assertEquals(49, result.toInteger().intValue());
	}

	@Test
	public void testEquivalentQueries() throws Exception {
		String query1 = "from x : list(1..5)                  "
				+ "      with isPrime(x)                      "
				+ "      reportSet x, from y : list(21..25),  "
				+ "                        z : list(21..30)   "
				+ "                   with isPrime(y+x) and isPrime(z+x) "
				+ "                   reportSet y, z end      "
				+ "      end                                  ";
		GreqlEvaluator e1 = new GreqlEvaluator(query1, null, null);
		e1.setOptimize(false);
		e1.startEvaluation();
		JValue r1 = e1.getResult();
		String query2 = "from x : list(1..5)                   "
				+ "      with isPrime(x)                       "
				+ "      reportSet x, from y : from a : list(21..25) with isPrime(a+x) reportSet a end,"
				+ "                        z : from b : list(21..30) with isPrime(b+x) reportSet b end "
				+ "                   reportSet y, z end       "
				+ "      end                                   ";
		GreqlEvaluator e2 = new GreqlEvaluator(query2, null, null);
		e2.setOptimize(false);
		e2.startEvaluation();
		JValue r2 = e2.getResult();

		assertEquals(r1, r2);
	}

	@Test
	public void testWhereWithSameScope() throws Exception {
		String query = "from a,b:list(1..10) with equivalent reportSet a end where equivalent := a=b";
		JValue result = evalTestQuery("WhereWithSameScope", query);
		assertTrue(result.isCollection());
		assertTrue(result.toCollection().isJValueSet());
		JValueSet resSet = result.toJValueSet();
		assertEquals(10, resSet.size());
		for (int i = 1; i < 11; i++) {
			assertTrue(resSet.contains(new JValueImpl(i)));
		}
	}

	@Test
	public void testLetWithSameScope() throws Exception {
		String query = "let equivalent := a=b in from a,b:list(1..10) with equivalent reportSet a end";
		JValue result = evalTestQuery("LetWithSameScope", query);
		assertTrue(result.isCollection());
		assertTrue(result.toCollection().isJValueSet());
		JValueSet resSet = result.toJValueSet();
		assertEquals(10, resSet.size());
		for (int i = 1; i < 11; i++) {
			assertTrue(resSet.contains(new JValueImpl(i)));
		}
	}

	@Test
	public void checkVariableOrder1() throws Exception {
		// Changing the order of the independent variables x and y must not
		// change the result!
		String query = "from a : list(1..10),                     "
				+ "          b : list(1..20)                      "
				+ "     with isPrime(a + 1) and isPrime(b)        "
				+ "          and (exists! x : list(1..30), y : list(10..20), x+a<y+b @ isPrime(x+y)) "
				+ "     reportSet a, b end";
		// x and y were swapped
		String query2 = "from a : list(1..10),                     "
				+ "           b : list(1..20)                      "
				+ "     with isPrime(a + 1) and isPrime(b)        "
				+ "          and (exists! y : list(10..20), x : list(1..30), x+a<y+b @ isPrime(x+y)) "
				+ "     reportSet a, b end";
		assertEquals(evalTestQuery("VariableOrder1", query),
				evalTestQuery("VariableOrder1", query2));
	}

	@Test
	public void checkVariableOrder2() throws Exception {
		// Changing the order of the independent variables a and b must not
		// change the result!
		String query = "from a : list(1..10),                     "
				+ "          b : list(1..20)                      "
				+ "     with isPrime(a + 1) and isPrime(b)        "
				+ "          and (exists! x : list(1..30), y : list(10..20), x+a<y+b @ isPrime(x+y)) "
				+ "     reportSet a, b end";
		// a and b were swapped
		String query2 = "from b : list(1..20),                     "
				+ "               a : list(1..10)                      "
				+ "     with isPrime(a + 1) and isPrime(b)        "
				+ "          and (exists! x : list(1..30), y : list(10..20), x+a<y+b @ isPrime(x+y)) "
				+ "     reportSet a, b end";
		assertEquals(evalTestQuery("VariableOrder2", query),
				evalTestQuery("VariableOrder2", query2));
	}

	/**
	 * Checks if query parsing and optimization is skipped, when executing a
	 * query multiple times with optimization turned on.
	 * 
	 * Also checks that parsing is done every time when optimization is off. In
	 * that case, caches should be disabled.
	 */
	@Test
	public void testParsingAndOptimizationSkipping() throws Exception {
		String query1 = "from x, y, z : list(1..30)     "
				+ "     with isPrime(x) and isPrime(z)"
				+ "     reportSet from a, b : list(x..z)             "
				+ "               with a + b = y                     "
				+ "                    and isPrime(y)                "
				+ "               report a, b, y, b end              "
				+ "     end                                          ";
		String query2 = "from a : list(1..10),                     "
				+ "          b : list(1..20)                      "
				+ "     with isPrime(a + 1) and isPrime(b)        "
				+ "          and (exists! x : list(1..30), y : list(10..20), x+a<y+b @ isPrime(x+y)) "
				+ "     reportSet a, b end";
		GreqlEvaluator eval = new GreqlEvaluator((String) null,
				getTestGraph(TestVersion.GREQL_GRAPH), null);

		Field parseTime = eval.getClass().getDeclaredField("parseTime");
		parseTime.setAccessible(true);
		Field optimizationTime = eval.getClass().getDeclaredField(
				"optimizationTime");
		optimizationTime.setAccessible(true);

		for (int i = 0; i < 6; i++) {
			eval.setQuery((i % 2 == 0) ? query1 : query2);
			eval.startEvaluation();
			eval.getResult();
			// eval.printEvaluationTimes();
			if (i < 2) {
				// The first two times, both parsing and optimizing have to be
				// done!
				assertTrue(parseTime.getLong(eval) > 0);
				assertTrue(optimizationTime.getLong(eval) > 0);
			} else {
				// From that on, there should be no parsing and optimizing
				// anymore, cause the optimized graph is cached.
				assertEquals(0, parseTime.getLong(eval));
				assertEquals(0, optimizationTime.getLong(eval));
			}
		}

		eval.setOptimize(false);
		for (int i = 0; i < 6; i++) {
			eval.setQuery((i % 2 == 0) ? query1 : query2);
			eval.startEvaluation();
			eval.getResult();
			// eval.printEvaluationTimes();
			assertTrue(parseTime.getLong(eval) > 0);
			assertEquals(0, optimizationTime.getLong(eval));
		}
	}

	@Test
	public void testSliceCanConvertToSet() throws Exception {
		evalTestQuery("testSliceCanConvertToSet",
				"count(slice(getVertex(1), <--+))");
		// no exception means success for this test.
	}

	@Test
	public void testPathSystemCanConvertToSet() throws Exception {
		evalTestQuery("testPathSystemCanConvertToSet",
				"count(pathSystem(getVertex(1), <--+))");
		// no exception means success for this test.
	}
}
