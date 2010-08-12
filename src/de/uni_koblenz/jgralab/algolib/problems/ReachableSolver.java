package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Vertex;

//TODO write problem specification
//can be defined for directed and undirected graphs
public interface ReachableSolver extends ProblemSolver {

	public ReachableSolver execute(Vertex start, Vertex target);

	public boolean isReachable();
}
