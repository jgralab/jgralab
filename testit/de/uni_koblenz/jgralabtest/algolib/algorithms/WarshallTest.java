package de.uni_koblenz.jgralabtest.algolib.algorithms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.reachability.WarshallAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.BinaryFunction;
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
				boolean reachable = result.get(v, w);
				assertEquals(expectedResults[v.getId()][w.getId()], reachable);
				if (reachable) {
					verifyPath(v, w, warshall.getSuccessor());
				}
			}
		}
	}

	@Test
	public void testUnsupportedVisitors() {
		WarshallAlgorithm warshall = new WarshallAlgorithm(g).undirected();
		try {
			warshall.addVisitor(null);
			fail("Setting visitors should not be allowed for the warshall algorithm.");
		} catch (UnsupportedOperationException e) {
		}
		try {
			warshall.removeVisitor(null);
			fail("Removing visitors should not be allowed for the warshall algorithm.");
		} catch (UnsupportedOperationException e) {
		}
	}

	public static void verifyPath(Vertex source, Vertex target,
			BinaryFunction<Vertex, Vertex, Edge> successor) {
		if (source == target) {
			return;
		}
		Vertex currentVertex = source;
		Edge nextEdge = successor.get(currentVertex, target);
		while (currentVertex != target && nextEdge != null) {
			currentVertex = nextEdge.getThat();
			nextEdge = successor.get(currentVertex, target);
		}
		if (currentVertex != target) {
			fail("No valid path found from " + source + " to " + target + ".");
		}
	}
}
