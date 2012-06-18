package de.uni_koblenz.jgralabtest.algolib.algorithms;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ BreadthFirstSearchTest.class, DepthFirstSearchTest.class,
		ShortestPathsWithBFSTest.class, TopologicalOrderTest.class,
		WarshallTest.class, ReachableWithSearchTest.class,
		StrongComponentsTest.class, WeakComponentsTest.class, IsTreeTest.class,
		FloydTest.class, FordMooreTest.class, DijkstraAndAStarTest.class })
public class RunAlgorithmTests {

}
