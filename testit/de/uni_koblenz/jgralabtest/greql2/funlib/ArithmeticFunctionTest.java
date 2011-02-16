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

import static java.lang.Double.NaN;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;

import org.junit.Test;

import de.uni_koblenz.jgralabtest.greql2.GenericTests;

public class ArithmeticFunctionTest extends GenericTests {

	@Test
	public void testAddInfix() throws Exception {
		assertQueryEquals("0 + 1.5", 1.5);
		assertQueryEquals("6 + 0", 6);
		assertQueryEquals("0 + 0", 0);
		assertQueryEquals("6 + 1.5", 7.5);
		assertQueryEquals("6 + -1.5", 4.5);
		assertQueryEquals("0.025 + 0.975", 1.0);
	}

	@Test
	public void testAdd() throws Exception {
		assertQueryEquals("add(6, 0)", 6);
		assertQueryEquals("add(0, 1.5)", 1.5);
		assertQueryEquals("add(0, 0)", 0);
		assertQueryEquals("add(6, 1.5)", 7.5);
		assertQueryEquals("add(6, -1.5)", 4.5);
		assertQueryEquals("add(0.025, 0.975)", 1.0);
	}

	@Test
	public void testAddSpecialCases1Infix() throws Exception {
		assertQueryEquals("Infinity + 1.5", POSITIVE_INFINITY);
		assertQueryEquals("6 + Infinity", POSITIVE_INFINITY);
		assertQueryEquals("-Infinity + 1.5", NEGATIVE_INFINITY);
		assertQueryEquals("6 + -Infinity", NEGATIVE_INFINITY);
		assertQueryEquals("NaN + 1.5", NaN);
		assertQueryEquals("6 + -NaN", NaN);
	}

	@Test
	public void testAddSpecialCases1() throws Exception {
		assertQueryEquals("add(Infinity, 1.5)", POSITIVE_INFINITY);
		assertQueryEquals("add(6, Infinity)", POSITIVE_INFINITY);
		assertQueryEquals("add(-Infinity, 1.5)", NEGATIVE_INFINITY);
		assertQueryEquals("add(6, -Infinity)", NEGATIVE_INFINITY);
		assertQueryEquals("add(-NaN, 1.5)", NEGATIVE_INFINITY);
		assertQueryEquals("add(6, -NaN)", NaN);
	}

	@Test
	public void testAddSpecialCases2Infix() throws Exception {
		assertQueryEquals("Infinity + Infinity", POSITIVE_INFINITY);
		assertQueryEquals("-Infinity + -Infinity", NEGATIVE_INFINITY);
		assertQueryEquals("-NaN + -NaN", NaN);
		assertQueryEquals("Infinity + -Infinity", NaN);
		assertQueryEquals("Infinity + NaN", NaN);
		assertQueryEquals("-Infinity + NaN", NaN);
	}

	@Test
	public void testAddSpecialCases2() throws Exception {
		assertQueryEquals("add(Infinity, Infinity)", POSITIVE_INFINITY);
		assertQueryEquals("add(-Infinity, -Infinity)", NEGATIVE_INFINITY);
		assertQueryEquals("add(-NaN, -NaN)", NaN);
		assertQueryEquals("add(Infinity, -Infinity)", NaN);
		assertQueryEquals("add(Infinity, NaN)", NaN);
		assertQueryEquals("add(-Infinity, NaN)", NaN);
	}

	@Test
	public void testDiv1() throws Exception {
		assertQueryEquals("3 / 0", POSITIVE_INFINITY);
		assertQueryEquals("div(3, 0)", POSITIVE_INFINITY);
	}

	@Test
	public void testDiv2() throws Exception {
		assertQueryEquals("-3 / 0", NEGATIVE_INFINITY);
		assertQueryEquals("div(-3, 0)", NEGATIVE_INFINITY);
	}

	@Test
	public void testDiv3() throws Exception {
		assertQueryEquals("0 / 3", 0.0);
		assertQueryEquals("div(0, 3)", 0.0);
	}

	@Test
	public void testDiv4() throws Exception {
		assertQueryEquals("0 / 3.5", 0.0);
		assertQueryEquals("div(0, 3.5)", 0.0);
	}

	@Test
	public void testDiv5() throws Exception {
		assertQueryEquals("3 / 1", 3.0);
		assertQueryEquals("div(3, 1)", 3.0);
	}

	@Test
	public void testDiv6() throws Exception {
		assertQueryEquals("3 / 7", 3 / 7.0);
		assertQueryEquals("div(3, 7)", 3 / 7.0);
	}

	@Test
	public void testDivSpecialCase1() throws Exception {
		assertQueryEquals("Infinity / 7", POSITIVE_INFINITY);
		assertQueryEquals("div(Infinity, 7)", POSITIVE_INFINITY);
	}

