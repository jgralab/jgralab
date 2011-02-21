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
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.Way;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.WeightedGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.WeightedSchema;

public class GraphDbPerformance {

	private static final String GRAPH_ID = "Hugo";
	private static final int VERTEX_COUNT = 10000;
	private static final int INCIDENCES_PER_VERTEX = 4;
	private static final double SIZE = 1000.0;
	private static final double MAX_DEVIATION = 25.0;

	private static final int REPEAT = 5;
	private static final long SEED = 42l;

	private static enum RunType {
		CREATE, DFS, BFS, DIJKSTRA
	}

	private static class RunConfiguration {
		private RunType type;
		private boolean withForeignKeys;
		private boolean withIndices;
		private boolean withCache;
		private int repeat;

		public RunConfiguration(RunType type, boolean withForeignKeys,
				boolean withIndices, boolean withCache, int vertexCount,
				int repeat) {
			super();
			this.type = type;
			this.withForeignKeys = withForeignKeys;
			this.withIndices = withIndices;
			this.withCache = withCache;
			this.repeat = repeat;
		}

	}

	private static GraphDatabase gdb;
	private static RandomGraphForAStar graphGenerator;

	public static void main(String[] args) throws Exception {

		List<RunConfiguration> runs = new LinkedList<RunConfiguration>();

		boolean[] possibleValues1 = new boolean[] { false, true };
		for (RunType currentRunType : RunType.values()) {
			for (boolean currentWithForeignKeys : possibleValues1) {
				for (boolean currentWithIndices : possibleValues1) {
					for (boolean currentWithCache : possibleValues1) {
						if (!(currentRunType.equals(RunType.CREATE) && currentWithCache == false)) {
							runs.add(new RunConfiguration(currentRunType,
									currentWithForeignKeys, currentWithIndices,
									currentWithCache, VERTEX_COUNT, REPEAT));
						}
					}
				}
			}
		}

		System.out.println("Connecting DB...");
		String dbAddress = System.getProperty("jgralabtest_dbconnection");
		gdb = GraphDatabase.openGraphDatabase(dbAddress);
		gdb.setAutoCommit(false);
		try {
			System.out
					.println("Clearing graph db (hopefully it was only a test DB :-) )...");
			gdb.clearAllTables();
			gdb.commitTransaction();
		} catch (SQLException e) {
			e.printStackTrace();
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

		graphGenerator = new RandomGraphForAStar(SIZE, SIZE, MAX_DEVIATION);
		System.out.println("Launching benchmark...");
		System.out.println();

		for (RunConfiguration currentConfiguration : runs) {
			performRun(currentConfiguration);
		}
		
		System.out.println("Fini.");
	}

	private static void performRun(RunConfiguration config) throws Exception {
		if (config.withForeignKeys) {
			gdb.addForeignKeyConstraints();
		}
		if (config.withIndices) {
			gdb.addIndices();
		}
		try {
			System.out.println("Starting " + REPEAT + " run(s) " + config.type
					+ " with:");
			// System.out.println("Vertices    : " + config.vertexCount);
			System.out.println("Foreign keys: "
					+ (config.withForeignKeys ? "ON" : "OFF"));
			System.out.println("Indices     : "
					+ (config.withIndices ? "ON" : "OFF"));
			System.out.println("Cache       : "
					+ (config.withCache ? "ON" : "OFF"));

			Stopwatch sw = new Stopwatch();
			double totalDuration = 0.0;

			WeightedGraph graph = config.type.equals(RunType.CREATE) ? null
					: getTheGraph();

			for (int i = 0; i < config.repeat; i++) {
				sw.reset();

				if (config.type.equals(RunType.CREATE) && !config.withCache) {
					System.out.println("Skipping...");
					break;
				}

				switch (config.type) {
				case CREATE:
					if (config.withForeignKeys) {
						gdb.dropForeignKeyConstraints();
					}
					deleteTheGraph();
					if (config.withForeignKeys) {
						gdb.addForeignKeyConstraints();
					}
					sw.start();
					createTheGraph();
					sw.stop();
					break;
				case DFS:
					if (!config.withCache) {
						clearCache(graph);
					}
					sw.start();
					runDFS(graph);
					sw.stop();
					break;
				case BFS:
					if (!config.withCache) {
						clearCache(graph);
					}
					sw.start();
					runBFS(graph);
					sw.stop();
					break;
				case DIJKSTRA:
					if (!config.withCache) {
						clearCache(graph);
					}
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
			if (config.withForeignKeys) {
				gdb.dropForeignKeyConstraints();
			}
			if (config.withIndices) {
				gdb.dropIndices();
			}
		}
	}

	private static void runDFS(WeightedGraph graph) {
		DepthFirstSearch dfs = new RecursiveDepthFirstSearch(graph)
				.undirected();
		try {
			dfs.execute();
		} catch (AlgorithmTerminatedException e) {
		}
	}

	private static void runBFS(WeightedGraph graph) {
		BreadthFirstSearch bfs = new BreadthFirstSearch(graph);
		try {
			bfs.execute();
		} catch (AlgorithmTerminatedException e) {
		}
	}

	private static void runDijkstra(final WeightedGraph graph) {
		DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph, null, null,
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
				.createWeightedGraphWithDatabaseSupport(GRAPH_ID, gdb);
		graphGenerator.createPlanarRandomGraph(graph, VERTEX_COUNT,
				INCIDENCES_PER_VERTEX, new Random(SEED), false);
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
	}

	private static void clearCache(WeightedGraph graph) {
		((de.uni_koblenz.jgralab.impl.db.GraphImpl) graph).clearCache();
	}

}
