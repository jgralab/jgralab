package de.uni_koblenz.jgralab.algolib.problems.directed;

import de.uni_koblenz.jgralab.algolib.problems.ProblemSolver;

//TODO write problem specification
public interface AcyclicitySolver extends ProblemSolver {

	public AcyclicitySolver execute();

	public boolean isAcyclic();
}
