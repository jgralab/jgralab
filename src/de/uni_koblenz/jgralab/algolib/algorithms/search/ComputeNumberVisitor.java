package de.uni_koblenz.jgralab.algolib.algorithms.search;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;

public class ComputeNumberVisitor extends SearchAlgorithmVisitor {
	
	protected IntFunction<Vertex> number;
	
	public IntFunction<Vertex> getIntermediateNumber() {
		return number;
	}

	public IntFunction<Vertex> getNumber() {
		if (algorithm.getState() == AlgorithmStates.FINISHED
				|| algorithm.getState() == AlgorithmStates.STOPPED) {
			return number;
		} else {
			throw new IllegalStateException(
					"Parameters may only be changed when in state "
							+ AlgorithmStates.INITIALIZED);
		}
	}

	@Override
	public void visitVertex(Vertex v) {
		number.set(v, algorithm.getIntermediateNum());
	}

	@Override
	public void reset() {
		number = new IntegerVertexMarker(algorithm.getGraph());
	}
	
}
