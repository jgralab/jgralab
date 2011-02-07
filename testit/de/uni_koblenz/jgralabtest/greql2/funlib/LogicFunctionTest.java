package de.uni_koblenz.jgralabtest.greql2.funlib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;

public class LogicFunctionTest extends GenericTests {

	/*
	 * Test method for the GReQL function 'and'.
	 */
	@Test
	public void testAnd() throws Exception {
		String queryString = "and(true, false)";
		JValue result = evalTestQuery("and", queryString);
	}

	/*
	 * Test method for the GReQL function 'and' as infix.
	 */
	@Test
	public void testAndInfix() throws Exception {
		String queryString = "from el:list(1..100) "
				+ "with el % 2 = 0 and el % 3 = 0 report el end";
		JValue result = evalTestQuery("and", queryString);
		assertEquals(16, result.toCollection().size());
	}

	/*
	 * Test method for the GReQL function 'not'.
	 */
	@Test
	public void testNot() throws Exception {
		String queryString = "from el:list(1..100) "
				+ "with not (el % 2 = 0) report el end";
		JValue result = evalTestQuery("or", queryString);
		assertEquals(50, result.toCollection().size());
	}

	/*
	 * Test method for the GReQL function 'not' as prefix.
	 */
	@Test
	public void testNotPrefix() throws Exception {
		String queryString = "from el:list(1..100) "
				+ "with not(el % 2 = 0) report el end";
		JValue result = evalTestQuery("or", queryString);
		assertEquals(50, result.toCollection().size());
	}

	/*
	 * Test method for the GReQL function 'or'.
	 */
	@Test
	public void testOr() throws Exception {
		String queryString = "from el:list(1..100) "
				+ "with el % 2 = 0 or el % 3 = 0 report el end";
		JValue result = evalTestQuery("or", queryString);
		assertEquals(50 + 17, result.toCollection().size());
	}

	/*
	 * Test method for the GReQL function 'or'.
	 */
	@Test
	public void testOrInfix() throws Exception {
		String queryString = "from el:list(1..100) "
				+ "with or(el % 2 = 0, el % 3 = 0) report el end";
		JValue result = evalTestQuery("or", queryString);
		assertEquals(50 + 17, result.toCollection().size());
	}

	/*
	 * Test method for the GReQL function 'xor'.
	 */
	@Test
	public void testXor() throws Exception {
		String queryString = "from el:list(1..100) "
				+ "with el % 2 = 0 xor el % 3 = 0 report el end";
		JValue result = evalTestQuery("or", queryString);
		assertEquals(50 + 17 - 16, result.toCollection().size());
	}

	/*
	 * Test method for the GReQL function 'xor'.
	 */
	@Test
	public void testXorInfix() throws Exception {
		String queryString = "from el:list(1..100) "
				+ "with xor(el % 2 = 0, el % 3 = 0) report el end";
		JValue result = evalTestQuery("or", queryString);
		assertEquals(50 + 17 - 16, result.toCollection().size());
	}
}
