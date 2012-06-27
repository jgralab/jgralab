/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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

package de.uni_koblenz.jgralabtest.greql2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.pcollections.PCollection;
import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.Query;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.greql.parser.GreqlParser;
import de.uni_koblenz.jgralab.greql.types.Table;
import de.uni_koblenz.jgralab.greql.types.Tuple;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.connections.Way;

public class SystemTest extends GenericTest {

	@Override
	protected Graph createGreqlTestGraph() {
		int count = 1;
		String part1 = "from i:a report ";
		String part2 = " end where q:=a, l:=a, m:=a, n:=a, o:=a, p:=a, k:= a, j:=a, h := a, g := a, f:= a, e := a, d:=a, c:=a, b:=a, a:=4";
		StringBuilder queryString = new StringBuilder();
		for (int i = 0; i < count; i++) { // 65
			queryString.append(part1);
		}
		queryString
				.append(" add(i, mul( a, sub(b, mod( c, sub( d, add( e)))))) ");
		for (int i = 0; i < count; i++) {
			queryString.append(part2);
		}
		return GreqlParser.parse(queryString.toString());
	}

	@Test
	public void testFunctionArgumentsEvaluation() throws Exception {
		String queryString = "from x:V{FunctionApplication}, y:V{Expression} with x <--{IsArgumentOf} y report tup( tup(\"Function: \", x ), tup(\"Argument: \", y )) end";
		Object result = evalTestQuery("FunctionArgumentsEvaluation",
				queryString);
		assertEquals(11, ((PCollection<?>) result).size());
	}

	@Test
	public void testFunctionAsFunctionArgumentEvaluation() throws Exception {
		String queryString = "from x:V{FunctionApplication}, y:V{FunctionApplication} with x <--{IsArgumentOf} y report tup( tup(\"Function: \", x ), tup(\"Argument: \", y )) end";
		Object result = evalTestQuery("FunctionAsFunctionArgumentEvaluation",
				queryString);
		assertEquals(5, ((PCollection<?>) result).size());
	}

	@Test
	public void testFunctionAsFunctionArgumentAsFuntionArgumentEvaluation()
			throws Exception {
		String queryString = "from x:V{FunctionApplication}, y:V{FunctionApplication} with x <--{IsArgumentOf} <--{IsArgumentOf} y report tup( tup(\"Function: \", x ), tup(\"Argument: \", y )) end";
		Object result = evalTestQuery(
				"FunctionAsFunctionAsFunctionArgumentEvaluation", queryString);
		assertEquals(4, ((PCollection<?>) result).size());
	}

	@Test
	public void testFunctionAsArgumentInListComprehensionEvaluation()
			throws Exception {
		String queryString = "from x:V{FunctionApplication}, y:V{ListComprehension} with x -->{IsArgumentOf}+ -->{IsCompResultDefOf} y report tup(tup(\"Function: \", x ), tup(\"ListComprehension: \", y )) end";
		Object result = evalTestQuery("FunctionAsArgumentInListComprehension",
				queryString);
		assertEquals(5, ((PCollection<?>) result).size());
	}

	@Test
	public void test() throws Exception {
		String X1 = "X1";
		String X2 = "X2";

		String queryString = "from x:V{junctions.Crossroad}, y:V{junctions.Crossroad} "
				+ "with x -->{connections.Street} <--{connections.Footpath} y report id(x) as '"
				+ X1 + "', id(y) as '" + X2 + "' end";
		Table<?> result = ((Table<?>) evalTestQuery(queryString));
		checkHeader(result, X1, X2);

		assertEquals(12, result.size());
	}

	private void checkHeader(Table<?> table, String... headerStrings) {
		PVector<?> header = table.getTitles();
		assertTrue(header.size() == headerStrings.length);
		for (String headerString : headerStrings) {
			assertTrue(header.contains(headerString));
		}
	}

	@Test
	public void testCrossroadWithUsage() throws Exception {
		String VERTEX = "Vertex";
		String IDENTIFIER = "Identifier";
		String USAGE_COUNT = "UsageCount";
		String USAGES = "Usages";

		String queryString = "from c:V{junctions.Crossroad} report c as '"
				+ VERTEX + "', id(c) as '" + IDENTIFIER + "', "
				+ "outDegree{connections.Way}(c) as '" + USAGE_COUNT + "', "
				+ "edgesFrom(c) as '" + USAGES + "' end";
		Table<?> result = (Table<?>) evalTestQuery(queryString);
		@SuppressWarnings("unchecked")
		PVector<Tuple> data = (PVector<Tuple>) result.toPVector();

		checkHeader(result, VERTEX, IDENTIFIER, USAGE_COUNT, USAGES);

		for (Tuple tuple : data) {
			Vertex vertex = (Vertex) tuple.get(0);
			int identifier = ((Integer) tuple.get(1)).intValue();
			int usage_count = ((Integer) tuple.get(2)).intValue();
			PCollection<?> usages = (PCollection<?>) tuple.get(3);

			assertEquals(vertex.getId(), identifier);
			assertEquals(vertex.getDegree(Way.EC, EdgeDirection.OUT),
					usage_count);

			int n = 0;
			for (Edge edge : vertex.incidences(Way.EC, EdgeDirection.OUT)) {
				++n;
				assertTrue(usages.contains(edge));
			}
			assertEquals(n, usages.size());
		}

		assertEquals(crossroadCount, result.size());
	}

	// @Test
	public void testSimpleQuery() {
		Map<String, Object> boundVars = new HashMap<String, Object>();
		PSet<Integer> x = JGraLab.set();
		for (int i = 1; i < 2000; i++) {
			x = x.plus(i);
		}
		PSet<Integer> y = JGraLab.set();
		for (int i = 1; i < 3000; i++) {
			y = y.plus(i);
		}
		boundVars.put("X", x);
		boundVars.put("Y", y);
		// String query = "using X,Y: from x:X, y:Y reportList x*y end";
		// String query =
		// "using X,Y: from x:X, y:Y with (y % 2 <> 1) and (x % 3 = 0) reportList x*y end";
		String query = // "using X,Y: forall x:X, y:Y @ x*y > 0";
		"using X,Y: from x:X, y:Y reportMap y->x end";
		long startTime = System.currentTimeMillis();
		Query.createQuery(query).evaluate(null,
				new GreqlEnvironmentAdapter(boundVars));
		long usedTime = System.currentTimeMillis() - startTime;
		System.out.println("Evaluation of interpreted query took " + usedTime
				+ "msec");
	}

}
