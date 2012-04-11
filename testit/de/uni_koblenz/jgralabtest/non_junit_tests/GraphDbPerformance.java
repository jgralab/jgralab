/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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
package de.uni_koblenz.jgralabtest.non_junit_tests;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.RecursiveDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.shortest_paths.DijkstraAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.adapters.MethodCallToDoubleFunctionAdapter;
import de.uni_koblenz.jgralab.impl.db.GraphDatabase;
import de.uni_koblenz.jgralab.impl.db.GraphDatabaseException;
import de.uni_koblenz.jgralabtest.algolib.nonjunit.RandomGraphForAStar;
import de.uni_koblenz.jgralabtest.algolib.nonjunit.Stopwatch;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.Location;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.Way;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.WeightedGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.WeightedSchema;

public class GraphDbPerformance {

	private static final String GRAPH_ID = "Hugo";
	private static final int VERTICES_PER_DIMENSION = 10;

	private static final int REPEAT = 1;
	private static final long SEED = 42l;

	private static enum RunType {
		CREATE, DELETE, DFS, BFS, DIJKSTRA
	}

	private static class RunConfiguration {
		private RunType type;
		private boolean withIndices;
		private int repeat;

		public RunConfiguration(RunType type, boolean withIndices,
				int vertexCount, int repeat) {
			super();
			this.type = type;
			this.withIndices = withIndices;
			this.repeat = repeat;
		}

	}

	private static GraphDatabase gdb;

	// private static RandomGraphForAStar graphGenerator;

	public static void main(String[] args) throws Exception {

		List<RunConfiguration> runs = new LinkedList<RunConfiguration>();

		RunType currentRunType = RunType.CREATE;

		for (boolean currentWithIndices : new boolean[] { false, true }) {
			runs.add(new RunConfiguration(currentRunType, currentWithIndices,
					VERTICES_PER_DIMENSION, REPEAT));
		}

		currentRunType = RunType.DELETE;

		for (boolean currentWithIndices : new boolean[] { false, true }) {
			runs.add(new RunConfiguration(currentRunType, currentWithIndices,
					VERTICES_PER_DIMENSION, REPEAT));
		}

		RunType[] algorithmRunTypes = new RunType[] { RunType.DFS, RunType.BFS,
				RunType.DIJKSTRA };

		for (RunType type : algorithmRunTypes) {

			for (boolean currentWithIndices : new boolean[] { true }) {
				runs.add(new RunConfiguration(type, currentWithIndices,
						VERTICES_PER_DIMENSION, REPEAT));
			}

		}

		gdb = prepareDatabase();

		// graphGenerator = new RandomGraphForAStar(SIZE, SIZE, MAX_DEVIATION);
		System.out.println("Launching benchmark...");
		System.out.println();

		for (RunConfiguration currentConfiguration : runs) {
			performRun(currentConfiguration);
			gdb.commitTransaction();
		}
		System.out.println("Fini.");
	}

	public static GraphDatabase prepareDatabase() throws GraphDatabaseException {
		System.out.println("Connecting DB...");
		String dbAddress = System.getProperty("jgralabtest_dbconnection");
		GraphDatabase gdb = GraphDatabase.openGraphDatabase(dbAddress);
		gdb.setAutoCommit(false);
		try {
			System.out
					.println("Clearing graph db (hopefully it was only a test DB :-) )...");
			gdb.clearAllTables();
			gdb.commitTransaction();
		} catch (SQLException e) {
			e.printStackTrace();
			// gdb.rollback();
			// gdb.applyDbSchema();
		}
		try {
			if (!gdb.contains(WeightedSchema.instance())) {
				gdb.insertSchema(WeightedSchema.instance());
			}
		} catch (GraphDatabaseException e) {
			gdb.applyDbSchema();
			// if (!gdb.contains(JniTestSchema.instance())) {
			gdb.insertSchema(WeightedSchema.instance());
			// }
			e.printStackTrace();
		}
		gdb.commitTransaction();
		return gdb;
	}

