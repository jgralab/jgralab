package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;

/**
 * This is the super interface for all problem solvers that have an edge weight
 * function as input parameter. This function assigns each edge a weight.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface WeightedProblemSolver extends ProblemSolver {

	/**
	 * Sets the parameter <i>edge weight</i>. If this parameter is not set (set
	 * to <code>null</code>), the weight is assumed to be 1 for all edges.
	 * 
	 * @param edgeWeight
	 *            the edge weight function.
	 */
	public void setEdgeWeight(DoubleFunction<Edge> edgeWeight);

}
