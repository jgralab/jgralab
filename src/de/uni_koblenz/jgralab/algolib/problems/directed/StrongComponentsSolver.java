package de.uni_koblenz.jgralab.algolib.problems.directed;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.problems.ProblemSolver;

/**
 * The problem <b>strong components</b> is defined for directed graphs only.
 * There are no further parameters. The result <i>strongComponents</i> is a
 * representative function that uses the strong roots as representative
 * vertices.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface StrongComponentsSolver extends ProblemSolver {

	/**
	 * Solves the problem <b>strong components</b>.
	 * 
	 * @return this algorithm object.
	 * @throws AlgorithmTerminatedException
	 *             if this algorithm terminated before the actual execution is
	 *             completed. This can happen from inside (early termination) or
	 *             from outside (Thread interruption). The algorithm state
	 *             changes accordingly.
	 */
	public StrongComponentsSolver execute();

	/**
	 * Retrieves the result <code>strongComponents</code>.
	 * 
	 * @return the result <code>strongComponents</code>.
	 * @throws IllegalStateException
	 *             if the result is requested without being available.
	 */
	public Function<Vertex, Vertex> getStrongComponents();
}