	private static void performRun(RunConfiguration config) throws Exception {
		if (config.withIndices) {
			gdb.addIndices();
		}
		try {
			System.out.println("Starting " + REPEAT + " run(s) " + config.type
					+ " with:");
			// System.out.println("Vertices    : " + config.vertexCount);
			System.out.println("Indices     : "
					+ (config.withIndices ? "ON" : "OFF"));

			Stopwatch sw = new Stopwatch();
			double totalDuration = 0.0;

			WeightedGraph graph = config.type.equals(RunType.CREATE) ? null
					: getTheGraph();

			for (int i = 0; i < config.repeat; i++) {
				sw.reset();

				switch (config.type) {
				case CREATE:
					deleteTheGraph();
					sw.start();
					createTheGraph();
					sw.stop();
					break;
				case DELETE:
					getTheGraph();
					sw.start();
					deleteTheGraph();
					sw.stop();
					break;
				case DFS:
					clearCache(graph);
					sw.start();
					runDFS(graph);
					sw.stop();
					break;
				case BFS:
					clearCache(graph);
					sw.start();
					runBFS(graph);
					sw.stop();
					break;
				case DIJKSTRA:
					clearCache(graph);
					sw.start();
					runDijkstra(graph);
					sw.stop();
					break;
				}
				System.out.println(sw.getDurationString());
				totalDuration += sw.getDuration();
			}
			System.out.println("Average duration: "
					+ (totalDuration / (config.repeat * 1000)));
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (config.withIndices) {
				gdb.dropIndices();
			}
		}
	}

	private static void runDFS(WeightedGraph graph) {
		DepthFirstSearch dfs = new RecursiveDepthFirstSearch(graph);
		try {
			dfs.undirected().execute(graph.getFirstVertex());
			// System.out.println(dfs.getNum());
		} catch (AlgorithmTerminatedException e) {
		}
	}

	private static void runBFS(WeightedGraph graph) {
		BreadthFirstSearch bfs = new BreadthFirstSearch(graph);
		try {
			bfs.undirected().execute(graph.getFirstVertex());
		} catch (AlgorithmTerminatedException e) {
		}
	}

	private static void runDijkstra(final WeightedGraph graph) {
		DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph, null,
				new MethodCallToDoubleFunctionAdapter<Edge>() {

					@Override
					public double get(Edge parameter) {
						return ((Way) parameter).get_weight();
					}

					@Override
					public boolean isDefined(Edge parameter) {
						return parameter.getGraph() == graph;
					}

				});
		try {
			dijkstra.undirected().execute(graph.getFirstVertex());
		} catch (AlgorithmTerminatedException e) {
		}
	}

	private static WeightedGraph createTheGraph() throws GraphDatabaseException {
		WeightedGraph graph = WeightedSchema.instance()
				.createWeightedGraph(GRAPH_ID, gdb);
		// graphGenerator.createPlanarRandomGraph(graph, VERTICES_PER_DIMENSION,
		// INCIDENCES_PER_VERTEX, new Random(SEED), false);
		createGraphWithRandomEdgeWeight(graph, VERTICES_PER_DIMENSION, SEED);
		gdb.commitTransaction();
		return graph;
	}

	private static WeightedGraph getTheGraph() throws GraphDatabaseException {
		return gdb.containsGraph(GRAPH_ID) ? (WeightedGraph) gdb
				.getGraph(GRAPH_ID) : createTheGraph();
	}

	private static void deleteTheGraph() throws GraphDatabaseException {
		if (gdb.containsGraph(GRAPH_ID)) {
			gdb.deleteGraph(GRAPH_ID);
		}
		gdb.commitTransaction();
	}

	private static void clearCache(WeightedGraph graph) {
		((de.uni_koblenz.jgralab.impl.db.GraphImpl) graph).clearCache();
	}

	public static void createGraphWithRandomEdgeWeight(WeightedGraph graph,
			int verticesPerDimension, long seed) {
		assert graph.getVCount() == 0;
		Random rng = new Random(seed);
		Location[][] vertices = new Location[verticesPerDimension][verticesPerDimension];

		// create vertices along grid
		for (int i = 0; i < verticesPerDimension; i++) {
			for (int j = 0; j < verticesPerDimension; j++) {
				Location newVertex = graph.createLocation();
				newVertex.set_x(i);
				newVertex.set_y(j);
				newVertex.set_name("(" + i + ":" + j + ")");
				vertices[j][i] = newVertex;
				// create at most 2 edges
				if (i > 0) {
					Location alpha = vertices[j][i - 1];
					Way newWay = graph.createWay(alpha, newVertex);
					createRandomWeight(rng, newWay);
				}
				if (j > 0) {
					Location alpha = vertices[j - 1][i];
					Way newWay = graph.createWay(alpha, newVertex);
					createRandomWeight(rng, newWay);
				}
			}
		}
	}

	private static void createRandomWeight(Random rng, Way newWay) {
		// set length to at most twice the actual distance and at
		// least as long as the actual distance
		Location alpha = (Location) newWay.getAlpha();
		Location omega = (Location) newWay.getOmega();
		newWay.set_weight(RandomGraphForAStar.euclideanDistance(alpha.get_x(),
				alpha.get_y(), omega.get_x(), omega.get_y())
				* (1 + rng.nextDouble()));
	}

}
