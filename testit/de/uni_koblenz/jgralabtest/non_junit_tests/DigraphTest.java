package de.uni_koblenz.jgralabtest.non_junit_tests;

import de.uni_koblenz.jgralab.schema.impl.DirectedAcyclicGraph;

public class DigraphTest {
	public static void main(String[] args) {
		DirectedAcyclicGraph<String> g = new DirectedAcyclicGraph<String>(true);
		g.createNode("E");
		g.createNode("A");
		g.createNode("B");
		g.createNode("C");
		g.createNode("F");
		g.createEdge("E", "A");
		g.createEdge("E", "B");
		g.createEdge("E", "C");
		g.createEdge("E", "F");
		g.createEdge("A", "B");
		g.createEdge("B", "F");
		g.createEdge("C", "F");

		System.out.println(g);
	}
}
