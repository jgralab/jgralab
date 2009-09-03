/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
 *
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.uni_koblenz.jgralabtest.greql2.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBag;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueRecord;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTable;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTuple;
import de.uni_koblenz.jgralab.greql2.optimizer.DefaultOptimizer;
import de.uni_koblenz.jgralab.greql2.parser.ManualGreqlParser;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;

public class GreqlEvaluatorTest extends GenericTests {

	private JValueBag createBagWithMeat(List<JValue> list) {
		JValueBag bag = new JValueBag();
		list.add(new JValue("Currywurst"));
		list.add(new JValue("Bratwurst"));
		list.add(new JValue("Käsewurst"));
		list.add(new JValue("Steak"));
		list.add(new JValue("Pommes"));
		list.add(new JValue("Mayo"));
		list.add(new JValue("Ketchup"));
		list.add(new JValue("Zwiebeln"));
		for (JValue v : list) {
			bag.add(v, 3);
		}
		return bag;
	}

	@Test
	public void testEvaluateNullLiteral2() throws Exception {
		String queryString = "null";
		JValue result = evalTestQuery("NullLiteral2", queryString);
		assertFalse(result.isValid());
		// Now check if the optimized query produces the same result.
		JValue resultWO = evalTestQuery("NullLiteral2 (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateNullLiteral() throws Exception {
		String queryString = "from a : set(true, null, false) with a reportSet a end";
		JValue result = evalTestQuery("NullLiteral", queryString);
		assertFalse(result.isValid());
		// Now check if the optimized query produces the same result.
		JValue resultWO = evalTestQuery("NullLiteral (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateAlternativePathDescription(AlternativePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateAlternativePathDescription() throws Exception {
		String queryString = "from var: V{Definition}, def: V{WhereExpression} with var  -->{IsDefinitionOf} | -->{IsVarOf}  def report var end";
		JValue result = evalTestQuery("AlternativePathDescription", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("AlternativePathDescription (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateForwardVertexSet(ForwardVertexSet,
	 * Graph)'
	 */
	@Test
	public void testEvaluateBackwardVertexSet() throws Exception {
		String queryString = "from w: V{WhereExpression} report w <--{IsDefinitionOf} <--{IsVarOf} end";
		JValue result = evalTestQuery("BackwardVertexSet1", queryString);
		assertEquals(1, result.toCollection().size());
		for (JValue j : result.toCollection()) {
			assertEquals(4, j.toCollection().size());
		}
		JValue resultWO = evalTestQuery("BackwardVertexSet1 (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testReachability() throws Exception {
		String queryString = "forall e: E{IsDefinitionOf}"
				+ "  @ startVertex(e) -->{IsDefinitionOf} endVertex(e)";
		JValue result = evalTestQuery("Reachability", queryString);
		// TODO
		 assertEquals(1, result.toCollection().size());
		 for (JValue j : result.toCollection()) {
		 assertEquals(4, j.toCollection().size());
		 }
		 JValue resultWO = evalTestQuery("BackwardVertexSet1 (wo)",
		 queryString,
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
		String queryString = "from w: V{WhereExpression} report w <--{IsDefinitionOf} [<--{IsVarOf}] end";
		JValue result = evalTestQuery("BackwardVertexSet2", queryString);
		assertEquals(1, result.toCollection().size());
		for (JValue j : result.toCollection()) {
			assertEquals(8, j.toCollection().size());
		}
		JValue resultWO = evalTestQuery("BackwardVertexSet2 (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateBagComprehension(BagComprehension,
	 * Graph)'
	 */
	@Test
	public void testEvaluateBagComprehension() throws Exception {
		ArrayList<JValue> list = new ArrayList<JValue>();
		boundVariables.put("FOO", createBagWithMeat(list));
		String queryString = "using FOO: from i: toSet(FOO) report i end";
		JValue result = evalTestQuery("BagComprehension", queryString);
		assertEquals(8, result.toCollection().size());
		for (JValue v : list) {
			assertEquals(1, result.toCollection().toJValueBag().getQuantity(v));
		}
		JValue resultWO = evalTestQuery("BagComprehension (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateBagComprehension(BagComprehension,
	 * Graph)'
	 */
	@Test
	public void testUsing() throws Exception {
		boundVariables.put("FOO", new JValue("A String"));
		String queryString = "using FOO: FOO";
		JValue result = evalTestQuery("Using", queryString);
		assertEquals("A String", result.toString());
		JValue resultWO = evalTestQuery("Using", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateBagComprehension(BagComprehension,
	 * Graph)'
	 */
	@Test
	public void testUsingWithFunction() throws Exception {
		boundVariables.put("FOO", new JValue("A String"));
		String queryString = "using FOO: plus(FOO, \" Another String\")";
		JValue result = evalTestQuery("UsingWithFunction", queryString);
		assertEquals("A String Another String", result.toString());
		JValue resultWO = evalTestQuery("UsingWithFunction", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateBagComprehension(BagComprehension,
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
	 * 'greql2.evaluator.GreqlEvaluator.evaluateBagComprehension(BagComprehension,
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
	 * 'greql2.evaluator.GreqlEvaluator.evaluateBagConstruction(BagConstruction,
	 * Graph)'
	 */
	@Test
	public void testEvaluateBagConstruction() throws Exception {
		String queryString = "bag ( 1 , 2 , 3 , 7 , 34, 456, 7, 5, 455, 456, 457, 1, 2, 3, 3, 3, 3 )";
		JValue result = evalTestQuery("BagConstruction", queryString);
		assertEquals(17, result.toCollection().size());
		assertEquals(2, result.toCollection().toJValueBag().getQuantity(
				new JValue(1)));
		assertEquals(2, result.toCollection().toJValueBag().getQuantity(
				new JValue(2)));
		assertEquals(5, result.toCollection().toJValueBag().getQuantity(
				new JValue(3)));
		assertEquals(1, result.toCollection().toJValueBag().getQuantity(
				new JValue(5)));
		assertEquals(2, result.toCollection().toJValueBag().getQuantity(
				new JValue(7)));
		assertEquals(1, result.toCollection().toJValueBag().getQuantity(
				new JValue(34)));
		assertEquals(1, result.toCollection().toJValueBag().getQuantity(
				new JValue(455)));
		assertEquals(2, result.toCollection().toJValueBag().getQuantity(
				new JValue(456)));
		assertEquals(1, result.toCollection().toJValueBag().getQuantity(
				new JValue(457)));

		JValue resultWO = evalTestQuery("BagConstruction (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateAlternativePathDescription(AlternativePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateComplexePathDescription() throws Exception {
		String queryString = "from var: V{Variable}, def: V{WhereExpression} with var  (-->{IsVarOf} -->{IsDefinitionOf}) | (-->{IsVarOf} -->{IsArgumentOf} -->{IsExprOf}+)  def report var end";
		JValue result = evalTestQuery("ComplexDescription", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("ComplexDescription (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateConditionalExpression(ConditionalExpression,
	 * Graph)'
	 */
	@Test
	public void testEvaluateConditionalExpression() throws Exception {
		String queryString = "1=1?1:2:3";
		JValue result = evalTestQuery("ConditionalExpression", queryString);
		assertEquals(1, (int) result.toInteger());
		JValue resultWO = evalTestQuery("ConditionalExpression (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateConditionalExpression2() throws Exception {
		String queryString = "1=2?1:2:3";
		JValue result = evalTestQuery("ConditionalExpression2", queryString);
		assertEquals(2, (int) result.toInteger());
		JValue resultWO = evalTestQuery("ConditionalExpression2 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateConditionalExpression3() throws Exception {
		String queryString = "1?1:2:3";
		JValue result = evalTestQuery("ConditionalExpression3", queryString);
		assertEquals(3, (int) result.toInteger());
		JValue resultWO = evalTestQuery("ConditionalExpression3 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateEdgePathDescription(EdgePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateEdgePathDescription() throws Exception {
		String queryString = "from edge: E, var: V{Definition}, def: V{WhereExpression} in eSubgraph{IsDefinitionOf!} with var --edge-> def report var end";
		JValue result = evalTestQuery("EdgePathDescription", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("EdgePathDescription (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateEdgePathDescription(EdgePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateEdgePathDescription2() throws Exception {
		String queryString = "from edge: E in eSubgraph{IsDefinitionOf!} report from var: V{Definition}, def: V{WhereExpression} with var --edge-> def report var end end";
		JValue result = evalTestQuery("EdgePathDescription2", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("EdgePathDescription2 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateEdgePathDescription(EdgePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateEdgePathDescription3() throws Exception {
		String queryString = "from edge: E in eSubgraph{IsVarOf} report from var: V{Variable}, def: V{Definition} with var --edge-> def report var end end";
		String queryString2 = "from edge: E in eSubgraph{IsVarOf} report from var: V{Variable}, def: V{Definition} with contains(--edge-> def, var) = true report var end end";
		JValue result = evalTestQuery("EdgePathDescription3", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("EdgePathDescription3 (wo)",
				queryString2/* , new DefaultOptimizer() */);
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateEdgePathDescription(EdgePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateEdgePathDescription4() throws Exception {
		String queryString = "from edge: E in eSubgraph{IsVarOf} report from var: V{Definition} report var end end";
		JValue result = evalTestQuery("EdgePathDescription4", queryString);
		assertEquals(4, result.toCollection().size());
		for (JValue j : result.toCollection()) {
			assertEquals(4, j.toCollection().size());
		}
		JValue resultWO = evalTestQuery("EdgePathDescription4 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateEdgePathDescription(EdgePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateEdgePathDescription5() throws Exception {
		String queryString = "from edge: E in eSubgraph{IsDefinitionOf!} report from var: V{Definition}, def: V{WhereExpression} with contains(--edge-> def, var) report var end end";
		JValue result = evalTestQuery("EdgePathDescription5", queryString);
		System.out.println(result);
		// assertEquals(4, result.toCollection().size());
		// JValue resultWO = evalTestQuery("EdgePathDescription3 (wo)",
		// queryString, new DefaultOptimizer());
		// assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateEdgeSetExpression(EdgeSetExpression,
	 * Graph)'
	 */
	@Test
	public void testEvaluateEdgeSetExpression() throws Exception {
		String queryString = "from i:E{IsDefinitionOf} report i end";
		JValue result = evalTestQuery("EdgeSetExpression", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("EdgeSetExpression (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateEdgeSubgraphExpression1() throws Exception {
		String queryString = "from i: E in eSubgraph{IsDefinitionOf} report i end";
		JValue result = evalTestQuery("EdgeSubgraphExpression1", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("EdgeSubgraphExpression1 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateEdgeSubgraphExpression2() throws Exception {
		String queryString = "from i: V in eSubgraph{IsDefinitionOf} report i end";
		JValue result = evalTestQuery("EdgeSubgraphExpression2", queryString);
		assertEquals(5, result.toCollection().size());
		JValue resultWO = evalTestQuery("EdgeSubgraphExpression2 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateEdgeSubgraphExpression3() throws Exception {
		String queryString = "from i: V in eSubgraph{^IsDefinitionOf} report i end";
		JValue result = evalTestQuery("EdgeSubgraphExpression3", queryString);
		assertEquals(16, result.toCollection().size()); /*
														 * with new parser only
														 * 16
														 */
		JValue resultWO = evalTestQuery("EdgeSubgraphExpression3 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateExponentiatedPathDescription1() throws Exception {
		String queryString = "from def: V{Definition}, whe: V{WhereExpression} with def -->{IsDefinitionOf}^1 whe report def end";
		JValue result = evalTestQuery("ExponentiatedPathDescription1",
				queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("ExponentiatedPathDescription1 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateExponentiatedPathDescription2() throws Exception {
		String queryString = "from def: V{Definition}, whe: V{WhereExpression} with def -->{IsDefinitionOf}^2 whe report def end";
		JValue result = evalTestQuery("ExponentiatedPathDescription2",
				queryString);
		assertEquals(0, result.toCollection().size());
		JValue resultWO = evalTestQuery("ExponentiatedPathDescription2 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateExponentiatedPathDescription3() throws Exception {
		String queryString = "from def: V{Definition}, whe: V{WhereExpression} with def -->{IsDefinitionOf}^3 whe report def end";
		JValue result = evalTestQuery("ExponentiatedPathDescription3",
				queryString);
		assertEquals(0, result.toCollection().size());
		JValue resultWO = evalTestQuery("ExponentiatedPathDescription3 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateForwardVertexSet(ForwardVertexSet,
	 * Graph)'
	 */
	@Test
	public void testEvaluateForwardVertexSet() throws Exception {
		String queryString = "from var: V{Variable} report var --> end";
		JValue result = evalTestQuery("ForwardVertexSet", queryString);
		assertEquals(5, result.toCollection().size());
		JValue resultWO = evalTestQuery("ForwardVertexSet (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateFunctionApplication(FunctionApplication,
	 * Graph)'
	 */
	@Test
	public void testEvaluateFunctionApplication() throws Exception {
		String queryString = "using FOO: from i: V{Identifier} report i.name end";
		JValue result = evalTestQuery("FunctionApplication", queryString);
		assertEquals(5, result.toCollection().size());
		JValue resultWO = evalTestQuery("FunctionApplication (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateSequentialPathDescription(SequentialPathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateGoalRestriction1() throws Exception {
		String queryString = "from var: V{Variable}, def: V{WhereExpression} with var  -->{IsVarOf} & {Definition} -->{IsDefinitionOf} def report var end";
		JValue result = evalTestQuery("GoalRestriction1", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("GoalRestriction1 (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateSequentialPathDescription(SequentialPathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateRestrictedExpression() throws Exception {
		String queryString = "from var: V{Variable}, def: V{WhereExpression} with var & {hasType(thisVertex, \"Variable\")} -->{IsVarOf} -->{IsDefinitionOf} def report var end";
		JValue result = evalTestQuery("RestrictedExpression", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("RestrictedExpression", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
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
		String queryString = "from var: V{Variable}, def: V{WhereExpression} with var  -->{IsVarOf} & {type(thisEdge)} def report var end";
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

	@Test
	public void testEvaluateIntermediateVertexDescription() throws Exception {
		String queryString = "from var: V{Variable}, def:V{Definition}, whr: V{WhereExpression} with var -->{IsVarOf} def -->{IsDefinitionOf} whr report var end";
		JValue result = evalTestQuery("IntermediateVertexDescription",
				queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("IntermediateVertexDescription (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateIntermediateVertexPathDescription()
			throws Exception {
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateIteratedPathDescription(IteratedPathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateIteratedPathDescription1() throws Exception {
		String queryString = "from var: V{Definition}, def: V{WhereExpression} with var  -->{IsDefinitionOf}* def report var end";
		JValue result = evalTestQuery("IteratedPathDescription1", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("IteratedPathDescription1 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
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
		String queryString = "from var: V{Definition}, def: V{WhereExpression} with var  -->{IsDefinitionOf}+ def report var end";
		JValue result = evalTestQuery("IteratedPathDescription3", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("IteratedPathDescription3 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateIteratedPathDescription4() throws Exception {
		String queryString = "from var: V{Definition}, def: V{WhereExpression} with var -->{IsVarOf}* -->{IsDefinitionOf} def report var end";
		JValue result = evalTestQuery("IteratedPathDescription4", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("IteratedPathDescription4 (wo)",
				queryString, new DefaultOptimizer());
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

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateOptionalPathDescription(OptionalPathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateOptionalPathDescription() throws Exception {
		String queryString = "from var: V{Definition}, def: V{WhereExpression} with var [ -->{IsVarOf} ] -->{IsDefinitionOf} def report var end";
		JValue result = evalTestQuery("OptionalPathDescription1", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("OptionalPathDescription1 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateOptionalPathDescription2() throws Exception {
		String queryString = "from var: V{Definition}, def: V{WhereExpression} with var -->{IsVarOf} [-->{IsDefinitionOf}] def report var end";
		JValue result = evalTestQuery("OptionalPathDescription2", queryString);
		assertEquals(0, result.toCollection().size());
		JValue resultWO = evalTestQuery("OptionalPathDescription2 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateEdgePathDescription(EdgePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluatePrimaryPathDescription1() throws Exception {
		String queryString = "from var: V{Definition}, def: V{WhereExpression} with var -->{IsDefinitionOf} def report var end";
		JValue result = evalTestQuery("PrimaryPathDescription1", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("PrimaryPathDescription1 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateEdgePathDescription(EdgePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluatePrimaryPathDescription2() throws Exception {
		String queryString = "from def: V{Definition}, whe: V{WhereExpression} with def -->{IsDefinitionOf} whe report def end";
		JValue result = evalTestQuery("PrimaryPathDescription2", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("PrimaryPathDescription2 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateEdgePathDescription(EdgePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluatePrimaryPathDescription4() throws Exception {
		String queryString = "from var: V{Definition}, def: V{WhereExpression} with var --> def report var end";
		JValue result = evalTestQuery("PrimaryPathDescription4", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("PrimaryPathDescription4 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateEdgePathDescription(EdgePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluatePrimaryPathDescription5() throws Exception {
		String queryString = "from var: V{Definition}, def: V{WhereExpression} with var -->{IsExprOf} def report var end";
		JValue result = evalTestQuery("PrimaryPathDescription5", queryString);
		assertEquals(0, result.toCollection().size());
		JValue resultWO = evalTestQuery("PrimaryPathDescription5 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateEdgePathDescription(EdgePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluatePrimaryPathDescription6() throws Exception {
		String queryString = "from var: V{Definition}, def: V{WhereExpression} with var -->{IsDefinitionOf!} def report var end";
		JValue result = evalTestQuery("PrimaryPathDescription6", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("PrimaryPathDescription6 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateQuantifiedExpression1() throws Exception {
		JValueSet set = new JValueSet();
		set.add(new JValue(false));
		set.add(new JValue(true));
		set.add(new JValue(false));
		boundVariables.put("FOO", set);
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
		set.add(new JValue(false));
		set.add(new JValue(true));
		set.add(new JValue(false));
		boundVariables.put("FOO", set);
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
		set.add(new JValue(false));
		set.add(new JValue(true));
		set.add(new JValue(false));
		boundVariables.put("FOO", set);
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
	public void testEvaluateQuantifiedExpression5() throws Exception {
		JValueMap map = new JValueMap();
		map.put(new JValue(1), new JValue(JValueBoolean.getNullValue()));
		map.put(new JValue(2), new JValue(true));
		boundVariables.put("FOO", map);
		String queryString = "using FOO: exists! s:list(1,2) @ get(FOO, s)";
		JValue result = evalTestQuery("QuantifiedExpression5", queryString);
		System.out.println("Result is: " + result);
		assertNull(result.toBoolean());
		JValue resultWO = evalTestQuery("QuantifiedExpression5 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}
	
	@Test
	public void testEvaluateQuantifiedExpression6() throws Exception {
		JValueMap map = new JValueMap();
		map.put(new JValue(1), new JValue(JValueBoolean.getNullValue()));
		map.put(new JValue(2), new JValue(JValueBoolean.getNullValue()));
		boundVariables.put("FOO", map);
		String queryString = "using FOO: exists! s:list(1,2) @ get(FOO, s)";
		JValue result = evalTestQuery("QuantifiedExpression6", queryString);
		assertNull(result.toBoolean());
		JValue resultWO = evalTestQuery("QuantifiedExpression6 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	
	@Test
	public void testEvaluateQuantifiedExpression7() throws Exception {
		JValueMap map = new JValueMap();
		map.put(new JValue(1), new JValue(JValueBoolean.getNullValue()));
		map.put(new JValue(2), new JValue(JValueBoolean.getNullValue()));
		boundVariables.put("FOO", map);
		String queryString = "using FOO: exists s:list(1,2) @ get(FOO, s)";
		JValue result = evalTestQuery("QuantifiedExpression7", queryString);
		assertNull(result.toBoolean());
		JValue resultWO = evalTestQuery("QuantifiedExpression7 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}
	
	@Test
	public void testEvaluateQuantifiedExpression8() throws Exception {
		JValueMap map = new JValueMap();
		map.put(new JValue(1), new JValue(JValueBoolean.getNullValue()));
		map.put(new JValue(2), new JValue(JValueBoolean.getNullValue()));
		boundVariables.put("FOO", map);
		String queryString = "using FOO: forall s:list(1,2) @ get(FOO, s)";
		JValue result = evalTestQuery("QuantifiedExpression8", queryString);
		assertNull(result.toBoolean());
		JValue resultWO = evalTestQuery("QuantifiedExpression8 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}
	
	@Test
	public void testEvaluateQuantifiedExpression9() throws Exception {
		JValueMap map = new JValueMap();
		map.put(new JValue(1), new JValue(JValueBoolean.getNullValue()));
		map.put(new JValue(2), new JValue(JValueBoolean.getNullValue()));
		boundVariables.put("FOO", map);
		String queryString = "using FOO: forall s:list(1,2) @ get(FOO, s)";
		JValue result = evalTestQuery("QuantifiedExpression9", queryString);
		assertNull(result.toBoolean());
		JValue resultWO = evalTestQuery("QuantifiedExpression9 (wo)",
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
		set.add(new JValue(2));
		set.add(new JValue(1));
		set.add(new JValue(3));
		set.add(new JValue(4));
		boundVariables.put("FOO", set);
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
		String queryString = "from var: V{Variable}, def: V{WhereExpression} with var  -->{IsVarOf} -->{IsDefinitionOf} def report var end";
		JValue result = evalTestQuery("SequentialPathDescription1", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("SequentialPathDescription1 (wo)",
				queryString, new DefaultOptimizer());
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
		ArrayList<JValue> list = new ArrayList<JValue>();
		boundVariables.put("FOO", createBagWithMeat(list));
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
		assertTrue(result.toCollection().contains(new JValue(1)));
		assertTrue(result.toCollection().contains(new JValue(2)));
		assertTrue(result.toCollection().contains(new JValue(3)));
		assertTrue(result.toCollection().contains(new JValue(5)));
		assertTrue(result.toCollection().contains(new JValue(7)));
		assertTrue(result.toCollection().contains(new JValue(34)));
		assertTrue(result.toCollection().contains(new JValue(455)));
		assertTrue(result.toCollection().contains(new JValue(456)));
		assertTrue(result.toCollection().contains(new JValue(457)));
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
		String queryString = "from var: V{Definition}, def: V{WhereExpression} with var --> def report var end";
		JValue result = evalTestQuery("SimplePathDescription1", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("SimplePathDescription1 (wo)",
				queryString, new DefaultOptimizer());
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
		String queryString = "from var: V{Definition}, def: V{BagComprehension, WhereExpression}  with var --> def report var end";
		JValue result = evalTestQuery("SimplePathDescription3", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("SimplePathDescription3 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateStartRestriction1() throws Exception {
		String queryString = "from var: V{Variable}, def: V{WhereExpression} with var -->{IsVarOf} {Definition} & -->{IsDefinitionOf} def report var end";
		JValue result = evalTestQuery("StartRestriction1", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("StartRestriction1 (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateStartRestriction5() throws Exception {
		String queryString = "from var: V{Variable}, def: V{WhereExpression} with var -->{IsVarOf} {Definition} & -->{IsDefinitionOf} def report var end";
		String queryString2 = "from var: V{Variable}, def: V{WhereExpression} with contains(-->{IsVarOf} {Definition} & -->{IsDefinitionOf} def, var) report var end";
		JValue result = evalTestQuery("StartRestriction5", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("StartRestriction5 (wo)", queryString2);
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
		String queryString = "from var: V{Variable}, def: V{WhereExpression} with var  -->{IsVarOf} & {1 = 2} -->{IsDefinitionOf} def  report var end";
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
		String queryString = "from var: V{Variable}, def: V{WhereExpression} with var  -->{IsVarOf} & {1 = 1} -->{IsDefinitionOf} def  report var end";
		JValue result = evalTestQuery("StartRestriction4", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("StartRestriction4 (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateTableComprehension() throws Exception {
		String queryString = "from d:V{Definition} report d as \"Definition\" end";
		JValue result = evalTestQuery("TableComprehension", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("TableComprehension (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateTableComprehension2() throws Exception {
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
		String queryString = "from def: V{Definition}, whe: V{WhereExpression} with def <--{IsDefinitionOf}^T whe report def end";
		JValue result = evalTestQuery("TransposedPathDescription", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("TransposedPathDescription (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateRecordConstruction(RecordConstruction,
	 * Graph)'
	 */
	@Test
	public void testEvaluateTupleAccess() throws Exception {
		String queryString = "let x := tup ( \"bratwurst\", \"currywurst\", \"steak\", \"kaenguruhfleisch\", \"spiessbraten\") in from i:V{Identifier} report x[3] end";
		JValue result = evalTestQuery("TupleAccess", queryString);
		assertEquals(5, result.toCollection().size());
		assertEquals(5, result.toCollection().toJValueBag().getQuantity(
				new JValue("kaenguruhfleisch")));
		JValue resultWO = evalTestQuery("TupleAccess (wo)", queryString,
				new DefaultOptimizer());
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
	 * 'greql2.evaluator.GreqlEvaluator.evaluateBagComprehension(BagComprehension,
	 * Graph)'
	 */
	@Test
	public void testEvaluateVarTableComprehension1() throws Exception {
		JValueBag bag = new JValueBag();
		bag.add(new JValue(3));
		bag.add(new JValue(4));
		bag.add(new JValue(5));
		boundVariables.put("FOO", bag);
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
		JValueBag bag = new JValueBag();
		bag.add(new JValue(3));
		bag.add(new JValue(4));
		bag.add(new JValue(5));
		boundVariables.put("FOO", bag);
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
		String queryString = "from i: V{Variable} report i.name end";
		JValue result = evalTestQuery("VertexSetExpression", queryString);
		assertEquals(5, result.toCollection().size());
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(1, bag.getQuantity(new JValue("a")));
		assertEquals(1, bag.getQuantity(new JValue("b")));
		assertEquals(1, bag.getQuantity(new JValue("c")));
		assertEquals(1, bag.getQuantity(new JValue("d")));
		assertEquals(1, bag.getQuantity(new JValue("i")));
		JValue resultWO = evalTestQuery("VertexSetExpression (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateEdgeSetExpression(EdgeSetExpression,
	 * Graph)'
	 */
	@Test
	public void testEvaluateVertexSubgraphExpression1() throws Exception {
		String queryString = "from i:V{Identifier} in vSubgraph{Expression} report i.name end";
		JValue result = evalTestQuery("VertexSubgraphExpression1", queryString);
		assertEquals(5, result.toCollection().size());
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(1, bag.getQuantity(new JValue("a")));
		assertEquals(1, bag.getQuantity(new JValue("b")));
		assertEquals(1, bag.getQuantity(new JValue("c")));
		assertEquals(1, bag.getQuantity(new JValue("d")));
		assertEquals(1, bag.getQuantity(new JValue("i")));
		JValue resultWO = evalTestQuery("VertexSubgraphExpression1 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateVertexSubgraphExpression2() throws Exception {
		String queryString = "from i:V{Identifier} in vSubgraph{^Definition} report i.name end";
		JValue result = evalTestQuery("VertexSubgraphExpression2", queryString);
		assertEquals(5, result.toCollection().size());
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(1, bag.getQuantity(new JValue("a")));
		assertEquals(1, bag.getQuantity(new JValue("b")));
		assertEquals(1, bag.getQuantity(new JValue("c")));
		assertEquals(1, bag.getQuantity(new JValue("d")));
		assertEquals(1, bag.getQuantity(new JValue("i")));
		JValue resultWO = evalTestQuery("VertexSubgraphExpression2 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateVertexSubgraphExpression3() throws Exception {
		String queryString = "from i:V{Identifier} in vSubgraph{Identifier} report i.name end";
		JValue result = evalTestQuery("VertexSubgraphExpression3", queryString);
		assertEquals(5, result.toCollection().size());
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(1, bag.getQuantity(new JValue("a")));
		assertEquals(1, bag.getQuantity(new JValue("b")));
		assertEquals(1, bag.getQuantity(new JValue("c")));
		assertEquals(1, bag.getQuantity(new JValue("d")));
		assertEquals(1, bag.getQuantity(new JValue("i")));
		JValue resultWO = evalTestQuery("VertexSubgraphExpression3 (wo)",
				queryString, new DefaultOptimizer());
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
		String queryString = "from i:V{Identifier} in vSubgraph{^WhereExpression} report i.name end";
		JValue result = evalTestQuery("VertexSubgraphExpression5", queryString);
		assertEquals(5, result.toCollection().size());
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(1, bag.getQuantity(new JValue("a")));
		assertEquals(1, bag.getQuantity(new JValue("b")));
		assertEquals(1, bag.getQuantity(new JValue("c")));
		assertEquals(1, bag.getQuantity(new JValue("d")));
		assertEquals(1, bag.getQuantity(new JValue("i")));
		JValue resultWO = evalTestQuery("VertexSubgraphExpression5 (wo)",
				queryString, new DefaultOptimizer());
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
	}
	
	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateFunctionApplication(FunctionApplication,
	 * Graph)'
	 */
	@Test
	public void testMultipleEvaluationStarts() throws Exception {
		String queryString = "using FOO: from i: V{Identifier} report i.name end";
		Graph datagraph = getTestGraph();
		GreqlEvaluator eval = new GreqlEvaluator(queryString, datagraph,
				boundVariables);
		eval.startEvaluation();
		eval.startEvaluation();
		eval.startEvaluation();
		eval.startEvaluation();
		eval.startEvaluation();
		eval.getEvaluationResult();
		eval.startEvaluation();
		eval.startEvaluation();
		eval.startEvaluation();
		eval.startEvaluation();
		eval.startEvaluation();
		printTestFunctionFooter("MultipleEvaluationStarts");
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
		String queryString = "from v: V{Variable} report v.name end store as VariableNames";
		evalTestQuery("Store", queryString);
		JValueBag storedBag = boundVariables.get("VariableNames")
				.toCollection().toJValueBag();
		assertEquals(5, storedBag.size());
		assertTrue(storedBag.contains(new JValue("a")));
		assertTrue(storedBag.contains(new JValue("b")));
		assertTrue(storedBag.contains(new JValue("c")));
		assertTrue(storedBag.contains(new JValue("d")));
		assertTrue(storedBag.contains(new JValue("i")));
		assertTrue(!storedBag.contains(new JValue("x")));
	}

	@Test
	public void testTableComprehension() throws Exception {
		String queryString = "from x,y:list(1..10) reportTable \"X\", \"Y\", x*y end";
		JValue result = evalTestQuery("TableComprehension", queryString);
		assertTrue(result.toCollection().isJValueTable());
		
		
	}

	@Test
	public void testMapComprehension() throws Exception {
		String queryString = "from x : set(1, 2, 3, 4, 5) reportMap x, x*x end";
		JValue result = evalTestQuery("MapComprehension", queryString);
		assertTrue(result.isMap());
		JValueMap map = result.toJValueMap();
		assertEquals(5, map.size());
		assertEquals(new JValue(1), map.get(new JValue(1)));
		assertEquals(new JValue(4), map.get(new JValue(2)));
		assertEquals(new JValue(9), map.get(new JValue(3)));
		assertEquals(new JValue(16), map.get(new JValue(4)));
		assertEquals(new JValue(25), map.get(new JValue(5)));
	}

	@Test
	public void testMapComprehension2() throws Exception {
		String queryString = "from x : V{Variable} reportMap x.name, x end";
		JValue result = evalTestQuery("MapComprehension2", queryString);
		JValueMap map = result.toJValueMap();
		assertEquals(5, map.size());
		assertTrue(map.containsKey(new JValue("a")));
		assertTrue(map.containsKey(new JValue("b")));
		assertTrue(map.containsKey(new JValue("c")));
		assertTrue(map.containsKey(new JValue("d")));
		assertTrue(map.containsKey(new JValue("i")));
	}
	
	@Test
	public void testTableComprehension2() throws Exception {
		String queryString = "from x : V{Variable} reportMap x.name, x end";
		JValue result = evalTestQuery("MapComprehension2", queryString);
		JValueMap map = result.toJValueMap();
		assertEquals(5, map.size());
		assertTrue(map.containsKey(new JValue("a")));
		assertTrue(map.containsKey(new JValue("b")));
		assertTrue(map.containsKey(new JValue("c")));
		assertTrue(map.containsKey(new JValue("d")));
		assertTrue(map.containsKey(new JValue("i")));
	}
	
	@Test
	public void testQueryWithoutDatagraph() throws Exception {
		String queryString = "(3 + 4) * 7";
		JValue result = evalTestQuery("QueryWithoutDatagraph", queryString, (Graph)null);
		assertEquals(49, result.toInteger());
	}
	
	@Test
	public void testGraphExecution() throws Exception {
		String queryString = "(3 + 4) * 7";
		Greql2 graph = ManualGreqlParser.parse(queryString);
		System.out.println("Parsed query ");
		GreqlEvaluator eval = new GreqlEvaluator(graph, null, new HashMap<String, JValue>());
		eval.startEvaluation();
		JValue result = eval.getEvaluationResult();
		assertEquals(49, result.toInteger());
	}
}
