package de.uni_koblenz.jgralabtest.algolib.algorithms;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.algolib.algorithms.search.IterativeDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.strong_components.StrongComponentsWithDFS;
import de.uni_koblenz.jgralabtest.algolib.algorithms.test_visitors.StrongComponentsTestVisitor;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;

public class StrongComponentsTest {
	private SimpleGraph g;

	@Before
	public void setUp() {
		SimpleGraph g = TestGraphs.getSimpleCyclicGraph();
		this.g = g;
	}

	@Test
	public void testAlgorithm() throws Exception {
		StrongComponentsWithDFS algorithm = new StrongComponentsWithDFS(g,
				new IterativeDepthFirstSearch(g));
		Graph[] graphs = new Graph[] { g };

		for (Graph graph : graphs) {
			algorithm.setGraph(graph);
			StrongComponentsTestVisitor sctv = new StrongComponentsTestVisitor();
			algorithm.addVisitor(sctv);
			algorithm.execute();
			sctv.performPostTests();
		}

	}

}
