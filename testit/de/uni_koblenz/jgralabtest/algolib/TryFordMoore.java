package de.uni_koblenz.jgralabtest.algolib;

import java.util.Stack;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.weighted_shortest_paths.FloydAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.weighted_shortest_paths.FordMooreAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.BinaryDoubleFunction;
import de.uni_koblenz.jgralab.algolib.functions.BinaryFunction;
import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.graphmarker.ArrayVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.DoubleEdgeMarker;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.Location;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.Way;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.WeightedGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.WeightedSchema;

public class TryFordMoore {

	private static Location[] v;
	private static WeightedGraph graph;
	// private static DoubleVertexMarker heuristic;
	private static DoubleEdgeMarker weightFunction;
	private static ArrayVertexMarker<String> names;

	private static void createEdge(int ia, int io, double w) {
		Way e = graph.createWay(v[ia], v[io]);
		weightFunction.mark(e, w);
	}

	public static void main(String[] args) {
		graph = WeightedSchema.instance().createWeightedGraph(20, 20);
		// heuristic = new DoubleVertexMarker(graph);
		weightFunction = new DoubleEdgeMarker(graph);
		names = new ArrayVertexMarker<String>(graph);
		v = new Location[20];

		createGraph();
		// System.out.println(graph.getECount());

		System.out.println("Floyd");
		FloydAlgorithm floyd = new FloydAlgorithm(graph);
		floyd.setEdgeWeight(weightFunction);
		try {
			floyd.execute();
		} catch (AlgorithmTerminatedException e) {
		}

		if (floyd.hasNegativeCycles()) {
			System.out.println("negative cycle detected");
		} else {
			printResult(graph, floyd.getWeightedDistance(), floyd
					.getSuccessor());
		}

		System.out.println();
		System.out.println("Ford-Moore");

		Location start = v[0];
		Location target = v[1];
		FordMooreAlgorithm fm = new FordMooreAlgorithm(graph, null, null,
				weightFunction);
		try {
			fm.execute(start);
		} catch (AlgorithmTerminatedException e) {
		}

		if (fm.hasNegativeCycleDetected()) {
			System.out.println("negative cycle detected");
		} else {
			printResult2(target, fm.getParent(), fm.getWeightedDistance());
		}

	}

	private static void printResult(WeightedGraph graph,
			BinaryDoubleFunction<Vertex, Vertex> weightedDistance,
			BinaryFunction<Vertex, Vertex, Edge> successor) {
		for (Vertex v : graph.vertices()) {
			for (Vertex w : graph.vertices()) {
				if (v != w && !Double.isInfinite(weightedDistance.get(v, w))) {
					Edge edge = successor.get(v, w);
					System.out.println("From " + ((Location) v).get_name()
							+ " to " + ((Location) w).get_name() + " follow "
							+ edge + " to "
							+ ((Location) edge.getThat()).get_name()
							+ " Length: " + weightFunction.get(edge)
							+ " Distance: " + weightedDistance.get(v, w));
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private static void printResult2(Location target,
			Function<Vertex, Edge> result, DoubleFunction<Vertex> distance) {
		Stack<Vertex> stack = new Stack<Vertex>();
		stack.push(target);
		Edge currentEdge = result.get(target);
		while (currentEdge != null) {
			Vertex currentVertex = currentEdge.getAlpha();
			stack.push(currentVertex);
			currentEdge = result.get(currentVertex);
		}
		while (!stack.isEmpty()) {
			Vertex currentVertex = stack.pop();
			System.out.println(((Location) currentVertex).get_name());
		}
		System.out.println("Distance: " + distance.get(target));
	}

	private static void createGraph() {
		for (int i = 0; i < v.length; i++) {
			v[i] = graph.createLocation();
		}

		v[0].set_name("Arad");
		// heuristic.mark(v[0], 366);
		createEdge(0, 19, 75);
		createEdge(0, 15, 140);
		createEdge(0, 16, 118);

		v[1].set_name("Bucharest");
		// heuristic.mark(v[1], 0);
		createEdge(1, 5, 211);
		createEdge(1, 13, 101);
		createEdge(1, 6, 90);
		createEdge(1, 17, 85);

		v[2].set_name("Craiova");
		// heuristic.mark(v[2], 160);
		createEdge(2, 3, 120);
		createEdge(2, 14, 146);
		createEdge(2, 13, 138);

		v[3].set_name("Dobreta");
		// heuristic.mark(v[3], 242);
		createEdge(3, 10, 75);
		createEdge(3, 2, 120);

		v[4].set_name("Eforie");
		// heuristic.mark(v[4], 161);
		createEdge(4, 7, 86);

		v[5].set_name("Fagaras");
		// heuristic.mark(v[5], 178);
		createEdge(5, 15, 99);
		createEdge(5, 1, 211);

		v[6].set_name("Giurgiu");
		// heuristic.mark(v[6], 77);
		createEdge(6, 1, 90);

		v[7].set_name("Hirsova");
		// heuristic.mark(v[7], 151);
		createEdge(7, 4, 86);
		createEdge(7, 17, 98);

		v[8].set_name("Iasi");
		// heuristic.mark(v[8], 226);
		createEdge(8, 11, 87);
		createEdge(8, 18, 92);

		v[9].set_name("Lugoj");
		// heuristic.mark(v[9], 244);
		createEdge(9, 16, 111);
		createEdge(9, 10, 70);

		v[10].set_name("Mehadia");
		// heuristic.mark(v[10], 241);
		createEdge(10, 3, 75);
		createEdge(10, 9, 70);

		v[11].set_name("Neamt");
		// heuristic.mark(v[11], 234);
		createEdge(11, 8, 87);

		v[12].set_name("Oradea");
		// heuristic.mark(v[12], 380);
		createEdge(12, 19, 71);
		createEdge(12, 15, 151);

		v[13].set_name("Pitesti");
		// heuristic.mark(v[13], 98);
		createEdge(13, 1, 101);
		createEdge(13, 14, 97);
		createEdge(13, 2, 138);

		v[14].set_name("Rimnicu Vilcea");
		// heuristic.mark(v[14], 193);
		createEdge(14, 15, 80);
		createEdge(14, 13, 97);
		createEdge(14, 2, 146);

		v[15].set_name("Sibiu");
		// heuristic.mark(v[15], 253);
		createEdge(15, 0, 140);
		// createEdge(15, 0, -200);
		createEdge(15, 12, 151);
		createEdge(15, 5, 99);
		createEdge(15, 14, 80);

		v[16].set_name("Timisoara");
		// heuristic.mark(v[16], 329);
		createEdge(16, 0, 118);
		createEdge(16, 9, 111);

		v[17].set_name("Urziceni");
		// heuristic.mark(v[17], 80);
		createEdge(17, 1, 85);
		createEdge(17, 18, 142);
		createEdge(17, 7, 98);

		v[18].set_name("Vaslui");
		// heuristic.mark(v[18], 199);
		createEdge(18, 17, 142);
		createEdge(18, 8, 92);

		v[19].set_name("Zerind");
		// heuristic.mark(v[19], 374);
		createEdge(19, 0, 75);
		createEdge(19, 12, 71);
	}
}
