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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBag;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePath;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;

public class PathSystemTest extends GenericTests {

	/**
	 * • v :-) -->α baut ein Pfadsystem über Pfade, die dem regulären Ausdruck α
	 * entsprechen, mit dem Wurzelknoten v auf. • v :-) -->α :-) w liefert einen
	 * Pfad der Gestalt α von v nach w. • -->α :-) w liefert ein Pfadsystem mit
	 * Pfaden der Gestalt αT mit dem Wurzelknoten w. • v :-) ( -->α :-) w )
	 * liefert dementsprechend einen Pfad der Gestalt αT von w nach v.
	 */

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateForwardVertexSet(ForwardVertexSet,
	 * Graph)'
	 */
	@Test
	public void testPathSystemConstruction() throws Exception {
		String queryString = "from c: V{localities.County} "
				+ "with c.name = 'Rheinland-Pfalz' "
				+ "report pathSystem(c, -->{localities.ContainsLocality} -->{connections.AirRoute}) "
				+ "end";
		JValue result = evalTestQuery("PathSystemConstruction", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		System.out.println(bag);
		assertEquals(1, bag.size());
		for (JValue v : bag) {
			JValuePathSystem sys = v.toPathSystem();
			assertEquals(2, sys.depth());
			assertEquals(3, sys.weight());
		}
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateForwardVertexSet(ForwardVertexSet,
	 * Graph)'
	 */
	@Test
	public void testPathSystemWeight() throws Exception {
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
	public void testPathSystemContains() throws Exception {
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

	@Test
	public void testPathSystemDistance() throws Exception {
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
	public void testPathSystemEdgesConnected() throws Exception {
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
	public void testPathSystemEdgesFrom() throws Exception {
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
	public void testPathSystemEdgesTo() throws Exception {
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
	public void testSiblings() throws Exception {
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
	public void testIsCycle() throws Exception {
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
	public void testNodeTrace() throws Exception {
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
	public void testIsSubPath() throws Exception {
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
}
