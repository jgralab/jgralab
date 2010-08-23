package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.Function;

/**
 * The problem <b>weighted shortest path from vertex to vertex</b> can be
 * defined for directed and undirected graphs. The graph may not have negative
 * cycles. Algorithms solving this problem are not required to check this
 * precondition. The further parameters are the <i>start vertex</i> and the
 * <i>target vertex</i>. The result is the function <i>parent</i> that describes
 * an incomplete path system (tree) that contains paths from the given vertex to
 * some reachable vertices. It is guaranteed to contain a shortest path from the
 * start vertex to the target vertex.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface WeightedShortestPathFromVertexToVertexSolver extends
		ProblemSolver {

	/**
	 * Solves the problem <b>weighted shortest path from vertex to vertex</b>.
	 * 
	 * @param start
	 *            the start vertex
	 * @param target
	 *            the target vertex
	 * @return this algorithm object.
	 */
	public WeightedShortestPathFromVertexToVertexSolver execute(Vertex start,
			Vertex target);

	/**
	 * Retrieves the result <i>parent</i>.
	 * 
	 * @return the result <i>parent</i>.
	 * @throws IllegalStateException
	 *             if the result is requested without being available
	 */
	public Function<Vertex, Edge> getParent();
}
