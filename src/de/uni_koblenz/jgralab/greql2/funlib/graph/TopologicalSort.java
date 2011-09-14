package de.uni_koblenz.jgralab.greql2.funlib.graph;

import org.pcollections.ArrayPVector;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.KahnKnuthAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.Permutation;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class TopologicalSort extends Function {
	public TopologicalSort() {
		super(
				"Returns a list of vertices in topological order if the graph is acyclic. Otherwise, the result is undefined.\n"
						+ "The topological sort can be restricted to a subgraph.\n"
						+ "See also: isAcyclic().", 100, 1, 0.1, Category.GRAPH);
	}

	public PVector<? extends Vertex> evaluate(Graph graph) {
		return evaluate(graph, null);
	}

	public PVector<? extends Vertex> evaluate(SubGraphMarker subgraph) {
		return evaluate(subgraph.getGraph(), subgraph);
	}

	private PVector<? extends Vertex> evaluate(Graph graph,
			SubGraphMarker subgraph) {
		KahnKnuthAlgorithm a = new KahnKnuthAlgorithm(graph, subgraph, null);
		try {
			a.execute();
		} catch (AlgorithmTerminatedException e) {
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
		if (!a.isAcyclic()) {
			return null;
		}
		PVector<Vertex> result = ArrayPVector.empty();
		Permutation<Vertex> t = a.getTopologicalOrder();
		for (Vertex v : t.getRangeElements()) {
			result = result.plus(v);
		}
		return result;
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return inElements;
	}
}
