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
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBag;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePath;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;
import de.uni_koblenz.jgralab.greql2.optimizer.DefaultOptimizer;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralabtest.greql2.GenericTest;

public class PathSystemFunctionTest extends GenericTest {

	/**
	 * ‚Ä¢ v :-) -->Œ± baut ein Pfadsystem √ºber Pfade, die dem regul√§ren
	 * Ausdruck Œ± entsprechen, mit dem Wurzelknoten v auf. ‚Ä¢ v :-) -->Œ± :-)
	 * w liefert einen Pfad der Gestalt Œ± von v nach w. ‚Ä¢ -->Œ± :-) w liefert
	 * ein Pfadsystem mit Pfaden der Gestalt Œ±T mit dem Wurzelknoten w. ‚Ä¢ v
	 * :-) ( -->Œ± :-) w ) liefert dementsprechend einen Pfad der Gestalt Œ±T
	 * von w nach v.
	 */

	private static JValuePath emptyPath, oneElementPath, twoElementPath,
			multipleElementPath, loopPath, longPath;
	private static JValuePathSystem emptyPathSystem;
	private static JValuePathSystem depthOnePathSystemWithOnePath,
			depthTwoPathSystemWithOnePath, multipleDepthPathSystemWithOnePath;
	private static JValuePathSystem depthOnePathSystemWithTwoPaths,
			depthTwoPathSystemWithTwoPaths,
			multipleDepthPathSystemWithTwoPaths;
	private static JValuePathSystem depthOnePathSystemWithMultiPaths,
			depthTwoPathSystemWithMultiPaths,
			multipleDepthPathSystemWithMultiPaths;

	@BeforeClass
	public static void initializePathAndPathSystemVariables()
			throws JValueInvalidTypeException, Exception {
		PathSystemFunctionTest t = new PathSystemFunctionTest();
		t
				.evalTestQuery("theElement(from v : V{localities.County} with v.name = 'Hessen' report v end) store as hessen");
		t
				.evalTestQuery("using hessen: pathSystem(hessen, -->{localities.ContainsLocality} -->{connections.AirRoute}* ) store as noPS");
		emptyPath = t.evalTestQuery(
				"using noPS: extractPath(noPS, firstVertex())").toPath();
		multipleElementPath = t.evalTestQuery(
				"using noPS: extractPath(noPS, 2)[0]").toPath();

		t
				.evalTestQuery("using hessen: pathSystem(hessen, -->{localities.ContainsLocality} ) store as PS");
		twoElementPath = t.evalTestQuery("using PS: extractPath(PS, 1)[0]")
				.toPath();

		t
				.evalTestQuery("theElement(from v : V{junctions.Crossroad} with v --> v report v end) store as suedallee");
		t
				.evalTestQuery("using suedallee: pathSystem(suedallee, -->{connections.Street}) store as PS");
		loopPath = t.evalTestQuery(
				"using suedallee, PS: extractPath(PS, suedallee)").toPath();

		t
				.evalTestQuery("using suedallee: pathSystem(suedallee, [-->{connections.Footpath}]) store as PS");
		oneElementPath = t.evalTestQuery(
				"using suedallee, PS: extractPath(PS, suedallee)").toPath();
		longPath = t
				.evalTestQuery(
						"extractPath(pathSystem(V{junctions.Crossroad}[0], <->*), 5)[0]")
				.toPath();

		// TODO initialize path systems
		t
				.evalTestQuery("theElement(from v : V{localities.County} with v.name = 'Berlin' report v end) store as berlin");
		emptyPathSystem = t
				.evalTestQuery(
						"using berlin : pathSystem(berlin, -->{localities.ContainsLocality})")
				.toPathSystem();

		t
				.evalTestQuery("theElement(from v: V{localities.County} with v.name = 'Rheinland-Pfalz' report v end) store as rp");
		depthOnePathSystemWithOnePath = t.evalTestQuery(
				"using rp: pathSystem(rp, -->{localities.HasCapital})")
				.toPathSystem();

		depthTwoPathSystemWithOnePath = t
				.evalTestQuery(
						"using hessen : pathSystem(hessen,-->{localities.HasCapital}-->{localities.ContainsCrossroad})")
				.toPathSystem();

		multipleDepthPathSystemWithOnePath = t
				.evalTestQuery(
						"using hessen : pathSystem(hessen,-->{localities.HasCapital}-->{localities.ContainsCrossroad}<--{connections.Highway})")
				.toPathSystem();
		depthOnePathSystemWithTwoPaths = t
				.evalTestQuery(
						"using hessen : pathSystem(hessen,-->{localities.ContainsLocality})")
				.toPathSystem();
		depthTwoPathSystemWithTwoPaths = t
				.evalTestQuery(
						"using hessen : pathSystem(hessen,-->{localities.ContainsLocality}-->{localities.ContainsCrossroad})")
				.toPathSystem();
		multipleDepthPathSystemWithTwoPaths = t
				.evalTestQuery(
						"using hessen : pathSystem(hessen,-->{localities.ContainsLocality}-->{localities.ContainsCrossroad}<--{connections.Highway})")
				.toPathSystem();

		depthOnePathSystemWithMultiPaths = t.evalTestQuery(
				"using rp : pathSystem(rp,-->)").toPathSystem();
		depthTwoPathSystemWithMultiPaths = t.evalTestQuery(
				"using rp : pathSystem(rp,-->^2)").toPathSystem();
		t
				.evalTestQuery("theElement(from v:V{junctions.Plaza} with v.name = 'L\u00f6hr-Center' report v end) store as loehrCenter");
		multipleDepthPathSystemWithMultiPaths = t
				.evalTestQuery(
						"using loehrCenter: pathSystem(loehrCenter, <->{connections.Street}+)")
				.toPathSystem();
	}

