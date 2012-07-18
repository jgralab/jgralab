package de.uni_koblenz.jgralabtest.greql.parallel;

import static org.junit.Assert.assertEquals;

import java.util.ConcurrentModificationException;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.exception.GreqlException;
import de.uni_koblenz.jgralab.greql.funlib.FunLib;
import de.uni_koblenz.jgralab.greql.parallel.ParallelGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql.schema.Greql2Graph;
import de.uni_koblenz.jgralab.greql.schema.Greql2Schema;

public class ParallelTest {

	private static ParallelGreqlEvaluator pge;
	private static Graph dependencyGraph;

	@Before
	public void test() {
		pge = new ParallelGreqlEvaluator();
		dependencyGraph = pge.createGraph();
		Vertex v1 = pge.createQueryVertex("using xo: xo + 20 store as hv");
		Vertex v2 = pge.createQueryVertex("using vk: vk + 78 store as qf");
		Vertex v3 = pge.createQueryVertex("96 store as vk");
		Vertex v4 = pge.createQueryVertex("using xo: xo + 76 store as ae");
		Vertex v5 = pge
				.createQueryVertex("using ae, xo: ae + xo + 44 store as vu");
		Vertex v6 = pge.createQueryVertex("using vk: vk + 48 store as erg1");
		Vertex v7 = pge
				.createQueryVertex("using ya, ae, vu: ya + ae + vu + 4 store as erg3");
		Vertex v8 = pge.createQueryVertex("using xo: xo + 24 store as ya");
		Vertex v9 = pge
				.createQueryVertex("using xo, hv: xo + hv + 38 store as erg2");
		Vertex v10 = pge.createQueryVertex("using vk: vk + 63 store as xo");
		pge.createDependency(v5, v7);
		pge.createDependency(v3, v6);
		pge.createDependency(v10, v8);
		pge.createDependency(v10, v5);
		pge.createDependency(v8, v7);
		pge.createDependency(v4, v5);
		pge.createDependency(v3, v2);
		pge.createDependency(v1, v9);
		pge.createDependency(v10, v1);
		pge.createDependency(v4, v7);
		pge.createDependency(v10, v4);
		pge.createDependency(v10, v9);
		pge.createDependency(v2, v7);
		pge.createDependency(v3, v10);
	}

	@Test
	public void executionTest() {
		GreqlEnvironment environment = pge.evaluate();
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

	@Test(expected = GreqlException.class)
	public void executionTestWithException() {
		Vertex vEx = pge.createQueryVertex("exception");
		pge.createDependency(vEx, dependencyGraph.getVertex(3));
		pge.evaluate();
	}

	@Test(expected = ConcurrentModificationException.class)
	public void executionTestWithConcurrentModificationException() {
		FunLib.registerSubQueryFunction("modifyGraph", new GreqlQuery() {

			@Override
			public Set<String> getUsedVariables() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Set<String> getStoredVariables() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Greql2Expression getRootExpression() {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getQueryText() {
				return "modifying dependency graph ....";
			}

			@Override
			public Greql2Graph getQueryGraph() {
				Greql2Graph graph = Greql2Schema.instance().createGreql2Graph(
						ImplementationType.STANDARD);
				graph.createVertex(Greql2Expression.VC);
				return graph;
			}

			@Override
			public long getParseTime() {
				throw new UnsupportedOperationException();
			}

			@Override
			public long getOptimizationTime() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Object evaluate(Graph datagraph,
					GreqlEnvironment environment,
					ProgressFunction progressFunction) {
				modifyGraph();
				return null;
			}

			@Override
			public Object evaluate(Graph datagraph,
					ProgressFunction progressFunction) {
				modifyGraph();
				return null;
			}

			@Override
			public Object evaluate(Graph datagraph, GreqlEnvironment environment) {
				modifyGraph();
				return null;
			}

			@Override
			public Object evaluate(Graph datagraph) {
				modifyGraph();
				return null;
			}

			@Override
			public Object evaluate() {
				modifyGraph();
				return null;
			}

			public void modifyGraph() {
				ParallelTest.dependencyGraph.getFirstVertex().delete();
			}
		}, false);

		Vertex vMod = pge.createQueryVertex("modifyGraph()");
		pge.createDependency(vMod, dependencyGraph.getVertex(3));
		pge.evaluate();
	}

}
