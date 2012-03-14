package de.uni_koblenz.jgralabtest.algolib.algorithms;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.algolib.algorithms.search.RecursiveDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.KahnKnuthAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.TopologicalOrderWithDFS;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.visitors.TopologicalOrderVisitor;
import de.uni_koblenz.jgralab.algolib.problems.TopologicalOrderSolver;
import de.uni_koblenz.jgralabtest.algolib.algorithms.test_visitors.TopologicalOrderTestVisitor;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;

public class TopologicalOrderTest {
	private SimpleGraph g;

	@Before
	public void setUp() {
		SimpleGraph g = TestGraphs.getSimpleAcyclicGraph();
		this.g = g;
	}

	@Test
	public void testAlgorithm() throws Exception {
		TopologicalOrderVisitor visitor = new TopologicalOrderTestVisitor();
		TopologicalOrderSolver[] algorithms = new TopologicalOrderSolver[] {
				new KahnKnuthAlgorithm(g),
				new TopologicalOrderWithDFS(g, new RecursiveDepthFirstSearch(g)) };
		for (TopologicalOrderSolver algorithm : algorithms) {
			algorithm.addVisitor(visitor);
			algorithm.execute();
		}
	}

}
