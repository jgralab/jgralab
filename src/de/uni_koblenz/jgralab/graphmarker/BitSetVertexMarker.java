package de.uni_koblenz.jgralab.graphmarker;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

public class BitSetVertexMarker extends BitSetGraphMarker<Vertex> {

	public BitSetVertexMarker(Graph graph) {
		super(graph);
	}

	@Override
	public void vertexDeleted(Vertex v) {
		removeMark(v);
	}

	@Override
	public void edgeDeleted(Edge e) {
		// do nothing
	}

}
