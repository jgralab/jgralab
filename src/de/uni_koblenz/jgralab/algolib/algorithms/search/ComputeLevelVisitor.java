package de.uni_koblenz.jgralab.algolib.algorithms.search;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;

public class ComputeLevelVisitor extends SearchAlgorithmVisitor {

	protected IntFunction<Vertex> level;

	@Override
	public void visitRoot(Vertex v) {
		level.set(v, 0);
	}

	@Override
	public void visitTreeEdge(Edge e) {
		level.set(e.getThat(), level.get(e.getThis()) + 1);
	}

	@Override
	public void reset() {
		level = new IntegerVertexMarker(algorithm.getGraph());
	}

	public IntFunction<Vertex> getIntermediateLevel() {
		return level;
	}

	public IntFunction<Vertex> getLevel() {
		if (algorithm.getState() == AlgorithmStates.FINISHED
				|| algorithm.getState() == AlgorithmStates.STOPPED) {
			return level;
		} else {
			throw new IllegalStateException(
					"Parameters may only be changed when in state "
							+ AlgorithmStates.INITIALIZED);
		}
	}

}