	@Before
	public void setPathAndPathSystemVariables() {
		setBoundVariable("emptyPath", emptyPath);
		setBoundVariable("oneElementPath", oneElementPath);
		setBoundVariable("twoElementPath", twoElementPath);
		setBoundVariable("multiElementPath", multipleElementPath);
		setBoundVariable("loopPath", loopPath);
		setBoundVariable("longPath", loopPath);

		setBoundVariable("emtpyPathSystem", emptyPathSystem);
		setBoundVariable("d1p1PS", depthOnePathSystemWithOnePath);
		setBoundVariable("d2p1PS", depthTwoPathSystemWithOnePath);
		setBoundVariable("dmp1PS", multipleDepthPathSystemWithOnePath);
		setBoundVariable("d1p2PS", depthOnePathSystemWithTwoPaths);
		setBoundVariable("d2p2PS", depthTwoPathSystemWithTwoPaths);
		setBoundVariable("dmp2PS", multipleDepthPathSystemWithTwoPaths);
		setBoundVariable("d1pmPS", depthOnePathSystemWithMultiPaths);
		setBoundVariable("d2pmPS", depthTwoPathSystemWithMultiPaths);
		setBoundVariable("dmpmPS", multipleDepthPathSystemWithMultiPaths);
	}

	@Test
	public void testContains() throws Exception {
		// TODO A meaningful test is missing for
		// MARKER x ATTRELEM -> BOOL
		fail();
	}

	// @Test
	public void testContainsNull() throws Exception {
		// TODO A meaningful test for test for the following signatures is
		// missing:
		// PATH x ATTRELEM -> BOOL
		// PATHSYSTEM x ATTRELEM -> BOOL
		// PATHSYSTEM x PATH -> BOOL
		// MARKER x ATTRELEM -> BOOL
		// SLICE x ATTRELEM -> BOOL
		//
		// The rest of the null test is implemented in the
		// CollectionFunctionTest.
		fail();
	}

	@Test
	public void testContainsPath() throws Exception {
		// TODO A meaningful test is missing for
		// PATH x ATTRELEM -> BOOL
		fail();
	}

