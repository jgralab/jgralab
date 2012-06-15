package de.uni_koblenz.jgralabtest.algolib.algorithms;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.reachability.WarshallAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.Relation;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;

public class WarshallTest {
	private boolean[][] expectedResults = {
			{ false, false, false, false, false, false, false },
			{ false, true, true, true, true, false, false },
			{ false, true, true, true, true, false, false },
			{ false, true, true, true, true, false, false },
			{ false, true, true, true, true, false, false },
			{ false, false, false, false, false, true, true },
			{ false, false, false, false, false, true, true } };

	private SimpleGraph g;

	@Before
	public void setUp() {
		g = TestGraphs.getReachabilityTestGraph();
	}

	@Test
	public void testAlgorithm() {
		WarshallAlgorithm warshall = new WarshallAlgorithm(g).undirected();
		try {
			warshall.execute();
		} catch (AlgorithmTerminatedException e) {
		}
		Relation<Vertex, Vertex> result = warshall.getReachable();
		for (Vertex v : g.vertices()) {
			for (Vertex w : g.vertices()) {
				assertEquals(expectedResults[v.getId()][w.getId()],
						result.get(v, w));
			}
		}
	}
}
