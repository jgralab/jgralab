package de.uni_koblenz.jgralab.algolib.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

public class SimpleVisitorComposition extends VisitorComposition implements
		SimpleVisitor {

	@Override
	public void visitEdge(Edge e) {
		if (visitors != null) {
			for (Visitor currentVisitor : visitors) {
				if (currentVisitor instanceof SimpleVisitor) {
					((SimpleVisitor) currentVisitor).visitEdge(e);
				}
			}
		}
	}

	@Override
	public void visitVertex(Vertex v) {
		if (visitors != null) {
			for (Visitor currentVisitor : visitors) {
				if (currentVisitor instanceof SimpleVisitor) {
					((SimpleVisitor) currentVisitor).visitVertex(v);
				}
			}
		}
	}

}
