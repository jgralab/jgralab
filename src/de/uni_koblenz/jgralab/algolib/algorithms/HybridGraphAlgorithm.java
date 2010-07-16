package de.uni_koblenz.jgralab.algolib.algorithms;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;

public abstract class HybridGraphAlgorithm extends GraphAlgorithm {
	
	public HybridGraphAlgorithm(Graph graph) {
		super(graph);
	}

	public HybridGraphAlgorithm(Graph graph,
			BooleanFunction<GraphElement> subgraph, boolean directed) {
		super(graph, subgraph);
		setDirected(directed);
	}

	public abstract void setDirected(boolean directed);

}