	@Test
	public void testDivSpecialCase2() throws Exception {
		assertQueryEquals("3 / Infinity", POSITIVE_INFINITY);
		assertQueryEquals("div(3, Infinity)", POSITIVE_INFINITY);
	}

	@Test
	public void testDivSpecialCase3() throws Exception {
		assertQueryEquals("-Infinity / 7", NaN);
		assertQueryEquals("div(-Infinity, 7)", NaN);
	}

	@Test
	public void testDivSpecialCase4() throws Exception {
		assertQueryEquals("3 / -Infinity", NaN);
		assertQueryEquals("div(3, -Infinity)", NaN);
	}

	@Test
	public void testDivSpecialCase5() throws Exception {
		assertQueryEquals("NaN / 7", NaN);
		assertQueryEquals("div(Nan, 7)", NaN);
	}

	@Test
	public void testDivSpecialCase6() throws Exception {
		assertQueryEquals("3 / NaN", NaN);
		assertQueryEquals("div(3, NaN)", NaN);
	}

	@Test
	public void testDivSpecialCases() throws Exception {

		assertQueryEquals("Infinity / Infinity", NaN);
		assertQueryEquals("-Infinity / -Infinity", NaN);
		assertQueryEquals("NaN - NaN", NaN);
		assertQueryEquals("Infinity / -Infinity", NaN);
		assertQueryEquals("Infinity / NaN", NaN);
		assertQueryEquals("-Infinity / NaN", NaN);

		// As functions
		assertQueryEquals("div(Infinity, Infinity)", NaN);
		assertQueryEquals("div(-Infinity, -Infinity)", NaN);
		assertQueryEquals("div(NaN, NaN)", NaN);
		assertQueryEquals("div(Infinity, -Infinity)", NaN);
		assertQueryEquals("div(Infinity, NaN)", NaN);
		assertQueryEquals("div(-Infinity, NaN)", NaN);
	}

	@Test
	public void testMod1() throws Exception {
		assertQueryEquals("9.5 % 2", 1.5);
		assertQueryEquals("mod(9.5, 2)", 1.5);
	}

	@Test
	public void testMod2() throws Exception {
		assertQueryEquals("9 % 2", 1);
		assertQueryEquals("mod(9, 2)", 1);
	}

	@Test
	public void testMod3() throws Exception {
		assertQueryEquals("-9 % 2", -1);
		assertQueryEquals("mod(-9, 2)", -1);
	}

	@Test
	public void testMod4() throws Exception {
		assertQueryEquals("9 % 3", 0);
		assertQueryEquals("mod(9, 3)", 0);
	}

	@Test
	public void testModSpecialCase1() throws Exception {
		assertQueryEquals("Infinity % 7", NaN);
		assertQueryEquals("mod(Infinity, 7)", NaN);
	}

	@Test
	public void testModSpecialCase2() throws Exception {
		assertQueryEquals("3 % Infinity", NaN);
		assertQueryEquals("mod(3, Infinity)", NaN);
	}

	@Test
	public void testModSpecialCase3() throws Exception {
		assertQueryEquals("-Infinity % 7", NaN);
		assertQueryEquals("mod(-Infinity, 7)", NaN);
	}

	@Test
	public void testModSpecialCase4() throws Exception {
		assertQueryEquals("3 % -Infinity", NaN);
		assertQueryEquals("mod(3, -Infinity)", NaN);
	}

	@Test
	public void testModSpecialCase5() throws Exception {
		assertQueryEquals("NaN % 7", NaN);
		assertQueryEquals("mod(Nan, 7)", NaN);
	}

	@Test
	public void testModSpecialCase6() throws Exception {
		assertQueryEquals("3 % NaN", NaN);
		assertQueryEquals("mod(3, NaN)", NaN);
	}

	@Test
	public void testModSpecialCases() throws Exception {

		assertQueryEquals("Infinity % Infinity", NaN);
		assertQueryEquals("-Infinity % -Infinity", NaN);
		assertQueryEquals("NaN % NaN", NaN);
		assertQueryEquals("Infinity % -Infinity", NaN);
		assertQueryEquals("Infinity % NaN", NaN);
		assertQueryEquals("-Infinity % NaN", NaN);

		// As functions
		assertQueryEquals("mod(Infinity, Infinity)", NaN);
		assertQueryEquals("mod(-Infinity, -Infinity)", NaN);
		assertQueryEquals("mod(NaN, NaN)", NaN);
		assertQueryEquals("mod(Infinity, -Infinity)", NaN);
		assertQueryEquals("mod(Infinity, NaN)", NaN);
		assertQueryEquals("mod(-Infinity, NaN)", NaN);
	}

