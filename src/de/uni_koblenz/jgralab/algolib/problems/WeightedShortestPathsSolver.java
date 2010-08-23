package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.BinaryFunction;

/**
 * The problem <b>weighted shortest paths</b> can be defined for directed and
 * undirected graphs. The graph may not have negative cycles. Algorithms solving
 * this problem are not required to check this precondition. There are no
 * further parameters. The result is a binary function <i>successor</i> that
 * assigns each vertex pair an edge, which is the next edge from the first
 * vertex to follow for reaching the second vertex using a shortest path.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface WeightedShortestPathsSolver extends ProblemSolver {

	/**
	 * Solves the problem <b>weighted shortest paths</b>.
	 * 
	 * @return this algorithm object.
	 */
	public WeightedShortestPathsSolver execute();

	/**
	 * Retrieves the result <code>successor</code>.
	 * 
	 * @return the result <code>successor</code>.
	 * @throws IllegalStateException
	 *             if the result is requested without being available
	 */
	public BinaryFunction<Vertex, Vertex, Edge> getSuccessor();
}
