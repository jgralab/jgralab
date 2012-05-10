package de.uni_koblenz.jgralab;

public interface VertexFilter<V extends Vertex> {
	public boolean accepts(V vertex);
}
