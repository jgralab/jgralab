package de.uni_koblenz.jgralabtest.greql2.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluatorImpl;
import de.uni_koblenz.jgralab.greql2.evaluator.QueryImpl;
import de.uni_koblenz.jgralab.greql2.exception.UndefinedVariableException;

public class VariableEvaluatorTest {

	private Object evaluateQuery(String query) {
		return evaluateQuery(query, new GreqlEnvironmentAdapter());
	}

	private Object evaluateQuery(String query, GreqlEnvironment environment) {
		return new GreqlEvaluatorImpl(new QueryImpl(query), null, environment)
				.getResult();
	}

	/**
	 * Test if a variable defined via the query<br>
	 * 1 store as varX<br>
	 * can be used in the query<br>
	 * using varX: varX
	 */
	@Test
	public void testStoreAsAndUsing() {
		GreqlEnvironment environment = new GreqlEnvironmentAdapter();

		Object erg = evaluateQuery("1 store as varX", environment);
		assertNotNull(erg);
		assertEquals(1, erg);

		assertEquals(1, environment.getVariable("varX"));

		erg = evaluateQuery("using varX: varX", environment);
		assertNotNull(erg);
		assertEquals(1, erg);
	}

	/**
	 * Tests whether an {@link UndefinedVariableException} is thrown if an
	 * undefined variable is used.
	 */
	@Test(expected = UndefinedVariableException.class)
	public void testUndefinedVariable() {
		evaluateQuery("using varX: varX");
	}

	/**
	 * Tests the query:<br>
	 * let x:=10, y:=12 in x+y
	 */
	@Test
	public void testLet() throws GraphIOException {
		Object erg = evaluateQuery("let x:=10, y:=12 in x+y");
		assertEquals(22, erg);
	}

	/**
	 * Tests the query:<br>
	 * x+y where x:=10, y:=12
	 */
	@Test
	public void testWhere() {
		Object erg = evaluateQuery("x+y where x:=10, y:=12");
		assertEquals(22, erg);
	}

}
