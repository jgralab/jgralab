package de.uni_koblenz.jgralab.greql2.funlib.graph;

import org.pcollections.PVector;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class TopologicalSort extends Function {
	public TopologicalSort() {
		super(
				"Returns a list of vertices in topological order if the graph is acyclic. Otherwise, the result is undefined.\n"
						+ "The topological sort can be restricted to a subgraph.\n"
						+ "See also: isAcyclic().", 100, 1, 0.1, Category.GRAPH);
	}

	public PVector<? extends Vertex> evaluate(Graph graph) {
		return evaluate(graph);
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return inElements;
	}
}
