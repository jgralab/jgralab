package de.uni_koblenz.jgralabtest.algolib;

import de.uni_koblenz.jgralab.algolib.algorithms.search.ComputeLevelVisitor;
import de.uni_koblenz.jgralab.algolib.algorithms.search.ComputeNumberVisitor;
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
		RecursiveDepthFirstSearch dfs = new RecursiveDepthFirstSearch(graph);
		DebugSearchVisitor visitor = new DebugSearchVisitor();
		ComputeNumberVisitor visitor2 = new ComputeNumberVisitor();
		ComputeLevelVisitor visitor3 = new ComputeLevelVisitor();

		dfs.addDFSVisitor(visitor);
		dfs.addSearchVisitor(visitor2);
		dfs.addSearchVisitor(visitor3);

		dfs.solveTraversalFromVertex(v1);

		System.out.println("vertex order: \n" + dfs.getVertexOrder());
		System.out.println();
		System.out.println("edge order: \n" + dfs.getEdgeOrder());
		System.out.println();
		System.out.println("number: \n" + visitor2.getNumber());
		System.out.println();
		System.out.println("level: \n" + visitor3.getLevel());
		System.out.println();
		System.out.println(dfs.getState());
		System.out.println("Fini");
	}
}
