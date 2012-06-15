package de.uni_koblenz.jgralabtest.algolib.algorithms;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.shortest_paths.ShortestPathsWithBFS;
import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;

public class ShortestPathsWithBFSTest {

	private static final double DELTA = 0.0001;
	private SimpleGraph graph;

	@Before
	public void setUp() {
		graph = TestGraphs.getShortestPathTestGraph();
	}

	@Test
	public void TestAlgorithm() {
		ShortestPathsWithBFS alg = new ShortestPathsWithBFS(graph,
				new BreadthFirstSearch(graph));
		try {
			alg.execute(graph.getVertex(2));
		} catch (AlgorithmTerminatedException e) {
		}

		DoubleFunction<Vertex> distance = alg.getDistance();
		assertEquals(0, distance.get(graph.getVertex(2)), DELTA);
		assertEquals(1, distance.get(graph.getVertex(3)), DELTA);
		assertEquals(1, distance.get(graph.getVertex(4)), DELTA);
		assertEquals(2, distance.get(graph.getVertex(1)), DELTA);

	}
}
