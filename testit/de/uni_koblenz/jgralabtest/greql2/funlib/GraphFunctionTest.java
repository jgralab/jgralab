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
package de.uni_koblenz.jgralabtest.greql2.funlib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueRecord;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTuple;
import de.uni_koblenz.jgralab.greql2.parser.GreqlParser;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.connections.Way;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.junctions.Crossroad;

public class GraphFunctionTest extends GenericTests {

	@Test
	public void testChildren() throws Exception {
		JValueMap map = evalTestQuery("from v:V reportMap v -> children(v) end")
				.toJValueMap().toJValueMap();

		for (Entry<JValue, JValue> entry : map.entrySet()) {
			Vertex vertex = entry.getKey().toVertex();
			JValueSet children = entry.getValue().toJValueSet();

			Set<Vertex> children2 = getChildren(vertex);

			for (JValue child : children) {
				System.out.println(child);
				assertTrue(child.toString() + " not in " + children2,
						children2.remove(child.toVertex()));
			}
			assertTrue(children2 + " is not empty", children2.isEmpty());
		}
	}

	private Set<Vertex> getChildren(Vertex vertex) {
		Set<Vertex> children = new HashSet<Vertex>();
		for (Edge edge : vertex.incidences(EdgeDirection.OUT)) {
			children.add(edge.getThat());
		}
		return children;
	}

	@Test
	public void testDegree() throws Exception {
		JValueMap map = evalTestQuery("from v:V reportMap v -> degree(v) end")
				.toJValueMap();

		for (Entry<JValue, JValue> entry : map.entrySet()) {
			Vertex vertex = entry.getKey().toVertex();
			Integer degree = entry.getValue().toInteger();

			assertEquals(vertex.getDegree(), degree, DELTA);
		}
	}

	@Test
	public void testDescribeGraphElements() throws Exception {
		JValueMap map = evalTestQuery("from v:V reportMap v -> describe(v) end")
				.toJValueMap();

		for (Entry<JValue, JValue> entry : map.entrySet()) {
			Vertex vertex = entry.getKey().toVertex();
			JValueTuple tuple = entry.getValue().toJValueTuple();

			assertEquals(vertex.getAttributedElementClass().getQualifiedName(),
					tuple.get(0).toString());
			assertEquals(vertex.getId(), tuple.get(1).toInteger().intValue());
			JValueRecord attributeRecord = tuple.get(2).toJValueRecord();
			for (Entry<String, JValue> recordEntry : attributeRecord.entrySet()) {
				assertEquals(vertex.getAttribute(recordEntry.getKey()),
						recordEntry.getValue().toObject());
			}
		}
	}

	@Test
	public void testDescribeGraph() throws Exception {
		JValueTuple tuple = evalTestQuery("describe()").toJValueTuple();
		Graph graph = getTestGraph(TestVersion.CITY_MAP_GRAPH);

		assertEquals(graph.getAttributedElementClass().getQualifiedName(),
				tuple.get(0).toString());
		JValueRecord versionRecord = tuple.get(1).toJValueRecord();
		assertEquals(graph.getId(), versionRecord.get("id").toObject());
		assertEquals(graph.getGraphVersion(), versionRecord.get("version")
				.toObject());
		JValueRecord attributeRecord = tuple.get(2).toJValueRecord();
		for (Entry<String, JValue> recordEntry : attributeRecord.entrySet()) {
			assertEquals(graph.getAttribute(recordEntry.getKey()), recordEntry
					.getValue().toObject());
		}
	}

	@Test
	public void testEdgeSeq() throws Exception {
		assertQueryEqualsQuery("edgeSeq(firstEdge(), lastEdge())", "E");
	}

	@Test
	public void testFirstEdge() throws Exception {
		Graph graph = getTestGraph(TestVersion.CITY_MAP_GRAPH);
		assertQueryEquals("firstEdge()", graph.getFirstEdge());
		assertQueryEquals("firstEdge{connections.Way}()",
				graph.getFirstEdge(Way.class));
		assertQueryEquals("firstEdge{Edge!}()", (Edge) null);
	}

	@Test
	public void testFirstVertex() throws Exception {
		Graph graph = getTestGraph(TestVersion.CITY_MAP_GRAPH);
		assertQueryEquals("firstVertex()", graph.getFirstVertex());
		assertQueryEquals("firstVertex{junctions.Crossroad}()",
				graph.getFirstVertex(Crossroad.class));
		assertQueryEquals("firstVertex{Vertex!}()", (Vertex) null);
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
		JValueMap map = evalTestQuery(
				"from el:union(V,E) reportMap el -> id(el) end").toJValueMap();

		for (Entry<JValue, JValue> entry : map.entrySet()) {
			GraphElement element = (GraphElement) entry.getKey().toObject();
			int id = entry.getValue().toInteger();
			assertEquals(element.getId(), id);
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
	public void testLastEdge() throws Exception {
		Graph graph = getTestGraph(TestVersion.CITY_MAP_GRAPH);
		assertQueryEquals("lastEdge()", graph.getLastEdge());
		assertQueryEquals("lastEdge{connections.Way}()",
				getLastEdgeForType(Way.class));
		assertQueryEquals("lastEdge{Edge!}()", (Edge) null);
	}

	@Test
	public void testLastVertex() throws Exception {
		Graph graph = getTestGraph(TestVersion.CITY_MAP_GRAPH);
		assertQueryEquals("lastVertex()", graph.getLastVertex());
		assertQueryEquals("lastVertex{junctions.Crossroad}()",
				getLastVertexForType(Crossroad.class));
		assertQueryEquals("lastVertex{Vertex!}()", (Vertex) null);
	}

	public Edge getLastEdgeForType(Class<? extends Edge> type) throws Exception {
		Graph graph = getTestGraph(TestVersion.CITY_MAP_GRAPH);
		Edge lastEdge = graph.getLastEdge();

		while (lastEdge != null) {
			if (type.isAssignableFrom(lastEdge.getClass())) {
				break;
			}
			lastEdge = lastEdge.getPrevEdge();
		}

		return lastEdge;
	}

	public Vertex getLastVertexForType(Class<? extends Vertex> type)
			throws Exception {
		Graph graph = getTestGraph(TestVersion.CITY_MAP_GRAPH);
		Vertex lastVertex = graph.getLastVertex();

		while (lastVertex != null) {
			if (type.isAssignableFrom(lastVertex.getClass())) {
				break;
			}
			lastVertex = lastVertex.getPrevVertex();
		}

		return lastVertex;
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
