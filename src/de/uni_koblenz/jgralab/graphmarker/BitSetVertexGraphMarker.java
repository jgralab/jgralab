package de.uni_koblenz.jgralab.graphmarker;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

public class BitSetVertexGraphMarker extends BitSetGraphMarker<Vertex> {

	public BitSetVertexGraphMarker(Graph graph) {
		super(graph);
	}

	@Override
	public void vertexDeleted(Vertex v) {
		unmark(v);
	}

	@Override
	public void edgeDeleted(Edge e) {
		// do nothing
	}

}
