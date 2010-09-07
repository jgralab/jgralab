package de.uni_koblenz.jgralab.algolib.algorithms.reachability.visitors;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;
import de.uni_koblenz.jgralab.algolib.visitors.VisitorComposition;

public class TransitiveVisitorComposition extends VisitorComposition implements
		TransitiveVisitor {

	@Override
	public void visitVertexTriple(Vertex u, Vertex v, Vertex w) {
		if (visitors != null) {
			for (Visitor currentVisitor : visitors) {
				((TransitiveVisitor) currentVisitor)
						.visitVertexTriple(u, v, w);
			}
		}
	}

	@Override
	public void addVisitor(Visitor visitor) {
		if (visitor instanceof TransitiveVisitor) {
			super.addVisitor(visitor);
		} else {
			throw new IllegalArgumentException(
					"The given visitor is incompatiple with this visitor composition.");
		}
	}

}
