package de.uni_koblenz.jgralab.greql2.funlib.graph;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.KahnKnuthAlgorithm;
import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class IsAcyclic extends Function {
	public IsAcyclic() {
		super(
				"Returns true iff the current graph or the given subgraph is cycle-free.\n"
						+ "See also: topologicalSort().", 100, 1, 0.1,
				Category.GRAPH);
	}

	public Boolean evaluate(Graph graph) {
		KahnKnuthAlgorithm a = new KahnKnuthAlgorithm(graph, null, null);
		try {
			a.execute();
		} catch (AlgorithmTerminatedException e) {
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
		return a.isAcyclic();
	}
}
