package de.uni_koblenz.jgralabtest.algolib.algorithms;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.weak_components.WeakComponentsWithBFS;
import de.uni_koblenz.jgralabtest.algolib.algorithms.test_visitors.WeakComponentsTestVisitor;
import de.uni_koblenz.jgralabtest.algolib.nonjunit.RandomGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;

public class WeakComponentsTest {
	private SimpleGraph g;

	@Before
	public void setUp() {
		SimpleGraph g = TestGraphs.getSimpleCyclicGraph();
		this.g = g;
	}

	@Test
	public void testAlgorithm() throws Exception {
		WeakComponentsWithBFS algorithm = new WeakComponentsWithBFS(g,
				new BreadthFirstSearch(g));
		int[] expectedKappas = new int[] { 1, 5, 100, 0 };
		Graph[] graphs = new Graph[expectedKappas.length];
		for (int i = 0; i < graphs.length; i++) {
			graphs[i] = RandomGraph.createGraphWithWeakComponents(
					System.nanoTime(), expectedKappas[i], 1000, 500);
		}
		int i = 0;
		for (Graph graph : graphs) {
			algorithm.reset();
			algorithm.setGraph(graph);
			WeakComponentsTestVisitor wctv = new WeakComponentsTestVisitor();
			algorithm.addVisitor(wctv);
			algorithm.execute();
			wctv.performPostTests();
			int kappa = algorithm.getKappa();
			assertEquals(expectedKappas[i++], kappa);
		}

	}

}
