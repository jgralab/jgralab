package de.uni_koblenz.jgralabtest.greql.parallel;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.exception.GreqlException;
import de.uni_koblenz.jgralab.greql.executable.ExecutableQuery;
import de.uni_koblenz.jgralab.greql.executable.GreqlCodeGenerator;
import de.uni_koblenz.jgralab.greql.parallel.ParallelGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.parallel.ParallelGreqlEvaluator.TaskHandle;

public class ParallelTest {
	private static GreqlQuery q1, q2, q3, q4, q5, q6, q7, q8, q9, q10;

	private ParallelGreqlEvaluator pge;
	private TaskHandle h3, h7;

	@BeforeClass
	public static void createQueries() {
		q1 = GreqlQuery.createQuery("using xo: xo + 20 store as hv");
		q2 = GreqlQuery.createQuery("using vk: vk + 78 store as qf");
		q3 = GreqlQuery.createQuery("96 store as vk");
		q4 = GreqlQuery.createQuery("using xo: xo + 76 store as ae");
		q5 = GreqlQuery.createQuery("using ae, xo: ae + xo + 44 store as vu");
		q6 = GreqlQuery.createQuery("using vk: vk + 48 store as erg1");
		q7 = GreqlQuery
				.createQuery("using ya, ae, vu: ya + ae + vu + 4 store as erg3");
		q8 = GreqlQuery.createQuery("using xo: xo + 24 store as ya");
		q9 = GreqlQuery.createQuery("using xo, hv: xo + hv + 38 store as erg2");
		q10 = GreqlQuery.createQuery("using vk: vk + 63 store as xo");
	}

	@Before
	public void test() {
		pge = new ParallelGreqlEvaluator();
		pge.addGreqlQuery(q1);
		pge.addGreqlQuery(q2);
		h3 = pge.addGreqlQuery(q3);
		pge.addGreqlQuery(q4);
		pge.addGreqlQuery(q5);
		pge.addGreqlQuery(q6);
		h7 = pge.addGreqlQuery(q7);
		pge.addGreqlQuery(q8);
		pge.addGreqlQuery(q9);
		pge.addGreqlQuery(q10);
	}

	@Test
	public void executionTest() {
		GreqlEnvironment environment = pge.evaluate().getGreqlEnvironment();
		assertEquals(96, environment.getVariable("vk"));
		assertEquals(96 + 48, environment.getVariable("erg1"));
		assertEquals(96 + 78, environment.getVariable("qf"));
		assertEquals(96 + 63, environment.getVariable("xo"));
		assertEquals(96 + 63 + 20, environment.getVariable("hv"));
		assertEquals(96 + 63 + 76, environment.getVariable("ae"));
		assertEquals(96 + 63 + 24, environment.getVariable("ya"));
		assertEquals((96 + 63) + (96 + 63 + 20) + 38,
				environment.getVariable("erg2"));
		assertEquals((96 + 63 + 76) + (96 + 63) + 44,
				environment.getVariable("vu"));
		assertEquals(((96 + 63 + 76) + (96 + 63) + 44) + (96 + 63 + 76)
				+ (96 + 63 + 24) + 4, environment.getVariable("erg3"));
	}

	@Test
	public void executionTestWithGeneratedQuery()
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, GraphIOException {
		Graph testGraph = GraphIO.loadGraphFromFile(
				"testit/testgraphs/greqltestgraph.tg", null);
		String classname = "testdata.GeneratedQuery";
		Class<ExecutableQuery> generatedClass = GreqlCodeGenerator
				.generateCode("using erg3: erg3 * 2 store as erg4",
						testGraph.getSchema(), classname);
		GreqlQuery query = (GreqlQuery) generatedClass.newInstance();

		TaskHandle gen = pge.addGreqlQuery(query);
		pge.defineDependency(gen, h7);

		GreqlEnvironment environment = pge.evaluate().getGreqlEnvironment();
		assertEquals(((Integer) environment.getVariable("erg3")) * 2,
				environment.getVariable("erg4"));
	}

	@Test(expected = GreqlException.class)
	public void executionTestWithException() {
		Callable<Object> c = new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				throw new GreqlException("Bah!");
			};
		};
		TaskHandle ex = pge.addCallable(c);
		pge.defineDependency(ex, h3);
		pge.evaluate();
	}
}
