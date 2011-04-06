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

package de.uni_koblenz.jgralabtest.greql2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBag;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTable;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTuple;
import de.uni_koblenz.jgralab.greql2.parser.GreqlParser;
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
		JValue result = evalTestQuery("FunctionArgumentsEvaluation",
				queryString);
		assertEquals(11, result.toCollection().size());
	}

	@Test
	public void testFunctionAsFunctionArgumentEvaluation() throws Exception {
		String queryString = "from x:V{FunctionApplication}, y:V{FunctionApplication} with x <--{IsArgumentOf} y report tup( tup(\"Function: \", x ), tup(\"Argument: \", y )) end";
		JValue result = evalTestQuery("FunctionAsFunctionArgumentEvaluation",
				queryString);
		assertEquals(5, result.toCollection().size());
	}

	@Test
	public void testFunctionAsFunctionArgumentAsFuntionArgumentEvaluation()
			throws Exception {
		String queryString = "from x:V{FunctionApplication}, y:V{FunctionApplication} with x <--{IsArgumentOf} <--{IsArgumentOf} y report tup( tup(\"Function: \", x ), tup(\"Argument: \", y )) end";
		JValue result = evalTestQuery(
				"FunctionAsFunctionAsFunctionArgumentEvaluation", queryString);
		assertEquals(4, result.toCollection().size());
	}

	@Test
	public void testFunctionAsArgumentInBagComprehensionEvaluation()
			throws Exception {
		String queryString = "from x:V{FunctionApplication}, y:V{BagComprehension} with x -->{IsArgumentOf}+ -->{IsCompResultDefOf} y report tup(tup(\"Function: \", x ), tup(\"BagComprehension: \", y )) end";
		JValue result = evalTestQuery("FunctionAsArgumentInBagComprehension",
				queryString);
		assertEquals(5, result.toCollection().size());
	}

	@Test
	public void testVariableAsVariableDefinition() throws Exception {

		String X1 = "X1";
		String X2 = "X2";

		String queryString = "from x:V{junctions.Street}, y:V{junctions.Street} "
				+ "with x --> <-- y report id(x) as '"
				+ X1
				+ "', id(y) as '"
				+ X2 + "' end";
		JValueTable result = evalTestQuery(queryString).toJValueTable();
		checkHeader(result, X1, X2);

		for (JValue value : result.getData()) {
			JValueTuple tuple = value.toJValueTuple();

			Vertex x1 = tuple.get(0).toVertex();
			Vertex x2 = tuple.get(1).toVertex();
		}

	}

	private void checkHeader(JValueTable table, String... headerStrings) {
		JValueList header = table.getHeader().toJValueList();

		for (String headerString : headerStrings) {
			assertTrue(header.remove(new JValueImpl(headerString)));
		}
		assertTrue(header.isEmpty());
	}

	@Test
	public void testIdentifierWithUsage() throws Exception {
		String VERTEX = "Vertex";
		String IDENTIFIER = "Identifier";
		String USAGE_COUNT = "UsageCount";
		String USAGES = "Usages";

		String queryString = "from c:V{junctions.Crossroad} report c as '"
				+ VERTEX + "', id(c) as '" + IDENTIFIER + "', "
				+ "outDegree{connections.Way}(c) as '" + USAGE_COUNT + "', "
				+ "edgesFrom(c) as '" + USAGES + "' end";
		JValueTable result = evalTestQuery(queryString).toJValueTable();
		JValueBag data = result.getData().toJValueBag();

		checkHeader(result, VERTEX, IDENTIFIER, USAGE_COUNT, USAGES);

		for (JValue value : data) {
			JValueTuple tuple = value.toJValueTuple();
			Vertex vertex = tuple.get(0).toVertex();
			int identifier = tuple.get(1).toInteger().intValue();
			int usage_count = tuple.get(2).toInteger().intValue();
			JValueCollection usages = tuple.get(3).toCollection();

			assertEquals(vertex.getId(), identifier);
			assertEquals(vertex.getDegree(EdgeDirection.OUT), usage_count);

			for (Edge edge : vertex.incidences(Way.class, EdgeDirection.OUT)) {
				assertTrue(usages.remove(new JValueImpl(edge)));
			}
			assertTrue(usages.isEmpty());
		}
	}
}
