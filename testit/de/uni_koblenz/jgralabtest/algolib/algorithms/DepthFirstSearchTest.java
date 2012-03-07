package de.uni_koblenz.jgralabtest.algolib.algorithms;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.IterativeDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.RecursiveDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.SearchAlgorithm;
import de.uni_koblenz.jgralabtest.algolib.algorithms.test_visitors.DFSTestVisitor;
import de.uni_koblenz.jgralabtest.algolib.algorithms.test_visitors.SearchTestVisitor;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;

public class DepthFirstSearchTest {
	private SimpleGraph g;

	@Before
	public void setUp() {
		SimpleGraph g = TestGraphs.createTestGraph1();
		this.g = g;
	}

	@Test
	public void testAlgorithm() throws Exception {
		DFSTestVisitor dfstv = new DFSTestVisitor();
		DepthFirstSearch[] algorithms = new DepthFirstSearch[] {
				new RecursiveDepthFirstSearch(g),
				new IterativeDepthFirstSearch(g) };
		Graph[] graphs = new Graph[] { g };

		for (DepthFirstSearch dfs : algorithms) {
			for (Graph graph : graphs) {
				dfs.setGraph(graph);
				dfs.addVisitor(dfstv);
				dfs.withLevel().withParent().execute();
				dfs.reset();
			}
		}
	}

}
