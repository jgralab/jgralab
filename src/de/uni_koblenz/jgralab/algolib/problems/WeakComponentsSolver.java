package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.functions.Function;

/**
 * The problem <b>weak components</b> is defined for undirected graphs only.
 * There are no further parameters. The result <i>weakComponents</i> is a
 * representative function that may use an arbitrary vertex of a component as
 * representative vertex.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface WeakComponentsSolver extends ProblemSolver {
	/**
	 * Solves the problem <b>weak components</b>.
	 * 
	 * @return this algorithm object.
	 * @throws AlgorithmTerminatedException
	 *             if this algorithm terminated before the actual execution is
	 *             completed. This can happen from inside (early termination) or
	 *             from outside (Thread interruption). The algorithm state
	 *             changes accordingly.
	 */
	public WeakComponentsSolver execute() throws AlgorithmTerminatedException;

	/**
	 * Retrieves the result <code>weakComponents</code>.
	 * 
	 * @return the result <code>weakComponents</code>.
	 * @throws IllegalStateException
	 *             if the result is requested without being available.
	 */
	public Function<Vertex, Vertex> getWeakComponents();
	
	/**
	 * Retrieves the result <code>kappa</code>.
	 * 
	 * @return the result <code>kappa</code>.
	 * @throws IllegalStateException
	 *             if the result is requested without being available.
	 */
	public int getKappa();
}
