package de.uni_koblenz.jgralabtest.greql2.funlib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;

public class ArithmeticFunctionTest extends GenericTests {

	@Test
	public void testAdd() throws Exception {
		String queryString = "6 + 1.5";
		JValue result = evalTestQuery("Add", queryString);
		assertEquals(9, (int) ((result.toDouble() + 1.5)));
	}

	@Test
	public void testDiv() throws Exception {
		String queryString = "3/3";
		JValue result = evalTestQuery("Div", queryString);
		assertEquals(new JValueImpl(1.0), result);
	}

	@Test
	public void testDiv2() throws Exception {
		String queryString = "3.0/1";
		JValue result = evalTestQuery("Div", queryString);
		assertEquals(new JValueImpl(3.0), result);
	}

	@Test
	public void testDiv3() throws Exception {
		String queryString = "3/7";
		JValue result = evalTestQuery("Div", queryString);
		assertEquals(new JValueImpl(3.0 / 7), result);
	}

	@Test
	public void testSub() throws Exception {
		String queryString = "6 - 1.5";
		JValue result = evalTestQuery("Sub", queryString);
		assertEquals(4.5, result.toDouble(), 0.01);
	}

	@Test
	public void testSub2() throws Exception {
		String queryString = "6 - 3";
		JValue result = evalTestQuery("Sub", queryString);
		assertEquals(3l, (long) result.toLong());
	}

	@Test
	public void testSub3() throws Exception {
		String queryString = "16 - 323";
		JValue result = evalTestQuery("Sub", queryString);
		assertEquals(-307l, (long) result.toLong());
	}

	@Test
	public void testSub4() throws Exception {
		String queryString = "1.5 - 6";
		JValue result = evalTestQuery("Sub", queryString);
		assertEquals(-4.5, result.toDouble(), 0.01);
	}

	@Test
	public void testSub5() throws Exception {
		String queryString = "10 - 4 - 3 - 2";
		JValue result = evalTestQuery("Sub5", queryString);
		assertEquals(Integer.valueOf(1), result.toInteger());
	}

	@Test
	public void testMod() throws Exception {
		String queryString = "9 % 2";
		JValue result = evalTestQuery("Mod", queryString);
		assertEquals(Integer.valueOf(1), result.toInteger());
	}

	@Test
	public void testMul() throws Exception {
		String queryString = "6 * 1.5";
		JValue result = evalTestQuery("Mul", queryString);
		assertEquals(9, (int) (double) result.toDouble());
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
