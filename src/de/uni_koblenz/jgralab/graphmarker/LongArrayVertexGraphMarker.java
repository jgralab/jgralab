package de.uni_koblenz.jgralab.graphmarker;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

public class LongArrayVertexGraphMarker extends LongArrayGraphMarker<Vertex> {

	protected LongArrayVertexGraphMarker(Graph graph) {
		super(graph, graph.getMaxVCount());
	}

	@Override
	public void edgeDeleted(Edge e) {
		// do nothing
	}

	@Override
	public void maxEdgeCountIncreased(int newValue) {
		// do nothing
	}

	@Override
	public void maxVertexCountIncreased(int newValue) {
		if (newValue > temporaryAttributes.length) {
			expand(newValue);
		}
	}

	@Override
	public void vertexDeleted(Vertex v) {
		removeMark(v);
	}

}
