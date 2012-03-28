package de.uni_koblenz.jgralabtest.algolib.algorithms;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.algolib.algorithms.weak_components.IsTree;
import de.uni_koblenz.jgralabtest.algolib.nonjunit.RandomGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;

public class IsTreeTest {

	private SimpleGraph[] gs;
	private boolean[] expectedResults;

	@Before
	public void setUp() {
		gs = new SimpleGraph[] {
				TestGraphs.getSimpleCyclicGraph(),
				RandomGraph.createGraphWithWeakComponents(System.nanoTime(), 1,
						100, 0),
				RandomGraph.createGraphWithWeakComponents(System.nanoTime(), 1,
						100, 10),
				RandomGraph.createGraphWithWeakComponents(System.nanoTime(), 2,
						100, 0),
				RandomGraph.createGraphWithWeakComponents(System.nanoTime(), 2,
						100, 10) };
		expectedResults = new boolean[] { false, true, false, false, false };
	}

	@Test
	public void testAlgorithm() throws Exception {
		IsTree algorithm = new IsTree(gs[0]);
		for (int i = 0; i < gs.length; i++) {
			Graph graph = gs[i];
			algorithm.reset();
			algorithm.setGraph(graph);
			algorithm.execute();
			boolean result = algorithm.isTree();
			assertEquals(expectedResults[i], result);
		}
	}
}
