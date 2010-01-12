package de.uni_koblenz.jgralab.graphmarker;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

public class IntegerEdgeMarker extends IntegerGraphMarker<Edge> {

	public IntegerEdgeMarker(Graph graph) {
		super(graph, graph.getMaxECount() + 1);
	}

	@Override
	public void edgeDeleted(Edge e) {
		removeMark(e.getNormalEdge());
	}

	@Override
	public void maxEdgeCountIncreased(int newValue) {
		newValue++;
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

	@Override
	public int mark(Edge edge, int value) {
		return super.mark(edge.getNormalEdge(), value);
	}

}
