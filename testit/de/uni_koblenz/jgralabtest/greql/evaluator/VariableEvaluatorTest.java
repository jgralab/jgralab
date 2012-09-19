package de.uni_koblenz.jgralabtest.greql.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.greql.exception.UndefinedVariableException;
import de.uni_koblenz.jgralab.greql.executable.ExecutableQuery;
import de.uni_koblenz.jgralab.greql.executable.GreqlCodeGenerator;

public class VariableEvaluatorTest {

	public GreqlQuery createQueryClass(String query, String classname)
			throws InstantiationException, IllegalAccessException {
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null, classname);
		return (GreqlQuery) generatedQuery.newInstance();
	}

	/**
	 * Test if a variable defined via the query<br>
	 * 1 store as varX<br>
	 * can be used in the query<br>
	 * using varX: varX
	 * 
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@Test
	public void testStoreAsAndUsing() throws InstantiationException,
			IllegalAccessException {
		GreqlEnvironment environment = new GreqlEnvironmentAdapter();

		String queryText1 = "1 store as varX";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText1),
				createQueryClass(queryText1, "testdata.TestStoreAsAndUsing1") }) {
			Object erg = query.evaluate(null, environment);
			assertNotNull(erg);
			assertEquals(1, erg);

			assertEquals(1, environment.getVariable("varX"));
		}

		String queryText2 = "using varX: varX";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText2),
				createQueryClass(queryText2, "testdata.TestStoreAsAndUsing2") }) {
			Object erg = query.evaluate(null, environment);
			assertNotNull(erg);
			assertEquals(1, erg);
		}
	}

	/**
	 * Tests whether an {@link UndefinedVariableException} is thrown if an
	 * undefined variable is used.
	 */
	@Test(expected = UndefinedVariableException.class)
	public void testUndefinedVariable() {
		GreqlQuery.createQuery("using varX: varX").evaluate();
	}

	/**
	 * Tests whether an {@link UndefinedVariableException} is thrown if an
	 * undefined variable is used.
	 * 
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@Test(expected = UndefinedVariableException.class)
	public void testUndefinedVariableGenerated() throws InstantiationException,
			IllegalAccessException {
		createQueryClass("using varX: varX",
				"testdata.TestUndefinedVariableGenerated").evaluate();
	}

	/**
	 * Tests the query:<br>
	 * let x:=10, y:=12 in x+y
	 * 
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@Test
	public void testLet() throws GraphIOException, InstantiationException,
			IllegalAccessException {
		String queryText = "let x:=10, y:=12 in x+y";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				createQueryClass(queryText, "testdata.Let") }) {
			Object erg = query.evaluate();
			assertEquals(22, erg);
		}
	}

	@Test
	public void testSeveralLet() throws GraphIOException,
			InstantiationException, IllegalAccessException {
		String queryText = "let x:=10 in let y:=x+2 in x+y";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				createQueryClass(queryText, "testdata.TestSeveralLet") }) {
			Object erg = query.evaluate();
			assertEquals(22, erg);
		}
	}

	/**
	 * Tests the query:<br>
	 * x+y where x:=10, y:=12
	 * 
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@Test
	public void testWhere() throws InstantiationException,
			IllegalAccessException {
		String queryText = "x+y where x:=10, y:=12";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				createQueryClass(queryText, "testdata.TestWhere") }) {
			Object erg = query.evaluate();
			assertEquals(22, erg);
		}
	}

}
