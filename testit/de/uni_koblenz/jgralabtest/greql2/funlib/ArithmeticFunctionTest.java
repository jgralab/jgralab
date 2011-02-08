package de.uni_koblenz.jgralabtest.greql2.funlib;

import static org.junit.Assert.assertEquals;
import static java.lang.Double.NaN;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;

public class ArithmeticFunctionTest extends GenericTests {

	private static final String MUL = "Mul";
	private static final String SUB = "Sub";
	private static final String MOD = "Mod";
	private static final String DIV = "Div";
	private static final String ADD = "Add";

	@Test
	public void testAdd1() throws Exception {
		assertQueryEquals(ADD, "0 + 1.5", 1.5);
		assertQueryEquals(ADD, "add(0, 1.5)", 1.5);
	}

	@Test
	public void testAdd2() throws Exception {
		assertQueryEquals(ADD, "6 + 0", 6);
		assertQueryEquals(ADD, "add(6, 0)", 6);
	}

	@Test
	public void testAdd3() throws Exception {
		assertQueryEquals(ADD, "0 + 0", 0);
		assertQueryEquals(ADD, "add(0, 0)", 0);
	}

	@Test
	public void testAdd4() throws Exception {
		assertQueryEquals(ADD, "6 + 1.5", 7.5);
		assertQueryEquals(ADD, "add(6, 1.5)", 7.5);
	}

	@Test
	public void testAdd5() throws Exception {
		assertQueryEquals(ADD, "6 + -1.5", 4.5);
		assertQueryEquals(ADD, "add(6, -1.5)", 4.5);
	}

	@Test
	public void testAdd6() throws Exception {
		assertQueryEquals(ADD, "0.025 + 0.975", 1.0);
		assertQueryEquals(ADD, "add(0.025, 0.975)", 1.0);

		System.out.println(ADD);
		System.out.println(NaN + NaN);
		System.out.println(NaN + POSITIVE_INFINITY);
		System.out.println(NaN + NEGATIVE_INFINITY);
		System.out.println(POSITIVE_INFINITY + POSITIVE_INFINITY);
		System.out.println(POSITIVE_INFINITY + NEGATIVE_INFINITY);
		System.out.println(NEGATIVE_INFINITY + NEGATIVE_INFINITY);

		System.out.println(SUB);
		System.out.println(NaN - NaN);
		System.out.println(NaN - POSITIVE_INFINITY);
		System.out.println(NaN - NEGATIVE_INFINITY);
		System.out.println(POSITIVE_INFINITY - POSITIVE_INFINITY);
		System.out.println(POSITIVE_INFINITY - NEGATIVE_INFINITY);
		System.out.println(NEGATIVE_INFINITY - NEGATIVE_INFINITY);

		System.out.println(MUL);
		System.out.println(NaN * NaN);
		System.out.println(NaN * POSITIVE_INFINITY);
		System.out.println(NaN * NEGATIVE_INFINITY);
		System.out.println(POSITIVE_INFINITY * POSITIVE_INFINITY);
		System.out.println(POSITIVE_INFINITY * NEGATIVE_INFINITY);
		System.out.println(NEGATIVE_INFINITY * NEGATIVE_INFINITY);

		System.out.println(DIV);
		System.out.println(NaN / NaN);
		System.out.println(NaN / POSITIVE_INFINITY);
		System.out.println(NaN / NEGATIVE_INFINITY);
		System.out.println(POSITIVE_INFINITY / POSITIVE_INFINITY);
		System.out.println(POSITIVE_INFINITY / NEGATIVE_INFINITY);
		System.out.println(NEGATIVE_INFINITY / NEGATIVE_INFINITY);

		System.out.println(MOD);
		System.out.println(NaN % NaN);
		System.out.println(NaN % POSITIVE_INFINITY);
		System.out.println(NaN % NEGATIVE_INFINITY);
		System.out.println(POSITIVE_INFINITY % POSITIVE_INFINITY);
		System.out.println(POSITIVE_INFINITY % NEGATIVE_INFINITY);
		System.out.println(NEGATIVE_INFINITY % NEGATIVE_INFINITY);
	}

	@Test
	public void testAddSpecialCase1() throws Exception {
		assertQueryEquals(ADD, "Infinity + 1.5", POSITIVE_INFINITY);
		assertQueryEquals(ADD, "add(Infinity, 1.5)", POSITIVE_INFINITY);
	}

