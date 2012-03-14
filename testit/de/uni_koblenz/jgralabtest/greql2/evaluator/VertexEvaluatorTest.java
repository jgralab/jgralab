/**
 * 
 */
package de.uni_koblenz.jgralabtest.greql2.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluatorImpl;
import de.uni_koblenz.jgralab.greql2.evaluator.QueryImpl;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.types.Undefined;

/**
 * Test for all {@link VertexEvaluator}s. Used {@link Graph} is the
 * greqltestgraph.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class VertexEvaluatorTest {

	private static Graph datagraph;

	@BeforeClass
	public static void setUpBeforeClass() throws GraphIOException {
		datagraph = GraphIO.loadGraphFromFile(
				"./testit/testgraphs/greqltestgraph.tg",
				ImplementationType.STANDARD, null);
	}

	@AfterClass
	public static void tearDownAfterClass() {
		datagraph = null;
	}

	private Object evaluateQuery(String query) {
		return new GreqlEvaluatorImpl(new QueryImpl(query), datagraph,
				new HashMap<String, Object>()).getResult();
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
	 * Test of query:<br>
	 * 12L
	 */
	@Test
	public void testLongLiteralEvaluator() {
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
	 * fffffffffh
	 */
	@Test
	public void testLongLiteralEvaluator_HexaNotation() {
		assertEquals(0xfffffffffL, evaluateQuery("fffffffffh"));
	}

}
