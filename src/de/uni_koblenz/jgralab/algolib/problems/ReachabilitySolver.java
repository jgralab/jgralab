package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;

//TODO write problem specification
//can be defined for directed and undirected graphs
public interface ReachabilitySolver extends ProblemSolver {

	public ReachabilitySolver execute();

	public BooleanFunction<Vertex> getReachabilityRelation();
}
