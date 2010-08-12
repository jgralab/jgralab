package de.uni_koblenz.jgralab.algolib.algorithms.search.visitors;

import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.SearchAlgorithm;
import de.uni_koblenz.jgralab.algolib.visitors.DFSVisitorAdapter;

/**
 * Handles the storage of the DFS algorithm object for all implementations of
 * <code>DFSVisitor</code>. This class should be used as superclass instead of
 * <code>DFSVisitorAdapter</code>.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public class DFSAlgorithmVisitor extends DFSVisitorAdapter {

	/**
	 * The DFS this visitor is used by.
	 */
	protected DepthFirstSearch algorithm;

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
