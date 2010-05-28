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

package de.uni_koblenz.jgralabtest.greql2.funlib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePath;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.parser.ManualGreqlParser;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;
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
	public void testGetEdge() throws Exception {
		String dataGraphQuery = "true"; // should contains only one edge
		Greql2 dataGraph = ManualGreqlParser.parse(dataGraphQuery);
		JValue result = evalTestQuery("getEdge", "getEdge(1)", dataGraph);
		assertEquals(dataGraph.getFirstEdgeInGraph(), result.toEdge());
	}

	@Test
	public void testContainsKey1() throws Exception {
		JValueMap map = new JValueMap();
		map.put(new JValueImpl(1), new JValueImpl("a string"));
		setBoundVariable("emap", map);
		String queryString = "using emap: containsKey(emap, 1)";
		JValue result = evalTestQuery("ContainsKey1", queryString);
		assertTrue(result.toBoolean());
	}

	@Test
	public void testContainsKey2() throws Exception {
		JValueMap map = new JValueMap();
		map.put(new JValueImpl(1), new JValueImpl("a string"));
		setBoundVariable("emap", map);
		String queryString = "using emap: containsKey(emap, 2)";
		JValue result = evalTestQuery("ContainsKey2", queryString);
		assertFalse(result.toBoolean());
	}

	@Test
	public void testContainsValue1() throws Exception {
		JValueMap map = new JValueMap();
		map.put(new JValueImpl(1), new JValueImpl("a string"));
		setBoundVariable("emap", map);
		String queryString = "using emap: containsValue(emap, \"a string\")";
		JValue result = evalTestQuery("ContainsValue1", queryString);
		assertTrue(result.toBoolean());
	}

	@Test
	public void testContainsValue2() throws Exception {
		JValueMap map = new JValueMap();
		map.put(new JValueImpl(1), new JValueImpl("a string"));
		setBoundVariable("emap", map);
		String queryString = "using emap: containsValue(emap, 1)";
		JValue result = evalTestQuery("ContainsValue2", queryString);
		assertFalse(result.toBoolean());
	}

	@Test
	public void testContainsValue3() throws Exception {
		JValueMap map = new JValueMap();
		map.put(new JValueImpl(1), new JValueImpl("a string"));
		setBoundVariable("emap", map);
		String queryString = "using emap: containsValue(emap, \"string\")";
		JValue result = evalTestQuery("ContainsValue3", queryString);
		assertFalse(result.toBoolean());
	}

	@Test
	public void testAvg() throws Exception {
		String queryString = "let x:= list (5..13) in avg(x)";
		JValue result = evalTestQuery("Avg", queryString);
		assertEquals(9.0, result.toDouble(), 0.001);
	}

	@Test
	public void testContains() throws Exception {
		String queryString = "let x:= list (5..13) in contains(x, 7)";
		JValue result = evalTestQuery("ContainsTrue", queryString);
		assertEquals(true, (boolean) result.toBoolean());
	}

	@Test
	public void testContains2() throws Exception {
		String queryString = "let x:= list (5..13) in contains(x, 56)";
		JValue result = evalTestQuery("ContainsFalse", queryString);
		assertEquals(false, (boolean) result.toBoolean());
	}

	@Test
	public void testContains3() throws Exception {
		String queryString = "let x:= list (5..13) in contains(x, 13)";
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
	public void testDegree5() throws Exception {
		String queryString = "from x : V{BagComprehension} report edgesConnected(x) end";
		JValue result = evalTestQuery("Degree5", queryString);
		assertEquals(1, result.toCollection().size());
		for (JValue j : result.toCollection()) {
			System.out.println(j);
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
	public void testDiv() throws Exception {
		String queryString = "3/3";
		JValue result = evalTestQuery("Div", queryString);
		assertEquals(new JValueImpl(1.0), result);
	}

	@Test
	public void testDiv2() throws Exception {
		String queryString = "3.0/1";
		JValue result = evalTestQuery("Div", queryString);
		assertEquals(new JValueImpl(3.0), result);
	}

	@Test
	public void testDiv3() throws Exception {
		String queryString = "3/7";
		JValue result = evalTestQuery("Div", queryString);
		assertEquals(new JValueImpl(3.0 / 7), result);
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
	public void testTypes() throws Exception {
		String queryString = "from x : V{WhereExpression} report types(edgesConnected(x)) end";
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
				new JValueImpl("kaenguruhfleisch")));
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
				new JValueImpl("kaenguruhfleisch")));
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
		for (JValue j : result.toCollection()) {
			System.out.println("Element:" + j);
		}
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
		String queryString = "from v : V reportSet isCycle(extractPath(pathSystem(v, <->*), v)) end";
		JValue result = evalTestQuery("isCycle", queryString,
				getCyclicTestGraph());
		for (JValue v : result.toCollection()) {
			assertEquals(JValueBoolean.getTrueValue(), v.toBoolean());
		}
	}

	@Test
	public void testIsCycle1() throws Exception {
		String queryString = "from v,w : V with v <> w reportSet isCycle(extractPath(pathSystem(v, -->+), w)) end";
		JValue result = evalTestQuery("isCycle", queryString,
				getCyclicTestGraph());
		for (JValue v : result.toCollection()) {
			assertEquals(JValueBoolean.getFalseValue(), v.toBoolean());
		}
	}

	@Test
	public void testContains4() throws Exception {
		String queryString = "from v : V reportSet contains(eSubgraph{Link}, v) end";
		JValue result = evalTestQuery("Contains", queryString,
				getCyclicTestGraph());
		for (JValue v : result.toCollection()) {
			assertEquals(JValueBoolean.getTrueValue(), v.toBoolean());
		}
	}

	@Test
	public void testContains5() throws Exception {
		String queryString = "from v : V "
				+ "           in eSubgraph{Link}      "
				+ "           reportSet isIn(v) end";
		try {
			evalTestQuery("Contains5", queryString, getCyclicTestGraph());
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
	public void testSub() throws Exception {
		String queryString = "6 - 1.5";
		JValue result = evalTestQuery("Sub", queryString);
		assertEquals(4.5, result.toDouble(), 0.01);
	}

	@Test
	public void testSub2() throws Exception {
		String queryString = "6 - 3";
		JValue result = evalTestQuery("Sub", queryString);
		assertEquals(3l, (long) result.toLong());
	}

	@Test
	public void testSub3() throws Exception {
		String queryString = "16 - 323";
		JValue result = evalTestQuery("Sub", queryString);
		assertEquals(-307l, (long) result.toLong());
	}

	@Test
	public void testSub4() throws Exception {
		String queryString = "1.5 - 6";
		JValue result = evalTestQuery("Sub", queryString);
		assertEquals(-4.5, result.toDouble(), 0.01);
	}

	@Test
	public void testSub5() throws Exception {
		String queryString = "10 - 4 - 3 - 2";
		JValue result = evalTestQuery("Sub5", queryString);
		assertEquals(Integer.valueOf(1), result.toInteger());
	}

	@Test
	public void testMod() throws Exception {
		String queryString = "9 % 2";
		JValue result = evalTestQuery("Mod", queryString);
		assertEquals(Integer.valueOf(1), result.toInteger());
	}

	@Test
	public void testMultiplicative() throws Exception {
		String queryString = "100 / 10 / 5 * 2";
		JValue result = evalTestQuery("Mod", queryString);
		assertEquals(Double.valueOf(4), result.toDouble(), 0.01);
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
		JValuePath p = new JValuePath(nodes.get(0));
		p.addEdge(links.get(0));
		p.addEdge(links.get(1));
		p.addEdge(links.get(2));
		List<Vertex> estimatedList = new ArrayList<Vertex>();
		estimatedList.add(nodes.get(0));
		estimatedList.add(nodes.get(1));
		estimatedList.add(nodes.get(2));
		estimatedList.add(nodes.get(3));
		assertEquals(4, p.nodeTrace().size());
		for (int i = 0; i < estimatedList.size(); i++) {
			assertEquals(estimatedList.get(i), p.nodeTrace().get(i));
		}
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
	public void testAdd() throws Exception {
		String queryString = "6 + 1.5";
		JValue result = evalTestQuery("Add", queryString);
		assertEquals(9, (int) ((result.toDouble() + 1.5)));
	}

	@Test
	public void testConcat() throws Exception {
		String queryString = "\"foo\" ++ \"bar\" ++ \"baz\"";
		JValue result = evalTestQuery("Concat", queryString);
		assertEquals("foobarbaz", result.toString());
	}

	@Test
	public void testConcat2() throws Exception {
		String queryString = "list(1..3) ++ list(4..6)";
		JValue result = evalTestQuery("Concat", queryString);
		JValueList l = new JValueList();
		l.add(new JValueImpl(1));
		l.add(new JValueImpl(2));
		l.add(new JValueImpl(3));
		l.add(new JValueImpl(4));
		l.add(new JValueImpl(5));
		l.add(new JValueImpl(6));
		assertEquals(l, result);
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
	public void testSortList() throws Exception {
		String queryString = "sort(list(4, 1, 2, 10, 9, 7, 8, 3, 5, 6))";
		JValueList result = evalTestQuery("Sort1", queryString).toJValueList();
		assertEquals(10, result.size());
		for (int i = 1; i <= 10; i++) {
			assertEquals(new JValueImpl(i), result.get(i - 1));
		}
	}

	@Test
	public void testSortSet() throws Exception {
		String queryString = "sort(set(4, 1, 2, 10, 9, 7, 8, 3, 5, 6))";
		JValueList result = evalTestQuery("Sort1", queryString).toJValueList();
		assertEquals(10, result.size());
		for (int i = 1; i <= 10; i++) {
			assertEquals(new JValueImpl(i), result.get(i - 1));
		}
	}

	@Test
	public void testSortBag() throws Exception {
		String queryString = "sort(bag(4, 1, 2, 10, 9, 7, 8, 3, 5, 6))";
		JValueList result = evalTestQuery("Sort1", queryString).toJValueList();
		assertEquals(10, result.size());
		for (int i = 1; i <= 10; i++) {
			assertEquals(new JValueImpl(i), result.get(i - 1));
		}
	}

	@Test
	public void testSortMap() throws Exception {
		String queryString = "sort(from i : list (1..10) reportMap i, i*i end)";
		JValueMap result = evalTestQuery("Sort2", queryString).toJValueMap();
		assertEquals(10, result.size());
		int i = 1;
		for (Entry<JValue, JValue> e : result.entrySet()) {
			assertEquals(new JValueImpl(i), e.getKey());
			assertEquals(new JValueImpl(i * i), e.getValue());
			i++;
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
	public void testMul() throws Exception {
		String queryString = "6 * 1.5";
		JValue result = evalTestQuery("Mul", queryString);
		assertEquals(9, (int) (double) result.toDouble());
	}

	@Test
	public void testNeg() throws Exception {
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
	public void testUnion1() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(5,6,7,8) in union(x, y)";
		JValue result = evalTestQuery("Union1", queryString);
		assertEquals(6, result.toCollection().size());
	}

	@Test
	public void testUnion2() throws Exception {
		JValueMap map1 = new JValueMap();
		map1.put(new JValueImpl(1), new JValueImpl("A"));
		map1.put(new JValueImpl(2), new JValueImpl("A"));
		map1.put(new JValueImpl(3), new JValueImpl("B"));
		JValueMap map2 = new JValueMap();
		map2.put(new JValueImpl(4), new JValueImpl("A"));
		map2.put(new JValueImpl(5), new JValueImpl("C"));
		map2.put(new JValueImpl(6), new JValueImpl("D"));

		setBoundVariable("map1", map1);
		setBoundVariable("map2", map2);

		String queryString = "using map1, map2: union(map1, map2, true)";
		JValue result = evalTestQuery("Union2", queryString);
		assertEquals(6, result.toJValueMap().size());
		JValueMap rmap = result.toJValueMap();
		assertEquals(new JValueImpl("A"), rmap.get(new JValueImpl(1)));
		assertEquals(new JValueImpl("A"), rmap.get(new JValueImpl(2)));
		assertEquals(new JValueImpl("B"), rmap.get(new JValueImpl(3)));
		assertEquals(new JValueImpl("A"), rmap.get(new JValueImpl(4)));
		assertEquals(new JValueImpl("C"), rmap.get(new JValueImpl(5)));
		assertEquals(new JValueImpl("D"), rmap.get(new JValueImpl(6)));
	}

	@Test
	public void testUnion3() throws Exception {
		JValueMap map1 = new JValueMap();
		map1.put(new JValueImpl(1), new JValueImpl("A"));
		map1.put(new JValueImpl(2), new JValueImpl("A"));
		map1.put(new JValueImpl(3), new JValueImpl("B"));
		JValueMap map2 = new JValueMap();
		map2.put(new JValueImpl(1), new JValueImpl("A"));
		map2.put(new JValueImpl(3), new JValueImpl("C"));
		map2.put(new JValueImpl(4), new JValueImpl("D"));

		setBoundVariable("map1", map1);
		setBoundVariable("map2", map2);

		String queryString = "using map1, map2: union(map1, map2)";
		try {
			evalTestQuery("Union3", queryString);
			fail("Expected Exception on using union with two non-disjoint maps");
		} catch (Exception ex) {
			// :)
		}
	}

	@Test
	public void testUnion4() throws Exception {
		JValueSet set1 = new JValueSet();
		set1.add(new JValueImpl(1));
		set1.add(new JValueImpl(2));
		set1.add(new JValueImpl(3));

		JValueSet set2 = new JValueSet();
		set2.add(new JValueImpl(1));
		set2.add(new JValueImpl(2));
		set2.add(new JValueImpl(3));

		JValueSet set3 = new JValueSet();
		set3.add(new JValueImpl(3));
		set3.add(new JValueImpl(4));
		set3.add(new JValueImpl(5));

		JValueSet set4 = new JValueSet();
		set4.add(new JValueImpl(7));
		set4.add(new JValueImpl(8));
		set4.add(new JValueImpl(9));

		JValueSet cset = new JValueSet();
		cset.add(set1);
		cset.add(set2);
		cset.add(set3);
		cset.add(set4);

		setBoundVariable("cset", cset);

		String queryString = "using cset: union(cset)";
		JValue result = evalTestQuery("Union4", queryString);
		assertEquals(8, result.toJValueSet().size());
		JValueSet rset = result.toJValueSet();
		assertTrue(rset.contains(new JValueImpl(1)));
		assertTrue(rset.contains(new JValueImpl(2)));
		assertTrue(rset.contains(new JValueImpl(3)));
		assertTrue(rset.contains(new JValueImpl(4)));
		assertTrue(rset.contains(new JValueImpl(5)));
		assertTrue(rset.contains(new JValueImpl(7)));
		assertTrue(rset.contains(new JValueImpl(8)));
		assertTrue(rset.contains(new JValueImpl(9)));
	}

	@Test
	public void testIsEmpty1() throws Exception {
		JValueSet set1 = new JValueSet();
		set1.add(new JValueImpl(1));
		set1.add(new JValueImpl(2));
		set1.add(new JValueImpl(3));
		setBoundVariable("cset", set1);
		String queryString = "using cset: isEmpty(cset)";
		JValue result = evalTestQuery("IsEmpty1", queryString);
		assertEquals(false, result.toBoolean());
	}

	@Test
	public void testIsEmpty2() throws Exception {
		JValueSet set1 = new JValueSet();
		setBoundVariable("cset", set1);
		String queryString = "using cset: isEmpty(cset)";
		JValue result = evalTestQuery("IsEmpty2", queryString);
		assertEquals(true, result.toBoolean());
	}

	@Test
	public void testIsEmpty3() throws Exception {
		JValueMap map1 = new JValueMap();
		setBoundVariable("cset", map1);
		String queryString = "using cset: isEmpty(cset)";
		JValue result = evalTestQuery("IsEmpty3", queryString);
		assertEquals(true, result.toBoolean());
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
		assertTrue(set.contains(new JValueImpl(1)));
		assertTrue(set.contains(new JValueImpl(2)));
		assertTrue(set.contains(new JValueImpl(3)));
		assertTrue(set.contains(new JValueImpl(4)));
		assertTrue(set.contains(new JValueImpl(5)));
		assertTrue(set.contains(new JValueImpl(6)));
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
		assertTrue(set.contains(new JValueImpl("One")));
		assertTrue(set.contains(new JValueImpl("Two")));
		assertTrue(set.contains(new JValueImpl("Three")));
		assertTrue(set.contains(new JValueImpl("Four")));
		assertTrue(set.contains(new JValueImpl("Five")));
		assertTrue(set.contains(new JValueImpl("Six")));
	}

	@Test
	public void testGet2() throws Exception {
		String queryString = "let m := map(1 -> \"One\",   2 -> \"Two\",  "
				+ "                        3 -> \"Three\", 4 -> \"Four\", "
				+ "                        5 -> \"Five\",  6 -> \"Six\")  "
				+ "           in                                          "
				+ "           from x : keySet(m) "
				+ "           reportSet m[x] end";
		JValue result = evalTestQuery("Get", queryString);
		JValueSet set = result.toJValueSet();
		assertEquals(6, set.size());
		assertTrue(set.contains(new JValueImpl("One")));
		assertTrue(set.contains(new JValueImpl("Two")));
		assertTrue(set.contains(new JValueImpl("Three")));
		assertTrue(set.contains(new JValueImpl("Four")));
		assertTrue(set.contains(new JValueImpl("Five")));
		assertTrue(set.contains(new JValueImpl("Six")));
	}

	@Test
	public void testGrEqual() throws Exception {
		assertTrue(evalTestQuery("GrEqual", "3 >= 2").toBoolean());
		assertTrue(evalTestQuery("GrEqual", "17 >= 17").toBoolean());
		assertTrue(evalTestQuery("GrEqual", "grEqual(17, 17.0)").toBoolean());
		assertFalse(evalTestQuery("GrEqual", "17 >= 199").toBoolean());
		assertTrue(evalTestQuery("GrEqual", "5.50 >= 4.701").toBoolean());
		assertTrue(evalTestQuery("GrEqual", "33.1 >= 33.1").toBoolean());
		assertTrue(evalTestQuery("GrEqual", "grEqual(117.4, 111)").toBoolean());
		assertFalse(evalTestQuery("GrEqual", "3 >= 187.00001").toBoolean());
	}

	@Test
	public void testMergeMaps1() throws Exception {
		// merging equal maps should return an equal map
		assertEquals(
				evalTestQuery("expected MergeMaps",
						"map(tup(1,2) -> set(3), tup(3,4) -> set(7))"),
				evalTestQuery(
						"MergeMaps",
						"mergeMaps(map(tup(1,2) -> set(3), tup(3,4) -> set(7)), map(tup(1,2) -> set(3), tup(3,4) -> set(7)))"));
	}

	@Test
	public void testMergeMaps2() throws Exception {
		assertEquals(
				evalTestQuery("expected MergeMaps",
						"map(tup(1,2) -> set(3,4), tup(3,4) -> set(7,8,9))"),
				evalTestQuery(
						"MergeMaps",
						"mergeMaps(map(tup(1,2) -> set(3), tup(3,4) -> set(7)), map(tup(1,2) -> set(4), tup(3,4) -> set(8,9)))"));
	}
}
