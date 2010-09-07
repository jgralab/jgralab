package de.uni_koblenz.jgralab.algolib.algorithms.topological_order.visitors;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;
import de.uni_koblenz.jgralab.algolib.visitors.VisitorComposition;

public class TopologicalOrderVisitorComposition extends VisitorComposition
		implements TopologicalOrderVisitor {

	@Override
	public void addVisitor(Visitor visitor) {
		if (visitor instanceof TopologicalOrderVisitor) {
			super.addVisitor(visitor);
		} else {
			throw new IllegalArgumentException(
					"This visitor composition is only compatible with implementations of "
							+ TopologicalOrderVisitor.class.getSimpleName()
							+ ".");
		}
	}

	@Override
	public void visitVertexInTopologicalOrder(Vertex v) {
		if (visitors != null) {
			for (Visitor currentVisitor : visitors) {
				((TopologicalOrderVisitor) currentVisitor)
						.visitVertexInTopologicalOrder(v);
			}
		}
	}

}
