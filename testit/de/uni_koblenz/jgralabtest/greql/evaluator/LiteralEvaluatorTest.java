package de.uni_koblenz.jgralabtest.greql.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.executable.ExecutableQuery;
import de.uni_koblenz.jgralab.greql.executable.GreqlCodeGenerator;
import de.uni_koblenz.jgralab.greql.types.Undefined;

public class LiteralEvaluatorTest {

	/**
	 * Test of query:<br>
	 * &epsilon;
	 * 
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@Test
	public void testEmptyQuery() throws InstantiationException,
			IllegalAccessException {
		String query = "";
		Object result = GreqlQuery.createQuery(query).evaluate();
		assertEquals(Undefined.UNDEFINED, result);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null, "testdata.TestEmptyQuery");
		assertEquals(result, generatedQuery.newInstance().execute(null));
	}

	/*
	 * UndefinedLiteral
	 */

	@Test
	public void testUndefinedLiteral() throws InstantiationException,
			IllegalAccessException {
		String query = "undefined";
		Object result = GreqlQuery.createQuery(query).evaluate();
		assertEquals(Undefined.UNDEFINED, result);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null, "testdata.TestUndefinedLiteral");
		assertEquals(result, generatedQuery.newInstance().execute(null));
	}

	/*
	 * Tests of BoolLiteralEvaluator
	 */

	/**
	 * Test of query:<br>
	 * true
	 * 
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@Test
	public void testBoolLiteralEvaluator_True() throws InstantiationException,
			IllegalAccessException {
		String query = "true";
		Object result = GreqlQuery.createQuery(query).evaluate();
		assertTrue((Boolean) result);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestBoolLiteralEvaluatorTrue");
		assertEquals(result, generatedQuery.newInstance().execute(null));
	}

	/**
	 * Test of query:<br>
	 * false
	 */
	@Test
	public void testBoolLiteralEvaluator_False() throws InstantiationException,
			IllegalAccessException {
		String query = "false";
		Object result = GreqlQuery.createQuery(query).evaluate();
		assertFalse((Boolean) result);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestBoolLiteralEvaluatorFalse");
		assertEquals(result, generatedQuery.newInstance().execute(null));
	}

	/*
	 * Tests of DoubleLiteralEvaluator
	 */

	/**
	 * Test of query:<br>
	 * 1.0
	 */
	@Test
	public void testDoubleLiteralEvaluator_DottedNotation()
			throws InstantiationException, IllegalAccessException {
		String query = "1.0";
		Object result = GreqlQuery.createQuery(query).evaluate();
		assertEquals(1d, result);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestDoubleLiteralEvaluatorDottedNotation");
		assertEquals(result, generatedQuery.newInstance().execute(null));
	}

	// /**
	// * Test of negative double value:<br>
	// * -10d
	// */
	// @Test
	// public void testDoubleLiteralEvaluator_WithSuffix() {
	// assertEquals(-10d, evaluateQuery("-10d"));
	// }

	/**
	 * Test of exponential double value:<br>
	 * 23e-7
	 */
	@Test
	public void testDoubleLiteralEvaluator_Exponential()
			throws InstantiationException, IllegalAccessException {
		String query = "23e-7";
		Object result = GreqlQuery.createQuery(query).evaluate();
		assertEquals(23e-7, result);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestDoubleLiteralEvaluatorExponential");
		assertEquals(result, generatedQuery.newInstance().execute(null));
	}

	// /**
	// * Test of query:<br>
	// * POSITIVE_INFINITY
	// */
	// @Test
	// public void testDoubleLiteralEvaluator_PositivInfinity() {
	// assertEquals(Double.POSITIVE_INFINITY,
	// evaluateQuery("POSITIVE_INFINITY"));
	// }

	// /**
	// * Test of query:<br>
	// * NEGATIVE_INFINITY
	// */
	// @Test
	// public void testDoubleLiteralEvaluator_NegativInfinity() {
	// assertEquals(Double.NEGATIVE_INFINITY,
	// evaluateQuery("NEGATIVE_INFINITY"));
	// }

	// /**
	// * Test of query:<br>
	// * NaN
	// */
	// @Test
	// public void testDoubleLiteralEvaluator_NaN() {
	// assertEquals(Double.NaN, evaluateQuery("NaN"));
	// }

	/*
	 * Tests of IntLiteralEvaluator
	 */

	/**
	 * Test of query:<br>
	 * 12
	 */
	@Test
	public void testIntLiteralEvaluator() throws InstantiationException,
			IllegalAccessException {
		String query = "12";
		Object result = GreqlQuery.createQuery(query).evaluate();
		assertEquals(12, result);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null, "testdata.TestIntLiteralEvaluator");
		assertEquals(result, generatedQuery.newInstance().execute(null));
	}

	/**
	 * Test of query:<br>
	 * -051
	 */
	@Test
	public void testIntLiteralEvaluator_OctalNotation()
			throws InstantiationException, IllegalAccessException {
		String query = "-051";
		Object result = GreqlQuery.createQuery(query).evaluate();
		assertEquals(-051, result);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestIntLiteralEvaluatorOctalNotation");
		assertEquals(result, generatedQuery.newInstance().execute(null));
	}

	/**
	 * Test of query:<br>
	 * 0x2f
	 */
	@Test
	public void testIntLiteralEvaluator_HexaNotation()
			throws InstantiationException, IllegalAccessException {
		String query = "0x2f";
		Object result = GreqlQuery.createQuery(query).evaluate();
		assertEquals(0x2f, result);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestIntLiteralEvaluatorHexaNotation");
		assertEquals(result, generatedQuery.newInstance().execute(null));
	}

	/*
	 * Tests of LongLiteralEvaluator
	 */

	/**
	 * Test of query (Long.MAX_VALUE):<br>
	 * 9223372036854775807
	 */
	@Test
	public void testLongLiteralEvaluator() throws InstantiationException,
			IllegalAccessException {
		String query = new Long(Long.MAX_VALUE).toString();
		Object result = GreqlQuery.createQuery(query).evaluate();
		assertEquals(Long.MAX_VALUE, result);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null, "testdata.TestLongLiteralEvaluator");
		assertEquals(result, generatedQuery.newInstance().execute(null));
	}

	// /**
	// * Test of query:<br>
	// * 12L
	// */
	// @Test
	// public void testLongLiteralEvaluator_withSuffix() {
	// assertEquals(12L, evaluateQuery("12L"));
	// }

	// /**
	// * Test of query:<br>
	// * -051L
	// */
	// @Test
	// public void testLongLiteralEvaluator_OctalNotation() {
	// assertEquals(-051L, evaluateQuery("-051L"));
	// }

	/**
	 * Test of query:<br>
	 * 0xfffffffff
	 */
	@Test
	public void testLongLiteralEvaluator_HexaNotation()
			throws InstantiationException, IllegalAccessException {
		String query = "0xfffffffff";
		Object result = GreqlQuery.createQuery(query).evaluate();
		assertEquals(0xfffffffffL, result);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestLongLiteralEvaluatorHexaNotation");
		assertEquals(result, generatedQuery.newInstance().execute(null));
	}

	// /**
	// * Test of query:<br>
	// * 0xfl
	// */
	// @Test
	// public void testLongLiteralEvaluator_HexaNotationWithSuffix() {
	// assertEquals(0xfl, evaluateQuery("0xfl"));
	// }

	/*
	 * Tests of StringLiteralEvaluator
	 */

	/**
	 * Test of query:<br>
	 * "this is a test<br>
	 * \"String\""
	 */
	@Test
	public void testStringLiteralEvaluator() throws InstantiationException,
			IllegalAccessException {
		String query = "\"this is a test aöäßAÖÜ \r\n\t\\\"String\\\"\"";
		Object result = GreqlQuery.createQuery(query).evaluate();
		assertEquals("this is a test aöäßAÖÜ \r\n\t\"String\"", result);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestStringLiteralEvaluator");
		assertEquals(result, generatedQuery.newInstance().execute(null));
	}

}
