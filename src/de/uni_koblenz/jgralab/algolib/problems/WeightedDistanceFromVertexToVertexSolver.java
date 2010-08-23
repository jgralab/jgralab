package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Vertex;

/**
 * The problem <b>weighted distance from vertex to vertex</b> can be defined for
 * directed and undirected graphs. The graph may not have negative cycles.
 * Algorithms solving this problem are not required to check this precondition.
 * The further parameters are the <i>start vertex</i> and the <i>target
 * vertex</i>. The result is a double value <i>single weighted Distance</i> that
 * contains the weighted distance from the start vertex to the target vertex.
 * start vertex to the target vertex.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface WeightedDistanceFromVertexToVertexSolver extends
		WeightedProblemSolver {

	/**
	 * Solves the problem <b>weighted distance from vertex to vertex</b>.
	 * 
	 * @param start
	 *            the start vertex.
	 * @param target
	 *            the target vertex.
	 * @return this algorithm object.
	 */
	public WeightedDistanceFromVertexToVertexSolver execute(Vertex start,
			Vertex target);

	/**
	 * Retrieves the result <code>singleWeightedDistance</code>.
	 * @return the result <code>singleWeightedDistance</code>.
	 */
	public double getSingleWeightedDistance();
}