	@Test
	public void testContainsPathSystem() throws Exception {
		String queryString = "from c: V{localities.County}, a:V{junctions.Airport} "
				+ "with c.name = 'Hessen' "
				+ "report contains(pathSystem(c, -->{localities.ContainsLocality} -->{connections.AirRoute}), a) "
				+ "end";
		JValue result = evalTestQuery("PathSystemContains", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(airportCount, bag.size());
		int falseFound = 0;
		for (JValue v : bag) {
			if (v.toBoolean() == false) {
				falseFound++;
			}
		}
		assertEquals(0, falseFound);

		// TODO A meaningful test is missing for
		// PATHSYSTEM x PATH -> BOOL
		fail();
	}

	@Test
	public void testContainsSlice() throws Exception {
		// TODO A meaningful test is missing for
		// SLICE x ATTRELEM -> BOOL
		fail();
	}

	@Test
	public void testDepth() throws Exception {
		// TODO A meaningful pathsystem for a test is missing
		fail();
	}

	@Test
	public void testDepthNull() throws Exception {
		assertQueryEqualsNull("using nll: depth(nll)");
	}

	@Test
	public void testDistancePathSystem() throws Exception {
		evalTestQuery("from c : V{localities.County} report pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad}) end store as pS");
		String queryString = "using pS: from p:pS, r:V{junctions.Crossroad} report distance(p, r)"
				+ "end";
		JValue result = evalTestQuery(queryString);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(crossroadCount * countyCount, bag.size());
		int falseFound = 0;
		for (JValue v : bag) {
			int distance = v.toInteger();
			if (distance > 0) {
				assertEquals(2, distance);
			} else {
				assertEquals(-1, distance);
				falseFound++;
			}
		}
		assertEquals(uncontainedCrossroadCount
				+ (crossroadCount * (countyCount - 1)), falseFound);
	}

	@Test
	public void testDistanceNull() throws Exception {
		// TODO A meaningful Pathsystem is missing
		assertQueryEqualsNull("using nll: distance(nll, nll)");
		// assertQueryEqualsNull("using nll: distance(?, nll)");
		// assertQueryEqualsNull("using nll: distance(nll, ?)");
		fail();
	}

	@Test
	public void testEdgesConnectedNull() throws Exception {
		// TODO A meaningful Pathsystem is missing
		assertQueryEqualsNull("using nll: edgesConnected(nll, nll)");
		assertQueryEqualsNull("using nll: edgesConnected(firstVertex(), nll)");
		// assertQueryEqualsNull("using nll: edgesConnected(nll, ?)");
		fail();
	}

	@Test
	public void testEdgesConnectedPath() throws Exception {
		// TODO A meaningful test is missing for
		// VERTEX x PATH -> COLLECTION
		fail();
	}

	@Test
	public void testEdgesConnectedPathSystem() throws Exception {
		String queryString = "from c: V{localities.County}, r:V{junctions.Crossroad} "
				+ "report edgesConnected(r, pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad}))"
				+ "end";
		JValue result = evalTestQuery("PathSystemEdgesConnected", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
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
		assertEquals(uncontainedCrossroadCount
				+ (crossroadCount * (countyCount - 1)), empty);
	}

	@Test
	public void testEdgesConnectedTypeCollection() throws Exception {
		// TODO A meaningful test is missing for
		// VERTEX x TYPECOLLECTION -> COLLECTION
		fail();
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
	public void testEdgesFromNull() throws Exception {
		// TODO A meaningful Pathsystem is missing
		assertQueryEqualsNull("using nll: edgesFrom(nll, nll)");
		assertQueryEqualsNull("using nll: edgesFrom(firstVertex(), nll)");
		// assertQueryEqualsNull("using nll: edgesFrom(nll, ?)");
		fail();
	}

	@Test
	public void testEdgesFromPath() throws Exception {
		// TODO A meaningful test is missing for
		// VERTEX x PATH -> COLLECTION
		fail();
	}

	@Test
	public void testEdgesFromPathSystem() throws Exception {
		String queryString = "from c: V{localities.County}, a:V{junctions.Airport} "
				+ "with c.name = 'Hessen' "
				+ "report edgesFrom(a, pathSystem(c, -->{localities.ContainsLocality} -->{connections.AirRoute})) "
				+ "end";
		JValue result = evalTestQuery("PathSystemEdgesFrom", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
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
	public void testEdgesToNull() throws Exception {
		// TODO A meaningful Pathsystem is missing
		assertQueryEqualsNull("using nll: edgesTo(nll, nll)");
		assertQueryEqualsNull("using nll: edgesTo(firstVertex(), nll)");
		// assertQueryEqualsNull("using nll: edgesTo(nll, ?)");
		fail();
	}

	@Test
	public void testEdgesToPath() throws Exception {
		// TODO A meaningful test is missing for
		// VERTEX x PATH -> COLLECTION
		fail();
	}

	@Test
	public void testEdgesToPathSystem() throws Exception {
		String queryString = "from c: V{localities.County}, r:V{junctions.Crossroad} "
				+ "report edgesTo(r, pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad}))"
				+ "end";
		JValue result = evalTestQuery("PathSystemEdgesTo", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
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
		assertEquals((crossroadCount * (countyCount - 1))
				+ uncontainedCrossroadCount, empty);
	}

	@Test
	public void testEdgesTraceNull() throws Exception {
		assertQueryEqualsNull("using nll: edgeTrace(nll)");
	}

	@Test
	public void testEdgeTrace() throws Exception {

		testEdgeTrace(emptyPath);
		testEdgeTrace(oneElementPath);
		testEdgeTrace(twoElementPath);
		testEdgeTrace(multipleElementPath);
		testEdgeTrace(longPath);
		testEdgeTrace(loopPath);
	}

	public void testEdgeTrace(JValuePath usedPath)
			throws JValueInvalidTypeException, Exception {
		setBoundVariable("usedPath", usedPath);
		JValueCollection trace = evalTestQuery(
				"using usedPath: edgeTrace(usedPath)").toCollection();

		List<Edge> edgeTrace = usedPath.edgeTrace();
		for (JValue tracedEdge : trace) {
			Edge edge = tracedEdge.toEdge();
			assertTrue(edgeTrace.remove(edge));
		}
		assertTrue(edgeTrace.isEmpty());
	}

	@Test
	public void testElementsPath() throws Exception {
		// TODO A meaningful test is missing for
		// PATH -> COLLECTION
		fail();
	}

	@Test
	public void testElementsPathSystem() throws Exception {
		// TODO A meaningful test is missing for
		// PATHSYSTEM -> COLLECTION
		fail();
	}

	@Test
	public void testElementsSlice() throws Exception {
		// TODO A meaningful test is missing for
		// SLICE -> COLLECTION
		fail();
	}

	@Test
	public void testEndVertex() throws Exception {
		testEndVertex(loopPath);
		testEndVertex(emptyPath);
		testEndVertex(oneElementPath);
		testEndVertex(twoElementPath);
		testEndVertex(multipleElementPath);
		testEndVertex(longPath);
	}

	private void testEndVertex(JValuePath usedPath) throws Exception {
		setBoundVariable("usedPath", usedPath);
		Vertex endVertex = usedPath.getEndVertex();
		assertQueryEquals("using usedPath: endVertex(usedPath)", endVertex);
	}

	@Test
	public void testExtractPathWithVertex() throws Exception {
		String queryString = "from c: V{localities.County}, r:V{junctions.Crossroad} "
				+ "report extractPath(pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad}), r)"
				+ "end";
		JValue result = evalTestQuery("ExtractPath", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
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
		assertEquals(uncontainedCrossroadCount
				+ (crossroadCount * (countyCount - 1)), invalidPaths);
	}

	@Test
	public void testExtractPath() throws Exception {
		// TODO A meaningful test is missing for
		// PATHSYSTEM -> PATH
		fail();
	}

	@Test
	public void testExtractPathWithInt() throws Exception {
		// TODO A meaningful test is missing for
		// PATHSYSTEM x INT -> COLLECTION
		fail();
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
	public void testInnerNodesPathSystem() throws Exception {

		String queryString = "from c: V{localities.County} with c.name <> 'Berlin'"
				+ "report innerNodes(pathSystem(c, -->{localities.ContainsLocality} -->{connections.AirRoute})) "
				+ "end";
		JValue result = evalTestQuery("InnerNodes", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(countyCount - 1, bag.size());
		for (JValue v : bag) {
			assertEquals(1, v.toCollection().size());
		}
	}

	@Test
	public void testInnerNodesSlice() throws Exception {
		// TODO A meaningful test is missing for
		// SLICE -> COLLECTION
		fail();
	}

	@Test
	public void testInnerNodesNull() throws Exception {
		assertQueryEqualsNull("using nll: innerNodes(nll)");
	}

	@Test
	public void testIsCycle() throws Exception {
		testIsCycle(loopPath);
		testIsCycle(emptyPath);
		testIsCycle(oneElementPath);
		testIsCycle(twoElementPath);
		testIsCycle(multipleElementPath);
		testIsCycle(longPath);
	}

	private void testIsCycle(JValuePath usedPath) throws Exception {
		setBoundVariable("usedPath", usedPath);
		boolean isCycle = (usedPath.getStartVertex() != null)
				&& (usedPath.getStartVertex() == usedPath.getEndVertex());
		assertQueryEquals("using usedPath: isCycle(usedPath)", isCycle);
	}

	@Test
	public void testIsCyclicNull() throws Exception {
		assertQueryEqualsNull("using nll: isCycle(nll)");
	}

	@Test
	public void testIsReachable() throws Exception {
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
	public void testIsReachableNull() throws Exception {
		// TODO A meaningful Pathsystem is missing
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
				TestVersion.ROUTE_MAP_GRAPH);
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
		// TODO A meaningful Pathsystem is missing

		assertQueryEqualsNull("using nll: isSubPathOf(nll, nll)");
		// assertQueryEqualsNull("using nll: isSubPathOf(?, nll)");
		// assertQueryEqualsNull("using nll: isSubPathOf(nll, ?)");
		fail();
	}

	@Test
	public void testLeavesPathSystem() throws Exception {
		String queryString = "from c: V{localities.County} with c.name <> 'Berlin'"
				+ "report leaves(pathSystem(c, -->{localities.ContainsLocality} <--{localities.HasCapital})) "
				+ "end";
		JValue result = evalTestQuery("Leaves", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(countyCount - 1, bag.size());
		for (JValue v : bag) {
			assertEquals(1, v.toCollection().size());
		}
	}

	@Test
	public void testLeavesSlice() throws Exception {
		// TODO A meaningful test is missing for
		// SLICE -> COLLECTION
		fail();
	}

	@Test
	public void testLeavesNull() throws Exception {
		assertQueryEqualsNull("using nll: leaves(nll)");
	}

	@Test
	public void testMatches() throws Exception {
		// TODO A meaningful Automaton is missing
		fail();
	}

	@Test
	public void testMatchesNull() throws Exception {
		// TODO A meaningful Automaton is missing
		assertQueryEqualsNull("using nll: matches(nll, nll)");
		// assertQueryEqualsNull("using nll: matches(nll, ?)");
		// assertQueryEqualsNull("using nll: matches(?, nll)");
		fail();
	}

	@Test
	public void testMaxPathLength() throws Exception {
		String queryString = "from c: V{localities.County} "
				+ "report minPathLength(pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad} -->{connections.Street})) "
				+ "end";
		JValue result = evalTestQuery("MaxPathLength", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(countyCount, bag.size());
		for (JValue v : bag) {
			assertEquals(3, (int) v.toInteger());
		}
	}

	@Test
	public void testMaxPathLengthNull() throws Exception {
		assertQueryEqualsNull("using nll: maxPathLength(nll)");
	}

	@Test
	public void testMinPathLength() throws Exception {
		// TODO This test fails! The course should be investigated
		String queryString = "from c: V{localities.County} "
				+ "report minPathLength(pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad} -->{connections.Street})) "
				+ "end";
		JValue result = evalTestQuery("MinPathLength", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(countyCount, bag.size());
		for (JValue v : bag) {
			assertEquals(3, (int) v.toInteger());
		}
	}

	@Test
	public void testMinPathLengthNull() throws Exception {
		assertQueryEqualsNull("using nll: minPathLength(nll)");
	}

	@Test
	public void testNodesPathSystem() throws Exception {
		// TODO A meaningful test is missing for
		// PATHSYSTEM -> COLLECTION
		fail();
	}

	@Test
	public void testNodesSlice() throws Exception {
		// TODO A meaningful test is missing for
		// SLICE -> COLLECTION
		fail();
	}

	@Test
	public void testNodesNull() throws Exception {
		assertQueryEqualsNull("using nll: nodes(nll)");
	}

	@Test
	public void testNodeTrace() throws Exception {
		testNodeTrace(loopPath);
		testNodeTrace(emptyPath);
		testNodeTrace(oneElementPath);
		testNodeTrace(twoElementPath);
		testNodeTrace(multipleElementPath);
		testNodeTrace(longPath);
	}

	private void testNodeTrace(JValuePath usedPath) throws Exception {
		setBoundVariable("usedPath", usedPath);

		JValueCollection collection = evalTestQuery(
				"using usedPath: nodeTrace(usedPath)").toCollection();
		List<Vertex> innerNodes = usedPath.nodeTrace();
		for (JValue value : collection) {
			Vertex innerNode = value.toVertex();
			assertTrue(innerNodes.remove(innerNode));
		}
		assertTrue(innerNodes.isEmpty());

	}

	@Test
	public void testNodeTrace2() throws Exception {
		// TODO This test fails! The course should be investigated.
		String queryString = "from c: V{localities.County}, r:V{junctions.Crossroad}"
				+ "report nodeTrace(extractPath(pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad}), r)) "
				+ "end";
		JValue result = evalTestQuery("PathLength", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
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
		// TODO This test fails! The course should be investigated.
		String queryString = "from c: V{localities.County}, r:V{junctions.Crossroad} "
				+ "report parent(pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad}), r) "
				+ "end";
		JValue result = evalTestQuery("Parent", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
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
		// TODO A meaningful Pathsystem is missing
		assertQueryEqualsNull("using nll: parent(nll, nll)");
		// assertQueryEqualsNull("using nll: parent(?, nll)");
		// assertQueryEqualsNull("using nll: parent(nll, ?)");
		fail();
	}

	@Test
	public void testPathConcat() throws Exception {
		// TODO This function should be renamed to concat
		// TODO A meaningful Paths are missing
		fail();
	}

	@Test
	public void testPathConcatNull() throws Exception {
		// TODO A meaningful Paths are missing
		assertQueryEqualsNull("using nll: pathConcat(nll, nll)");
		// assertQueryEqualsNull("using nll: pathConcat(?, nll)");
		// assertQueryEqualsNull("using nll: pathConcat(nll, ?)");
		fail();
	}

	@Test
	public void testPathExpr() throws Exception {
		// TODO A meaningful Automaton Expression is missing
		fail();
	}

	@Test
	public void testPathExprNull() throws Exception {
		assertQueryEqualsNull("using nll: pathExpr(nll)");
	}

	@Test
	public void testPathLength() throws Exception {
		testPathLength(loopPath);
		testPathLength(emptyPath);
		testPathLength(oneElementPath);
		testPathLength(twoElementPath);
		testPathLength(multipleElementPath);
		testPathLength(longPath);
	}

	private void testPathLength(JValuePath usedPath) throws Exception {
		setBoundVariable("usedPath", usedPath);
		int length = usedPath.pathLength();
		assertQueryEquals("using usedPath: pathLength(usedPath)", length);
	}

	@Test
	public void testPathLengthNull() throws Exception {
		assertQueryEqualsNull("using nll: pathLength(nll)");
	}

	@Test
	public void testPathSystem() throws Exception {
		// TODO A meaningful test is missing for
		// VERTEX x AUTOMATON -> COLLECTION
		fail();
	}

	@Test
	public void testPathSystemNull() throws Exception {
		// TODO A meaningful test is missing for
		// * x * -> COLLECTION
		fail();
	}

	@Test
	public void testReachableVertex() throws Exception {
		// TODO Has to be implemented
		fail();
	}

	@Test
	public void testReachableVertexNull() throws Exception {
		// TODO A meaningful Pathsystem is missing
		assertQueryEqualsNull("using nll: reachableVertices(nll, nll)");
		// assertQueryEqualsNull("using nll: reachableVertices(nll, ?)");
		assertQueryEqualsNull("using nll: reachableVertices(firstVertex(), nll)");

		assertQueryEqualsNull("using nll: reachableVertices(nll, nll, nll)");
		// assertQueryEqualsNull("using nll: reachableVertices(nll, ?, nll)");
		assertQueryEqualsNull("using nll: reachableVertices(firstVertex(), nll, nll)");
		// assertQueryEqualsNull("using nll: reachableVertices(firstVertex(), ?, nll)");

		// assertQueryEqualsNull("using nll: reachableVertices(nll, nll, ?)");
		// assertQueryEqualsNull("using nll: reachableVertices(nll, ?, ?)");
		// assertQueryEqualsNull("using nll: reachableVertices(firstVertex(), nll, ?)");
		// assertQueryEqualsNull("using nll: reachableVertices(firstVertex(), ?, ?)");
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
				TestVersion.ROUTE_MAP_GRAPH);
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
		// TODO A meaningful Pathsystem is missing
		assertQueryEqualsNull("using nll: siblings(nll, nll)");
		// assertQueryEqualsNull("using nll: siblings(nll, ?)");
		assertQueryEqualsNull("using nll: siblings(firstVertex(), nll)");
		fail();
	}

	@Test
	public void testSliceCollection() throws Exception {
		// TODO A meaningful test is missing for
		// COLLECTION x AUTOMATON -> COLLECTION
		fail();
	}

	@Test
	public void testSliceVertex() throws Exception {
		// TODO A meaningful test is missing for
		// VERTEX x AUTOMATON -> COLLECTION
		fail();
	}

	@Test
	public void testSliceNull() throws Exception {
		// TODO Has to be implemented
		// TODO A meaningful Slice are missing
		fail();
	}

	@Test
	public void testStartVertex() throws Exception {
		testStartVertex(loopPath);
		testStartVertex(emptyPath);
		testStartVertex(oneElementPath);
		testStartVertex(twoElementPath);
		testStartVertex(multipleElementPath);
		testStartVertex(longPath);
	}

	private void testStartVertex(JValuePath usedPath) throws Exception {
		setBoundVariable("usedPath", usedPath);
		Vertex startVertex = usedPath.getStartVertex();
		assertQueryEquals("using usedPath: startVertex(usedPath)", startVertex);
	}

	// @Test
	public void testStartVertexNull() throws Exception {
		// See GraphFunctionTest
	}

	@Test
	public void testTypes() throws Exception {
		// A pathSystem starting at vertex 25 captures vertices and edges of all
		// concrete types.
		String queryString = "types(pathSystem(getVertex(25), <->*))";
		JValueCollection result = evalTestQuery("TypeSet", queryString,
				TestVersion.ROUTE_MAP_GRAPH).toCollection();
		GraphClass gc = getTestGraph(TestVersion.ROUTE_MAP_GRAPH).getSchema()
				.getGraphClass();
		int nonAbstractClassCount = 0;
		for (GraphElementClass gec : gc.getGraphElementClasses()) {
			if (gec.isAbstract()) {
				continue;
			}
			nonAbstractClassCount++;
			assertTrue(result.contains(JValueImpl.fromObject(gec)));
		}
		assertEquals(nonAbstractClassCount, result.size());
	}

	@Test
	public void testWeightNull() throws Exception {
		assertQueryEqualsNull("using nll: weight(nll)");
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateForwardVertexSet(ForwardVertexSet,
	 * Graph)'
	 */
	@Test
	public void testWeightPathSystem() throws Exception {
		// TODO This test fails! The course should be investigated.
		String queryString = "from c: V{localities.County} "
				+ "with c.name <> 'Rheinland-Pfalz' "
				+ "report weight(pathSystem(c, -->{localities.ContainsLocality} -->{connections.AirRoute})) "
				+ "end";
		JValue result = evalTestQuery("PathSystemWeight", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(1, bag.size());
		for (JValue v : bag) {
			assertEquals(4, (int) v.toInteger());
		}
	}
}
