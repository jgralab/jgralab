package de.uni_koblenz.jgralab.graphmarker;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

public class BitSetEdgeGraphMarker extends BitSetGraphMarker<Edge> {

	protected BitSetEdgeGraphMarker(Graph graph) {
		super(graph);
	}

	@Override
	public void edgeDeleted(Edge e) {
		removeMark(e);
	}

	@Override
	public void vertexDeleted(Vertex v) {
		// do nothing
	}

}