	@Test
	public void testAddSpecialCase2() throws Exception {
		assertQueryEquals(ADD, "6 + Infinity", POSITIVE_INFINITY);
		assertQueryEquals(ADD, "add(6, Infinity)", POSITIVE_INFINITY);
	}

	@Test
	public void testAddSpecialCase3() throws Exception {
		assertQueryEquals(ADD, "-Infinity + 1.5", NEGATIVE_INFINITY);
		assertQueryEquals(ADD, "add(-Infinity, 1.5)", NEGATIVE_INFINITY);
	}

	@Test
	public void testAddSpecialCase4() throws Exception {
		assertQueryEquals(ADD, "6 + -Infinity", NEGATIVE_INFINITY);
		assertQueryEquals(ADD, "add(6, -Infinity)", NEGATIVE_INFINITY);
	}

	@Test
	public void testAddSpecialCase5() throws Exception {
		assertQueryEquals(ADD, "NaN + 1.5", NaN);
		assertQueryEquals(ADD, "add(-NaN, 1.5)", NEGATIVE_INFINITY);
	}

	@Test
	public void testAddSpecialCase6() throws Exception {
		assertQueryEquals(ADD, "6 + -NaN", NaN);
		assertQueryEquals(ADD, "add(6, -NaN)", NaN);

	}

	@Test
	public void testAddSpecialCases() throws Exception {

		assertQueryEquals(ADD, "Infinity + Infinity", POSITIVE_INFINITY);
		assertQueryEquals(ADD, "-Infinity + -Infinity", NEGATIVE_INFINITY);
		assertQueryEquals(ADD, "-NaN + -NaN", NaN);
		assertQueryEquals(ADD, "Infinity + -Infinity", NaN);
		assertQueryEquals(ADD, "Infinity + NaN", NaN);
		assertQueryEquals(ADD, "-Infinity + NaN", NaN);

		// As functions
		assertQueryEquals(ADD, "add(Infinity, Infinity)", POSITIVE_INFINITY);
		assertQueryEquals(ADD, "add(-Infinity, -Infinity)", NEGATIVE_INFINITY);
		assertQueryEquals(ADD, "add(-NaN, -NaN)", NaN);
		assertQueryEquals(ADD, "add(Infinity, -Infinity)", NaN);
		assertQueryEquals(ADD, "add(Infinity, NaN)", NaN);
		assertQueryEquals(ADD, "add(-Infinity, NaN)", NaN);
	}

	@Test
	public void testDiv1() throws Exception {
		assertQueryEquals(DIV, "3 / 0", POSITIVE_INFINITY);
		assertQueryEquals(DIV, "div(3, 0)", POSITIVE_INFINITY);
	}

	@Test
	public void testDiv2() throws Exception {
		assertQueryEquals(DIV, "-3 / 0", NEGATIVE_INFINITY);
		assertQueryEquals(DIV, "div(-3, 0)", NEGATIVE_INFINITY);
	}

	@Test
	public void testDiv3() throws Exception {
		assertQueryEquals(DIV, "0 / 3", 0.0);
		assertQueryEquals(DIV, "div(0, 3)", 0.0);
	}

	@Test
	public void testDiv4() throws Exception {
		assertQueryEquals(DIV, "0 / 3.5", 0.0);
		assertQueryEquals(DIV, "div(0, 3.5)", 0.0);
	}

	@Test
	public void testDiv5() throws Exception {
		assertQueryEquals(DIV, "3 / 1", 3.0);
		assertQueryEquals(DIV, "div(3, 1)", 3.0);
	}

	@Test
	public void testDiv6() throws Exception {
		assertQueryEquals(DIV, "3 / 7", 3 / 7.0);
		assertQueryEquals(DIV, "div(3, 7)", 3 / 7.0);
	}

	@Test
	public void testDivSpecialCase1() throws Exception {
		assertQueryEquals(DIV, "Infinity / 7", POSITIVE_INFINITY);
		assertQueryEquals(DIV, "div(Infinity, 7)", POSITIVE_INFINITY);
	}

	@Test
	public void testDivSpecialCase2() throws Exception {
		assertQueryEquals(DIV, "3 / Infinity", POSITIVE_INFINITY);
		assertQueryEquals(DIV, "div(3, Infinity)", POSITIVE_INFINITY);
	}

