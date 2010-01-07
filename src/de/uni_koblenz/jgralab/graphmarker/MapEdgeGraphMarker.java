package de.uni_koblenz.jgralab.graphmarker;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

public class MapEdgeGraphMarker<O> extends MapGraphMarker<Edge, O> {

	public MapEdgeGraphMarker(Graph g) {
		super(g);
	}

	@Override
	public void edgeDeleted(Edge e) {
		tempAttributeMap.remove(e);
	}

	@Override
	public void vertexDeleted(Vertex v) {
		// do nothing
	}

}
