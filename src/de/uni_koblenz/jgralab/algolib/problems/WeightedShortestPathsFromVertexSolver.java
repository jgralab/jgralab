package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.Function;

/**
 * The problem <b>weighted shortest paths from vertex</b> can be defined for
 * directed and undirected graphs. The graph may not have negative cycles.
 * Algorithms solving this problem are not required to check this precondition.
 * The only further parameter is the <i>start vertex</i>. The result is the
 * function <i>parent</i> that describes a path system (tree) that contains all
 * shortest paths from the given vertex to all reachable vertices.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface WeightedShortestPathsFromVertexSolver extends ProblemSolver {

	/**
	 * Solves the problem <b>weighted shortest paths from vertex</b>.
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
	public WeightedShortestPathsFromVertexSolver execute(Vertex start);

	/**
	 * Retrieves the result <code>parent</code>.
	 * 
	 * @return the result <code>parent</code>.
	 * @throws IllegalStateException
	 *             if the result is requested without being available
	 */
	public Function<Vertex, Edge> getParent();
}
