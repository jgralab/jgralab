package de.uni_koblenz.jgralab.algolib.visitors;

import java.util.LinkedList;
import java.util.List;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

public class SimpleVisitorComposition extends SimpleVisitorAdapter implements
		SimpleVisitor {

	private List<SimpleVisitor> simpleVisitors;

	public SimpleVisitorComposition() {

	}

	private void createSimpleVisitorsLazily() {
		if (simpleVisitors == null) {
			simpleVisitors = new LinkedList<SimpleVisitor>();
		}
	}

	public void addSimpleVisitor(SimpleVisitor newVisitor) {
		createSimpleVisitorsLazily();
		simpleVisitors.add(newVisitor);
	}

	public void removeSimpleVisitor(SimpleVisitor toRemove) {
		if (simpleVisitors != null) {
			simpleVisitors.remove(toRemove);
			if (simpleVisitors.size() == 0) {
				clearSimpleVisitors();
			}
		}
	}

	public void clearSimpleVisitors() {
		simpleVisitors = null;
	}

	@Override
	public void visitEdge(Edge e) {
		if (simpleVisitors != null) {
			for (SimpleVisitor currentVisitor : simpleVisitors) {
				currentVisitor.visitEdge(e);
			}
		}
	}

	@Override
	public void visitVertex(Vertex v) {
		if (simpleVisitors != null) {
			for (SimpleVisitor currentVisitor : simpleVisitors) {
				currentVisitor.visitVertex(v);
			}
		}
	}

	@Override
	public void reset() {
		if (simpleVisitors != null) {
			for (SimpleVisitor currentVisitor : simpleVisitors) {
				currentVisitor.reset();
			}
		}
	}

}
