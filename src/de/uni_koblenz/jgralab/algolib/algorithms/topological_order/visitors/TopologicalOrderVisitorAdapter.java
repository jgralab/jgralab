package de.uni_koblenz.jgralab.algolib.algorithms.topological_order.visitors;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.problems.directed.TopologicalOrderSolver;

public class TopologicalOrderVisitorAdapter implements TopologicalOrderVisitor {

	protected TopologicalOrderSolver algorithm;
	
	@Override
	public void visitVertexInTopologicalOrder(Vertex v) {
		
	}

	@Override
	public void reset() {
		
	}

	@Override
	public void setAlgorithm(GraphAlgorithm algorithm) {
		if (algorithm instanceof TopologicalOrderSolver) {
			this.algorithm = (TopologicalOrderSolver) algorithm;
			reset();
		} else {
			throw new IllegalArgumentException(
					"This visitor is not compatible with "
							+ algorithm.getClass().getSimpleName()
							+ " It only works with instances of "
							+ TopologicalOrderSolver.class.getSimpleName());
		}
	}

}
