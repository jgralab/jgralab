package de.uni_koblenz.jgralab.algolib.algorithms.search.visitors;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.functions.ArrayFunction;
import de.uni_koblenz.jgralab.algolib.functions.IntDomainFunction;

public class ComputeRorderVisitor extends DFSAlgorithmVisitor {

	protected Vertex[] rorder;

	public Vertex[] getIntermediateRorder() {
		return rorder;
	}

	public IntDomainFunction<Vertex> getRorder() {
		if (algorithm.getState() == AlgorithmStates.FINISHED
				|| algorithm.getState() == AlgorithmStates.STOPPED) {
			return new ArrayFunction<Vertex>(rorder);
		} else {
			throw new IllegalStateException(
					"The result cannot be obtained while in this state: "
							+ algorithm.getState());
		}
	}

	@Override
	public void leaveVertex(Vertex v) {
		rorder[algorithm.getIntermediateRNum()] = v;
	}

	@Override
	public void reset() {
		rorder = new Vertex[algorithm.getGraph().getVCount() + 1];
	}
}
