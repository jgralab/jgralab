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

public class LogicFunctionTest extends GenericTests {

	public void testBooleanFunction(String functionName, boolean arg1,
			boolean arg2, boolean expected) throws Exception {
		String queryString = functionName + "(" + arg1 + "," + arg2 + ")";
		assertQueryEquals(queryString, expected);
	}

	public void testBooleanOperant(String functionName, boolean arg1,
			boolean arg2, boolean expected) throws Exception {
		String queryString = arg1 + " " + functionName + " " + arg2;
		assertQueryEquals(queryString, expected);
	}

	public void testBooleanOperation(String functionName, boolean arg1,
			boolean arg2, boolean expected) throws Exception {
		testBooleanFunction(functionName, arg1, arg2, expected);
		testBooleanOperant(functionName, arg1, arg2, expected);
	}

	@Test
	public void testAndAsInfixExpression() throws Exception {
		assertQueryEqualsDB("true", "true and true");
		assertQueryEqualsDB("false", "true and false");
		assertQueryEqualsDB("false", "false and true");
		assertQueryEqualsDB("false", "false and false");
	}

	@Test
	public void testAndAsFunction() throws Exception {
		assertQueryEqualsDB("true", "and(true,true)");
		assertQueryEqualsDB("false", "and(true,false)");
		assertQueryEqualsDB("false", "and(false,true)");
		assertQueryEqualsDB("false", "and(false,false)");
	}

	public void assertQueryEqualsDB(String expected, String current)
			throws Exception {
		assertEquals(evalTestQuery("", expected), evalTestQuery("", current));
	}

	/*
	 * Test method for the GReQL function 'and'.
	 */
	@Test
	public void testAnd1() throws Exception {
		testBooleanOperation("and", false, false, false);
		testBooleanOperation("and", false, true, false);
		testBooleanOperation("and", true, false, false);
		testBooleanOperation("and", true, true, true);

		assertQueryEquals("false and false", false);
	}

	/*
	 * Test method for the GReQL function 'or'.
	 */
	@Test
	public void testOr1() throws Exception {
		testBooleanOperation("or", false, false, false);
		testBooleanOperation("or", false, true, true);
		testBooleanOperation("or", true, false, true);
		testBooleanOperation("or", true, true, true);
	}

	/*
	 * Test method for the GReQL function 'or'.
	 */
	@Test
	public void testOr() throws Exception {
		String queryString = "from el:list(1..100) "
				+ "with or(el % 2 = 0, el % 3 = 0) report el end";
		JValue result = evalTestQuery("or", queryString);
		assertEquals(50 + 17, result.toCollection().size());
	}

	/*
	 * Test method for the GReQL function 'xor' as Infix.
	 */
	@Test
	public void testXorInfix() throws Exception {
		String queryString = "from el:list(1..100) "
				+ "with el % 2 = 0 xor el % 3 = 0 report el end";
		JValue result = evalTestQuery("or", queryString);
		assertEquals(50 + 17 - 16, result.toCollection().size());
	}

	/*
	 * Test method for the GReQL function 'xor'.
	 */
	@Test
	public void testXor1() throws Exception {
		testBooleanOperation("xor", false, false, false);
	}

	/*
	 * Test method for the GReQL function 'xor'.
	 */
	@Test
	public void testXor2() throws Exception {
		testBooleanOperation("xor", false, true, true);
	}

	/*
	 * Test method for the GReQL function 'xor'.
	 */
	@Test
	public void testXor3() throws Exception {
		testBooleanOperation("xor", true, false, true);
	}

	/*
	 * Test method for the GReQL function 'xor'.
	 */
	@Test
	public void testXor4() throws Exception {
		testBooleanOperation("xor", true, true, false);
	}

	/*
	 * Test method for the GReQL function 'xor'.
	 */
	@Test
	public void testXor() throws Exception {
		String queryString = "from el:list(1..100) "
				+ "with xor(el % 2 = 0, el % 3 = 0) report el end";
		JValue result = evalTestQuery("or", queryString);
		assertEquals(50 + 17 - 16, result.toCollection().size());
	}
}
