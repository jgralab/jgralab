package de.uni_koblenz.jgralabtest.algolib.algorithms;

import de.uni_koblenz.jgralab.ImplementationType;
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

}