	@Test
	public void testDivSpecialCase3() throws Exception {
		assertQueryEquals(DIV, "-Infinity / 7", NaN);
		assertQueryEquals(DIV, "div(-Infinity, 7)", NaN);
	}

	@Test
	public void testDivSpecialCase4() throws Exception {
		assertQueryEquals(DIV, "3 / -Infinity", NaN);
		assertQueryEquals(DIV, "div(3, -Infinity)", NaN);
	}

	@Test
	public void testDivSpecialCase5() throws Exception {
		assertQueryEquals(DIV, "NaN / 7", NaN);
		assertQueryEquals(DIV, "div(Nan, 7)", NaN);
	}

	@Test
	public void testDivSpecialCase6() throws Exception {
		assertQueryEquals(DIV, "3 / NaN", NaN);
		assertQueryEquals(DIV, "div(3, NaN)", NaN);
	}

	@Test
	public void testDivSpecialCases() throws Exception {

		assertQueryEquals(DIV, "Infinity / Infinity", NaN);
		assertQueryEquals(DIV, "-Infinity / -Infinity", NaN);
		assertQueryEquals(DIV, "NaN - NaN", NaN);
		assertQueryEquals(DIV, "Infinity / -Infinity", NaN);
		assertQueryEquals(DIV, "Infinity / NaN", NaN);
		assertQueryEquals(DIV, "-Infinity / NaN", NaN);

		// As functions
		assertQueryEquals(DIV, "div(Infinity, Infinity)", NaN);
		assertQueryEquals(DIV, "div(-Infinity, -Infinity)", NaN);
		assertQueryEquals(DIV, "div(NaN, NaN)", NaN);
		assertQueryEquals(DIV, "div(Infinity, -Infinity)", NaN);
		assertQueryEquals(DIV, "div(Infinity, NaN)", NaN);
		assertQueryEquals(DIV, "div(-Infinity, NaN)", NaN);
	}

	@Test
	public void testMod1() throws Exception {
		assertQueryEquals(MOD, "9.5 % 2", 1.5);
		assertQueryEquals(MOD, "mod(9.5, 2)", 1.5);
	}

	@Test
	public void testMod2() throws Exception {
		assertQueryEquals(MOD, "9 % 2", 1);
		assertQueryEquals(MOD, "mod(9, 2)", 1);
	}

	@Test
	public void testMod3() throws Exception {
		assertQueryEquals(MOD, "-9 % 2", -1);
		assertQueryEquals(MOD, "mod(-9, 2)", -1);
	}

	@Test
	public void testMod4() throws Exception {
		assertQueryEquals(MOD, "9 % 3", 0);
		assertQueryEquals(MOD, "mod(9, 3)", 0);
	}

	@Test
	public void testModSpecialCase1() throws Exception {
		assertQueryEquals(MOD, "Infinity % 7", NaN);
		assertQueryEquals(MOD, "mod(Infinity, 7)", NaN);
	}

	@Test
	public void testModSpecialCase2() throws Exception {
		assertQueryEquals(MOD, "3 % Infinity", NaN);
		assertQueryEquals(MOD, "mod(3, Infinity)", NaN);
	}

	@Test
	public void testModSpecialCase3() throws Exception {
		assertQueryEquals(MOD, "-Infinity % 7", NaN);
		assertQueryEquals(MOD, "mod(-Infinity, 7)", NaN);
	}

	@Test
	public void testModSpecialCase4() throws Exception {
		assertQueryEquals(MOD, "3 % -Infinity", NaN);
		assertQueryEquals(MOD, "mod(3, -Infinity)", NaN);
	}

	@Test
	public void testModSpecialCase5() throws Exception {
		assertQueryEquals(MOD, "NaN % 7", NaN);
		assertQueryEquals(MOD, "mod(Nan, 7)", NaN);
	}

	@Test
	public void testModSpecialCase6() throws Exception {
		assertQueryEquals(MOD, "3 % NaN", NaN);
		assertQueryEquals(MOD, "mod(3, NaN)", NaN);
	}

