package de.uni_koblenz.jgralab.algolib.problems.directed;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.problems.ProblemSolver;

//TODO write problem specification
public interface StrongComponentsSolver extends ProblemSolver {

	public StrongComponentsSolver execute();

	public Function<Vertex, Vertex> getStrongComponents();
}
