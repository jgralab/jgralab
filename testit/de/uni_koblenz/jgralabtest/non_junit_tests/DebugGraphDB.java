package de.uni_koblenz.jgralabtest.non_junit_tests;

import java.util.Iterator;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.IterativeDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.weak_components.WeakComponentsWithBFS;
import de.uni_koblenz.jgralab.algolib.problems.WeakComponentsSolver;
import de.uni_koblenz.jgralab.impl.db.GraphDatabase;
import de.uni_koblenz.jgralabtest.algolib.nonjunit.Stopwatch;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.Location;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.WeightedGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.weighted.WeightedSchema;

public class DebugGraphDB {
	private static final long SEED = 42l;
	private static final int VERTICES_PER_DIMENSION = 100;

	public static void main(String[] args) throws Exception {
		WeightedSchema schema = WeightedSchema.instance();
		Stopwatch sw = new Stopwatch();
		System.out.println("Creating graph in memory...");
		sw.start();
		WeightedGraph graph = schema.createWeightedGraph();
		GraphDbPerformance.createGraphWithRandomEdgeWeight(graph,
				VERTICES_PER_DIMENSION, SEED);
		sw.stop();
		System.out.println(sw.getDurationString());

		sw.reset();

		System.out.println();
		System.out.println("Creating graph in database...");
		sw.start();
		GraphDatabase db = GraphDbPerformance.prepareDatabase();
		System.out.println("Creating...");
		WeightedGraph graph2 = schema.createWeightedGraphWithDatabaseSupport(
				"Foo", db);
		GraphDbPerformance.createGraphWithRandomEdgeWeight(graph2,
				VERTICES_PER_DIMENSION, SEED);
		db.commitTransaction();
		sw.stop();
		System.out.println(sw.getDurationString());

		System.out.println();
		printInfo(graph);
		System.out.println();
		printInfo(graph2);
		
		// db.addIndices();
		// db.commitTransaction();

		// RandomGraphForAStar generator = new RandomGraphForAStar(1000.0,
		// 1000.0,
		// 25.0);
		// generator.createPlanarRandomGraph(graph, 10000, 4, new Random(42l),
		// true);

		// schema.saveWeightedGraph("./foo.tg", graph,
		// new ConsoleProgressFunction());

		System.out.println();
		System.out.println("Running on graph in memory...");
		sampleRun(graph);

		System.out.println();
		System.out.println("Running on graph in database...");
		sampleRun(graph2);

		((de.uni_koblenz.jgralab.impl.db.GraphImpl) graph2).clearCache();
		System.out.println();
		System.out
				.println("Running on graph in database with cleared cache...");
		sampleRun(graph2);

		// db.dropIndices();
		// db.commitTransaction();
		System.out.println("Fini.");
	}
	
	private static void compareGraphs(WeightedGraph graph1, WeightedGraph graph2){
		Iterator<Vertex> iter1 = graph1.vertices().iterator();
		Iterator<Vertex> iter2 = graph2.vertices().iterator();
		while(iter1.hasNext()){
			assert iter2.hasNext();
			Vertex next1 = iter1.next();
			Vertex next2 = iter2.next();
			assert next1.getId() == next2.getId();
		}
	}
	
	private static void printInfo(WeightedGraph graph){
		System.out.println("Info for " + graph.getClass());
		System.out.println("Vertices: " + graph.getVCount());
		System.out.println("Edges: " + graph.getECount());
	}

	private static void sampleRun(WeightedGraph graph)
			throws AlgorithmTerminatedException {
		DepthFirstSearch search = new IterativeDepthFirstSearch(graph)
				.undirected();
		Location firstVertex = (Location) graph.getFirstVertex();
		System.out.println("Launching DFS at " + firstVertex.get_name());
		search.execute(firstVertex);
		System.out.println("Num: " + search.getNum());

		WeakComponentsSolver wc = new WeakComponentsWithBFS(graph,
				new BreadthFirstSearch(graph));
		wc.execute();
		System.out.println("Weak components: " + wc.getKappa());
	}
}
