package de.uni_koblenz.jgralabtest.algolib;

import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.RecursiveDepthFirstSearch;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleSchema;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleVertex;

public class TryDFS {
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
		graph.createSimpleEdge(v1, v5);
		graph.createSimpleEdge(v3, v2);
		graph.createSimpleEdge(v2, v4);
		graph.createSimpleEdge(v3, v4);
		graph.createSimpleEdge(v4, v5);
		graph.createSimpleEdge(v3, v5);
		DepthFirstSearch dfs = new RecursiveDepthFirstSearch(graph).withLevel().withParent().withRorder();
		dfs.addVisitor(new DebugSearchVisitor());

		dfs.execute(v1);

		printResults(dfs);
		
		dfs.reset();
		dfs.execute();
		printResults(dfs);
		
		System.out.println("Fini");
	}

	private static void printResults(DepthFirstSearch dfs) {
		System.out.println("vertex order: \n" + dfs.getVertexOrder());
		System.out.println();
		System.out.println("rorder: \n" + dfs.getRorder());
		System.out.println();
		System.out.println("edge order: \n" + dfs.getEdgeOrder());
		System.out.println();
		System.out.println("number: \n" + dfs.getNumber());
		System.out.println();
		System.out.println("level: \n" + dfs.getLevel());
		System.out.println();
		System.out.println("parent: \n" + dfs.getParent());
		System.out.println();
		System.out.println(dfs.getState());
	}
}
