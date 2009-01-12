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

package de.uni_koblenz.jgralabtest.greql2test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralabtest.schemas.minimal.Link;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.minimal.Node;

/**
 * Tests all functions that are provided by the Greql2FunctionLibrary
 *
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 *
 */
public class FunctionTest extends GenericTests {

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateSequentialPathDescription(SequentialPathDescription,
	 * Graph)'
	 */
	@Test
	public void testAnd() throws Exception {
		String queryString = "from var: V{Variable}, def: V{WhereExpression} with var  -->{IsVarOf} -->{IsDefinitionOf} def and 1=1 report var end";
		JValue result = evalTestQuery("And", queryString);
		assertEquals(4, result.toCollection().size());
	}

	@Test
	public void testAnd2() throws Exception {
		String queryString = "from x, y : list(true, null, false) report x and y end";
		JValue result = evalTestQuery("And2", queryString);
		JValueCollection r = result.toCollection();
		assertEquals(9, r.size());
		int f = 0, t = 0, n = 0;
		for (JValue v : r) {
			if (JValueBoolean.getNullValue() == v.toBoolean()) {
				n++;
			} else if (JValueBoolean.getTrueValue() == v.toBoolean()) {
				t++;
			} else if (JValueBoolean.getFalseValue() == v.toBoolean()) {
				f++;
			}
		}
		assertEquals(5, f);
		assertEquals(3, n);
		assertEquals(1, t);
	}

	@Test
	public void testAvg() throws Exception {
		String queryString = "let x:= list (5..13) in avg(x)";
		JValue result = evalTestQuery("Avg", queryString);
		assertEquals(9.0, (double) result.toDouble());
	}

	@Test
	public void testContains() throws Exception {
		String queryString = "let x:= list (5..13) in isIn(7, x)";
		JValue result = evalTestQuery("ContainsTrue", queryString);
		assertEquals(true, (boolean) result.toBoolean());
	}

	@Test
	public void testContains2() throws Exception {
		String queryString = "let x:= list (5..13) in isIn(56, x)";
		JValue result = evalTestQuery("ContainsFalse", queryString);
		assertEquals(false, (boolean) result.toBoolean());
	}

	@Test
	public void testContains3() throws Exception {
		String queryString = "let x:= list (5..13) in isIn(13, x)";
		JValue result = evalTestQuery("ContainsTrue2", queryString);
		assertEquals(true, (boolean) result.toBoolean());
	}

	@Test
	public void testCount() throws Exception {
		String queryString = "let x:= list (5..13) in count(x)";
		JValue result = evalTestQuery("Count", queryString);
		assertEquals(9, (int) result.toInteger());
	}

	@Test
	public void testCount2() throws Exception {
		String queryString = "let x:= 17 in count(x)";
		JValue result = evalTestQuery("Count", queryString);
		assertEquals(1, (int) result.toInteger());
	}

	@Test
	public void testDegree1() throws Exception {
		String queryString = "from x : V{BagComprehension} report degree{IsCompResultDefOf}(x) end";
		JValue result = evalTestQuery("Degree1", queryString);
		assertEquals(1, result.toCollection().size());
		for (JValue j : result.toCollection()) {
			assertEquals(1, (int) j.toInteger());
		}
	}

	@Test
	public void testDegree2() throws Exception {
		String queryString = "from x : V{BagComprehension} report degree(x) end";
		JValue result = evalTestQuery("Degree2", queryString);
		assertEquals(1, result.toCollection().size());
		for (JValue j : result.toCollection()) {
			assertEquals(3, (int) j.toInteger());
		}
	}

