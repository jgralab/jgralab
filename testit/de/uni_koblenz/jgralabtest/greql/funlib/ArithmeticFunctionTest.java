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

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;

import org.junit.Test;

import de.uni_koblenz.jgralabtest.greql.GenericTest;

public class ArithmeticFunctionTest extends GenericTest {

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

	// @Test
	public void testAddSpecialCases1Infix() throws Exception {
		assertQueryEquals("Infinity + 1.5", POSITIVE_INFINITY);
		assertQueryEquals("6 + Infinity", POSITIVE_INFINITY);
		assertQueryEquals("-Infinity + 1.5", NEGATIVE_INFINITY);
		assertQueryEquals("6 + -Infinity", NEGATIVE_INFINITY);
		assertQueryEquals("NaN + 1.5", NaN);
		assertQueryEquals("6 + -NaN", NaN);
	}

	// @Test
	public void testAddSpecialCases1() throws Exception {
		assertQueryEquals("add(Infinity, 1.5)", POSITIVE_INFINITY);
		assertQueryEquals("add(6, Infinity)", POSITIVE_INFINITY);
		assertQueryEquals("add(-Infinity, 1.5)", NEGATIVE_INFINITY);
		assertQueryEquals("add(6, -Infinity)", NEGATIVE_INFINITY);
		assertQueryEquals("add(-NaN, 1.5)", NEGATIVE_INFINITY);
		assertQueryEquals("add(6, -NaN)", NaN);
	}

	// @Test
	public void testAddSpecialCases2Infix() throws Exception {
		assertQueryEquals("Infinity + Infinity", POSITIVE_INFINITY);
		assertQueryEquals("-Infinity + -Infinity", NEGATIVE_INFINITY);
		assertQueryEquals("-NaN + -NaN", NaN);
		assertQueryEquals("Infinity + -Infinity", NaN);
		assertQueryEquals("Infinity + NaN", NaN);
		assertQueryEquals("-Infinity + NaN", NaN);
	}

	// @Test
	public void testAddSpecialCases2() throws Exception {
		assertQueryEquals("add(Infinity, Infinity)", POSITIVE_INFINITY);
		assertQueryEquals("add(-Infinity, -Infinity)", NEGATIVE_INFINITY);
		assertQueryEquals("add(-NaN, -NaN)", NaN);
		assertQueryEquals("add(Infinity, -Infinity)", NaN);
		assertQueryEquals("add(Infinity, NaN)", NaN);
		assertQueryEquals("add(-Infinity, NaN)", NaN);
	}

	@Test
	public void testDivInfix() throws Exception {
		assertQueryEquals("3 / 0", POSITIVE_INFINITY);
		assertQueryEquals("-3 / 0", NEGATIVE_INFINITY);
		assertQueryEquals("0 / 3", 0.0);
		assertQueryEquals("0 / 3.5", 0.0);
		assertQueryEquals("3 / 1", 3.0);
		assertQueryEquals("3 / 7", 3 / 7.0);
	}

	@Test
	public void testAddNull() throws Exception {

		assertQueryIsUndefined("using nll: add(nll, nll)");
		assertQueryIsUndefined("using nll: add(nll, 100)");
		assertQueryIsUndefined("using nll: add(100, nll)");
	}

	@Test
	public void testAddInfixNull() throws Exception {

		assertQueryIsUndefined("using nll: nll + nll");
		assertQueryIsUndefined("using nll: nll + 100");
		assertQueryIsUndefined("using nll: 100 + nll");
	}

	@Test
	public void testDiv() throws Exception {
		assertQueryEquals("div(-3, 0)", NEGATIVE_INFINITY);
		assertQueryEquals("div(3, 0)", POSITIVE_INFINITY);
		assertQueryEquals("div(0, 3)", 0.0);
		assertQueryEquals("div(0, 3.5)", 0.0);
		assertQueryEquals("div(3, 1)", 3.0);
		assertQueryEquals("div(3, 7)", 3 / 7.0);
	}

	// @Test
	public void testDivSpecialCases1Infix() throws Exception {
		assertQueryEquals("Infinity / 7", POSITIVE_INFINITY);
		assertQueryEquals("3 / Infinity", POSITIVE_INFINITY);
		assertQueryEquals("-Infinity / 7", NaN);
		assertQueryEquals("3 / -Infinity", NaN);
		assertQueryEquals("NaN / 7", NaN);
		assertQueryEquals("3 / NaN", NaN);
	}

	// @Test
	public void testDivSpecialCases1() throws Exception {
		assertQueryEquals("div(3, Infinity)", POSITIVE_INFINITY);
		assertQueryEquals("div(Infinity, 7)", POSITIVE_INFINITY);
		assertQueryEquals("div(-Infinity, 7)", NaN);
		assertQueryEquals("div(3, -Infinity)", NaN);
		assertQueryEquals("div(Nan, 7)", NaN);
		assertQueryEquals("div(3, NaN)", NaN);
	}

