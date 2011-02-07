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

package de.uni_koblenz.jgralabtest.greql2.funlib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePath;
import de.uni_koblenz.jgralab.greql2.parser.GreqlParser;
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

	@Test
	public void testGetEdge() throws Exception {
		String dataGraphQuery = "true"; // should contains only one edge
		Greql2 dataGraph = GreqlParser.parse(dataGraphQuery);
		JValue result = evalTestQuery("getEdge", "getEdge(1)", dataGraph);
		assertEquals(dataGraph.getFirstEdge(), result.toEdge());
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
	public void testEdgesConnected() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{WhereExpression} report edgesConnected(x) end";
		JValue result = evalTestQuery("EdgesConnected", queryString);
		assertEquals(6, getNthValue(result.toCollection(), 0).toCollection()
				.size());
	}

	@Test
	public void testEdgesFrom() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{WhereExpression} report edgesFrom(x) end";
		JValue result = evalTestQuery("EdgesFrom", queryString);
		assertEquals(1, result.toCollection().size());
		assertEquals(1, getNthValue(result.toCollection(), 0).toCollection()
				.size());
	}

	@Test
	public void testEdgesTo() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{WhereExpression} report edgesTo(x) end";
		JValue result = evalTestQuery("EdgesTo", queryString);
		assertEquals(1, result.toCollection().size());
		for (JValue j : result.toCollection()) {
			assertEquals(5, j.toCollection().size());
		}
	}

	@Test
	public void testTypes() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{WhereExpression} report types(edgesConnected(x)) end";
		JValue result = evalTestQuery("EdgeTypeSet", queryString);
		assertEquals(3, getNthValue(result.toCollection(), 0).toCollection()
				.size());
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateRecordConstruction(RecordConstruction,
	 * Graph)'
	 */
	@Test
	public void testEvaluateListAccess() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
		assertTrue(result.toCollection().toJValueSet()
				.contains(new JValueImpl("kaenguruhfleisch")));
	}

	@Test
	public void testGetValue() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{WhereExpression} report hasType(x, \"WhereExpression\") end";
		JValue result = evalTestQuery("HasType", queryString);
		assertEquals(1, result.toCollection().size());
		assertEquals(true, (boolean) getNthValue(result.toCollection(), 0)
				.toBoolean());
	}

	@Test
	public void testHasType2() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{WhereExpression} report hasType(x, \"Variable\") end";
		JValue result = evalTestQuery("HasType2", queryString);
		assertEquals(1, result.toCollection().size());
		assertEquals(false, (boolean) getNthValue(result.toCollection(), 0)
				.toBoolean());
	}

	@Test
	public void testHasType3() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{WhereExpression} report isIsolated(x) end";
		JValue result = evalTestQuery("IsIsolated", queryString);
		assertEquals(1, result.toCollection().size());
		assertEquals(false, (boolean) getNthValue(result.toCollection(), 0)
				.toBoolean());
	}

	@Test
	public void testIsLoop() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : E{IsBoundExprOfDefinition} report isLoop(x) end";
		JValue result = evalTestQuery("IsLoop", queryString);
		assertEquals(1, result.toCollection().size());
		assertEquals(false, (boolean) getNthValue(result.toCollection(), 0)
				.toBoolean());
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

	@Test
	public void testSiblings() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x: V{Definition} report siblings(x) end";
		JValue result = evalTestQuery("Siblings", queryString);
		assertEquals(4, result.toCollection().size());
		for (JValue j : result.toCollection()) {
			assertTrue(5 <= j.toCollection().size());
		}
	}

	@Test
	public void testTopologicalSort() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
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
}
