package de.uni_koblenz.jgralab;

public interface TraversalContext {
	public boolean containsGraphElement(GraphElement e);

	public boolean containsVertex(Vertex v);

	public boolean containsEdge(Edge e);

	public int getVCount();

	public int getECount();
}
