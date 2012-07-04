package de.uni_koblenz.jgralab.greql.parallel;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.RecursiveDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.TopologicalOrderWithDFS;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.greql.exception.GreqlException;
import de.uni_koblenz.jgralab.impl.generic.GenericGraphFactoryImpl;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

public class ParallelGreqlEvaluator {

	/*
	 * generic GreqlQueryDebendencySchema definition
	 */

	static final String QUERY_TEXT_ATTRIBUTE = "queryText";

	private static Schema schema;
	static {
		schema = new SchemaImpl("GreqlQueryDebendencySchema",
				"de.uni_koblenz.jgralab.greql.parallelgreql");
	}

	private static GraphClass graphClass;
	static {
		graphClass = schema.createGraphClass("GreqlQueryDependencyGraph");
	}

	private static VertexClass queryVertexClass;
	static {
		queryVertexClass = graphClass.createVertexClass("queries.Query");
		queryVertexClass.createAttribute(QUERY_TEXT_ATTRIBUTE,
				schema.getStringDomain());
	}

	private static EdgeClass dependsOnQueryEdgeClass;
	static {
		dependsOnQueryEdgeClass = graphClass.createEdgeClass(
				"queries.DependsOn", queryVertexClass, 0, Integer.MAX_VALUE,
				"successor", AggregationKind.SHARED, queryVertexClass, 0,
				Integer.MAX_VALUE, "predecessor", AggregationKind.NONE);
	}

	/*
	 * Methods to create a new GreqlQueryDependencyGraph
	 */

	private final GenericGraphFactoryImpl genericGraphFactory = new GenericGraphFactoryImpl(
			schema);

	private Graph graph;

	public Graph getGraph() {
		return graph;
	}

	public Graph createGraph(String id, int vMax, int eMax) {
		graph = genericGraphFactory.createGraph(graphClass, id, vMax, eMax);
		return graph;
	}

	public Graph createGraph() {
		return createGraph(null, 1000, 1000);
	}

	public void setGraph(Graph graph) {
		if (graph.getGraphClass() != graphClass) {
			throw new IllegalArgumentException(
					"graph must be an instance of graphclass "
							+ graphClass.getQualifiedName()
							+ " but was instance of "
							+ graph.getGraphClass().getQualifiedName() + ".");
		}
		this.graph = graph;
	}

	public Vertex createQueryVertex(int id, String queryText) {
		Vertex v = genericGraphFactory
				.createVertex(queryVertexClass, id, graph);
		v.setAttribute(QUERY_TEXT_ATTRIBUTE, queryText);
		return v;
	}

	public Edge createDependency(int id, Vertex predecessor, Vertex successor) {
		return genericGraphFactory.createEdge(dependsOnQueryEdgeClass, id,
				graph, successor, predecessor);
	}

	/*
	 * Methods to execute all queries of a GreqlQueryDependencyGraph parallel
	 */

	public ParallelGreqlEvaluator() {

	}

	// TODO optimizer + optimizerinfo
	public ParallelGreqlEvaluator(Graph graph) {
		setGraph(graph);
	}

	public Object evaluate() {
		return evaluate(null, new GreqlEnvironmentAdapter());
	}

	public Object evaluate(Graph datagraph) {
		return evaluate(datagraph, new GreqlEnvironmentAdapter());
	}

	public Object evaluate(Graph datagraph, GreqlEnvironment environment) {
		if (graph == null) {
			throw new GreqlException(
					"There exists no graph which contains the queries and their dependencies.");
		}
		// check acyclicity
		try {
			if (!new TopologicalOrderWithDFS(graph,
					new RecursiveDepthFirstSearch(datagraph)).execute()
					.isAcyclic()) {
				throw new GreqlException(
						"The dependency graph must be acyclic.");
			}
		} catch (AlgorithmTerminatedException e1) {
			e1.printStackTrace();
		}

		// at least 2 threads, at most available processors + 1 (for the
		// resultcollector)
		int threads = Math.max(2,
				Runtime.getRuntime().availableProcessors() + 1);
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		GraphMarker<GreqlEvaluatorTask> evaluators = new GraphMarker<GreqlEvaluatorTask>(
				graph);
		IntegerVertexMarker inDegree = new IntegerVertexMarker(graph);
		Object result = null;

		ArrayList<Vertex> initialNodes = new ArrayList<Vertex>();
		ArrayList<Vertex> finalNodes = new ArrayList<Vertex>();

		// following variables are used for a KahnKnuth acyclicity check
		for (Vertex v : graph.vertices(queryVertexClass)) {
			int i = v.getDegree(dependsOnQueryEdgeClass, EdgeDirection.IN);
			inDegree.mark(v, i);
			if (i == 0) {
				initialNodes.add(v);
			}
			if (v.getDegree(dependsOnQueryEdgeClass, EdgeDirection.OUT) == 0) {
				finalNodes.add(v);
			}
			GreqlEvaluatorTask t = new GreqlEvaluatorTask(v, datagraph,
					environment);
			evaluators.mark(v, t);
		}

		FutureTask<Object> rc = new FutureTask<Object>(
				new GreqlResultCollectorCallable(finalNodes));
		executor.execute(rc);
		for (Vertex v : initialNodes) {
			executor.execute(evaluators.getMark(v));
		}
		try {
			result = rc.get();
			executor.shutdown();
			return result;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

}