	// @Test
	public void testDivSpecialCases2Infix() throws Exception {
		assertQueryEquals("Infinity / Infinity", NaN);
		assertQueryEquals("-Infinity / -Infinity", NaN);
		assertQueryEquals("NaN - NaN", NaN);
		assertQueryEquals("Infinity / -Infinity", NaN);
		assertQueryEquals("Infinity / NaN", NaN);
		assertQueryEquals("-Infinity / NaN", NaN);
	}

	// @Test
	public void testDivSpecialCases2() throws Exception {
		assertQueryEquals("div(Infinity, Infinity)", NaN);
		assertQueryEquals("div(-Infinity, -Infinity)", NaN);
		assertQueryEquals("div(NaN, NaN)", NaN);
		assertQueryEquals("div(Infinity, -Infinity)", NaN);
		assertQueryEquals("div(Infinity, NaN)", NaN);
		assertQueryEquals("div(-Infinity, NaN)", NaN);
	}

	@Test
	public void testDivNull() throws Exception {

		assertQueryIsUndefined("using nll: div(nll, nll)");
		assertQueryIsUndefined("using nll: div(nll, 100)");
		assertQueryIsUndefined("using nll: div(100, nll)");
	}

	@Test
	public void testDivInfixNull() throws Exception {

		assertQueryIsUndefined("using nll: nll / nll");
		assertQueryIsUndefined("using nll: nll / 100");
		assertQueryIsUndefined("using nll: 100 / nll");
	}

	@Test
	public void testModInfix() throws Exception {
		assertQueryEquals("9.5 % 2", 1.5);
		assertQueryEquals("9 % 2", 1);
		assertQueryEquals("-9 % 2", -1);
		assertQueryEquals("9 % 3", 0);
	}

	@Test
	public void testMod() throws Exception {
		assertQueryEquals("mod(9, 2)", 1);
		assertQueryEquals("mod(9.5, 2)", 1.5);
		assertQueryEquals("mod(-9, 2)", -1);
		assertQueryEquals("mod(9, 3)", 0);
	}

	// @Test
	public void testModSpecialCases1Infix() throws Exception {
		assertQueryEquals("Infinity % 7", NaN);
		assertQueryEquals("3 % Infinity", NaN);
		assertQueryEquals("-Infinity % 7", NaN);
		assertQueryEquals("3 % -Infinity", NaN);
		assertQueryEquals("NaN % 7", NaN);
		assertQueryEquals("3 % NaN", NaN);
	}

	// @Test
	public void testModSpecialCases1() throws Exception {
		assertQueryEquals("mod(3, Infinity)", NaN);
		assertQueryEquals("mod(Infinity, 7)", NaN);
		assertQueryEquals("mod(-Infinity, 7)", NaN);
		assertQueryEquals("mod(3, -Infinity)", NaN);
		assertQueryEquals("mod(Nan, 7)", NaN);
		assertQueryEquals("mod(3, NaN)", NaN);
	}

	// @Test
	public void testModSpecialCases2Infix() throws Exception {
		assertQueryEquals("Infinity % Infinity", NaN);
		assertQueryEquals("-Infinity % -Infinity", NaN);
		assertQueryEquals("NaN % NaN", NaN);
		assertQueryEquals("Infinity % -Infinity", NaN);
		assertQueryEquals("Infinity % NaN", NaN);
		assertQueryEquals("-Infinity % NaN", NaN);
	}

	// @Test
	public void testModSpecialCases2() throws Exception {
		assertQueryEquals("mod(Infinity, Infinity)", NaN);
		assertQueryEquals("mod(-Infinity, -Infinity)", NaN);
		assertQueryEquals("mod(NaN, NaN)", NaN);
		assertQueryEquals("mod(Infinity, -Infinity)", NaN);
		assertQueryEquals("mod(Infinity, NaN)", NaN);
		assertQueryEquals("mod(-Infinity, NaN)", NaN);
	}

	@Test
	public void testModNull() throws Exception {

		assertQueryIsUndefined("using nll: mod(nll, nll)");
		assertQueryIsUndefined("using nll: mod(nll, 100)");
		assertQueryIsUndefined("using nll: mod(100, nll)");
	}

	@Test
	public void testModInfixNull() throws Exception {

		assertQueryIsUndefined("using nll: nll % nll");
		assertQueryIsUndefined("using nll: nll % 100");
		assertQueryIsUndefined("using nll: 100 % nll");
	}

	@Test
	public void testMulInfix() throws Exception {
		assertQueryEquals("6 * 1.5", 9.0);
		assertQueryEquals("0 * 1.5", 0.0);
		assertQueryEquals("6 * 0", 0.0);
		assertQueryEquals("0 * 0", 0.0);
		assertQueryEquals("1 * 1.5", 1.5);
		assertQueryEquals("6 * 1", 6.0);
		assertQueryEquals("1 * 1", 1.0);
	}

