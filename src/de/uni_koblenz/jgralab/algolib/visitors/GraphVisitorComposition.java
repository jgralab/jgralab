package de.uni_koblenz.jgralab.algolib.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

public class GraphVisitorComposition extends VisitorComposition implements
		GraphVisitor {

	@Override
	public void visitEdge(Edge e) {
		if (visitors != null) {
			for (Visitor currentVisitor : visitors) {
				if (currentVisitor instanceof GraphVisitor) {
					((GraphVisitor) currentVisitor).visitEdge(e);
				}
			}
		}
	}

	@Override
	public void visitVertex(Vertex v) {
		if (visitors != null) {
			for (Visitor currentVisitor : visitors) {
				if (currentVisitor instanceof GraphVisitor) {
					((GraphVisitor) currentVisitor).visitVertex(v);
				}
			}
		}
	}

}
