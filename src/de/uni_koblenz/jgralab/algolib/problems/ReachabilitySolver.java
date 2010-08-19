package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.Relation;

/**
 * The problem <b>reachability</b> can be defined for directed and undirected
 * graphs. There are no further parameters. The result is the <i>reachability
 * relation</i> that tells for every vertex pair if the second vertex is
 * reachable from the first vertex.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface ReachabilitySolver extends ProblemSolver {

	/**
	 * Solves the problem <b>reachability</b>.
	 * 
	 * @return this algorithm object.
	 */
	public ReachabilitySolver execute();

	/**
	 * Retrieves the result <code>reachability relation</code>.
	 * @return the result <code>reachability relation</code>
	 */
	public Relation<Vertex, Vertex> getReachabilityRelation();
}
