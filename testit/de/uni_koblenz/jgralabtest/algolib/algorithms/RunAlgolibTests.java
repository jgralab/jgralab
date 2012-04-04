package de.uni_koblenz.jgralabtest.algolib.algorithms;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ BreadthFirstSearchTest.class, DepthFirstSearchTest.class,
		TopologicalOrderTest.class, StrongComponentsTest.class,
		WeakComponentsTest.class, IsTreeTest.class, DijkstraAndAStarTest.class })
public class RunAlgolibTests {

}
