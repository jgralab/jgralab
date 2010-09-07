package de.uni_koblenz.jgralab.algolib.algorithms.reachability.visitors;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

/**
 * This visitor can be used by all algorithms that compute the reachability
 * relation using the transitive closure.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface TransitiveVisitor extends Visitor {

	/**
	 * Visits the vertex triple (u,v,w). Each vertex triple may only be visited
	 * at most once.
	 * 
	 * @param u
	 *            the first vertex
	 * @param v
	 *            the second vertex
	 * @param w
	 *            the third vertex
	 */
	public void visitVertexTriple(Vertex u, Vertex v, Vertex w);
}
