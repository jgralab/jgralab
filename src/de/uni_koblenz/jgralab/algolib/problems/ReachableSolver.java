package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Vertex;

/**
 * The problem <b>reachable</b> is defined for directed and undirected graphs.
 * The further parameters are the <i>start vertex</i> and the <i>target
 * vertex</i>. The result <i>reachable</i> is a boolean value that tells if the
 * target vertex is reachable from the start vertex.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface ReachableSolver extends ProblemSolver {

	/**
	 * Solves the problem <b>reachable</b>.
	 * 
	 * @param start
	 *            the start vertex
	 * @param target
	 *            the target vertex
	 * @return this algorithm object
	 * @throws AlgorithmTerminatedException
	 *             if this algorithm terminated before the actual execution is
	 *             completed. This can happen from inside (early termination) or
	 *             from outside (Thread interruption). The algorithm state
	 *             changes accordingly.
	 */
	public ReachableSolver execute(Vertex start, Vertex target);

	/**
	 * Retrieves the result <code>reachable</code>.
	 * 
	 * @return the result <code>reachable</code>.
	 * @throws IllegalStateException
	 *             if the result is requested without being available
	 */
	public boolean isReachable();
}
