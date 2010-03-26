package de.uni_koblenz.jgralabtest.graphmarkertest;


import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.DoubleEdgeMarker;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;
import de.uni_koblenz.jgralabtest.schemas.minimal.Link;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.minimal.Node;

public class TryGraphMarkers {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MinimalGraph graph = MinimalSchema.instance().createMinimalGraph(5, 5);
		Node v1 = graph.createNode();
		Node v2 = graph.createNode();
		Node v3 = graph.createNode();
		Node v4 = graph.createNode();
		Link l1 = graph.createLink(v1, v2);
		Link l2 = graph.createLink(v1, v3);
		Link l3 = graph.createLink(v4, v3);
		Link l4 = graph.createLink(v4, v2);
		Link l5 = graph.createLink(v1, v4);
		IntegerVertexMarker marker1 = new IntegerVertexMarker(
				graph);
		marker1.mark(v1, 1);
		marker1.mark(v2, 2);
		marker1.mark(v3, 3);
		marker1.mark(v4, 4);

		DoubleEdgeMarker marker2 = new DoubleEdgeMarker(
				graph);
		marker2.mark(l1, 0.1);
		marker2.mark(l2, 0.2);
		marker2.mark(l3, 0.3);
		marker2.mark(l4, 0.4);
		marker2.mark(l5, Double.NEGATIVE_INFINITY);

		printAllMarks(graph, marker1, marker2);

		graph.createLink(graph.createNode(), graph.createNode());
		System.out.println(marker1.maxSize());

		printAllMarks(graph, marker1, marker2);
		System.out.println(marker2.size());

		// marker2.setUnmarkedValue(Double.NEGATIVE_INFINITY);
		// printAllMarks(graph, marker1, marker2);
		// System.out.println(marker2.size());

	}

	private static void printAllMarks(MinimalGraph graph,
			IntegerVertexMarker marker1,
			DoubleEdgeMarker marker2) {
		for (Vertex current : graph.vertices()) {
			System.out.println(current + " " + marker1.getMark(current));
		}

		for (Edge current : graph.edges()) {
			System.out.println(current + " " + marker2.getMark(current));
		}
	}
}
