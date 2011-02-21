package de.uni_koblenz.jgralabtest.algolib.nonjunit;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

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

	private KDTree<LocationPoint> kdtree;
	private double width;
	private double height;
	private double maxDeviation;

	public RandomGraphForAStar(double width, double height, double maxDeviation) {
		assert width > 0;
		assert height > 0;
		assert maxDeviation > 0;

		this.width = width;
		this.height = height;
		this.maxDeviation = maxDeviation;
	}

	public LinkedList<LocationPoint> createKDTree(WeightedGraph graph) {
		LinkedList<LocationPoint> locations = new LinkedList<LocationPoint>();

		for (Location currentLocation : graph.getLocationVertices()) {
			locations.add(new LocationPoint(currentLocation));
		}

		// System.out.println("Creating KD-tree...");
		kdtree = new KDTree<LocationPoint>(locations, KD_SEGMENT_SIZE);
		return locations;
	}

	public KDTree<LocationPoint> getKDTree() {
		return kdtree;
	}

	public WeightedGraph createPlanarRandomGraph(int vertexCount,
			int edgesPerVertex) {
		WeightedGraph graph = WeightedSchema.instance().createWeightedGraph();
		Random rng = new Random();
		return createPlanarRandomGraph(graph, vertexCount, edgesPerVertex, rng,
				true);
	}

	public WeightedGraph createPlanarRandomGraph(WeightedGraph graph,
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
			List<LocationPoint> nearestNeighbors = getNearestNeighbors(
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
		double distance = euclideanDistance(alpha.get_x(), alpha.get_y(), omega
				.get_x(), omega.get_y());
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

	public List<LocationPoint> getNearestNeighbors(LocationPoint from, int count) {
		return kdtree.getNearestNeighbors(from, count);
	}
}
