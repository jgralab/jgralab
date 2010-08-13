package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.Function;

/**
 * The problem <b>shortest paths from vertex</b> is defined for directed and
 * undirected graphs. The only further parameter is the <i>vertex</i> from which
 * the shortest paths to all reachable vertices has to be computed. The result
 * is the function parent that describes a path system (tree) that contains all
 * shortest paths from the given vertex to all reachable vertices.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface ShortestPathsFromVertexSolver extends ProblemSolver {

	/**
	 * Solves the problem <b>shortest paths from vertex</b>.
	 * 
	 * @param start
	 *            the vertex to start at.
	 * @return this algorithm object
	 */
	public ShortestPathsFromVertexSolver execute(Vertex start);

	/**
	 * Retrieves the result <code>parent</code> as function.
	 * 
	 * @return the result <code>parent</code>.
	 * @throws IllegalStateException
	 *             if the result is requested without being available
	 */
	public Function<Vertex, Edge> getParent();

}