	@Test
	public void testDifference() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(5,6,7,8) in difference(x, y)";
		JValue result = evalTestQuery("Difference", queryString);
		assertEquals(2, result.toCollection().size());
	}

	@Test
	public void testDifference2() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := list(5,5,6,7,8) in difference(x, y)";
		JValue result = evalTestQuery("Difference", queryString);
		assertEquals(2, result.toCollection().size());
	}

	@Test
	public void testDividedBy() throws Exception {
		String queryString = "3/3";
		JValue result = evalTestQuery("DividedBy", queryString);
		assertEquals(new JValue(1.0), result);
	}

	@Test
	public void testDividedBy2() throws Exception {
		String queryString = "3.0/1";
		JValue result = evalTestQuery("DividedBy", queryString);
		assertEquals(new JValue(3.0), result);
	}

	@Test
	public void testDividedBy3() throws Exception {
		String queryString = "3/7";
		JValue result = evalTestQuery("DividedBy", queryString);
		assertEquals(new JValue(3.0 / 7), result);
	}

	@Test
	public void testEdgesConnected() throws Exception {
		String queryString = "from x : V{WhereExpression} report edgesConnected(x) end";
		JValue result = evalTestQuery("EdgesConnected", queryString);
		assertEquals(6, getNthValue(result.toCollection(), 0).toCollection()
				.size());
	}

	@Test
	public void testEdgesFrom() throws Exception {
		String queryString = "from x : V{WhereExpression} report edgesFrom(x) end";
		JValue result = evalTestQuery("EdgesFrom", queryString);
		assertEquals(1, result.toCollection().size());
		assertEquals(1, getNthValue(result.toCollection(), 0).toCollection()
				.size());
	}

	@Test
	public void testEdgesTo() throws Exception {
		String queryString = "from x : V{WhereExpression} report edgesTo(x) end";
		JValue result = evalTestQuery("EdgesTo", queryString);
		assertEquals(1, result.toCollection().size());
		for (JValue j : result.toCollection()) {
			assertEquals(5, j.toCollection().size());
		}
	}

	@Test
	public void testEdgesTypeSet() throws Exception {
		String queryString = "from x : V{WhereExpression} report edgeTypeSet(edgesConnected(x)) end";
		JValue result = evalTestQuery("EdgeTypeSet", queryString);
		assertEquals(3, getNthValue(result.toCollection(), 0).toCollection()
				.size());
	}

	@Test
	public void testEquals() throws Exception {
		String queryString = "equals(5, 9)";
		JValue result = evalTestQuery("Equals", queryString);
		assertEquals(false, (boolean) result.toBoolean());
	}

	@Test
	public void testEquals2() throws Exception {
		String queryString = "from x : V{WhereExpression}, y : V{Greql2Expression} report equals(x,y) end";
		JValue result = evalTestQuery("Equals2", queryString);

		assertEquals(false, (boolean) getNthValue(result.toCollection(), 0)
				.toBoolean());
	}

	@Test
	public void testEquals3() throws Exception {
		String queryString = "from x : V{WhereExpression}, y : V{WhereExpression} report equals(x,y) end";
		JValue result = evalTestQuery("Equals3", queryString);
		assertEquals(true, (boolean) getNthValue(result.toCollection(), 0)
				.toBoolean());
	}

	@Test
	public void testEquals4() throws Exception {
		String queryString = "from x : V{WhereExpression}, y : E{IsBoundExprOfDefinition} report equals(x,y) end";
		JValue result = evalTestQuery("Equals4", queryString);
		assertEquals(1, result.toCollection().size());
		assertEquals(false, (boolean) getNthValue(result.toCollection(), 0)
				.toBoolean());
	}

	public void testEquals5() throws Exception {
		String queryString = "from x : V, y : E report equals(x,y) end";
		JValue result = evalTestQuery("Equals5", queryString);
		for (JValue v : result.toCollection()) {
			assertEquals(Boolean.FALSE, v.toBoolean());
		}
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateRecordConstruction(RecordConstruction,
	 * Graph)'
	 */
	@Test
	public void testEvaluateListAccess() throws Exception {
		String queryString = "let x := list ( \"bratwurst\", \"currywurst\", \"steak\", \"kaenguruhfleisch\", \"spiessbraten\") in from i:V{Identifier} report x[3] end";
		JValue result = evalTestQuery("ListAccess", queryString);
		assertEquals(5, result.toCollection().size());
		assertTrue(result.toCollection().contains(
				new JValue("kaenguruhfleisch")));
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateRecordConstruction(RecordConstruction,
	 * Graph)'
	 */
	@Test
	public void testEvaluateRecordAccess() throws Exception {
		String queryString = "let x := rec ( menue1:\"bratwurst\", menue2:\"currywurst\", menue3:\"steak\", menue4:\"kaenguruhfleisch\", menue5:\"spiessbraten\") in from i:V{Identifier} report x.menue4 end";
		JValue result = evalTestQuery("RecordAccess", queryString);
		assertEquals(1, result.toCollection().toJValueSet().size());
		assertTrue(result.toCollection().toJValueSet().contains(
				new JValue("kaenguruhfleisch")));
	}

	@Test
	public void testGetValue() throws Exception {
		String queryString = "from x : V{Variable} report x.name end";
		JValue result = evalTestQuery("GetValue", queryString);
		assertEquals(5, result.toCollection().size());
		Iterator<JValue> iter = result.toCollection().iterator();
		while (iter.hasNext()) {
			JValue col = iter.next();
			assertTrue((col.toString().equals("a"))
					|| (col.toString().equals("b"))
					|| (col.toString().equals("c"))
					|| (col.toString().equals("d"))
					|| (col.toString().equals("i")));
		}
	}

	@Test
	public void testHasAttribute() throws Exception {
		String queryString = "from x : V{Variable} report hasAttribute(x, \"name\") end";
		JValue result = evalTestQuery("HasAttribute", queryString);
		for (JValue v : result.toCollection()) {
			assertEquals(JValueBoolean.getTrueValue(), v.toBoolean());
		}
	}

	@Test
	public void testHasAttribute2() throws Exception {
		String queryString = "from x : V{Variable} report hasAttribute(type(x), \"name\") end";
		JValue result = evalTestQuery("HasAttribute2", queryString);
		for (JValue v : result.toCollection()) {
			assertEquals(JValueBoolean.getTrueValue(), v.toBoolean());
		}
	}

	@Test
	public void testHasType() throws Exception {
		String queryString = "from x : V{WhereExpression} report hasType(x, \"WhereExpression\") end";
		JValue result = evalTestQuery("HasType", queryString);
		assertEquals(1, result.toCollection().size());
		assertEquals(true, (boolean) getNthValue(result.toCollection(), 0)
				.toBoolean());
	}

	@Test
	public void testHasType2() throws Exception {
		String queryString = "from x : V{WhereExpression} report hasType(x, \"Variable\") end";
		JValue result = evalTestQuery("HasType2", queryString);
		assertEquals(1, result.toCollection().size());
		assertEquals(false, (boolean) getNthValue(result.toCollection(), 0)
				.toBoolean());
	}

	@Test
	public void testHasType3() throws Exception {
		String queryString = "from x : V{WhereExpression} report hasType{WhereExpression, Definition}(x) end";
		JValue result = evalTestQuery("HasType3", queryString);
		assertEquals(1, result.toCollection().size());
		for (JValue j : result.toCollection()) {
			assertEquals(true, (boolean) j.toBoolean());
		}
	}

	@Test
	public void testId() throws Exception {
		String queryString = "from x : V{Greql2Expression} report id(x) end";
		JValue result = evalTestQuery("Id", queryString);
		assertEquals(1, result.toCollection().size());
		for (JValue j : result.toCollection()) {
			assertEquals(1, (int) j.toInteger());
		}
	}

	@Test
	public void testIntersection() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(5,6,7,8) in intersection(x, y)";
		JValue result = evalTestQuery("Intersection", queryString);
		assertEquals(2, result.toCollection().size());
	}

	@Test
	public void testIsA() throws Exception {
		String queryString = "isA(\"Variable\", \"Identifier\")";
		JValue result = evalTestQuery("IsA", queryString);
		assertEquals(JValueBoolean.getTrueValue(), result.toBoolean());
	}

	@Test
	public void testIsAcyclic() throws Exception {
		String queryString = "isAcyclic()";
		JValue result = evalTestQuery("IsAcyclic", queryString);
		assertEquals(JValueBoolean.getTrueValue(), result.toBoolean());
	}

	@Test
	public void testIsAcyclic2() throws Exception {
		String queryString = "isAcyclic()";
		JValue result = evalTestQuery("IsAcyclic2", queryString,
				getCyclicTestGraph());
		assertEquals(JValueBoolean.getFalseValue(), result.toBoolean());
	}

	@Test
	public void testIsCycle() throws Exception {
		String queryString = "from v : V reportSet isCycle(extractPath(pathSystem(v, -->), v)) end";
		JValue result = evalTestQuery("isCycle", queryString,
				getCyclicTestGraph());
		for (JValue v : result.toCollection()) {
			assertEquals(JValueBoolean.getTrueValue(), v.toBoolean());
		}
	}

	@Test
	public void testIsCycle2() throws Exception {
		String queryString = "from v,w : V with v <> w reportSet isCycle(extractPath(pathSystem(v, -->+), w)) end";
		JValue result = evalTestQuery("isCycle", queryString,
				getCyclicTestGraph());
		for (JValue v : result.toCollection()) {
			assertEquals(JValueBoolean.getFalseValue(), v.toBoolean());
		}
	}

	@Test
	public void testIsIn() throws Exception {
		String queryString = "from v : V reportSet isIn(v, eSubgraph{Link}) end";
		JValue result = evalTestQuery("isIn", queryString, getCyclicTestGraph());
		for (JValue v : result.toCollection()) {
			assertEquals(JValueBoolean.getTrueValue(), v.toBoolean());
		}
	}

	@Test
	public void testIsIn2() throws Exception {
		String queryString = "from v : from w : V with isIn(w) reportSet w end "
				+ "           in eSubgraph{Link}      "
				+ "           reportSet v end";
		JValue result = evalTestQuery("isIn2", queryString,
				getCyclicTestGraph());
		assertEquals(10, result.toCollection().size());
	}

	@Test
	public void testIsIn3() throws Exception {
		String queryString = "from v : V "
				+ "           in eSubgraph{Link}      "
				+ "           reportSet isIn(v) end";
		try {
			evalTestQuery("isIn3", queryString, getCyclicTestGraph());
			fail();
		} catch (EvaluateException e) {
			// an eval exception is expected here
		}
	}

	@Test
	public void testIsIsolated() throws Exception {
		String queryString = "from x : V{WhereExpression} report isIsolated(x) end";
		JValue result = evalTestQuery("IsIsolated", queryString);
		assertEquals(1, result.toCollection().size());
		assertEquals(false, (boolean) getNthValue(result.toCollection(), 0)
				.toBoolean());
	}

	@Test
	public void testIsLoop() throws Exception {
		String queryString = "from x : E{IsBoundExprOfDefinition} report isLoop(x) end";
		JValue result = evalTestQuery("IsLoop", queryString);
		assertEquals(1, result.toCollection().size());
		assertEquals(false, (boolean) getNthValue(result.toCollection(), 0)
				.toBoolean());
	}

	@Test
	public void testIsNeighbour() throws Exception {
		String queryString = "from x : V{WhereExpression}, y:V{Greql2Expression} report isNeighbour(x, y) end";
		JValue result = evalTestQuery("IsNeighbour", queryString);
		assertEquals(1, result.toCollection().size());
		for (JValue j : result.toCollection()) {
			assertEquals(true, (boolean) j.toBoolean());
		}
	}

	@Test
	public void testIsNeighbour2() throws Exception {
		String queryString = "from x : V{Greql2Expression}, y:V{Variable} report isNeighbour(x, y) end";
		JValue result = evalTestQuery("IsNeighbour2", queryString);
		assertEquals(5, result.toCollection().size());
		for (JValue j : result.toCollection()) {
			assertEquals(false, (boolean) j.toBoolean());
		}
	}

	@Test
	public void testIsPrime() throws Exception {
		// check the first 58 prime numbers
		String queryString = "from x : list(1..271) with isPrime(x) report x end";
		JValue result = evalTestQuery("IsPrime", queryString);
		assertEquals(58, result.toCollection().size());
		int primes[] = { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43,
				47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109,
				113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179,
				181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241,
				251, 257, 263, 269, 271 };
		for (int prime : primes) {
			assertTrue(result.toCollection().contains(JValue.fromObject(prime)));
		}
	}

	@Test
	public void testIsPrime2() throws Exception {
		// check some really large numbers (and check if the optional parameter
		// noOfTestRuns works, too)
		assertTrue(evalTestQuery("IsPrime2", "isPrime(37956673, 8)")
				.toBoolean());
	}

	@Test
	public void testIsPrime3() throws Exception {
		assertFalse(evalTestQuery("IsPrime3 (not a prime)",
				"isPrime(7171712, 12)").toBoolean());
	}

	@Test
	public void testIsSibling() throws Exception {
		String queryString = "from x : V{Definition}, y:V{BagComprehension} report isSibling(x, y) end";
		JValue result = evalTestQuery("IsSibling", queryString);
		assertEquals(4, result.toCollection().size());
		for (JValue j : result.toCollection()) {
			assertEquals(true, (boolean) j.toBoolean());
		}
	}

	@Test
	public void testIsSibling2() throws Exception {
		String queryString = "from x : V{Definition}, y:V{Declaration} report isSibling(x, y) end";
		JValue result = evalTestQuery("IsSibling2", queryString);
		assertEquals(4, result.toCollection().size());
		for (JValue j : result.toCollection()) {
			assertEquals(false, (boolean) j.toBoolean());
		}
	}

	@Test
	public void testIsSubSet1() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(5,6,7,8) in isSubSet(x,y)";
		JValue result = evalTestQuery("IsSubset", queryString);
		assertEquals(false, (boolean) result.toBoolean());
	}

	@Test
	public void testIsSubSet2() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(5,7) in isSubSet(x, y)";
		JValue result = evalTestQuery("IsSubset2", queryString);
		assertEquals(false, (boolean) result.toBoolean());
	}

	@Test
	public void testIsSubSet3() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(5,7) in isSubSet(y, x)";
		JValue result = evalTestQuery("IsSubset3", queryString);
		assertEquals(true, (boolean) result.toBoolean());
	}

	@Test
	public void testIsSuperSet1() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(1, 5,7) in isSuperSet(x, y)";
		JValue result = evalTestQuery("IsSuperset1", queryString);
		assertEquals(false, (boolean) result.toBoolean());
	}

	@Test
	public void testIsSuperSet2() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(5,7) in isSuperSet(x, y)";
		JValue result = evalTestQuery("IsSuperset2", queryString);
		assertEquals(true, (boolean) result.toBoolean());
	}

	@Test
	public void testIsSuperSet3() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(5,7) in isSuperSet(y, x)";
		JValue result = evalTestQuery("IsSuperset3", queryString);
		assertEquals(false, (boolean) result.toBoolean());
	}

	@Test
	public void testIsSuperSet4() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(5, 7, 13, 9) in isSuperSet(x, y)";
		JValue result = evalTestQuery("IsSuperset4", queryString);
		assertEquals(true, (boolean) result.toBoolean());
	}

	@Test
	public void testIsTree() throws Exception {
		String queryString = "isTree()";
		JValue result = evalTestQuery("IsTree", queryString,
				getCyclicTestGraph());
		assertEquals(JValueBoolean.getFalseValue(), result.toBoolean());
	}

	@Test
	public void testIsTree2() throws Exception {
		String queryString = "isTree()";
		JValue result = evalTestQuery("IsTree2", queryString);
		assertEquals(JValueBoolean.getFalseValue(), result.toBoolean());
	}

	@Test
	public void testIsTree3() throws Exception {
		String queryString = "isTree()";
		JValue result = evalTestQuery("IsTree3", queryString, getTestTree());
		assertEquals(JValueBoolean.getTrueValue(), result.toBoolean());
	}

	@Test
	public void testMinus() throws Exception {
		String queryString = "6 - 1.5";
		JValue result = evalTestQuery("Minus", queryString);
		assertEquals(4.5, result.toDouble(), 0.01);
	}

	@Test
	public void testMinus2() throws Exception {
		String queryString = "6 - 3";
		JValue result = evalTestQuery("Minus", queryString);
		assertEquals(3l, (long) result.toLong());
	}

	@Test
	public void testMinus3() throws Exception {
		String queryString = "16 - 323";
		JValue result = evalTestQuery("Minus", queryString);
		assertEquals(-307l, (long) result.toLong());
	}

	@Test
	public void testMinus4() throws Exception {
		String queryString = "1.5 - 6";
		JValue result = evalTestQuery("Minus", queryString);
		assertEquals(-4.5, result.toDouble(), 0.01);
	}

	@Test
	public void testModulo() throws Exception {
		String queryString = "9 % 2";
		JValue result = evalTestQuery("Modulo", queryString);
		assertEquals(new Integer(1), result.toInteger());
	}

	@Test
	public void testNotEquals() throws Exception {
		String queryString = "from x : V{WhereExpression}, y : V{Variable} reportSet x <> y end";
		JValue result = evalTestQuery("NotEquals", queryString);
		assertEquals(1, result.toCollection().size());
		assertEquals(true, (boolean) getNthValue(result.toCollection(), 0)
				.toBoolean());
	}

	@Test
	public void testNotEquals2() throws Exception {
		String queryString = "from x : V{Greql2Expression}, y : V{Greql2Expression} reportSet x <> y end";
		JValue result = evalTestQuery("NotEquals2", queryString);
		assertEquals(1, result.toCollection().size());
		assertEquals(false, (boolean) getNthValue(result.toCollection(), 0)
				.toBoolean());
	}

	@Test
	public void testNodeTrace() throws Exception {
		MinimalGraph minimalGraph = MinimalSchema.instance()
				.createMinimalGraph(10, 10);
		ArrayList<Node> nodes = new ArrayList<Node>();
		ArrayList<Link> links = new ArrayList<Link>();
		for (int i = 0; i < 10; i++) {
			nodes.add(minimalGraph.createNode());
		}
		for (int i = 0; i < 9; i++) {
			links.add(minimalGraph.createLink(nodes.get(i), nodes.get(i + 1)));
		}
		boundVariables.put("rootNode", new JValue(nodes.get(3)));

		String queryString = "using rootNode: nodeTrace(extractPath(pathSystem(rootNode, -->+), 2)[0])";
		JValue result = evalTestQuery("NodeTrace", queryString, minimalGraph);
		assertEquals(2, result.toCollection().size());
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateSequentialPathDescription(SequentialPathDescription,
	 * Graph)'
	 */
	@Test
	public void testOr() throws Exception {
		String queryString = "from var: V{Variable}, def: V{WhereExpression} with var  -->{IsVarOf} -->{IsDefinitionOf} def or 1=2 report var end";
		JValue result = evalTestQuery("Or", queryString);
		assertEquals(4, result.toCollection().size());
	}

	@Test
	public void testPlus() throws Exception {
		String queryString = "6 + 1.5";
		JValue result = evalTestQuery("Plus", queryString);
		assertEquals(9, (int) ((result.toDouble() + 1.5)));
	}

	@Test
	public void testPlus2() throws Exception {
		String queryString = "\"foo\" + \"bar\" + \"baz\"";
		JValue result = evalTestQuery("Plus2", queryString);
		assertEquals("foobarbaz", result.toString());
	}

	@Test
	public void testPos() throws Exception {
		String queryString = "let x:= list (5..13) in pos(x, 7)";
		JValue result = evalTestQuery("Pos", queryString);
		assertEquals(2, (int) result.toInteger());
	}

	@Test
	public void testPos2() throws Exception {
		String queryString = "let x:= list (5..13) in pos(x, 2)";
		JValue result = evalTestQuery("Pos2", queryString);
		assertEquals(-1, (int) result.toInteger());
	}

	@Test
	public void testSiblings() throws Exception {
		String queryString = "from x: V{Definition} report siblings(x) end";
		JValue result = evalTestQuery("Siblings", queryString);
		assertEquals(4, result.toCollection().size());
		for (JValue j : result.toCollection()) {
			assertTrue(5 <= j.toCollection().size());
		}
	}

	@Test
	public void testSum() throws Exception {
		String queryString = "let x:= list (5..13) in sum(x)";
		JValue result = evalTestQuery("Sum", queryString);
		assertEquals(81, (int) (double) result.toDouble());
	}

	@Test
	public void testSymDifference() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(5,6,7,8) in symDifference(x, y)";
		JValue result = evalTestQuery("SymetricDifference", queryString);
		assertEquals(4, result.toCollection().size());
	}

	@Test
	public void testTimes() throws Exception {
		String queryString = "6 * 1.5";
		JValue result = evalTestQuery("Times", queryString);
		assertEquals(9, (int) (double) result.toDouble());
	}

	@Test
	public void testTimes2() throws Exception {
		String queryString = "\"foo\" * 3";
		JValue result = evalTestQuery("Times2", queryString);
		assertEquals("foofoofoo", result.toString());
	}

	@Test
	public void testUminus() throws Exception {
		String queryString = "let x:= list (5..13) in from i:x report -i end";
		JValue result = evalTestQuery("UMinus", queryString);
		assertEquals(9, result.toCollection().size());
		int sum = 0;
		for (JValue j : result.toCollection()) {
			sum += j.toInteger();
		}
		assertEquals(-81, sum);
	}

	@Test
	public void testUnion() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(5,6,7,8) in union(x, y)";
		JValue result = evalTestQuery("Union", queryString);
		assertEquals(6, result.toCollection().size());
	}

	@Test
	public void testTopologicalSort() throws Exception {
		String q = "topologicalSort()";
		JValue result = evalTestQuery("TopologicalSort", q);
		JValueList resultList = result.toJValueList();
		assertEquals(16, resultList.size());
		/*
		 * test, if for each vertex v in the result list each vertex w in
		 * Lambda^-(v) is contained in the result list at a lower position than
		 * v
		 */
		HashSet<Vertex> previousVertices = new HashSet<Vertex>();
		for (JValue value : resultList) {
			Vertex vertex = value.toVertex();
			for (Edge e : vertex.incidences(EdgeDirection.IN)) {
				assertTrue(previousVertices.contains(e.getAlpha()));
			}
			previousVertices.add(vertex);
		}
	}

	@Test
	public void testKeySet() throws Exception {
		String queryString = "from x : keySet(map(1 -> \"One\",   2 -> \"Two\", "
				+ "                               3 -> \"Three\", 4 -> \"Four\", "
				+ "                               5 -> \"Five\",  6 -> \"Six\")) "
				+ "           reportSet x end";
		JValue result = evalTestQuery("KeySet", queryString);
		JValueSet set = result.toJValueSet();
		assertEquals(6, set.size());
		assertTrue(set.contains(new JValue(1)));
		assertTrue(set.contains(new JValue(2)));
		assertTrue(set.contains(new JValue(3)));
		assertTrue(set.contains(new JValue(4)));
		assertTrue(set.contains(new JValue(5)));
		assertTrue(set.contains(new JValue(6)));
	}

	@Test
	public void testGet() throws Exception {
		String queryString = "let m := map(1 -> \"One\",   2 -> \"Two\",  "
				+ "                        3 -> \"Three\", 4 -> \"Four\", "
				+ "                        5 -> \"Five\",  6 -> \"Six\")  "
				+ "           in                                          "
				+ "           from x : keySet(m) "
				+ "           reportSet get(m, x) end";
		JValue result = evalTestQuery("Get", queryString);
		JValueSet set = result.toJValueSet();
		assertEquals(6, set.size());
		assertTrue(set.contains(new JValue("One")));
		assertTrue(set.contains(new JValue("Two")));
		assertTrue(set.contains(new JValue("Three")));
		assertTrue(set.contains(new JValue("Four")));
		assertTrue(set.contains(new JValue("Five")));
		assertTrue(set.contains(new JValue("Six")));
	}

	@Test
	public void testGrEqual() throws Exception {
		assertTrue(evalTestQuery("GrEqual", "3 >= 2").toBoolean());
		assertTrue(evalTestQuery("GrEqual", "17 >= 17").toBoolean());
		assertTrue(evalTestQuery("GrEqual", "grEqual(17, 17.0)").toBoolean());
		assertFalse(evalTestQuery("GrEqual", "17 >= 199)").toBoolean());
		assertTrue(evalTestQuery("GrEqual", "5.50 >= 4.701").toBoolean());
		assertTrue(evalTestQuery("GrEqual", "33.1 >= 33.1").toBoolean());
		assertTrue(evalTestQuery("GrEqual", "grEqual(117.4, 111)").toBoolean());
		assertFalse(evalTestQuery("GrEqual", "37 >= 119.01").toBoolean());
	}
}
