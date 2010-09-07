package de.uni_koblenz.jgralab.algolib.problems.directed;

import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.problems.ProblemSolver;

/**
 * The problem <b>acyclicity</b> is defined for directed graphs only. There are
 * no further parameters. The result is a boolean value <i>acyclic</i> that is
 * true if the graph is acyclic.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface AcyclicitySolver extends ProblemSolver {

	/**
	 * Solves the problem <b>acyclicity</b>.
	 * 
	 * @return this algorithm object.
	 * @throws AlgorithmTerminatedException
	 *             if this algorithm terminated before the actual execution is
	 *             completed. This can happen from inside (early termination) or
	 *             from outside (Thread interruption). The algorithm state
	 *             changes accordingly.
	 */
	public AcyclicitySolver execute();

	/**
	 * Retrieves the result <i>acyclic</i> as boolean value.
	 * 
	 * @return the result <i>acyclic</i>.
	 * @throws IllegalStateException
	 *             if the result is requested without being available.
	 */
	public boolean isAcyclic();
}
