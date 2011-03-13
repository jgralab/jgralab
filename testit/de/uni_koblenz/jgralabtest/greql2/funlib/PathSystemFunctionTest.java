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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBag;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePath;
import de.uni_koblenz.jgralabtest.greql2.GenericTest;
import de.uni_koblenz.jgralabtest.schemas.minimal.Link;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.minimal.Node;

public class PathSystemFunctionTest extends GenericTest {

	/**
	 * ‚Ä¢ v :-) -->Œ± baut ein Pfadsystem √ºber Pfade, die dem regul√§ren
	 * Ausdruck Œ± entsprechen, mit dem Wurzelknoten v auf. ‚Ä¢ v :-) -->Œ± :-)
	 * w liefert einen Pfad der Gestalt Œ± von v nach w. ‚Ä¢ -->Œ± :-) w liefert
	 * ein Pfadsystem mit Pfaden der Gestalt Œ±T mit dem Wurzelknoten w. ‚Ä¢ v
	 * :-) ( -->Œ± :-) w ) liefert dementsprechend einen Pfad der Gestalt Œ±T
	 * von w nach v.
	 */

	@Test
	public void testContains() throws Exception {
		// TODO
		fail();
	}

	@Test
	public void testContainsPathSystem() throws Exception {
		String queryString = "from c: V{localities.County}, a:V{junctions.Airport} "
				+ "with c.name <> 'Rheinland-Pfalz' "
				+ "report contains(pathSystem(c, -->{localities.ContainsLocality} -->{connections.AirRoute}), a) "
				+ "end";
		JValue result = evalTestQuery("PathSystemContains", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(airportCount, bag.size());
		int falseFound = 0;
		for (JValue v : bag) {
			if (v.toBoolean() == false) {
				falseFound++;
			}
		}
		assertEquals(0, falseFound);
	}

	// @Test
	public void testContainsNull() throws Exception {
		// See CollectionFunctions
	}

	@Test
	public void testDepth() throws Exception {
		// TODO
		fail();
	}

	@Test
	public void testDepthNull() throws Exception {
		assertQueryEqualsNull("using nll: depth(nll)");
	}

	@Test
	public void testDistance() throws Exception {
		// TODO
		fail();
	}

	@Test
	public void testDistanceNull() throws Exception {
		// TODO
		assertQueryEqualsNull("using nll: distance(nll, nll)");
		// assertQueryEqualsNull("using nll: distance(?, nll)");
		// assertQueryEqualsNull("using nll: distance(nll, ?)");
		fail();
	}

	@Test
	public void testDistancePathSystem() throws Exception {
		String queryString = "from c: V{localities.County}, r:V{junctions.Crossroad} "
				+ "report distance(pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad}), r)"
				+ "end";
		JValue result = evalTestQuery("PathSystemDistance", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(crossroadCount * countyCount, bag.size());
		int falseFound = 0;
		for (JValue v : bag) {
			int distance = v.toInteger();
			if (distance > 0) {
				System.out.println(distance);
				assertEquals(2, distance);
			} else {
				assertEquals(-1, distance);
				falseFound++;
			}
		}
		assertEquals(uncontainedCrossroadCount + crossroadCount, falseFound);
	}

	@Test
	public void testEdgesConnected() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{WhereExpression} report edgesConnected(x) end";
		JValue result = evalTestQuery("EdgesConnected", queryString);
		JValue result2 = evalTestQuery("", "V{WhereExpression}");
		System.out.println(result2);
		assertEquals(6, getNthValue(result.toCollection(), 0).toCollection()
				.size());
	}

	// @Test
	public void testEdgesConnectedNull() throws Exception {
		// See GraphFunctions
	}

	@Test
	public void testEdgesConnectedPathSystem() throws Exception {
		String queryString = "from c: V{localities.County}, r:V{junctions.Crossroad} "
				+ "report edgesConnected(r, pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad}))"
				+ "end";
		JValue result = evalTestQuery("PathSystemEdgesConnected", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(crossroadCount * countyCount, bag.size());
		int empty = 0;
		for (JValue v : bag) {
			int connectedEdges = v.toCollection().size();
			if (connectedEdges == 0) {
				empty++;
			} else {
				assertEquals(1, connectedEdges);
			}
		}
		assertEquals(uncontainedCrossroadCount + crossroadCount, empty);
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

	// @Test
	public void testEdgesFromNull() throws Exception {
		// See GraphFunctions
	}

	@Test
	public void testEdgesFromPathSystem() throws Exception {
		String queryString = "from c: V{localities.County}, a:V{junctions.Airport} "
				+ "with c.name <> 'Rheinland-Pfalz' "
				+ "report edgesFrom(a, pathSystem(c, -->{localities.ContainsLocality} -->{connections.AirRoute})) "
				+ "end";
		JValue result = evalTestQuery("PathSystemEdgesFrom", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(airportCount, bag.size());
		int empty = 0;
		for (JValue v : bag) {
			int edgeCount = v.toCollection().size();
			if (edgeCount == 0) {
				empty++;
			} else {
				assertEquals(2, edgeCount);
			}
		}
		// TODO this value is hard-coded and therefore not good.
		assertEquals(2, empty);
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

	// @Test
	public void testEdgesToNull() throws Exception {
		// See GraphFunctions
	}

	@Test
	public void testEdgesToPathSystem() throws Exception {
		String queryString = "from c: V{localities.County}, r:V{junctions.Crossroad} "
				+ "report edgesTo(r, pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad}))"
				+ "end";
		JValue result = evalTestQuery("PathSystemEdgesTo", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(crossroadCount * countyCount, bag.size());
		int empty = 0;
		for (JValue v : bag) {
			int connectedEdges = v.toCollection().size();
			if (connectedEdges == 0) {
				empty++;
			} else {
				assertEquals(1, connectedEdges);
			}
		}
		assertEquals(crossroadCount + uncontainedCrossroadCount, empty);
	}

	@Test
	public void testEdgeTrace() throws Exception {
		String queryString = "from v: V{localities.County}, w: V{junctions.Crossroad}"
				+ "report edgeTrace(extractPath(pathSystem(v, -->{localities.ContainsLocality} {junctions.Airport} & -->{localities.ContainsCrossroad}), w)) end";
		JValue result = evalTestQuery("EdgeTrace", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();

		assertEquals(countyCount * crossroadCount, bag.size());
		int traceCount = 0;
		for (JValue value : bag) {
			int size = value.toCollection().size();
			if (size == 0) {
				continue;
			}

			traceCount++;
			assertEquals(2, value.toCollection().size());
		}
		assertEquals(airportCount, traceCount);
	}

	@Test
	public void testEdgesTraceNull() throws Exception {
		assertQueryEqualsNull("using nll: edgeTrace(nll)");
	}

	@Test
	public void testElements() throws Exception {
		// TODO
		fail();
	}

	// @Test
	public void testElementsNull() throws Exception {
		// See CollectionFunctions
	}

	@Test
	public void testEndVertex() throws Exception {
		// TODO
		fail();
	}

	// @Test
	public void testEndVertexNull() throws Exception {
		// See GraphFunctions
	}

	@Test
	public void testExtractPath() throws Exception {
		String queryString = "from c: V{localities.County}, r:V{junctions.Crossroad} "
				+ "report extractPath(pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad}), r)"
				+ "end";
		JValue result = evalTestQuery("ExtractPath", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(countyCount * crossroadCount, bag.size());
		int invalidPaths = 0;
		for (JValue v : bag) {
			JValuePath p = v.toPath();
			if (!p.isValidPath()) {
				invalidPaths++;
			} else {
				assertEquals(2, p.pathLength());
			}
		}
		assertEquals(uncontainedCrossroadCount + crossroadCount, invalidPaths);
	}

	@Test
	public void testExtractPathNull() throws Exception {
		// TODO
		assertQueryEqualsNull("using nll: extractPath(nll)");
		assertQueryEqualsNull("using nll: extractPath(nll, nll)");
		// assertQueryEqualsNull("using nll: extractPath(?, nll)");
		assertQueryEqualsNull("using nll: extractPath(nll, 1)");
		assertQueryEqualsNull("using nll: extractPath(nll, firstVertex())");
	}

	@Test
	public void testInnerNodes() throws Exception {

		String queryString = "from c: V{localities.County} "
				+ "report innerNodes(pathSystem(c, -->{localities.ContainsLocality} -->{connections.AirRoute})) "
				+ "end";
		JValue result = evalTestQuery("InnerNodes", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(countyCount, bag.size());
		for (JValue v : bag) {
			assertEquals(1, v.toCollection().size());
		}
	}

	@Test
	public void testInnerNodesNull() throws Exception {
		assertQueryEqualsNull("using nll: innerNodes(nll)");
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
	public void testIsCycle2() throws Exception {
		String queryString = "from c: V{localities.County}, r:V{junctions.Crossroad}"
				+ "report isCycle(extractPath(pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad}), r)) "
				+ "end";
		JValue result = evalTestQuery("isCycle", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(countyCount * crossroadCount, bag.size());
		for (JValue v : bag) {
			assertFalse(v.toBoolean());
		}
	}

	@Test
	public void testIsCyclicNull() throws Exception {
		assertQueryEqualsNull("using nll: isCycle(nll)");
	}

	@Test
	public void testIsReachable() throws Exception {
		// TODO
		fail();
	}

	@Test
	public void testIsReachableNull() throws Exception {
		// TODO
		assertQueryEqualsNull("using nll: isReachable(nll, nll, nll)");
		assertQueryEqualsNull("using nll: isReachable(firstVertex(), nll, nll)");
		assertQueryEqualsNull("using nll: isReachable(nll, lastVertex(), nll)");
		assertQueryEqualsNull("using nll: isReachable(firstVertex(), lastVertex(), nll)");

		// assertQueryEqualsNull("using nll: isReachable(nll, nll, ?)");
		// assertQueryEqualsNull("using nll: isReachable(firstVertex(), nll, ?)");
		// assertQueryEqualsNull("using nll: isReachable(nll, lastVertex(), ?)");
		// assertQueryEqualsNull("using nll: isReachable(firstVertex(), lastVertex(), ?)");
		fail();
	}

	@Test
	public void testIsSubPathOf() throws Exception {
		String queryString = "from v: V{localities.County}, a:V{junctions.Airport}, w: V{junctions.Crossroad} report"
				+ " isSubPathOf( "
				+ "extractPath(pathSystem(v, -->{localities.ContainsLocality}), a), "
				+ "extractPath(pathSystem(v, -->{localities.ContainsLocality} a -->{localities.ContainsCrossroad}), w)) end";
		JValue result = evalTestQuery("IsSubPath", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(countyCount * airportCount * crossroadCount, bag.size());
		int trueCounts = 0;
		for (JValue v : bag) {
			if (v.toBoolean()) {
				trueCounts++;
			}
		}
		assertEquals(airportCount, trueCounts);
	}

	@Test
	public void testIsSubPathOfNull() throws Exception {
		// TODO

		assertQueryEqualsNull("using nll: isSubPathOf(nll, nll)");
		// assertQueryEqualsNull("using nll: isSubPathOf(?, nll)");
		// assertQueryEqualsNull("using nll: isSubPathOf(nll, ?)");
		fail();
	}

	@Test
	public void testLeaves() throws Exception {
		String queryString = "from c: V{localities.County} "
				+ "report leaves(pathSystem(c, -->{localities.ContainsLocality} <--{localities.HasCapital})) "
				+ "end";
		JValue result = evalTestQuery("Leaves", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(countyCount, bag.size());
		for (JValue v : bag) {
			assertEquals(1, v.toCollection().size());
		}
	}

	@Test
	public void testLeavesNull() throws Exception {
		assertQueryEqualsNull("using nll: leaves(nll)");
	}

	@Test
	public void testMatches() throws Exception {
		// TODO
		fail();
	}

	@Test
	public void testMatchesNull() throws Exception {
		assertQueryEqualsNull("using nll: matches(nll, nll)");
		assertQueryEqualsNull("using nll: matches(nll, ?)");
		assertQueryEqualsNull("using nll: matches(?, nll)");
	}

	@Test
	public void testMaxPathLength() throws Exception {
		String queryString = "from c: V{localities.County} "
				+ "report minPathLength(pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad} -->{connections.Street})) "
				+ "end";
		JValue result = evalTestQuery("MaxPathLength", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(countyCount, bag.size());
		for (JValue v : bag) {
			assertEquals(3, (int) v.toInteger());
		}
	}

	@Test
	public void testMaxPathLengthNull() throws Exception {
		assertQueryEqualsNull("using nll: maxLength(nll)");
	}

	@Test
	public void testMinPathLength() throws Exception {
		String queryString = "from c: V{localities.County} "
				+ "report minPathLength(pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad} -->{connections.Street})) "
				+ "end";
		JValue result = evalTestQuery("MinPathLength", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(countyCount, bag.size());
		for (JValue v : bag) {
			assertEquals(3, (int) v.toInteger());
		}
	}

	@Test
	public void testMinPathLengthNull() throws Exception {
		assertQueryEqualsNull("using nll: minLength(nll)");
		fail();
	}

	@Test
	public void testNodes() throws Exception {
		// TODO
		fail();
	}

	@Test
	public void testNodesNull() throws Exception {
		assertQueryEqualsNull("using nll: nodes(nll)");
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
	public void testNodeTrace2() throws Exception {
		String queryString = "from c: V{localities.County}, r:V{junctions.Crossroad}"
				+ "report nodeTrace(extractPath(pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad}), r)) "
				+ "end";
		JValue result = evalTestQuery("PathLength", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(countyCount * crossroadCount, bag.size());
		int emptyTraces = 0;
		for (JValue v : bag) {
			if (v.toCollection().isEmpty()) {
				emptyTraces++;
			} else {
				assertEquals(3, v.toCollection().size());
			}
		}
		assertEquals(crossroadCount + uncontainedCrossroadCount, emptyTraces);
	}

	@Test
	public void testNodeTraceNull() throws Exception {
		assertQueryEqualsNull("using nll: nodeTrace(nll)");
	}

	@Test
	public void testParent() throws Exception {
		String queryString = "from c: V{localities.County}, r:V{junctions.Crossroad} "
				+ "report parent(pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad}), r) "
				+ "end";
		JValue result = evalTestQuery("Parent", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(countyCount * crossroadCount, bag.size());
		int invalid = 0;
		for (JValue v : bag) {
			if (!v.isValid()) {
				invalid++;
			}
		}
		assertEquals(crossroadCount + uncontainedCrossroadCount, invalid);
	}

	@Test
	public void testParentNull() throws Exception {
		// TODO
		assertQueryEqualsNull("using nll: parent(nll, nll)");
		// assertQueryEqualsNull("using nll: parent(?, nll)");
		// assertQueryEqualsNull("using nll: parent(nll, ?)");
		fail();
	}

	@Test
	public void testPathConcat() throws Exception {
		// TODO
		fail();
	}

	@Test
	public void testPathConcatNull() throws Exception {
		// TODO
		assertQueryEqualsNull("using nll: pathConcat(nll, nll)");
		// assertQueryEqualsNull("using nll: pathConcat(?, nll)");
		// assertQueryEqualsNull("using nll: pathConcat(nll, ?)");
		fail();
	}

	@Test
	public void testPathExpr() throws Exception {
		// TODO
		fail();
	}

	@Test
	public void testPathExprNull() throws Exception {
		assertQueryEqualsNull("using nll: pathExpr(nll)");
	}

	@Test
	public void testPathLength() throws Exception {
		String queryString = "from c: V{localities.County}, r:V{junctions.Crossroad}"
				+ "report pathLength(extractPath(pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad}), r)) "
				+ "end";
		JValue result = evalTestQuery("PathLength", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(countyCount * crossroadCount, bag.size());
		int nullPath = 0;
		for (JValue v : bag) {
			if (v.toInteger() == 0) {
				nullPath++;
			} else {
				assertEquals(2, (int) v.toInteger());
			}
		}
		assertEquals(crossroadCount + uncontainedCrossroadCount, nullPath);
	}

	@Test
	public void testPathLengthNull() throws Exception {
		assertQueryEqualsNull("using nll: pathLength(nll)");
	}

	@Test
	public void testReachableVertex() throws Exception {
		// TODO
		fail();
	}

	@Test
	public void testReachableVertexNull() throws Exception {
		// TODO
		assertQueryEqualsNull("using nll: pathConcat(nll, nll)");
		// assertQueryEqualsNull("using nll: pathConcat(nll, ?)");
		assertQueryEqualsNull("using nll: pathConcat(firstVertex(), nll)");

		assertQueryEqualsNull("using nll: pathConcat(nll, nll, nll)");
		// assertQueryEqualsNull("using nll: pathConcat(nll, ?, nll)");
		assertQueryEqualsNull("using nll: pathConcat(firstVertex(), nll, nll)");
		// assertQueryEqualsNull("using nll: pathConcat(firstVertex(), ?, nll)");

		assertQueryEqualsNull("using nll: pathConcat(nll, nll, ?)");
		// assertQueryEqualsNull("using nll: pathConcat(nll, ?, ?)");
		// assertQueryEqualsNull("using nll: pathConcat(firstVertex(), nll, ?)");
		// assertQueryEqualsNull("using nll: pathConcat(firstVertex(), ?, ?)");
		fail();
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
	public void testSiblings2() throws Exception {
		String queryString = "from c: V{localities.County}, r:V{junctions.Crossroad} "
				+ "report siblings(r, pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad})) "
				+ "end";
		JValue result = evalTestQuery("Siblings", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();

		assertEquals(countyCount * crossroadCount, bag.size());

		int noSiblingsFound = 0;
		int expectedValue = crossroadCount - 2 * uncontainedCrossroadCount;

		for (JValue v : bag) {
			int size = v != null ? v.toCollection().size() : 0;

			if (size == 0) {
				noSiblingsFound++;
			} else {
				assertEquals(expectedValue, v.toCollection().size());
			}
		}
	}

	@Test
	public void testSiblingsNull() throws Exception {
		// TODO
		assertQueryEqualsNull("using nll: pathConcat(nll, nll)");
		// assertQueryEqualsNull("using nll: pathConcat(nll, ?)");
		assertQueryEqualsNull("using nll: pathConcat(firstVertex(), nll)");
		fail();
	}

	@Test
	public void testSlice() throws Exception {
		// TODO
		fail();
	}

	@Test
	public void testSliceNull() throws Exception {
		// TODO
		fail();
	}

	@Test
	public void testTypes() throws Exception {
		String queryString = "from c: V{localities.County}"
				+ "report types(pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad})) "
				+ "end";
		JValue result = evalTestQuery("TypeSet", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(countyCount, bag.size());
		// TODO this test is not well thought through
		for (JValue v : bag) {
			int size = v.toCollection().size();
			assertTrue(size == 6 || size == 10);
		}
	}

	@Test
	public void testStartVertex() throws Exception {
		// TODO
		fail();
	}

	// @Test
	public void testStartVertexNull() throws Exception {
		// See GraphFunctionTest
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateForwardVertexSet(ForwardVertexSet,
	 * Graph)'
	 */
	@Test
	public void testWeightPathSystem() throws Exception {
		String queryString = "from c: V{localities.County} "
				+ "with c.name <> 'Rheinland-Pfalz' "
				+ "report weight(pathSystem(c, -->{localities.ContainsLocality} -->{connections.AirRoute})) "
				+ "end";
		JValue result = evalTestQuery("PathSystemWeight", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(1, bag.size());
		for (JValue v : bag) {
			assertEquals(4, (int) v.toInteger());
		}
	}

	@Test
	public void testWeightNull() throws Exception {
		assertQueryEqualsNull("using nll: weight(nll)");
	}
}
