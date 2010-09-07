package de.uni_koblenz.jgralab.algolib.algorithms.strong_components.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

/**
 * This visitor visits all graph elements of a graph that are relevant for
 * computing a reduced graph. It can be used during the computation of strong
 * components for performing tasks whenever a representative vertex of a strong
 * component or a reduced edge (edge connecting two strong components) is
 * encountered. Each graph element can be visited at most once.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface ReducedGraphVisitor extends Visitor {

	/**
	 * Visits the representative vertex of a strong component.
	 * 
	 * @param v
	 *            the representative vertex to visit
	 */
	public void visitRepresentativeVertex(Vertex v);

	/**
	 * Visits a reduced edge.
	 * 
	 * @param e
	 *            the reduced edge to visit
	 */
	public void visitReducedEdge(Edge e);
}
