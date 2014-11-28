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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.BinaryDoubleFunction;
import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;
import de.uni_koblenz.jgralab.algolib.functions.adapters.MethodCallToBinaryDoubleFunctionAdapter;
import de.uni_koblenz.jgralab.algolib.functions.adapters.MethodCallToDoubleFunctionAdapter;
import de.uni_koblenz.jgralabtest.algolib.nonjunit.kdtree.KDTree;
import de.uni_koblenz.jgralabtest.algolib.nonjunit.kdtree.Point;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.Location;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.Way;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.WeightedGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.WeightedSchema;

public class RandomGraphForAStar {

	private static final int KD_SEGMENT_SIZE = 100;

	public static class LocationPoint extends Point {
		public Location l;

		public LocationPoint(Location l) {
			super(2);
			this.l = l;
		}

		@Override
		public double get(int position) {
			switch (position) {
			case 0:
				return l.get_x();
			case 1:
				return l.get_y();
			default:
				throw new IndexOutOfBoundsException();
			}
		}

	}

	private double width;
	private double height;
	private double maxDeviation;
	private Map<WeightedGraph, KDTree<LocationPoint>> kdTrees;

	public RandomGraphForAStar(double width, double height, double maxDeviation) {
		assert width > 0;
		assert height > 0;
		assert maxDeviation > 0;

		this.width = width;
		this.height = height;
		this.maxDeviation = maxDeviation;
	}

	public LinkedList<LocationPoint> createKDTree(WeightedGraph graph) {
		LinkedList<LocationPoint> locations = new LinkedList<>();

		for (Location currentLocation : graph.getLocationVertices()) {
			locations.add(new LocationPoint(currentLocation));
		}

		// System.out.println("Creating KD-tree...");
		KDTree<LocationPoint> kdtree = new KDTree<>(locations, KD_SEGMENT_SIZE);
		if (kdTrees == null) {
			kdTrees = new HashMap<>();
		}
		kdTrees.put(graph, kdtree);
		return locations;
	}

	public KDTree<LocationPoint> getKDTree(WeightedGraph graph) {
		return kdTrees.get(graph);
	}

	public WeightedGraph createRandomWeightedGraph(int vertexCount,
			int edgesPerVertex, boolean verbose) {
		WeightedGraph graph = WeightedSchema.instance().createWeightedGraph(
				ImplementationType.STANDARD);
		Random rng = new Random();
		return createRandomWeightedGraph(graph, vertexCount, edgesPerVertex,
				rng, verbose);
	}

	public WeightedGraph createRandomWeightedGraph(int vertexCount,
			int edgesPerVertex) {
		return createRandomWeightedGraph(vertexCount, edgesPerVertex, true);
	}

	public WeightedGraph createRandomWeightedGraph(WeightedGraph graph,
			int vertexCount, int edgesPerVertex, Random rng, boolean verbose) {
		int chunkSize = vertexCount / 100;
		createRandomVertices(vertexCount, rng, graph, chunkSize, verbose);

		LinkedList<LocationPoint> locations = createKDTree(graph);

		if (verbose) {
			System.out.println("Creating edges...");
		}
		int i = 0;
		for (LocationPoint currentAlpha : locations) {
			// for (int i = 0; i < vertexCount; i++) {
			if (verbose && chunkSize > 0) {
				printPoint(chunkSize, i);
			}
			Location alpha = currentAlpha.l;
			List<LocationPoint> nearestNeighbors = getNearestNeighbors(graph,
					currentAlpha, edgesPerVertex);
			// System.out.println(nearestNeighbors);
			// Location[] omegas = getNearestNeighbors(graph, alpha,
			// edgesPerVertex);
			for (LocationPoint currentOmega : nearestNeighbors) {
				Location omega = currentOmega.l;
				createEdgePair(rng, graph, alpha, omega);
			}
			i++;
		}

		if (verbose) {
			System.out.println();
			System.out.println("Created " + graph.getECount() + " edges.");
			System.out.println("Graph created.");
		}
		return graph;
	}

	private void createRandomVertices(int vertexCount, Random rng,
			WeightedGraph graph, int chunkSize, boolean verbose) {
		// Location[] vertices = new Location[vertexCount];
		if (verbose) {
			System.out.println("Creating vertices...");
		}
		for (int i = 0; i < vertexCount; i++) {
			if (verbose && chunkSize > 0) {
				printPoint(chunkSize, i);
			}
			Location currentVertex = graph.createLocation();
			currentVertex.set_x(rng.nextDouble() * width);
			currentVertex.set_y(rng.nextDouble() * height);
		}
		if (verbose) {
			System.out.println();
			System.out.println("Created " + graph.getVCount() + " vertices.");
		}
	}

	private void createEdgePair(Random rng, WeightedGraph graph,
			Location alpha, Location omega) {
		double distance = euclideanDistance(alpha.get_x(), alpha.get_y(),
				omega.get_x(), omega.get_y());
		double weight = distance + rng.nextDouble() * maxDeviation;

		boolean create = true;
		for (Way current : alpha.getWayIncidences()) {
			if (current.getThat() == omega) {
				create = false;
				break;
			}
		}
		if (create) {
			Way link = graph.createWay(alpha, omega);
			link.set_weight(weight);
		}
	}

	private void printPoint(int chunkSize, int i) {
		if (i % chunkSize == 0) {
			System.out.print(".");
			System.out.flush();
		}
	}

	public static double euclideanDistance(double x1, double y1, double x2,
			double y2) {
		double x = x2 - x1;
		double y = y2 - y1;
		double value = Math.sqrt(x * x + y * y);
		return value;
	}

	public List<LocationPoint> getNearestNeighbors(WeightedGraph graph,
			LocationPoint from, int count) {
		return getKDTree(graph).getNearestNeighbors(from, count);
	}

	public static DoubleFunction<Edge> getWeightFunction() {
		return new MethodCallToDoubleFunctionAdapter<Edge>() {

			@Override
			public double get(Edge parameter) {
				Way edge = (Way) parameter;
				return edge.get_weight();
			}

			@Override
			public boolean isDefined(Edge parameter) {
				return true;
			}

		};
	}

	public static BinaryDoubleFunction<Vertex, Vertex> getHeuristic() {
		return new MethodCallToBinaryDoubleFunctionAdapter<Vertex, Vertex>() {

			@Override
			public double get(Vertex parameter1, Vertex parameter2) {
				Location location = (Location) parameter1;
				Location target = (Location) parameter2;
				return euclideanDistance(location.get_x(), location.get_y(),
						target.get_x(), target.get_y());
			}

			@Override
			public boolean isDefined(Vertex parameter1, Vertex parameter2) {
				return true;
			}

		};
	}
}
