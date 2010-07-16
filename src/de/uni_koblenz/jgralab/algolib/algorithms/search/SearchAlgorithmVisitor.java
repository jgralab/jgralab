package de.uni_koblenz.jgralab.algolib.algorithms.search;

import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.visitors.SearchVisitorAdapter;

public class SearchAlgorithmVisitor extends SearchVisitorAdapter {

	protected SearchAlgorithm algorithm;

	@Override
	public void setAlgorithm(GraphAlgorithm algorithm) {
		if (algorithm instanceof SearchAlgorithm) {
			this.algorithm = (SearchAlgorithm) algorithm;
			reset();
		} else {
			throw new IllegalArgumentException(
					"This visitor is not compatible with "
							+ algorithm.getClass().getSimpleName()
							+ " It only works with instances of "
							+ SearchAlgorithm.class.getSimpleName());
		}
	}

	public SearchAlgorithm getAlgorithm() {
		return algorithm;
	}

}