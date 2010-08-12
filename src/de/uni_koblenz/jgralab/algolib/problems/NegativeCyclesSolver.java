package de.uni_koblenz.jgralab.algolib.problems;

//TODO write problem specification
//can be defined for directed and undirected graphs
public interface NegativeCyclesSolver extends ProblemSolver {

	public NegativeCyclesSolver execute();

	public boolean hasNegativeCycles();
}
