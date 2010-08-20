package de.uni_koblenz.jgralabtest.algolib;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.reachability.WarshallAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.reachability.visitors.TransitiveVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.functions.BinaryFunction;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.functions.Relation;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleSchema;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleVertex;

public class TryWarshall {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleGraph graph = SimpleSchema.instance().createSimpleGraph(8, 7);
		SimpleVertex v1 = graph.createSimpleVertex();
		SimpleVertex v2 = graph.createSimpleVertex();
		SimpleVertex v3 = graph.createSimpleVertex();
		SimpleVertex v4 = graph.createSimpleVertex();
		SimpleVertex v5 = graph.createSimpleVertex();
		SimpleVertex v6 = graph.createSimpleVertex();
		SimpleVertex v7 = graph.createSimpleVertex();
		SimpleVertex v8 = graph.createSimpleVertex();

		graph.createSimpleEdge(v1, v2);
		graph.createSimpleEdge(v1, v3);
		graph.createSimpleEdge(v2, v4);
		graph.createSimpleEdge(v3, v4);
		graph.createSimpleEdge(v4, v5);
		graph.createSimpleEdge(v6, v5);
		graph.createSimpleEdge(v8, v7);

		final WarshallAlgorithm w = new WarshallAlgorithm(graph);
		TransitiveVisitorAdapter visitor = new TransitiveVisitorAdapter() {

			private Edge[][] successor = w.getInternalSuccessor();
			private IntFunction<Vertex> indexMapping = w.getIndexMapping();

			@Override
			public void visitVertexTriple(Vertex u, Vertex v, Vertex w) {
				System.out.println("From " + u + " with "
						+ successor[indexMapping.get(u)][indexMapping.get(w)]
						+ " over " + v + " eventually reaching " + w);
			}

		};
		// w.addVisitor(visitor);
		w.setSearchDirection(EdgeDirection.INOUT);
		w.execute();
		// System.out.println(w.getVertexOrder());
		// System.out.println(w.getReachabilityRelation());
		// System.out.println(w.getSuccessor());
		System.out.println();
		printResult(graph, w.getReachable(), w.getSuccessor());
		System.out.println("Fini");
	}

	private static void printResult(SimpleGraph graph,
			Relation<Vertex, Vertex> reachable,
			BinaryFunction<Vertex, Vertex, Edge> successor) {
		for (Vertex v : graph.vertices()) {
			for (Vertex w : graph.vertices()) {
				if (v != w && reachable.get(v, w)) {
					System.out.println("From " + v + " to " + w + " follow "
							+ successor.get(v, w));
				}
			}
		}
	}

}
