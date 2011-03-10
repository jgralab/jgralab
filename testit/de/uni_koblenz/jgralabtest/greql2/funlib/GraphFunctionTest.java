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
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueRecord;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTuple;
import de.uni_koblenz.jgralabtest.greql2.GenericTest;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.connections.Way;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.junctions.Crossroad;

public class GraphFunctionTest extends GenericTest {

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

	@Test
	public void testChildrenNull() throws Exception {
		assertQueryEqualsNull("using nll: children(nll)");
	}

	private Set<Vertex> getChildren(Vertex vertex) {
		Set<Vertex> children = new HashSet<Vertex>();
		for (Edge edge : vertex.incidences(EdgeDirection.OUT)) {
			children.add(edge.getThat());
		}
		return children;
	}

	public void testDegree(String query, Class<? extends Edge> clazz,
			EdgeDirection direction) throws Exception {
		JValueMap map = evalTestQuery(query).toJValueMap();

		for (Entry<JValue, JValue> entry : map.entrySet()) {
			Vertex vertex = entry.getKey().toVertex();
			Integer degree = entry.getValue().toInteger();

			assertEquals(vertex.getDegree(clazz, direction), degree.intValue());
		}
	}

	@Test
	public void testDegreeWithTypeCollection() throws Exception {
		EdgeDirection direction = EdgeDirection.INOUT;
		String query = "from v:V reportMap v -> degree(v) end";
		testDegree(query, Edge.class, direction);
		query = "from v:V reportMap v -> degree{connections.Way}(v) end";
		testDegree(query, Way.class, direction);
	}

	@Test
	public void testDegreeNull() throws Exception {
		assertQueryEqualsNull("using nll: degree(nll)");
	}

