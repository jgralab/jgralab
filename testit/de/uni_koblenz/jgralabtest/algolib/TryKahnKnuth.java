package de.uni_koblenz.jgralabtest.algolib;

import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.RecursiveDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.TopologicalOrderWithDFS;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.KahnKnuthAlgorithm;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleSchema;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleVertex;

public class TryKahnKnuth {
	
	public static SimpleGraph danielsGraph(){
		SimpleGraph graph = SimpleSchema.instance().createSimpleGraph();
		int vertexCount = 7;
		SimpleVertex[] vertices = new SimpleVertex[vertexCount];
		for (int i = 1; i < vertexCount; i++) {
			vertices[i] = graph.createSimpleVertex();
		}
		graph.createSimpleEdge(vertices[5], vertices[2]);
		graph.createSimpleEdge(vertices[2], vertices[3]);
		graph.createSimpleEdge(vertices[2], vertices[4]);
		graph.createSimpleEdge(vertices[4], vertices[1]);
		graph.createSimpleEdge(vertices[5], vertices[1]);
		graph.createSimpleEdge(vertices[6], vertices[3]);
		return graph;
	}
	
	public static SimpleGraph myGraph(){
		SimpleGraph graph = SimpleSchema.instance().createSimpleGraph();
		int vertexCount = 9;
		SimpleVertex[] vertices = new SimpleVertex[vertexCount];
		for (int i = 1; i < vertexCount; i++) {
			vertices[i] = graph.createSimpleVertex();
		}
		graph.createSimpleEdge(vertices[5], vertices[1]);
		graph.createSimpleEdge(vertices[3], vertices[1]);
		graph.createSimpleEdge(vertices[3], vertices[2]);
		graph.createSimpleEdge(vertices[4], vertices[2]);
		graph.createSimpleEdge(vertices[5], vertices[3]);
		graph.createSimpleEdge(vertices[4], vertices[3]);
		graph.createSimpleEdge(vertices[7], vertices[4]);
		graph.createSimpleEdge(vertices[6], vertices[5]);
		graph.createSimpleEdge(vertices[7], vertices[6]);
		graph.createSimpleEdge(vertices[1], vertices[8]);
		graph.createSimpleEdge(vertices[2], vertices[8]);
		return graph;
	}
	public static void main(String[] args) {
		
		SimpleGraph graph = danielsGraph();
		
		// graph.createSimpleEdge(vertices[7], vertices[3]);

		KahnKnuthAlgorithm solver = new KahnKnuthAlgorithm(graph);
		DepthFirstSearch dfs = new RecursiveDepthFirstSearch(graph);
		TopologicalOrderWithDFS solver2 = new TopologicalOrderWithDFS(graph, dfs);
		dfs.addVisitor(new DebugSearchVisitor());
		System.out.println("Kahn Knuth:");

		solver.execute();

		System.out.println(solver.isAcyclic());
		System.out.println(solver.getTopologicalOrder());

		System.out.println();
		System.out.println("DFS:");

		solver2.execute();

		System.out.println(solver2.isAcyclic());
		System.out.println(solver2.getTopologicalOrder());

	}
}
