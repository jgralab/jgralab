package de.uni_koblenz.jgralab.algolib.problems.directed;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.Permutation;
import de.uni_koblenz.jgralab.algolib.problems.ProblemSolver;

/**
 * The problem <b>topological order</b> is defined for directed graphs only.
 * There are no further parameters. The result is a permutation of all vertices
 * in the (sub)graph in topological order. If the (sub)graph is cyclic, the
 * result is undefined.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface TopologicalOrderSolver extends ProblemSolver {

	/**
	 * Solves the problem <i>topological order</i>.
	 * 
	 * @return this algorithm object
	 * @throws AlgorithmTerminatedException
	 *             if this algorithm terminated before the actual execution is
	 *             completed. This can happen from inside (early termination) or
	 *             from outside (Thread interruption). The algorithm state
	 *             changes accordingly.
	 */
	public TopologicalOrderSolver execute();

	/**
	 * Retrieves the result <i>topologicalOrder</i>.
	 * 
	 * @return the result <i>topologicalOrder</i>.
	 * @throws IllegalStateException
	 *             if the result is requested without being available.
	 */
	public Permutation<Vertex> getTopologicalOrder();
}
