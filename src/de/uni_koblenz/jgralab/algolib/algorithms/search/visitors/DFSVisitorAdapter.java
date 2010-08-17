package de.uni_koblenz.jgralab.algolib.algorithms.search.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.SearchAlgorithm;

/**
 * It implements all methods from <code>DFSVisitor</code> as empty stubs. Beyond
 * that, it also handles the storage of the DFS algorithm object implementing
 * visitors are used by. All instances of <code>DFSVisitor</code> should use
 * this class as superclass.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public class DFSVisitorAdapter extends SearchVisitorAdapter implements
		DFSVisitor {

	/**
	 * The DFS this visitor is used by.
	 */
	protected DepthFirstSearch algorithm;

	@Override
	public void leaveTreeEdge(Edge e) {

	}

	@Override
	public void leaveVertex(Vertex v) {

	}

	@Override
	public void visitBackwardArc(Edge e) {

	}

	@Override
	public void visitCrosslink(Edge e) {

	}

	@Override
	public void visitForwardArc(Edge e) {

	}

	@Override
	public void setAlgorithm(GraphAlgorithm algorithm) {
		if (algorithm instanceof SearchAlgorithm) {
			this.algorithm = (DepthFirstSearch) algorithm;
			reset();
		} else {
			throw new IllegalArgumentException(
					"This visitor is not compatible with "
							+ algorithm.getClass().getSimpleName()
							+ " It only works with instances of "
							+ DepthFirstSearch.class.getSimpleName());
		}
	}

	public DepthFirstSearch getAlgorithm() {
		return algorithm;
	}

}
