package de.uni_koblenz.jgralab.algolib.algorithms.reachability.visitors;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;

public class TransitiveVisitorAdapter implements TransitiveVisitor {

	@Override
	public void visitVertexTriple(Vertex u, Vertex v, Vertex w) {
	}

	@Override
	public void reset() {
	}

	@Override
	public void setAlgorithm(GraphAlgorithm alg) {
	}

}
