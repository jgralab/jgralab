package de.uni_koblenz.jgralabtest.algolib.algorithms;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.shortest_paths.FloydAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.shortest_paths.FordMooreAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.BinaryDoubleFunction;
import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;
import de.uni_koblenz.jgralabtest.algolib.nonjunit.RandomGraphForAStar;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.WeightedGraph;

public class FordMooreTest {
	private static final int MAX = 100;

	private WeightedGraph g;
	private BinaryDoubleFunction<Vertex, Vertex> referenceResults;

	@Before
	public void setUp() {
		RandomGraphForAStar rgas = new RandomGraphForAStar(100, 100, 25);
		g = rgas.createRandomWeightedGraph(MAX, 4, false);
		FloydAlgorithm floyd = new FloydAlgorithm(g).normal();
		floyd.setEdgeWeight(RandomGraphForAStar.getWeightFunction());
		try {
			floyd.execute();
		} catch (AlgorithmTerminatedException e) {
		}
		referenceResults = floyd.getDistances();
	}

	@Test
	public void testAlgorithm() {
		FordMooreAlgorithm fordMoore = new FordMooreAlgorithm(g);
		fordMoore.setEdgeWeight(RandomGraphForAStar.getWeightFunction());
		for (Vertex v : g.vertices()) {
			fordMoore.reset();
			try {
				fordMoore.execute(v);
			} catch (AlgorithmTerminatedException e) {
			}
			DoubleFunction<Vertex> distance = fordMoore.getDistance();
			for (Vertex w : g.vertices()) {
				assertEquals(referenceResults.get(v, w), distance.get(w),
						0.0001);
			}
		}
	}
}
