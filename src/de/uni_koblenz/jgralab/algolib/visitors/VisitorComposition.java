package de.uni_koblenz.jgralab.algolib.visitors;

import java.util.LinkedHashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;

public abstract class VisitorComposition implements
		Visitor {

	protected Set<Visitor> visitors;

	private void createVisitorsLazily() {
		if (visitors == null) {
			visitors = new LinkedHashSet<Visitor>();
		}
	}

	public void addVisitor(Visitor visitor) {
		createVisitorsLazily();
		visitors.add(visitor);
	}

	public void removeVisitor(Visitor visitor) {
		if (visitors != null) {
			visitors.remove(visitor);
			if (visitors.size() == 0) {
				clearVisitors();
			}
		}
	}

	public void clearVisitors() {
		visitors = null;
	}

	@Override
	public void reset() {
		if (visitors != null) {
			for (Visitor visitor : visitors) {
				visitor.reset();
			}
		}
	}

	@Override
	public void setAlgorithm(GraphAlgorithm alg) {
		if (visitors != null) {
			for (Visitor visitor : visitors) {
				visitor.setAlgorithm(alg);
			}
		}
	}
}
