package de.uni_koblenz.jgralab.graphmarker;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

public class BitSetEdgeGraphMarker extends BitSetGraphMarker<Edge> {

	public BitSetEdgeGraphMarker(Graph graph) {
		super(graph);
	}

	@Override
	public void edgeDeleted(Edge e) {
		removeMark(e.getNormalEdge());
	}

	@Override
	public void vertexDeleted(Vertex v) {
		// do nothing
	}

	@Override
	public boolean mark(Edge edge) {
		return super.mark(edge.getNormalEdge());
	}

}
