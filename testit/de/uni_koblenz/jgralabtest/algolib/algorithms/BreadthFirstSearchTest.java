package de.uni_koblenz.jgralabtest.algolib.algorithms;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.IterativeDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.RecursiveDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.SearchAlgorithm;
import de.uni_koblenz.jgralabtest.algolib.algorithms.test_visitors.SearchTestVisitor;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;

public class BreadthFirstSearchTest {
	private SimpleGraph g;

	@Before
	public void setUp() {
		SimpleGraph g = TestGraphs.createTestGraph1();
		this.g = g;
	}

	@Test
	public void testAlgorithm() throws Exception {
		SearchTestVisitor stv = new SearchTestVisitor();
		SearchAlgorithm[] algorithms = new SearchAlgorithm[] { new BreadthFirstSearch(
				g) };
		Graph[] graphs = new Graph[] { g };

		for (SearchAlgorithm algorithm : algorithms) {
			for (Graph graph : graphs) {
				algorithm.setGraph(graph);
				algorithm.addVisitor(stv);
				algorithm.withParent().execute();
				algorithm.reset();
			}
		}
	}

}
