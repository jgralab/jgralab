package de.uni_koblenz.jgralab.algolib.algorithms.search.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.visitors.GraphVisitorComposition;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public class SearchVisitorComposition extends GraphVisitorComposition
		implements SearchVisitor {

	@Override
	public void visitFrond(Edge e) {
		if (visitors != null) {
			for (Visitor currentVisitor : visitors) {
				if (currentVisitor instanceof SearchVisitor) {
					((SearchVisitor) currentVisitor).visitFrond(e);
				}
			}
		}
	}

	@Override
	public void visitRoot(Vertex v) {
		if (visitors != null) {
			for (Visitor currentVisitor : visitors) {
				if (currentVisitor instanceof SearchVisitor) {
					((SearchVisitor) currentVisitor).visitRoot(v);
				}
			}
		}
	}

	@Override
	public void visitTreeEdge(Edge e) {
		if (visitors != null) {
			for (Visitor currentVisitor : visitors) {
				if (currentVisitor instanceof SearchVisitor) {
					((SearchVisitor) currentVisitor).visitTreeEdge(e);
				}
			}
		}
	}
}
