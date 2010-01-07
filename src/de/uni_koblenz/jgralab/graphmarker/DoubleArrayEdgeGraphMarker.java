package de.uni_koblenz.jgralab.graphmarker;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

public class DoubleArrayEdgeGraphMarker extends DoubleArrayGraphMarker<Edge> {

	public DoubleArrayEdgeGraphMarker(Graph graph) {
		super(graph, graph.getMaxECount());
	}

	@Override
	public void edgeDeleted(Edge e) {
		removeMark(e);
	}

	@Override
	public void maxEdgeCountIncreased(int newValue) {
		if (newValue > temporaryAttributes.length) {
			expand(newValue);
		}
	}

	@Override
	public void maxVertexCountIncreased(int newValue) {
		// do nothing
	}

	@Override
	public void vertexDeleted(Vertex v) {
		// do nothing
	}

}
