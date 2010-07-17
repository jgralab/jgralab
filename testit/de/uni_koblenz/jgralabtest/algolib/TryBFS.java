package de.uni_koblenz.jgralabtest.algolib;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.ComputeLevelVisitor;
import de.uni_koblenz.jgralab.algolib.algorithms.search.ComputeNumberVisitor;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleSchema;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleVertex;

public class TryBFS {
	public static void main(String[] args) {
		SimpleGraph graph = SimpleSchema.instance().createSimpleGraph();
		SimpleVertex v1 = graph.createSimpleVertex();
		SimpleVertex v2 = graph.createSimpleVertex();
		SimpleVertex v3 = graph.createSimpleVertex();
		SimpleVertex v4 = graph.createSimpleVertex();
		SimpleVertex v5 = graph.createSimpleVertex();
		graph.createSimpleVertex();
		graph.createSimpleEdge(v1, v2);
		graph.createSimpleEdge(v1, v4);
		graph.createSimpleEdge(v2, v1);
		graph.createSimpleEdge(v1, v3);
//		graph.createSimpleEdge(v1, v5);
		graph.createSimpleEdge(v3, v2);
		graph.createSimpleEdge(v2, v4);
		graph.createSimpleEdge(v3, v4);
		graph.createSimpleEdge(v4, v5);
		graph.createSimpleEdge(v5, v3);
		BreadthFirstSearch bfs = new BreadthFirstSearch(graph);
		ComputeLevelVisitor levelVisitor = new ComputeLevelVisitor();
		DebugSearchVisitor debugSearchVisitor = new DebugSearchVisitor(levelVisitor);
		ComputeNumberVisitor numberVisitor = new ComputeNumberVisitor();
		
		
		bfs.addSearchVisitor(debugSearchVisitor);
		bfs.addSearchVisitor(numberVisitor);
		bfs.addSearchVisitor(new PauseVisitor());
		
		bfs.setSearchDirection(EdgeDirection.IN);
		
		bfs.solveTraversalFromVertex(v1);
		
		System.out.println("vertex order: \n" + bfs.getVertexOrder());
		System.out.println();
		System.out.println("edge order: \n" + bfs.getEdgeOrder());
		System.out.println();
		System.out.println("number: \n" + numberVisitor.getNumber());
		System.out.println();
		System.out.println("level: \n" + levelVisitor.getLevel());
		System.out.println();
		System.out.println(bfs.getState());
		System.out.println("Fini");
	}
}
