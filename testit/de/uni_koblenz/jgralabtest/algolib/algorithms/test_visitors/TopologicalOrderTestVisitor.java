package de.uni_koblenz.jgralabtest.algolib.algorithms.test_visitors;

import static org.junit.Assert.assertTrue;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.visitors.TopologicalOrderVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.graphmarker.BitSetVertexMarker;

public class TopologicalOrderTestVisitor extends TopologicalOrderVisitorAdapter {

	private BooleanFunction<Vertex> visited;

	@Override
	public void visitVertexInTopologicalOrder(Vertex v) {
		super.visitVertexInTopologicalOrder(v);
		if (visited == null) {
			visited = new BitSetVertexMarker(v.getGraph());
		}
		for (Edge incoming : v.incidences(EdgeDirection.IN)) {
			assertTrue("Not in topological order: " + v,
					visited.get(incoming.getAlpha()));
		}
		visited.set(v, true);
	}

	@Override
	public void reset() {
		super.reset();
		visited = null;
	}

}