	@Test
	public void testMul1() throws Exception {
		assertQueryEquals("6 * 1.5", 9.0);
		assertQueryEquals("mul(6, 1.5)", 9.0);
	}

	@Test
	public void testMul2() throws Exception {
		assertQueryEquals("0 * 1.5", 0.0);
		assertQueryEquals("mul(0, 1.5)", 0.0);
	}

	@Test
	public void testMul3() throws Exception {
		assertQueryEquals("6 * 0", 0.0);
		assertQueryEquals("mul(6, 0)", 0.0);
	}

	@Test
	public void testMul4() throws Exception {
		assertQueryEquals("0 * 0", 0.0);
		assertQueryEquals("mul(0, 0)", 0.0);
	}

	@Test
	public void testMul5() throws Exception {
		assertQueryEquals("1 * 1.5", 1.5);
		assertQueryEquals("mul(1, 1.5)", 1.5);
	}

	@Test
	public void testMul6() throws Exception {
		assertQueryEquals("6 * 1", 6.0);
		assertQueryEquals("mul(6, 1)", 6.0);
	}

	@Test
	public void testMul7() throws Exception {
		assertQueryEquals("1 * 1", 1.0);
		assertQueryEquals("mul(1, 1)", 1.0);
	}

	@Test
	public void testMulSpecialCase1() throws Exception {
		assertQueryEquals("Infinity * 7", POSITIVE_INFINITY);
		assertQueryEquals("mul(Infinity, 7)", POSITIVE_INFINITY);
	}

	@Test
	public void testMulSpecialCase2() throws Exception {
		assertQueryEquals("3 * Infinity", POSITIVE_INFINITY);
		assertQueryEquals("mul(3, Infinity)", POSITIVE_INFINITY);
	}

	@Test
	public void testMulSpecialCase3() throws Exception {
		assertQueryEquals("-Infinity * 7", NEGATIVE_INFINITY);
		assertQueryEquals("mul(-Infinity, 7)", NEGATIVE_INFINITY);
	}

	@Test
	public void testMulSpecialCase4() throws Exception {
		assertQueryEquals("3 * -Infinity", NEGATIVE_INFINITY);
		assertQueryEquals("mul(3, -Infinity)", NEGATIVE_INFINITY);
	}

	@Test
	public void testMulSpecialCase5() throws Exception {
		assertQueryEquals("NaN * 7", NaN);
		assertQueryEquals("mul(Nan, 7)", NaN);
	}

	@Test
	public void testMulSpecialCase6() throws Exception {
		assertQueryEquals("3 * NaN", NaN);
		assertQueryEquals("mul(3, NaN)", NaN);
	}

	@Test
	public void testMulSpecialCases() throws Exception {

		assertQueryEquals("Infinity * Infinity", POSITIVE_INFINITY);
		assertQueryEquals("-Infinity * -Infinity", POSITIVE_INFINITY);
		assertQueryEquals("NaN * NaN", NaN);
		assertQueryEquals("Infinity * -Infinity", NEGATIVE_INFINITY);
		assertQueryEquals("Infinity * NaN", NaN);
		assertQueryEquals("-Infinity * NaN", NaN);

		// As functions
		assertQueryEquals("mul(Infinity, Infinity)", POSITIVE_INFINITY);
		assertQueryEquals("mul(-Infinity, -Infinity)", POSITIVE_INFINITY);
		assertQueryEquals("mul(NaN, NaN)", NaN);
		assertQueryEquals("mul(Infinity, -Infinity)", NEGATIVE_INFINITY);
		assertQueryEquals("mul(Infinity, NaN)", NaN);
		assertQueryEquals("mul(-Infinity, NaN)", NaN);
	}

	@Test
	public void testMultiplicative() throws Exception {
		assertQueryEquals("100 / 10 / 5 * 2", 4.0);
	}

	@Test
	public void testNegSpecialCase1() throws Exception {
		assertQueryEquals("-NaN", NaN);
		assertQueryEquals("neg(NaN)", NaN);
	}

	@Test
	public void testNegSpecialCase2() throws Exception {
		assertQueryEquals("-(Infinity)", NEGATIVE_INFINITY);
		assertQueryEquals("neg(Infinity)", NEGATIVE_INFINITY);
	}

	@Test
	public void testNegSpecialCase3() throws Exception {
		assertQueryEquals("-(-Infinity)", POSITIVE_INFINITY);
		assertQueryEquals("neg(-Infinity)", POSITIVE_INFINITY);
	}

