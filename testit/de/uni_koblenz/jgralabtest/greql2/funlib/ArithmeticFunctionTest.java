package de.uni_koblenz.jgralabtest.greql2.funlib;

import static org.junit.Assert.assertEquals;
import static java.lang.Double.NaN;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;

public class ArithmeticFunctionTest extends GenericTests {

	@Test
	public void testAdd1() throws Exception {
		assertQueryEquals("Add", "0 + 1.5", 1.5);
		assertQueryEquals("Add", "add(0, 1.5)", 1.5);
	}

	@Test
	public void testAdd2() throws Exception {
		assertQueryEquals("Add", "6 + 0", 6);
		assertQueryEquals("Add", "add(6, 0)", 6);
	}

	@Test
	public void testAdd3() throws Exception {
		assertQueryEquals("Add", "0 + 0", 0);
		assertQueryEquals("Add", "add(0, 0)", 0);
	}

	@Test
	public void testAdd4() throws Exception {
		assertQueryEquals("Add", "6 + 1.5", 7.5);
		assertQueryEquals("Add", "add(6, 1.5)", 7.5);
	}

	@Test
	public void testAdd5() throws Exception {
		assertQueryEquals("Add", "6 + -1.5", 4.5);
		assertQueryEquals("Add", "add(6, -1.5)", 4.5);
	}

	@Test
	public void testAdd6() throws Exception {
		assertQueryEquals("Add", "0.025 + 0.975", 1.0);
		assertQueryEquals("Add", "add(0.025, 0.975)", 1.0);

		System.out.println("Add");
		System.out.println(NaN + NaN);
		System.out.println(NaN + POSITIVE_INFINITY);
		System.out.println(NaN + NEGATIVE_INFINITY);
		System.out.println(POSITIVE_INFINITY + POSITIVE_INFINITY);
		System.out.println(POSITIVE_INFINITY + NEGATIVE_INFINITY);
		System.out.println(NEGATIVE_INFINITY + NEGATIVE_INFINITY);

		System.out.println("Sub");
		System.out.println(NaN - NaN);
		System.out.println(NaN - POSITIVE_INFINITY);
		System.out.println(NaN - NEGATIVE_INFINITY);
		System.out.println(POSITIVE_INFINITY - POSITIVE_INFINITY);
		System.out.println(POSITIVE_INFINITY - NEGATIVE_INFINITY);
		System.out.println(NEGATIVE_INFINITY - NEGATIVE_INFINITY);

		System.out.println("Mul");
		System.out.println(NaN * NaN);
		System.out.println(NaN * POSITIVE_INFINITY);
		System.out.println(NaN * NEGATIVE_INFINITY);
		System.out.println(POSITIVE_INFINITY * POSITIVE_INFINITY);
		System.out.println(POSITIVE_INFINITY * NEGATIVE_INFINITY);
		System.out.println(NEGATIVE_INFINITY * NEGATIVE_INFINITY);

		System.out.println("Div");
		System.out.println(NaN / NaN);
		System.out.println(NaN / POSITIVE_INFINITY);
		System.out.println(NaN / NEGATIVE_INFINITY);
		System.out.println(POSITIVE_INFINITY / POSITIVE_INFINITY);
		System.out.println(POSITIVE_INFINITY / NEGATIVE_INFINITY);
		System.out.println(NEGATIVE_INFINITY / NEGATIVE_INFINITY);

		System.out.println("Mod");
		System.out.println(NaN % NaN);
		System.out.println(NaN % POSITIVE_INFINITY);
		System.out.println(NaN % NEGATIVE_INFINITY);
		System.out.println(POSITIVE_INFINITY % POSITIVE_INFINITY);
		System.out.println(POSITIVE_INFINITY % NEGATIVE_INFINITY);
		System.out.println(NEGATIVE_INFINITY % NEGATIVE_INFINITY);
	}

