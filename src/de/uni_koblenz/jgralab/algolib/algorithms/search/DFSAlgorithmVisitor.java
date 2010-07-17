package de.uni_koblenz.jgralab.algolib.algorithms.search;

import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.visitors.DFSVisitorAdapter;

public class DFSAlgorithmVisitor extends DFSVisitorAdapter {

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
