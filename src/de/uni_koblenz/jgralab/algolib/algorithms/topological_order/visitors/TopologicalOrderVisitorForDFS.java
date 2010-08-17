package de.uni_koblenz.jgralab.algolib.algorithms.topological_order.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.DFSVisitor;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.DFSVisitorAdapter;

/**
 * This is an adapter class for allowing DFS algorithms to solve the problems
 * <b>topological order</b> and <b>acyclicity</b>. It detects cycles (and
 * terminates the algorithm when encountering one) and adapts a
 * <code>TopologicalOrderVisitor</code> to a <code>DFSVisitor</code>. However,
 * this only works, if the search direction of the DFS is set to
 * <code>EdgeDirection.IN</code>, meaning the graph is <b>searched
 * backwards</b>. Otherwise the vertices are not visited in topological order,
 * but in a reverse topological order. If multiple visitors should be adapted at
 * once, an instance of <code>TopologicalOrderVisitorComposition</code> can be
 * used. This visitor adapter is only needed, if multiple problems are solved by
 * a single run of a DFS. If only the problem <b>topological order</b> needs to
 * be solved, please use the implementation <code>KahnKnuthAlgorithm</code> or
 * <code>TopologicalOrderWithDFS</code>. The latter uses an instance of this
 * class for solving the problem and adjusts the DFS properly.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public class TopologicalOrderVisitorForDFS extends DFSVisitorAdapter implements
		DFSVisitor {

	private boolean acyclic;

	private TopologicalOrderVisitor torderVisitor;

	public TopologicalOrderVisitorForDFS(TopologicalOrderVisitor torderVisitor) {
		this.torderVisitor = torderVisitor;
		acyclic = false;
	}

	@Override
	public void setAlgorithm(GraphAlgorithm algorithm) {
		super.setAlgorithm(algorithm);
		torderVisitor.setAlgorithm(algorithm);
	}

	public boolean isAcyclic() {
		return acyclic;
	}

	@Override
	public void visitBackwardArc(Edge e) {
		acyclic = false;
		throw new AlgorithmTerminatedException();
	}

	@Override
	public void leaveVertex(Vertex v) {
		torderVisitor.visitVertexInTopologicalOrder(v);
	}

}
