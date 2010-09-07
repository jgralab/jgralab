package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;

/**
 * This is the super interface for all problem solvers. The common attribute for
 * all problems is a <i>graph</i>. Depending on the problem, the graph can be
 * directed or undirected. Some problems are defined for both, directed and
 * undirected graphs. The graph can be restricted to a <i>subgraph</i> by a
 * function defining which graph elements should be concerned and which graph
 * elements should be ignored. Implementations (graph algorithms) are encouraged
 * to check the integrity of the subgraph function, but they do not have to do
 * it. So it is up to the user to ensure this integrity. If an invalid subgraph
 * function is used, the results are undefined and not necessarily correct.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface ProblemSolver {

	/**
	 * Sets the parameter <i>graph</i>. Implementations have to be initialized
	 * with this parameter in the constructor. This method is only needed for
	 * changing the graph when reusing this <code>ProblemSolver</code>.
	 * 
	 * @param graph
	 *            the graph this <code>ProblemSolver</code> works with.
	 */
	public void setGraph(Graph graph);

	/**
	 * Sets the parameter <i>subgraph</i>. If this parameter is not set (or set
	 * to <code>null</code>), the whole graph is concerned.
	 * 
	 * @param subgraph
	 *            the subgraph function that restricts the graph this
	 *            <code>ProblemSolver</code> works with.
	 */
	public void setSubgraph(BooleanFunction<GraphElement> subgraph);

}
