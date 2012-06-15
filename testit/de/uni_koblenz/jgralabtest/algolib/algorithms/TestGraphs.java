package de.uni_koblenz.jgralabtest.algolib.algorithms;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;
import de.uni_koblenz.jgralab.graphmarker.DoubleEdgeMarker;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleSchema;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleVertex;

public class TestGraphs {

	public static SimpleGraph getSimpleCyclicGraph() {
		SimpleGraph g = SimpleSchema.instance().createSimpleGraph(
				ImplementationType.STANDARD);
		SimpleVertex v1 = g.createSimpleVertex();
		SimpleVertex v2 = g.createSimpleVertex();
		SimpleVertex v3 = g.createSimpleVertex();
		SimpleVertex v4 = g.createSimpleVertex();
		g.createSimpleEdge(v1, v2);
		g.createSimpleEdge(v1, v3);
		g.createSimpleEdge(v1, v4);
		g.createSimpleEdge(v2, v4);
		g.createSimpleEdge(v3, v2);
		g.createSimpleEdge(v4, v1);
		return g;
	}

	public static SimpleGraph getSimpleAcyclicGraph() {
		SimpleGraph g = SimpleSchema.instance().createSimpleGraph(
				ImplementationType.STANDARD);
		SimpleVertex v1 = g.createSimpleVertex();
		SimpleVertex v2 = g.createSimpleVertex();
		SimpleVertex v3 = g.createSimpleVertex();
		SimpleVertex v4 = g.createSimpleVertex();
		g.createSimpleEdge(v1, v2);
		g.createSimpleEdge(v1, v3);
		g.createSimpleEdge(v1, v4);
		g.createSimpleEdge(v2, v4);
		g.createSimpleEdge(v3, v2);
		return g;
	}

	public static SimpleGraph getShortestPathTestGraph() {
		SimpleGraph g = SimpleSchema.instance().createSimpleGraph(
				ImplementationType.STANDARD);
		SimpleVertex v1 = g.createSimpleVertex();
		SimpleVertex v2 = g.createSimpleVertex();
		SimpleVertex v3 = g.createSimpleVertex();
		SimpleVertex v4 = g.createSimpleVertex();
		g.createSimpleEdge(v1, v2);
		g.createSimpleEdge(v2, v3);
		g.createSimpleEdge(v2, v4);
		g.createSimpleEdge(v3, v4);
		g.createSimpleEdge(v4, v1);
		return g;
	}

	public static SimpleGraph getReachabilityTestGraph() {
		SimpleGraph g = SimpleSchema.instance().createSimpleGraph(
				ImplementationType.STANDARD);
		SimpleVertex v1 = g.createSimpleVertex();
		SimpleVertex v2 = g.createSimpleVertex();
		SimpleVertex v3 = g.createSimpleVertex();
		SimpleVertex v4 = g.createSimpleVertex();
		SimpleVertex v5 = g.createSimpleVertex();
		SimpleVertex v6 = g.createSimpleVertex();

		g.createSimpleEdge(v1, v2);
		g.createSimpleEdge(v1, v3);

		g.createSimpleEdge(v2, v3);

		g.createSimpleEdge(v3, v4);

		g.createSimpleEdge(v5, v6);

		return g;
	}

	public static DoubleFunction<Edge> getWeightForReachabilityTestGraph(
			SimpleGraph g) {
		DoubleFunction<Edge> weight = new DoubleEdgeMarker(g);
		weight.set(g.getEdge(1), 10);
		weight.set(g.getEdge(2), 50);
		weight.set(g.getEdge(3), 20);
		weight.set(g.getEdge(4), 15);
		weight.set(g.getEdge(5), 10);
		return weight;
	}

}
