package de.uni_koblenz.jgralab.algolib.algorithms.search.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

public interface DFSVisitor extends SearchVisitor {

	/**
	 * This method visits a vertex when the algorithm "leaves" it. (After the
	 * recursive call of the search)
	 * 
	 * @param v
	 *            the vertex that is currently visited
	 */
	public void leaveVertex(Vertex v);

	/**
	 * This method visits a tree edge when the algorithm "leaves" it. (After the
	 * recursive call of the search)
	 * 
	 * @param e
	 *            the tree edge that is currently visited
	 */
	public void leaveTreeEdge(Edge e);

	/**
	 * This method visits an edge if it has been classified as forward arc.
	 * 
	 * @param e
	 *            the forward arc that is currently visited
	 */
	public void visitForwardArc(Edge e);

	/**
	 * This method visits an edge if it has been classified as backward arc.
	 * 
	 * @param e
	 *            the backward arc that is currently visited
	 */
	public void visitBackwardArc(Edge e);

	/**
	 * This method visits an edge if it has been classified as crosslink.
	 * 
	 * @param e
	 *            the crosslink that is currently visited
	 */
	public void visitCrosslink(Edge e);
}
