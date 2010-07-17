package de.uni_koblenz.jgralab.algolib.visitors;

import java.util.LinkedList;
import java.util.List;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

public class SimpleVisitorComposition extends SimpleVisitorAdapter implements
		SimpleVisitor {

	private List<SimpleVisitor> simpleVisitors;

	public SimpleVisitorComposition() {
		simpleVisitors = new LinkedList<SimpleVisitor>();
	}

	public void addSimpleVisitor(SimpleVisitor newVisitor) {
		simpleVisitors.add(newVisitor);
	}

	public void removeSimpleVisitor(SimpleVisitor toRemove) {
		simpleVisitors.remove(toRemove);
	}

	public void clearSimpleVisitors() {
		simpleVisitors.clear();
	}

	@Override
	public void visitEdge(Edge e) {
		for (SimpleVisitor currentVisitor : simpleVisitors) {
			currentVisitor.visitEdge(e);
		}
	}

	@Override
	public void visitVertex(Vertex v) {
		for (SimpleVisitor currentVisitor : simpleVisitors) {
			currentVisitor.visitVertex(v);
		}
	}

	@Override
	public void reset() {
		for (SimpleVisitor currentVisitor : simpleVisitors) {
			currentVisitor.reset();
		}
	}

}
