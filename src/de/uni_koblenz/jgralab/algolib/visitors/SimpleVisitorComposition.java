package de.uni_koblenz.jgralab.algolib.visitors;

import java.util.LinkedList;
import java.util.List;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

public class SimpleVisitorComposition extends SimpleVisitorAdapter {

	private List<SimpleVisitor> visitors;

	public SimpleVisitorComposition(SimpleVisitor initialVisitor) {
		visitors = new LinkedList<SimpleVisitor>();
		visitors.add(initialVisitor);
	}

	@Override
	public void visitEdge(Edge e) {
		for (SimpleVisitor currentVisitor : visitors) {
			currentVisitor.visitEdge(e);
		}
	}

	@Override
	public void visitVertex(Vertex v) {
		for (SimpleVisitor currentVisitor : visitors) {
			currentVisitor.visitVertex(v);
		}
	}

}
