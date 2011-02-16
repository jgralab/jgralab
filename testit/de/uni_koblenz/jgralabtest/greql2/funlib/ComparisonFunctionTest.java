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

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;

public class ComparisonFunctionTest extends GenericTests {

	@Test
	public void testEqualsInfix() throws Exception {
		assertQueryEquals("5 = 9", false);
		assertQueryEquals("'' = 'a'", false);
		assertQueryEquals("'a' = ''", false);
		assertQueryEquals("'' = ''", true);
		assertQueryEquals("'a' = 'a'", true);
		assertQueryEquals("99.001 = 99.001", true);
		assertQueryEquals("'Eckhard Großmann' = 'Eckhard Grossmann'", false);
		assertQueryEquals("'Eckhard Großmann' = 'Eckhard Großmann'", true);
	}

	@Test
	public void testEquals() throws Exception {
		assertQueryEquals("equals(5, 9)", false);
		assertQueryEquals("equals('', 'a')", false);
		assertQueryEquals("equals('a', '')", false);
		assertQueryEquals("equals('', '')", true);
		assertQueryEquals("equals('a', 'a')", true);
		assertQueryEquals("equals(99.001, 99.001)", true);
		assertQueryEquals("equals('Eckhard Großmann', 'Eckhard Grossmann')",
				false);
		assertQueryEquals("equals('Eckhard Großmann', 'Eckhard Großmann')",
				true);
	}

	@Test
	public void testEquals7() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{WhereExpression}, y : V{WhereExpression} report equals(x,y) end";
		assertQueryEquals(queryString, true);
	}

	@Test
	public void testEquals8() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{WhereExpression}, y : E{IsBoundExprOfDefinition} report equals(x,y) end";
		assertQueryEquals(queryString, true);
	}

	public void testEquals9() throws Exception {
		String queryString = "from x : V, y : E report equals(x,y) end";
		JValue result = evalTestQuery("Equals5", queryString);
		for (JValue v : result.toCollection()) {
			assertEquals(Boolean.FALSE, v.toBoolean());
		}
	}

	@Test
	public void testGrEqual1() throws Exception {
		assertQueryEquals("3 >= 2", true);
		assertQueryEquals("grEqual(3, 2)", true);
	}

	@Test
	public void testGrEqual2() throws Exception {
		assertQueryEquals("17 >= 17", true);
		assertQueryEquals("grEqual(17, 17.0)", true);
	}

	@Test
	public void testGrEqual3() throws Exception {
		assertQueryEquals("0.000000000000001 >= 0", true);
		assertQueryEquals("grEqual(0.000000000000001, 0)", true);
	}

	@Test
	public void testGrEqual4() throws Exception {
		assertQueryEquals("17 >= 199", false);
		assertQueryEquals("grEqual(17, 199)", false);
	}

	@Test
	public void testGrEqual5() throws Exception {
		assertQueryEquals("5.50 >= 4.701", true);
		assertQueryEquals("grEqual(5.50, 4.701)", true);
	}

	@Test
	public void testGrEqual6() throws Exception {
		assertQueryEquals("33.1 >= 33.1", true);
		assertQueryEquals("grEqual(33.1, 33.1)", true);
	}

	@Test
	public void testGrEqual7() throws Exception {
		assertQueryEquals("117.4 >= 111", true);
		assertQueryEquals("grEqual(117.4, 111)", true);
	}

	@Test
	public void testGrEqual8() throws Exception {
		assertQueryEquals("3 >= 187.00001", false);
		assertQueryEquals("grEqual(3, 187.00001)", false);
	}

	@Test
	public void testNotEquals() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{WhereExpression}, y : V{Variable} reportSet x <> y end";
		JValue result = evalTestQuery("NotEquals", queryString);
		assertEquals(1, result.toCollection().size());
		assertEquals(true, (boolean) getNthValue(result.toCollection(), 0)
				.toBoolean());
	}

	@Test
	public void testNotEquals2() throws Exception {
		String queryString = "from x : V{Greql2Expression}, y : V{Greql2Expression} reportSet x <> y end";
		JValue result = evalTestQuery("NotEquals2", queryString);
		assertEquals(1, result.toCollection().size());
		assertEquals(false, (boolean) getNthValue(result.toCollection(), 0)
				.toBoolean());
	}
}