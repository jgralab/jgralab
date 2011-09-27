package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;

/**
 * The problem <b> IsTree </b> is defined for undirected graphs only. There are
 * no further parameters. The result is a boolean value that tells whether the
 * graph is a tree or not.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface IsTreeSolver extends ProblemSolver {

	/**
	 * Solves the problem IsTree.
	 * 
	 * @return this algorithm object.
	 * @throws AlgorithmTerminatedException
	 *             if this algorithm terminated before the actual execution is
	 *             completed. This can happen from inside (early termination) or
	 *             from outside (Thread interruption). The algorithm state
	 *             changes accordingly.
	 */
	public IsTreeSolver execute() throws AlgorithmTerminatedException;

	/**
	 * Retrieves the result <code>isTree</code>
	 * 
	 * @return the result <code>isTree</code>
	 * @throws IllegalStateException
	 *             if the result is requested without being available.
	 */
	public boolean isTree() throws IllegalStateException;

}
