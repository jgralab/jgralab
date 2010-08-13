package de.uni_koblenz.jgralabtest.algolib;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleSchema;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleVertex;

public class TryBFS2 {
	public static void main(String[] args) {
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

		BreadthFirstSearch bfs = new BreadthFirstSearch(graph);
		bfs.setSearchDirection(EdgeDirection.IN);
		bfs.addVisitor(new DebugSearchVisitor());
		bfs.execute();
		
		System.out.println(bfs.getVertexOrder());
		
	}
}