	@Test
	public void testModSpecialCases() throws Exception {

		assertQueryEquals(MOD, "Infinity % Infinity", NaN);
		assertQueryEquals(MOD, "-Infinity % -Infinity", NaN);
		assertQueryEquals(MOD, "NaN % NaN", NaN);
		assertQueryEquals(MOD, "Infinity % -Infinity", NaN);
		assertQueryEquals(MOD, "Infinity % NaN", NaN);
		assertQueryEquals(MOD, "-Infinity % NaN", NaN);

		// As functions
		assertQueryEquals(MOD, "mod(Infinity, Infinity)", NaN);
		assertQueryEquals(MOD, "mod(-Infinity, -Infinity)", NaN);
		assertQueryEquals(MOD, "mod(NaN, NaN)", NaN);
		assertQueryEquals(MOD, "mod(Infinity, -Infinity)", NaN);
		assertQueryEquals(MOD, "mod(Infinity, NaN)", NaN);
		assertQueryEquals(MOD, "mod(-Infinity, NaN)", NaN);
	}

	@Test
	public void testMul1() throws Exception {
		assertQueryEquals(MUL, "6 * 1.5", 9.0);
		assertQueryEquals(MUL, "mul(6, 1.5)", 9.0);
	}

	@Test
	public void testMul2() throws Exception {
		assertQueryEquals(MUL, "0 * 1.5", 0.0);
		assertQueryEquals(MUL, "mul(0, 1.5)", 0.0);
	}

	@Test
	public void testMul3() throws Exception {
		assertQueryEquals(MUL, "6 * 0", 0.0);
		assertQueryEquals(MUL, "mul(6, 0)", 0.0);
	}

	@Test
	public void testMul4() throws Exception {
		assertQueryEquals(MUL, "0 * 0", 0.0);
		assertQueryEquals(MUL, "mul(0, 0)", 0.0);
	}

	@Test
	public void testMul5() throws Exception {
		assertQueryEquals(MUL, "1 * 1.5", 1.5);
		assertQueryEquals(MUL, "mul(1, 1.5)", 1.5);
	}

	@Test
	public void testMul6() throws Exception {
		assertQueryEquals(MUL, "6 * 1", 6.0);
		assertQueryEquals(MUL, "mul(6, 1)", 6.0);
	}

	@Test
	public void testMul7() throws Exception {
		assertQueryEquals(MUL, "1 * 1", 1.0);
		assertQueryEquals(MUL, "mul(1, 1)", 1.0);
	}

	@Test
	public void testMulSpecialCase1() throws Exception {
		assertQueryEquals(MUL, "Infinity * 7", POSITIVE_INFINITY);
		assertQueryEquals(MUL, "mul(Infinity, 7)", POSITIVE_INFINITY);
	}

	@Test
	public void testMulSpecialCase2() throws Exception {
		assertQueryEquals(MUL, "3 * Infinity", POSITIVE_INFINITY);
		assertQueryEquals(MUL, "mul(3, Infinity)", POSITIVE_INFINITY);
	}

	@Test
	public void testMulSpecialCase3() throws Exception {
		assertQueryEquals(MUL, "-Infinity * 7", NEGATIVE_INFINITY);
		assertQueryEquals(MUL, "mul(-Infinity, 7)", NEGATIVE_INFINITY);
	}

	@Test
	public void testMulSpecialCase4() throws Exception {
		assertQueryEquals(MUL, "3 * -Infinity", NEGATIVE_INFINITY);
		assertQueryEquals(MUL, "mul(3, -Infinity)", NEGATIVE_INFINITY);
	}

	@Test
	public void testMulSpecialCase5() throws Exception {
		assertQueryEquals(MUL, "NaN * 7", NaN);
		assertQueryEquals(MUL, "mul(Nan, 7)", NaN);
	}

	@Test
	public void testMulSpecialCase6() throws Exception {
		assertQueryEquals(MUL, "3 * NaN", NaN);
		assertQueryEquals(MUL, "mul(3, NaN)", NaN);
	}

