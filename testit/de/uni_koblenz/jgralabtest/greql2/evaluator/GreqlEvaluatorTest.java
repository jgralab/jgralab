/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2FunctionLibrary;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBag;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueRecord;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTable;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTuple;
import de.uni_koblenz.jgralab.greql2.optimizer.DefaultOptimizer;
import de.uni_koblenz.jgralab.greql2.optimizer.VariableDeclarationOrderOptimizer;
import de.uni_koblenz.jgralab.greql2.schema.Definition;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.impl.std.Greql2Impl;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;
import de.uni_koblenz.jgralabtest.greql2.testfunctions.IsPrime;

public class GreqlEvaluatorTest extends GenericTests {
	static {
		Greql2FunctionLibrary.instance().registerUserDefinedFunction(
				IsPrime.class);
	}

	private JValueBag createBagWithMeat(List<JValueImpl> list) {
		JValueBag bag = new JValueBag();
		list.add(new JValueImpl("Currywurst"));
		list.add(new JValueImpl("Bratwurst"));
		list.add(new JValueImpl("Käsewurst"));
		list.add(new JValueImpl("Steak"));
		list.add(new JValueImpl("Pommes"));
		list.add(new JValueImpl("Mayo"));
		list.add(new JValueImpl("Ketchup"));
		list.add(new JValueImpl("Zwiebeln"));
		for (JValueImpl v : list) {
			bag.add(v, 3);
		}
		return bag;
	}

	@Test
	public void testCombinations() {
		String query = "from a:list(1..10), b:list(1..10), c:list(1..10), d:list(1..10) report d end";
		GreqlEvaluator eval = new GreqlEvaluator(query, new Greql2Impl(),
				new HashMap<String, JValue>());
		eval.startEvaluation();
		JValue result = eval.getEvaluationResult();
		for (JValue v : result.toCollection()) {
			System.out.println(v);
		}
	}

	@Test
	public void testCombinations2() {
		HashMap<String, JValue> boundVars = new HashMap<String, JValue>();
		GreqlEvaluator eval = null;
		String createboundVars = "set(1,2,3) store as s123";
		String createboundVars2 = "set(4,5,6) store as s456 ";
		eval = new GreqlEvaluator(createboundVars, new Greql2Impl(), boundVars);
		eval.startEvaluation();
		System.out.println("HashMap: " + boundVars.size());
		for (java.util.Map.Entry<String, JValue> entry : boundVars.entrySet()) {
			System.out.println("<" + entry.getKey() + "," + entry.getValue()
					+ ">");
		}
		eval = new GreqlEvaluator(createboundVars2, new Greql2Impl(), boundVars);
		eval.startEvaluation();
		String query = "using s123, s456: "
				+ // img_Action, img_State
				"from t:s123, "
				+ // keySet img_Action
				"     a:s456, "
				+ // keySet img_state
				"     m: from _m:list(t..5) reportSet _m end " + "with a < m "
				+ "reportSet t,a end";
		eval = new GreqlEvaluator(query, new Greql2Impl(), boundVars);
		eval.setOptimize(false);
		eval.startEvaluation();

		JValue result = eval.getEvaluationResult();
		for (JValue v : result.toCollection()) {
			System.out.println(v);
		}
	}

