package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.IntDomainFunction;

//TODO write problem specification
//can be defined for directed and undirected graphs
public interface CompleteTraversalSolver {

	public CompleteTraversalSolver execute();

	public IntDomainFunction<Vertex> getVertexOrder();

	public IntDomainFunction<Edge> getEdgeOrder();
}
