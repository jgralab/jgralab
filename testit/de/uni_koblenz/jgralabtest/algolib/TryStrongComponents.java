package de.uni_koblenz.jgralabtest.algolib;

import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.RecursiveDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.strong_components.StrongComponentsWithDFS;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleSchema;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleVertex;

public class TryStrongComponents {
	public static void main(String[] args) {
		SimpleGraph graph = SimpleSchema.instance().createSimpleGraph();
		int vertexCount = 7;
		SimpleVertex[] vertices = new SimpleVertex[vertexCount];
		for (int i = 1; i < vertexCount; i++) {
			vertices[i] = graph.createSimpleVertex();
		}

		graph.createSimpleEdge(vertices[1], vertices[2]);
		graph.createSimpleEdge(vertices[2], vertices[3]);
		graph.createSimpleEdge(vertices[3], vertices[4]);
		graph.createSimpleEdge(vertices[3], vertices[1]);	
		graph.createSimpleEdge(vertices[2], vertices[4]);
		graph.createSimpleEdge(vertices[1], vertices[5]);
		graph.createSimpleEdge(vertices[5], vertices[6]);
		graph.createSimpleEdge(vertices[6], vertices[3]);
		graph.createSimpleEdge(vertices[6], vertices[5]);
		graph.createSimpleEdge(vertices[1], vertices[6]);
		
		DepthFirstSearch dfs = new RecursiveDepthFirstSearch(graph);
		dfs.addVisitor(new DebugSearchVisitor());
		StrongComponentsWithDFS solver = new StrongComponentsWithDFS(graph, dfs);
		solver.execute();
		System.out.println(solver.getLowlink());
		System.out.println(solver.getStrongComponents());
	}
}
