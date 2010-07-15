package de.uni_koblenz.jgralab.algolib.algorithms;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;

public abstract class HybridGraphAlgorithm extends GraphAlgorithm {
	public static final boolean DEFAULT_DIRECTED = true;

	protected boolean directed;

	public HybridGraphAlgorithm(Graph graph) {
		super(graph);
	}

	public HybridGraphAlgorithm(Graph graph,
			BooleanFunction<GraphElement> subgraph, boolean directed) {
		super(graph, subgraph);
		this.directed = directed;
	}

	@Override
	public boolean isDirected() {
		return directed;
	}

	public void setDirected(boolean directed) {
		this.directed = directed;
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		directed = DEFAULT_DIRECTED;
	}

}
