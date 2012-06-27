package de.uni_koblenz.jgralab.greql.parallel;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.generic.GenericGraphFactoryImpl;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

public class ParallelGreqlEvaluator {

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
		queryVertexClass.createAttribute("queryText", schema.getStringDomain());
	}

	private static EdgeClass dependsOnQueryEdgeClass;
	static {
		dependsOnQueryEdgeClass = graphClass.createEdgeClass(
				"queries.DependsOn", queryVertexClass, 0, Integer.MAX_VALUE,
				"successor", AggregationKind.SHARED, queryVertexClass, 0,
				Integer.MAX_VALUE, "predecessor", AggregationKind.NONE);
	}

	private final GenericGraphFactoryImpl genericGraphFactory = new GenericGraphFactoryImpl(
			schema);

	private Graph graph;

	public Graph createGraph(String id, int vMax, int eMax) {
		graph = genericGraphFactory.createGraph(graphClass, id, vMax, eMax);
		return graph;
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

	public Graph getGraph() {
		return graph;
	}

	public Vertex createQueryVertex(String queryText, int id) {
		Vertex v = genericGraphFactory
				.createVertex(queryVertexClass, id, graph);
		v.setAttribute("queryText", queryText);
		return v;
	}

	public Edge createDependency(Vertex predecessor, Vertex successor, int id) {
		return genericGraphFactory.createEdge(dependsOnQueryEdgeClass, id,
				graph, successor, predecessor);
	}
}
