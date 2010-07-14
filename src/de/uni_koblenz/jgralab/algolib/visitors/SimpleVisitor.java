package de.uni_koblenz.jgralab.algolib.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

/**
 * This visitor allows visiting vertices and edges without distinguishing
 * between special vertices and edges. During an algorithm run, each method
 * should only be called at most once per vertex/edge. The algorithms have to
 * ensure this.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface SimpleVisitor {

	/**
	 * Executes arbitrary code in the context of the given vertex <code>v</code>
	 * .
	 * 
	 * @param v
	 *            the vertex that is currently visited
	 */
	public void visitVertex(Vertex v);

	/**
	 * Executes arbitrary code in the context of the given edge <code>e</code>.
	 * 
	 * @param e
	 *            the edge that is currently visited
	 */
	public void visitEdge(Edge e);

}
