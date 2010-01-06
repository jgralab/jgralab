package de.uni_koblenz.jgralab.graphmarker;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

/**
 * This class is the generic vertex graph marker. It is used for temporary
 * attributes on vertices which can be of an arbitrary type.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ArrayGenericVertexGraphMarker<O> extends
		ArrayGenericGraphMarker<Vertex, O> {

	public ArrayGenericVertexGraphMarker(Graph graph) {
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
		unmark(v);
	}

}
