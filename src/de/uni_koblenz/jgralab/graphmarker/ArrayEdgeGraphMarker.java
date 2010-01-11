package de.uni_koblenz.jgralab.graphmarker;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

public class ArrayEdgeGraphMarker<O> extends ArrayGraphMarker<Edge, O> {

	public ArrayEdgeGraphMarker(Graph graph) {
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
	public O mark(Edge edge, O value) {
		return super.mark(edge.getNormalEdge(), value);
	}

}
