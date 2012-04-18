package de.uni_koblenz.jgralabtest.greql2.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluatorImpl;
import de.uni_koblenz.jgralab.greql2.evaluator.QueryImpl;
import de.uni_koblenz.jgralab.greql2.types.Undefined;

public class LiteralEvaluatorTest {

	private Object evaluateQuery(String query) {
		return new GreqlEvaluatorImpl(new QueryImpl(query), null,
				new GreqlEnvironmentAdapter()).getResult();
	}

	/**
	 * Test of query:<br>
	 * &epsilon;
	 */
	@Test
	public void testEmptyQuery() {
		assertEquals(Undefined.UNDEFINED, evaluateQuery(""));
	}

	/*
	 * Tests of BoolLiteralEvaluator
	 */

	/**
	 * Test of query:<br>
	 * true
	 */
	@Test
	public void testBoolLiteralEvaluator_True() {
		assertTrue((Boolean) evaluateQuery("true"));
	}

	/**
	 * Test of query:<br>
	 * false
	 */
	@Test
	public void testBoolLiteralEvaluator_False() {
		assertFalse((Boolean) evaluateQuery("false"));
	}

	/*
	 * Tests of DoubleLiteralEvaluator
	 */

	/**
	 * Test of query:<br>
	 * 1.0
	 */
	@Test
	public void testDoubleLiteralEvaluator_DottedNotation() {
		assertEquals(1d, evaluateQuery("1.0"));
	}

	/**
	 * Test of negative double value:<br>
	 * -1L
	 */
	@Test
	public void testDoubleLiteralEvaluator_WithSuffix() {
		assertEquals(-10d, evaluateQuery("-10d"));
	}

	/**
	 * Test of exponential double value:<br>
	 * 23e-7
	 */
	@Test
	public void testDoubleLiteralEvaluator_Exponential() {
		assertEquals(23e-7, evaluateQuery("23e-7"));
	}

	/**
	 * Test of query:<br>
	 * POSITIVE_INFINITY
	 */
	@Test
	public void testDoubleLiteralEvaluator_PositivInfinity() {
		assertEquals(Double.POSITIVE_INFINITY,
				evaluateQuery("POSITIVE_INFINITY"));
	}

	/**
	 * Test of query:<br>
	 * NEGATIVE_INFINITY
	 */
	@Test
	public void testDoubleLiteralEvaluator_NegativInfinity() {
		assertEquals(Double.NEGATIVE_INFINITY,
				evaluateQuery("NEGATIVE_INFINITY"));
	}

	/**
	 * Test of query:<br>
	 * NaN
	 */
	@Test
	public void testDoubleLiteralEvaluator_NaN() {
		assertEquals(Double.NaN, evaluateQuery("NaN"));
	}

	/*
	 * Tests of IntLiteralEvaluator
	 */

	/**
	 * Test of query:<br>
	 * 12
	 */
	@Test
	public void testIntLiteralEvaluator() {
		assertEquals(12, evaluateQuery("12"));
	}

	/**
	 * Test of query:<br>
	 * -051
	 */
	@Test
	public void testIntLiteralEvaluator_OctalNotation() {
		assertEquals(-051, evaluateQuery("-051"));
	}

	/**
	 * Test of query:<br>
	 * 0x2f
	 */
	@Test
	public void testIntLiteralEvaluator_HexaNotation() {
		assertEquals(0x2f, evaluateQuery("0x2f"));
	}

	/*
	 * Tests of LongLiteralEvaluator
	 */

	/**
	 * Test of query (Long.MAX_VALUE):<br>
	 * 9223372036854775807
	 */
	@Test
	public void testLongLiteralEvaluator() {
		assertEquals(Long.MAX_VALUE,
				evaluateQuery(new Long(Long.MAX_VALUE).toString()));
	}

	/**
	 * Test of query:<br>
	 * 12L
	 */
	@Test
	public void testLongLiteralEvaluator_withSuffix() {
		assertEquals(12L, evaluateQuery("12L"));
	}

	/**
	 * Test of query:<br>
	 * -051L
	 */
	@Test
	public void testLongLiteralEvaluator_OctalNotation() {
		assertEquals(-051L, evaluateQuery("-051L"));
	}

	/**
	 * Test of query:<br>
	 * 0xfffffffff
	 */
	@Test
	public void testLongLiteralEvaluator_HexaNotation() {
		assertEquals(0xfffffffffL, evaluateQuery("0xfffffffff"));
	}

	/**
	 * Test of query:<br>
	 * 0xfl
	 */
	@Test
	public void testLongLiteralEvaluator_HexaNotationWithSuffix() {
		assertEquals(0xfl, evaluateQuery("0xfl"));
	}

	/*
	 * Tests of StringLiteralEvaluator
	 */

	/**
	 * Test of query:<br>
	 * "this is a test<br>
	 * \"String\""
	 */
	@Test
	public void testStringLiteralEvaluator() {
		assertEquals("this is a test \r\n\t\"String\"",
				evaluateQuery("\"this is a test \r\n\t\\\"String\\\"\""));
	}

}
