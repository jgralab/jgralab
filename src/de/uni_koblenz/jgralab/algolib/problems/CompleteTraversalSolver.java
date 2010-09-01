package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.Permutation;

/**
 * The problem <b>complete traversal</b> is defined for directed and undirected
 * graphs. There are no further parameters. The results are a <i>permutation of
 * vertices<\i> and a <i>permutation of edges</i> of the whole graph.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface CompleteTraversalSolver extends TraversalSolver {

	/**
	 * Solves the problem <b>complete traversal</b>.
	 * 
	 * @return this algorithm object
	 * @throws AlgorithmTerminatedException
	 *             if this algorithm terminated before the actual execution is
	 *             completed. This can happen from inside (early termination) or
	 *             from outside (Thread interruption). The algorithm state
	 *             changes accordingly.
	 */
	public CompleteTraversalSolver execute();

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
