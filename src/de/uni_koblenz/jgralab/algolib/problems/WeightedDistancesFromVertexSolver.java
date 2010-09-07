package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;

/**
 * The problem <b>weighted distances from vertex</b> can be defined for directed
 * and undirected graphs. The graph may not have negative cycles. Algorithms
 * solving this problem are not required to check this precondition. The only
 * further parameter is the <i>start vertex</i>. Implementations compute the
 * weighted shortest distances from this vertex to all reachable vertices. The
 * result is a function <i>weighted distance</i> that assigns each vertex the
 * weighted distance from the start vertex.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface WeightedDistancesFromVertexSolver extends ProblemSolver {

	/**
	 * Solves the problem <b>weighted distance from vertex</b>.
	 * 
	 * @param start
	 *            the start vertex.
	 * @return this algorithm object.
	 * @throws AlgorithmTerminatedException
	 *             if this algorithm terminated before the actual execution is
	 *             completed. This can happen from inside (early termination) or
	 *             from outside (Thread interruption). The algorithm state
	 *             changes accordingly.
	 */
	public WeightedDistancesFromVertexSolver execute(Vertex start);

	/**
	 * Retrieves the result <code>weightedDistance</code>.
	 * 
	 * @return the result <code>weightedDistance</code>.
	 * @throws IllegalStateException
	 *             if the result is requested without being available
	 */
	public DoubleFunction<Vertex> getWeightedDistance();
}
