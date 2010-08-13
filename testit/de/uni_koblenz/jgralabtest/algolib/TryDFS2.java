package de.uni_koblenz.jgralabtest.algolib;

import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.RecursiveDepthFirstSearch;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleSchema;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleVertex;

public class TryDFS2 {
	public static void main(String[] args) {
		SimpleGraph graph = SimpleSchema.instance().createSimpleGraph(4, 6);
		SimpleVertex a = graph.createSimpleVertex();
		SimpleVertex b = graph.createSimpleVertex();
		SimpleVertex c = graph.createSimpleVertex();
		SimpleVertex d = graph.createSimpleVertex();
		
		graph.createSimpleEdge(a, b);
		graph.createSimpleEdge(a, c);
		graph.createSimpleEdge(a, d);
		
		graph.createSimpleEdge(b, d);
		
		graph.createSimpleEdge(c, b);
		
		graph.createSimpleEdge(d, a);
		
		DepthFirstSearch dfs = new RecursiveDepthFirstSearch(graph);
		dfs.addVisitor(new DebugSearchVisitor());
		
		dfs.execute();
	}
}
