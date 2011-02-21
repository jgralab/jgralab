/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

import java.io.File;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.shortest_paths.AStarSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.shortest_paths.DijkstraAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.shortest_paths.FordMooreAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.BinaryDoubleFunction;
import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;
import de.uni_koblenz.jgralab.algolib.functions.adapters.MethodCallToBinaryDoubleFunctionAdapter;
import de.uni_koblenz.jgralab.algolib.functions.adapters.MethodCallToDoubleFunctionAdapter;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralabtest.algolib.nonjunit.RandomGraphForAStar.LocationPoint;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.Location;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.Way;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.WeightedGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.WeightedSchema;

public class TryAStar {

	private static final int VERTEXCOUNT = 500000;
	private static final int EDGESPERVERTEX = 4;
	private static final double MAX = 1000.0;
	private static final double MAX_DEVIATION = 25;
	private static final String filename = "./testit/testgraphs/astar.tg.gz";

	// private static double[] distances;
	private static WeightedGraph graph;
	private static Location start;
	private static Location target;
	private static RandomGraphForAStar graphGenerator;

	public static void main(String[] args) throws GraphIOException,
			AlgorithmTerminatedException {
		Stopwatch sw = new Stopwatch();
		graphGenerator = new RandomGraphForAStar(MAX, MAX, MAX_DEVIATION);
		if (new File(filename).exists()) {
			graph = WeightedSchema.instance().loadWeightedGraph(filename,
					new ConsoleProgressFunction());
			System.out.println("Loaded graph with " + graph.getVCount()
					+ " vertices and " + graph.getECount() + " edges.");
			graphGenerator.createKDTree(graph);
		} else {
			sw.start();
			graph = graphGenerator.createPlanarRandomGraph(VERTEXCOUNT,
					EDGESPERVERTEX);
			sw.stop();
			System.out.println(sw.getDurationString());
			System.out.println();
			WeightedSchema.instance().saveWeightedGraph(filename, graph,
					new ConsoleProgressFunction());
		}

		DoubleFunction<Edge> edgeWeight = new MethodCallToDoubleFunctionAdapter<Edge>() {

			@Override
			public double get(Edge parameter) {
				return ((Way) parameter).get_weight();
			}

			@Override
			public boolean isDefined(Edge parameter) {
				return parameter.getGraph() == graph;
			}

		};

		BinaryDoubleFunction<Vertex, Vertex> heuristic = new MethodCallToBinaryDoubleFunctionAdapter<Vertex, Vertex>() {

			@Override
			public double get(Vertex parameter1, Vertex parameter2) {
				Location v1 = (Location) parameter1;
				Location v2 = (Location) parameter2;

				return RandomGraphForAStar.euclideanDistance(v1.get_x(), v1
						.get_y(), v2.get_x(), v2.get_y());
			}

			@Override
			public boolean isDefined(Vertex parameter1, Vertex parameter2) {
				return parameter1.getGraph() == graph
						&& parameter2.getGraph() == graph;
			}

		};

		selectVertices(graph);

		System.out.println("AStar");
		for (int i = 0; i < 10; i++) {
			AStarSearch astar = new AStarSearch(graph, null, null, edgeWeight,
					heuristic);
			astar.undirected();
			sw.reset();
			sw.start();
			try {
				astar.execute(start, target);
			} catch (AlgorithmTerminatedException e) {
			}
			sw.stop();
			System.out.println(sw.getDurationString());
			System.out.println(astar.getDistanceToTarget());
			System.out.println("Max elements in queue: "
					+ astar.getVertexQueue().getAddedCount());
		}

		System.out.println();
		for (int i = 0; i < 10; i++) {
			System.out.println("Dijkstra");
			DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph, null,
					null, edgeWeight);
			dijkstra.undirected();
			sw.reset();
			sw.start();
			try {
				dijkstra.execute(start, target);
			} catch (AlgorithmTerminatedException e) {
			}
			sw.stop();
			System.out.println(sw.getDurationString());
			// System.out.println(dijkstra.getWeightedDistance().get(target));
			System.out.println(dijkstra.getDistanceToTarget());
			System.out.println("Max elements in queue: "
					+ dijkstra.getVertexQueue().getAddedCount());
		}

		System.out.println();
		System.out.println("Ford-Moore");
		FordMooreAlgorithm fm = new FordMooreAlgorithm(graph, null, null,
				edgeWeight);
		fm.undirected();
		sw.reset();
		sw.start();
		fm.execute(start, target);
		sw.stop();
		System.out.println(sw.getDurationString());
		System.out.println(fm.getDistanceToTarget());

		System.out.println();
		System.out.println("Fini.");
	}

	private static void selectVertices(WeightedGraph graph) {
		Location nearBorder = graph.createLocation();
		nearBorder.set_x(0.0);
		nearBorder.set_y(0.0);
		LocationPoint from = new LocationPoint(nearBorder);
		start = graphGenerator.getNearestNeighbors(from, 1).get(0).l;
		Location nearCenter = graph.createLocation();
		nearCenter.set_x(MAX / 2.0);
		nearCenter.set_y(MAX / 2.0);
		LocationPoint to = new LocationPoint(nearCenter);
		target = graphGenerator.getNearestNeighbors(to, 1).get(0).l;
		double distance = RandomGraphForAStar.euclideanDistance(start.get_x(),
				start.get_y(), target.get_x(), target.get_y());
		System.out.println("Selected start vertex at location: ("
				+ start.get_x() + "," + start.get_y() + ")");
		System.out.println("Selected target vertex at location: ("
				+ target.get_x() + "," + target.get_y() + ")");
		System.out.println("Direct distance: " + distance);
		nearBorder.delete();
		nearCenter.delete();
	}
}
