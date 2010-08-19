package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.BinaryFunction;

/**
 * The problem <b>simple paths</b> can be defined for directed and undirected
 * graphs. There are no further parameters. The result is a binary function
 * <i>successor</i> that assigns each vertex pair an edge, which is the next
 * edge from the first vertex to follow for reaching the second vertex. The
 * simple paths that can be extracted from this function are <b>not</b>
 * necessarily shortest paths.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface SimplePathsSolver extends ProblemSolver {

	/**
	 * Solves the problem <b>simple paths</b>.
	 * 
	 * @return this algorithm object.
	 */
	public SimplePathsSolver execute();

	/**
	 * Retrieves the result <code>successor</code>.
	 * 
	 * @return the result <code>successor</code>.
	 */
	public BinaryFunction<Vertex, Vertex, Edge> getSuccessor();
}
