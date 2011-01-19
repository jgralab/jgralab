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

package de.uni_koblenz.jgralabtest.greql2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBag;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePath;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;

public class PathSystemTest extends GenericTests {

	/**
	 * • v :-) -->α baut ein Pfadsystem über Pfade, die dem regulären Ausdruck α
	 * entsprechen, mit dem Wurzelknoten v auf. • v :-) -->α :-) w liefert einen
	 * Pfad der Gestalt α von v nach w. • -->α :-) w liefert ein Pfadsystem mit
	 * Pfaden der Gestalt αT mit dem Wurzelknoten w. • v :-) ( -->α :-) w )
	 * liefert dementsprechend einen Pfad der Gestalt αT von w nach v.
	 */

	private static int airportCount, crossroadCount, countyCount,
			uncontainedCrossroadCount;

	@BeforeClass
	public static void globalSetUp() throws Exception {
		GenericTests test = new GenericTests();
		queryAirportCount(test);
		queryCrossroadCount(test);
		queryCountyCount(test);
		queryUncontainedCrossroadCount(test);
	}

	private static void queryAirportCount(GenericTests test) throws Exception {
		String queryString = "count(V{junctions.Airport})";
		JValue result = test.evalTestQuery("static Query", queryString,
				TestVersion.CITY_MAP_GRAPH);
		airportCount = result.toInteger();
	}

	private static void queryCrossroadCount(GenericTests test) throws Exception {
		String queryString = "count(V{junctions.Crossroad})";
		JValue result = test.evalTestQuery("static Query", queryString,
				TestVersion.CITY_MAP_GRAPH);
		crossroadCount = result.toInteger();
	}

	private static void queryCountyCount(GenericTests test) throws Exception {
		String queryString = "count(V{localities.County})";
		JValue result = test.evalTestQuery("static Query", queryString,
				TestVersion.CITY_MAP_GRAPH);
		countyCount = result.toInteger();
	}

