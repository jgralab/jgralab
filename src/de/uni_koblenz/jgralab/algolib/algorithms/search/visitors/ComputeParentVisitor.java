package de.uni_koblenz.jgralab.algolib.algorithms.search.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.graphmarker.ArrayVertexMarker;

public class ComputeParentVisitor extends SearchAlgorithmVisitor {
	protected Function<Vertex, Edge> parent;

	public Function<Vertex, Edge> getIntermediateParent() {
		return parent;
	}

	public Function<Vertex, Edge> getParent() {
		if (algorithm.getState() == AlgorithmStates.FINISHED
				|| algorithm.getState() == AlgorithmStates.STOPPED) {
			return parent;
		} else {
			throw new IllegalStateException(
					"The result cannot be obtained while in this state: "
							+ algorithm.getState());
		}
	}

	@Override
	public void visitTreeEdge(Edge e) {
		parent.set(e.getThat(), e);
	}

	@Override
	public void reset() {
		parent = new ArrayVertexMarker<Edge>(algorithm.getGraph());
	}

}
