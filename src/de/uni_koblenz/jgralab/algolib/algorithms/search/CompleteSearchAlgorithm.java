package de.uni_koblenz.jgralab.algolib.algorithms.search;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.problems.CompleteTraversalSolver;

public abstract class CompleteSearchAlgorithm extends SearchAlgorithm implements
		CompleteTraversalSolver {

	public CompleteSearchAlgorithm(Graph graph,
			BooleanFunction<GraphElement> subgraph, boolean directed,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph, directed, navigable);
	}

	public CompleteSearchAlgorithm(Graph graph) {
		super(graph);
	}

	@Override
	public CompleteSearchAlgorithm execute() {
		for (Vertex currentRoot : graph.vertices()) {
			execute(currentRoot);
		}
		assert (state == AlgorithmStates.FINISHED);
		return this;
	}

}
