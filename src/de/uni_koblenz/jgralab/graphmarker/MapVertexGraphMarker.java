package de.uni_koblenz.jgralab.graphmarker;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

public class MapVertexGraphMarker<O> extends MapGraphMarker<Vertex, O> {

	public MapVertexGraphMarker(Graph g) {
		super(g);
	}

	@Override
	public void edgeDeleted(Edge e) {
		// do nothing

	}

	@Override
	public void vertexDeleted(Vertex v) {
		tempAttributeMap.remove(v);
	}

}
