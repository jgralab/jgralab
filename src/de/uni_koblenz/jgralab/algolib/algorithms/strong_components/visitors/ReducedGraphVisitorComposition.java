package de.uni_koblenz.jgralab.algolib.algorithms.strong_components.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;
import de.uni_koblenz.jgralab.algolib.visitors.VisitorComposition;

public class ReducedGraphVisitorComposition extends VisitorComposition
		implements ReducedGraphVisitor {

	@Override
	public void addVisitor(Visitor visitor) {
		if (visitor instanceof ReducedGraphVisitor) {
			super.addVisitor(visitor);
		} else {
			throw new IllegalArgumentException(
					"This visitor composition is only compatible with implementations of "
							+ ReducedGraphVisitor.class.getSimpleName() + ".");
		}
	}

	@Override
	public void visitReducedEdge(Edge e) {
		if (visitors != null) {
			for (Visitor currentVisitor : visitors) {
				((ReducedGraphVisitor) currentVisitor).visitReducedEdge(e);
			}
		}
	}

	@Override
	public void visitRepresentativeVertex(Vertex v) {
		if (visitors != null) {
			for (Visitor currentVisitor : visitors) {
				((ReducedGraphVisitor) currentVisitor)
						.visitRepresentativeVertex(v);
			}
		}
	}
}
