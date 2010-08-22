package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.BinaryDoubleFunction;

/**
 * The problem <b>weighted distances</b> can be defined for directed and
 * undirected graphs. The graph may not have negative cycles. Algorithms solving
 * this problem are not required to check this precondition. There are no
 * further parameters. The result is a binary function <i>weighted distance</i>
 * that assigns each vertex pair the weighted distance from the first vertex to
 * the second vertex using a shortest path.
 * 
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface WeightedDistancesSolver extends WeightedProblemSolver {

	/**
	 * Solves the problem <b>weighted distances</b>.
	 * 
	 * @return this algorithm object.
	 */
	public WeightedDistancesSolver execute();

	/**
	 * Retrieves the result <code>weightedDistance</code>.
	 * 
	 * @return the result <code>weightedDistance</code>.
	 */
	public BinaryDoubleFunction<Vertex, Vertex> getWeightedDistance();

}
