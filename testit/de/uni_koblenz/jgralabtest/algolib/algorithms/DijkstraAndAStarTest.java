package de.uni_koblenz.jgralabtest.algolib.algorithms;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.shortest_paths.AStarSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.shortest_paths.DijkstraAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.shortest_paths.FordMooreAlgorithm;
import de.uni_koblenz.jgralab.algolib.problems.DistanceFromVertexToVertexSolver;
import de.uni_koblenz.jgralabtest.algolib.nonjunit.RandomGraphForAStar;
import de.uni_koblenz.jgralabtest.algolib.nonjunit.RandomGraphForAStar.LocationPoint;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.Location;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.WeightedGraph;

public class DijkstraAndAStarTest {

	private static final int MAX = 1000;

	private WeightedGraph[] gs;
	private double[] expectedResults;
	private Location[] start;
	private Location[] target;

	@Before
	public void setUp() {
		RandomGraphForAStar rgas = new RandomGraphForAStar(100, 100, 25);
		gs = new WeightedGraph[] {
				rgas.createRandomWeightedGraph(MAX, 4, false),
				rgas.createRandomWeightedGraph(MAX, 3, false),
				rgas.createRandomWeightedGraph(MAX, 5, false) };

		expectedResults = new double[gs.length];
		start = new Location[gs.length];
		target = new Location[gs.length];

		FordMooreAlgorithm fma;

		for (int i = 0; i < gs.length; i++) {
			selectVertices(gs[i], rgas, i);
			assert (gs[i] == start[i].getGraph());
			assert (gs[i] == target[i].getGraph());
			fma = new FordMooreAlgorithm(gs[i]);
			fma.setEdgeWeight(RandomGraphForAStar.getWeightFunction());
			try {
				fma.execute(start[i], target[i]);
			} catch (AlgorithmTerminatedException e) {
			}
			expectedResults[i] = fma.getDistanceToTarget();
		}
	}

	private void selectVertices(WeightedGraph graph,
			RandomGraphForAStar graphGenerator, int position) {
		Location nearBorder = graph.createLocation();
		nearBorder.set_x(0.0);
		nearBorder.set_y(0.0);
		LocationPoint from = new LocationPoint(nearBorder);
		start[position] = graphGenerator.getNearestNeighbors(graph, from, 1)
				.get(0).l;
		assert (graph == start[position].getGraph());
		Location nearCenter = graph.createLocation();
		nearCenter.set_x(MAX / 2.0);
		nearCenter.set_y(MAX / 2.0);
		LocationPoint to = new LocationPoint(nearCenter);
		target[position] = graphGenerator.getNearestNeighbors(graph, to, 1)
				.get(0).l;
		assert (graph == target[position].getGraph());
		nearBorder.delete();
		nearCenter.delete();
	}

	@Test
	public void testAlgorithm() {
		DistanceFromVertexToVertexSolver[] algs = new DistanceFromVertexToVertexSolver[] {
				new AStarSearch(gs[0]), new DijkstraAlgorithm(gs[0]) };
		for (DistanceFromVertexToVertexSolver alg : algs) {
			for (int i = 0; i < gs.length; i++) {
				Graph graph = gs[i];
				((AStarSearch) alg).reset();
				alg.setGraph(graph);
				((AStarSearch) alg).setEdgeWeight(RandomGraphForAStar
						.getWeightFunction());
				try {
					alg.execute(start[i], target[i]);
				} catch (AlgorithmTerminatedException e) {
				}
				assertEquals(expectedResults[i], alg.getDistanceToTarget(),
						0.0001);
			}
		}
	}

	@Test
	public void testDijkstraAlgorithm() {
		DijkstraAlgorithm alg = new DijkstraAlgorithm(gs[0]);
		for (int i = 0; i < gs.length; i++) {
			Graph graph = gs[i];
			alg.reset();
			alg.setGraph(graph);
			alg.setEdgeWeight(RandomGraphForAStar.getWeightFunction());
			try {
				alg.execute(start[i], target[i]);
			} catch (AlgorithmTerminatedException e) {
			}
			assertEquals(expectedResults[i], alg.getDistanceToTarget(), 0.0001);
		}
	}

	@Test
	public void testAStarAlgorithm() {
		AStarSearch alg = new AStarSearch(gs[0]);
		for (int i = 0; i < gs.length; i++) {
			Graph graph = gs[i];
			alg.reset();
			alg.setGraph(graph);
			alg.setEdgeWeight(RandomGraphForAStar.getWeightFunction());
			alg.setHeuristic(RandomGraphForAStar.getHeuristic());
			try {
				alg.execute(start[i], target[i]);
			} catch (AlgorithmTerminatedException e) {
			}
			assertEquals(expectedResults[i], alg.getDistanceToTarget(), 0.0001);
		}
	}
}
