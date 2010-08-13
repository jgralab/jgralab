package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;

/**
 * The problem <b>distance from vertex</b> is defined for directed and
 * undirected graphs. The only further parameter is the <i>vertex</i> from which
 * the distance to all reachable vertices has to be computed. The result is a
 * function that contains the distances from the given vertex to all other
 * vertices.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface DistanceFromVertexSolver extends ProblemSolver {

	/**
	 * Solves the problem <b>distance from vertex</b>.
	 * 
	 * @param start
	 *            the vertex to start at.
	 * @return this algorithm object.
	 */
	public DistanceFromVertexSolver execute(Vertex start);

	/**
	 * Retrieves the result <code>distance</code> as function.
	 * 
	 * @return the result <code>distance</code>.
	 * @throws IllegalStateException
	 *             if the result is requested without being available
	 */
	public IntFunction<Vertex> getDistance();
}
