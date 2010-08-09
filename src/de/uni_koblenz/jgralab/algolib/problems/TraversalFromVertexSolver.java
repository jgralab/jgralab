package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.IntDomainFunction;

public interface TraversalFromVertexSolver {
	/**
	 * Traverses the reachable subgraph from <code>root</code> and computes the
	 * order the vertices and edges are visited. The graph can be directed or
	 * undirected.
	 * 
	 * @param root
	 *            the vertex to start the traversal at
	 */
	public TraversalFromVertexSolver execute(Vertex root);

	/**
	 * <code>vertexOrder</code> represents the order the vertices have been
	 * visited.
	 * 
	 * @return the result <code>vertexOrder</code>.
	 */
	public IntDomainFunction<Vertex> getVertexOrder();

	/**
	 * <code>edgeOrder</code> represents the order the edges have been visited.
	 * 
	 * @return the result <code>edgeOrder</code>.
	 */
	public IntDomainFunction<Edge> getEdgeOrder();
}
