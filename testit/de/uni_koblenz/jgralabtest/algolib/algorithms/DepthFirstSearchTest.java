package de.uni_koblenz.jgralabtest.algolib.algorithms;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.IterativeDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.RecursiveDepthFirstSearch;
import de.uni_koblenz.jgralabtest.algolib.algorithms.test_visitors.DFSTestVisitor;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;

public class DepthFirstSearchTest {
	private SimpleGraph g;

	@Before
	public void setUp() {
		SimpleGraph g = TestGraphs.getSimpleCyclicGraph();
		this.g = g;
	}

	@Test
	public void testAlgorithm() throws Exception {
		DepthFirstSearch[] algorithms = new DepthFirstSearch[] {
				new RecursiveDepthFirstSearch(g),
				new IterativeDepthFirstSearch(g) };
		Graph[] graphs = new Graph[] { g };

		for (DepthFirstSearch dfs : algorithms) {
			for (Graph graph : graphs) {
				dfs.setGraph(graph);
				DFSTestVisitor dfstv = new DFSTestVisitor();
				dfs.addVisitor(dfstv);
				dfs.withLevel().withParent().execute();
				dfstv.performPostTests();

				dfs.reset();
				dfs.undirected();
				dfs.execute();
				dfstv.performPostTests();

				dfs.reset();
				dfs.reversed();
				dfs.execute();
				dfstv.performPostTests();
			}
		}
	}
}