	@Test
	public void testDegreeWithTypeCollectionAndNull() throws Exception {
		assertQueryEqualsNull("using nll: degree(nll)");
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
	public void testDescribeNull() throws Exception {
		assertQueryEqualsNull("using nll: describe(nll)");
	}

	@Test
	public void testEdgeSeq() throws Exception {
		assertQueryEqualsQuery("edgeSeq(firstEdge(), lastEdge())", "E");
	}

	@Test
	public void testEdgeSeqWithTypeCollection() throws Exception {
		assertQueryEqualsQuery(
				"edgeSeq{connections.Way}(firstEdge(), lastEdge())",
				"E{connections.Way}");
	}

	@Test
	public void testEdgeSeqWithTypeCollectionAndNull() throws Exception {

		assertQueryEqualsNull("using nll: edgeSeq(nll, lastEdge())");
		assertQueryEqualsNull("using nll: edgeSeq(firstEdge(), nll)");
		assertQueryEqualsNull("using nll: edgeSeq(nll, nll)");

		assertQueryEqualsNull("using nll: edgeSeq{Edge}(nll, lastEdge())");
		assertQueryEqualsNull("using nll: edgeSeq{Edge}(firstEdge(), nll)");
		assertQueryEqualsNull("using nll: edgeSeq{Edge}(nll, nll)");

		assertQueryEqualsNull("using nll: edgeSeq(lastEdge(), firstEdge())");
		assertQueryEqualsNull("using nll: edgeSeq{Edge}(lastEdge(), firstEdge())");
	}

	public void testConnectedEdges(String query, Class<? extends Edge> clazz,
			EdgeDirection direction) throws Exception {

		JValueMap map = evalTestQuery(query).toJValueMap();

		for (Entry<JValue, JValue> entry : map.entrySet()) {
			Vertex vertex = entry.getKey().toVertex();
			JValueCollection connectedEdges = entry.getValue().toCollection();
			for (Edge edge : vertex.incidences(clazz, direction)) {
				boolean success = connectedEdges.remove(new JValueImpl(edge));
				assertTrue(success);
			}
			assertTrue(connectedEdges.isEmpty());
		}
	}

	@Test
	public void testEdgesConnectedWithTypeCollection() throws Exception {
		EdgeDirection direction = EdgeDirection.INOUT;
		String query = "from v:V reportMap v -> edgesConnected(v) end";
		testConnectedEdges(query, Edge.class, direction);
		query = "from v:V reportMap v -> edgesConnected{connections.Way}(v) end";
		testConnectedEdges(query, Way.class, direction);
	}

	@Test
	public void testEdgesConnectedNull() throws Exception {
		assertQueryEqualsNull("using nll: edgesConnected(nll)");
		assertQueryEqualsNull("using nll: edgesConnected{Vertex}(nll)");
	}

	@Test
	public void testEdgesFromWithTypeCollection() throws Exception {
		EdgeDirection direction = EdgeDirection.OUT;
		String query = "from v:V reportMap v -> edgesFrom(v) end";
		testConnectedEdges(query, Edge.class, direction);
		query = "from v:V reportMap v -> edgesFrom{connections.Way}(v) end";
		testConnectedEdges(query, Way.class, direction);
	}

	@Test
	public void testEdgesFromNull() throws Exception {
		assertQueryEqualsNull("using nll: edgesFrom(nll)");
		assertQueryEqualsNull("using nll: edgesFrom{Vertex}(nll)");
	}

	@Test
	public void testEdgesToWithTypeCollection() throws Exception {
		EdgeDirection direction = EdgeDirection.IN;
		String query = "from v:V reportMap v -> edgesTo(v) end";
		testConnectedEdges(query, Edge.class, direction);
		query = "from v:V reportMap v -> edgesTo{connections.Way}(v) end";
		testConnectedEdges(query, Way.class, direction);
	}

	@Test
	public void testEdgesToNull() throws Exception {
		assertQueryEqualsNull("using nll: edgesTo(nll)");
		assertQueryEqualsNull("using nll: edgesTo{Vertex}(nll)");
	}

	@Test
	public void testEndVertex() throws Exception {
		String query = "from e:E reportMap e -> endVertex(e) end";
		JValueMap map = evalTestQuery(query).toJValueMap();

		for (Entry<JValue, JValue> entry : map.entrySet()) {
			Edge edge = entry.getKey().toEdge();
			Vertex omega = entry.getValue().toVertex();

			assertEquals(edge.getOmega(), omega);
		}
	}

	@Test
	public void testEndVertexNull() throws Exception {
		assertQueryEqualsNull("using nll: endVertex(nll)");
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
		evalTestQuery("list(1..id(lastEdge())) ++ list(-id(lastEdge())..-1) store as idList");
		JValueMap map = evalTestQuery(
				"using idList: from el:idList reportMap el -> getEdge(el) end")
				.toJValueMap();
		Graph graph = getTestGraph(TestVersion.CITY_MAP_GRAPH);

		for (Entry<JValue, JValue> entry : map.entrySet()) {
			int id = entry.getKey().toInteger();
			Edge edge = entry.getValue().toEdge();
			assertEquals(graph.getEdge(id), edge);
		}
	}

	@Test
	public void testGetEdgeNull() throws Exception {
		assertQueryEqualsNull("using nll: getEdge(nll)");
		assertQueryEqualsNull("getEdge(0)");
		assertQueryEqualsNull("getEdge(id(lastEdge()) + 1)");
		assertQueryEqualsNull("getEdge(-id(lastEdge()) -1)");
	}

	@Test
	public void testGetGraph() throws Exception {
		assertQueryEquals("getGraph()",
				getTestGraph(TestVersion.CITY_MAP_GRAPH));
	}

	@Test
	public void testGetValue() throws Exception {
		String subQuery = "from name:attributeNames(el) reportMap name -> getValue(el, name) end";
		String query = "from el:union(E,V) reportMap el -> " + subQuery
				+ " end";
		JValueMap map = evalTestQuery(query).toJValueMap();

		for (Entry<JValue, JValue> entry : map.entrySet()) {
			AttributedElement element = entry.getKey().toAttributedElement();
			JValueMap attributes = entry.getValue().toJValueMap();

			for (Entry<JValue, JValue> attributePair : attributes.entrySet()) {
				String name = attributePair.getKey().toString();
				Object value = attributePair.getValue().toObject();

				Object expectedValue = element.getAttribute(name);
				assertEquals(expectedValue, value);
			}
		}
	}

	@Test
	public void testGetValueNull() throws Exception {
		evalTestQuery("V[1] store as vertex");
		evalTestQuery("using vertex: attributeNames(vertex)[0] store as attributeName");
		assertQueryEqualsNull("using nll: getValue(nll, nll)");
		assertQueryEqualsNull("using nll, attributeName: getValue(nll, attributeName)");
		assertQueryEqualsNull("using nll, vertex: getValue(vertex, nll)");
	}

	@Test
	public void testGetVertex() throws Exception {
		JValueMap map = evalTestQuery(
				"from el:list(1..id(lastVertex())) reportMap el -> getVertex(el) end")
				.toJValueMap();
		Graph graph = getTestGraph(TestVersion.CITY_MAP_GRAPH);

		for (Entry<JValue, JValue> entry : map.entrySet()) {
			int id = entry.getKey().toInteger();
			Vertex vertex = entry.getValue().toVertex();
			assertEquals(graph.getVertex(id), vertex);
		}
	}

	@Test
	public void testGetVertexNull() throws Exception {
		assertQueryEqualsNull("using nll: getVertex(nll)");
		assertQueryEqualsNull("getVertex(0)");
		assertQueryEqualsNull("getVertex(-1)");
		assertQueryEqualsNull("getVertex(id(lastVertex()) + 1)");
		assertQueryEqualsNull("getVertex(-id(lastVertex()) -1)");
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
	public void testIdNull() throws Exception {
		assertQueryEqualsNull("using nll: id(nll)");
	}

	@Test
	public void testInDegreeWithTypeCollection() throws Exception {
		EdgeDirection direction = EdgeDirection.IN;
		String query = "from v:V reportMap v -> inDegree(v) end";
		testDegree(query, Edge.class, direction);
		query = "from v:V reportMap v -> inDegree{connections.Way}(v) end";
		testDegree(query, Way.class, direction);
	}

	@Test
	public void testIsAcyclic() throws Exception {
		assertQueryEquals("isAcyclic()", false);
	}

	@Test
	public void testIsAcyclic2() throws Exception {
		String queryString = "isAcyclic()";
		JValue result = evalTestQuery("IsAcyclic", queryString);
		assertEquals(JValueBoolean.getTrueValue(), result.toBoolean());
	}

	@Test
	public void testIsAcyclic3() throws Exception {
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
		assertEquals(false, getNthValue(result.toCollection(), 0).toBoolean()
				.booleanValue());
	}

	@Test
	public void testIsNull() throws Exception {
		assertQueryEquals("isNull(list())", false);
		assertQueryEquals("using nll: isNull(nll)", true);
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
		assertQueryEqualsNull("lastEdge{Edge!}()");
	}

	@Test
	public void testLastVertex() throws Exception {
		Graph graph = getTestGraph(TestVersion.CITY_MAP_GRAPH);
		assertQueryEquals("lastVertex()", graph.getLastVertex());
		assertQueryEquals("lastVertex{junctions.Crossroad}()",
				getLastVertexForType(Crossroad.class));
		assertQueryEqualsNull("lastVertex{Vertex!}()");
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
	public void testOutDegreeWithTypeCollection() throws Exception {
		EdgeDirection direction = EdgeDirection.OUT;
		String query = "from v:V reportMap v -> outDegree(v) end";
		testDegree(query, Edge.class, direction);
		query = "from v:V reportMap v -> outDegree{connections.Way}(v) end";
		testDegree(query, Way.class, direction);
	}

	@Test
	public void testOutDegreeNull() throws Exception {
		assertQueryEqualsNull("using nll: outDegree(nll)");
	}

	@Test
	public void testSiblings() throws Exception {
		String query = "from v:V reportMap v -> siblings(v) end";
		JValueMap map = evalTestQuery(query).toJValueMap();

		for (Entry<JValue, JValue> entry : map.entrySet()) {
			Vertex vertex = entry.getKey().toVertex();
			Set<Vertex> parents = getParentVertices(vertex);
			JValueCollection siblings = entry.getValue().toCollection();

			for (JValue sibling : siblings) {
				Vertex siblingVertex = sibling.toVertex();
				Set<Vertex> siblingParents = getParentVertices(siblingVertex);

				boolean test = containsAny(parents, siblingParents);
				assertTrue(test);
			}
		}
	}

	@Test
	public void testSiblingsNull() throws Exception {
		assertQueryEqualsNull("using nll: siblings(nll)");
	}

	private Set<Vertex> getParentVertices(Vertex vertex) {
		Set<Vertex> parents = new HashSet<Vertex>();
		for (Edge outgoing : vertex.incidences(EdgeDirection.OUT)) {
			parents.add(outgoing.getOmega());
		}
		return parents;
	}

	private boolean containsAny(Set<Vertex> parents, Set<Vertex> siblingParents) {
		boolean containsAny = false;
		for (Vertex parent : parents) {
			containsAny = containsAny | siblingParents.contains(parent);
		}
		return containsAny;
	}

	@Test
	public void testStartVertex() throws Exception {
		String query = "from e:E reportMap e -> startVertex(e) end";
		JValueMap map = evalTestQuery(query).toJValueMap();

		for (Entry<JValue, JValue> entry : map.entrySet()) {
			Edge edge = entry.getKey().toEdge();
			Vertex alpha = entry.getValue().toVertex();

			assertEquals(edge.getAlpha(), alpha);
		}
	}

	@Test
	public void testStartVertexNull() throws Exception {
		assertQueryEqualsNull("using nll: startVertex(nll)");
	}

	@Test
	public void testTopologicalSort() throws Exception {
		String query = "topologicalSort()";
		assertQueryEqualsNull(query);
	}

	@Test
	public void testTopologicalSort2() throws Exception {
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

	@Test
	public void testVertexSeq() throws Exception {
		assertQueryEqualsQuery("vertexSeq(firstVertex(), lastVertex())", "V");
	}

	@Test
	public void testVertexSeqWithTypeCollection() throws Exception {
		assertQueryEqualsQuery(
				"vertexSeq{junctions.Crossroad}(firstVertex(), lastVertex())",
				"V{junctions.Crossroad}");
	}

	@Test
	public void testVertexSeqWithTypeCollectionAndNull() throws Exception {

		assertQueryEqualsNull("using nll: vertexSeq(nll, lastVertex())");
		assertQueryEqualsNull("using nll: vertexSeq(firstVertex(), nll)");
		assertQueryEqualsNull("using nll: vertexSeq(nll, nll)");

		assertQueryEqualsNull("using nll: vertexSeq{Vertex}(nll, lastVertex())");
		assertQueryEqualsNull("using nll: vertexSeq{Vertex}(firstVertex(), nll)");
		assertQueryEqualsNull("using nll: vertexSeq{Vertex}(nll, nll)");

		assertQueryEqualsNull("using nll: vertexSeq(lastVertex(), firstVertex())");
		assertQueryEqualsNull("using nll: vertexSeq{Vertex}(lastVertex(), firstVertex())");
	}

}
