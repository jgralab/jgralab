package de.uni_koblenz.jgralab.utilities.tg2xml;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

public abstract class GraphVisitor {
	protected Graph graph;

	public GraphVisitor(Graph graph) {
		super();
		this.graph = graph;
	}
	
	public void visitAll() throws Exception{
		// visit the graph
		preVisitor();
		
		// visit all vertices
		for(Vertex currentVertex : graph.vertices()){
			visitVertex(currentVertex);
		}
		
		// visit all edges
		for(Edge currentEdge : graph.edges()){
			visitEdge(currentEdge);
		}
		
		postVisitor();
	}
	
	protected abstract void preVisitor() throws Exception;
	protected abstract void visitVertex(Vertex v) throws Exception;
	protected abstract void visitEdge(Edge e) throws Exception;
	protected abstract void postVisitor() throws Exception;
}
