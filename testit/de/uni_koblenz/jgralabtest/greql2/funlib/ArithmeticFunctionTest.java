package de.uni_koblenz.jgralabtest.greql2.funlib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;

public class ArithmeticFunctionTest extends GenericTests {

	// TODO create some methods named assertEqual receiving a greql string and
	// the expected value. This should make it easier to understand and create
	// new tests.

	@Test
	public void testAdd1() throws Exception {
		assertQueryEquals("Add", "6 + 1.5", 7.5);
	}

	@Test
	public void testAdd2() throws Exception {
		assertQueryEquals("Add", "6 + -1.5", 4.5);
	}

	@Test
	public void testAdd3() throws Exception {
		assertQueryEquals("Add", "0.025 + 0.975", 1.0);
	}

	@Test
	public void testDiv1() throws Exception {
		assertQueryEquals("Div", "3 / 3", 1.0);
	}

	@Test
	public void testDiv2() throws Exception {
		assertQueryEquals("Div", "3 / 1", 3.0);
	}

	@Test
	public void testDiv3() throws Exception {
		assertQueryEquals("Div", "3 / 7", 3 / 7.0);
	}

	@Test
	public void testMod1() throws Exception {
		assertQueryEquals("Sub", "9 % 2", 1);
	}

	@Test
	public void testMod2() throws Exception {
		assertQueryEquals("Mod", "-9 % 2", -1);
	}

	@Test
	public void testMod3() throws Exception {
		assertQueryEquals("Mod", "9 % 3", 0);
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
