package de.uni_koblenz.jgralab.algolib.problems.directed;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.Permutation;
import de.uni_koblenz.jgralab.algolib.problems.ProblemSolver;

//TODO write problem specification
public interface TopologicalOrderSolver extends ProblemSolver {
	
	public TopologicalOrderSolver execute();

	public Permutation<Vertex> getTopologicalOrder();
}