	@Test
	public void testNegSpecialCase4() throws Exception {
		assertQueryEquals("-(0.0)", -0.0);
		assertQueryEquals("neg(0.0)", -0.0);
		assertQueryEquals("-(-0.0)", 0.0);
		assertQueryEquals("neg(-0.0)", 0.0);
	}

	@Test
	public void testSqrt1() throws Exception {
		assertQueryEquals("sqrt(4)", 2.0);
	}

	@Test
	public void testSqrt2() throws Exception {
		assertQueryEquals("sqrt(100)", 10.0);
	}

	@Test
	public void testSqrt3() throws Exception {
		assertQueryEquals("sqrt(0.25)", 0.5);
	}

	@Test
	public void testSqrt4() throws Exception {
		assertQueryEquals("sqrt(56.25)", 7.5);
	}

	@Test
	public void testSqrt5() throws Exception {
		assertQueryEquals("sqrt(-2)", NaN);
	}

	@Test
	public void testSqrtSpecialCase1() throws Exception {
		assertQueryEquals("sqrt(NaN)", NaN);
	}

	@Test
	public void testSqrtSpecialCase2() throws Exception {
		assertQueryEquals("sqrt(Infinity)", NEGATIVE_INFINITY);
	}

	@Test
	public void testSqrtSpecialCase3() throws Exception {
		assertQueryEquals("sqrt(-Infinity)", POSITIVE_INFINITY);
	}

	@Test
	public void testSqrtSpecialCase4() throws Exception {
		assertQueryEquals("sqrt(0.0)", 0.0);
		assertQueryEquals("sqrt(-0.0)", -0.0);
	}

	@Test
	public void testSub1() throws Exception {
		assertQueryEquals("6 - -1.5", 7.5);
		assertQueryEquals("sub(6, -1.5)", 7.5);
	}

	@Test
	public void testSub2() throws Exception {
		assertQueryEquals("6 - 3", 3L);
		assertQueryEquals("sub(6, 3)", 3L);
	}

	@Test
	public void testSub3() throws Exception {
		assertQueryEquals("16 - 323", -307L);
		assertQueryEquals("sub(16, 323)", -307L);
	}

	@Test
	public void testSub4() throws Exception {
		assertQueryEquals("1.5 - 6", -4.5);
		assertQueryEquals("sub(1.5, 6)", -4.5);
	}

	@Test
	public void testSub5() throws Exception {
		assertQueryEquals("10 - 4 - 3 - 2", 1);
		assertQueryEquals("sub(sub(sub(10, 4), 3), 2)", 1);
	}

	@Test
	public void testSubSpecialCase1() throws Exception {
		assertQueryEquals("Infinity - 7", POSITIVE_INFINITY);
		assertQueryEquals("sub(Infinity, 7)", POSITIVE_INFINITY);
	}

	@Test
	public void testSubSpecialCase2() throws Exception {
		assertQueryEquals("3 - Infinity", POSITIVE_INFINITY);
		assertQueryEquals("sub(3, Infinity)", POSITIVE_INFINITY);
	}

	@Test
	public void testSubSpecialCase3() throws Exception {
		assertQueryEquals("-Infinity - 7", NaN);
		assertQueryEquals("sub(-Infinity, 7)", NaN);
	}

	@Test
	public void testSubSpecialCase4() throws Exception {
		assertQueryEquals("3 - -Infinity", NaN);
		assertQueryEquals("sub(3, -Infinity)", NaN);
	}

	@Test
	public void testSubSpecialCase5() throws Exception {
		assertQueryEquals("NaN - 7", NaN);
		assertQueryEquals("sub(Nan, 7)", NaN);
	}

	@Test
	public void testSubSpecialCase6() throws Exception {
		assertQueryEquals("3 - NaN", NaN);
		assertQueryEquals("sub(3, NaN)", NaN);
	}

	@Test
	public void testSubSpecialCases() throws Exception {

		assertQueryEquals("Infinity - Infinity", NaN);
		assertQueryEquals("-Infinity - -Infinity", NaN);
		assertQueryEquals("NaN - NaN", NaN);
		assertQueryEquals("Infinity - -Infinity", POSITIVE_INFINITY);
		assertQueryEquals("Infinity - NaN", NaN);
		assertQueryEquals("-Infinity - NaN", NaN);

		// As functions
		assertQueryEquals("sub(Infinity, Infinity)", NaN);
		assertQueryEquals("sub(-Infinity, -Infinity)", NaN);
		assertQueryEquals("sub(NaN, NaN)", NaN);
		assertQueryEquals("sub(Infinity, -Infinity)", POSITIVE_INFINITY);
		assertQueryEquals("sub(Infinity, NaN)", NaN);
		assertQueryEquals("sub(-Infinity, NaN)", NaN);
	}
}