	@Test
	public void testVertexSeq() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		Graph graph = getTestGraph();
		Vertex first = graph.getFirstVertex().getNextVertex();
		Vertex last = graph.getLastVertex().getPrevVertex().getPrevVertex();
		JValueImpl firstV = new JValueImpl(first);
		JValueImpl lastV = new JValueImpl(last);
		setBoundVariable("firstV", firstV);
		setBoundVariable("lastV", lastV);
		String queryString = "using firstV, lastV: vertexSeq{Definition}(firstV, lastV)";
		JValue result = evalTestQuery("vertexSeq", queryString);
		JValueSet set = result.toJValueSet();
		if (!(first instanceof Definition)) {
			first = first.getNextVertex(Definition.class);
		}
		Definition current = (Definition) first;
		for (JValue cv : set) {
			assertEquals(current, cv.toVertex());
			current = current.getNextDefinition();
		}
		assertNull(current.getNextDefinition());
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateAlternativePathDescription(AlternativePathDescription,
	 * Graph)'
	 */
	@Test
	public void testEvaluateAlternativePathDescription() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from var: V{Definition}, def: V{WhereExpression} with var  -->{IsDefinitionOf} | -->{IsVarOf}  def report var end";
		JValue result = evalTestQuery("AlternativePathDescription", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("AlternativePathDescription (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateAlternativePathDescription2() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from v: V{Variable} "
				+ "           reportSet v, v.name, v (-->{^IsVarOf, ^IsDefinitionOf, ^IsBoundExprOfDefinition} | (-->{IsVarOf} -->{IsDefinitionOf}))* end";
		JValue result = evalTestQuery("AlternativePathDescription2",
				queryString);
		assertEquals(5, result.toCollection().size());
		JValue resultWO = evalTestQuery("AlternativePathDescription2 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);

		System.out.println(result);
	}

	@Test
	public void testEvaluateExponentiatedPathDescription() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from var: V{Definition} with var  <->^2 var report var end";
		JValue result = evalTestQuery("ExponentiatedPathDescription",
				queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("ExponentiatedPathDescription (wo)",
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
		assertEquals(true, result.toBoolean());
		JValue resultWO = evalTestQuery("BackwardVertexSet1 (wo)", queryString,
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
		ArrayList<JValueImpl> list = new ArrayList<JValueImpl>();
		setBoundVariable("FOO", createBagWithMeat(list));
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
		setBoundVariable("FOO", new JValueImpl("A String"));
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
				new JValueImpl(1)));
		assertEquals(2, result.toCollection().toJValueBag().getQuantity(
				new JValueImpl(2)));
		assertEquals(5, result.toCollection().toJValueBag().getQuantity(
				new JValueImpl(3)));
		assertEquals(1, result.toCollection().toJValueBag().getQuantity(
				new JValueImpl(5)));
		assertEquals(2, result.toCollection().toJValueBag().getQuantity(
				new JValueImpl(7)));
		assertEquals(1, result.toCollection().toJValueBag().getQuantity(
				new JValueImpl(34)));
		assertEquals(1, result.toCollection().toJValueBag().getQuantity(
				new JValueImpl(455)));
		assertEquals(2, result.toCollection().toJValueBag().getQuantity(
				new JValueImpl(456)));
		assertEquals(1, result.toCollection().toJValueBag().getQuantity(
				new JValueImpl(457)));

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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
		String queryString = "1=1?1:2";
		JValue result = evalTestQuery("ConditionalExpression", queryString);
		assertEquals(1, (int) result.toInteger());
		JValue resultWO = evalTestQuery("ConditionalExpression (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateConditionalExpression2() throws Exception {
		String queryString = "1=2?1:2";
		JValue result = evalTestQuery("ConditionalExpression2", queryString);
		assertEquals(2, (int) result.toInteger());
		JValue resultWO = evalTestQuery("ConditionalExpression2 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateConditionalExpression3() throws Exception {
		String queryString = "1?1:2";
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from i:E{IsDefinitionOf} report i end";
		JValue result = evalTestQuery("EdgeSetExpression", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("EdgeSetExpression (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateEdgeSubgraphExpression1() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from i: E in eSubgraph{IsDefinitionOf} report i end";
		JValue result = evalTestQuery("EdgeSubgraphExpression1", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("EdgeSubgraphExpression1 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateEdgeSubgraphExpression2() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from i: V in eSubgraph{IsDefinitionOf} report i end";
		JValue result = evalTestQuery("EdgeSubgraphExpression2", queryString);
		assertEquals(5, result.toCollection().size());
		JValue resultWO = evalTestQuery("EdgeSubgraphExpression2 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateEdgeSubgraphExpression3() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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

	@Test
	public void testEvaluateIntermediateVertexDescription() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from var: V{Variable}, def:V{Definition}, whr: V{WhereExpression} with isReachable(var, whr, -->{IsVarOf} def -->{IsDefinitionOf}) report var end";
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from var: V{Definition}, def: V{WhereExpression} with var  -->{IsDefinitionOf}* def report var end";
		JValue result = evalTestQuery("IteratedPathDescription1", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("IteratedPathDescription1 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateIteratedPathDescription12() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from var: V{Definition}, def: V{WhereExpression} with var  -->{IsDefinitionOf}* def report var end";
		JValue result = evalTestQuery("IteratedPathDescription12", queryString);
		assertEquals(4, result.toCollection().size());

		String queryString2 = "from var: V{Definition}, def: var -->{IsDefinitionOf}* &{WhereExpression} report var end";
		JValue result2 = evalTestQuery("IteratedPathDescription12 (2)",
				queryString2);
		assertEquals(result, result2);

		String queryString3 = "from var: V{Definition}, def: var (-->{IsDefinitionOf}*) &{WhereExpression} report var end";
		JValue result3 = evalTestQuery("IteratedPathDescription12 (2)",
				queryString3);
		assertEquals(result, result3);
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from var: V{Definition}, def: V{WhereExpression} with var  -->{IsDefinitionOf}+ def report var end";
		JValue result = evalTestQuery("IteratedPathDescription3", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("IteratedPathDescription3 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateIteratedPathDescription4() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from var: V{Definition}, def: V{WhereExpression} with var -->{IsVarOf}* -->{IsDefinitionOf} def report var end";
		JValue result = evalTestQuery("IteratedPathDescription4", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("IteratedPathDescription4 (wo)",
				queryString, new DefaultOptimizer());
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from whe: V{WhereExpression}, def: -->{IsDefinitionOf} whe reportSet def end";
		JValue result = evalTestQuery("DependentDeclarations2", queryString);
		assertEquals(4, result.toCollection().size());
		JValueSet set = result.toCollection().toJValueSet();
		for (Definition def : ((Greql2) getTestGraph()).getDefinitionVertices()) {
			assertTrue(set.contains(new JValueImpl(def)));
		}
		JValue resultWO = evalTestQuery("DependentDeclarations2 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateDependentDeclarations3() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from def: V{Definition}, whe: <--{IsDefinitionOf} def report def end";
		JValue result = evalTestQuery("DependentDeclarations3", queryString);
		assertEquals(4, result.toCollection().size());
		JValueSet set = result.toCollection().toJValueSet();
		for (Definition def : ((Greql2) getTestGraph()).getDefinitionVertices()) {
			assertNotNull(def.getFirstIsDefinitionOfIncidence());
			assertTrue(set.contains(new JValueImpl(def)));
		}
		JValue resultWO = evalTestQuery("DependentDeclarations3 (wo)",
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
		ArrayList<JValueImpl> list = new ArrayList<JValueImpl>();
		setBoundVariable("FOO", createBagWithMeat(list));
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from var: V{Definition}, def: V{BagComprehension, WhereExpression}  with var --> def report var end";
		JValue result = evalTestQuery("SimplePathDescription3", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("SimplePathDescription3 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateAggregationPathDescription1() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from var: V{Variable}, def: V{Definition} with var --<> def report var end";
		JValue result = evalTestQuery("SimplePathDescription2", queryString);
		assertEquals(6, result.toCollection().size());
		JValue resultWO = evalTestQuery("SimplePathDescription2 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateAggregationPathDescription2() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from var: V{Variable}, def: V{Definition} with def <>-- var report var end";
		JValue result = evalTestQuery("SimplePathDescription2", queryString);
		assertEquals(6, result.toCollection().size());
		JValue resultWO = evalTestQuery("SimplePathDescription2 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
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
	public void testEvaluateStartRestriction1() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from var: V{Variable}, def: V{WhereExpression} with var -->{IsVarOf} {Definition} & -->{IsDefinitionOf} def report var end";
		JValue result = evalTestQuery("StartRestriction1", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("StartRestriction1 (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateStartRestriction5() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from var: V{Variable}, def: V{WhereExpression} with var  -->{IsVarOf} & {@1 = 1} -->{IsDefinitionOf} def  report var end";
		JValue result = evalTestQuery("StartRestriction4", queryString);
		assertEquals(4, result.toCollection().size());
		JValue resultWO = evalTestQuery("StartRestriction4 (wo)", queryString,
				new DefaultOptimizer());
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
	 * 'greql2.evaluator.GreqlEvaluator.evaluateRecordConstruction(RecordConstruction,
	 * Graph)'
	 */
	@Test
	public void testEvaluateTupleAccess() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "let x := tup ( \"bratwurst\", \"currywurst\", \"steak\", \"kaenguruhfleisch\", \"spiessbraten\") in from i:V{Identifier} report x[3] end";
		JValue result = evalTestQuery("TupleAccess", queryString);
		assertEquals(5, result.toCollection().size());
		assertEquals(5, result.toCollection().toJValueBag().getQuantity(
				new JValueImpl("kaenguruhfleisch")));
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
		bag.add(new JValueImpl(3));
		bag.add(new JValueImpl(4));
		bag.add(new JValueImpl(5));
		setBoundVariable("FOO", bag);
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
		bag.add(new JValueImpl(3));
		bag.add(new JValueImpl(4));
		bag.add(new JValueImpl(5));
		setBoundVariable("FOO", bag);
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from i: V{Variable} report i.name end";
		JValue result = evalTestQuery("VertexSetExpression", queryString);
		assertEquals(5, result.toCollection().size());
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(1, bag.getQuantity(new JValueImpl("a")));
		assertEquals(1, bag.getQuantity(new JValueImpl("b")));
		assertEquals(1, bag.getQuantity(new JValueImpl("c")));
		assertEquals(1, bag.getQuantity(new JValueImpl("d")));
		assertEquals(1, bag.getQuantity(new JValueImpl("i")));
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from i:V{Identifier} in vSubgraph{Expression} report i.name end";
		JValue result = evalTestQuery("VertexSubgraphExpression1", queryString);
		assertEquals(5, result.toCollection().size());
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(1, bag.getQuantity(new JValueImpl("a")));
		assertEquals(1, bag.getQuantity(new JValueImpl("b")));
		assertEquals(1, bag.getQuantity(new JValueImpl("c")));
		assertEquals(1, bag.getQuantity(new JValueImpl("d")));
		assertEquals(1, bag.getQuantity(new JValueImpl("i")));
		JValue resultWO = evalTestQuery("VertexSubgraphExpression1 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateVertexSubgraphExpression2() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from i:V{Identifier} in vSubgraph{^Definition} report i.name end";
		JValue result = evalTestQuery("VertexSubgraphExpression2", queryString);
		assertEquals(5, result.toCollection().size());
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(1, bag.getQuantity(new JValueImpl("a")));
		assertEquals(1, bag.getQuantity(new JValueImpl("b")));
		assertEquals(1, bag.getQuantity(new JValueImpl("c")));
		assertEquals(1, bag.getQuantity(new JValueImpl("d")));
		assertEquals(1, bag.getQuantity(new JValueImpl("i")));
		JValue resultWO = evalTestQuery("VertexSubgraphExpression2 (wo)",
				queryString, new DefaultOptimizer());
		assertEquals(result, resultWO);
	}

	@Test
	public void testEvaluateVertexSubgraphExpression3() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from i:V{Identifier} in vSubgraph{Identifier} report i.name end";
		JValue result = evalTestQuery("VertexSubgraphExpression3", queryString);
		assertEquals(5, result.toCollection().size());
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(1, bag.getQuantity(new JValueImpl("a")));
		assertEquals(1, bag.getQuantity(new JValueImpl("b")));
		assertEquals(1, bag.getQuantity(new JValueImpl("c")));
		assertEquals(1, bag.getQuantity(new JValueImpl("d")));
		assertEquals(1, bag.getQuantity(new JValueImpl("i")));
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from i:V{Identifier} in vSubgraph{^WhereExpression} report i.name end";
		JValue result = evalTestQuery("VertexSubgraphExpression5", queryString);
		assertEquals(5, result.toCollection().size());
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(1, bag.getQuantity(new JValueImpl("a")));
		assertEquals(1, bag.getQuantity(new JValueImpl("b")));
		assertEquals(1, bag.getQuantity(new JValueImpl("c")));
		assertEquals(1, bag.getQuantity(new JValueImpl("d")));
		assertEquals(1, bag.getQuantity(new JValueImpl("i")));
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
		Graph datagraph = getTestGraph();
		GreqlEvaluator eval = new GreqlEvaluator(queryString, datagraph, null);
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from v: V{Variable} report v.name end store as VariableNames";
		evalTestQuery("Store", queryString);
		JValueBag storedBag = getBoundVariable("VariableNames").toCollection()
				.toJValueBag();
		assertEquals(5, storedBag.size());
		assertTrue(storedBag.contains(new JValueImpl("a")));
		assertTrue(storedBag.contains(new JValueImpl("b")));
		assertTrue(storedBag.contains(new JValueImpl("c")));
		assertTrue(storedBag.contains(new JValueImpl("d")));
		assertTrue(storedBag.contains(new JValueImpl("i")));
		assertTrue(!storedBag.contains(new JValueImpl("x")));
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
		assertEquals(new JValueImpl(1), map.get(new JValueImpl(1)));
		assertEquals(new JValueImpl(4), map.get(new JValueImpl(2)));
		assertEquals(new JValueImpl(9), map.get(new JValueImpl(3)));
		assertEquals(new JValueImpl(16), map.get(new JValueImpl(4)));
		assertEquals(new JValueImpl(25), map.get(new JValueImpl(5)));
	}

	@Test
	public void testMapComprehension2() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{Variable} reportMap x.name, x end";
		JValue result = evalTestQuery("MapComprehension2", queryString);
		JValueMap map = result.toJValueMap();
		assertEquals(5, map.size());
		assertTrue(map.containsKey(new JValueImpl("a")));
		assertTrue(map.containsKey(new JValueImpl("b")));
		assertTrue(map.containsKey(new JValueImpl("c")));
		assertTrue(map.containsKey(new JValueImpl("d")));
		assertTrue(map.containsKey(new JValueImpl("i")));
	}

	@Test
	public void testMapComprehension3() throws Exception {
		// GreqlEvaluator.DEBUG_DECLARATION_ITERATIONS = true;
		String queryString = "from x : list(1..3) reportMap x, from y : list(1..x) reportMap y, list(y..x) end end";
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
	public void testTableComprehension2() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{Variable} reportMap x.name, x end";
		JValue result = evalTestQuery("MapComprehension2", queryString);
		JValueMap map = result.toJValueMap();
		assertEquals(5, map.size());
		assertTrue(map.containsKey(new JValueImpl("a")));
		assertTrue(map.containsKey(new JValueImpl("b")));
		assertTrue(map.containsKey(new JValueImpl("c")));
		assertTrue(map.containsKey(new JValueImpl("d")));
		assertTrue(map.containsKey(new JValueImpl("i")));
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
		JValue r1 = e1.getEvaluationResult();
		String query2 = "from x : list(1..5)                   "
				+ "      with isPrime(x)                       "
				+ "      reportSet x, from y : from a : list(21..25) with isPrime(a+x) reportSet a end,"
				+ "                        z : from b : list(21..30) with isPrime(b+x) reportSet b end "
				+ "                   reportSet y, z end       "
				+ "      end                                   ";
		GreqlEvaluator e2 = new GreqlEvaluator(query2, null, null);
		e2.setOptimize(false);
		e2.startEvaluation();
		JValue r2 = e2.getEvaluationResult();

		assertEquals(r1, r2);
	}

	@Test
	public void testWhereWithSameScope() throws Exception {
		String query = "from a,b:list(1..10) with equivalent reportSet a end where equivalent := a=b";
		JValue result = evalTestQuery("WhereWithSameScope", query);
		assertTrue(result.isCollection());
		assertTrue(result.toCollection().isJValueSet());
		JValueSet resBag = result.toJValueSet();
		assertEquals(10, resBag.size());
		for (int i = 1; i < 11; i++) {
			assertTrue(resBag.contains(new JValueImpl(i)));
		}
	}

	@Test
	public void testLetWithSameScope() throws Exception {
		String query = "let equivalent := a=b in from a,b:list(1..10) with equivalent reportSet a end";
		JValue result = evalTestQuery("LetWithSameScope", query);
		assertTrue(result.isCollection());
		assertTrue(result.toCollection().isJValueSet());
		JValueSet resBag = result.toJValueSet();
		assertEquals(10, resBag.size());
		for (int i = 1; i < 11; i++) {
			assertTrue(resBag.contains(new JValueImpl(i)));
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
		assertEquals(evalTestQuery("VariableOrder1", query), evalTestQuery(
				"VariableOrder1", query2));
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
		assertEquals(evalTestQuery("VariableOrder2", query), evalTestQuery(
				"VariableOrder2", query2));
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
		GreqlEvaluator eval = new GreqlEvaluator((String) null, getTestGraph(),
				null);

		Field parseTime = eval.getClass().getDeclaredField("parseTime");
		parseTime.setAccessible(true);
		Field optimizationTime = eval.getClass().getDeclaredField(
				"optimizationTime");
		optimizationTime.setAccessible(true);

		for (int i = 0; i < 6; i++) {
			eval.setQuery((i % 2 == 0) ? query1 : query2);
			eval.startEvaluation();
			eval.getEvaluationResult();
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
			eval.getEvaluationResult();
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
