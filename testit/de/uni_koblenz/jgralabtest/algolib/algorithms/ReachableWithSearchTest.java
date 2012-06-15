package de.uni_koblenz.jgralabtest.algolib.algorithms;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.reachability.ReachableWithSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.reachability.WarshallAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.IterativeDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.RecursiveDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.SearchAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.Relation;
import de.uni_koblenz.jgralabtest.algolib.nonjunit.RandomGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;

public class ReachableWithSearchTest {
	private static final int GRAPHS = 5;
	private static final int VC = 30;
	private static final int AEC = 10;

	private SimpleGraph[] gs;
	private SearchAlgorithm[] searches;
	private ReachableWithSearch rws;

	private List<Relation<Vertex, Vertex>> referenceResults;

	@Before
	public void setUp() {
		gs = new SimpleGraph[GRAPHS];
		for (int i = 0; i < GRAPHS; i++) {
			gs[i] = RandomGraph.createGraphWithWeakComponents(
					System.nanoTime(), i + 1, VC, AEC);
			// gs[i] = TestGraphs.getReachabilityTestGraph();
		}

		searches = new SearchAlgorithm[] {
				new BreadthFirstSearch(gs[0]).undirected(),
				new IterativeDepthFirstSearch(gs[0]).undirected(),
				new RecursiveDepthFirstSearch(gs[0]).undirected() };
		rws = new ReachableWithSearch(gs[0], searches[0]).undirected();

		referenceResults = new ArrayList<Relation<Vertex, Vertex>>(GRAPHS);

		WarshallAlgorithm warshall = new WarshallAlgorithm(gs[0]).undirected();
		for (SimpleGraph g : gs) {
			warshall.reset();
			warshall.setGraph(g);
			try {
				warshall.execute();
			} catch (AlgorithmTerminatedException e) {
			}
			referenceResults.add(warshall.getReachable());
		}
	}

	@Test
	public void testAlgorithm() {
		for (int i = 0; i < GRAPHS; i++) {
			SimpleGraph g = gs[i];
			for (SearchAlgorithm search : searches) {
				search.reset();
				search.setGraph(g);
				rws.reset();
				rws.setGraph(g);
				for (Vertex v : g.vertices()) {
					for (Vertex w : g.vertices()) {
						rws.reset();
						try {
							rws.execute(v, w);
						} catch (AlgorithmTerminatedException e) {
						}
						assertEquals("Error for " + v + " to " + w,
								referenceResults.get(i).get(v, w),
								rws.isReachable());
					}
				}
			}
		}
	}
}
