/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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

package de.uni_koblenz.jgralabtest.greql.funlib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePath;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.optimizer.DefaultOptimizer;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralabtest.greql2.GenericTest;

public class PathSystemFunctionTest extends GenericTest {

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
				"using noPS: extractPath(noPS, getVertex(157))").toPath();

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

		setBoundVariable("emptyPathSystem", emptyPathSystem);
		setBoundVariable("d1p1PathSystem", depthOnePathSystemWithOnePath);
		setBoundVariable("d2p1PathSystem", depthTwoPathSystemWithOnePath);
		setBoundVariable("dmp1PathSystem", multipleDepthPathSystemWithOnePath);
		setBoundVariable("d1p2PathSystem", depthOnePathSystemWithTwoPaths);
		setBoundVariable("d2p2PathSystem", depthTwoPathSystemWithTwoPaths);
		setBoundVariable("dmp2PathSystem", multipleDepthPathSystemWithTwoPaths);
		setBoundVariable("d1pmPathSystem", depthOnePathSystemWithMultiPaths);
		setBoundVariable("d2pmPathSystem", depthTwoPathSystemWithMultiPaths);
		setBoundVariable("dmpmPathSystem",
				multipleDepthPathSystemWithMultiPaths);
	}

	@Test
	public void testPathContainsElement() throws Exception {
		// PATH x ATTRELEM -> BOOL

		// path with two edges in normal direction
		assertQueryIsTrue("using multiElementPath: contains(multiElementPath,getVertex(144))");
		assertQueryIsTrue("using multiElementPath: contains(multiElementPath,getEdge(354))");
		assertQueryIsFalse("using multiElementPath: contains(multiElementPath,getEdge(-354))");
		assertQueryIsTrue("using multiElementPath: contains(multiElementPath,getVertex(155))");
		assertQueryIsTrue("using multiElementPath: contains(multiElementPath,getEdge(297))");
		assertQueryIsFalse("using multiElementPath: contains(multiElementPath,getEdge(-297))");
		assertQueryIsTrue("using multiElementPath: contains(multiElementPath,getVertex(157))");

		// path with two edges with one reversed edge
		JValuePath result = evalTestQuery(
				"extractPath(pathSystem(getVertex(143),-->{localities.HasCapital}<--{localities.ContainsLocality}-->{localities.ContainsLocality}),getVertex(153))")
				.toPath();

		setBoundVariable("testPathForContainsElement1", result);
		assertQueryIsTrue("using testPathForContainsElement1: contains(testPathForContainsElement1,getVertex(143))");
		assertQueryIsTrue("using testPathForContainsElement1: contains(testPathForContainsElement1,getEdge(345))");
		assertQueryIsFalse("using testPathForContainsElement1: contains(testPathForContainsElement1,getEdge(-345))");
		assertQueryIsTrue("using testPathForContainsElement1: contains(testPathForContainsElement1,getVertex(152))");
		assertQueryIsTrue("using testPathForContainsElement1: contains(testPathForContainsElement1,getEdge(-348))");
		assertQueryIsFalse("using testPathForContainsElement1: contains(testPathForContainsElement1,getEdge(348))");
		assertQueryIsTrue("using testPathForContainsElement1: contains(testPathForContainsElement1,getEdge(347))");
		assertQueryIsFalse("using testPathForContainsElement1: contains(testPathForContainsElement1,getEdge(-347))");
		assertQueryIsTrue("using testPathForContainsElement1: contains(testPathForContainsElement1,getVertex(153))");

		// path with arbitrary elements not contained in path
		assertQueryIsFalse("using testPathForContainsElement1: contains(testPathForContainsElement1,getEdge(1))");
		assertQueryIsFalse("using testPathForContainsElement1: contains(testPathForContainsElement1,getEdge(-1))");
		assertQueryIsFalse("using testPathForContainsElement1: contains(testPathForContainsElement1,getVertex(1))");
		assertQueryIsFalse("using testPathForContainsElement1: contains(testPathForContainsElement1,getVertex(30))");

		// is null contained?
		assertQueryIsUndefined("using nll,testPathForContainsElement1: contains(testPathForContainsElement1,nll)");

	}

	@Test
	public void testPathSystemContainsElement() throws Exception {
		String queryString = "from c: V{localities.County}, a:V{junctions.Airport} "
				+ "with c.name = 'Hessen' "
				+ "report contains(pathSystem(c, -->{localities.ContainsLocality} -->{connections.AirRoute}), a) "
				+ "end";
		JValue result = evalTestQuery("PathSystemContains", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
		JValueList list = result.toCollection().toJValueList();
		assertEquals(airportCount, list.size());
		int falseFound = 0;
		for (JValue v : list) {
			if (v.toBoolean() == false) {
				falseFound++;
			}
		}
		assertEquals(0, falseFound);
	}

	@Test
	public void testPathSystemContainsPath() throws Exception {

		// take two arbitrary paths from path system
		assertQueryIsTrue("using dmpmPathSystem: contains(dmpmPathSystem,extractPath(dmpmPathSystem,getVertex(133)))");
		assertQueryIsTrue("using dmpmPathSystem: contains(dmpmPathSystem,extractPath(dmpmPathSystem,getVertex(86)))");

		// path from start vertex
		assertQueryIsTrue("using dmpmPathSystem: contains(dmpmPathSystem,extractPath(dmpmPathSystem,getVertex(142)))");

		// empty path
		assertQueryIsFalse("using dmpmPathSystem,emptyPath: contains(dmpmPathSystem,emptyPath)");

		// arbitrary path not included in PS
		assertQueryIsFalse("using dmpmPathSystem,multiElementPath: contains(dmpmPathSystem,multiElementPath)");

	}

	@Test
	public void testPathSystemContainsNull() throws Exception {
		assertQueryIsUndefined("using emptyPathSystem,nll : contains(emptyPathSystem,nll)");
		assertQueryIsUndefined("using d1p1PathSystem,nll : contains(d1p1PathSystem,nll)");
		assertQueryIsUndefined("using d1p2PathSystem,nll : contains(d1p2PathSystem,nll)");
		assertQueryIsUndefined("using d1pmPathSystem,nll : contains(d1pmPathSystem,nll)");
		assertQueryIsUndefined("using d2p1PathSystem,nll : contains(d2p1PathSystem,nll)");
		assertQueryIsUndefined("using d2p2PathSystem,nll : contains(d2p2PathSystem,nll)");
		assertQueryIsUndefined("using d2pmPathSystem,nll : contains(d2pmPathSystem,nll)");
		assertQueryIsUndefined("using dmp1PathSystem,nll : contains(dmp1PathSystem,nll)");
		assertQueryIsUndefined("using dmp2PathSystem,nll : contains(dmp2PathSystem,nll)");
		assertQueryIsUndefined("using dmpmPathSystem,nll : contains(dmpmPathSystem,nll)");
	}

	// TODO markers cannot be bound to variables

	// @Test
	// public void testMarkerContainsElement() throws Exception {
	// // TODO how to create a marker for using in GReQL
	// fail();
	// }
	//
	// @Test
	// public void testMarkerConstainsNull() throws Exception {
	// // TODO how to create a marker for using in GReQL
	// fail();
	// }

	@Test
	public void testDepth() throws Exception {
		assertQueryEquals("using emptyPathSystem : depth(emptyPathSystem)", 0);
		assertQueryEquals("using d1p1PathSystem : depth(d1p1PathSystem)", 1);
		assertQueryEquals("using d1p2PathSystem : depth(d1p2PathSystem)", 1);
		assertQueryEquals("using d1pmPathSystem : depth(d1pmPathSystem)", 1);
		assertQueryEquals("using d2p1PathSystem : depth(d2p1PathSystem)", 2);
		assertQueryEquals("using d2p2PathSystem : depth(d2p2PathSystem)", 2);
		assertQueryEquals("using d2pmPathSystem : depth(d2pmPathSystem)", 2);
		assertQueryEquals("using dmp1PathSystem : depth(dmp1PathSystem)", 3);
		assertQueryEquals("using dmp2PathSystem : depth(dmp2PathSystem)", 3);
		assertQueryEquals("using dmpmPathSystem : depth(dmpmPathSystem)", 14);
	}

	@Test
	public void testDepthNull() throws Exception {
		assertQueryIsUndefined("using nll: depth(nll)");
	}

	@Test
	public void testDistancePathSystem() throws Exception {
		evalTestQuery("from c : V{localities.County} report pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad}) end store as pS");
		String queryString = "using pS: from p:pS, r:V{junctions.Crossroad} report distance(p, r)"
				+ "end";
		JValue result = evalTestQuery(queryString);
		JValueList list = result.toCollection().toJValueList();
		assertEquals(crossroadCount * countyCount, list.size());
		int falseFound = 0;
		for (JValue v : list) {
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
		assertQueryIsUndefined("using dmpmPathSystem,nll: distance(dmpmPathSystem,nll)");
		assertQueryIsUndefined("using nll: distance(nll,firstVertex())");
		assertQueryIsUndefined("using nll: distance(nll, nll)");
	}

	@Test
	public void testEdgesConnectedVertexOnly() throws Exception {
		JValueSet result = evalTestQuery(
				"let v20 := getVertex(20) in edgesConnected(v20)")
				.toJValueSet();
		Graph testgraph = getTestGraph(TestVersion.ROUTE_MAP_GRAPH);
		assertTrue(result
				.contains(JValueImpl.fromObject(testgraph.getEdge(-2))));
		assertTrue(result.contains(JValueImpl
				.fromObject(testgraph.getEdge(315))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-316))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-336))));
	}

	@Test
	public void testEdgesConnectedPath() throws Exception {
		// create a sample path with three vertices to test the method with the
		// first vertex, the last vertex and a vertex in between
		JValuePath samplePath = evalTestQuery(
				"using dmpmPathSystem: extractPath(dmpmPathSystem,getVertex(103))")
				.toPath();

		setBoundVariable("samplePath", samplePath);
		Graph testgraph = getTestGraph(TestVersion.ROUTE_MAP_GRAPH);

		JValue result = evalTestQuery("using samplePath: theElement(edgesConnected(getVertex(142),samplePath))");
		assertEquals(testgraph.getEdge(-294), result.toEdge());

		result = evalTestQuery("using samplePath: edgesConnected(getVertex(104),samplePath)");
		JValueCollection col = result.toCollection();
		assertTrue(col.contains(JValueImpl.fromObject(testgraph.getEdge(294))));
		assertTrue(col.contains(JValueImpl.fromObject(testgraph.getEdge(-232))));

		result = evalTestQuery("using samplePath: theElement(edgesConnected(getVertex(103),samplePath))");
		assertEquals(testgraph.getEdge(232), result.toEdge());

		// not in PS
		col = evalTestQuery(
				"using dmpmPathSystem: edgesConnected(getVertex(64),dmpmPathSystem)")
				.toCollection();
		assertEquals(0, col.size());
	}

	@Test
	public void testEdgesConnectedPathSystem() throws Exception {
		String queryString = "from c: V{localities.County}, r:V{junctions.Crossroad} "
				+ "report edgesConnected(r, pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad}))"
				+ "end";
		JValue result = evalTestQuery("PathSystemEdgesConnected", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
		JValueList list = result.toCollection().toJValueList();
		assertEquals(crossroadCount * countyCount, list.size());
		int empty = 0;
		for (JValue v : list) {
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
	public void testEdgesConnectedPathSystem2() throws Exception {
		Graph testgraph = getTestGraph(TestVersion.ROUTE_MAP_GRAPH);
		JValueCollection result = evalTestQuery(
				"using dmpmPathSystem: edgesConnected(getVertex(142),dmpmPathSystem)")
				.toCollection();
		// root of PS
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-294))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(294))));
		assertEquals(1, result.size());

		// inside PS
		result = evalTestQuery(
				"using dmpmPathSystem: edgesConnected(getVertex(104),dmpmPathSystem)")
				.toCollection();
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-232))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(232))));

		// check if duplicate edge 233 is not included (it should also not be
		// included in PS)
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-233))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(233))));

		assertTrue(result.contains(JValueImpl
				.fromObject(testgraph.getEdge(234))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-234))));
		assertEquals(3, result.size());

		// leaf of PS
		result = evalTestQuery(
				"using dmpmPathSystem: edgesConnected(getVertex(1),dmpmPathSystem)")
				.toCollection();
		assertTrue(result.contains(JValueImpl
				.fromObject(testgraph.getEdge(135))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-135))));
		assertEquals(1, result.size());

		// not in PS
		result = evalTestQuery(
				"using dmpmPathSystem: edgesConnected(getVertex(64),dmpmPathSystem)")
				.toCollection();
		assertEquals(0, result.size());
	}

	@Test
	public void testEdgesConnectedTypeCollection() throws Exception {
		// VERTEX x TYPECOLLECTION -> COLLECTION
		Graph testgraph = getTestGraph(TestVersion.ROUTE_MAP_GRAPH);
		JValueCollection result;

		// test with all types at one vertex
		result = evalTestQuery(
				"edgesConnected{localities.ContainsCrossroad,connections.Street}(getVertex(120))")
				.toCollection();
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-100))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-245))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-281))));
		assertTrue(result.contains(JValueImpl
				.fromObject(testgraph.getEdge(291))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-319))));
		assertTrue(result.contains(JValueImpl
				.fromObject(testgraph.getEdge(320))));
		assertEquals(6, result.size());

		// test with only one type at one vertex (including subtypes)
		result = evalTestQuery(
				"edgesConnected{connections.Street}(getVertex(120))")
				.toCollection();
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-100))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-245))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-281))));
		assertTrue(result.contains(JValueImpl
				.fromObject(testgraph.getEdge(291))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-319))));
		assertTrue(result.contains(JValueImpl
				.fromObject(testgraph.getEdge(320))));
		assertEquals(5, result.size());

		// test with only one type at one vertex (excluding subtypes)
		result = evalTestQuery(
				"edgesConnected{connections.Street!}(getVertex(120))")
				.toCollection();
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-100))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-245))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-281))));
		assertTrue(result.contains(JValueImpl
				.fromObject(testgraph.getEdge(291))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-319))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(320))));
		assertEquals(3, result.size());

		// test with excluding one type (including subtypes)
		result = evalTestQuery(
				"edgesConnected{^connections.Street}(getVertex(120))")
				.toCollection();
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-100))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-245))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-281))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(291))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-319))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(320))));
		assertEquals(1, result.size());

		// test with excluding one type (excluding subtypes)
		result = evalTestQuery(
				"edgesConnected{^connections.Street!}(getVertex(120))")
				.toCollection();
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-100))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-245))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-281))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(291))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-319))));
		assertTrue(result.contains(JValueImpl
				.fromObject(testgraph.getEdge(320))));
		assertEquals(3, result.size());

		// test with a type not incident to a vertex
		result = evalTestQuery(
				"edgesConnected{connections.Footpath}(getVertex(120))")
				.toCollection();
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-100))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-245))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-281))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(291))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-319))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(320))));
		assertTrue(result.isEmpty());

		// test with empty type collection
		// TODO empty type collection impossible
		// result = evalTestQuery("edgesConnected{}(getVertex(120))")
		// .toCollection();
		// assertFalse(result.contains(JValueImpl.fromObject(testgraph
		// .getEdge(-100))));
		// assertFalse(result.contains(JValueImpl.fromObject(testgraph
		// .getEdge(-245))));
		// assertFalse(result.contains(JValueImpl.fromObject(testgraph
		// .getEdge(-281))));
		// assertFalse(result.contains(JValueImpl.fromObject(testgraph
		// .getEdge(291))));
		// assertFalse(result.contains(JValueImpl.fromObject(testgraph
		// .getEdge(-319))));
		// assertFalse(result.contains(JValueImpl.fromObject(testgraph
		// .getEdge(320))));
		// assertTrue(result.isEmpty());

		// test with vertex "null"
		assertQueryIsUndefined("using nll: edgesConnected{connections.Street}(nll)");

	}

	@Test
	public void testEdgesConnectedNull() throws Exception {
		assertQueryIsUndefined("using nll: edgesConnected(nll, nll)");
		assertQueryIsUndefined("using nll: edgesConnected(firstVertex(), nll)");
		assertQueryIsUndefined("using dmpmPathSystem,nll: edgesConnected(nll, dmpmPathSystem)");
	}

	@Test
	public void testEdgesFromVertexOnly() throws Exception {
		JValueSet result = evalTestQuery(
				"let v20 := getVertex(20) in edgesFrom(v20)").toJValueSet();

		Graph testgraph = getTestGraph(TestVersion.ROUTE_MAP_GRAPH);
		assertFalse(result.contains(JValueImpl
				.fromObject(testgraph.getEdge(-2))));
		assertTrue(result.contains(JValueImpl
				.fromObject(testgraph.getEdge(315))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-316))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-336))));
	}

	@Test
	public void testEdgesFromPath() throws Exception {
		// create a sample path with three vertices to test the method with the
		// first vertex, the last vertex and a vertex in between
		JValuePath samplePath = evalTestQuery(
				"using dmpmPathSystem: extractPath(dmpmPathSystem,getVertex(103))")
				.toPath();

		setBoundVariable("samplePath", samplePath);
		Graph testgraph = getTestGraph(TestVersion.ROUTE_MAP_GRAPH);

		JValueCollection result = evalTestQuery(
				"using samplePath: edgesFrom(getVertex(142),samplePath)")
				.toCollection();
		assertEquals(0, result.size());

		result = evalTestQuery(
				"using samplePath: edgesFrom(getVertex(104),samplePath)")
				.toCollection();

		assertTrue(result.contains(JValueImpl
				.fromObject(testgraph.getEdge(294))));
		// test if incident reversed edge is not in collection
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-232))));

		result = evalTestQuery(
				"using samplePath: edgesFrom(getVertex(103),samplePath)")
				.toCollection();
		assertTrue(result.contains(JValueImpl
				.fromObject(testgraph.getEdge(232))));

		// not in PS
		result = evalTestQuery(
				"using dmpmPathSystem: edgesFrom(getVertex(64),dmpmPathSystem)")
				.toCollection();
		assertEquals(0, result.size());
	}

	@Test
	public void testEdgesFromPathSystem() throws Exception {
		String queryString = "from c: V{localities.County}, a:V{junctions.Airport} "
				+ "with c.name = 'Hessen' "
				+ "report edgesFrom(a, pathSystem(c, -->{localities.ContainsLocality} -->{connections.AirRoute})) "
				+ "end";
		JValue result = evalTestQuery("PathSystemEdgesFrom", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
		JValueList list = result.toCollection().toJValueList();
		assertEquals(airportCount, list.size());
		int empty = 0;
		for (JValue v : list) {
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
	public void testEdgesFromPathSystem2() throws Exception {
		Graph testgraph = getTestGraph(TestVersion.ROUTE_MAP_GRAPH);
		JValueCollection result = evalTestQuery(
				"using dmpmPathSystem: edgesFrom(getVertex(142),dmpmPathSystem)")
				.toCollection();

		// root of PS
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-294))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(294))));
		assertEquals(0, result.size());

		// inside PS
		result = evalTestQuery(
				"using dmpmPathSystem: edgesFrom(getVertex(104),dmpmPathSystem)")
				.toCollection();

		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-232))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(232))));
		assertTrue(result.contains(JValueImpl
				.fromObject(testgraph.getEdge(294))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-294))));

		// check if duplicate edge 233 is not included (it should also not be
		// included in PS)
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-233))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(233))));

		assertTrue(result.contains(JValueImpl
				.fromObject(testgraph.getEdge(234))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-234))));
		assertEquals(2, result.size());

		// leaf of PS
		result = evalTestQuery(
				"using dmpmPathSystem: edgesFrom(getVertex(1),dmpmPathSystem)")
				.toCollection();
		assertTrue(result.contains(JValueImpl
				.fromObject(testgraph.getEdge(135))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-135))));
		assertEquals(1, result.size());

		// not in PS
		result = evalTestQuery(
				"using dmpmPathSystem: edgesFrom(getVertex(64),dmpmPathSystem)")
				.toCollection();
		assertEquals(0, result.size());
	}

	@Test
	public void testEdgesFromTypeCollection() throws Exception {
		// VERTEX x TYPECOLLECTION -> COLLECTION
		Graph testgraph = getTestGraph(TestVersion.ROUTE_MAP_GRAPH);
		JValueCollection result;

		// test with all types at one vertex
		result = evalTestQuery(
				"edgesFrom{localities.ContainsCrossroad,connections.Street}(getVertex(120))")
				.toCollection();
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-100))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-245))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-281))));
		assertTrue(result.contains(JValueImpl
				.fromObject(testgraph.getEdge(291))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-319))));
		assertTrue(result.contains(JValueImpl
				.fromObject(testgraph.getEdge(320))));
		assertEquals(2, result.size());

		// test with only one type at one vertex (including subtypes)
		result = evalTestQuery("edgesFrom{connections.Street}(getVertex(120))")
				.toCollection();
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-100))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-245))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-281))));
		assertTrue(result.contains(JValueImpl
				.fromObject(testgraph.getEdge(291))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-319))));
		assertTrue(result.contains(JValueImpl
				.fromObject(testgraph.getEdge(320))));
		assertEquals(2, result.size());

		// test with only one type at one vertex (excluding subtypes)
		result = evalTestQuery("edgesFrom{connections.Street!}(getVertex(120))")
				.toCollection();
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-100))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-245))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-281))));
		assertTrue(result.contains(JValueImpl
				.fromObject(testgraph.getEdge(291))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-319))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(320))));
		assertEquals(1, result.size());

		// test with excluding one type (including subtypes)
		result = evalTestQuery("edgesFrom{^connections.Street}(getVertex(120))")
				.toCollection();
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-100))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-245))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-281))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(291))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-319))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(320))));
		assertEquals(0, result.size());

		// test with excluding one type (excluding subtypes)
		result = evalTestQuery(
				"edgesFrom{^connections.Street!}(getVertex(120))")
				.toCollection();
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-100))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-245))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-281))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(291))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-319))));
		assertTrue(result.contains(JValueImpl
				.fromObject(testgraph.getEdge(320))));
		assertEquals(1, result.size());

		// test with a type not incident to a vertex
		result = evalTestQuery(
				"edgesFrom{connections.Footpath}(getVertex(120))")
				.toCollection();
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-100))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-245))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-281))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(291))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-319))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(320))));
		assertTrue(result.isEmpty());

		// test with empty type collection
		// TODO empty type collection impossible
		// result = evalTestQuery("edgesConnected{}(getVertex(120))")
		// .toCollection();
		// assertFalse(result.contains(JValueImpl.fromObject(testgraph
		// .getEdge(-100))));
		// assertFalse(result.contains(JValueImpl.fromObject(testgraph
		// .getEdge(-245))));
		// assertFalse(result.contains(JValueImpl.fromObject(testgraph
		// .getEdge(-281))));
		// assertFalse(result.contains(JValueImpl.fromObject(testgraph
		// .getEdge(291))));
		// assertFalse(result.contains(JValueImpl.fromObject(testgraph
		// .getEdge(-319))));
		// assertFalse(result.contains(JValueImpl.fromObject(testgraph
		// .getEdge(320))));
		// assertTrue(result.isEmpty());

		// test with vertex "null"
		assertQueryIsUndefined("using nll: edgesFrom{connections.Street}(nll)");
	}

	@Test
	public void testEdgesFromNull() throws Exception {
		assertQueryIsUndefined("using nll: edgesFrom(nll, nll)");
		assertQueryIsUndefined("using nll: edgesFrom(firstVertex(), nll)");
		assertQueryIsUndefined("using dmpmPathSystem,nll: edgesFrom(nll, dmpmPathSystem)");
	}

	@Test
	public void testEdgesToVertexOnly() throws Exception {
		JValueSet result = evalTestQuery(
				"let v20 := getVertex(20) in edgesTo(v20)").toJValueSet();
		Graph testgraph = getTestGraph(TestVersion.ROUTE_MAP_GRAPH);
		assertTrue(result
				.contains(JValueImpl.fromObject(testgraph.getEdge(-2))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(315))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-316))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-336))));
	}

	@Test
	public void testEdgesToPath() throws Exception {
		// create a sample path with three vertices to test the method with the
		// first vertex, the last vertex and a vertex in between
		JValuePath samplePath = evalTestQuery(
				"using dmpmPathSystem: extractPath(dmpmPathSystem,getVertex(103))")
				.toPath();

		setBoundVariable("samplePath", samplePath);
		Graph testgraph = getTestGraph(TestVersion.ROUTE_MAP_GRAPH);

		JValueCollection result = evalTestQuery(
				"using samplePath: edgesTo(getVertex(142),samplePath)")
				.toCollection();
		assertEquals(1, result.size());
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-294))));

		result = evalTestQuery(
				"using samplePath: edgesTo(getVertex(104),samplePath)")
				.toCollection();
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(294))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-232))));

		result = evalTestQuery(
				"using samplePath: edgesTo(getVertex(103),samplePath)")
				.toCollection();
		assertTrue(result.isEmpty());

		// not in PS
		result = evalTestQuery(
				"using dmpmPathSystem: edgesTo(getVertex(64),dmpmPathSystem)")
				.toCollection();
		assertTrue(result.isEmpty());
	}

	@Test
	public void testEdgesToPathSystem() throws Exception {
		String queryString = "from c: V{localities.County}, r:V{junctions.Crossroad} "
				+ "report edgesTo(r, pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad}))"
				+ "end";
		JValue result = evalTestQuery("PathSystemEdgesTo", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
		JValueList list = result.toCollection().toJValueList();
		assertEquals(crossroadCount * countyCount, list.size());
		int empty = 0;
		for (JValue v : list) {
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
	public void testEdgesToPathSystem2() throws Exception {
		Graph testgraph = getTestGraph(TestVersion.ROUTE_MAP_GRAPH);
		JValueCollection result = evalTestQuery(
				"using dmpmPathSystem: edgesTo(getVertex(142),dmpmPathSystem)")
				.toCollection();
		// root of PS
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-294))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(294))));
		assertEquals(1, result.size());

		// inside PS
		result = evalTestQuery(
				"using dmpmPathSystem: edgesTo(getVertex(104),dmpmPathSystem)")
				.toCollection();
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-232))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(232))));

		// check if duplicate edge 233 is not included (it should also not be
		// included in PS)
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-233))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(233))));

		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(234))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-234))));
		assertEquals(1, result.size());

		// leaf of PS
		result = evalTestQuery(
				"using dmpmPathSystem: edgesTo(getVertex(1),dmpmPathSystem)")
				.toCollection();
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(135))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-135))));
		assertEquals(0, result.size());

		// not in PS
		result = evalTestQuery(
				"using dmpmPathSystem: edgesTo(getVertex(64),dmpmPathSystem)")
				.toCollection();
		assertEquals(0, result.size());
	}

	@Test
	public void testEdgesToTypeCollection() throws Exception {
		// VERTEX x TYPECOLLECTION -> COLLECTION
		Graph testgraph = getTestGraph(TestVersion.ROUTE_MAP_GRAPH);
		JValueCollection result;

		// test with all types at one vertex
		result = evalTestQuery(
				"edgesTo{localities.ContainsCrossroad,connections.Street}(getVertex(120))")
				.toCollection();
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-100))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-245))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-281))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(291))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-319))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(320))));
		assertEquals(4, result.size());

		// test with only one type at one vertex (including subtypes)
		result = evalTestQuery("edgesTo{connections.Street}(getVertex(120))")
				.toCollection();
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-100))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-245))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-281))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(291))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-319))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(320))));
		assertEquals(3, result.size());

		// test with only one type at one vertex (excluding subtypes)
		result = evalTestQuery("edgesTo{connections.Street!}(getVertex(120))")
				.toCollection();
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-100))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-245))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-281))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(291))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-319))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(320))));
		assertEquals(2, result.size());

		// test with excluding one type (including subtypes)
		result = evalTestQuery("edgesTo{^connections.Street}(getVertex(120))")
				.toCollection();
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-100))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-245))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-281))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(291))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-319))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(320))));
		assertEquals(1, result.size());

		// test with excluding one type (excluding subtypes)
		result = evalTestQuery("edgesTo{^connections.Street!}(getVertex(120))")
				.toCollection();
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-100))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-245))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-281))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(291))));
		assertTrue(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-319))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(320))));
		assertEquals(2, result.size());

		// test with a type not incident to a vertex
		result = evalTestQuery("edgesTo{connections.Footpath}(getVertex(120))")
				.toCollection();
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-100))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-245))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-281))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(291))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(-319))));
		assertFalse(result.contains(JValueImpl.fromObject(testgraph
				.getEdge(320))));
		assertTrue(result.isEmpty());

		// test with empty type collection
		// TODO empty type collection impossible
		// result = evalTestQuery("edgesTo{}(getVertex(120))")
		// .toCollection();
		// assertFalse(result.contains(JValueImpl.fromObject(testgraph
		// .getEdge(-100))));
		// assertFalse(result.contains(JValueImpl.fromObject(testgraph
		// .getEdge(-245))));
		// assertFalse(result.contains(JValueImpl.fromObject(testgraph
		// .getEdge(-281))));
		// assertFalse(result.contains(JValueImpl.fromObject(testgraph
		// .getEdge(291))));
		// assertFalse(result.contains(JValueImpl.fromObject(testgraph
		// .getEdge(-319))));
		// assertFalse(result.contains(JValueImpl.fromObject(testgraph
		// .getEdge(320))));
		// assertTrue(result.isEmpty());

		// test with vertex "null"
		assertQueryIsUndefined("using nll: edgesTo{connections.Street}(nll)");
	}

	@Test
	public void testEdgesToNull() throws Exception {
		// TODO A meaningful Pathsystem is missing
		assertQueryIsUndefined("using nll: edgesTo(nll, nll)");
		assertQueryIsUndefined("using nll: edgesTo(firstVertex(), nll)");
		assertQueryIsUndefined("using dmpmPathSystem,nll: edgesTo(nll, dmpmPathSystem)");
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

	@Test
	public void testEdgesTraceNull() throws Exception {
		assertQueryIsUndefined("using nll: edgeTrace(nll)");
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
		// PATH -> COLLECTION

		// test a normal path
		JValueCollection result = evalTestQuery(
				"elements(extractPath(pathSystem(getVertex(144), -->{localities.ContainsLocality} -->{connections.AirRoute}* ),getVertex(157)))")
				.toCollection();

		Set<Integer> vertexIds = new HashSet<Integer>();
		vertexIds.add(144);
		vertexIds.add(155);
		vertexIds.add(157);
		Set<Integer> edgeIds = new HashSet<Integer>();
		edgeIds.add(354);
		edgeIds.add(297);
		testCollectionForGraphElements(result, vertexIds, edgeIds);

		// test path including reversed edges
		result = evalTestQuery(
				"using dmpmPathSystem: elements(extractPath(dmpmPathSystem,getVertex(72)))")
				.toCollection();
		vertexIds.clear();
		vertexIds.add(142);
		vertexIds.add(104);
		vertexIds.add(72);
		edgeIds.clear();
		edgeIds.add(-294);
		edgeIds.add(234);
		testCollectionForGraphElements(result, vertexIds, edgeIds);

		// test empty path
		result = evalTestQuery("using emptyPath: elements(emptyPath)")
				.toCollection();
		// System.out.println(emptyPath);
		// System.out.println(result);

		testCollectionForGraphElements(result, null, null);
	}

	@Test
	public void testElementsPathSystem() throws Exception {
		// PATHSYSTEM -> COLLECTION

		// test normal path system
		JValueCollection result = evalTestQuery(
				"using d2p2PathSystem: elements(d2p2PathSystem)")
				.toCollection();

		Set<Integer> vertexIds = new HashSet<Integer>();
		vertexIds.add(144);
		vertexIds.add(154);
		vertexIds.add(16);
		vertexIds.add(155);
		vertexIds.add(17);
		Set<Integer> edgeIds = new HashSet<Integer>();
		edgeIds.add(350);
		edgeIds.add(127);
		edgeIds.add(354);
		edgeIds.add(126);

		testCollectionForGraphElements(result, vertexIds, edgeIds);

		// test path system containing reversed edges
		result = evalTestQuery("elements(pathSystem(getVertex(2),<->))")
				.toCollection();
		vertexIds.clear();
		vertexIds.add(2);
		vertexIds.add(19);
		vertexIds.add(21);
		edgeIds.clear();
		edgeIds.add(-325);
		edgeIds.add(326);

		testCollectionForGraphElements(result, vertexIds, edgeIds);

		// test empty path system (only root)
		result = evalTestQuery("elements(pathSystem(getVertex(1),<--))")
				.toCollection();

		vertexIds.clear();
		vertexIds.add(1);
		testCollectionForGraphElements(result, vertexIds, null);

	}

	private void testCollectionForGraphElements(JValueCollection collection,
			Set<Integer> vertexIds, Set<Integer> edgeIds) throws Exception {
		Graph testgraph = getTestGraph(TestVersion.ROUTE_MAP_GRAPH);
		for (Vertex v : testgraph.vertices()) {
			boolean collectionContainsVertex = collection.contains(JValueImpl
					.fromObject(v));
			if (vertexIds != null && vertexIds.contains(v.getId())) {
				assertTrue(v + " should be in collection, but is not.",
						collectionContainsVertex);
			} else {
				assertFalse(v + " is in collection, but should not be.",
						collectionContainsVertex);
			}
		}
		for (Edge e : testgraph.edges()) {
			// check normal edge
			boolean collectionContainsEdge = collection.contains(JValueImpl
					.fromObject(e));
			Edge re = e.getReversedEdge();
			boolean collectionContainsReversedEdge = collection
					.contains(JValueImpl.fromObject(re));

			if (edgeIds != null && edgeIds.contains(e.getId())) {
				assertTrue(e + "should be in collection, but is not.",
						collectionContainsEdge);
			} else {
				assertFalse(e + " is in collection, but should not be.",
						collectionContainsEdge);
			}

			// check reversed edge
			if (edgeIds != null && edgeIds.contains(re.getId())) {
				assertTrue(re + "should be in collection, but is not.",
						collectionContainsReversedEdge);
			} else {
				assertFalse(re + " is in collection, but should not be.",
						collectionContainsReversedEdge);
			}
		}

		assertEquals((vertexIds != null ? vertexIds.size() : 0)
				+ (edgeIds != null ? edgeIds.size() : 0), collection.size());

	}

	@Test
	public void testElementsNull() throws Exception {
		assertQueryIsUndefined("using nll: elements(nll)");
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
		JValueList list = result.toCollection().toJValueList();
		assertEquals(countyCount * crossroadCount, list.size());
		int invalidPaths = 0;
		for (JValue v : list) {
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
		assertQueryIsUndefined("using nll: extractPath(nll)");
		assertQueryIsUndefined("using nll: extractPath(nll, nll)");
		// assertQueryEqualsNull("using nll: extractPath(?, nll)");
		assertQueryIsUndefined("using nll: extractPath(nll, 1)");
		assertQueryIsUndefined("using nll: extractPath(nll, firstVertex())");
	}

	@Test
	public void testInnerNodesPathSystem() throws Exception {

		String queryString = "from c: V{localities.County} with c.name <> 'Berlin'"
				+ "report innerNodes(pathSystem(c, -->{localities.ContainsLocality} -->{connections.AirRoute})) "
				+ "end";
		JValue result = evalTestQuery("InnerNodes", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
		JValueList list = result.toCollection().toJValueList();
		assertEquals(countyCount - 1, list.size());
		for (JValue v : list) {
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
		assertQueryIsUndefined("using nll: innerNodes(nll)");
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
		assertQueryIsUndefined("using nll: isCycle(nll)");
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
		assertQueryIsUndefined("using nll: isReachable(nll, nll, nll)");
		assertQueryIsUndefined("using nll: isReachable(firstVertex(), nll, nll)");
		assertQueryIsUndefined("using nll: isReachable(nll, lastVertex(), nll)");
		assertQueryIsUndefined("using nll: isReachable(firstVertex(), lastVertex(), nll)");

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
		JValueList list = result.toCollection().toJValueList();
		assertEquals(countyCount * airportCount * crossroadCount, list.size());
		int trueCounts = 0;
		for (JValue v : list) {
			if (v.toBoolean()) {
				trueCounts++;
			}
		}
		assertEquals(airportCount, trueCounts);
	}

	@Test
	public void testIsSubPathOfNull() throws Exception {
		// TODO A meaningful Pathsystem is missing

		assertQueryIsUndefined("using nll: isSubPathOf(nll, nll)");
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
		JValueList list = result.toCollection().toJValueList();
		assertEquals(countyCount - 1, list.size());
		for (JValue v : list) {
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
		assertQueryIsUndefined("using nll: leaves(nll)");
	}

	@Test
	public void testMatches() throws Exception {
		// TODO A meaningful Automaton is missing
		fail();
	}

	@Test
	public void testMatchesNull() throws Exception {
		// TODO A meaningful Automaton is missing
		assertQueryIsUndefined("using nll: matches(nll, nll)");
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
		JValueList list = result.toCollection().toJValueList();
		assertEquals(countyCount, list.size());
		for (JValue v : list) {
			assertEquals(3, (int) v.toInteger());
		}
	}

	@Test
	public void testMaxPathLengthNull() throws Exception {
		assertQueryIsUndefined("using nll: maxPathLength(nll)");
	}

	@Test
	public void testMinPathLength() throws Exception {
		// TODO This test fails! The course should be investigated
		String queryString = "from c: V{localities.County} "
				+ "report minPathLength(pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad} -->{connections.Street})) "
				+ "end";
		JValue result = evalTestQuery("MinPathLength", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
		JValueList list = result.toCollection().toJValueList();
		assertEquals(countyCount, list.size());
		for (JValue v : list) {
			assertEquals(3, (int) v.toInteger());
		}
	}

	@Test
	public void testMinPathLengthNull() throws Exception {
		assertQueryIsUndefined("using nll: minPathLength(nll)");
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
		assertQueryIsUndefined("using nll: nodes(nll)");
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
		JValueList list = result.toCollection().toJValueList();
		assertEquals(countyCount * crossroadCount, list.size());
		int emptyTraces = 0;
		for (JValue v : list) {
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
		assertQueryIsUndefined("using nll: nodeTrace(nll)");
	}

	@Test
	public void testParent() throws Exception {
		// TODO This test fails! The course should be investigated.
		String queryString = "from c: V{localities.County}, r:V{junctions.Crossroad} "
				+ "report parent(pathSystem(c, -->{localities.ContainsLocality} -->{localities.ContainsCrossroad}), r) "
				+ "end";
		JValue result = evalTestQuery("Parent", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
		JValueList list = result.toCollection().toJValueList();
		assertEquals(countyCount * crossroadCount, list.size());
		int invalid = 0;
		for (JValue v : list) {
			if (!v.isValid()) {
				invalid++;
			}
		}
		assertEquals(crossroadCount + uncontainedCrossroadCount, invalid);
	}

	@Test
	public void testParentNull() throws Exception {
		// TODO A meaningful Pathsystem is missing
		assertQueryIsUndefined("using nll: parent(nll, nll)");
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
		assertQueryIsUndefined("using nll: pathConcat(nll, nll)");
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
		assertQueryIsUndefined("using nll: pathExpr(nll)");
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
		assertQueryIsUndefined("using nll: pathLength(nll)");
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
		assertQueryIsUndefined("using nll: reachableVertices(nll, nll)");
		// assertQueryEqualsNull("using nll: reachableVertices(nll, ?)");
		assertQueryIsUndefined("using nll: reachableVertices(firstVertex(), nll)");

		assertQueryIsUndefined("using nll: reachableVertices(nll, nll, nll)");
		// assertQueryEqualsNull("using nll: reachableVertices(nll, ?, nll)");
		assertQueryIsUndefined("using nll: reachableVertices(firstVertex(), nll, nll)");
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
		JValueList list = result.toCollection().toJValueList();

		assertEquals(countyCount * crossroadCount, list.size());

		int noSiblingsFound = 0;
		int expectedValue = crossroadCount - 2 * uncontainedCrossroadCount;

		for (JValue v : list) {
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
		assertQueryIsUndefined("using nll: siblings(nll, nll)");
		// assertQueryEqualsNull("using nll: siblings(nll, ?)");
		assertQueryIsUndefined("using nll: siblings(firstVertex(), nll)");
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
		assertQueryIsUndefined("using nll: weight(nll)");
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
		JValueList list = result.toCollection().toJValueList();
		assertEquals(1, list.size());
		for (JValue v : list) {
			assertEquals(4, (int) v.toInteger());
		}
	}
}
