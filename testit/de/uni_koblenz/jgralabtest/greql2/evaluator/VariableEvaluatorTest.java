package de.uni_koblenz.jgralabtest.greql2.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluatorImpl;
import de.uni_koblenz.jgralab.greql2.evaluator.QueryImpl;

public class VariableEvaluatorTest {

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
		return evaluateQuery(query, new GreqlEnvironmentAdapter());
	}

	private Object evaluateQuery(String query, GreqlEnvironment environment) {
		return new GreqlEvaluatorImpl(new QueryImpl(query), datagraph,
				environment).getResult();
	}

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

}