	@Test
	public void testAddSpecialCase1() throws Exception {
		assertQueryEquals("Add", "Infinity + 1.5", POSITIVE_INFINITY);
		assertQueryEquals("Add", "add(Infinity, 1.5)", POSITIVE_INFINITY);
	}

	@Test
	public void testAddSpecialCase2() throws Exception {
		assertQueryEquals("Add", "6 + Infinity", POSITIVE_INFINITY);
		assertQueryEquals("Add", "add(6, Infinity)", POSITIVE_INFINITY);
	}

	@Test
	public void testAddSpecialCase3() throws Exception {
		assertQueryEquals("Add", "-Infinity + 1.5", NEGATIVE_INFINITY);
		assertQueryEquals("Add", "add(-Infinity, 1.5)", NEGATIVE_INFINITY);
	}

	@Test
	public void testAddSpecialCase4() throws Exception {
		assertQueryEquals("Add", "6 + -Infinity", NEGATIVE_INFINITY);
		assertQueryEquals("Add", "add(6, -Infinity)", NEGATIVE_INFINITY);
	}

	@Test
	public void testAddSpecialCase5() throws Exception {
		assertQueryEquals("Add", "NaN + 1.5", NaN);
		assertQueryEquals("Add", "add(-NaN, 1.5)", NEGATIVE_INFINITY);
	}

	@Test
	public void testAddSpecialCase6() throws Exception {
		assertQueryEquals("Add", "6 + -NaN", NaN);
		assertQueryEquals("Add", "add(6, -NaN)", NaN);

	}

	@Test
	public void testAddSpecialCases() throws Exception {

		assertQueryEquals("Add", "Infinity + Infinity", POSITIVE_INFINITY);
		assertQueryEquals("Add", "-Infinity + -Infinity", NEGATIVE_INFINITY);
		assertQueryEquals("Add", "-NaN + -NaN", NaN);
		assertQueryEquals("Add", "Infinity + -Infinity", NaN);
		assertQueryEquals("Add", "Infinity + NaN", NaN);
		assertQueryEquals("Add", "-Infinity + NaN", NaN);

		// As functions
		assertQueryEquals("Add", "add(Infinity, Infinity)", POSITIVE_INFINITY);
		assertQueryEquals("Add", "add(-Infinity, -Infinity)", NEGATIVE_INFINITY);
		assertQueryEquals("Add", "add(-NaN, -NaN)", NaN);
		assertQueryEquals("Add", "add(Infinity, -Infinity)", NaN);
		assertQueryEquals("Add", "add(Infinity, NaN)", NaN);
		assertQueryEquals("Add", "add(-Infinity, NaN)", NaN);
	}

	@Test
	public void testDiv1() throws Exception {
		assertQueryEquals("Div", "3 / 0", POSITIVE_INFINITY);
		assertQueryEquals("Div", "div(3, 0)", POSITIVE_INFINITY);
	}

	@Test
	public void testDiv2() throws Exception {
		assertQueryEquals("Div", "-3 / 0", NEGATIVE_INFINITY);
		assertQueryEquals("Div", "div(-3, 0)", NEGATIVE_INFINITY);
	}

	@Test
	public void testDiv3() throws Exception {
		assertQueryEquals("Div", "0 / 3", 0.0);
		assertQueryEquals("Div", "div(0, 3)", 0.0);
	}

	@Test
	public void testDiv4() throws Exception {
		assertQueryEquals("Div", "0 / 3.5", 0.0);
		assertQueryEquals("Div", "div(0, 3.5)", 0.0);
	}

	@Test
	public void testDiv5() throws Exception {
		assertQueryEquals("Div", "3 / 1", 3.0);
		assertQueryEquals("Div", "div(3, 1)", 3.0);
	}

	@Test
	public void testDiv6() throws Exception {
		assertQueryEquals("Div", "3 / 7", 3 / 7.0);
		assertQueryEquals("Div", "div(3, 7)", 3 / 7.0);
	}

	@Test
	public void testDiv7() throws Exception {
		assertQueryEquals("Div", "Infinity / 7", POSITIVE_INFINITY);
		assertQueryEquals("Div", "div(Infinity, 7)", POSITIVE_INFINITY);
	}

