package de.uni_koblenz.jgralab.greql2.funlib.graph;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.IterativeDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.TopologicalOrderWithDFS;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.funlib.NeedsGraphArgument;

@NeedsGraphArgument
public class IsAcyclic extends Function {
	public IsAcyclic() {
		super("Returns true, iff the graph is acyclic.", 100, 1, 0.1,
				Category.GRAPH);
	}

	public Boolean evaluate(Graph g) {
		DepthFirstSearch dfs = new IterativeDepthFirstSearch(g);
		TopologicalOrderWithDFS a = new TopologicalOrderWithDFS(g, dfs);
		try {
			a.execute();
		} catch (AlgorithmTerminatedException e) {
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
		return a.isAcyclic();
	}
}
