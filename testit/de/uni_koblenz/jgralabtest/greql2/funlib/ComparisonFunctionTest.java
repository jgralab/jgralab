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
import de.uni_koblenz.jgralabtest.greql2.GenericTests;

public class ComparisonFunctionTest extends GenericTests {

	@Test
	public void testEquals() throws Exception {
		String queryString = "equals(5, 9)";
		JValue result = evalTestQuery("Equals", queryString);
		assertEquals(false, (boolean) result.toBoolean());
	}

	@Test
	public void testEquals2() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{WhereExpression}, y : V{Greql2Expression} report equals(x,y) end";
		JValue result = evalTestQuery("Equals2", queryString);

		assertEquals(false, (boolean) getNthValue(result.toCollection(), 0)
				.toBoolean());
	}

	@Test
	public void testEquals3() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{WhereExpression}, y : V{WhereExpression} report equals(x,y) end";
		JValue result = evalTestQuery("Equals3", queryString);
		assertEquals(true, (boolean) getNthValue(result.toCollection(), 0)
				.toBoolean());
	}

	@Test
	public void testEquals4() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{WhereExpression}, y : E{IsBoundExprOfDefinition} report equals(x,y) end";
		JValue result = evalTestQuery("Equals4", queryString);
		assertEquals(1, result.toCollection().size());
		assertEquals(false, (boolean) getNthValue(result.toCollection(), 0)
				.toBoolean());
	}

	public void testEquals5() throws Exception {
		String queryString = "from x : V, y : E report equals(x,y) end";
		JValue result = evalTestQuery("Equals5", queryString);
		for (JValue v : result.toCollection()) {
			assertEquals(Boolean.FALSE, v.toBoolean());
		}
	}

	@Test
	public void testGrEqual() throws Exception {
		assertTrue(evalTestQuery("GrEqual", "3 >= 2").toBoolean());
		assertTrue(evalTestQuery("GrEqual", "17 >= 17").toBoolean());
		assertTrue(evalTestQuery("GrEqual", "grEqual(17, 17.0)").toBoolean());
		assertFalse(evalTestQuery("GrEqual", "17 >= 199").toBoolean());
		assertTrue(evalTestQuery("GrEqual", "5.50 >= 4.701").toBoolean());
		assertTrue(evalTestQuery("GrEqual", "33.1 >= 33.1").toBoolean());
		assertTrue(evalTestQuery("GrEqual", "grEqual(117.4, 111)").toBoolean());
		assertFalse(evalTestQuery("GrEqual", "3 >= 187.00001").toBoolean());
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
