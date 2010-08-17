package de.uni_koblenz.jgralab.algolib.algorithms.strong_components.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.problems.directed.StrongComponentsSolver;

public class ReducedGraphVisitorAdapter implements ReducedGraphVisitor {

	private StrongComponentsSolver algorithm;

	@Override
	public void reset() {

	}

	@Override
	public void setAlgorithm(GraphAlgorithm alg) {
		if (algorithm instanceof StrongComponentsSolver) {
			this.algorithm = (StrongComponentsSolver) algorithm;
			reset();
		} else {
			throw new IllegalArgumentException(
					"This visitor is not compatible with "
							+ algorithm.getClass().getSimpleName()
							+ " It only works with instances of "
							+ StrongComponentsSolver.class.getSimpleName());
		}
	}

	@Override
	public void visitReducedEdge(Edge e) {
		
	}

	@Override
	public void visitRepresentativeVertex(Vertex v) {
		
	}

}
