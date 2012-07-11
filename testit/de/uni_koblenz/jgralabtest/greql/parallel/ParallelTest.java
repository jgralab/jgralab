package de.uni_koblenz.jgralabtest.greql.parallel;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.parallel.ParallelGreqlEvaluator;

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
		Vertex v6 = pge.createQueryVertex("using vk: vk + 48");
		Vertex v7 = pge.createQueryVertex("using ya, ae, vu: ya + ae + vu + 4");
		Vertex v8 = pge.createQueryVertex("using xo: xo + 24 store as ya");
		Vertex v9 = pge.createQueryVertex("using xo, hv: xo + hv + 38");
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
		Map<Vertex, Object> result = pge.evaluate();
		assertEquals(96, result.get(dependencyGraph.getVertex(3)));
		assertEquals(96 + 48, result.get(dependencyGraph.getVertex(6)));
		assertEquals(96 + 78, result.get(dependencyGraph.getVertex(2)));
		assertEquals(96 + 63, result.get(dependencyGraph.getVertex(10)));
		assertEquals(96 + 63 + 20, result.get(dependencyGraph.getVertex(1)));
		assertEquals(96 + 63 + 76, result.get(dependencyGraph.getVertex(4)));
		assertEquals(96 + 63 + 24, result.get(dependencyGraph.getVertex(8)));
		assertEquals((96 + 63) + (96 + 63 + 20) + 38,
				result.get(dependencyGraph.getVertex(9)));
		assertEquals((96 + 63 + 76) + (96 + 63) + 44,
				result.get(dependencyGraph.getVertex(5)));
		assertEquals(((96 + 63 + 76) + (96 + 63) + 44) + (96 + 63 + 76)
				+ (96 + 63 + 24) + 4, result.get(dependencyGraph.getVertex(7)));
	}

}