	private static void queryUncontainedCrossroadCount(GenericTests test)
			throws Exception {
		String queryString = "sum(from r:V{junctions.Crossroad} report depth(pathSystem(r, <--{localities.ContainsCrossroad})) end)";
		JValue result = test.evalTestQuery("static Query", queryString,
				TestVersion.CITY_MAP_GRAPH);

		String queryString2 = "from r:V{junctions.Crossroad} report depth(pathSystem(r, <--{localities.ContainsCrossroad})) end";
		JValue result2 = test.evalTestQuery("static Query", queryString2,
				TestVersion.CITY_MAP_GRAPH);
		System.out.println(result2);
		uncontainedCrossroadCount = crossroadCount
				- result.toDouble().intValue();
	}

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
		JValue result = evalTestQuery("PathSystemConstruction", queryString,
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
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from v: V{WhereExpression}, w:V{Variable}  report edgesConnected(w, v  :-) <--{IsDefinitionOf} <--{IsVarOf}) end";
		JValue result = evalTestQuery("PathSystemConstruction", queryString);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(5, bag.size());
		int empty = 0;
		for (JValue v : bag) {
			if (v.toCollection().size() == 0) {
				empty++;
			} else {
				assertEquals(1, v.toCollection().size());
			}
		}
		assertEquals(1, empty);
	}

	@Test
	public void testPathSystemEdgesFrom() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from v: V{WhereExpression}, w:V{Variable}  report edgesFrom(w, v  :-) <--{IsDefinitionOf} <--{IsVarOf}) end";
		JValue result = evalTestQuery("PathSystemEdgesFrom", queryString);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(5, bag.size());
		int empty = 0;
		for (JValue v : bag) {
			if (v.toCollection().size() == 0) {
				empty++;
			} else {
				assertEquals(1, v.toCollection().size());
			}
		}
		assertEquals(1, empty);
	}

	@Test
	public void testPathSystemEdgesTo() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from v: V{WhereExpression}, w:V{Variable}  report edgesTo(v, v  :-) <--{IsDefinitionOf} <--{IsVarOf}) end";
		JValue result = evalTestQuery("PathSystemEdgesTo", queryString);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(5, bag.size());
		for (JValue v : bag) {
			assertEquals(4, v.toCollection().size());
		}
	}

	@Test
	public void testExtractPath() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from v: V{WhereExpression}, w:V{Variable}  report extractPath(v  :-) <--{IsDefinitionOf} <--{IsVarOf}, w) end";
		JValue result = evalTestQuery("ExtractPath", queryString);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(5, bag.size());
		int invalidPaths = 0;
		for (JValue v : bag) {
			JValuePath p = v.toPath();
			if (!p.isValidPath()) {
				invalidPaths++;
			} else {
				assertEquals(2, p.pathLength());
			}
		}
		assertEquals(1, invalidPaths);
	}

	@Test
	public void testInnerNodes() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from v: V{WhereExpression}, w:V{Variable}  report innerNodes(v  :-) <--{IsDefinitionOf} <--{IsVarOf}) end";
		JValue result = evalTestQuery("InnerNodes", queryString);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(5, bag.size());
		for (JValue v : bag) {
			assertEquals(4, v.toCollection().size());
		}
	}

	@Test
	public void testLeaves() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from v: V{WhereExpression}, w:V{Variable}  report leaves(v  :-) <--{IsDefinitionOf} <--{IsVarOf}) end";
		JValue result = evalTestQuery("Leaves", queryString);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(5, bag.size());
		for (JValue v : bag) {
			assertEquals(4, v.toCollection().size());
		}
	}

	@Test
	public void testMinPathLength() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from v: V{WhereExpression}, w:V{Variable}  report minPathLength( v  :-) <--{IsDefinitionOf} <--{IsVarOf}) end";
		JValue result = evalTestQuery("MinPathLength", queryString);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(5, bag.size());
		for (JValue v : bag) {
			assertEquals(2, (int) v.toInteger());
		}
	}

	@Test
	public void testMaxPathLength() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from v: V{WhereExpression}, w:V{Variable}  report maxPathLength( v  :-) <--{IsDefinitionOf} <--{IsVarOf}) end";
		JValue result = evalTestQuery("MaxPathLength", queryString);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(5, bag.size());
		for (JValue v : bag) {
			assertEquals(2, (int) v.toInteger());
		}
	}

	@Test
	public void testParent() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from v: V{WhereExpression}, w:V{Variable}  report parent( v  :-) <--{IsDefinitionOf} <--{IsVarOf}, w) end";
		JValue result = evalTestQuery("Parent", queryString);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(5, bag.size());
		int invalid = 0;
		for (JValue v : bag) {
			if (!v.isValid()) {
				invalid++;
			}
		}
		assertEquals(1, invalid);
	}

	@Test
	public void testSiblings() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from v: V{WhereExpression}, w:V{Definition}  report siblings(w,  v  :-) <--{IsDefinitionOf} <--{IsVarOf}) end";
		JValue result = evalTestQuery("Siblings", queryString);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(4, bag.size());
		for (JValue v : bag) {
			assertEquals(3, v.toCollection().size());
		}
	}

	@Test
	public void testTypes() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from v: V{WhereExpression} report types(v  :-) <--{IsDefinitionOf} <--{IsVarOf}) end";
		JValue result = evalTestQuery("TypeSet", queryString);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(1, bag.size());
		for (JValue v : bag) {
			assertEquals(5, v.toCollection().size());
		}
	}

	@Test
	public void testPathLength() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from v: V{WhereExpression}, w: V{Variable} report pathLength(extractPath(v  :-) <--{IsDefinitionOf} <--{IsVarOf}, w)) end";
		JValue result = evalTestQuery("PathLength", queryString);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(5, bag.size());
		int nullPath = 0;
		for (JValue v : bag) {
			if (v.toInteger() == 0) {
				nullPath++;
			} else {
				assertEquals(2, (int) v.toInteger());
			}
		}
		assertEquals(1, nullPath);
	}

	@Test
	public void testIsCycle() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from v: V{WhereExpression}, w: V{Variable} report isCycle(extractPath(v  :-) <--{IsDefinitionOf} <--{IsVarOf}, w)) end";
		JValue result = evalTestQuery("PathLength", queryString);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(5, bag.size());
		for (JValue v : bag) {
			assertFalse(v.toBoolean());
		}
	}

	@Test
	public void testNodeTrace() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from v: V{WhereExpression}, w: V{Variable} report nodeTrace(extractPath(v  :-) <--{IsDefinitionOf} <--{IsVarOf}, w)) end";
		JValue result = evalTestQuery("PathLength", queryString);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(5, bag.size());
		int emptyTraces = 0;
		for (JValue v : bag) {
			if (v.toCollection().size() == 0) {
				emptyTraces++;
			} else {
				assertEquals(3, v.toCollection().size());
			}
		}
		assertEquals(1, emptyTraces);
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
