/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralabtest.algolib.nonjunit;

import java.util.Stack;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.shortest_paths.FloydAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.shortest_paths.FordMooreAlgorithm;
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

	private Location[] v;
	private WeightedGraph graph;
	// private static DoubleVertexMarker heuristic;
	private DoubleEdgeMarker weightFunction;
	private ArrayVertexMarker<String> names;

	public TryFordMoore() {
		createGraph();
	}

	private void createEdge(int ia, int io, double w) {
		Way e = graph.createWay(v[ia], v[io]);
		weightFunction.mark(e, w);
	}

	public static void main(String[] args) {
		TryFordMoore tfm = new TryFordMoore();

		// System.out.println(graph.getECount());

		System.out.println("Floyd");
		FloydAlgorithm floyd = new FloydAlgorithm(tfm.getGraph());
		floyd.setEdgeWeight(tfm.getWeightFunction());
		try {
			floyd.execute();
		} catch (AlgorithmTerminatedException e) {
		}

		if (floyd.hasNegativeCycles()) {
			System.out.println("negative cycle detected");
		} else {
			tfm.printMatrix(floyd.getInternalWeightedDistance());
			tfm.printResult(tfm.getGraph(), floyd.getDistances(),
					floyd.getSuccessor());
		}

		System.out.println();
		System.out.println("Ford-Moore");

		Location start = tfm.getV()[0];
		Location target = tfm.getV()[1];
		FordMooreAlgorithm fm = new FordMooreAlgorithm(tfm.getGraph(), null,
				tfm.getWeightFunction());
		try {
			fm.execute(start);
		} catch (AlgorithmTerminatedException e) {
		}

		if (fm.hasNegativeCycleDetected()) {
			System.out.println("negative cycle detected");
		} else {
			tfm.printResult2(target, fm.getParent(), fm.getDistance());
		}

	}

	private void printResult(WeightedGraph graph,
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

	private void printMatrix(double[][] matrix) {
		System.out.print("{");
		for (int i = 0; i < matrix.length; i++) {
			System.out.print("{");
			for (int j = 0; j < matrix[i].length; j++) {
				double val = matrix[i][j];
				System.out.print(Double.isInfinite(val) ? "inf" : val);
				System.out.print(j < matrix[i].length - 1 ? "," : "}");
			}
			System.out.println(i < matrix.length - 1 ? "," : "}");
		}
	}

	private void printResult2(Location target, Function<Vertex, Edge> result,
			DoubleFunction<Vertex> distance) {
		Stack<Vertex> stack = new Stack<>();
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

	private void createGraph() {
		graph = WeightedSchema.instance().createWeightedGraph(
				ImplementationType.STANDARD);
		// heuristic = new DoubleVertexMarker(graph);
		weightFunction = new DoubleEdgeMarker(graph);
		names = new ArrayVertexMarker<>(graph);
		v = new Location[20];

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
		// createEdge(1, 17, 85);

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
		// createEdge(17, 1, 85);
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

	public Location[] getV() {
		return v;
	}

	public WeightedGraph getGraph() {
		return graph;
	}

	public DoubleEdgeMarker getWeightFunction() {
		return weightFunction;
	}

	public ArrayVertexMarker<String> getNames() {
		return names;
	}
}
