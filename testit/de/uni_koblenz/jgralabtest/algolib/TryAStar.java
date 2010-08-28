package de.uni_koblenz.jgralabtest.algolib;

import java.io.File;
import java.util.Iterator;
import java.util.Random;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.weighted_shortest_paths.AStarSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.weighted_shortest_paths.DijkstraAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.weighted_shortest_paths.FordMooreAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.BinaryDoubleFunction;
import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;
import de.uni_koblenz.jgralab.algolib.functions.entries.DoubleFunctionEntry;
import de.uni_koblenz.jgralab.impl.ProgressFunctionImpl;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.Location;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.Way;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.WeightedGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.WeightedSchema;

public class TryAStar {
	private static final double MAX = 1000.0;
	private static final double MAX_LONGER = 10;
	private static final int VERTEXCOUNT = 250000;
	private static final int EDGESPERVERTEX = 4;
	private static final String filename = "./testit/testgraphs/astar.tg.gz";

	private static double[] distances;
	private static WeightedGraph graph;
	private static Location start;
	private static Location target;

	public static void main(String[] args) throws GraphIOException {
		Stopwatch sw = new Stopwatch();
		if (new File(filename).exists()) {
			graph = WeightedSchema.instance().loadWeightedGraph(filename,
					new ProgressFunctionImpl());
			System.out.println("Loaded graph with " + graph.getVCount()
					+ " vertices and " + graph.getECount() + " edges.");
		} else {
			sw.start();
			graph = createPlanarRandomGraph(VERTEXCOUNT, EDGESPERVERTEX);
			sw.stop();
			System.out.println(sw.getDurationString());
			System.out.println();
			WeightedSchema.instance().saveWeightedGraph(filename, graph,
					new ProgressFunctionImpl());
		}

		DoubleFunction<Edge> edgeWeight = new DoubleFunction<Edge>() {

			@Override
			public double get(Edge parameter) {
				return ((Way) parameter).get_weight();
			}

			@Override
			public Iterable<Edge> getDomainElements() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isDefined(Edge parameter) {
				return parameter.getGraph() == graph;
			}

			@Override
			public void set(Edge parameter, double value) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Iterator<DoubleFunctionEntry<Edge>> iterator() {
				throw new UnsupportedOperationException();
			}

		};

		BinaryDoubleFunction<Vertex, Vertex> heuristic = new BinaryDoubleFunction<Vertex, Vertex>() {

			@Override
			public double get(Vertex parameter1, Vertex parameter2) {
				Location v1 = (Location) parameter1;
				Location v2 = (Location) parameter2;

				return euclideanDistance(v1.get_x(), v1.get_y(), v2.get_x(), v2
						.get_y());
			}

			@Override
			public boolean isDefined(Vertex parameter1, Vertex parameter2) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void set(Vertex parameter1, Vertex parameter2, double value) {
				throw new UnsupportedOperationException();
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
			System.out.println(astar.getWeightedDistanceToTarget());
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
			dijkstra.execute(start);
			sw.stop();
			System.out.println(sw.getDurationString());
			System.out.println(dijkstra.getWeightedDistance().get(target));
			System.out.println("Max elements in queue: "
					+ dijkstra.getVertexQueue().getAddedCount());
		}

		// System.out.println();
		// System.out.println("Ford-Moore");
		// FordMooreAlgorithm fm = new FordMooreAlgorithm(graph, null, null,
		// edgeWeight);
		// fm.undirected();
		// sw.reset();
		// sw.start();
		// fm.execute(start, target);
		// sw.stop();
		// System.out.println(sw.getDurationString());
		// System.out.println(fm.getWeightedDistanceToTarget());

		System.out.println();
		System.out.println("Fini.");
	}

	private static void selectVertices(WeightedGraph graph) {
		start = graph.getFirstLocation();
		target = start;
		double minimum = start.get_x() + start.get_y();
		double maximum = minimum;
		for (Vertex current : graph.vertices()) {
			double currentValue = ((Location) current).get_x()
					+ ((Location) current).get_y();
			if (currentValue < minimum) {
				minimum = currentValue;
				start = (Location) current;
			}
			if (currentValue > maximum) {
				maximum = currentValue;
				target = (Location) current;
			}
		}
		// target = (Location) graph
		// .getVertex(new Random().nextInt(VERTEXCOUNT) + 1);
	}

	private static void cacheDistances(WeightedGraph graph, Location target) {
		int size = graph.getVCount() + 1;
		if (distances == null || distances.length != size) {
			distances = new double[size];
		}

		for (Vertex current : graph.vertices()) {
			distances[current.getId()] = euclideanDistance(target.get_x(),
					target.get_y(), ((Location) current).get_x(),
					((Location) current).get_y());
		}
	}

	private static Location[] getNearestNeighbors(WeightedGraph in,
			Location from, int count) {
		assert (count < in.getVCount());
		cacheDistances(in, from);
		// init array
		Location[] out = new Location[count];
		double[] minimalDistances = new double[count];
		for (int i = 0; i < count; i++) {
			minimalDistances[i] = Double.POSITIVE_INFINITY;
		}
		for (Vertex current : in.vertices()) {
			Location currentLocation = (Location) current;
			double currentDistance = euclideanDistance(currentLocation.get_x(),
					currentLocation.get_y(), from.get_x(), from.get_y());
			// insert to array
			if (current != from) {
				for (int i = 0; i < out.length; i++) {
					if (out[i] == null) {
						out[i] = currentLocation;
						continue;
					}
					if (currentDistance < minimalDistances[i]) {
						// shift right
						for (int j = out.length - 1; j < i; j--) {
							distances[j] = distances[j - 1];
							out[j] = out[j - 1];
						}
						// write value
						minimalDistances[i] = currentDistance;
						out[i] = currentLocation;
						break;
					}
				}
			}
		}
		return out;
	}

	private static WeightedGraph createPlanarRandomGraph(int vertexCount,
			int edgesPerVertex) {
		Random rng = new Random();
		WeightedGraph graph = WeightedSchema.instance().createWeightedGraph();
		int chunkSize = vertexCount / 100;
		Location[] vertices = createRandomVertices(vertexCount, rng, graph,
				chunkSize);

		System.out.println("Creating eges...");
		for (int i = 0; i < vertexCount; i++) {
			printPoint(chunkSize, i);
			Location alpha = vertices[i];
			Location[] omegas = getNearestNeighbors(graph, alpha,
					edgesPerVertex);
			for (Location omega : omegas) {
				createEdgePair(rng, graph, alpha, omega);
			}
		}

		System.out.println();
		System.out.println("Created " + graph.getECount() + " edges.");
		System.out.println("Graph created.");
		return graph;
	}

	@SuppressWarnings("unused")
	private static WeightedGraph createRandomGraph(int vertexCount,
			int edgesPerVertex) {
		Random rng = new Random();
		WeightedGraph graph = WeightedSchema.instance().createWeightedGraph();
		int chunkSize = vertexCount / 100;

		Location[] vertices = createRandomVertices(vertexCount, rng, graph,
				chunkSize);

		System.out.println("Creating eges...");
		for (int i = 0; i < vertexCount; i++) {
			printPoint(chunkSize, i);
			Location alpha = vertices[i];
			for (int j = 0; j < edgesPerVertex; j++) {
				int omegaIndex = i;
				while (omegaIndex == i) {
					omegaIndex = rng.nextInt(vertexCount);
				}
				Location omega = vertices[omegaIndex];
				createEdgePair(rng, graph, alpha, omega);
			}
		}
		System.out.println();
		System.out.println("Graph created.");
		return graph;
	}

	private static Location[] createRandomVertices(int vertexCount, Random rng,
			WeightedGraph graph, int chunkSize) {
		Location[] vertices = new Location[vertexCount];
		System.out.println("Creating vertices...");
		for (int i = 0; i < vertexCount; i++) {
			printPoint(chunkSize, i);
			vertices[i] = graph.createLocation();
			vertices[i].set_x(rng.nextDouble() * MAX);
			vertices[i].set_y(rng.nextDouble() * MAX);
		}
		System.out.println();
		System.out.println("Created " + graph.getVCount() + " vertices.");
		return vertices;
	}

	private static void createEdgePair(Random rng, WeightedGraph graph,
			Location alpha, Location omega) {
		double distance = euclideanDistance(alpha.get_x(), alpha.get_y(), omega
				.get_x(), omega.get_y());
		double weight = distance + rng.nextDouble() * MAX_LONGER;

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
			// link = graph.createWay(omega, alpha);
			// link.set_weight(weight);
		}
	}

	private static void printPoint(int chunkSize, int i) {
		if (i % chunkSize == 0) {
			System.out.print(".");
			System.out.flush();
		}
	}

	private static double euclideanDistance(double x1, double y1, double x2,
			double y2) {
		double x = x2 - x1;
		double y = y2 - y1;
		double value = Math.sqrt(x * x + y * y);
		return value;
	}
}
