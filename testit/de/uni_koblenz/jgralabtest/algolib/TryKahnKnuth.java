package de.uni_koblenz.jgralabtest.algolib;

import de.uni_koblenz.jgralab.algolib.algorithms.acyclicity.DFSImplementation;
import de.uni_koblenz.jgralab.algolib.algorithms.acyclicity.KahnKnuthAlgorithm;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleSchema;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleVertex;

public class TryKahnKnuth {
	public static void main(String[] args) {
		SimpleGraph graph = SimpleSchema.instance().createSimpleGraph();
		SimpleVertex[] vertices = new SimpleVertex[8];
		for (int i = 1; i < 8; i++) {
			vertices[i] = graph.createSimpleVertex();
		}
		graph.createSimpleEdge(vertices[1], vertices[5]);
		graph.createSimpleEdge(vertices[1], vertices[3]);
		graph.createSimpleEdge(vertices[2], vertices[3]);
		graph.createSimpleEdge(vertices[2], vertices[4]);
		graph.createSimpleEdge(vertices[3], vertices[5]);
		graph.createSimpleEdge(vertices[3], vertices[4]);
		graph.createSimpleEdge(vertices[4], vertices[7]);
		graph.createSimpleEdge(vertices[5], vertices[6]);
		graph.createSimpleEdge(vertices[6], vertices[7]);
		// graph.createSimpleEdge(vertices[7], vertices[3]);

		KahnKnuthAlgorithm solver = new KahnKnuthAlgorithm(graph);
		DFSImplementation solver2 = new DFSImplementation(graph);

		solver.execute();

		System.out.println(solver.isAcyclic());
		System.out.println(solver.getTopologicalOrder());

		solver2.execute();
		System.out.println(solver2.isAcyclic());
		System.out.println(solver2.getTopologicalOrder());
		
	}
}
