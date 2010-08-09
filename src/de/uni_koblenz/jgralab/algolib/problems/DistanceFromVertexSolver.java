package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;

//TODO write problem specification
//can be defined for directed and undirected graphs
public interface DistanceFromVertexSolver {

	public DistanceFromVertexSolver execute(Vertex start);

	public IntFunction<Vertex> getDistance();
}
