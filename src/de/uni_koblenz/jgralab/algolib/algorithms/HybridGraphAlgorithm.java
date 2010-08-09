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

	/**
	 * Sets the interpretation mode of the current graph.
	 * 
	 * @param directed
	 *            if true, the current graph is interpreted as a directed graph.
	 */
	public abstract void setDirected(boolean directed);

}
