package de.uni_koblenz.jgralab.algolib.problems;

/**
 * The problem <b>negative cycles</b> can be defined for directed and undirected
 * graphs. There are no further parameters. The result is a boolean value
 * <i>negative cycles</i> that is true if the graph has negative cycles.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface NegativeCyclesSolver extends WeightedProblemSolver {

	/**
	 * Solves the problem <b>negative cycles</b>.
	 * 
	 * @return this algorithm object.
	 */
	public NegativeCyclesSolver execute();

	/**
	 * Retrieves the result <code>negativeCycles</code>.
	 * 
	 * @return the result <code>negativeCycles</code>.
	 * @throws IllegalStateException
	 *             if the result is requested without being available
	 */
	public boolean hasNegativeCycles();
}
