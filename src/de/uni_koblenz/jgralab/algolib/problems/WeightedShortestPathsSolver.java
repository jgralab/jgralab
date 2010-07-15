package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.algolib.VertexPair;
import de.uni_koblenz.jgralab.algolib.functions.Function;

//TODO write problem specification
//can be defined for directed and undirected graphs
public interface WeightedShortestPathsSolver {

	public void solveWeightedShortestPaths();

	public Function<VertexPair, Edge> getSuccessorRelation();
}
