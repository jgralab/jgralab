package de.uni_koblenz.jgralabtest.algolib.algorithms;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.shortest_paths.FloydAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.BinaryDoubleFunction;
import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;

public class FloydTest {
	private double inf = Double.POSITIVE_INFINITY;
	private double[][] expectedResults = { { 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 10, 30, 45, inf, inf }, { 0, 10, 0, 20, 35, inf, inf },
			{ 0, 30, 20, 0, 15, inf, inf }, { 0, 45, 35, 15, 0, inf, inf },
			{ 0, inf, inf, inf, inf, 0, 10 }, { 0, inf, inf, inf, inf, 10, 0 } };

	private SimpleGraph g;
	private DoubleFunction<Edge> weight;

	@Before
	public void setUp() {
		g = TestGraphs.getReachabilityTestGraph();
		weight = TestGraphs.getWeightForReachabilityTestGraph(g);
	}

	@Test
	public void testAlgorithm() {
		FloydAlgorithm floyd = new FloydAlgorithm(g).undirected();
		floyd.setEdgeWeight(weight);
		try {
			floyd.execute();
		} catch (AlgorithmTerminatedException e) {
		}
		BinaryDoubleFunction<Vertex, Vertex> result = floyd.getDistances();
		for (Vertex v : g.vertices()) {
			for (Vertex w : g.vertices()) {
				assertEquals(expectedResults[v.getId()][w.getId()],
						result.get(v, w), 0.0001);
			}
		}
	}
}