	@Test
	public void testDiv9() throws Exception {
		assertQueryEquals("Div", "3 / Infinity", POSITIVE_INFINITY);
		assertQueryEquals("Div", "div(3, Infinity)", POSITIVE_INFINITY);
	}

	@Test
	public void testDiv10() throws Exception {
		assertQueryEquals("Div", "Infinity / Infinity", POSITIVE_INFINITY);
		assertQueryEquals("Div", "div(Infinity, Infinity)", POSITIVE_INFINITY);
	}

	@Test
	public void testDiv11() throws Exception {
		assertQueryEquals("Div", "-Infinity / 7", NaN);
		assertQueryEquals("Div", "div(-Infinity, 7)", NaN);
	}

	@Test
	public void testDiv12() throws Exception {
		assertQueryEquals("Div", "3 / -Infinity", NaN);
		assertQueryEquals("Div", "div(3, -Infinity)", NaN);
	}

	@Test
	public void testDiv13() throws Exception {
		assertQueryEquals("Div", "-Infinity / -Infinity", NaN);
		assertQueryEquals("Div", "div(-Infinity, -Infinity)", NaN);
	}

	@Test
	public void testDiv14() throws Exception {
		assertQueryEquals("Div", "NaN / 7", NaN);
		assertQueryEquals("Div", "div(Nan, 7)", NaN);
	}

	@Test
	public void testDiv15() throws Exception {
		assertQueryEquals("Div", "3 / NaN", NaN);
		assertQueryEquals("Div", "div(3, NaN)", NaN);
	}

	@Test
	public void testDiv16() throws Exception {
		assertQueryEquals("Div", "NaN / NaN", NaN);
		assertQueryEquals("Div", "div(Nan, NaN)", NaN);
	}

	@Test
	public void testMod1() throws Exception {
		assertQueryEquals("Mod", "9.5 % 2", 1.5);
		assertQueryEquals("Mod", "mod(9.5, 2)", 1.5);
	}

	@Test
	public void testMod2() throws Exception {
		assertQueryEquals("Mod", "9 % 2", 1);
		assertQueryEquals("Mod", "mod(9, 2)", 1);
	}

	@Test
	public void testMod3() throws Exception {
		assertQueryEquals("Mod", "-9 % 2", -1);
		assertQueryEquals("Mod", "mod(-9, 2)", -1);
	}

	@Test
	public void testMod4() throws Exception {
		assertQueryEquals("Mod", "9 % 3", 0);
		assertQueryEquals("Mod", "mod(9, 3)", 0);
	}

	@Test
	public void testMul() throws Exception {
		assertQueryEquals("Mul", "6 * 1.5", 9.0);
	}

	@Test
	public void testMultiplicative() throws Exception {
		assertQueryEquals("Several Operations", "100 / 10 / 5 * 2", 4.0);
	}

	@Test
	public void testNeg() throws Exception {
		String queryString = "let x:= list (5..13) in from i:x report -i end";
		JValue result = evalTestQuery("UMinus", queryString);
		assertEquals(9, result.toCollection().size());
		int sum = 0;
		for (JValue j : result.toCollection()) {
			sum += j.toInteger();
		}
		assertEquals(-81, sum);
	}

	@Test
	public void testSub1() throws Exception {
		assertQueryEquals("Sub", "6 - -1.5", 7.5);
	}

	@Test
	public void testSub2() throws Exception {
		assertQueryEquals("Sub", "6 - 3", 3L);
	}

	@Test
	public void testSub3() throws Exception {
		assertQueryEquals("Sub", "16 - 323", -307L);
	}

	@Test
	public void testSub4() throws Exception {
		assertQueryEquals("Sub", "1.5 - 6", -4.5);
	}

	@Test
	public void testSub5() throws Exception {
		assertQueryEquals("Sub", "10 - 4 - 3 - 2", 1);
	}

}
