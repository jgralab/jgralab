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

import java.util.HashSet;
import java.util.Iterator;

import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.parser.GreqlParser;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;

public class GraphFunctionTest extends GenericTests {

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
	public void testDegree5() throws Exception {
		String queryString = "from x : V{BagComprehension} report edgesConnected(x) end";
		JValue result = evalTestQuery("Degree5", queryString);
		assertEquals(1, result.toCollection().size());
		for (JValue j : result.toCollection()) {
			System.out.println(j);
		}
	}

	@Test
	public void testGetEdge() throws Exception {
		String dataGraphQuery = "true"; // should contains only one edge
		Greql2 dataGraph = GreqlParser.parse(dataGraphQuery);
		JValue result = evalTestQuery("getEdge", "getEdge(1)", dataGraph);
		assertEquals(dataGraph.getFirstEdge(), result.toEdge());
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
	public void testId() throws Exception {
		String queryString = "from x : V{Greql2Expression} report id(x) end";
		JValue result = evalTestQuery("Id", queryString);
		assertEquals(1, result.toCollection().size());
		for (JValue j : result.toCollection()) {
			assertEquals(1, (int) j.toInteger());
		}
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
