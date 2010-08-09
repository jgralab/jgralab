package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.Function;

//TODO write problem specification
//can be defined for directed and undirected graphs
public interface WeightedShortestPathFromVertexToVertexSolver {

	public WeightedShortestPathFromVertexToVertexSolver execute(Vertex start,
			Vertex target);

	public Function<Vertex, Edge> getParent();
}
