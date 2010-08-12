package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.Permutation;

/**
 * The problem <b>traversal from vertex</b> is defined for directed and
 * undirected graphs. The only further input parameter is the <i>start
 * vertex<\i>. The results are a <i>permutation of vertices<\i> and a
 * <i>permutation of edges</i> of the reachable subgraph from the start vertex.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface TraversalFromVertexSolver extends TraversalSolver {
	/**
	 * Solves the problem <b>traversal from vertex</b>.
	 * 
	 * @param root
	 *            the vertex to start the traversal at
	 * @return this algorithm object
	 */
	public TraversalFromVertexSolver execute(Vertex root);

	/**
	 * Retrieves the result <code>vertexOrder</code> as permutation of vertices.
	 * 
	 * @return the result <code>vertexOrder</code>.
	 * @throws IllegalStateException
	 *             if the result is requested without being available
	 */
	public Permutation<Vertex> getVertexOrder();

	/**
	 * Retrieves the result <code>edgeOrder</code> as permutation of edges.
	 * 
	 * @return the result <code>edgeOrder</code>.
	 * @throws IllegalStateException
	 *             if the result is requested without being available
	 */
	public Permutation<Edge> getEdgeOrder();
}
