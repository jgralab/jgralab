package de.uni_koblenz.jgralab.greql.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.RecursiveDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.TopologicalOrderWithDFS;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.MapVertexMarker;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
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

	private Graph graph;

	public Graph getGraph() {
		return graph;
	}

	public Graph createGraph(String id, int vMax, int eMax) {
		graph = new GenericGraphFactoryImpl(schema).createGraph(graphClass, id,
				vMax, eMax);
		greqlQueriesMarker = new MapVertexMarker<GreqlQuery>(graph);
		return graph;
	}

	public Graph createGraph() {
		return createGraph(null, 1000, 1000);
	}

	public Vertex createQueryVertex(String queryText) {
		return createQueryVertex(GreqlQuery.createQuery(queryText));
	}

	public Vertex createQueryVertex(GreqlQuery query) {
		Vertex v = graph.createVertex(queryVertexClass);
		greqlQueriesMarker.mark(v, query);
		return v;
	}

	public Edge createDependency(Vertex predecessor, Vertex successor) {
		return graph
				.createEdge(dependsOnQueryEdgeClass, successor, predecessor);
	}

	/*
	 * Methods to execute all queries of a GreqlQueryDependencyGraph parallel
	 */

	private MapVertexMarker<GreqlQuery> greqlQueriesMarker;

	private IntegerVertexMarker inDegree;

	private ExecutorService executor;

	private GraphMarker<GreqlEvaluatorTask> evaluators;

	private RuntimeException exception;

	public ParallelGreqlEvaluator() {

	}

	public GreqlEnvironmentAdapter evaluate() {
		GreqlEnvironmentAdapter environment = new GreqlEnvironmentAdapter();
		evaluate(null, environment);
		return environment;
	}

	public GreqlEnvironment evaluate(Graph datagraph) {
		GreqlEnvironmentAdapter environment = new GreqlEnvironmentAdapter();
		evaluate(datagraph, environment);
		return environment;
	}

	public GreqlEnvironment evaluate(Graph datagraph,
			GreqlEnvironment environment) {
		if (graph == null) {
			throw new GreqlException(
					"There exists no graph which contains the queries and their dependencies.");
		}
		// check acyclicity
		try {
			if (!new TopologicalOrderWithDFS(graph,
					new RecursiveDepthFirstSearch(graph)).execute().isAcyclic()) {
				throw new GreqlException(
						"The dependency graph must be acyclic.");
			}
		} catch (AlgorithmTerminatedException e1) {
			e1.printStackTrace();
		}

		long graphVersion = graph.getGraphVersion();// TODO check graphVersions

		// at least 2 threads, at most available processors + 1 (for the
		// resultcollector)
		int threads = Math.max(2,
				Runtime.getRuntime().availableProcessors() + 1);
		executor = Executors.newFixedThreadPool(threads);
		evaluators = new GraphMarker<GreqlEvaluatorTask>(graph);
		inDegree = new IntegerVertexMarker(graph);

		List<Vertex> initialNodes = new ArrayList<Vertex>();
		List<GreqlEvaluatorTask> finalEvaluators = new ArrayList<GreqlEvaluatorTask>();

		for (Vertex v : graph.vertices(queryVertexClass)) {
			GreqlEvaluatorTask t = new GreqlEvaluatorTask(
					greqlQueriesMarker.getMark(v), datagraph, environment, v,
					graphVersion, this);
			evaluators.mark(v, t);
			int i = v.getDegree(dependsOnQueryEdgeClass, EdgeDirection.OUT);
			inDegree.mark(v, i);
			if (i == 0) {
				initialNodes.add(v);
			}
			if (v.getDegree(dependsOnQueryEdgeClass, EdgeDirection.IN) == 0) {
				finalEvaluators.add(t);
			}
		}

		FutureTask<Void> rc = new FutureTask<Void>(
				new GreqlResultCollectorCallable(finalEvaluators));
		executor.execute(rc);
		for (Vertex v : initialNodes) {
			executor.execute(evaluators.getMark(v));
		}
		try {
			rc.get();
			executor.shutdown();
			return environment;
		} catch (InterruptedException e) {
			// e.printStackTrace();
			shutdownNow();
			if (exception != null) {
				throw exception;
			}
			return environment;
		} catch (ExecutionException e) {
			// e.printStackTrace();
			shutdownNow();
			if (exception != null) {
				throw exception;
			}
			return environment;
		}
	}

	public synchronized void shutdownNow() {
		executor.shutdownNow();
	}

	public synchronized void shutdownNow(Throwable t) {
		shutdownNow();
		if (t instanceof RuntimeException) {
			assert exception == null : "previous:\n" + exception.toString()
					+ "\ncurrent:\n" + t.toString();
			exception = (RuntimeException) t;
		}
	}

	public void scheduleNext(Vertex dependencyVertex) {
		for (Edge isDependingOne : dependencyVertex.incidences(
				dependsOnQueryEdgeClass, EdgeDirection.IN)) {
			Vertex s = isDependingOne.getThat();
			synchronized (dependencyVertex.getGraph()) {
				int i = inDegree.getMark(s) - 1;
				inDegree.mark(s, i);
				if (i == 0) {
					try {
						if (exception == null) {
							executor.execute(evaluators.getMark(s));
						}
					} catch (RejectedExecutionException e) {
						break;
					}
				}
			}
		}
	}

	public RuntimeException getException() {
		return exception;
	}

}
