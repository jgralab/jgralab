package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;

/**
 * This interface is the super interface for all problem solvers that solve
 * traversal problems. It does not specify the traversal problems, but it
 * introduces a further parameter, the <i>navigability</i> of edges. In some
 * situations it does not only depend on the edge for deciding whether it is
 * navigable but also on other factors like its attributes or intermediate
 * results. For this purpose, all traversal solvers provide an additional
 * parameter for allowing the decision if an edge is navigable based on such
 * information.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface TraversalSolver extends ProblemSolver {

	/**
	 * Sets the parameter <i>navigable</i>. If this parameter is not set (or set
	 * to <code>null</code>), all edges that are concerned are defined as
	 * navigable edges. This function has to be defined for all edges of the
	 * graph or else this <code>TraversalSolver</code> might fail.
	 * 
	 * @param navigable
	 *            a function that defines whether an edge is navigable or not.
	 */
	public void setNavigable(BooleanFunction<Edge> navigable);
}