	@Test
	public void testMul() throws Exception {
		assertQueryEquals("mul(0, 1.5)", 0.0);
		assertQueryEquals("mul(6, 1.5)", 9.0);
		assertQueryEquals("mul(6, 0)", 0.0);
		assertQueryEquals("mul(0, 0)", 0.0);
		assertQueryEquals("mul(1, 1.5)", 1.5);
		assertQueryEquals("mul(6, 1)", 6.0);
		assertQueryEquals("mul(1, 1)", 1.0);
	}

	// @Test
	public void testMulSpecialCases1Infix() throws Exception {
		assertQueryEquals("Infinity * 7", POSITIVE_INFINITY);
		assertQueryEquals("3 * Infinity", POSITIVE_INFINITY);
		assertQueryEquals("-Infinity * 7", NEGATIVE_INFINITY);
		assertQueryEquals("3 * -Infinity", NEGATIVE_INFINITY);
		assertQueryEquals("NaN * 7", NaN);
		assertQueryEquals("3 * NaN", NaN);
	}

	// @Test
	public void testMulSpecialCases1() throws Exception {
		assertQueryEquals("mul(3, Infinity)", POSITIVE_INFINITY);
		assertQueryEquals("mul(Infinity, 7)", POSITIVE_INFINITY);
		assertQueryEquals("mul(-Infinity, 7)", NEGATIVE_INFINITY);
		assertQueryEquals("mul(3, -Infinity)", NEGATIVE_INFINITY);
		assertQueryEquals("mul(Nan, 7)", NaN);
		assertQueryEquals("mul(3, NaN)", NaN);
	}

	// @Test
	public void testMulSpecialCases2Infix() throws Exception {
		assertQueryEquals("Infinity * Infinity", POSITIVE_INFINITY);
		assertQueryEquals("-Infinity * -Infinity", POSITIVE_INFINITY);
		assertQueryEquals("NaN * NaN", NaN);
		assertQueryEquals("Infinity * -Infinity", NEGATIVE_INFINITY);
		assertQueryEquals("Infinity * NaN", NaN);
		assertQueryEquals("-Infinity * NaN", NaN);
	}

	// @Test
	public void testMulSpecialCases2() throws Exception {
		assertQueryEquals("mul(Infinity, Infinity)", POSITIVE_INFINITY);
		assertQueryEquals("mul(-Infinity, -Infinity)", POSITIVE_INFINITY);
		assertQueryEquals("mul(NaN, NaN)", NaN);
		assertQueryEquals("mul(Infinity, -Infinity)", NEGATIVE_INFINITY);
		assertQueryEquals("mul(Infinity, NaN)", NaN);
		assertQueryEquals("mul(-Infinity, NaN)", NaN);
	}

	@Test
	public void testMulNull() throws Exception {

		assertQueryIsUndefined("using nll: mul(nll, nll)");
		assertQueryIsUndefined("using nll: mul(nll, 100)");
		assertQueryIsUndefined("using nll: mul(100, nll)");
	}

	@Test
	public void testMulInfixNull() throws Exception {

		assertQueryIsUndefined("using nll: nll * nll");
		assertQueryIsUndefined("using nll: nll * 100");
		assertQueryIsUndefined("using nll: 100 * nll");
	}

	@Test
	public void testMultiplicative() throws Exception {
		assertQueryEquals("100 / 10 / 5 * 2", 4.0);
	}

	@Test
	public void testNegInfix() throws Exception {
		assertQueryEquals("-(-1)", 1);
		assertQueryEquals("-(1)", -1);
		assertQueryEquals("-(-(-1))", -1);
		assertQueryEquals("-(1.23123)", -1.23123);
	}

	@Test
	public void testNeg() throws Exception {
		assertQueryEquals("neg(-1)", 1);
		assertQueryEquals("neg(1)", -1);
		assertQueryEquals("neg(neg(-1))", -1);
		assertQueryEquals("neg(1.23123)", -1.23123);
	}

	// @Test
	public void testNegSpecialCasesInfix() throws Exception {
		assertQueryEquals("-NaN", NaN);
		assertQueryEquals("-(Infinity)", NEGATIVE_INFINITY);
		assertQueryEquals("-(-Infinity)", POSITIVE_INFINITY);
		assertQueryEquals("-(0.0)", -0.0);
		assertQueryEquals("-(-0.0)", 0.0);
	}

	// @Test
	public void testNegSpecialCases() throws Exception {
		assertQueryEquals("neg(NaN)", NaN);
		assertQueryEquals("neg(Infinity)", NEGATIVE_INFINITY);
		assertQueryEquals("neg(-Infinity)", POSITIVE_INFINITY);
		assertQueryEquals("neg(0.0)", -0.0);
		assertQueryEquals("neg(-0.0)", 0.0);
	}

