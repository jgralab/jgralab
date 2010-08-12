package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;

//TODO write problem specification
//can be defined for directed and undirected graphs
public interface WeightedDistanceFromVertexSolver extends ProblemSolver {

	public WeightedDistanceFromVertexSolver execute(Vertex start);

	public DoubleFunction<Vertex> getDistance();
}
