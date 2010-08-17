package de.uni_koblenz.jgralab.algolib.algorithms.topological_order.visitors;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

/**
 * This visitor allows visiting vertices in topological order. It can only be
 * used by algorithms solving the problem <b>topological order</b>. If the graph
 * is acyclic, each vertex in the graph is visited exactly once. The order the
 * vertices are visited corresponds to a topological order.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface TopologicalOrderVisitor extends Visitor {

	/**
	 * Visits a vertex in topological order.
	 * 
	 * * @param v the vertex to visit
	 */
	public void visitVertexInTopologicalOrder(Vertex v);
}
