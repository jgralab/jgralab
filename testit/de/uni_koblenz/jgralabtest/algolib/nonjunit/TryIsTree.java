package de.uni_koblenz.jgralabtest.algolib.nonjunit;

import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.algolib.algorithms.weak_components.IsTree;
import de.uni_koblenz.jgralab.algolib.problems.IsTreeSolver;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleSchema;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleVertex;

public class TryIsTree {
	private static SimpleGraph getSmallGraph() {
		SimpleGraph graph = SimpleSchema.instance().createSimpleGraph(ImplementationType.STANDARD);
		SimpleVertex v1 = graph.createSimpleVertex();
		SimpleVertex v2 = graph.createSimpleVertex();
		SimpleVertex v3 = graph.createSimpleVertex();
		SimpleVertex v4 = graph.createSimpleVertex();
		SimpleVertex v5 = graph.createSimpleVertex();
		SimpleVertex v6 = graph.createSimpleVertex();
		SimpleVertex v7 = graph.createSimpleVertex();
		SimpleVertex v8 = graph.createSimpleVertex();

		graph.createSimpleEdge(v1, v2);
		graph.createSimpleEdge(v1, v3);
		graph.createSimpleEdge(v2, v4);
		graph.createSimpleEdge(v2, v5);
		graph.createSimpleEdge(v3, v6);
		graph.createSimpleEdge(v3, v7);
		graph.createSimpleEdge(v3, v8);

		graph.createSimpleEdge(v1, v8);
		return graph;
	}

	public static void main(String[] args) throws Exception {
		IsTreeSolver solver = new IsTree(getSmallGraph());
		System.out.println(solver.execute().isTree());

	}
}
