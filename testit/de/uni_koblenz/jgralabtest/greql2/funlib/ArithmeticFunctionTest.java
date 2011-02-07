package de.uni_koblenz.jgralabtest.greql2.funlib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;

public class ArithmeticFunctionTest extends GenericTests {

	private static final double DELTA = 0.00000001;

	public void testArithmeticOperationAsDouble(String operationSign,
			double arg1, double arg2, double expected) throws Exception {

		String queryString = arg1 + " " + operationSign + " " + arg2;
		JValue result = evalTestQuery(operationSign, queryString);
		assertEquals(expected, result.toDouble().doubleValue(), DELTA);
	}

	public void testArithmeticOperationAsLong(String operationSign, long arg1,
			long arg2, long expected) throws Exception {
		String queryString = arg1 + " " + operationSign + " " + arg2;
		JValue result = evalTestQuery(operationSign, queryString);
		assertEquals(expected, result.toLong().longValue());
	}

	public void testArithmeticOperationAsInteger(String operationSign,
			int arg1, int arg2, int expected) throws Exception {
		String queryString = arg1 + " " + operationSign + " " + arg2;
		JValue result = evalTestQuery(operationSign, queryString);
		assertEquals(expected, result.toInteger().intValue());
	}

	@Test
	public void testAdd1() throws Exception {
		testArithmeticOperationAsDouble("+", 6, 1.5, 7.5);
	}

	@Test
	public void testAdd2() throws Exception {
		testArithmeticOperationAsDouble("+", 6, -1.5, 4.5);
	}

	@Test
	public void testAdd3() throws Exception {
		testArithmeticOperationAsDouble("+", 0.025, 0.975, 1);
	}

	@Test
	public void testDiv1() throws Exception {
		testArithmeticOperationAsDouble("/", 3, 3, 1);
	}

	@Test
	public void testDiv2() throws Exception {
		testArithmeticOperationAsDouble("/", 3, 1, 3);
	}

	@Test
	public void testDiv3() throws Exception {
		testArithmeticOperationAsDouble("/", 3, 7, 3 / 7.0);
	}

	@Test
	public void testSub1() throws Exception {
		testArithmeticOperationAsDouble("-", 6, -1.5, 7.5);
	}

	@Test
	public void testSub2() throws Exception {
		testArithmeticOperationAsLong("-", 6, 3, 3);
	}

	@Test
	public void testSub3() throws Exception {
		testArithmeticOperationAsLong("-", 16, 323, -307);
	}

	@Test
	public void testSub4() throws Exception {
		testArithmeticOperationAsDouble("-", 1.5, 6, -4.5);
	}

	@Test
	public void testSub5() throws Exception {
		String queryString = "10 - 4 - 3 - 2";
		JValue result = evalTestQuery("Sub5", queryString);
		assertEquals(Integer.valueOf(1), result.toInteger());
	}

	@Test
	public void testMod1() throws Exception {
		testArithmeticOperationAsInteger("%", 9, 2, 1);
	}

	@Test
	public void testMod2() throws Exception {
		testArithmeticOperationAsInteger("%", -9, 2, -1);
	}

	@Test
	public void testMod3() throws Exception {
		testArithmeticOperationAsInteger("%", 9, 3, 0);
	}

	@Test
	public void testMul() throws Exception {
		testArithmeticOperationAsDouble("*", 6, 1.5, 9);
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
	public void testMultiplicative() throws Exception {
		String queryString = "100 / 10 / 5 * 2";
		JValue result = evalTestQuery("Mod", queryString);
		assertEquals(Double.valueOf(4), result.toDouble(), 0.01);
	}

}
