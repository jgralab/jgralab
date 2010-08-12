package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.Function;

//TODO write problem specification
//can be defined for directed and undirected graphs
public interface WeightedShortestPathsFromVertexSolver extends ProblemSolver {

	public WeightedShortestPathsFromVertexSolver execute(Vertex start);

	public Function<Vertex, Edge> getParent();
}
