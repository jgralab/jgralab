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

import org.junit.Test;

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
	public void testNEqualsInfix() throws Exception {
		assertQueryEquals("5 <> 9", true);
		assertQueryEquals("'' <> 'a'", true);
		assertQueryEquals("'a' <> ''", true);
		assertQueryEquals("'' <> ''", false);
		assertQueryEquals("'a' <> 'a'", false);
		assertQueryEquals("99.001 <> 99.001", false);
		assertQueryEquals("'Eckhard Großmann' <> 'Eckhard Grossmann'", true);
		assertQueryEquals("'Eckhard Großmann' <> 'Eckhard Großmann'", false);
	}

	@Test
	public void testNEquals() throws Exception {
		assertQueryEquals("nequals(5, 9)", true);
		assertQueryEquals("nequals('', 'a')", true);
		assertQueryEquals("nequals('a', '')", true);
		assertQueryEquals("nequals('', '')", false);
		assertQueryEquals("nequals('a', 'a')", false);
		assertQueryEquals("nequals(99.001, 99.001)", false);
		assertQueryEquals("nequals('Eckhard Großmann', 'Eckhard Grossmann')",
				true);
		assertQueryEquals("nequals('Eckhard Großmann', 'Eckhard Großmann')",
				false);
	}

	@Test
	public void testGrEqualInfix() throws Exception {
		assertQueryEquals("3 >= 2", true);
		assertQueryEquals("17 >= 17", true);
		assertQueryEquals("0.000000000000001 >= 0", true);
		assertQueryEquals("17 >= 199", false);
		assertQueryEquals("5.50 >= 4.701", true);
		assertQueryEquals("33.1 >= 33.1", true);
		assertQueryEquals("117.4 >= 111", true);
		assertQueryEquals("3 >= 187.00001", false);
	}

	@Test
	public void testGrEqual() throws Exception {
		assertQueryEquals("grEqual(3, 2)", true);
		assertQueryEquals("grEqual(17, 17.0)", true);
		assertQueryEquals("grEqual(0.000000000000001, 0)", true);
		assertQueryEquals("grEqual(17, 199)", false);
		assertQueryEquals("grEqual(5.50, 4.701)", true);
		assertQueryEquals("grEqual(33.1, 33.1)", true);
		assertQueryEquals("grEqual(117.4, 111)", true);
		assertQueryEquals("grEqual(3, 187.00001)", false);
	}

	@Test
	public void testGrThanInfix() throws Exception {
		assertQueryEquals("3 > 2", true);
		assertQueryEquals("17 > 17", false);
		assertQueryEquals("0.000000000000001 > 0", true);
		assertQueryEquals("17 > 199", false);
		assertQueryEquals("5.50 > 4.701", true);
		assertQueryEquals("33.1 > 33.1", false);
		assertQueryEquals("117.4 > 111", true);
		assertQueryEquals("3 > 187.00001", false);
	}

	@Test
	public void testGrThan() throws Exception {
		assertQueryEquals("grThan(3, 2)", true);
		assertQueryEquals("grThan(17, 17.0)", false);
		assertQueryEquals("grThan(0.000000000000001, 0)", true);
		assertQueryEquals("grThan(17, 199)", false);
		assertQueryEquals("grThan(5.50, 4.701)", true);
		assertQueryEquals("grThan(33.1, 33.1)", false);
		assertQueryEquals("grThan(117.4, 111)", true);
		assertQueryEquals("grThan(3, 187.00001)", false);
	}

	@Test
	public void testLeEqualInfix() throws Exception {
		assertQueryEquals("3 <= 2", false);
		assertQueryEquals("17 <= 17", true);
		assertQueryEquals("0.000000000000001 <= 0", false);
		assertQueryEquals("17 <= 199", true);
		assertQueryEquals("5.50 <= 4.701", false);
		assertQueryEquals("33.1 <= 33.1", true);
		assertQueryEquals("117.4 <= 111", false);
		assertQueryEquals("3 <= 187.00001", true);
	}

	@Test
	public void testLeEqual() throws Exception {
		assertQueryEquals("leEqual(3, 2)", false);
		assertQueryEquals("leEqual(17, 17.0)", true);
		assertQueryEquals("leEqual(0.000000000000001, 0)", false);
		assertQueryEquals("leEqual(17, 199)", true);
		assertQueryEquals("leEqual(5.50, 4.701)", false);
		assertQueryEquals("leEqual(33.1, 33.1)", true);
		assertQueryEquals("leEqual(117.4, 111)", false);
		assertQueryEquals("leEqual(3, 187.00001)", true);
	}

	@Test
	public void testLeThanInfix() throws Exception {
		assertQueryEquals("3 < 2", false);
		assertQueryEquals("17 < 17", false);
		assertQueryEquals("0.000000000000001 < 0", false);
		assertQueryEquals("17 < 199", true);
		assertQueryEquals("5.50 < 4.701", false);
		assertQueryEquals("33.1 < 33.1", false);
		assertQueryEquals("117.4 < 111", false);
		assertQueryEquals("3 < 187.00001", true);
	}

	@Test
	public void testLeThan() throws Exception {
		assertQueryEquals("leThan(3, 2)", false);
		assertQueryEquals("leThan(17, 17.0)", false);
		assertQueryEquals("leThan(0.000000000000001, 0)", false);
		assertQueryEquals("leThan(17, 199)", true);
		assertQueryEquals("leThan(5.50, 4.701)", false);
		assertQueryEquals("leThan(33.1, 33.1)", false);
		assertQueryEquals("leThan(117.4, 111)", false);
		assertQueryEquals("leThan(3, 187.00001)", true);
	}
}