	@Test
	public void testNegNull() throws Exception {
		assertQueryIsUndefined("using nll: neg(nll)");

	}

	@Test
	public void testNegInfixNull() throws Exception {

		assertQueryIsUndefined("using nll: nll - nll");
		assertQueryIsUndefined("using nll: nll - 100");
		assertQueryIsUndefined("using nll: 100 - nll");
	}

	@Test
	public void testSqrt() throws Exception {
		assertQueryEquals("sqrt(4)", 2.0);
		assertQueryEquals("sqrt(100)", 10.0);
		assertQueryEquals("sqrt(0.25)", 0.5);
		assertQueryEquals("sqrt(56.25)", 7.5);
		assertQueryEquals("sqrt(-2)", NaN);
		assertQueryEquals("sqrt(0.0)", 0.0);
		assertQueryEquals("sqrt(-0.0)", -0.0);
	}

	// @Test
	public void testSqrtSpecialCase2() throws Exception {
		assertQueryEquals("sqrt(NaN)", NaN);
		assertQueryEquals("sqrt(Infinity)", NEGATIVE_INFINITY);
		assertQueryEquals("sqrt(-Infinity)", POSITIVE_INFINITY);
	}

	@Test
	public void testSqrtNull() throws Exception {
		assertQueryIsUndefined("using nll: sqrt(nll)");

	}

	@Test
	public void testSubInfix() throws Exception {
		assertQueryEquals("6 - -1.5", 7.5);
		assertQueryEquals("6 - 3", 3L);
		assertQueryEquals("16 - 323", -307L);
		assertQueryEquals("1.5 - 6", -4.5);
		assertQueryEquals("10 - 4 - 3 - 2", 1);
	}

	@Test
	public void testSub2() throws Exception {
		assertQueryEquals("sub(6, -1.5)", 7.5);
		assertQueryEquals("sub(6, 3)", 3L);
		assertQueryEquals("sub(16, 323)", -307L);
		assertQueryEquals("sub(1.5, 6)", -4.5);
		assertQueryEquals("sub(sub(sub(10, 4), 3), 2)", 1);
	}

	// @Test
	public void testSubSpecialCase1() throws Exception {
		assertQueryEquals("Infinity - 7", POSITIVE_INFINITY);
		assertQueryEquals("3 - Infinity", POSITIVE_INFINITY);
		assertQueryEquals("-Infinity - 7", NaN);
		assertQueryEquals("3 - -Infinity", NaN);
		assertQueryEquals("NaN - 7", NaN);
		assertQueryEquals("3 - NaN", NaN);
	}

	// @Test
	public void testSubSpecialCase2() throws Exception {
		assertQueryEquals("sub(Infinity, 7)", POSITIVE_INFINITY);
		assertQueryEquals("sub(3, Infinity)", POSITIVE_INFINITY);
		assertQueryEquals("sub(-Infinity, 7)", NaN);
		assertQueryEquals("sub(3, -Infinity)", NaN);
		assertQueryEquals("sub(Nan, 7)", NaN);
		assertQueryEquals("sub(3, NaN)", NaN);
	}

	// @Test
	public void testSubSpecialCasesInfix() throws Exception {
		assertQueryEquals("Infinity - Infinity", NaN);
		assertQueryEquals("-Infinity - -Infinity", NaN);
		assertQueryEquals("NaN - NaN", NaN);
		assertQueryEquals("Infinity - -Infinity", POSITIVE_INFINITY);
		assertQueryEquals("Infinity - NaN", NaN);
		assertQueryEquals("-Infinity - NaN", NaN);
	}

	// @Test
	public void testSubSpecialCases() throws Exception {
		assertQueryEquals("sub(Infinity, Infinity)", NaN);
		assertQueryEquals("sub(-Infinity, -Infinity)", NaN);
		assertQueryEquals("sub(NaN, NaN)", NaN);
		assertQueryEquals("sub(Infinity, -Infinity)", POSITIVE_INFINITY);
		assertQueryEquals("sub(Infinity, NaN)", NaN);
		assertQueryEquals("sub(-Infinity, NaN)", NaN);
	}

	@Test
	public void testSubNull() throws Exception {
		assertQueryIsUndefined("using nll: sub(nll, nll)");
		assertQueryIsUndefined("using nll: sub(nll, 100)");
		assertQueryIsUndefined("using nll: sub(100, nll)");
	}

	@Test
	public void testSubInfixNull() throws Exception {
		assertQueryIsUndefined("using nll: nll - nll");
		assertQueryIsUndefined("using nll: nll - 100");
		assertQueryIsUndefined("using nll: 100 - nll");
	}
}