	@Test
	public void testMulSpecialCases() throws Exception {

		assertQueryEquals(MUL, "Infinity * Infinity", POSITIVE_INFINITY);
		assertQueryEquals(MUL, "-Infinity * -Infinity", POSITIVE_INFINITY);
		assertQueryEquals(MUL, "NaN * NaN", NaN);
		assertQueryEquals(MUL, "Infinity * -Infinity", NEGATIVE_INFINITY);
		assertQueryEquals(MUL, "Infinity * NaN", NaN);
		assertQueryEquals(MUL, "-Infinity * NaN", NaN);

		// As functions
		assertQueryEquals(MUL, "mul(Infinity, Infinity)", POSITIVE_INFINITY);
		assertQueryEquals(MUL, "mul(-Infinity, -Infinity)", POSITIVE_INFINITY);
		assertQueryEquals(MUL, "mul(NaN, NaN)", NaN);
		assertQueryEquals(MUL, "mul(Infinity, -Infinity)", NEGATIVE_INFINITY);
		assertQueryEquals(MUL, "mul(Infinity, NaN)", NaN);
		assertQueryEquals(MUL, "mul(-Infinity, NaN)", NaN);
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
		assertQueryEquals(SUB, "6 - -1.5", 7.5);
		assertQueryEquals(SUB, "sub(6, -1.5)", 7.5);
	}

	@Test
	public void testSub2() throws Exception {
		assertQueryEquals(SUB, "6 - 3", 3L);
		assertQueryEquals(SUB, "sub(6, 3)", 3L);
	}

	@Test
	public void testSub3() throws Exception {
		assertQueryEquals(SUB, "16 - 323", -307L);
		assertQueryEquals(SUB, "sub(16, 323)", -307L);
	}

	@Test
	public void testSub4() throws Exception {
		assertQueryEquals(SUB, "1.5 - 6", -4.5);
		assertQueryEquals(SUB, "sub(1.5, 6)", -4.5);
	}

	@Test
	public void testSub5() throws Exception {
		assertQueryEquals(SUB, "10 - 4 - 3 - 2", 1);
		assertQueryEquals(SUB, "sub(sub(sub(10, 4), 3), 2)", 1);
	}

	@Test
	public void testSubSpecialCase1() throws Exception {
		assertQueryEquals(SUB, "Infinity - 7", POSITIVE_INFINITY);
		assertQueryEquals(SUB, "sub(Infinity, 7)", POSITIVE_INFINITY);
	}

	@Test
	public void testSubSpecialCase2() throws Exception {
		assertQueryEquals(SUB, "3 - Infinity", POSITIVE_INFINITY);
		assertQueryEquals(SUB, "sub(3, Infinity)", POSITIVE_INFINITY);
	}

	@Test
	public void testSubSpecialCase3() throws Exception {
		assertQueryEquals(SUB, "-Infinity - 7", NaN);
		assertQueryEquals(SUB, "sub(-Infinity, 7)", NaN);
	}

	@Test
	public void testSubSpecialCase4() throws Exception {
		assertQueryEquals(SUB, "3 - -Infinity", NaN);
		assertQueryEquals(SUB, "sub(3, -Infinity)", NaN);
	}

	@Test
	public void testSubSpecialCase5() throws Exception {
		assertQueryEquals(SUB, "NaN - 7", NaN);
		assertQueryEquals(SUB, "sub(Nan, 7)", NaN);
	}

	@Test
	public void testSubSpecialCase6() throws Exception {
		assertQueryEquals(SUB, "3 - NaN", NaN);
		assertQueryEquals(SUB, "sub(3, NaN)", NaN);
	}

	@Test
	public void testSubSpecialCases() throws Exception {

		assertQueryEquals(SUB, "Infinity - Infinity", NaN);
		assertQueryEquals(SUB, "-Infinity - -Infinity", NaN);
		assertQueryEquals(SUB, "NaN - NaN", NaN);
		assertQueryEquals(SUB, "Infinity - -Infinity", POSITIVE_INFINITY);
		assertQueryEquals(SUB, "Infinity - NaN", NaN);
		assertQueryEquals(SUB, "-Infinity - NaN", NaN);

		// As functions
		assertQueryEquals(SUB, "sub(Infinity, Infinity)", NaN);
		assertQueryEquals(SUB, "sub(-Infinity, -Infinity)", NaN);
		assertQueryEquals(SUB, "sub(NaN, NaN)", NaN);
		assertQueryEquals(SUB, "sub(Infinity, -Infinity)", POSITIVE_INFINITY);
		assertQueryEquals(SUB, "sub(Infinity, NaN)", NaN);
		assertQueryEquals(SUB, "sub(-Infinity, NaN)", NaN);
	}
}