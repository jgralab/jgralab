package de.uni_koblenz.jgralab.algolib.algorithms.search.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.visitors.GraphVisitor;

/**
 * This visitor allows visiting vertices and edges during the run of an
 * arbitrary search algorithm.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface SearchVisitor extends GraphVisitor {

	/**
	 * Visits one vertex that is the root of a search tree. Vertices should only
	 * be visited at most once by this method during a run of an algorithm.
	 * <code>v</code> should also be visited by <code>visitVertex</code>.
	 * 
	 * @param v
	 *            the root vertex of a search tree, which is currently visited
	 */
	public void visitRoot(Vertex v);

	/**
	 * Visits a tree edge in the search tree. An edge is either a tree edge or a
	 * frond. So it is either visited by this method or by
	 * <code>visitFrond</code>.
	 * 
	 * @param e
	 *            the tree edge that is currently visited
	 */
	public void visitTreeEdge(Edge e);

	/**
	 * Visits a frond in the search tree. An edge is either a frond or a tree
	 * edge. So it is either visited by this method or by
	 * <code>visitTreeEdge</code> (which is responsible for visiting tree
	 * edges).
	 * 
	 * @param e
	 *            the frond that is currently visited
	 */
	public void visitFrond(Edge e);

}
